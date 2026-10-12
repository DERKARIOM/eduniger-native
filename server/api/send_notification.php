<?php
// /var/www/html/eduniger/api/send_notification.php

header('Content-Type: application/json');
require_once("../connectBDD.php");
require_once(__DIR__ . "/auth/AuthMiddleware.php");

// ================================================================
// CONFIGURATION
// ================================================================

// Projets Firebase capables d'envoyer des notifications, dans l'ordre d'essai.
//  - elim-16f44 : application Elim (com.naniger.elim) — projet principal.
//  - fabi-e56e6 : ancienne application EduNiger (com.ninotech.eduniger), conservée
//    tant que des utilisateurs l'ont encore installée.
// Un jeton FCM n'est valable que pour le projet qui l'a émis : si FCM répond
// SENDER_ID_MISMATCH / UNREGISTERED / INVALID_ARGUMENT, on essaie le projet suivant.
// Un projet dont le fichier service-account est absent est simplement ignoré.
// Dossier privé (hors web) contenant les comptes de service : défini par connectBDD.php
// (ELIM_PRIVATE_DIR, ex. /home/<u>/private sur Hostinger) ; /var/www/private sinon.
$privateDir  = rtrim(getenv('ELIM_PRIVATE_DIR') ?: '/var/www/private', '/');
$fcmProjects = [
    ['id' => 'elim-16f44', 'serviceAccount' => $privateDir . '/elim-16f44-service-account.json'],
    ['id' => 'fabi-e56e6', 'serviceAccount' => $privateDir . '/fabi-e56e6-service-account.json'],
];

// ================================================================
// GÉNÉRATION DU TOKEN OAUTH2
// ================================================================

function getAccessToken(string $serviceAccountPath): ?string {
    if (!is_readable($serviceAccountPath)) {
        error_log("send_notification: service-account introuvable : $serviceAccountPath");
        return null;
    }

    $credentials = json_decode(file_get_contents($serviceAccountPath), true);

    if (!$credentials || empty($credentials['client_email']) || empty($credentials['private_key'])) {
        error_log("send_notification: service-account invalide : $serviceAccountPath");
        return null;
    }

    $now = time();

    $header = rtrim(strtr(base64_encode(json_encode([
        'alg' => 'RS256',
        'typ' => 'JWT'
    ])), '+/', '-_'), '=');

    $payload = rtrim(strtr(base64_encode(json_encode([
        'iss'   => $credentials['client_email'],
        'scope' => 'https://www.googleapis.com/auth/firebase.messaging',
        'aud'   => 'https://oauth2.googleapis.com/token',
        'iat'   => $now,
        'exp'   => $now + 3600
    ])), '+/', '-_'), '=');

    $signingInput = $header . '.' . $payload;
    $signature    = '';
    openssl_sign($signingInput, $signature, $credentials['private_key'], 'SHA256');
    $signature = rtrim(strtr(base64_encode($signature), '+/', '-_'), '=');

    $jwt = $signingInput . '.' . $signature;

    $ch = curl_init('https://oauth2.googleapis.com/token');
    curl_setopt_array($ch, [
        CURLOPT_POST           => true,
        CURLOPT_RETURNTRANSFER => true,
        CURLOPT_HTTPHEADER     => ['Content-Type: application/x-www-form-urlencoded'],
        CURLOPT_POSTFIELDS     => http_build_query([
            'grant_type' => 'urn:ietf:params:oauth:grant-type:jwt-bearer',
            'assertion'  => $jwt
        ])
    ]);

    $response = json_decode(curl_exec($ch), true);
    $httpCode = curl_getinfo($ch, CURLINFO_HTTP_CODE);
    curl_close($ch);

    if ($httpCode !== 200 || !isset($response['access_token'])) {
        error_log("send_notification: token OAuth2 refusé pour $serviceAccountPath (HTTP $httpCode)");
        return null;
    }

    return $response['access_token'];
}

// ================================================================
// ENVOI DE LA NOTIFICATION FCM v1
// ================================================================

