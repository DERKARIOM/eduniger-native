<?php
require_once("../connectBDD.php");

header('Content-Type: application/json');
header('X-Content-Type-Options: nosniff');

function sendResponse(bool $success, mixed $data = null, string $message = '', int $httpCode = 200): void {
    http_response_code($httpCode);
    echo json_encode(['success' => $success, 'data' => $data, 'message' => $message]);
    exit;
}

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    sendResponse(false, null, 'Méthode non autorisée.', 405);
}

$idBook   = isset($_POST['idBook'])   ? trim($_POST['idBook'])   : null;
$idNumber = isset($_POST['idNumber']) ? trim($_POST['idNumber']) : null;

if (empty($idBook) || empty($idNumber)) {
    sendResponse(false, null, 'Paramètres manquants : idBook et idNumber sont requis.', 400);
}

if (!ctype_alnum($idBook) || !ctype_alnum($idNumber)) {
    sendResponse(false, null, 'Paramètres invalides.', 422);
}

try {
    // Vérifier que la réservation existe
    $checkStmt = $pdo->prepare(
        "SELECT state FROM Reservation 
         WHERE idBook = ? AND idNumber = ? 
         LIMIT 1"
    );
    $checkStmt->execute([$idBook, $idNumber]);
    $reservation = $checkStmt->fetch(PDO::FETCH_ASSOC);

    if (!$reservation) {
        sendResponse(false, null, 'Réservation introuvable.', 404);
    }

    if ($reservation['state'] == 2) {
        sendResponse(false, null, 'Impossible d\'annuler : le livre est en cours de consultation.', 403);
    }

    // Suppression physique de la réservation
    $deleteStmt = $pdo->prepare(
        "DELETE FROM Reservation 
         WHERE idBook = ? AND idNumber = ?"
    );
    $deleteStmt->execute([$idBook, $idNumber]);

    if ($deleteStmt->rowCount() > 0) {
        sendResponse(true, null, 'Réservation annulée avec succès.');
    } else {
        sendResponse(false, null, 'Echec de la suppression.', 500);
    }

} catch (PDOException $e) {
    error_log('DB Error [cancel_reservation]: ' . $e->getMessage());
    sendResponse(false, null, 'Erreur interne du serveur.', 500);
}