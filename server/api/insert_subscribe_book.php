<?php
    require_once("../connectBDD.php");
    require_once(__DIR__ . "/auth/AuthMiddleware.php");

    // SECURITE : voir insert_like.php pour le même correctif.
    if (!empty($_POST['idBook']))
    {
        $auth = AuthMiddleware::requireAuth();
        $idNumber = $auth['sub'];
        $idBook = htmlspecialchars($_POST['idBook'], ENT_QUOTES, 'UTF-8');
        try
        {
            $sql = "INSERT INTO `SubscribeBook` VALUES(:idNumber, :idBook, NOW())";
            $stmt = $pdo->prepare($sql);
            $stmt->execute([':idNumber' => $idNumber, ':idBook' => $idBook]);
            echo "true";
        }catch(Exception $e)
        {
            try {
                $sql = "DELETE FROM `SubscribeBook` WHERE idBook = :idBook AND idNumber = :idNumber";
                $stmt = $pdo->prepare($sql);
                $stmt->execute([':idBook' => $idBook, ':idNumber' => $idNumber]);
                echo "false";
            } catch (Exception $e2) {
                error_log($e2->getMessage());
                echo "ras";
            }
        }
    }
