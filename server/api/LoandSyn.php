<?php
    require_once("../connectBDD.php");
    require_once(__DIR__ . "/auth/AuthMiddleware.php");

    // SECURITE : voir LoandClosing.php pour le même correctif.
    $auth = AuthMiddleware::requireAuth();
    $idNumber = $auth['sub'];

    try {
        $sql = "SELECT Loand.idLoand,Reservation.idNumber,Book.blanket,Book.title,Loand.dateLoand,Loand.realReturnDate,Reservation.deliveryDate FROM Loand INNER JOIN Reservation ON Loand.idReservation=Reservation.idReservation INNER JOIN Book ON Reservation.idBook=Book.idBook WHERE Reservation.idNumber = :idNumber AND Loand.view=0";
        $stmt = $pdo->prepare($sql);
        $stmt->execute([':idNumber' => $idNumber]);

        $tab = array();
        $updateStmt = $pdo->prepare("UPDATE `Loand` SET view = 1 WHERE idLoand = :idLoand");
        while ($row = $stmt->fetch()) {
            $tab[] = $row;
            $updateStmt->execute([':idLoand' => $row['idLoand']]);
        }
        echo json_encode($tab);
    } catch (Exception $e) {
        error_log($e->getMessage());
        echo json_encode([]);
    }
