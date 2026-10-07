<?php

namespace App\Http\Controllers;

use Illuminate\Http\Request;
use Illuminate\Database\Eloquent\Factories\HasFactory;
use App\Models\Structure;
use App\Models\StructBook;
use App\Models\StructAgent;
use App\Models\StructUser;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Log;
use Illuminate\Support\Carbon;
use Illuminate\Validation\Rule;
use Illuminate\Support\Facades\Storage;
class StructureController extends Controller
{
    //
    use HasFactory;
    public function store(Request $request)
    {
        $validated = $request->validate([
            'nameStruct' => 'required|string|max:255|unique:Structure,nameStruct',
            'description' => 'nullable|string|max:10000',
            'logo' => 'nullable|image|max:2048', //  max
            'banner' => 'nullable|image|max:2048',
            'location' => 'nullable|string|max:255', // Ajout de la validation pour l'emplacement
            // Ajout de la validation pour la limite de stockage et conversion string -> integer
            'storageLimit' => 'nullable|integer|min:0', // Limite de stockage en octets
            
        ]);
        // Gestion de creation des dossiers de la structure
        $idStruct = Structure::max('id') + 1; // Prochain ID
        $folders = ['blankets', 'pdfs', 'audios','banners','logos','backups'];
        foreach ($folders as $folder) {
            $path = 'structures/'.$idStruct.'/'.$folder;
            if (!Storage::disk('private')->exists($path)) {
                Storage::disk('private')->makeDirectory($path);
            }
        }

        // Gestion du logo
        $logoName = null;
        if ($request->hasFile('logo')) {
            $extension = $request->file('logo')->getClientOriginalExtension();
            $logoName = 'logo_' . time() . '.' . $extension;

            if (Storage::disk('private')->putFileAs('structures/'.$idStruct.'/logos', $request->file('logo'), $logoName)===false) {
                return response()->json([
                    'message' => 'Échec lors de l’enregistrement du logo.'
                ], 500);
            }
            // mettre en miniscule le nom du logo pour éviter les problèmes de casse sur certains systèmes de fichiers
            $publicLogo =  strtolower($request->nameStruct).'.'.$extension;
            if (Storage::disk('public')->putFileAs('logos', $request->file('logo'), $publicLogo)===false) {
                return response()->json([
                    'message' => 'Échec lors de l’enregistrement du logo.'
                ], 500);
            }
            
            
            
            //$logoPath = $request->file('logo')->store('structures/logos', 'public');
        }

        // Gestion de la bannière
        $bannerName = null;
        if ($request->hasFile('banner')) {
            $extension = $request->file('banner')->getClientOriginalExtension();
            $bannerName = 'banner_' . time() . '.' . $extension;
            if (Storage::disk('private')->putFileAs('structures/'.$idStruct.'/banners', $request->file('banner'), $bannerName)===false) {
                return response()->json([
                    'message' => 'Échec lors de l’enregistrement de la bannière.'
                ], 500);
            }
            
            //$bannerPath = $request->file('banner')->store('structures/banners', 'public');
        }

        // Création de la structure
        $structure = Structure::create([
            //forcer l'id pour correspondre au dossier créé, sinon on risque d'avoir des problèmes de synchronisation entre la base de données et le système de fichiers       
            'id' => $idStruct,
            'nameStruct' => $validated['nameStruct'],
            'description' => $validated['description'] ?? null,
            'logo' => $logoName ?? "eduniger.svg",
            'banner' => $bannerName ?? 'banner.svg', // Valeur par défaut
            'adhererNumber' => 0,
            'bookNumber' => 0,
            'location' => $validated['location'] ?? null, // Enregistrement de l'emplacement
            'storageLimit' => $validated['storageLimit'] ?? null,
        ]);

        return response()->json([
            'message' => 'Structure créée avec succès',
            'structure' => $structure
        ], 201);
    }
    public function getBooksByStructure($id)
    {
        $books = StructBook::where('idStruct', $id)->with('book')->get();
        return response()->json($books);
    }
    public function getAgentsByStructure($id)
    {
        $agents = StructAgent::where('idStruct', $id)->with('agent')->get();
        return response()->json($agents);
    }
    
