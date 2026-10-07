<?php
    require_once("../connectBDD.php");
    require_once(__DIR__ . "/auth/AuthMiddleware.php");

    // Donnée publique (catalogue d'auteurs), endpoint réservé aux utilisateurs authentifiés.
    AuthMiddleware::requireAuth();

    try {
        $sql = "SELECT * FROM Author ORDER BY level DESC LIMIT 10";
        $stmt = $pdo->prepare($sql);
        $stmt->execute();
        $result = $stmt->fetchAll();
        if ($result) {
            echo json_encode($result);
        } else {
            echo "RAS";
        }
    } catch (Exception $e) {
        error_log($e->getMessage());
        echo "error"; // message detaille journalise cote serveur uniquement (ne pas exposer au client)
    }
