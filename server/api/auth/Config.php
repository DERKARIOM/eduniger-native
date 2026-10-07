<?php
/**
 * Configuration centralisée de l'authentification par tokens.
 *
 * SECURITE : le secret de signature JWT n'est JAMAIS codé en dur ici — il est lu
 * uniquement depuis la variable d'environnement JWT_SECRET (configurée sur le serveur,
 * jamais commitée dans Git). Sans cette variable, l'API refuse de démarrer plutôt que
 * d'utiliser une valeur par défaut prévisible : un secret par défaut connu de tous
 * annulerait toute la sécurité des tokens (n'importe qui pourrait forger un access token
 * admin). Voir README_AUTH.md pour la commande de génération du secret.
 */
class AuthConfig
{
    // Durée de vie de l'Access Token : volontairement COURTE (voir demande explicite du
    // projet : "évite de créer des tokens avec une durée de validité excessivement
    // longue"). 15 minutes est la valeur standard de l'industrie pour un access token.
    public const ACCESS_TOKEN_TTL_SECONDS = 15 * 60;

    // Durée de vie du Refresh Token : plus longue, mais bornée (30 jours). Au-delà,
    // l'utilisateur doit se reconnecter avec identifiants. Peut être raccourcie si le
    // projet veut une politique plus stricte.
    public const REFRESH_TOKEN_TTL_SECONDS = 30 * 24 * 60 * 60;

    // Protection anti brute-force : verrouillage du compte après N échecs de connexion.
    public const MAX_FAILED_LOGIN_ATTEMPTS = 5;
    public const LOCKOUT_DURATION_SECONDS = 15 * 60;

    private static ?string $secret = null;

    public static function jwtSecret(): string
    {
        if (self::$secret !== null) {
            return self::$secret;
        }
        $secret = getenv('JWT_SECRET');
        if ($secret === false || trim($secret) === '') {
            // Echec explicite et journalisé plutôt qu'un secret par défaut dangereux.
            error_log('JWT_SECRET n\'est pas configuré : voir README_AUTH.md');
            http_response_code(500);
            header('Content-Type: application/json; charset=UTF-8');
            echo json_encode(['error' => 'Configuration serveur manquante (JWT_SECRET). Contactez l\'administrateur.']);
            exit;
        }
        if (strlen($secret) < 32) {
            error_log('JWT_SECRET est trop court (minimum 32 caractères recommandé)');
        }
        self::$secret = $secret;
        return self::$secret;
    }
}
