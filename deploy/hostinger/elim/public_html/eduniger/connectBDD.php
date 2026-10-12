<?php
/**
 * Connexion MySQL de l'API PHP historique d'Elim — version PRODUCTION (Hostinger).
 *
 * Aucun secret ici : tout est lu depuis un fichier privé situé HORS de public_html :
 *   /home/<utilisateur>/private/elim-config.php   (modèle : elim-config.example.php)
 * Le dossier privé peut être forcé avec la variable d'environnement ELIM_PRIVATE_DIR.
 *
 * Le dossier privé est /home/<utilisateur>/private, quel que soit l'emplacement du
 * sous-domaine (Hostinger place souvent les sous-domaines dans
 * ~/domains/<domaine>/public_html/<sous-domaine>/).
 */
$elimHome = preg_match('#^(/home/[^/]+)#', __DIR__, $m) ? $m[1] : (getenv('HOME') ?: dirname(__DIR__, 4));
$elimPrivateDir = getenv('ELIM_PRIVATE_DIR') ?: $elimHome . '/private';
$elimConfigFile = $elimPrivateDir . '/elim-config.php';

if (!is_readable($elimConfigFile)) {
    error_log("connectBDD: configuration introuvable ($elimConfigFile)");
    http_response_code(500);
    header('Content-Type: application/json; charset=UTF-8');
    echo json_encode(['success' => false, 'error' => 'Configuration serveur manquante']);
    exit;
}
$elimConfig = require $elimConfigFile;

// Secret JWT exposé via l'environnement, là où api/auth/Config.php le lit (getenv).
if (!empty($elimConfig['jwt_secret']) && getenv('JWT_SECRET') === false) {
    putenv('JWT_SECRET=' . $elimConfig['jwt_secret']);
}
// Clé de l'API Google Gemini pour l'assistant Eduna (eduniger/ai/eduna_unified.php).
if (!empty($elimConfig['gemini_api_key']) && getenv('GEMINI_API_KEY') === false) {
    putenv('GEMINI_API_KEY=' . $elimConfig['gemini_api_key']);
}
// Dossier privé (comptes de service Firebase…) partagé avec send_notification.php.
if (getenv('ELIM_PRIVATE_DIR') === false) {
    putenv('ELIM_PRIVATE_DIR=' . $elimPrivateDir);
}

try {
    $pdo = new PDO(
        'mysql:host=' . $elimConfig['db_host'] . ';dbname=' . $elimConfig['db_name'] . ';charset=utf8mb4',
        $elimConfig['db_user'],
        $elimConfig['db_password'],
        [PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION]
    );
    // MySQL 8 (Hostinger) active ONLY_FULL_GROUP_BY par défaut, contrairement à l'ancienne
    // base MariaDB : plusieurs scripts (books.php, book.php, category_in.php…) en dépendent.
    $pdo->exec("SET SESSION sql_mode = REPLACE(@@SESSION.sql_mode, 'ONLY_FULL_GROUP_BY', '')");
} catch (PDOException $e) {
    // Ne jamais renvoyer le détail de l'erreur (identifiants, hôte) au client.
    error_log('connectBDD: ' . $e->getMessage());
    http_response_code(500);
    header('Content-Type: application/json; charset=UTF-8');
    echo json_encode(['success' => false, 'error' => 'Base de données indisponible']);
    exit;
}
unset($elimConfig, $elimHome, $m);
