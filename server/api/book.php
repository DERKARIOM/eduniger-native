<?php
require_once("../connectBDD.php");

if (empty($_GET['id_number']) || empty($_GET['id_book'])) {
    http_response_code(400);
    echo json_encode(['error' => 'Paramètres id_number et id_book requis.']);
    exit;
}

$idNumber = filter_var($_GET['id_number'], FILTER_VALIDATE_INT);
$idBook   = preg_match('/^[a-zA-Z0-9]+$/', $_GET['id_book']) ? $_GET['id_book'] : false;

if ($idNumber === false || $idBook === false) {
    http_response_code(400);
    echo json_encode(['error' => 'Paramètres invalides.']);
    exit;
}

$idNumber = (int) $idNumber;
$idBook   = htmlspecialchars($idBook);

$sql = "SELECT
            Book.idBook,
            Book.blanket            AS bookBlanket,
            Book.title              AS bookTitle,
            Book.description,
            Book.isPhysic,
            Book.electronic,
            Book.isAudio,
            Book.numberLike,
            Book.numberNoLike,
            Book.numberSubscribe,
            Book.numberView,
            Book.size,
            Book.nbrPage,
            GROUP_CONCAT(DISTINCT Category.title    SEPARATOR ', ') AS categoryTitle,
            GROUP_CONCAT(DISTINCT Category.blanket  SEPARATOR ', ') AS categoryBlanket,
            GROUP_CONCAT(DISTINCT Structure.id      SEPARATOR ', ') AS idStructures,
            Author.idAuthor,
            Author.name,
            Author.firstName,
            Author.profile,
            Book.available,
            Author.profession,
            Author.call,
            Author.email,
            Author.whatsapp
        FROM Book
        INNER JOIN BookCategory ON Book.idBook             = BookCategory.idBook
        INNER JOIN Category     ON BookCategory.idCategory = Category.idCategory
        INNER JOIN Author       ON Book.idAuthor           = Author.idAuthor
        INNER JOIN StructBook   ON Book.idBook             = StructBook.idBook
        INNER JOIN Structure    ON StructBook.idStruct     = Structure.id
        WHERE Book.idBook = :idBook
        GROUP BY Book.idBook";

try {
    $stmt = $pdo->prepare($sql);
    $stmt->execute([':idBook' => $idBook]);
    $result = $stmt->fetch(PDO::FETCH_ASSOC);

    if ($result) {
        http_response_code(200);
        echo json_encode($result);
    } else {
        http_response_code(404);
        echo json_encode(['message' => 'Aucun livre trouvé.']);
    }
} catch (Exception $e) {
    http_response_code(500);
    error_log($e->getMessage());
    echo json_encode(['error' => 'Une erreur est survenue.']); // message detaille journalise cote serveur uniquement
}