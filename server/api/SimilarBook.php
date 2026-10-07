<?php
    require_once("../connectBDD.php");
    require_once(__DIR__ . "/auth/AuthMiddleware.php");

    // Endpoint réservé aux utilisateurs authentifiés ; requête paramétrée (l'ancienne
    // version concaténait categoryTitle/idBook directement dans le SQL — injection SQL
    // possible).
    if (!empty($_POST['idBook']) AND !empty($_POST['categoryTitle']))
    {
        try
        {
            AuthMiddleware::requireAuth();
            $categoryTitle = htmlspecialchars($_POST['categoryTitle'], ENT_QUOTES, 'UTF-8');
            $idBook = htmlspecialchars($_POST['idBook'], ENT_QUOTES, 'UTF-8');
            $sql = "SELECT Book.idBook,Book.blanket,Book.title,Book.isPhysic,Book.Electronic,Book.isAudio,Book.numberLike FROM Book INNER JOIN BookCategory ON Book.idBook=BookCategory.idBook INNER JOIN Category ON BookCategory.idCategory=Category.idCategory WHERE Category.title = :categoryTitle AND Book.idBook != :idBook";
            $stmt = $pdo->prepare($sql);
            $stmt->execute([':categoryTitle' => $categoryTitle, ':idBook' => $idBook]);
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
