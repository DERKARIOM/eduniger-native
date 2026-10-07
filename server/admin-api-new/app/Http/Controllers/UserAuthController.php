<?php

namespace App\Http\Controllers;

use Illuminate\Http\Request;
use App\Models\User;
use Illuminate\Support\Facades\Hash;
use Illuminate\Validation\Rule;
use Illuminate\Support\Facades\Validator;
use Illuminate\Support\Str;
use Illuminate\Support\Facades\Auth;
use Illuminate\Support\Facades\DB;
use Illuminate\Database\QueryException;
use Exception;
use Illuminate\Support\Facades\Log;

class UserAuthController extends Controller
{
    /**
     * Déconnecte l'utilisateur en révoquant le token d'accès courant.
     *
     * @param Request $request
     * @return \Illuminate\Http\JsonResponse
     */
    public function logout(Request $request)
    {
        try {
            $token = $request->user()->currentAccessToken();
            if ($token) {
                $token->delete();
                return response()->json([
                    'message' => 'Déconnexion réussie'
                ], 200);
            } else {
                return response()->json([
                    'message' => 'Aucun token à révoquer.'
                ], 400);
            }
        } catch (Exception $e) {
            Log::error('Erreur lors de la déconnexion : ' . $e->getMessage(), [
                'user_id' => $request->user()?->idUser,
                'trace' => $e->getTraceAsString()
            ]);
            return response()->json([
                'message' => 'Une erreur est survenue lors de la déconnexion.'
            ], 500);
        }
    }

    /**
     * Connecte un utilisateur et génère un token d'accès.
     *
     * @param Request $request
     * @return \Illuminate\Http\JsonResponse
     */
    public function login(Request $request)
    {
        // L'application mobile (lecteurs) n'a qu'un champ "numero" (telephone) a la
        // connexion, pas d'email : on accepte donc soit "email", soit "phoneNumber",
        // l'un des deux etant obligatoire. Le Web, s'il appelle un jour cet endpoint,
        // continue de fonctionner a l'identique avec "email" seul.
        $validator = Validator::make($request->all(), [
            'email' => 'required_without:phoneNumber|nullable|email',
            'phoneNumber' => 'required_without:email|nullable|string',
            'password' => 'required|string',
            'fcmToken' => 'nullable|string|max:10000',
        ], [
            'email.required_without' => 'Veuillez fournir votre adresse e-mail ou votre numéro.',
            'email.email' => 'Le format de l\'adresse e-mail est invalide.',
            'phoneNumber.required_without' => 'Veuillez fournir votre numéro ou votre adresse e-mail.',
            'password.required' => 'Veuillez fournir votre mot de passe.',
        ]);

        if ($validator->fails()) {
            return response()->json([
                'message' => 'Erreur de validation.',
                'errors' => $validator->errors()
            ], 422);
        }

        try {
            $query = User::query();
            if ($request->filled('email')) {
                $query->where('email', $request->email);
            } else {
                $query->where('phoneNumber', $request->phoneNumber);
            }
            $user = $query->first();
        } catch (QueryException $e) {
            Log::error('Erreur lors de la récupération de l\'utilisateur : ' . $e->getMessage(), [
                'email' => $request->email,
                'phoneNumber' => $request->phoneNumber,
                'trace' => $e->getTraceAsString()
            ]);
            return response()->json([
                'message' => 'Une erreur est survenue lors de la connexion.'
            ], 500);
        }

        if (!$user) {
            return response()->json([
                'message' => 'Aucun compte trouvé avec cette adresse e-mail.'
            ], 404);
        }

        if (!Hash::check($request->password, $user->password)) {
            return response()->json([
                'message' => 'Le mot de passe est incorrect.'
            ], 401);
        }

        // Le token FCM (notifications push) peut changer entre deux connexions (reinstall,
        // nouvel appareil...) : on le rafraichit a chaque connexion reussie, sans bloquer la
        // connexion si l'ecriture echoue.
        if ($request->filled('fcmToken')) {
            try {
                $user->fcm_token = $request->fcmToken;
                $user->save();
            } catch (Exception $e) {
                Log::error('Erreur lors de la mise a jour du token FCM : ' . $e->getMessage(), [
                    'user_id' => $user->idUser,
                ]);
            }
        }

        try {
            $token = $user->createToken('auth_token')->plainTextToken;
        } catch (Exception $e) {
            Log::error('Erreur lors de la génération du token : ' . $e->getMessage(), [
                'user_id' => $user->idUser,
                'trace' => $e->getTraceAsString()
            ]);
            return response()->json([
                'message' => 'Une erreur est survenue lors de la connexion.'
            ], 500);
        }

        return response()->json([
            'message' => 'Connexion réussie.',
            'user' => $user,
            'token' => $token,
        ], 200);
    }

