<?php
    require_once("../connectBDD.php");
    require_once(__DIR__ . "/auth/AuthMiddleware.php");

    // SECURITE : voir Structure2.php pour le même correctif (idUser pris du token vérifié,
    // requête paramétrée).
    $auth = AuthMiddleware::requireAuth();
    $idUser = $auth['sub'];

    try {
        $sql = "SELECT DISTINCT id,nameStruct,logo,banner,author,adhererNumber,bookNumber,`description` FROM Structure LEFT JOIN StructUser ON Structure.id=StructUser.idStruct WHERE StructUser.idUser != :idUser OR StructUser.idUser IS NULL LIMIT 4";
        $stmt = $pdo->prepare($sql);
        $stmt->execute([':idUser' => $idUser]);
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
