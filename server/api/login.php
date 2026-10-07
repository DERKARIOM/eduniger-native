<?php
require_once("../connectBDD.php");
require_once(__DIR__ . "/auth/PasswordHasher.php");
require_once(__DIR__ . "/auth/RateLimiter.php");
require_once(__DIR__ . "/auth/TokenStore.php");

if (!empty($_POST['id_number']) && !empty($_POST['password']) && !empty($_POST['token']) && !empty($_POST['version'])) {
    try {
        $idNumber = htmlspecialchars($_POST['id_number'], ENT_QUOTES, 'UTF-8');
        // Valeur déjà SHA-256(motDePasse) calculée côté client (comportement existant,
        // conservé — voir PasswordHasher.php pour le détail de la vérification/migration).
        $password = htmlspecialchars($_POST['password'], ENT_QUOTES, 'UTF-8');
        $token    = htmlspecialchars($_POST['token'],    ENT_QUOTES, 'UTF-8');
        $version  = htmlspecialchars($_POST['version'],  ENT_QUOTES, 'UTF-8');

        // ================================================================
        // VÉRIFICATION DE LA VERSION
        // ================================================================

        $stmtVersion = $pdo->prepare("SELECT idVersion FROM Version LIMIT 1");
        $stmtVersion->execute();
        $resultVersion = $stmtVersion->fetch(PDO::FETCH_ASSOC);

        if ($resultVersion['idVersion'] != $version) {
            echo "expiresVersion";
            exit;
        }

        // ================================================================
        // PROTECTION ANTI BRUTE-FORCE
        // ================================================================

        $rateLimiter = new LoginRateLimiter($pdo);
        if ($rateLimiter->secondsUntilUnlock($idNumber) > 0) {
            echo "accountLocked";
            exit;
        }

        // ================================================================
        // VÉRIFICATION DES IDENTIFIANTS
        // ================================================================
        // SECURITE : la comparaison du mot de passe se faisait auparavant directement en
        // SQL ("AND password = :password"), ce qui ne fonctionne qu'avec un stockage en
        // clair/SHA-256 non salé. Désormais : on récupère le hash stocké (quel que soit son
        // format, legacy SHA-256 ou bcrypt) puis on vérifie via PasswordHasher::verify(),
        // ce qui permet la migration progressive vers bcrypt (voir PasswordHasher.php).

        $stmt = $pdo->prepare("
            SELECT name, firstName, email, profile, password, profession, isAdmin, role
            FROM User
            WHERE idUser = :idNumber
        ");
        $stmt->execute([':idNumber' => $idNumber]);
        $utilisateur = $stmt->fetch(PDO::FETCH_ASSOC);

        if ($utilisateur && PasswordHasher::verify($password, $utilisateur['password'])) {

            $rateLimiter->recordSuccess($idNumber);

            // Migration transparente vers bcrypt au premier login réussi suivant le
            // déploiement (voir PasswordHasher::needsRehash()).
            if (PasswordHasher::needsRehash($utilisateur['password'])) {
                $rehash = $pdo->prepare("UPDATE User SET password = :password WHERE idUser = :idNumber");
                $rehash->execute([
                    ':password' => PasswordHasher::hash($password),
                    ':idNumber' => $idNumber,
                ]);
            }

            // ================================================================
            // MISE À JOUR DU TOKEN FCM SI DISPONIBLE
            // ================================================================

            if (!empty($token) && $token !== 'null') {
                updateFcmToken($pdo, $idNumber, $token);
            }

            // ================================================================
            // ÉMISSION DES TOKENS D'AUTHENTIFICATION (Access + Refresh)
            // ================================================================

            $role = $utilisateur['role'] ?: ($utilisateur['isAdmin'] ? 'ADMIN' : 'USER');
            $tokens = (new TokenStore($pdo))->issueTokenPair($idNumber, $role);

            // ================================================================
            // RETOUR DES DONNÉES UTILISATEUR
            // ================================================================
            // Note : le champ "password" est encore renvoyé ici (valeur bcrypt désormais,
            // plus une empreinte SHA-256 réutilisable) UNIQUEMENT parce que l'app Android
            // actuelle (LoginActivity.java, ligne ~476) le lit encore pour l'écran de
            // verrouillage local (LockActivity). C'est un problème résiduel documenté dans
            // le rapport final : à supprimer dès que le côté mobile utilisera le stockage
            // sécurisé des tokens à la place (voir tâche de migration Android).
            $utilisateur['accessToken'] = $tokens['accessToken'];
            $utilisateur['refreshToken'] = $tokens['refreshToken'];
            $utilisateur['expiresIn'] = $tokens['expiresIn'];
            $utilisateur['role'] = $role;

            echo json_encode($utilisateur);

        } elseif ($utilisateur) {
            // Mauvais mot de passe pour un compte existant.
            $rateLimiter->recordFailure($idNumber);
            echo "incorrectPassword";
        } else {
            echo "accountNotExist";
        }

    } catch (Exception $e) {
        error_log($e->getMessage());
        echo "error";
    }
}

// ================================================================
// FONCTION : MISE À JOUR DU FCM TOKEN
// ================================================================

function updateFcmToken(PDO $pdo, string $idNumber, string $token): void {

    // Vérifier si le token est déjà à jour (évite une écriture inutile)
    $stmtCheck = $pdo->prepare("SELECT fcm_token FROM User WHERE idUser = ? LIMIT 1");
    $stmtCheck->execute([$idNumber]);
    $current = $stmtCheck->fetch(PDO::FETCH_ASSOC);

    if ($current && $current['fcm_token'] === $token) {
        // Token déjà à jour, rien à faire
        return;
    }

    // Mise à jour du token
    // ✅ updated_at se met à jour automatiquement (ON UPDATE CURRENT_TIMESTAMP)
    $stmtUpdate = $pdo->prepare("
        UPDATE User
        SET fcm_token = ?
        WHERE idUser  = ?
    ");
    $stmtUpdate->execute([$token, $idNumber]);
}