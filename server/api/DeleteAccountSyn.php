<?php
    require_once("../connectBDD.php");
    require_once(__DIR__ . "/auth/AuthMiddleware.php");
    require_once(__DIR__ . "/auth/TokenStore.php");

    // SECURITE : la suppression de compte ne se fait plus JAMAIS sur un idNumber fourni
    // par le client (auparavant : n'importe qui pouvait supprimer N'IMPORTE QUEL compte en
    // connaissant juste son matricule, sans authentification). L'identité provient
    // désormais uniquement du token d'accès vérifié : un utilisateur ne peut supprimer que
    // SON PROPRE compte.
    $auth = AuthMiddleware::requireAuth();
    $idNumber = $auth['sub'];

    header('Content-Type: application/json; charset=UTF-8');

    try {
        $sql1 = "DELETE FROM User WHERE idUser = :idUser";
        $stmt1 = $pdo->prepare($sql1);
        $stmt1->execute([':idUser' => $idNumber]);

        if ($stmt1->rowCount() > 0) {
            // Revoque explicitement toutes les sessions (par securite/clarte), meme si la
            // suppression du User entraine deja la suppression en cascade des RefreshToken.
            (new TokenStore($pdo))->revokeAllForUser($idNumber);
            echo "true";
        } else {
            echo "false";
        }
    } catch (Exception $e) {
        error_log($e->getMessage());
        echo "false"; // message detaille journalise cote serveur uniquement (ne pas exposer au client)
    }
