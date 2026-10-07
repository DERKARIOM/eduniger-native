<?php
require_once("../connectBDD.php");
require_once(__DIR__ . "/auth/AuthMiddleware.php");

// SECURITE : id_user provenait auparavant du client, ce qui permettait de lister les
// structures (et le statut isAdmin) de N'IMPORTE QUEL autre utilisateur. Corrigé :
// identité prise du token vérifié. Au passage, correction du type de paramètre lié
// (PDO::PARAM_INT alors qu'idUser est un varchar) qui pouvait fausser silencieusement
// les résultats pour des matricules non numériques.
$auth = AuthMiddleware::requireAuth();
$idUser = $auth['sub'];

try {
    $sql = "SELECT
                s.id,
                s.nameStruct,
                s.logo,
                s.banner,
                s.author,
                s.adhererNumber,
                s.bookNumber,
                s.description,
                su.isAdmin
            FROM Structure s
            LEFT JOIN StructUser su ON s.id = su.idStruct
            WHERE su.idUser = :idUser";

    $stmt = $pdo->prepare($sql);
    $stmt->bindParam(':idUser', $idUser, PDO::PARAM_STR);
    $stmt->execute();

    $result = $stmt->fetchAll(PDO::FETCH_ASSOC);

    header('Content-Type: application/json; charset=utf-8');

    if ($result) {
        echo json_encode($result);
    } else {
        echo json_encode([]);
    }
} catch (Exception $e) {
    http_response_code(500);
    header('Content-Type: application/json; charset=utf-8');
    echo json_encode(['error' => 'Une erreur est survenue']);
    error_log($e->getMessage()); // Log l'erreur côté serveur
}
