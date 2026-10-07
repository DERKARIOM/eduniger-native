<?php
    require_once("../connectBDD.php");
    require_once(__DIR__ . "/auth/AuthMiddleware.php");

    // SECURITE : idNumber provenait auparavant du client (un commentaire pouvait être posté
    // au nom de N'IMPORTE QUEL autre utilisateur) ; "message" n'était même pas échappé et
    // était concaténé tel quel dans le SQL (injection SQL directe). Identité prise du token
    // vérifié ; requête paramétrée.
    if (!empty($_POST['idBook']) AND !empty($_POST['message']))
    {
        try
        {
            $auth = AuthMiddleware::requireAuth();
            $idNumber = $auth['sub'];
            $idBook = htmlspecialchars($_POST['idBook'], ENT_QUOTES, 'UTF-8');
            $message = htmlspecialchars($_POST['message'], ENT_QUOTES, 'UTF-8');
            $sql = "INSERT INTO Comment VALUES (NULL, :idNumber, :idBook, NOW(), :message)";
            $stmt = $pdo->prepare($sql);
            $stmt->execute([
                ':idNumber' => $idNumber,
                ':idBook' => $idBook,
                ':message' => $message,
            ]);
            echo "true";
        }catch(Exception $e)
        {
            error_log($e->getMessage());
            echo "error"; // message detaille journalise cote serveur uniquement (ne pas exposer au client)
        }
    }
