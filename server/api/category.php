<?php
    require_once("../connectBDD.php");
    if(!empty($_GET['id_number']))
    {
        try
        {
            $matricule = htmlspecialchars($_GET['id_number']);
            $sql = "SELECT * FROM Category";
            $stmt = $pdo->prepare($sql);
            $stmt->execute();
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