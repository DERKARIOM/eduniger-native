<?php
require_once("../connectBDD.php");
require_once(__DIR__ . "/auth/AuthMiddleware.php");

// SECURITE : voir structure.php pour le même correctif (identité prise du token vérifié ;
// correction du type de paramètre lié PDO::PARAM_INT -> PDO::PARAM_STR, idUser étant un
// varchar).
$auth = AuthMiddleware::requireAuth();
$idUser = $auth['sub'];

try {
    $sql = "SELECT DISTINCT
                s.id,
                s.nameStruct,
                s.logo,
                s.banner,
                s.author,
                s.adhererNumber,
                s.bookNumber,
                s.description
            FROM Structure s
            LEFT JOIN StructUser su ON s.id = su.idStruct
            WHERE su.idUser != :idUser OR su.idUser IS NULL
            ORDER BY s.id DESC
            LIMIT 2";

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
