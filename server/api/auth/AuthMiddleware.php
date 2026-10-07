<?php
require_once __DIR__ . '/Jwt.php';
require_once __DIR__ . '/Config.php';

/**
 * Middleware d'authentification pour les endpoints protégés de l'API.
 *
 * Utilisation typique en tête d'un endpoint protégé :
 *
 *     require_once __DIR__ . '/auth/AuthMiddleware.php';
 *     $auth = AuthMiddleware::requireAuth();   // 401 + exit si absent/invalide/expiré
 *     $idUser = $auth['sub'];                  // idUser vérifié cryptographiquement,
 *                                               // à utiliser à la place de tout idUser
 *                                               // envoyé par le client dans $_POST/$_GET.
 *
 * Pour un endpoint réservé aux admins :
 *     $auth = AuthMiddleware::requireAuth('ADMIN');   // 403 + exit si rôle insuffisant
 *
 * Sécurité : les permissions sont vérifiées ICI, côté serveur, à partir du rôle contenu
 * dans le token signé — jamais à partir d'une valeur envoyée séparément par le client.
 */
class AuthMiddleware
{
    // Hiérarchie des rôles : un rôle de niveau N a aussi accès à ce qui requiert un rôle
    // de niveau <= N. Permet d'ajouter facilement SUPER_ADMIN au-dessus d'ADMIN sans
    // toucher aux endpoints existants qui demandent seulement 'ADMIN'.
    private const ROLE_LEVELS = [
        'USER' => 0,
        'ADMIN' => 1,
        'SUPER_ADMIN' => 2,
    ];

    /**
     * Vérifie l'Access Token présenté et, optionnellement, un rôle minimum requis.
     * Termine la requête (401 ou 403 + JSON) si la vérification échoue.
     *
     * @return array Les claims du token (sub = idUser, role, iat, exp, type).
     */
    public static function requireAuth(?string $minimumRole = null): array
    {
        $token = self::extractBearerToken();

        if ($token === null) {
            self::deny(401, 'Authentification requise. En-tête Authorization manquant.');
        }

        try {
            $payload = Jwt::decode($token, AuthConfig::jwtSecret());
        } catch (JwtExpiredException $e) {
            self::deny(401, 'Token expiré.', 'token_expired');
        } catch (JwtException $e) {
            // Repli Laravel Sanctum : depuis la migration de la connexion lecteur vers
            // UserAuthController::login ($user->createToken(...)), le token stocke cote
            // mobile (TokenStore) et envoye a TOUS les endpoints (AuthInterceptor) est un
            // token Sanctum - jamais un JWT "maison". Sans ce repli, chaque appel
            // authentifie vers ce dossier legacy (ranking.php, StructBookMore.php,
            // author_book.php, FabiolaBook.php, CategoryIn.php, Category.php, etc.)
            // echouait systematiquement en "Token invalide" (constate en test reel -
            // audit recherche de livres : JSONException a la lecture de la reponse de
            // ranking.php par SearchActivity, alors que /api/authors et /api/structures,
            // proteges par le vrai auth:sanctum Laravel, acceptent ce meme token).
            $payload = self::verifySanctumToken($token);
            if ($payload === null) {
                // Ne jamais renvoyer le détail exact (raison cryptographique précise) au
                // client : cela aiderait un attaquant à affiner ses tentatives de falsification.
                error_log('Token invalide rejeté : ' . $e->getMessage());
                self::deny(401, 'Token invalide.');
            }
        }

        if (!isset($payload['type']) || $payload['type'] !== 'access') {
            // Empêche un Refresh Token (qui n'est de toute façon pas un JWT dans notre
            // implémentation) ou un token d'un autre usage d'être utilisé ici.
            self::deny(401, 'Type de token invalide pour cet usage.');
        }

        if ($minimumRole !== null && !self::roleSatisfies((string) ($payload['role'] ?? 'USER'), $minimumRole)) {
            self::deny(403, 'Permissions insuffisantes.');
        }

        return $payload;
    }

