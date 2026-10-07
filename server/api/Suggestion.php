<?php
    require_once("../connectBDD.php");
    require_once(__DIR__ . "/auth/AuthMiddleware.php");

    // SECURITE : idNumber provenait auparavant du client (une suggestion pouvait être
    // postée au nom de N'IMPORTE QUEL autre utilisateur) et était concaténé tel quel dans
    // le SQL. Identité prise du token vérifié ; requête paramétrée. Corrigé au passage :
    // la condition de validation avait une erreur de parenthésage
    // (!empty($_POST['model'] AND !empty($_POST['version']))) qui ne validait pas
    // réellement les deux champs indépendamment.
    if (!empty($_POST['objet']) AND !empty($_POST['message']) AND !empty($_POST['model']) AND !empty($_POST['version']))
    {
        try
        {
            $auth = AuthMiddleware::requireAuth();
            $idNumber = $auth['sub'];
            $model = htmlspecialchars($_POST['model'], ENT_QUOTES, 'UTF-8');
            $version = htmlspecialchars($_POST['version'], ENT_QUOTES, 'UTF-8');
            $objet = htmlspecialchars($_POST['objet'], ENT_QUOTES, 'UTF-8');
            $message = htmlspecialchars($_POST['message'], ENT_QUOTES, 'UTF-8');
            $sql = "INSERT INTO Suggestion VALUES (NULL, :idNumber, :objet, :message, :model, :version)";
            $stmt = $pdo->prepare($sql);
            $stmt->execute([
                ':idNumber' => $idNumber,
                ':objet' => $objet,
                ':message' => $message,
                ':model' => $model,
                ':version' => $version,
            ]);
            echo "true";
        }catch(Exception $e)
        {
            error_log($e->getMessage());
            echo "error"; // message detaille journalise cote serveur uniquement (ne pas exposer au client)
        }
    }
