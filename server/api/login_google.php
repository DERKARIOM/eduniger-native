<?php
require_once("../connectBDD.php");
require_once(__DIR__ . "/auth/PasswordHasher.php");
require_once(__DIR__ . "/auth/TokenStore.php");

if (!empty($_POST['id_token']) && !empty($_POST['token']) && !empty($_POST['version'])) {
    try {
        $id_token  = htmlspecialchars($_POST['id_token'],  ENT_QUOTES, 'UTF-8');
        $token_fcm = htmlspecialchars($_POST['token'],     ENT_QUOTES, 'UTF-8');
        $version   = htmlspecialchars($_POST['version'],   ENT_QUOTES, 'UTF-8');
        $firstName = htmlspecialchars($_POST['firstName'] ?? '', ENT_QUOTES, 'UTF-8');
        $name      = htmlspecialchars($_POST['name']      ?? '', ENT_QUOTES, 'UTF-8');
        $email     = htmlspecialchars($_POST['email']     ?? '', ENT_QUOTES, 'UTF-8');

        // ── 1. Vérification de la version (identique à login.php) ──────────
        $sqlVersion = "SELECT idVersion FROM Version LIMIT 1";
        $stmtVersion = $pdo->prepare($sqlVersion);
        $stmtVersion->execute();
        $resultVersion = $stmtVersion->fetch(PDO::FETCH_ASSOC);

        if ($resultVersion['idVersion'] != $version) {
            echo "expiresVersion";
            exit;
        }

        // ── 2. Vérification du token Google ────────────────────────────────
        // PROBLEME PRE-EXISTANT NON RESOLU (voir rapport) : ce fichier dépend de la
        // librairie google/apiclient via Composer, qui n'est PAS installée dans api/
        // (aucun composer.json/vendor/ dans ce dossier). Avant ce correctif, l'absence de
        // vendor/autoload.php provoquait une erreur fatale PHP non contrôlée (fuite
        // potentielle de chemins serveur). Ici, on échoue proprement avec un message
        // générique tant que la dépendance n'est pas installée, au lieu de crasher.
        // Implémenter une vérification JWT RS256 "maison" (comme Jwt.php le fait pour
        // HS256) nécessiterait de récupérer et mettre en cache les clés publiques Google
        // (JWKS, rotation régulière) : hors périmètre de cette intervention, qui porte sur
        // l'architecture Access/Refresh Token, pas sur l'intégration Google Sign-In.
        $vendorAutoload = __DIR__ . '/../vendor/autoload.php';
        if (!file_exists($vendorAutoload)) {
            error_log('login_google.php: google/apiclient non installé (vendor/autoload.php introuvable). Connexion Google indisponible.');
            http_response_code(503);
            echo "error";
            exit;
        }
        require_once $vendorAutoload;

        $client  = new Google_Client(['client_id' => getenv('GOOGLE_WEB_CLIENT_ID') ?: '']);
        $payload = $client->verifyIdToken($id_token);

        if (!$payload) {
            echo "error";
            exit;
        }

        $google_id    = $payload['sub'];
        $email_google = $payload['email']       ?? $email;
        $name_google  = $payload['family_name'] ?? $name;
        $first_google = $payload['given_name']  ?? $firstName;

        // ── 3. Recherche du compte par google_id ───────────────────────────
        $stmt = $pdo->prepare(
            "SELECT `name`, firstName, email, `profile`, `password`, profession, isAdmin, role, idUser
             FROM User WHERE google_id = :google_id"
        );
        $stmt->execute([':google_id' => $google_id]);
        $utilisateur = $stmt->fetch(PDO::FETCH_ASSOC);

        // ── 4. Sinon recherche par email ───────────────────────────────────
        if (!$utilisateur && !empty($email_google)) {
            $stmt = $pdo->prepare(
                "SELECT `name`, firstName, email, `profile`, `password`, profession, isAdmin, role, idUser
                 FROM User WHERE email = :email"
            );
            $stmt->execute([':email' => $email_google]);
            $utilisateur = $stmt->fetch(PDO::FETCH_ASSOC);

            // Lier le google_id au compte existant
            if ($utilisateur) {
                $stmtLink = $pdo->prepare(
                    "UPDATE User SET google_id = :google_id WHERE idUser = :idUser"
                );
                $stmtLink->execute([
                    ':google_id' => $google_id,
                    ':idUser'    => $utilisateur['idUser']
                ]);
            }
        }

        // ── 5. Création du compte si nouveau utilisateur ───────────────────
        if (!$utilisateur) {
            // BUG PRE-EXISTANT CORRIGE : idUser est la clé primaire (varchar) de User, PAS
            // une colonne auto-incrémentée — l'ancien code ne la renseignait jamais lors de
            // l'INSERT, ce qui aurait provoqué une erreur "column idUser cannot be null" à
            // la première tentative réelle d'utilisation. On génère ici un identifiant
            // stable et unique dérivé du compte Google.
            $newIdUser = 'g_' . $google_id;

            $hashed_password = PasswordHasher::hash(bin2hex(random_bytes(16)));

            $stmtInsert = $pdo->prepare("
                INSERT INTO User (idUser, google_id, email, `name`, firstName, `password`, profession, isAdmin, fcm_token)
                VALUES (:idUser, :google_id, :email, :name, :firstName, :password, :profession, :isAdmin, :fcmToken)
            ");
            $stmtInsert->execute([
                ':idUser'     => $newIdUser,
                ':google_id'  => $google_id,
                ':email'      => $email_google,
                ':name'       => $name_google,
                ':firstName'  => $first_google,
                ':password'   => $hashed_password,
                ':profession' => 0,
                ':isAdmin'    => 0,
                ':fcmToken'   => $token_fcm !== 'null' ? $token_fcm : '',
            ]);

            $stmt = $pdo->prepare(
                "SELECT `name`, firstName, email, `profile`, `password`, profession, isAdmin, role, idUser
                 FROM User WHERE idUser = :idUser"
            );
            $stmt->execute([':idUser' => $newIdUser]);
            $utilisateur = $stmt->fetch(PDO::FETCH_ASSOC);
        }

        // ── 6. Mise à jour du token FCM ────────────────────────────────────
        // BUG PRE-EXISTANT CORRIGE : la colonne s'appelle fcm_token, pas "token" (qui
        // n'existe pas sur User) — l'ancienne requête aurait échoué à chaque connexion.
        if ($token_fcm !== 'null' && !empty($token_fcm)) {
            $stmtToken = $pdo->prepare(
                "UPDATE User SET fcm_token = :fcmToken WHERE google_id = :google_id"
            );
            $stmtToken->execute([
                ':fcmToken'  => $token_fcm,
                ':google_id' => $google_id
            ]);
        }

        // ── 7. Émission des tokens Access + Refresh ─────────────────────────
        $role = $utilisateur['role'] ?: ($utilisateur['isAdmin'] ? 'ADMIN' : 'USER');
        $tokens = (new TokenStore($pdo))->issueTokenPair($utilisateur['idUser'], $role);

        // ── 8. Réponse JSON — même format que login.php ─────────────────────
        echo json_encode([
            'name'        => $utilisateur['name'],
            'firstName'   => $utilisateur['firstName'],
            'email'       => $utilisateur['email'],
            'profile'     => $utilisateur['profile'],
            'password'    => $utilisateur['password'],
            'profession'  => (string) $utilisateur['profession'],
            'isAdmin'     => (string) $utilisateur['isAdmin'],
            'role'        => $role,
            'accessToken' => $tokens['accessToken'],
            'refreshToken'=> $tokens['refreshToken'],
            'expiresIn'   => $tokens['expiresIn'],
        ]);

    } catch (Exception $e) {
        error_log($e->getMessage());
        echo "error";
    }
}
else
    echo "ras";
