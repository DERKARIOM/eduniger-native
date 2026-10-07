<?php
    require_once("../connectBDD.php");
    require_once(__DIR__ . "/auth/AuthMiddleware.php");

    // Endpoint réservé aux utilisateurs authentifiés. idBook reste un paramètre de
    // requête normal (les commentaires d'un livre sont une donnée partagée entre membres,
    // pas une donnée personnelle) ; requête paramétrée (l'ancienne version concaténait
    // idBook directement dans le SQL — injection SQL possible).
    if (!empty($_POST['idBook']))
    {
        AuthMiddleware::requireAuth();
        try
        {
            $idBook = htmlspecialchars($_POST['idBook'], ENT_QUOTES, 'UTF-8');
            $sql = "SELECT User.idUser,User.name,User.firstName,Comment.message FROM Comment INNER JOIN User ON Comment.idNumber=User.idUser WHERE idBook = :idBook ORDER BY idComment";
            $stmt = $pdo->prepare($sql);
            $stmt->execute([':idBook' => $idBook]);
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
