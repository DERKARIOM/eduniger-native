<?php
    require_once("../connectBDD.php");
    if(!empty($_POST['idNumber']) AND !empty($_POST['title']) AND !empty($_POST['description']) AND !empty($_POST['category']) AND !empty($_POST['pdf']) AND !empty($_POST['audio']))
    {
        try
        {
            $idNumber = htmlspecialchars($_POST['idNumber']);
            $title = $_POST['title'];
            $description = $_POST['description'];
            $category = htmlspecialchars($_POST['category']);
            $isPhisic = htmlspecialchars($_POST['isPhisic']);
            $pdf = htmlspecialchars($_POST['pdf']);
            $audio = htmlspecialchars($_POST['audio']);
            $sql = "INSERT INTO RegisterAuthor VALUES (null, '$idNumber', '$title', '$description', '$category', $isPhisic, '$pdf', '$audio', 0, now())";
            $stmt = $pdo->prepare($sql);
            if(!$stmt->execute())
            {
                echo "false";
                    exit(1);
            }
            else
                echo "true";
 
        }catch(Exception $e)
        {
            error_log($e->getMessage());
            echo "error"; // message detaille journalise cote serveur uniquement (ne pas exposer au client)
        }
       
    }
    else
        echo "OK";
    