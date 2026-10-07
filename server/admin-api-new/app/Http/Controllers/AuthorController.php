<?php

namespace App\Http\Controllers;

use Illuminate\Http\Request;
use App\Models\Author;
use App\Models\Book;
use Illuminate\Support\Facades\Validator;
use Illuminate\Support\Facades\Storage;
use Illuminate\Support\Str;
use Illuminate\Support\Facades\DB;
use Illuminate\Database\QueryException;
use Illuminate\Support\Facades\Log;
use Exception;

class AuthorController extends Controller
{
    /**
     * Affiche la liste des auteurs avec recherche optionnelle.
     *
     * @param Request $request
     * @return \Illuminate\Http\JsonResponse
     */
    public function index(Request $request)
    {
        try {
            if ($request->query('search')) {
                $search = $request->query('search');
                // Sécurisation : utilisation de whereRaw avec binding pour éviter l'injection SQL
                $authors = Author::whereRaw("CONCAT(firstName, ' ', name) LIKE ?", ["%$search%"])
                    ->orWhere('name', 'LIKE', "%$search%")
                    ->orWhere('firstName', 'LIKE', "%$search%")
                    ->get();
            } else {
                $authors = Author::all();
            }

            return response()->json($authors, 200);
        } catch (Exception $e) {
            Log::error('Erreur lors de la récupération des auteurs : ' . $e->getMessage(), [
                'trace' => $e->getTraceAsString()
            ]);
            return response()->json(['message' => 'Impossible de récupérer la liste des auteurs.'], 500);
        }
    }

    /**
     * Affiche un auteur spécifique par son ID.
     *
     * @param int $idAuthor
     * @return \Illuminate\Http\JsonResponse
     */
    public function show($idAuthor)
    {
        try {
            $author = Author::find($idAuthor);
            if (!$author) {
                return response()->json(['message' => 'Auteur non trouvé.'], 404);
            }
            // books_count (+ repartition par format) : necessaire a l'affichage
            // mobile ("Nombre de livres", widget "Mes livres electroniques / audios /
            // physiques") - absent du modele brut. Calcule cote serveur pour rester
            // exact quelle que soit la page chargee par /api/book/associations
            // (compter cote client depuis une seule page paginee serait faux).
            $authorData = $author->toArray();
            $authorData['books_count'] = Book::where('idAuthor', $idAuthor)->count();
            $authorData['books_count_electronic'] = Book::where('idAuthor', $idAuthor)->whereNotNull('electronic')->count();
            $authorData['books_count_physical'] = Book::where('idAuthor', $idAuthor)->where('isPhysic', 1)->count();
            $authorData['books_count_audio'] = Book::where('idAuthor', $idAuthor)->where('isAudio', 1)->count();
            return response()->json($authorData, 200);
        } catch (Exception $e) {
            Log::error('Erreur lors de l\'affichage de l\'auteur ID ' . $idAuthor . ' : ' . $e->getMessage(), [
                'trace' => $e->getTraceAsString()
            ]);
            return response()->json(['message' => 'Impossible de récupérer les informations de l\'auteur.'], 500);
        }
    }

