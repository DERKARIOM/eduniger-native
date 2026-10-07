<?php
    require_once("../connectBDD.php");
    require_once(__DIR__ . "/auth/AuthMiddleware.php");

    // Endpoint réservé aux utilisateurs authentifiés ; requête paramétrée (l'ancienne
    // version concaténait idBook directement dans le SQL — injection SQL possible).
    if (!empty($_POST['idBook']))
    {
        try
        {
            AuthMiddleware::requireAuth();
            $idBook = htmlspecialchars($_POST['idBook'], ENT_QUOTES, 'UTF-8');
            $sql = "SELECT * FROM Audio WHERE idBook = :idBook";
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
