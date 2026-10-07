<?php
    require_once("../connectBDD.php");
    require_once(__DIR__ . "/auth/AuthMiddleware.php");

    // SECURITE : idNumber provenait auparavant du client sans aucune vérification,
    // permettant de lire ET de marquer comme "vues" les notifications de N'IMPORTE QUEL
    // autre utilisateur (déni de notification pour la victime + fuite d'information).
    // Corrigé : identité prise du token vérifié, requêtes paramétrées (l'ancienne version
    // concaténait idNumber directement dans le SQL).
    $auth = AuthMiddleware::requireAuth();
    $idNumber = $auth['sub'];

    header('Content-Type: application/json; charset=UTF-8');

    try {
        $sql = "SELECT Notification.* FROM Notification
        LEFT JOIN NotifView
        ON Notification.idNotification = NotifView.idNotification
        AND NotifView.idUser = :idUser
        WHERE NotifView.idNotification IS NULL";
        $stmt = $pdo->prepare($sql);
        $stmt->execute([':idUser' => $idNumber]);

        $tab = [];
        $insertStmt = $pdo->prepare(
            "INSERT INTO NotifView (idNotification, idUser, date) VALUES (:idNotification, :idUser, NOW())"
        );

        while ($row = $stmt->fetch(PDO::FETCH_ASSOC)) {
            $tab[] = $row;
            $insertStmt->execute([
                ':idNotification' => $row['idNotification'],
                ':idUser' => $idNumber,
            ]);
        }

        echo json_encode($tab);
    } catch (Exception $e) {
        error_log($e->getMessage());
        echo json_encode([]);
    }