function sendFCMNotification(
    string $fcmToken,
    string $title,
    string $body,
    string $type,
    string $extraData,
    string $projectId,
    string $accessToken
): array {
    $url = "https://fcm.googleapis.com/v1/projects/{$projectId}/messages:send";

    $payload = json_encode([
        'message' => [
            'token' => $fcmToken,
            'data'  => [
                'type'      => $type,
                'title'     => $title,
                'body'      => $body,
                'extraData' => $extraData
            ],
            'android' => [
                'priority' => 'high'
            ]
        ]
    ]);

    $ch = curl_init($url);
    curl_setopt_array($ch, [
        CURLOPT_POST           => true,
        CURLOPT_RETURNTRANSFER => true,
        CURLOPT_HTTPHEADER     => [
            'Authorization: Bearer ' . $accessToken,
            'Content-Type: application/json'
        ],
        CURLOPT_POSTFIELDS => $payload
    ]);

    $response = json_decode(curl_exec($ch), true);
    $httpCode = curl_getinfo($ch, CURLINFO_HTTP_CODE);
    curl_close($ch);

    return [
        'httpCode' => $httpCode,
        'response' => $response
    ];
}

// ================================================================
// MAIN
// ================================================================

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    http_response_code(405);
    echo json_encode(['success' => false, 'error' => 'Méthode non autorisée']);
    exit;
}

// SECURITE : endpoint d'envoi de notification push administrative — auparavant
// accessible sans aucune authentification (n'importe qui pouvait envoyer une
// notification push, avec titre/message arbitraires, à n'importe quel utilisateur).
// Réservé maintenant aux comptes ADMIN. idNumber ici désigne le DESTINATAIRE choisi
// par l'admin, ce qui reste légitime.
AuthMiddleware::requireAuth('ADMIN');

// Récupération et validation des paramètres
$idNumber  = trim($_POST['idNumber']  ?? '');
$title     = trim($_POST['title']     ?? '');
$message   = trim($_POST['message']   ?? '');
$type      = trim($_POST['type']      ?? '');
$extraData = trim($_POST['extraData'] ?? '');

if (empty($idNumber) || empty($title) || empty($message)) {
    http_response_code(400);
    echo json_encode([
        'success' => false,
        'error'   => 'Paramètres manquants : idNumber, title et message sont requis'
    ]);
    exit;
}

// ================================================================
// RÉCUPÉRATION DU TOKEN FCM DEPUIS LA BDD
// ================================================================

$stmt = $pdo->prepare("SELECT fcm_token FROM User WHERE idUser = ? LIMIT 1");
$stmt->execute([$idNumber]);
$user = $stmt->fetch(PDO::FETCH_ASSOC);

if (!$user || empty($user['fcm_token']) || $user['fcm_token'] === 'null') {
    http_response_code(404);
    echo json_encode([
        'success' => false,
        'error'   => 'Token FCM introuvable pour cet utilisateur'
    ]);
    exit;
}

$fcmToken = $user['fcm_token'];

// ================================================================
// ENVOI
// ================================================================

// Erreurs FCM signifiant « ce jeton n'appartient pas à ce projet » : on passe au suivant.
function isWrongProjectError(array $result): bool {
    $status = $result['response']['error']['status'] ?? '';
    $code   = '';
    foreach (($result['response']['error']['details'] ?? []) as $detail) {
        if (!empty($detail['errorCode'])) { $code = $detail['errorCode']; }
    }
    return in_array($code, ['SENDER_ID_MISMATCH', 'UNREGISTERED', 'INVALID_ARGUMENT'], true)
        || in_array($status, ['PERMISSION_DENIED', 'NOT_FOUND', 'INVALID_ARGUMENT'], true);
}

$result     = null;
$usedProject = null;
foreach ($fcmProjects as $project) {
    $accessToken = getAccessToken($project['serviceAccount']);
    if ($accessToken === null) {
        continue; // projet non configuré sur ce serveur
    }
    $result = sendFCMNotification(
        $fcmToken,
        $title,
        $message,
        $type,
        $extraData,
        $project['id'],
        $accessToken
    );
    $usedProject = $project['id'];
    if ($result['httpCode'] === 200 || !isWrongProjectError($result)) {
        break;
    }
}

if ($result === null) {
    http_response_code(500);
    echo json_encode([
        'success' => false,
        'error'   => 'Aucun projet Firebase configuré (fichier service-account manquant)'
    ]);
} elseif ($result['httpCode'] === 200) {
    echo json_encode([
        'success' => true,
        'message' => 'Notification envoyée avec succès',
        'project' => $usedProject
    ]);
} else {
    $errorMessage = $result['response']['error']['message'] ?? 'Erreur inconnue';
    http_response_code(500);
    echo json_encode([
        'success' => false,
        'error'   => $errorMessage,
        'detail'  => $result['response']
    ]);
}