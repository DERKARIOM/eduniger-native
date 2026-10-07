<?php
// /var/www/html/eduniger/api/send_notification.php

header('Content-Type: application/json');
require_once("../connectBDD.php");
require_once(__DIR__ . "/auth/AuthMiddleware.php");

// ================================================================
// CONFIGURATION
// ================================================================

$serviceAccountPath = '/var/www/private/fabi-e56e6-service-account.json';
$projectId          = 'fabi-e56e6';

// ================================================================
// GÉNÉRATION DU TOKEN OAUTH2
// ================================================================

function getAccessToken(string $serviceAccountPath): string {
    if (!file_exists($serviceAccountPath)) {
        http_response_code(500);
        echo json_encode(['success' => false, 'error' => 'Fichier service-account introuvable']);
        exit;
    }

    $credentials = json_decode(file_get_contents($serviceAccountPath), true);

    if (!$credentials) {
        http_response_code(500);
        echo json_encode(['success' => false, 'error' => 'Fichier service-account invalide']);
        exit;
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
        http_response_code(500);
        echo json_encode([
            'success' => false,
            'error'   => 'Impossible d\'obtenir le token OAuth2',
            'detail'  => $response
        ]);
        exit;
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

$accessToken = getAccessToken($serviceAccountPath);

$result = sendFCMNotification(
    $fcmToken,
    $title,
    $message,
    $type,
    $extraData,
    $projectId,
    $accessToken
);

if ($result['httpCode'] === 200) {
    echo json_encode([
        'success' => true,
        'message' => 'Notification envoyée avec succès'
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