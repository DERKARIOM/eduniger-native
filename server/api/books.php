<?php
require_once("../connectBDD.php");
require_once(__DIR__ . "/auth/AuthMiddleware.php");

// SECURITE : id_number provenait auparavant du client, ce qui permettait de lister les
// livres des structures de N'IMPORTE QUEL autre utilisateur (fuite d'appartenance à une
// structure). Corrigé : identité prise du token vérifié.
$auth = AuthMiddleware::requireAuth();
$idNumber = $auth['sub'];

$sql = "SELECT 
            Book.idBook,
            Book.blanket,
            Book.title          AS bookTitle,
            GROUP_CONCAT(DISTINCT Category.idCategory)              AS idCategories,
            GROUP_CONCAT(DISTINCT Category.title    SEPARATOR ', ') AS categoryTitle,
            Book.isPhysic,
            Book.electronic,
            Book.isAudio,
            Book.numberLike,
            Book.numberView,
            GROUP_CONCAT(DISTINCT Structure.id          SEPARATOR ', ') AS idStructures,
            GROUP_CONCAT(DISTINCT Structure.nameStruct  SEPARATOR ', ') AS nameStruct,
            MAX(StructBook.date) AS lastDate
        FROM Book
        INNER JOIN StructBook    ON Book.idBook         = StructBook.idBook
        INNER JOIN Structure     ON StructBook.idStruct = Structure.id
        INNER JOIN StructUser    ON StructBook.idStruct = StructUser.idStruct
        INNER JOIN BookCategory  ON Book.idBook         = BookCategory.idBook
        INNER JOIN Category      ON BookCategory.idCategory = Category.idCategory
        WHERE StructUser.idUser = :idNumber
        GROUP BY Book.idBook
        ORDER BY lastDate DESC";

try {
    $stmt = $pdo->prepare($sql);
    $stmt->execute([':idNumber' => $idNumber]);
    $result = $stmt->fetchAll(PDO::FETCH_ASSOC);

    if ($result) {
        http_response_code(200);
        echo json_encode($result);
    } else {
        http_response_code(404);
        echo json_encode(['message' => 'Aucun résultat.']);
    }
} catch (Exception $e) {
    http_response_code(500);
    error_log($e->getMessage());
    echo json_encode(['error' => 'Une erreur est survenue.']); // message detaille journalise cote serveur uniquement
}