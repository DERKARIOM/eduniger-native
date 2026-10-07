<?php
    require_once("../connectBDD.php");
    require_once(__DIR__ . "/auth/AuthMiddleware.php");

    // Endpoint réservé aux utilisateurs authentifiés ; requête paramétrée (l'ancienne
    // version concaténait idAuthor directement dans le SQL — injection SQL possible).
    if (!empty($_POST['idAuthor']))
    {
        try
        {
            AuthMiddleware::requireAuth();
            $idAuthor = htmlspecialchars($_POST['idAuthor'], ENT_QUOTES, 'UTF-8');
            $sql = "SELECT Book.idBook,Author.idAuthor,Book.blanket,Book.title AS bookTitle,Category.title AS categoryTitle,Book.isPhysic,Book.electronic,Book.isAudio,Book.numberLike,Book.numberNoLike FROM Book INNER JOIN Author ON Book.idAuthor=Author.idAuthor INNER JOIN BookCategory ON Book.idBook=BookCategory.idBook INNER JOIN Category ON BookCategory.idCategory=Category.idCategory WHERE Author.idAuthor = :idAuthor AND Book.electronic != 'null' ORDER BY Book.idBook DESC";
            $stmt = $pdo->prepare($sql);
            $stmt->execute([':idAuthor' => $idAuthor]);
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
