<?php
    require_once("../connectBDD.php");
    require_once(__DIR__ . "/auth/AuthMiddleware.php");

    // NOTE : ce mécanisme de notification s'appuie sur Notification.idNumber, dont la
    // contrainte de clé étrangère pointe vers Student (pas User) — un reliquat de
    // l'ancien schéma "Student". Le point d'appel côté app Android
    // (NotificationService.java) est entièrement commenté/désactivé : ce fichier est donc
    // du code mort dans l'app actuelle. Durci ici (injection SQL corrigée, authentification
    // ajoutée) par précaution plutôt que supprimé, au cas où il serait réactivé.
    $auth = AuthMiddleware::requireAuth();
    $idNumber = $auth['sub'];

    try {
        $sql = "SELECT `idNotification`,`message`,`type`,`reference`,Book.title FROM `Notification` INNER JOIN Book ON Notification.reference=Book.idBook WHERE view=0 AND idNumber = :idNumber";
        $stmt = $pdo->prepare($sql);
        $stmt->execute([':idNumber' => $idNumber]);

        $tab = array();
        $updateStmt = $pdo->prepare("UPDATE `Notification` SET view=1 WHERE idNumber = :idNumber AND idNotification = :idNotification");
        while ($row = $stmt->fetch()) {
            $tab[] = $row;
            $updateStmt->execute([':idNumber' => $idNumber, ':idNotification' => $row['idNotification']]);
        }
        echo json_encode($tab);
    } catch (Exception $e) {
        error_log($e->getMessage());
        echo json_encode([]);
    }
