<?php

namespace App\Http\Controllers;

use App\Models\StructUser;
use App\Models\User; 
use Illuminate\Http\Request;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Log;
use Illuminate\Support\Facades\Validator;
use Illuminate\Validation\Rule;

/**
 * Class StructUserController
 *
 * Gestion des associations entre utilisateurs et structures.
 *
 * Ce contrôleur permet de :
 * - Lister les utilisateurs d'une structure
 * - Ajouter/supprimer un utilisateur d'une structure
 * - Mettre à jour le rôle (admin) d'un utilisateur dans une structure
 * - Vérifier l'appartenance d'un utilisateur à une structure
 * - Rechercher des utilisateurs (déjà membres ou disponibles)
 *
 * Toutes les méthodes nécessitent une authentification (middleware 'auth:sanctum').
 * La validation des autorisations (admin de structure) est à implémenter selon les besoins.
 */
class StructUserController extends Controller
{
    /**
     * Liste paginée des associations (utile pour l'administration).
     *
     * @param Request $request
     * @return \Illuminate\Http\JsonResponse
     */
    public function index(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'per_page' => 'nullable|integer|min:1|max:100',
            'idStruct' => 'nullable|integer|exists:Structure,id',
            'search'   => 'nullable|string|max:255',
        ]);

        if ($validator->fails()) {
            return response()->json([
                'message' => 'Paramètres invalides',
                'errors'  => $validator->errors(),
            ], 422);
        }

        $query = StructUser::with('user:idUser,firstName,name,email'); // Eager loading des infos utilisateur

        if ($request->has('idStruct')) {
            $query->where('idStruct', $request->idStruct);
        }

        if ($request->has('search')) {
            $search = $request->search;
            $query->whereHas('user', function ($q) use ($search) {
                $q->where('firstName', 'LIKE', "%{$search}%")
                  ->orWhere('name', 'LIKE', "%{$search}%")
                  ->orWhere('email', 'LIKE', "%{$search}%")
                  ->orWhere('idUser', 'LIKE', "%{$search}%");
            });
        }

        $perPage = $request->get('per_page', 20);
        $structUsers = $query->orderBy('created_at', 'desc')->paginate($perPage);

        return response()->json($structUsers);
    }

    /**
     * Récupère tous les utilisateurs appartenant à une structure donnée.
     *
     * @param Request $request
     * @param int $idStruct
     * @return \Illuminate\Http\JsonResponse
     */
    public function getUsersByStructure(Request $request, $idStruct)
    {
        // Vérifier que la structure existe (optionnel mais recommandé)
        // $structure = Structure::find($idStruct);
        // if (!$structure) {
        //     return response()->json(['message' => 'Structure introuvable'], 404);
        // }

        $query = StructUser::where('idStruct', $idStruct)
            ->with('user:idUser,firstName,name,email,phoneNumber');

        // Recherche optionnelle
        if ($request->has('search')) {
            $search = $request->search;
            $query->whereHas('user', function ($q) use ($search) {
                $q->where('firstName', 'LIKE', "%{$search}%")
                  ->orWhere('name', 'LIKE', "%{$search}%")
                  ->orWhere('email', 'LIKE', "%{$search}%")
                  ->orWhere('idUser', 'LIKE', "%{$search}%");
            });
        }

        $users = $query->get()->map(function ($structUser) {
            // Formatage des données pour le frontend
            return [
                'idUser'      => $structUser->idUser,
                'firstName'   => $structUser->user->firstName ?? '',
                'name'        => $structUser->user->name ?? '',
                'email'       => $structUser->user->email ?? '',
                'phoneNumber' => $structUser->user->phoneNumber ?? '',
                'isAdmin'     => $structUser->isAdmin,
                'registerNumber' => $structUser->registerNumber,
                'date'        => $structUser->date,
            ];
        });

        return response()->json($users);
    }

    /**
     * Vérifie si un utilisateur appartient à une structure.
     *
     * @param Request $request
     * @param int $idStruct
     * @param string $idUser
     * @return \Illuminate\Http\JsonResponse
     */
    public function checkUser(Request $request, $idStruct, $idUser)
    {
        $exists = StructUser::where('idStruct', $idStruct)
            ->where('idUser', $idUser)
            ->exists();

        return response()->json(['belongs' => $exists]);
    }

    /**
     * Ajoute un utilisateur existant à une structure.
     *
     * @param Request $request
     * @param int $idStruct
     * @return \Illuminate\Http\JsonResponse
     */
    public function addUser(Request $request, $idStruct)
    {
        $validator = Validator::make($request->all(), [
            'idUser'         => 'required|string|exists:User,idUser', // Vérifie que l'utilisateur existe
            'isAdmin'        => 'nullable|boolean',
            'registerNumber' => 'nullable|string|max:255',
        ]);

        if ($validator->fails()) {
            return response()->json([
                'message' => 'Données invalides',
                'errors'  => $validator->errors(),
            ], 422);
        }

        // Vérifier que l'utilisateur n'est pas déjà dans la structure
        $already = StructUser::where('idStruct', $idStruct)
            ->where('idUser', $request->idUser)
            ->exists();

        if ($already) {
            return response()->json([
                'message' => 'Cet utilisateur est déjà membre de la structure',
            ], 409);
        }

        try {
            DB::beginTransaction();

            $structUser = StructUser::create([
                'idStruct'       => $idStruct,
                'idUser'         => $request->idUser,
                'isAdmin'        => $request->boolean('isAdmin', false),
                'registerNumber' => $request->registerNumber,
                'date'           => now(), // Date d'ajout
            ]);

            DB::commit();

            // Charger les infos utilisateur pour la réponse
            $structUser->load('user:idUser,firstName,name,email');

            return response()->json([
                'message' => 'Utilisateur ajouté à la structure avec succès',
                'data'    => $structUser,
            ], 201);

        } catch (\Exception $e) {
            DB::rollBack();
            Log::error('Erreur lors de l\'ajout d\'un utilisateur à la structure : ' . $e->getMessage());
            return response()->json([
                'message' => 'Erreur interne du serveur',
            ], 500);
        }
    }

    /**
     * Met à jour le rôle ou le numéro d'enregistrement d'un utilisateur dans une structure.
     *
     * @param Request $request
     * @param int $idStruct
     * @param string $idUser
     * @return \Illuminate\Http\JsonResponse
     */
    public function updateUserRole(Request $request, $idStruct, $idUser)
    {
        $structUser = StructUser::where('idStruct', $idStruct)
            ->where('idUser', $idUser)
            ->first();

        if (!$structUser) {
            return response()->json([
                'message' => 'Association introuvable',
            ], 404);
        }

        $validator = Validator::make($request->all(), [
            'isAdmin'        => 'nullable|boolean',
            'registerNumber' => 'nullable|string|max:255',
        ]);

        if ($validator->fails()) {
            return response()->json([
                'message' => 'Données invalides',
                'errors'  => $validator->errors(),
            ], 422);
        }

        try {
            if ($request->has('isAdmin')) {
                $structUser->isAdmin = $request->boolean('isAdmin');
            }
            if ($request->has('registerNumber')) {
                $structUser->registerNumber = $request->registerNumber;
            }
            $structUser->save();

            return response()->json([
                'message' => 'Rôle mis à jour avec succès',
                'data'    => $structUser,
            ]);

        } catch (\Exception $e) {
            Log::error('Erreur mise à jour rôle : ' . $e->getMessage());
            return response()->json([
                'message' => 'Erreur interne du serveur',
            ], 500);
        }
    }

    /**
     * Retire un utilisateur d'une structure.
     *
     * @param Request $request
     * @param int $idStruct
     * @param string $idUser
     * @return \Illuminate\Http\JsonResponse
     */
    public function removeUser(Request $request, $idStruct, $idUser)
    {
        $structUser = StructUser::where('idStruct', $idStruct)
            ->where('idUser', $idUser)
            ->first();

        if (!$structUser) {
            return response()->json([
                'message' => 'Association introuvable',
            ], 404);
        }

        try {
            $structUser->deleteAssociation($idStruct, $idUser);
            return response()->json([
                'message' => 'Utilisateur retiré de la structure avec succès',
            ]);

        } catch (\Exception $e) {
            Log::error('Erreur suppression association : ' . $e->getMessage()." Trace: ".$e->getTraceAsString()." idStruct: ".$idStruct." idUser: ".$idUser);
            return response()->json([
                'message' => 'Erreur interne du serveur',
            ], 500);
        }
    }

    /**
     * Récupère les utilisateurs qui ne sont PAS encore membres de la structure.
     * (Utile pour les invitations)
     *
     * @param Request $request
     * @param int $idStruct
     * @return \Illuminate\Http\JsonResponse
     */
    public function getAvailableUsers(Request $request, $idStruct)
    {
        // Récupère tous les utilisateurs qui n'ont pas d'entrée dans StructUser pour cette structure
        $availableUsers = User::whereNotExists(function ($query) use ($idStruct) {
            $query->select(DB::raw(1))
                  ->from('StructUser')
                  ->whereColumn('StructUser.idUser', 'User.idUser')
                  ->where('StructUser.idStruct', $idStruct);
        })
        ->select('idUser', 'firstName', 'name', 'email', 'phoneNumber')
        ->get();

        return response()->json($availableUsers);
    }

    /**
     * Recherche des utilisateurs DÉJÀ membres d'une structure.
     * (Alternative à getUsersByStructure avec recherche intégrée)
     *
     * @param Request $request
     * @param int $idStruct
     * @return \Illuminate\Http\JsonResponse
     */
    public function searchUsers(Request $request, $idStruct)
    {
        $validator = Validator::make($request->all(), [
            'q' => 'required|string|min:2|max:255',
        ]);

        if ($validator->fails()) {
            return response()->json([
                'message' => 'Paramètre de recherche invalide',
                'errors'  => $validator->errors(),
            ], 422);
        }

        $search = $request->q;

        $users = StructUser::where('idStruct', $idStruct)
            ->whereHas('user', function ($query) use ($search) {
                $query->where('firstName', 'LIKE', "%{$search}%")
                      ->orWhere('name', 'LIKE', "%{$search}%")
                      ->orWhere('email', 'LIKE', "%{$search}%")
                      ->orWhere('idUser', 'LIKE', "%{$search}%");
            })
            ->with('user:idUser,firstName,name,email')
            ->get()
            ->map(function ($structUser) {
                return [
                    'idUser'      => $structUser->idUser,
                    'firstName'   => $structUser->user->firstName ?? '',
                    'name'        => $structUser->user->name ?? '',
                    'email'       => $structUser->user->email ?? '',
                    'isAdmin'     => $structUser->isAdmin,
                    'registerNumber' => $structUser->registerNumber,
                ];
            });

        return response()->json($users);
    }

    public function getMemberStats($idStruct)
{
    $total = StructUser::where('idStruct', $idStruct)->count();
    $admins = StructUser::where('idStruct', $idStruct)->where('isAdmin', true)->count();
    $members = $total - $admins;

    // Évolution mensuelle (exemple)
    $evolution = StructUser::where('idStruct', $idStruct)
        ->select(DB::raw("DATE_FORMAT(date, '%Y-%m') as month"), DB::raw("COUNT(*) as count"))
        ->groupBy('month')
        ->orderBy('month')
        ->get()
        ->map(fn($item) => ['date' => $item->month, 'count' => $item->count]);

    return response()->json([
        'total' => $total,
        'admins' => $admins,
        'members' => $members,
        'evolution' => $evolution,
    ]);
    }
    /**
     * Récupère une liste paginée des utilisateurs d'une structure avec possibilité de filtrer par rôle (admin/membre).
     *
     * @param Request $request
     * @param int $idStruct
     * @return \Illuminate\Http\JsonResponse
     */
    public function getPaginatedUsers(Request $request, $idStruct)
{
    $perPage = $request->get('per_page', 15);
    $search = $request->get('search');
    $isAdmin = $request->get('isAdmin'); // peut être null, "true" ou "false"

    $query = StructUser::where('idStruct', $idStruct)
        ->with('user:idUser,firstName,name,email,phoneNumber');

    if ($search) {
        $query->whereHas('user', function ($q) use ($search) {
            $q->where('firstName', 'LIKE', "%{$search}%")
              ->orWhere('name', 'LIKE', "%{$search}%")
              ->orWhere('email', 'LIKE', "%{$search}%")
              ->orWhere('idUser', 'LIKE', "%{$search}%");
        });
    }

    // Filtre par rôle si présent
    if ($isAdmin !== null) {
        // Convertir "true"/"false" en booléen
        $isAdminBool = filter_var($isAdmin, FILTER_VALIDATE_BOOLEAN, FILTER_NULL_ON_FAILURE);
        if ($isAdminBool !== null) {
            $query->where('isAdmin', $isAdminBool);
        }
    }

    $structUsers = $query->orderBy('created_at', 'desc')->paginate($perPage);

    return response()->json($structUsers);
}
    /**
     * Compte le nombre d'utilisateurs dans une structure.
     *
     * @param int $idStruct
     * @return \Illuminate\Http\JsonResponse
     */
    public function countUsersByStructure($idStruct)
    {
        $count = StructUser::where('idStruct', $idStruct)->count();
        return response()->json(['count' => $count]);
    }

    /**
     * Relation définie dans le modèle StructUser pour récupérer l'utilisateur associé.
     * (À ajouter dans le modèle)
     */
    // public function user()
    // {
    //     return $this->belongsTo(User::class, 'idUser', 'idUser');
    // }
}