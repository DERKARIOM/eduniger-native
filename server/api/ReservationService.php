<?php
    require_once("../connectBDD.php");
    require_once(__DIR__ . "/auth/AuthMiddleware.php");

    // SECURITE : idNumber provenait auparavant du client (fuite/altération des réservations
    // de N'IMPORTE QUEL autre utilisateur). Identité prise du token vérifié ; requête
    // paramétrée.
    $auth = AuthMiddleware::requireAuth();
    $idNumber = $auth['sub'];

    try {
        $sql = "SELECT state,idReservation,title FROM Reservation INNER JOIN Book ON Reservation.idBook=Book.idBook WHERE state=1 AND view=0 AND idNumber = :idNumber";
        $stmt = $pdo->prepare($sql);
        $stmt->execute([':idNumber' => $idNumber]);

        $tab = array();
        $updateStmt = $pdo->prepare("UPDATE Reservation SET view=1 WHERE idNumber = :idNumber AND idReservation = :idReservation");
        while ($row = $stmt->fetch()) {
            $tab[] = $row;
            $updateStmt->execute([':idNumber' => $idNumber, ':idReservation' => $row['idReservation']]);
        }
        echo json_encode($tab);
    } catch (Exception $e) {
        error_log($e->getMessage());
        echo json_encode([]);
    }