    public function countBooksInStructure($id)
    {
        $count = StructBook::where('idStruct', $id)->count();
        return response()->json(['count' => $count]);
    }
    /**
     * Liste des structures, avec pour chacune un champ 'isAdhere' calcule
     * pour l'utilisateur authentifie courant (appartenance StructUser).
     * Avant ce correctif, l'app mobile recevait des Structure "nues" (aucun
     * champ d'appartenance) et affectait systematiquement isAdhere=false cote
     * client (StructureFragment/HomeFragment) : un utilisateur qui venait
     * d'adherer via /join voyait son adhesion "revenir en arriere" des que la
     * liste etait rechargee, alors que /join avait bien persiste en base.
     */
    public function index(Request $request)
    {
        $structures = Structure::all();

        $idUser = optional($request->user())->idUser;
        $joinedIds = $idUser
            ? StructUser::where('idUser', $idUser)->pluck('idStruct')->map(fn ($id) => (string) $id)->all()
            : [];

        $structures = $structures->map(function (Structure $structure) use ($joinedIds) {
            $structure->isAdhere = in_array((string) $structure->id, $joinedIds, true);
            return $structure;
        });

        return response()->json($structures);
    }
    public function show(Request $request, $id)
    {
        

        $structure = Structure::find($id);
        
        if (!$structure) {
            return response()->json(['error' => 'Structure not found'], 404);
        }

        $idUser = optional($request->user())->idUser;
        $isAdhere = $idUser
            ? StructUser::where('idStruct', $id)->where('idUser', $idUser)->exists()
            : false;

        return response()->json([
            'id' => $structure->id,
            'nameStruct' => $structure->nameStruct,
            'description' => $structure->description,
            'logo' => $structure->logo,
            'banner' => $structure->banner,
            'adhererNumber' => $structure->adhererNumber,
            'bookNumber' => $structure->bookNumber,
            'location' => $structure->location,
            'isAdhere' => $isAdhere,
            'created_at' => $structure->created_at,
            'updated_at' => $structure->updated_at,
            // Ne renvoyer que les données nécessaires
        ]);
    }

    /**
     * "Mes structures" : structures dont l'utilisateur courant est membre
     * (table StructUser), avec son statut isAdmin pour chacune. Remplace
     * l'ancien structure.php (deja corrige cote legacy pour prendre
     * l'identite du token verifie plutot qu'un champ envoye par le client).
     * Utilise par SearchActivity (onglet Structures) pour la section
     * "Mes structures".
     */
    public function mine(Request $request)
    {
        $idUser = $request->user()->idUser;

        $structures = Structure::query()
            ->join('StructUser', 'Structure.id', '=', 'StructUser.idStruct')
            ->where('StructUser.idUser', $idUser)
            ->select('Structure.*', 'StructUser.isAdmin as struct_user_is_admin')
            ->get();

        $structures = $structures->map(function (Structure $structure) {
            $structure->isAdhere = true;
            $structure->isAdmin = $structure->struct_user_is_admin ? 1 : 0;
            unset($structure->struct_user_is_admin);
            return $structure;
        });

        return response()->json($structures);
    }

    /**
     * Structures a decouvrir : structures dont l'utilisateur courant n'est
     * PAS membre, limitees a 4 comme l'ancien StructureMore.php. Utilise par
     * SearchActivity (onglet Structures) pour la section "A decouvrir".
     */
    public function discover(Request $request)
    {
        $idUser = $request->user()->idUser;

        $joinedIds = StructUser::where('idUser', $idUser)->pluck('idStruct');

        $structures = Structure::whereNotIn('id', $joinedIds)->limit(4)->get();

        $structures = $structures->map(function (Structure $structure) {
            $structure->isAdhere = false;
            $structure->isAdmin = 0;
            return $structure;
        });

        return response()->json($structures);
    }

    /**
     * Adhesion en libre-service : l'utilisateur authentifie rejoint une structure.
     * Remplace l'ancien adherer_struct.php (deja corrige cote legacy pour prendre
     * l'identite du token verifie plutot qu'un champ envoye par le client).
     * Pas de middleware structure.scope ici : n'importe quel lecteur authentifie
     * peut adherer a une structure, ce n'est pas reserve aux agents/admins de
     * cette structure.
     *
     * @param Request $request
     * @param int $id
     * @return \Illuminate\Http\JsonResponse
     */
    public function join(Request $request, $id)
    {
        $structure = Structure::find($id);
        if (!$structure) {
            return response()->json(['message' => 'Structure introuvable.'], 404);
        }

        $idUser = $request->user()->idUser;

        $already = StructUser::where('idStruct', $id)->where('idUser', $idUser)->exists();
        if ($already) {
            return response()->json(['message' => 'Vous êtes déjà membre de cette structure.'], 409);
        }

        try {
            DB::transaction(function () use ($id, $idUser, $structure) {
                StructUser::create([
                    'idStruct'       => $id,
                    'idUser'         => $idUser,
                    'date'           => now(),
                    'isAdmin'        => false,
                    'registerNumber' => null,
                ]);
                $structure->adhererNumber = (int) $structure->adhererNumber + 1;
                $structure->save();
            });

            return response()->json(['message' => 'Adhésion réussie.'], 201);
        } catch (\Exception $e) {
            Log::error('Erreur lors de l\'adhesion a la structure ' . $id . ' : ' . $e->getMessage());
            return response()->json(['message' => 'Une erreur est survenue.'], 500);
        }
    }