    /**
     * Auteurs "similaires" à un auteur donné : auteurs ayant au moins un livre
     * dans une catégorie commune avec les livres de cet auteur, classés par
     * nombre de livres partageant une catégorie commune (pertinence).
     *
     * Remplace l'ancienne logique (AuthorSimular.php), qui renvoyait en réalité
     * les 10 auteurs au "level" le plus élevé - identique pour tout auteur
     * consulté, sans aucun rapport avec ses livres ou catégories réels.
     *
     * @param int $idAuthor
     * @return \Illuminate\Http\JsonResponse
     */
    public function similar($idAuthor)
    {
        try {
            $author = Author::find($idAuthor);
            if (!$author) {
                return response()->json(['message' => 'Auteur non trouvé.'], 404);
            }

            $categoryIds = DB::table('BookCategory')
                ->whereIn('idBook', DB::table('Book')->where('idAuthor', $idAuthor)->pluck('idBook'))
                ->pluck('idCategory')
                ->unique();

            if ($categoryIds->isEmpty()) {
                // Aucune catégorie connue pour les livres de cet auteur : pas de base
                // de comparaison pertinente -> liste vide plutôt que des suggestions
                // arbitraires (cf. audit mobile "Auteurs").
                return response()->json([], 200);
            }

            $bookIdsInSharedCategories = DB::table('BookCategory')
                ->whereIn('idCategory', $categoryIds)
                ->pluck('idBook')
                ->unique();

            // Un auteur par nombre de livres (distincts) partageant une catégorie
            // avec l'auteur consulté - c'est le signal de pertinence principal.
            $sharedCounts = DB::table('Book')
                ->select('idAuthor', DB::raw('COUNT(DISTINCT idBook) as shared_books_count'))
                ->whereIn('idBook', $bookIdsInSharedCategories)
                ->where('idAuthor', '!=', $idAuthor)
                ->whereNotNull('idAuthor')
                ->groupBy('idAuthor')
                ->orderByDesc('shared_books_count')
                ->limit(10)
                ->pluck('shared_books_count', 'idAuthor');

            if ($sharedCounts->isEmpty()) {
                return response()->json([], 200);
            }

            $booksCounts = DB::table('Book')
                ->select('idAuthor', DB::raw('COUNT(*) as cnt'))
                ->whereIn('idAuthor', $sharedCounts->keys())
                ->groupBy('idAuthor')
                ->pluck('cnt', 'idAuthor');

            $authors = Author::whereIn('idAuthor', $sharedCounts->keys())
                ->get(['idAuthor', 'name', 'firstName', 'profile']);

            $result = $authors->map(function ($a) use ($sharedCounts, $booksCounts) {
                return [
                    'idAuthor'           => $a->idAuthor,
                    'name'               => $a->name,
                    'firstName'          => $a->firstName,
                    'profile'            => $a->profile,
                    'books_count'        => $booksCounts[$a->idAuthor] ?? 0,
                    'shared_books_count' => $sharedCounts[$a->idAuthor] ?? 0,
                ];
            })->sortByDesc('shared_books_count')->values();

            return response()->json($result, 200);
        } catch (Exception $e) {
            Log::error('Erreur lors du calcul des auteurs similaires pour l\'auteur ID ' . $idAuthor . ' : ' . $e->getMessage(), [
                'trace' => $e->getTraceAsString()
            ]);
            return response()->json(['message' => 'Impossible de récupérer les auteurs similaires.'], 500);
        }
    }

