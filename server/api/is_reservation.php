<?php
require_once("../connectBDD.php");

header('Content-Type: application/json');
header('X-Content-Type-Options: nosniff');

// Fonction de réponse JSON standardisée
function sendResponse(bool $success, mixed $data = null, string $message = '', int $httpCode = 200): void {
    http_response_code($httpCode);
    echo json_encode([
        'success' => $success,
        'data'    => $data,
        'message' => $message
    ]);
    exit;
}

// Validation des paramètres GET
$idNumber = isset($_GET['idNumber']) ? trim($_GET['idNumber']) : null;
$idBook   = isset($_GET['idBook'])   ? trim($_GET['idBook'])   : null;

if (empty($idNumber) || empty($idBook)) {
    sendResponse(false, null, 'Paramètres manquants : idNumber et idBook sont requis.', 400);
}

// Validation : on accepte uniquement des valeurs alphanumériques
if (!ctype_alnum($idNumber) || !ctype_alnum($idBook)) {
    sendResponse(false, null, 'Paramètres invalides.', 422);
}

try {
    // Requête préparée avec des marqueurs positionnels (anti-injection SQL)
    $sql  = "SELECT `idNumber`, `state`, `treat` 
             FROM `Reservation` 
             WHERE `idNumber` = ? 
             AND `idBook` = ?";

    $stmt = $pdo->prepare($sql);
    $stmt->execute([$idNumber, $idBook]);

    $result = $stmt->fetch(PDO::FETCH_ASSOC);

    if ($result) {
        sendResponse(true, $result);
    } else {
        sendResponse(false, null, 'Aucune réservation trouvée.', 404);
    }

} catch (PDOException $e) {
    // Ne jamais exposer le détail de l'erreur en production
    error_log('DB Error [getReservation]: ' . $e->getMessage());
    sendResponse(false, null, 'Erreur interne du serveur.', 500);
}