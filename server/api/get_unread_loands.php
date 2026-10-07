<?php
// /var/www/html/eduniger/api/get_unread_loands.php

header('Content-Type: application/json');
require_once("../connectBDD.php");
require_once(__DIR__ . "/auth/AuthMiddleware.php");

// ================================================================
// VÉRIFICATION MÉTHODE HTTP
// ================================================================

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    http_response_code(405);
    echo json_encode(['success' => false, 'error' => 'Méthode non autorisée']);
    exit;
}

// SECURITE : idUser provenait auparavant du client, permettant de lire les prêts
// (Loand) de N'IMPORTE QUEL autre utilisateur. Corrigé : identité prise du token vérifié.
$auth = AuthMiddleware::requireAuth();
$idUser = $auth['sub'];

// ================================================================
// RÉCUPÉRATION DES LOANDS NON VUS (view = 0)
// ================================================================

try {
    $stmt = $pdo->prepare("
        SELECT
            l.idLoand,
            l.idReservation,
            l.idBook,
            l.idAgentGiver,
            l.idAgentRecover,
            l.dateLoand,
            l.realReturnDate,
            l.actualReturnDate,
            l.closing,
            l.view,
            l.idStruct,
            l.idUser,
            l.created_at,
            l.updated_at,
            b.title   AS bookTitle,
            b.blanket AS bookCover
        FROM Loand l
        LEFT JOIN Book b ON l.idBook = b.idBook
        WHERE l.idUser = :idUser
        AND   l.view   = 0
        ORDER BY l.updated_at DESC
    ");

    $stmt->execute([':idUser' => $idUser]);
    $loands = $stmt->fetchAll(PDO::FETCH_ASSOC);

    echo json_encode([
        'success' => true,
        'count'   => count($loands),
        'data'    => $loands
    ]);

} catch (PDOException $e) {
    error_log($e->getMessage());
    http_response_code(500);
    echo json_encode([
        'success' => false,
        'error'   => 'Erreur serveur.'
    ]);
}