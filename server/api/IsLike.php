<?php
    require_once("../connectBDD.php");
    require_once(__DIR__ . "/auth/AuthMiddleware.php");

    // SECURITE : idNumber provenait auparavant du client ET était concaténé directement
    // dans le SQL (injection SQL possible). Identité prise du token vérifié ; requête
    // paramétrée.
    if (!empty($_POST['idBook']))
    {
        $auth = AuthMiddleware::requireAuth();
        $idNumber = $auth['sub'];
        $idBook = htmlspecialchars($_POST['idBook'], ENT_QUOTES, 'UTF-8');
        try
        {
            $sql = "SELECT idNumber FROM `Like` WHERE idNumber = :idNumber AND idBook = :idBook";
            $stmt = $pdo->prepare($sql);
            $stmt->execute([':idNumber' => $idNumber, ':idBook' => $idBook]);
            $result = $stmt->fetch();
            echo $result ? $result['idNumber'] : "ras";
        }catch(Exception $e)
        {
            error_log($e->getMessage());
            echo "ras";
        }
    }
