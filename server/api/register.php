<?php
require_once("../connectBDD.php");
require_once(__DIR__ . "/auth/PasswordHasher.php");

if (!empty($_POST['id_user']) && !empty($_POST['name']) && !empty($_POST['first_name']) && 
    !empty($_POST['email']) && !empty($_POST['password']) && !empty($_POST['profession']) && 
    !empty($_POST['version']) && !empty($_POST['fcm_token'])) {
    
    try {
        $idUser = htmlspecialchars($_POST['id_user'], ENT_QUOTES, 'UTF-8');
        $name = htmlspecialchars($_POST['name'], ENT_QUOTES, 'UTF-8');
        $firstName = htmlspecialchars($_POST['first_name'], ENT_QUOTES, 'UTF-8');
        $email = htmlspecialchars($_POST['email'], ENT_QUOTES, 'UTF-8');
        // Le mot de passe reçu est déjà SHA-256(motDePasse) calculé côté client (voir
        // PasswordUtil.hashPassword() dans l'app Android) ; on le re-hache en bcrypt avant
        // stockage (voir PasswordHasher.php) au lieu de le stocker tel quel comme avant.
        $password = PasswordHasher::hash(htmlspecialchars($_POST['password'], ENT_QUOTES, 'UTF-8'));
        $profession = htmlspecialchars($_POST['profession'], ENT_QUOTES, 'UTF-8');
        $version = htmlspecialchars($_POST['version'], ENT_QUOTES, 'UTF-8');
        $fcmToken = htmlspecialchars($_POST['fcm_token'], ENT_QUOTES, 'UTF-8');
        
        // Vérification de la version
        $sqlVersion = "SELECT idVersion FROM Version LIMIT 1";
        $stmtVersion = $pdo->prepare($sqlVersion);
        $stmtVersion->execute();
        $resultVersion = $stmtVersion->fetch(PDO::FETCH_ASSOC);
        
        if ($resultVersion['idVersion'] != $version) {
            echo "expiresVersion";
            exit;
        }
        
        // Vérifier si l'utilisateur existe déjà
        $sql = "SELECT idUser FROM User WHERE idUser = :idUser";
        $stmt = $pdo->prepare($sql);
        $stmt->execute([':idUser' => $idUser]);
        $result = $stmt->fetch(PDO::FETCH_ASSOC);
        
        if ($result) {
            echo "existingAccount";
            exit;
        }
        
        // Vérifier si l'email existe déjà
        $sql1 = "SELECT email FROM User WHERE email = :email";
        $stmt1 = $pdo->prepare($sql1);
        $stmt1->execute([':email' => $email]);
        $result1 = $stmt1->fetch(PDO::FETCH_ASSOC);
        
        if ($result1) {
            echo "existingEmail";
            exit;
        }
        
        // Insérer le nouvel utilisateur avec le token FCM
        $sql3 = "INSERT INTO User (`idUser`, `name`, `firstName`, `email`, `password`, `profession`, `fcm_token`) 
                 VALUES (:idUser, :name, :firstName, :email, :password, :profession, :fcmToken)";
        $stmt3 = $pdo->prepare($sql3);
        
        if ($stmt3->execute([
            ':idUser' => $idUser,
            ':name' => $name,
            ':firstName' => $firstName,
            ':email' => $email,
            ':password' => $password,
            ':profession' => $profession,
            ':fcmToken' => $fcmToken
        ])) {
            // Insérer dans StructUser avec les bonnes colonnes
            $sql4 = "INSERT INTO StructUser (`idStruct`, `idUser`, `date`, `isAdmin`, `registerNumber`) 
                     VALUES (1, :idUser, NOW(), 0, NULL)";
            $stmt4 = $pdo->prepare($sql4);
            $stmt4->execute([':idUser' => $idUser]);
            
            echo "ok";
        }
        
    } catch (Exception $e) {
        error_log($e->getMessage());
        echo "error"; // message detaille deja journalise via error_log ci-dessus (ne pas exposer au client)
    }
    
} else {
    echo "Remplir tous les champs svp (id_user, name, first_name, email, password, profession, version, fcm_token)";
}
?>