    private static function roleSatisfies(string $actualRole, string $requiredRole): bool
    {
        $actualLevel = self::ROLE_LEVELS[$actualRole] ?? 0;
        $requiredLevel = self::ROLE_LEVELS[$requiredRole] ?? PHP_INT_MAX;
        return $actualLevel >= $requiredLevel;
    }

    /**
     * Verifie un token au format Laravel Sanctum ("id|chaineEnClair") en reproduisant
     * exactement la logique de PersonalAccessToken::findToken() de Sanctum (meme table,
     * meme hash sha256, meme comparaison en temps constant), sans dependance a Laravel
     * dans ce dossier legacy. Retourne un tableau de claims compatible avec celui produit
     * par Jwt::decode() (memes cles "sub"/"role"/"type"), ou null si le token n'est pas un
     * token Sanctum valide, actif et rattache a un compte lecteur (User) existant.
     */
    private static function verifySanctumToken(string $token): ?array
    {
        if (strpos($token, '|') === false) {
            return null;
        }
        [$id, $plainText] = explode('|', $token, 2);
        if ($id === '' || $plainText === '' || !ctype_digit($id)) {
            return null;
        }

        global $pdo;
        if (!isset($pdo)) {
            // connectBDD.php n'a pas ete inclus par l'endpoint appelant avant
            // AuthMiddleware - pas de repli possible sans connexion DB.
            return null;
        }

        $stmt = $pdo->prepare(
            'SELECT tokenable_type, tokenable_id, token, expires_at FROM personal_access_tokens WHERE id = :id'
        );
        $stmt->execute([':id' => $id]);
        $row = $stmt->fetch();
        if (!$row) {
            return null;
        }

        // hash_equals() : comparaison en temps constant, memes raisons que pour la
        // signature JWT ci-dessus (protection contre les attaques par mesure de temps).
        if (!hash_equals((string) $row['token'], hash('sha256', $plainText))) {
            return null;
        }

        if (!empty($row['expires_at']) && strtotime((string) $row['expires_at']) <= time()) {
            return null;
        }

        // Seuls les comptes lecteurs (App\Models\User) sont concernes par ce dossier
        // legacy (endpoints "lecteur" : livres, categories, auteurs...) ; un token
        // Sanctum d'agent (App\Models\StructAgent) n'a rien a y faire.
        if ($row['tokenable_type'] !== 'App\\Models\\User') {
            return null;
        }

        $userStmt = $pdo->prepare('SELECT idUser, isAdmin FROM User WHERE idUser = :idUser');
        $userStmt->execute([':idUser' => $row['tokenable_id']]);
        $user = $userStmt->fetch();
        if (!$user) {
            return null;
        }

        return [
            'sub' => $user['idUser'],
            'role' => !empty($user['isAdmin']) ? 'ADMIN' : 'USER',
            'type' => 'access',
        ];
    }

    private static function extractBearerToken(): ?string
    {
        $header = null;

        if (isset($_SERVER['HTTP_AUTHORIZATION'])) {
            $header = $_SERVER['HTTP_AUTHORIZATION'];
        } elseif (isset($_SERVER['REDIRECT_HTTP_AUTHORIZATION'])) {
            // Certaines configurations Apache/CGI ne propagent le header Authorization
            // que sous ce nom préfixé "REDIRECT_" — fallback nécessaire pour ne pas
            // perdre l'authentification selon la config serveur.
            $header = $_SERVER['REDIRECT_HTTP_AUTHORIZATION'];
        } elseif (function_exists('apache_request_headers')) {
            $headers = apache_request_headers();
            foreach ($headers as $name => $value) {
                if (strcasecmp($name, 'Authorization') === 0) {
                    $header = $value;
                    break;
                }
            }
        }

        if ($header === null || stripos($header, 'Bearer ') !== 0) {
            return null;
        }

        $token = trim(substr($header, 7));
        return $token !== '' ? $token : null;
    }

    private static function deny(int $statusCode, string $message, ?string $code = null): void
    {
        http_response_code($statusCode);
        header('Content-Type: application/json; charset=UTF-8');
        $body = ['error' => $message];
        if ($code !== null) {
            $body['code'] = $code;
        }
        echo json_encode($body);
        exit;
    }
}