    /**
     * Inscrit un nouvel utilisateur.
     *
     * @param Request $request
     * @return \Illuminate\Http\JsonResponse
     */
    public function register(Request $request)
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
            'fcmToken' => 'nullable|string|max:10000',
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

        DB::beginTransaction();
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
                'fcm_token' => $request->fcmToken,
            ]);
            $token = $user->createToken('auth_token')->plainTextToken;
            DB::commit();
        } catch (QueryException $e) {
            DB::rollBack();
            Log::error('Erreur lors de l\'inscription : ' . $e->getMessage(), [
                'email' => $request->email,
                'trace' => $e->getTraceAsString()
            ]);
            return response()->json([
                'message' => 'Impossible de créer l\'utilisateur. Vérifiez que l\'email et le numéro de téléphone ne sont pas déjà utilisés.'
            ], 409);
        } catch (Exception $e) {
            DB::rollBack();
            Log::error('Erreur inattendue lors de l\'inscription : ' . $e->getMessage(), [
                'email' => $request->email,
                'trace' => $e->getTraceAsString()
            ]);
            return response()->json([
                'message' => 'Une erreur est survenue lors de l\'inscription.'
            ], 500);
        }

        return response()->json([
            'message' => 'Inscription réussie. Bienvenue !',
            'user' => $user,
            'token' => $token,
        ], 201);
    }

    /**
     * Met à jour le profil de l'utilisateur connecté.
     *
     * @param Request $request
     * @return \Illuminate\Http\JsonResponse
     */
    public function updateProfile(Request $request)
    {
        $user = User::where('idUser', Auth::id())->first();

        if (!$user) {
            return response()->json(['message' => 'Aucun utilisateur connecté.'], 401);
        }

        $validator = Validator::make($request->all(), [
            'name' => 'required|string|max:256',
            'firstName' => 'required|string|max:256',
            'email' => ['required', 'email', Rule::unique('User')->ignore($user->idUser, 'idUser')],
            'phoneNumber' => ['nullable', 'string', 'max:50', Rule::unique('User')->ignore($user->idUser, 'idUser')],
            'profession' => 'nullable|integer',
        ], [
            'name.required' => 'Le nom est obligatoire.',
            'firstName.required' => 'Le prénom est obligatoire.',
            'email.required' => 'L\'adresse e-mail est obligatoire.',
            'email.email' => 'Le format de l\'adresse e-mail est invalide.',
            'email.unique' => 'Cette adresse e-mail est déjà utilisée.',
            'phoneNumber.unique' => 'Ce numéro de téléphone est déjà utilisé.',
        ]);

        if ($validator->fails()) {
            return response()->json([
                'message' => 'Erreur de validation.',
                'errors' => $validator->errors()
            ], 422);
        }

        DB::beginTransaction();
        try {
            $user->name = $request->name;
            $user->firstName = $request->firstName;
            $user->email = $request->email;
            $user->phoneNumber = $request->phoneNumber;
            $user->profession = $request->profession;
            $user->save();
            DB::commit();
        } catch (QueryException $e) {
            DB::rollBack();
            Log::error('Erreur lors de la mise à jour du profil : ' . $e->getMessage(), [
                'user_id' => $user->idUser,
                'trace' => $e->getTraceAsString()
            ]);
            return response()->json([
                'message' => 'Impossible de mettre à jour le profil. Vérifiez que l\'email ou le numéro de téléphone ne sont pas déjà utilisés.'
            ], 409);
        } catch (Exception $e) {
            DB::rollBack();
            Log::error('Erreur inattendue lors de la mise à jour du profil : ' . $e->getMessage(), [
                'user_id' => $user->idUser,
                'trace' => $e->getTraceAsString()
            ]);
            return response()->json([
                'message' => 'Une erreur est survenue lors de la mise à jour du profil.'
            ], 500);
        }

        return response()->json([
            'message' => 'Profil mis à jour avec succès.',
            'user' => $user,
        ], 200);
    }

    /**
     * Récupère les informations de l'utilisateur connecté.
     *
     * @param Request $request
     * @return \Illuminate\Http\JsonResponse
     */
    public function me(Request $request)
    {
        $user = Auth::user();

        if (!$user) {
            return response()->json(['message' => 'Aucun utilisateur connecté.'], 401);
        }

        return response()->json([
            'message' => 'Utilisateur récupéré avec succès.',
            'user' => $user,
        ], 200);
    }
}
