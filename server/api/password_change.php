<?php
    require_once("../connectBDD.php");
    require_once(__DIR__ . "/auth/PasswordHasher.php");
    require_once(__DIR__ . "/auth/RateLimiter.php");
    require_once(__DIR__ . "/auth/TokenStore.php");

    // Ecran "mot de passe oublié" (ChangePasswordActivity), utilisé AVANT connexion :
    // ne peut donc pas exiger de token d'accès (l'utilisateur n'en a pas encore). La
    // preuve d'identité reste la connaissance du matricule + de l'email associés au
    // compte (comportement existant, conservé pour ne pas casser cette fonctionnalité).
    //
    // Corrections de sécurité apportées, sans changer le contrat de l'écran :
    //  - requêtes paramétrées (l'ancienne version concaténait idNumber/email/mot de passe
    //    directement dans le SQL : injection SQL possible) ;
    //  - le nouveau mot de passe est haché en bcrypt avant stockage (au lieu d'être stocké
    //    tel quel) ;
    //  - limitation du nombre de tentatives par matricule (même mécanisme que le login),
    //    pour freiner le brute-force sur les couples (idNumber, email) ;
    //  - la réponse ne renvoie plus la ligne User complète (qui exposait l'ancien hash de
    //    mot de passe et d'autres données) ;
    //  - toutes les sessions actives de ce compte sont révoquées après un changement de
    //    mot de passe réussi (bonne pratique standard : un changement de mot de passe doit
    //    déconnecter les autres appareils).
    if (!empty($_POST['id_number']) && !empty($_POST['email']) && !empty($_POST['password_new'])) {
        $idNumber = htmlspecialchars($_POST['id_number'], ENT_QUOTES, 'UTF-8');
        $email = htmlspecialchars($_POST['email'], ENT_QUOTES, 'UTF-8');
        $passwordNew = htmlspecialchars($_POST['password_new'], ENT_QUOTES, 'UTF-8');

        $limiter = new LoginRateLimiter($pdo);
        $lockedSeconds = $limiter->secondsUntilUnlock($idNumber);
        if ($lockedSeconds > 0) {
            echo "false";
            exit;
        }

        try {
            $sql0 = "SELECT idUser FROM User WHERE idUser = :idUser AND email = :email";
            $stmt0 = $pdo->prepare($sql0);
            $stmt0->execute([':idUser' => $idNumber, ':email' => $email]);
            $utilisateur = $stmt0->fetch(PDO::FETCH_ASSOC);

            if ($utilisateur) {
                $sql = "UPDATE User SET password = :password WHERE idUser = :idUser";
                $stmt = $pdo->prepare($sql);
                $stmt->execute([
                    ':password' => PasswordHasher::hash($passwordNew),
                    ':idUser' => $idNumber,
                ]);

                $limiter->recordSuccess($idNumber);
                (new TokenStore($pdo))->revokeAllForUser($idNumber);

                echo "ok";
            } else {
                $limiter->recordFailure($idNumber);
                echo "noFoundIdNumberOrEmail";
            }
        } catch (Exception $e) {
            error_log($e->getMessage());
            echo "false";
        }
    } else {
        echo "false";
    }
