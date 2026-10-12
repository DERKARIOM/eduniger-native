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
use Illuminate\Support\Facades\Http;

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

    /**
     * Connexion / inscription via « Continuer avec Google » (application mobile).
     *
     * L'application envoie l'ID token obtenu par Google Sign-In. Rien de ce qu'envoie le
     * client n'est cru sur parole : l'identite (sub, email, nom) est lue dans le token
     * apres verification par Google (signature, audience = notre client OAuth Web,
     * emetteur, expiration, email verifie).
     *
     * - compte deja lie (google_id)             -> connexion
     * - sinon compte existant avec le meme email -> liaison du google_id puis connexion
     * - sinon                                    -> creation du compte puis connexion
     *
     * Reponse identique a login() : { message, user, token } (+ isNewUser).
     */
    public function loginWithGoogle(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'idToken' => 'required|string|max:4096',
            'fcmToken' => 'nullable|string|max:10000',
        ]);
        if ($validator->fails()) {
            return response()->json([
                'message' => 'Erreur de validation.',
                'errors' => $validator->errors()
            ], 422);
        }

        $allowedAudiences = config('services.google.client_ids', []);
        if (empty($allowedAudiences)) {
            Log::error('Connexion Google : GOOGLE_CLIENT_IDS absent du .env');
            return response()->json(['message' => 'Connexion Google indisponible.'], 503);
        }

        // Verification de l'ID token par Google (endpoint officiel tokeninfo : il valide
        // la signature et l'expiration). On controle ensuite nous-memes l'audience,
        // l'emetteur et l'email : un token emis pour une autre application est refuse.
        try {
            $response = Http::timeout(8)->get('https://oauth2.googleapis.com/tokeninfo', [
                'id_token' => $request->idToken,
            ]);
        } catch (Exception $e) {
            Log::error('Connexion Google : Google injoignable : ' . $e->getMessage());
            return response()->json(['message' => 'Service Google injoignable.'], 503);
        }

        $claims = $response->successful() ? $response->json() : null;
        $valid = is_array($claims)
            && in_array($claims['aud'] ?? '', $allowedAudiences, true)
            && in_array($claims['iss'] ?? '', ['accounts.google.com', 'https://accounts.google.com'], true)
            && ((int) ($claims['exp'] ?? 0)) > time()
            && !empty($claims['sub'])
            && !empty($claims['email'])
            && in_array($claims['email_verified'] ?? false, [true, 'true'], true);

        if (!$valid) {
            return response()->json(['message' => 'Jeton Google invalide.'], 401);
        }

        $googleId = (string) $claims['sub'];
        $email = mb_strtolower((string) $claims['email']);
        $fcmToken = $request->filled('fcmToken') ? $request->fcmToken : null;
        $isNewUser = false;

        try {
            $user = DB::transaction(function () use ($googleId, $email, $claims, $fcmToken, &$isNewUser) {
                $user = User::where('google_id', $googleId)->first();

                if (!$user) {
                    $user = User::where('email', $email)->lockForUpdate()->first();
                    if ($user) {
                        // Un compte different est deja lie a un autre compte Google :
                        // on ne l'ecrase pas silencieusement.
                        if (!empty($user->google_id) && $user->google_id !== $googleId) {
                            return null;
                        }
                        $user->google_id = $googleId;
                    }
                }

                if (!$user) {
                    $isNewUser = true;
                    $name = trim((string) ($claims['family_name'] ?? ''));
                    $firstName = trim((string) ($claims['given_name'] ?? ''));
                    if ($name === '' && $firstName === '') {
                        $firstName = trim((string) ($claims['name'] ?? strstr($email, '@', true)));
                    }
                    $user = new User([
                        'idUser' => (string) Str::uuid(),
                        'name' => mb_substr($name !== '' ? $name : $firstName, 0, 256),
                        'firstName' => mb_substr($firstName !== '' ? $firstName : $name, 0, 256),
                        'email' => $email,
                        'profession' => 0,
                        'profile' => 'user.png',
                        'isAdmin' => 0,
                        // Mot de passe aleatoire inutilisable : ce compte se connecte par
                        // Google (il pourra definir un mot de passe via « oublie »).
                        'password' => Hash::make(Str::random(64)),
                        'fcm_token' => '',
                    ]);
                    $user->google_id = $googleId;
                }

                if ($fcmToken !== null) {
                    $user->fcm_token = $fcmToken;
                }
                $user->save();
                return $user;
            });
        } catch (Exception $e) {
            Log::error('Connexion Google : erreur base de donnees : ' . $e->getMessage());
            return response()->json(['message' => 'Une erreur est survenue lors de la connexion.'], 500);
        }

        if (!$user) {
            return response()->json([
                'message' => 'Cette adresse e-mail est deja liee a un autre compte Google.'
            ], 409);
        }

        $token = $user->createToken('auth_token')->plainTextToken;

        return response()->json([
            'message' => $isNewUser ? 'Inscription réussie. Bienvenue !' : 'Connexion réussie.',
            'user' => $user,
            'token' => $token,
            'isNewUser' => $isNewUser,
        ], $isNewUser ? 201 : 200);
    }
}
