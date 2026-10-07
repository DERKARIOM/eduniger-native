<?php
    require_once("../connectBDD.php");
    require_once(__DIR__ . "/auth/AuthMiddleware.php");

    // SECURITE : voir adherer_struct.php (même correctif). Un utilisateur ne peut plus se
    // désinscrire (ni désinscrire quelqu'un d'autre) d'une structure que la sienne.
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
            $sql = "DELETE FROM StructUser WHERE idUser = :idUser AND idStruct = :idStruct";
            $stmt = $pdo->prepare($sql);
            $stmt->execute([':idUser' => $idUser, ':idStruct' => $idStruct]);
            echo "true";
        } catch (Exception $e) {
            error_log($e->getMessage());
            echo "ras";
        }
    } else {
        echo "ras";
    }
