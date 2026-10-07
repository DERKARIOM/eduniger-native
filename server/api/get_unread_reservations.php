<?php
// /var/www/html/eduniger/api/get_unread_reservations.php

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

$idNumber = trim($_POST['idNumber'] ?? '');

if (empty($idNumber)) {
    http_response_code(400);
    echo json_encode([
        'success' => false,
        'error'   => 'Paramètre manquant : idNumber est requis'
    ]);
    exit;
}

// ================================================================
// RÉCUPÉRATION DES RÉSERVATIONS TRAITÉES NON VUES
// state = 2 | treat = 1 | view = 0
// ================================================================

try {
    $stmt = $pdo->prepare("
        SELECT
            r.idReservation,
            r.idNumber,
            r.idBook,
            r.idStruct,
            r.date,
            r.numberOfDay,
            r.expireDate,
            r.deliveryDate,
            r.state,
            r.treat,
            r.view,
            r.created_at,
            r.updated_at,
            b.title   AS bookTitle,
            b.blanket AS bookCover
        FROM Reservation r
        LEFT JOIN Book b ON r.idBook = b.idBook
        WHERE r.idNumber = :idNumber
        AND   r.state    = 2
        AND   r.treat    = 1
        AND   r.view     = 0
        ORDER BY r.updated_at DESC
    ");

    $stmt->execute([':idNumber' => $idNumber]);
    $reservations = $stmt->fetchAll(PDO::FETCH_ASSOC);

    echo json_encode([
        'success' => true,
        'count'   => count($reservations),
        'data'    => $reservations
    ]);

} catch (PDOException $e) {
    error_log($e->getMessage());
    http_response_code(500);
    echo json_encode([
        'success' => false,
        'error'   => $e->getMessage()
    ]);
}