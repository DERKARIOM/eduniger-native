<?php
    require_once("../connectBDD.php");
    require_once(__DIR__ . "/auth/AuthMiddleware.php");
    require_once(__DIR__ . "/auth/PasswordHasher.php");
    require_once(__DIR__ . "/auth/TokenStore.php");

    // SECURITE : cet endpoint acceptait auparavant N'IMPORTE QUEL nom de colonne et
    // N'IMPORTE QUELLE valeur, injectés directement dans le SQL sans paramètre lié
    // (ex: column=isAdmin, column=role, ou même du SQL arbitraire dans "column") — combiné
    // à un idNumber non authentifié, cela permettait de modifier N'IMPORTE QUEL champ
    // (y compris les droits admin) de N'IMPORTE QUEL utilisateur. Corrigé par :
    //  1) authentification obligatoire, idNumber = utilisateur authentifié uniquement ;
    //  2) liste blanche stricte des colonnes modifiables via cet endpoint (celles utilisées
    //     par l'écran "Paramètres" de l'app : nom, prénom, email, mot de passe) ;
    //  3) valeur toujours passée en paramètre lié (jamais concaténée dans le SQL).
    const ALLOWED_COLUMNS = ['name', 'firstName', 'email', 'password'];

    if (!empty($_POST['column']) && isset($_POST['newValues']) && $_POST['newValues'] !== '') {
        $auth = AuthMiddleware::requireAuth();
        $idNumber = $auth['sub'];

        $column = htmlspecialchars($_POST['column'], ENT_QUOTES, 'UTF-8');
        $newValues = htmlspecialchars($_POST['newValues'], ENT_QUOTES, 'UTF-8');

        if (!in_array($column, ALLOWED_COLUMNS, true)) {
            http_response_code(400);
            echo "false";
            exit;
        }

        try {
            if ($column === 'password') {
                // La valeur reçue est déjà SHA-256(mot de passe) calculé côté client (voir
                // PasswordUtil.hashPassword() dans SettingAdapter.java) ; on la re-hache en
                // bcrypt avant stockage, comme pour login/register (voir PasswordHasher.php).
                $newValues = PasswordHasher::hash($newValues);
            }

            // Nom de colonne validé contre une liste blanche fixe juste au-dessus : on peut
            // donc l'interpoler en toute sécurité dans le SQL (les noms de colonnes ne
            // peuvent pas être des paramètres liés PDO), tandis que la valeur reste toujours
            // liée via un paramètre.
            $sql = "UPDATE User SET `" . $column . "` = :newValue WHERE idUser = :idUser";
            $stmt = $pdo->prepare($sql);
            $stmt->execute([':newValue' => $newValues, ':idUser' => $idNumber]);

            if ($column === 'password') {
                // Changement de mot de passe : on révoque toutes les sessions actives
                // (autres appareils) par précaution, comme le recommande OWASP après un
                // changement de mot de passe.
                (new TokenStore($pdo))->revokeAllForUser($idNumber);
            }

            echo "true";
        } catch (Exception $e) {
            error_log($e->getMessage());
            echo "false";
        }
    } else {
        echo "false";
    }
