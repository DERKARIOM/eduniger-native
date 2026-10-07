<?php
require_once("../connectBDD.php");
require_once(__DIR__ . "/auth/AuthMiddleware.php");

// SECURITE : voir StructCategoryIn.php pour le même correctif (identité prise du token
// vérifié, requête paramétrée).
if (!empty($_GET['categoryTitle'])) {
    $auth = AuthMiddleware::requireAuth();
    try {
        $idNumber = $auth['sub'];
        $categoryTitle = htmlspecialchars($_GET['categoryTitle'], ENT_QUOTES, 'UTF-8');
        $sql = "SELECT
            Book.idBook,
            Book.blanket,
            Book.title AS bookTitle,
            GROUP_CONCAT(DISTINCT Category.title SEPARATOR ', ') AS categoryTitle,
            Book.isPhysic,
            Book.electronic,
            Book.isAudio,
            Book.numberLike,
            Book.numberView,
            GROUP_CONCAT(DISTINCT Structure.nameStruct SEPARATOR ', ') AS nameStruct,
            Structure.id AS idStruct,
            MAX(StructBook.date) AS lastDate
        FROM Book
        INNER JOIN StructBook ON Book.idBook = StructBook.idBook
        INNER JOIN Structure ON StructBook.idStruct = Structure.id
        INNER JOIN StructUser ON StructBook.idStruct = StructUser.idStruct
        INNER JOIN BookCategory ON Book.idBook = BookCategory.idBook
        INNER JOIN Category ON BookCategory.idCategory = Category.idCategory
        WHERE StructUser.idUser = :idNumber AND Category.title = :categoryTitle
        GROUP BY Book.idBook, Structure.id
        ORDER BY lastDate DESC";
        $stmt = $pdo->prepare($sql);
        $stmt->execute([':idNumber' => $idNumber, ':categoryTitle' => $categoryTitle]);
        $result = $stmt->fetchAll();

        if ($result)
            echo json_encode($result);
        else
            echo "RAS";
    } catch (Exception $e) {
        error_log($e->getMessage());
        echo "error"; // message detaille journalise cote serveur uniquement (ne pas exposer au client)
    }
}
