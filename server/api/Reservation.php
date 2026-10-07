<?php
    require_once("../connectBDD.php");
    require_once(__DIR__ . "/auth/AuthMiddleware.php");

    // SECURITE : idNumber provenait auparavant du client (une réservation pouvait être
    // créée au nom de N'IMPORTE QUEL autre utilisateur) ; numberOfDay était concaténé tel
    // quel dans le SQL sans validation (injection SQL possible). Identité prise du token
    // vérifié ; numberOfDay validé comme entier ; requête paramétrée.
    if (!empty($_POST['idBook']) AND isset($_POST['numberOfDay']) AND ctype_digit((string) $_POST['numberOfDay']))
    {
        try
        {
            $auth = AuthMiddleware::requireAuth();
            $idNumber = $auth['sub'];
            $idBook = htmlspecialchars($_POST['idBook'], ENT_QUOTES, 'UTF-8');
            $numberOfDay = (int) $_POST['numberOfDay'];
            $sql = "INSERT INTO Reservation(idNumber,idBook,numberOfDay,date) VALUES (:idNumber, :idBook, :numberOfDay, NOW())";
            $stmt = $pdo->prepare($sql);
            $stmt->execute([
                ':idNumber' => $idNumber,
                ':idBook' => $idBook,
                ':numberOfDay' => $numberOfDay,
            ]);
            echo "true";
        }catch(Exception $e)
        {
            error_log($e->getMessage());
            echo "error"; // message detaille journalise cote serveur uniquement (ne pas exposer au client)
        }
    }
    else
        echo "false";
