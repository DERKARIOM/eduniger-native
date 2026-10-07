<?php
    require_once("../connectBDD.php");
    require_once(__DIR__ . "/auth/AuthMiddleware.php");

    // SECURITE : idUser provenait auparavant du client sans vérification (n'importe qui
    // pouvait inscrire N'IMPORTE QUEL utilisateur dans N'IMPORTE QUELLE structure).
    // Corrigé : identité prise du token vérifié ; requête paramétrée (l'ancienne version
    // concaténait idStruct/idUser directement dans le SQL, sans même valider qu'idStruct
    // était bien numérique) ; idStruct validé comme entier.
    if (!empty($_POST['id_struct'])) {
        $auth = AuthMiddleware::requireAuth();
        $idUser = $auth['sub'];

        if (!ctype_digit((string) $_POST['id_struct'])) {
            http_response_code(400);
            echo "ras";
            exit;
        }
        $idStruct = (int) $_POST['id_struct'];

        try {
            $sql = "INSERT INTO StructUser (idStruct, idUser, date, isAdmin, registerNumber) VALUES (:idStruct, :idUser, NOW(), 0, NULL)";
            $stmt = $pdo->prepare($sql);
            $stmt->execute([':idStruct' => $idStruct, ':idUser' => $idUser]);
            echo "true";
        } catch (Exception $e) {
            error_log($e->getMessage());
            echo "ras";
        }
    } else {
        echo "ras";
    }
