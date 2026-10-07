<?php
// /var/www/html/eduniger/api/mark_reservation_viewed.php

header('Content-Type: application/json');
require_once("../connectBDD.php");

// ================================================================
// VÉRIFICATION MÉTHODE HTTP
// ================================================================

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    http_response_code(405);
    echo json_encode(['success' => false, 'error' => 'Méthode non autorisée']);
    exit;
}

// ================================================================
// VALIDATION DES PARAMÈTRES
// ================================================================

$idReservation = trim($_POST['idReservation'] ?? '');

if (empty($idReservation)) {
    http_response_code(400);
    echo json_encode([
        'success' => false,
        'error'   => 'Paramètre manquant : idReservation est requis'
    ]);
    exit;
}

// ================================================================
// MISE À JOUR view = 1
// ================================================================

try {
    // Vérifier que la réservation existe
    $stmtCheck = $pdo->prepare("
        SELECT idReservation, view 
        FROM Reservation 
        WHERE idReservation = ? 
        LIMIT 1
    ");
    $stmtCheck->execute([$idReservation]);
    $reservation = $stmtCheck->fetch(PDO::FETCH_ASSOC);

    if (!$reservation) {
        http_response_code(404);
        echo json_encode([
            'success' => false,
            'error'   => 'Réservation introuvable'
        ]);
        exit;
    }

    // Déjà vue → inutile d'écrire en BDD
    if ($reservation['view'] == 1) {
        echo json_encode([
            'success' => true,
            'message' => 'Réservation déjà marquée comme vue'
        ]);
        exit;
    }

    // Mise à jour view = 1
    $stmtUpdate = $pdo->prepare("
        UPDATE Reservation
        SET view = 1
        WHERE idReservation = ?
    ");
    $stmtUpdate->execute([$idReservation]);

    if ($stmtUpdate->rowCount() > 0) {
        echo json_encode([
            'success' => true,
            'message' => 'Réservation marquée comme vue'
        ]);
    } else {
        http_response_code(500);
        echo json_encode([
            'success' => false,
            'error'   => 'Échec de la mise à jour'
        ]);
    }

} catch (PDOException $e) {
    error_log($e->getMessage());
    http_response_code(500);
    echo json_encode([
        'success' => false,
        'error'   => $e->getMessage()
    ]);
}
