<?php
// /var/www/html/eduniger/api/update_token.php

header('Content-Type: application/json');
require_once("../connectBDD.php");
require_once(__DIR__ . "/auth/AuthMiddleware.php");

// ================================================================
// MAIN
// ================================================================

// Vérification méthode HTTP
if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    http_response_code(405);
    echo json_encode(['success' => false, 'error' => 'Méthode non autorisée']);
    exit;
}

// SECURITE : idNumber provenait auparavant du client sans vérification, ce qui
// permettait de réécrire le token FCM (push notifications) de N'IMPORTE QUEL autre
// utilisateur — et donc de détourner ses notifications push vers un autre appareil.
// Corrigé : l'identité vient uniquement du token d'accès vérifié.
$auth = AuthMiddleware::requireAuth();
$idNumber = $auth['sub'];
$fcmToken = trim($_POST['fcmToken'] ?? '');

if (empty($fcmToken)) {
    http_response_code(400);
    echo json_encode([
        'success' => false,
        'error'   => 'Paramètre manquant : fcmToken est requis'
    ]);
    exit;
}

// Vérification que l'utilisateur existe
$stmt = $pdo->prepare("SELECT idUser, fcm_token FROM User WHERE idUser = ? LIMIT 1");
$stmt->execute([$idNumber]);
$user = $stmt->fetch();

if (!$user) {
    http_response_code(404);
    echo json_encode([
        'success' => false,
        'error'   => 'Utilisateur introuvable'
    ]);
    exit;
}

// Vérification si le token est déjà à jour (évite une écriture inutile)
if ($user['fcm_token'] === $fcmToken) {
    echo json_encode([
        'success' => true,
        'message' => 'Token déjà à jour'
    ]);
    exit;
}

// Mise à jour du token FCM
// ✅ updated_at se met à jour automatiquement (ON UPDATE CURRENT_TIMESTAMP)
$stmt = $pdo->prepare("
    UPDATE User
    SET fcm_token = ?
    WHERE idUser  = ?
");

$updated = $stmt->execute([$fcmToken, $idNumber]);

if ($updated && $stmt->rowCount() > 0) {
    echo json_encode([
        'success' => true,
        'message' => 'Token FCM mis à jour avec succès'
    ]);
} else {
    http_response_code(500);
    echo json_encode([
        'success' => false,
        'error'   => 'Échec de la mise à jour du token'
    ]);
}