<?php

namespace App\Http\Controllers;

use Illuminate\Http\Request;
use App\Models\User;
use Illuminate\Support\Facades\Hash;
use Illuminate\Validation\Rule;
use Illuminate\Support\Facades\Validator;
use Illuminate\Support\Str;
use Illuminate\Support\Facades\Log;
use Illuminate\Database\QueryException;
use Exception;

class UserController extends Controller
{
    /**
     * Récupère la liste de tous les utilisateurs.
     *
     * @return \Illuminate\Http\JsonResponse
     */
    public function index()
    {
        try {
            $users = User::all();
            if ($users->isEmpty()) {
                return response()->json(['message' => 'Aucun utilisateur trouvé.'], 404);
            }
            return response()->json($users, 200);
        } catch (Exception $e) {
            Log::error('Erreur lors de la récupération des utilisateurs : ' . $e->getMessage(), [
                'trace' => $e->getTraceAsString()
            ]);
            return response()->json(['message' => 'Impossible de récupérer la liste des utilisateurs.'], 500);
        }
    }

    /**
     * Crée un nouvel utilisateur.
     *
     * @param Request $request
     * @return \Illuminate\Http\JsonResponse
     */
    public function store(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'name' => 'required|string|max:256',
            'firstName' => 'required|string|max:256',
            'email' => 'required|email|max:256|unique:User,email',
            'phoneNumber' => 'nullable|string|max:50|unique:User,phoneNumber',
            'profession' => 'nullable|integer',
            'profile' => 'nullable|string|max:256',
            'isAdmin' => 'boolean',
            'password' => 'required|string|min:6|max:1000',
        ], [
            'name.required' => 'Le nom est obligatoire.',
            'firstName.required' => 'Le prénom est obligatoire.',
            'email.required' => 'L\'adresse e-mail est obligatoire.',
            'email.email' => 'Le format de l\'adresse e-mail est invalide.',
            'email.unique' => 'Cette adresse e-mail est déjà utilisée.',
            'phoneNumber.unique' => 'Ce numéro de téléphone est déjà utilisé.',
            'password.required' => 'Le mot de passe est obligatoire.',
            'password.min' => 'Le mot de passe doit contenir au moins 6 caractères.',
        ]);

        if ($validator->fails()) {
            return response()->json([
                'message' => 'Erreur de validation.',
                'errors' => $validator->errors()
            ], 422);
        }

        try {
            $user = User::create([
                'idUser' => (string) Str::uuid(),
                'name' => $request->name,
                'firstName' => $request->firstName,
                'email' => $request->email,
                'phoneNumber' => $request->phoneNumber,
                'profession' => $request->profession ?? 0,
                'profile' => $request->profile ?? 'user.png',
                'isAdmin' => $request->isAdmin ?? 0,
                'password' => Hash::make($request->password),
            ]);
        } catch (QueryException $e) {
            Log::error('Erreur lors de la création de l\'utilisateur : ' . $e->getMessage(), [
                'email' => $request->email,
                'trace' => $e->getTraceAsString()
            ]);
            return response()->json([
                'message' => 'Impossible de créer l\'utilisateur. Vérifiez que l\'email et le numéro de téléphone ne sont pas déjà utilisés.'
            ], 409);
        } catch (Exception $e) {
            Log::error('Erreur inattendue lors de la création de l\'utilisateur : ' . $e->getMessage(), [
                'email' => $request->email,
                'trace' => $e->getTraceAsString()
            ]);
            return response()->json([
                'message' => 'Une erreur est survenue lors de la création de l\'utilisateur.'
            ], 500);
        }

        return response()->json(['message' => 'Utilisateur créé avec succès.', 'user' => $user], 201);
    }

    /**
     * Met à jour un utilisateur existant.
     *
     * @param Request $request
     * @param string $id
     * @return \Illuminate\Http\JsonResponse
     */
    public function update(Request $request, $id)
    {
        try {
            $user = User::find($id);
            if (!$user) {
                return response()->json(['message' => 'Utilisateur non trouvé.'], 404);
            }
        } catch (Exception $e) {
            Log::error('Erreur lors de la recherche de l\'utilisateur ID ' . $id . ' : ' . $e->getMessage());
            return response()->json(['message' => 'Utilisateur non trouvé.'], 404);
        }

        $validator = Validator::make($request->all(), [
            'name' => 'required|string|max:256',
            'firstName' => 'required|string|max:256',
            'email' => [
                'required', 'email', 'max:256',
                Rule::unique('User')->ignore($user->idUser, 'idUser')
            ],
            'phoneNumber' => [
                'nullable', 'string', 'max:50',
                Rule::unique('User')->ignore($user->idUser, 'idUser')
            ],
            'profession' => 'nullable|integer',
            'profile' => 'nullable|string|max:256',
            'isAdmin' => 'boolean',
            'password' => 'nullable|string|min:8|max:1000',
        ], [
            'name.required' => 'Le nom est obligatoire.',
            'firstName.required' => 'Le prénom est obligatoire.',
            'email.required' => 'L\'adresse e-mail est obligatoire.',
            'email.email' => 'Le format de l\'adresse e-mail est invalide.',
            'email.unique' => 'Cette adresse e-mail est déjà utilisée.',
            'phoneNumber.unique' => 'Ce numéro de téléphone est déjà utilisé.',
            'password.min' => 'Le mot de passe doit contenir au moins 8 caractères.',
        ]);

        if ($validator->fails()) {
            return response()->json([
                'message' => 'Erreur de validation.',
                'errors' => $validator->errors()
            ], 422);
        }

        $data = [
            'name' => $request->name,
            'firstName' => $request->firstName,
            'email' => $request->email,
            'phoneNumber' => $request->phoneNumber,
            'profession' => $request->profession ?? 0,
            'profile' => $request->profile ?? 'user.png',
            'isAdmin' => $request->isAdmin ?? 0,
        ];
        if ($request->filled('password')) {
            $data['password'] = Hash::make($request->password);
        }

        try {
            $user->update($data);
        } catch (QueryException $e) {
            Log::error('Erreur lors de la mise à jour de l\'utilisateur ID ' . $id . ' : ' . $e->getMessage(), [
                'trace' => $e->getTraceAsString()
            ]);
            return response()->json([
                'message' => 'Impossible de mettre à jour l\'utilisateur. Vérifiez que l\'email et le numéro de téléphone ne sont pas déjà utilisés.'
            ], 409);
        } catch (Exception $e) {
            Log::error('Erreur inattendue lors de la mise à jour de l\'utilisateur ID ' . $id . ' : ' . $e->getMessage(), [
                'trace' => $e->getTraceAsString()
            ]);
            return response()->json([
                'message' => 'Une erreur est survenue lors de la mise à jour de l\'utilisateur.'
            ], 500);
        }

        return response()->json(['message' => 'Utilisateur mis à jour avec succès.', 'user' => $user], 200);
    }

    /**
     * Supprime un utilisateur.
     *
     * @param string $id
     * @return \Illuminate\Http\JsonResponse
     */
    public function destroy($id)
    {
        try {
            $user = User::find($id);
            if (!$user) {
                return response()->json(['message' => 'Utilisateur non trouvé.'], 404);
            }
        } catch (Exception $e) {
            Log::error('Erreur lors de la recherche de l\'utilisateur ID ' . $id . ' : ' . $e->getMessage());
            return response()->json(['message' => 'Utilisateur non trouvé.'], 404);
        }

        try {
            $user->delete();
        } catch (Exception $e) {
            Log::error('Erreur lors de la suppression de l\'utilisateur ID ' . $id . ' : ' . $e->getMessage(), [
                'trace' => $e->getTraceAsString()
            ]);
            return response()->json([
                'message' => 'Une erreur est survenue lors de la suppression de l\'utilisateur.'
            ], 500);
        }

        return response()->json(['message' => 'Utilisateur supprimé avec succès.'], 200);
    }

    /**
     * Affiche les détails d'un utilisateur spécifique.
     *
     * @param string $id
     * @return \Illuminate\Http\JsonResponse
     */
    public function show($id)
    {
        try {
            $user = User::find($id);
            if (!$user) {
                return response()->json(['message' => 'Utilisateur non trouvé.'], 404);
            }
            return response()->json($user, 200);
        } catch (Exception $e) {
            Log::error('Erreur lors de l\'affichage de l\'utilisateur ID ' . $id . ' : ' . $e->getMessage(), [
                'trace' => $e->getTraceAsString()
            ]);
            return response()->json(['message' => 'Impossible de récupérer les détails de l\'utilisateur.'], 500);
        }
    }

    /**
     * Compte le nombre total d'utilisateurs.
     *
     * @return \Illuminate\Http\JsonResponse
     */
    public function countUsers()
    {
        try {
            $count = User::count();
            return response()->json(['count' => $count], 200);
        } catch (Exception $e) {
            Log::error('Erreur lors du comptage des utilisateurs : ' . $e->getMessage(), [
                'trace' => $e->getTraceAsString()
            ]);
            return response()->json(['message' => 'Impossible de compter les utilisateurs.'], 500);
        }
    }

    /**
     * Recherche un utilisateur par adresse e-mail.
     *
     * @param string $email
     * @return \Illuminate\Http\JsonResponse
     */
    public function getByEmail($email)
    {
        try {
            $user = User::where('email', $email)->first();
            if (!$user) {
                return response()->json(['message' => 'Aucun utilisateur trouvé avec cette adresse e-mail.'], 404);
            }
            return response()->json($user, 200);
        } catch (Exception $e) {
            Log::error('Erreur lors de la recherche de l\'utilisateur par email : ' . $e->getMessage(), [
                'email' => $email,
                'trace' => $e->getTraceAsString()
            ]);
            return response()->json(['message' => 'Impossible de rechercher l\'utilisateur.'], 500);
        }
    }

    /**
     * Envoie les instructions de réinitialisation du mot de passe.
     *
     * @param Request $request
     * @return \Illuminate\Http\JsonResponse
     */
    public function forgetPassword(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'email' => 'required|email|max:256',
        ], [
            'email.required' => 'L\'adresse e-mail est obligatoire.',
            'email.email' => 'Le format de l\'adresse e-mail est invalide.',
        ]);

        if ($validator->fails()) {
            return response()->json([
                'message' => 'Erreur de validation.',
                'errors' => $validator->errors()
            ], 422);
        }

        try {
            $user = User::where('email', $request->email)->first();
            if (!$user) {
                return response()->json(['message' => 'Aucun compte trouvé avec cette adresse e-mail.'], 404);
            }

            // Ici, vous pouvez implémenter la logique pour envoyer un e-mail de réinitialisation de mot de passe.
            // Par exemple, générer un token et envoyer un e-mail avec un lien de réinitialisation.

            return response()->json(['message' => 'Instructions de réinitialisation du mot de passe envoyées à votre adresse e-mail.'], 200);
        } catch (Exception $e) {
            Log::error('Erreur lors de la demande de réinitialisation de mot de passe : ' . $e->getMessage(), [
                'email' => $request->email,
                'trace' => $e->getTraceAsString()
            ]);
            return response()->json(['message' => 'Une erreur est survenue lors de la demande de réinitialisation.'], 500);
        }
    }

    /**
     * Recherche un utilisateur par email ou numéro de téléphone.
     *
     * @param Request $request
     * @return \Illuminate\Http\JsonResponse
     */
    public function search(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'email' => 'nullable|email|max:256',
            'phoneNumber' => 'nullable|string|max:50',
        ]);

        if ($validator->fails()) {
            return response()->json([
                'message' => 'Paramètres invalides',
                'errors' => $validator->errors()
            ], 422);
        }

        try {
            $query = User::query();

            if ($request->filled('email')) {
                $query->where('email', $request->email);
            }

            if ($request->filled('phoneNumber')) {
                $query->orWhere('phoneNumber', $request->phoneNumber);
            }

            $user = $query->first();

            if (!$user) {
                return response()->json(['message' => 'Aucun utilisateur trouvé avec ces informations.'], 404);
            }

            return response()->json($user, 200);
        } catch (Exception $e) {
            Log::error('Erreur lors de la recherche d\'utilisateur : ' . $e->getMessage(), [
                'email' => $request->email,
                'phoneNumber' => $request->phoneNumber,
                'trace' => $e->getTraceAsString()
            ]);
            return response()->json(['message' => 'Impossible de rechercher l\'utilisateur.'], 500);
        }
    }
}