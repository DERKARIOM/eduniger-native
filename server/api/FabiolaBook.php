<?php
    require_once("../connectBDD.php");
    if(!empty($_POST['idNumber']))
    {
        try
        {
            $idNumber = htmlspecialchars($_POST['idNumber']);
            $sql = "SELECT Book.idBook,Book.blanket,Book.title AS bookTitle,Category.idCategory,Category.title AS categoryTitle,Book.isPhysic,Book.electronic,Book.isAudio,Book.numberLike,Book.numberView FROM Book INNER JOIN BookCategory on Book.idBook=BookCategory.idBook INNER JOIN Category ON BookCategory.idCategory=Category.idCategory WHERE isPhysic=1";
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
    
