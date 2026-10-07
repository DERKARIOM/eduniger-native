<?php

namespace App\Http\Controllers;

use App\Models\StructAgent;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Hash;
use Illuminate\Support\Facades\Validator;
use Illuminate\Validation\ValidationException;
use Carbon\Carbon;
use Illuminate\Support\Facades\Log;
use Exception;

class AuthController extends Controller
{
    /**
     * Enregistre un nouvel agent à partir d'une invitation.
     *
     * Vérifie que l'email existe dans struct_agent, que le code d'invitation est valide
     * et non expiré, puis met à jour le mot de passe et invalide le code.
     *
     * @param Request $request
     * @return \Illuminate\Http\JsonResponse
     */
    public function register(Request $request)
    {
        try {
            $validator = Validator::make($request->all(), [
                'email' => 'required|email|exists:struct_agent,email',
                'invitation_code' => 'required|string',
                'password' => 'required|string|min:8|confirmed',
            ],
            [
                'email.exists' => 'Aucun agent trouvé avec cet email.',
                'password.confirmed' => 'Le champ de confirmation du mot de passe ne correspond pas.',
                'email.required' => 'L\'email est requis.',
                'invitation_code.required' => 'Le code d\'invitation est requis.',
                'password.required' => 'Le mot de passe est requis.',
            ]);

            if ($validator->fails()) {
                return response()->json(['errors' => $validator->errors()->first()], 422);
            }

            $agent = StructAgent::where('email', $request->email)->first();

            if (!$agent) {
                return response()->json(['message' => 'Agent non trouvé.'], 404);
            }

            if (!$agent->isAuthorizedToCreateAccount) {
                return response()->json(['message' => 'Non autorisé à créer un compte.'], 403);
            }

            if (
                $agent->invitationCode !== $request->invitation_code ||
                !$agent->codeExpiresAt ||
                Carbon::now()->greaterThan($agent->codeExpiresAt)
            ) {
                return response()->json(['message' => 'Code d\'invitation invalide ou expiré.'], 403);
            }

            // Met à jour le mot de passe et invalide le code
            $agent->update([
                'password' => Hash::make($request->password),
                'invitationCode' => null,
                'codeExpiresAt' => null,
                'isAuthorizedToCreateAccount' => false,
            ]);

            return response()->json(['message' => 'Compte créé avec succès.']);
        } catch (Exception $e) {
            Log::error('Erreur lors de l\'enregistrement : ' . $e->getMessage(), [
                'email' => $request->email ?? 'inconnu',
                'trace' => $e->getTraceAsString()
            ]);
            return response()->json(['message' => 'Une erreur est survenue lors de la création du compte.'], 500);
        }
    }

    /**
     * Connecte un agent et génère un token d'accès.
     *
     * Valide les identifiants, crée un token Sanctum et retourne les informations utilisateur.
     *
     * @param Request $request
     * @return \Illuminate\Http\JsonResponse
     * @throws ValidationException
     */
    public function login(Request $request)
    {
        try {
            $validator = Validator::make($request->all(), [
                'email' => 'required|email',
                'password' => 'required|string',
            ],
            [
                'email.required' => 'L\'email est requis.',
                'email.email' => 'L\'email doit être une adresse email valide.',
                'password.required' => 'Le mot de passe est requis.',
            ]);

            if ($validator->fails()) {
                throw ValidationException::withMessages($validator->errors()->toArray());
            }

            $agent = StructAgent::where('email', $request->email)->first();

            if (!$agent || !Hash::check($request->password, $agent->password)) {
                throw ValidationException::withMessages([
                    'email' => ['Identifiants incorrects.'],
                ]);
            }

            $token = $agent->createToken('authToken')->plainTextToken;

            return response()->json([
                'message' => 'Connexion réussie',
                'token' => $token,
                'user' => $agent,
            ]);
        } catch (ValidationException $e) {
            // Les erreurs de validation doivent être remontées telles quelles
            throw $e;
        } catch (Exception $e) {
            Log::error('Erreur lors de la connexion : ' . $e->getMessage(), [
                'email' => $request->email ?? 'inconnu',
                'trace' => $e->getTraceAsString()
            ]);
            return response()->json(['message' => 'Une erreur est survenue lors de la connexion.'], 500);
        }
    }

