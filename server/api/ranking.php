<?php
    require_once("../connectBDD.php");
    require_once(__DIR__ . "/auth/AuthMiddleware.php");

    // SECURITE : idNumber provenait auparavant du client (voir books.php pour le même
    // correctif). Identité prise du token vérifié ; requête désormais paramétrée
    // (l'ancienne version concaténait idNumber directement dans le SQL).
    $auth = AuthMiddleware::requireAuth();
    {
        try
        {
            $idNumber = $auth['sub'];
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
            ORDER BY lastDate DESC";
            $stmt = $pdo->prepare($sql);
            $stmt->execute([':idNumber' => $idNumber]);
            $result = $stmt->fetchAll();
            if($result)
            {
                echo json_encode($result);
            }
            else
                echo "RAS";
        }catch(Exception $e)
        {
            error_log($e->getMessage());
            echo "error"; // message detaille journalise cote serveur uniquement (ne pas exposer au client)
        }
    }