    /**
     * Retrait en libre-service : l'utilisateur authentifie quitte une structure
     * dont il est membre. Remplace l'ancien detach_struct.php (meme principe de
     * securite que join() ci-dessus).
     *
     * @param Request $request
     * @param int $id
     * @return \Illuminate\Http\JsonResponse
     */
    public function leave(Request $request, $id)
    {
        $structure = Structure::find($id);
        if (!$structure) {
            return response()->json(['message' => 'Structure introuvable.'], 404);
        }

        $idUser = $request->user()->idUser;

        $membership = StructUser::where('idStruct', $id)->where('idUser', $idUser)->first();
        if (!$membership) {
            return response()->json(['message' => 'Vous n\'êtes pas membre de cette structure.'], 404);
        }

        try {
            DB::transaction(function () use ($membership, $structure) {
                $membership->delete();
                $structure->adhererNumber = max(0, (int) $structure->adhererNumber - 1);
                $structure->save();
            });

            return response()->json(['message' => 'Structure quittée avec succès.'], 200);
        } catch (\Exception $e) {
            Log::error('Erreur lors du retrait de la structure ' . $id . ' : ' . $e->getMessage());
            return response()->json(['message' => 'Une erreur est survenue.'], 500);
        }
    }

    public function update(Request $request, $id)
    {
        $structure = Structure::findOrFail($id);

        $validated = $request->validate([
            'nameStruct' => [
                'required',
                'string',
                'max:255',
                function ($attribute, $value, $fail) use ($structure) {
                    $exists = Structure::where('nameStruct', $value)
                                ->where('id', '!=', $structure->id)
                                ->exists();
                    
                    if ($exists) {
                        $fail('Ce nom de structure est déjà utilisé.');
                    }
                },
            ],
            'description' => 'nullable|string|max:10000',
            'logo' => 'nullable|image|max:2048',
            'banner' => 'nullable|image|max:2048',
            'location' => 'nullable|string|max:255',
            'storageLimit' => 'nullable|integer|min:0', // Limite de stockage en octets
        ]);
        
        // Gestion du logo
        if ($request->hasFile('logo')) {
            // Supprimer l'ancien logo s'il existe

            if ($structure->logo && Storage::disk('private')->exists($structure->logo)) {
                Storage::disk('private')->delete($structure->logo);

                // Recuperer le nom du logo public pour le supprimer également 
                $publicLogo =  strtolower($request->nameStruct).'.'.$structure->logo->split('.')->last();
                if (Storage::disk('public')->exists($publicLogo)) {
                    Storage::disk('public')->delete($publicLogo);
                }
            }
            //$logoPath = $request->file('logo')->store('structures/logos', 'public');
            $extension = $request->file('logo')->getClientOriginalExtension();
            $logoname = 'logo_' . time() . '.' . $extension;
            if (Storage::disk('private')->putFileAs('structures/'.$id.'/logos', $request->file('logo'), $logoname)===false) {
                return response()->json([
                    'message' => 'Échec lors de l’enregistrement du logo.'
                ], 500);
                
                $publicLogo =  strtolower($structure->nameStruct).'.'.$extension;
                if (Storage::disk('public')->putFileAs('logos', $request->file('logo'), $publicLogo)===false) {
                    return response()->json([
                        'message' => 'Échec lors de l’enregistrement du logo.'
                    ], 500);
                }
            }
            $structure->logo = $logoname;
        }
        
        // Gestion de la bannière
        if ($request->hasFile('banner')) {
            // Supprimer l'ancienne bannière si elle existe
            if ($structure->banner && Storage::disk('private')->exists($structure->banner)) {
                Storage::disk('private')->delete($structure->banner);
            }
            
            //$bannerPath = $request->file('banner')->store('structures/banners', 'public');
            $extension = $request->file('banner')->getClientOriginalExtension();
            $bannerName = 'banner_' . time() . '.' . $extension;
            if (Storage::disk('private')->putFileAs('structures/'.$id.'/banners', $request->file('banner'), $bannerName)===false) {
                return response()->json([
                    'message' => 'Échec lors de l’enregistrement de la bannière.'
                ], 500);
            }
            $structure->banner = $bannerName;
        }
        
        // Mise à jour des champs texte
        $structure->nameStruct = $validated['nameStruct'];
        $structure->location = $validated['location'] ?? null;
        $structure->storageLimit = $validated['storageLimit'] ?? null;
        $structure->description = $validated['description'] ?? null;
        $structure->save();
        
        return response()->json([
            'message' => 'Structure mise à jour avec succès',
            'structure' => $structure
        ]);
    }

    public function destroy($id)
    {
        try {
            DB::beginTransaction();

            // 1. Trouver la structure
            $structure = Structure::findOrFail($id);

            // 2. Supprimer les agents associés
            StructAgent::where('idStruct', $id)->delete();

            // 3. Supprimer les associations de livres (StructBook)
            DB::table('StructBook')->where('idStruct', $id)->delete();

            // 4. Supprimer la structure elle-même
            $structure->delete();

            if (Storage::disk('private')->exists('structures/'.$id)) {
                Storage::disk('private')->deleteDirectory('structures/'.$id);
            }
            DB::commit();
            


            return response()->json(['message' => 'Structure supprimée avec succès']);

        } catch (\Exception $e) {
            DB::rollBack();
            return response()->json([
                'error' => 'Échec de la suppression de la structure',
                'details' => $e->getMessage()
            ], 500);
        }
    }
}
