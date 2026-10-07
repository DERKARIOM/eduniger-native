<?php
    require_once("../connectBDD.php");
    // NOTE : endpoint déjà non fonctionnel avant cette intervention — il référence une
    // table `StudentAccount` qui N'EXISTE PAS dans le schéma actuel (base/eduniger.sql),
    // et son activité Android (ChangeEmailActivity) n'est déclarée dans aucun appel
    // startActivity(...) du projet : code mort des deux côtés. Il plantait auparavant avec
    // une erreur fatale non interceptée (aucun try/catch) à chaque appel — fuite potentielle
    // de détails serveur. Durci ici (requête paramétrée, erreurs interceptées et
    // journalisées côté serveur) par précaution, mais reste fonctionnellement inopérant
    // tant que la table cible n'est pas créée ou que l'endpoint n'est pas réécrit contre
    // `User` (voir UpdateSyn.php, colonne "email", pour le mécanisme actif équivalent).
    // Recommandation : supprimer ce fichier et ChangeEmailActivity.java si confirmé inutile.
    if(!empty($_POST['idNumber']) AND !empty($_POST['password']) AND !empty($_POST['emailNew']))
    {
        $idNumber = htmlspecialchars($_POST['idNumber'], ENT_QUOTES, 'UTF-8');
        $password = htmlspecialchars($_POST['password'], ENT_QUOTES, 'UTF-8');
        $emailNew = htmlspecialchars($_POST['emailNew'], ENT_QUOTES, 'UTF-8');
        try {
            $sql0 = "SELECT * FROM StudentAccount INNER JOIN Student ON StudentAccount.idNumber=Student.idNumber WHERE StudentAccount.idNumber = :idNumber AND password = :password";
            $stmt0 = $pdo->prepare($sql0);
            $stmt0->execute([':idNumber' => $idNumber, ':password' => $password]);
            $utilisateur = $stmt0->fetch();
            if($utilisateur)
            {
                $sql = "UPDATE StudentAccount SET email = :emailNew WHERE idNumber = :idNumber";
                $stmt = $pdo->prepare($sql);
                $stmt->execute([':emailNew' => $emailNew, ':idNumber' => $idNumber]);
                echo json_encode($utilisateur);
            }
            else
                echo "accountNotExist";
        } catch (Exception $e) {
            error_log($e->getMessage());
            echo "false";
        }
    }
    else
        echo "false";