    /**
     * Crée un nouvel auteur.
     *
     * @param Request $request
     * @return \Illuminate\Http\JsonResponse
     */
    public function store(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'fullName'   => 'required|string|max:512',
            'profile'    => 'nullable|file|mimes:jpg,jpeg,png|max:8000', // 8 Mo
            'level'      => 'nullable|integer',
            'profession' => 'nullable|string|max:1000',
            'biography'  => 'nullable|string',
        ], [
            'fullName.required' => 'Le nom complet est obligatoire.',
            'profile.mimes'     => 'La photo de profil doit être au format JPG ou PNG.',
            'profile.max'       => 'La photo de profil ne peut dépasser 8 Mo.',
            'level.integer'     => 'Le niveau doit être un nombre entier.',
            'profession.max'    => 'La profession ne peut dépasser 1000 caractères.',
        ]);

        if ($validator->fails()) {
            return response()->json([
                'message' => $validator->errors()->first(),
                'errors'  => $validator->errors(),
            ], 422);
        }

        DB::beginTransaction();
        try {
            $fullName = trim($request->input('fullName'));
            $parts = explode(' ', $fullName);
            $firstName = array_shift($parts);
            $name = implode(' ', $parts);

            $data = [
                'name'       => $name,
                'firstName'  => $firstName,
                'level'      => $request->filled('level') ? (int)$request->input('level') : 0,
                'profession' => $request->filled('profession') ? trim($request->input('profession')) : null,
                'biography'  => $request->filled('biography') ? trim($request->input('biography')) : null,
            ];

            // Gestion du fichier de profil
            if ($request->hasFile('profile')) {
                $file = $request->file('profile');
                if (!$file->isValid()) {
                    return response()->json(['message' => 'Le fichier de profil est invalide.'], 400);
                }

                $extension = $file->getClientOriginalExtension();
                $filename = 'author_' . Str::uuid()->toString() . '.' . $extension;

                // Stockage dans le disque 'private' (à adapter selon votre configuration)
                $stored = Storage::disk('private')->putFileAs('profils', $file, $filename);
                if ($stored === false) {
                    throw new Exception('Échec de l\'enregistrement du fichier');
                }

                $data['profile'] = $filename;
            } else {
                $data['profile'] = 'user.png';
            }

            $author = Author::create($data);

            DB::commit();
            return response()->json([
                'message' => 'Auteur créé avec succès.',
                'data'    => $author,
            ], 201);

        } catch (QueryException $qe) {
            DB::rollBack();
            Log::error('Erreur SQL lors de la création d\'un auteur : ' . $qe->getMessage(), [
                'data' => $data ?? [],
                'trace' => $qe->getTraceAsString()
            ]);
            return response()->json(['message' => 'Erreur de base de données lors de la création de l\'auteur.'], 500);
        } catch (Exception $e) {
            DB::rollBack();
            Log::error('Erreur inattendue lors de la création d\'un auteur : ' . $e->getMessage(), [
                'data' => $data ?? [],
                'trace' => $e->getTraceAsString()
            ]);
            return response()->json(['message' => 'Une erreur est survenue lors de la création de l\'auteur.'], 500);
        }
    }

    /**
     * Met à jour un auteur existant.
     *
     * @param Request $request
     * @param int $idAuthor
     * @return \Illuminate\Http\JsonResponse
     */
    public function update(Request $request, $idAuthor)
    {
        try {
            $author = Author::find($idAuthor);
            if (!$author) {
                return response()->json(['message' => 'Auteur non trouvé.'], 404);
            }
        } catch (Exception $e) {
            Log::error('Erreur lors de la recherche de l\'auteur ID ' . $idAuthor . ' : ' . $e->getMessage());
            return response()->json(['message' => 'Impossible de localiser l\'auteur.'], 500);
        }

        $validator = Validator::make($request->all(), [
            'fullName'   => 'sometimes|required|string|max:512',
            'profile'    => 'nullable|file|mimes:jpg,jpeg,png|max:5120',
            'level'      => 'nullable|integer',
            'profession' => 'nullable|string|max:1000',
            'biography'  => 'nullable|string',
        ], [
            'fullName.required' => 'Le nom complet est obligatoire.',
            'profile.mimes'     => 'La photo de profil doit être JPG ou PNG.',
            'profile.max'       => 'La photo de profil ne peut dépasser 5 Mo.',
            'level.integer'     => 'Le niveau doit être un nombre entier.',
            'profession.max'    => 'La profession ne peut dépasser 1000 caractères.',
        ]);

        if ($validator->fails()) {
            return response()->json([
                'message' => 'Validation échouée.',
                'errors'  => $validator->errors(),
            ], 422);
        }

        DB::beginTransaction();
        try {
            $updatedFields = [];

            if ($request->filled('fullName')) {
                $fullName = trim($request->input('fullName'));
                $parts = explode(' ', $fullName);
                $firstName = array_shift($parts);
                $name = implode(' ', $parts);

                $updatedFields['name'] = $name;
                $updatedFields['firstName'] = $firstName;
            }
            if ($request->filled('level')) {
                $updatedFields['level'] = (int)$request->input('level');
            }
            if ($request->filled('profession')) {
                $updatedFields['profession'] = trim($request->input('profession'));
            }
            if ($request->filled('biography')) {
                $updatedFields['biography'] = trim($request->input('biography'));
            }

            // Gestion du nouveau fichier de profil
            if ($request->hasFile('profile')) {
                $file = $request->file('profile');
                if (!$file->isValid()) {
                    return response()->json(['message' => 'Le fichier de profil est invalide.'], 400);
                }

                // Suppression de l'ancien fichier s'il existe
                if ($author->profile && $author->profile !== 'user.png') {
                    $oldPath = 'profils/' . $author->profile;
                    if (Storage::disk('private')->exists($oldPath)) {
                        Storage::disk('private')->delete($oldPath);
                    }
                }

                $extension = $file->getClientOriginalExtension();
                $filename = 'author_' . Str::uuid()->toString() . '.' . $extension;
                $stored = Storage::disk('private')->putFileAs('profils', $file, $filename);
                if ($stored === false) {
                    throw new Exception('Échec de l\'enregistrement du nouveau fichier de profil');
                }

                $updatedFields['profile'] = $filename;
            }

            if (!empty($updatedFields)) {
                $author->update($updatedFields);
            }

            DB::commit();
            return response()->json([
                'message' => 'Auteur mis à jour avec succès.',
                'data'    => $author->fresh(),
            ], 200);

        } catch (QueryException $qe) {
            DB::rollBack();
            Log::error('Erreur SQL lors de la mise à jour de l\'auteur ID ' . $idAuthor . ' : ' . $qe->getMessage(), [
                'updatedFields' => $updatedFields ?? [],
                'trace' => $qe->getTraceAsString()
            ]);
            return response()->json(['message' => 'Erreur de base de données lors de la mise à jour de l\'auteur.'], 500);
        } catch (Exception $e) {
            DB::rollBack();
            Log::error('Erreur inattendue lors de la mise à jour de l\'auteur ID ' . $idAuthor . ' : ' . $e->getMessage(), [
                'updatedFields' => $updatedFields ?? [],
                'trace' => $e->getTraceAsString()
            ]);
            return response()->json(['message' => 'Une erreur est survenue lors de la mise à jour de l\'auteur.'], 500);
        }
    }

    /**
     * Supprime un auteur ainsi que son fichier de profil associé.
     *
     * @param int $idAuthor
     * @return \Illuminate\Http\JsonResponse
     */
    public function destroy($idAuthor)
    {
        try {
            $author = Author::find($idAuthor);
            if (!$author) {
                return response()->json(['message' => 'Auteur non trouvé.'], 404);
            }
        } catch (Exception $e) {
            Log::error('Erreur lors de la recherche de l\'auteur ID ' . $idAuthor . ' : ' . $e->getMessage());
            return response()->json(['message' => 'Impossible de localiser l\'auteur.'], 500);
        }

        DB::beginTransaction();
        try {
            // Suppression du fichier de profil s'il existe
            if ($author->profile && $author->profile !== 'user.png') {
                $filePath = 'profils/' . $author->profile;
                if (Storage::disk('private')->exists($filePath)) {
                    Storage::disk('private')->delete($filePath);
                }
            }

            $author->delete();
            DB::commit();
            return response()->json(['message' => 'Auteur supprimé avec succès.'], 200);
        } catch (QueryException $qe) {
            DB::rollBack();
            Log::error('Erreur SQL lors de la suppression de l\'auteur ID ' . $idAuthor . ' : ' . $qe->getMessage(), [
                'trace' => $qe->getTraceAsString()
            ]);
            return response()->json(['message' => 'Erreur de base de données lors de la suppression de l\'auteur.'], 500);
        } catch (Exception $e) {
            DB::rollBack();
            Log::error('Erreur inattendue lors de la suppression de l\'auteur ID ' . $idAuthor . ' : ' . $e->getMessage(), [
                'trace' => $e->getTraceAsString()
            ]);
            return response()->json(['message' => 'Une erreur est survenue lors de la suppression de l\'auteur.'], 500);
        }
    }
}