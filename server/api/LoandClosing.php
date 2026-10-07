<?php
    require_once("../connectBDD.php");
    require_once(__DIR__ . "/auth/AuthMiddleware.php");

    // SECURITE : idNumber provenait auparavant du client, permettant de lire (et de faire
    // évoluer l'état "vu"/closing de) les prêts de N'IMPORTE QUEL autre utilisateur.
    // Identité prise du token vérifié ; requête paramétrée.
    $auth = AuthMiddleware::requireAuth();
    $idNumber = $auth['sub'];

    try {
        $sql = "SELECT Loand.idLoand,Reservation.idNumber,Book.blanket,Book.title,Loand.dateLoand,Loand.realReturnDate,Reservation.deliveryDate FROM Loand INNER JOIN Reservation ON Loand.idReservation=Reservation.idReservation INNER JOIN Book ON Reservation.idBook=Book.idBook WHERE Reservation.idNumber = :idNumber AND Loand.view=1 AND closing=1";
        $stmt = $pdo->prepare($sql);
        $stmt->execute([':idNumber' => $idNumber]);

        $tab = array();
        $updateStmt = $pdo->prepare("UPDATE `Loand` SET view = 2 WHERE idLoand = :idLoand");
        while ($row = $stmt->fetch()) {
            $tab[] = $row;
            // idLoand provient de notre propre requête (clé primaire numérique), pas du
            // client : pas de risque d'injection ici, mais on paramètre par cohérence.
            $updateStmt->execute([':idLoand' => $row['idLoand']]);
        }
        echo json_encode($tab);
    } catch (Exception $e) {
        error_log($e->getMessage());
        echo json_encode([]);
    }
