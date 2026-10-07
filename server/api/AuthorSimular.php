<?php
    require_once("../connectBDD.php");
    require_once(__DIR__ . "/auth/AuthMiddleware.php");

    // Endpoint public (catalogue), réservé aux utilisateurs authentifiés comme le reste
    // de l'API. Note : malgré son nom, le paramètre "idUser" désigne ici un idAuthor
    // (l'auteur à exclure des suggestions "auteurs similaires"), pas l'utilisateur courant
    // — comportement fonctionnel inchangé, seule la requête est désormais paramétrée
    // (l'ancienne version concaténait la valeur directement dans le SQL).
    AuthMiddleware::requireAuth();

    if (!empty($_POST['idUser'])) {
        try {
            $idAuthorToExclude = htmlspecialchars($_POST['idUser'], ENT_QUOTES, 'UTF-8');
            $sql = "SELECT * FROM Author WHERE idAuthor != :idAuthor ORDER BY level DESC LIMIT 10";
            $stmt = $pdo->prepare($sql);
            $stmt->execute([':idAuthor' => $idAuthorToExclude]);
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
    }