    /**
     * Retourne les informations de l'agent connecté.
     *
     * @param Request $request
     * @return \Illuminate\Http\JsonResponse
     */
    public function me(Request $request)
    {
        try {
            return response()->json($request->user());
        } catch (Exception $e) {
            Log::error('Erreur lors de la récupération du profil : ' . $e->getMessage(), [
                'user_id' => $request->user()?->id,
                'trace' => $e->getTraceAsString()
            ]);
            return response()->json(['message' => 'Impossible de récupérer les informations utilisateur.'], 500);
        }
    }

    /**
     * Déconnecte l'agent en supprimant le token d'accès courant.
     *
     * @param Request $request
     * @return \Illuminate\Http\JsonResponse
     */
    public function logout(Request $request)
    {
        try {
            $request->user()->currentAccessToken()->delete();
            return response()->json(['message' => 'Déconnecté avec succès.']);
        } catch (Exception $e) {
            Log::error('Erreur lors de la déconnexion : ' . $e->getMessage(), [
                'user_id' => $request->user()?->id,
                'trace' => $e->getTraceAsString()
            ]);
            return response()->json(['message' => 'Une erreur est survenue lors de la déconnexion.'], 500);
        }
    }

    /**
     * Met à jour le profil de l'agent connecté.
     *
     * Valide et enregistre les nouvelles informations (nom, email, téléphone).
     *
     * @param Request $request
     * @return \Illuminate\Http\JsonResponse
     */
    public function updateProfile(Request $request)
    {
        try {
            $user = $request->user();

            $validated = $request->validate([
                'name' => 'required|string|max:255',
                'email' => 'required|email|unique:struct_agent,email,' . $user->id,
                'phoneNumber' => 'required|string'
            ],
            [
                'name.required' => 'Le nom est requis.',
                'email.required' => 'L\'email est requis.',
                'email.email' => 'L\'email doit être une adresse email valide.',
                'email.unique' => 'Cet email est déjà utilisé.',
                'phoneNumber.required' => 'Le numéro de téléphone est requis.',
            ]);

            $user->update($validated);

            return response()->json([
                'message' => 'Profil mis à jour avec succès',
                'user' => $user
            ]);
        } catch (ValidationException $e) {
            throw $e;
        } catch (Exception $e) {
            Log::error('Erreur lors de la mise à jour du profil : ' . $e->getMessage(), [
                'user_id' => $request->user()?->id,
                'trace' => $e->getTraceAsString()
            ]);
            return response()->json(['message' => 'Une erreur est survenue lors de la mise à jour du profil.'], 500);
        }
    }

    /**
     * Met à jour le mot de passe de l'agent connecté.
     *
     * Vérifie le mot de passe actuel avant de le modifier.
     *
     * @param Request $request
     * @return \Illuminate\Http\JsonResponse
     */
    public function updatePassword(Request $request)
    {
        try {
            $user = $request->user();

            $validated = $request->validate([
                'current_password' => 'required|string',
                'new_password' => 'required|string|min:8|confirmed'
            ],
            [
                'current_password.required' => 'Le mot de passe actuel est requis.',
                'new_password.required' => 'Le nouveau mot de passe est requis.',
                'new_password.min' => 'Le nouveau mot de passe doit comporter au moins 8 caractères.',
                'new_password.confirmed' => 'Le champ de confirmation du nouveau mot de passe ne correspond pas.',
            ]);

            if (!Hash::check($validated['current_password'], $user->password)) {
                return response()->json(['error' => 'Le mot de passe actuel est incorrect'], 401);
            }

            $user->update([
                'password' => Hash::make($validated['new_password'])
            ]);

            return response()->json(['message' => 'Mot de passe mis à jour avec succès']);
        } catch (ValidationException $e) {
            throw $e;
        } catch (Exception $e) {
            Log::error('Erreur lors de la mise à jour du mot de passe : ' . $e->getMessage(), [
                'user_id' => $request->user()?->id,
                'trace' => $e->getTraceAsString()
            ]);
            return response()->json(['message' => 'Une erreur est survenue lors de la mise à jour du mot de passe.'], 500);
        }
    }
}