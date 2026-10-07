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
            $sql = "INSERT INTO `View` VALUES(:idNumber, :idBook, NOW())";
            $stmt = $pdo->prepare($sql);
            $stmt->execute([':idNumber' => $idNumber, ':idBook' => $idBook]);
            echo "true";
        }catch(Exception $e)
        {
            // Vue déjà enregistrée (contrainte unique) ou erreur : comportement inchangé,
            // silencieux côté client (l'ancienne version avalait déjà l'exception ici).
            error_log($e->getMessage());
            echo "ras";
        }
    }
