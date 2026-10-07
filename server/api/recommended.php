<?php
require_once("../connectBDD.php");
require_once(__DIR__ . "/auth/AuthMiddleware.php");

// Validation des paramètres
if (empty($_GET['version'])) {
    http_response_code(400);
    echo json_encode(['error' => 'Missing required parameters']);
    exit;
}

// SECURITE : id_number provenait auparavant du client (voir books.php pour le même
// correctif). Identité prise du token vérifié.
$auth = AuthMiddleware::requireAuth();

try {
    $idNumber = $auth['sub'];
    $version = htmlspecialchars($_GET['version'], ENT_QUOTES, 'UTF-8');
    
    // Vérification de la version avec requête préparée
    $sqlVersion = "SELECT idVersion FROM Version LIMIT 1";
    $stmtVersion = $pdo->prepare($sqlVersion);
    $stmtVersion->execute();
    $resultVersion = $stmtVersion->fetch(PDO::FETCH_ASSOC);
    
    if (!$resultVersion || $resultVersion['idVersion'] != $version) {
        echo "expiresVersion";
        exit;
    }
    
    // Requête principale optimisée avec paramètre lié
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
    GROUP_CONCAT(DISTINCT Structure.id SEPARATOR ', ') AS idStruct,
    GROUP_CONCAT(DISTINCT Structure.nameStruct SEPARATOR ', ') AS nameStruct,
    MAX(StructBook.date) AS lastDate
FROM Book
    INNER JOIN StructBook ON Book.idBook = StructBook.idBook
    INNER JOIN Structure ON StructBook.idStruct = Structure.id
    INNER JOIN StructUser ON StructBook.idStruct = StructUser.idStruct
    INNER JOIN BookCategory ON Book.idBook = BookCategory.idBook
    INNER JOIN Category ON BookCategory.idCategory = Category.idCategory
    WHERE StructUser.idUser = :idNumber
    GROUP BY Book.idBook
    ORDER BY lastDate DESC
    LIMIT 6";
    
    $stmt = $pdo->prepare($sql);
    $stmt->bindParam(':idNumber', $idNumber, PDO::PARAM_STR);
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