<?php
// /var/www/html/eduniger/api/mark_loand_viewed.php

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

// SECURITE : idUser provenait auparavant du client. Le code vérifiait déjà que le
// couple (idLoand, idUser) correspondait, mais un idUser arbitraire restait accepté
// (permettant de sonder/marquer comme vus les prêts d'autrui si idLoand est deviné,
// ces identifiants étant probablement séquentiels). Corrigé : identité prise du token.
$auth = AuthMiddleware::requireAuth();
$idUser = $auth['sub'];
$idLoand = trim($_POST['idLoand'] ?? '');

if (empty($idLoand)) {
    http_response_code(400);
    echo json_encode([
        'success' => false,
        'error'   => 'Paramètre manquant : idLoand est requis'
    ]);
    exit;
}

// ================================================================
// VÉRIFICATION QUE LE LOAND EXISTE ET APPARTIENT À L'UTILISATEUR
// ================================================================

try {
    $stmtCheck = $pdo->prepare("
        SELECT idLoand, view
        FROM Loand
        WHERE idLoand = ?
        AND   idUser  = ?
        LIMIT 1
    ");
    $stmtCheck->execute([$idLoand, $idUser]);
    $loand = $stmtCheck->fetch(PDO::FETCH_ASSOC);

    if (!$loand) {
        http_response_code(404);
        echo json_encode([
            'success' => false,
            'error'   => 'Loand introuvable pour cet utilisateur'
        ]);
        exit;
    }

    // Déjà vu → inutile d'écrire en BDD
    if ($loand['view'] == 1) {
        echo json_encode([
            'success' => true,
            'message' => 'Loand déjà marqué comme vu'
        ]);
        exit;
    }

    // ================================================================
    // MISE À JOUR view = 1
    // ================================================================

    $stmtUpdate = $pdo->prepare("
        UPDATE Loand
        SET view = 1
        WHERE idLoand = ?
        AND   idUser  = ?
    ");
    $stmtUpdate->execute([$idLoand, $idUser]);

    if ($stmtUpdate->rowCount() > 0) {
        echo json_encode([
            'success' => true,
            'message' => 'Loand marqué comme vu'
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
        'error'   => 'Erreur serveur.'
    ]);
}