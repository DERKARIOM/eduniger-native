<?php
    require_once("../connectBDD.php");
    require_once(__DIR__ . "/auth/AuthMiddleware.php");

    // SECURITE : idNumber provenait auparavant du client ET était concaténé directement
    // dans le SQL (injection SQL possible), ce qui permettait de "liker"/"unliker" un
    // livre au nom de N'IMPORTE QUEL autre utilisateur. Identité prise du token vérifié ;
    // requêtes paramétrées. Logique de bascule (insert, puis suppression si déjà "liké")
    // conservée à l'identique.
    if (!empty($_POST['idBook']))
    {
        $auth = AuthMiddleware::requireAuth();
        $idNumber = $auth['sub'];
        $idBook = htmlspecialchars($_POST['idBook'], ENT_QUOTES, 'UTF-8');
        try
        {
            $sql = "INSERT INTO `Like` VALUES(:idNumber, :idBook, NOW())";
            $stmt = $pdo->prepare($sql);
            $stmt->execute([':idNumber' => $idNumber, ':idBook' => $idBook]);
            echo "true";
        }catch(Exception $e)
        {
            try {
                $sql = "DELETE FROM `Like` WHERE idBook = :idBook AND idNumber = :idNumber";
                $stmt = $pdo->prepare($sql);
                $stmt->execute([':idBook' => $idBook, ':idNumber' => $idNumber]);
                echo "false";
            } catch (Exception $e2) {
                error_log($e2->getMessage());
                echo "ras";
            }
        }
    }
