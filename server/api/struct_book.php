<?php
require_once("../connectBDD.php");

if (empty($_POST['idNumber']) || empty($_POST['idStruct'])) {
    http_response_code(400);
    echo json_encode(['error' => 'Missing required parameters']);
    exit;
}

try {
    $idNumber = htmlspecialchars($_POST['idNumber'], ENT_QUOTES, 'UTF-8');
    $idStruct = htmlspecialchars($_POST['idStruct'], ENT_QUOTES, 'UTF-8');

    $sql = "SELECT
        Book.idBook,
        Book.blanket,
        Book.title AS bookTitle,
        GROUP_CONCAT(DISTINCT Category.idCategory) AS idCategories,
        GROUP_CONCAT(DISTINCT Category.title SEPARATOR ', ') AS categoryTitle,
        Book.isPhysic,
        Book.electronic,
        Book.isAudio,
        Book.numberLike,
        Book.numberView,
        Structure.nameStruct
    FROM Book
    INNER JOIN StructBook ON Book.idBook = StructBook.idBook
    INNER JOIN Structure ON StructBook.idStruct = Structure.id
    INNER JOIN BookCategory ON Book.idBook = BookCategory.idBook
    INNER JOIN Category ON BookCategory.idCategory = Category.idCategory
    WHERE Structure.id = :idStruct
    GROUP BY Book.idBook
    ORDER BY StructBook.date DESC
    LIMIT 6";

    $stmt = $pdo->prepare($sql);
    $stmt->bindParam(':idStruct', $idStruct, PDO::PARAM_INT);
    $stmt->execute();
    $result = $stmt->fetchAll(PDO::FETCH_ASSOC);

    if ($result) {
        header('Content-Type: application/json; charset=utf-8');
        echo json_encode($result, JSON_UNESCAPED_UNICODE);
    } else {
        echo "RAS";
    }

} catch (PDOException $e) {
    http_response_code(500);
    error_log("Database error: " . $e->getMessage());
    echo json_encode(['error' => 'Database error']);
} catch (Exception $e) {
    http_response_code(500);
    error_log("Error: " . $e->getMessage());
    echo json_encode(['error' => 'Server error']);
}