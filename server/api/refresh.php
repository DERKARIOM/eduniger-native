<?php
require_once("../connectBDD.php");
require_once(__DIR__ . "/auth/TokenStore.php");

/**
 * Renouvellement de l'Access Token à partir d'un Refresh Token.
 *
 * Appelé par l'app mobile quand un appel API échoue avec 401 (access token expiré). Ne
 * requiert PAS d'Authorization Bearer (l'access token est justement expiré/absent à ce
 * moment-là) : la preuve d'identité est le refresh token lui-même.
 *
 * Réponses :
 *  - 200 + {accessToken, refreshToken, expiresIn} : rotation réussie, à utiliser pour la
 *    suite (l'ancien refresh token est désormais invalide — voir TokenStore::rotate()).
 *  - 401 + {"error":..., "code":"invalid_refresh_token"} : refresh token invalide, expiré,
 *    déjà utilisé (réutilisation détectée) ou inconnu. Le client DOIT alors déconnecter
 *    l'utilisateur et le renvoyer à l'écran de connexion (jamais de nouvelle tentative
 *    automatique de refresh, pour éviter toute boucle infinie 401 -> refresh -> 401 -> ...).
 */
header('Content-Type: application/json; charset=UTF-8');

if (empty($_POST['refresh_token'])) {
    http_response_code(400);
    echo json_encode(['error' => 'Paramètre manquant : refresh_token est requis.']);
    exit;
}

$presented = trim($_POST['refresh_token']);

try {
    $result = (new TokenStore($pdo))->rotate($presented);

    if ($result === null) {
        http_response_code(401);
        echo json_encode([
            'error' => 'Refresh token invalide, expiré ou déjà utilisé.',
            'code' => 'invalid_refresh_token',
        ]);
        exit;
    }

    echo json_encode($result);
} catch (Exception $e) {
    error_log($e->getMessage());
    http_response_code(500);
    echo json_encode(['error' => 'Erreur serveur.']);
}
