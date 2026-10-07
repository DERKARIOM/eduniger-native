<?php
    require_once("../connectBDD.php");
    // Etape de pré-inscription (PreRegistrationActivity), utilisée AVANT la création d'un
    // compte User : ne peut donc pas exiger de token d'accès. Corrigé : requête
    // paramétrée (l'ancienne version concaténait tous les champs directement dans le SQL —
    // injection SQL possible).
    if(!empty($_POST['idNumber']) AND !empty($_POST['name']) AND !empty($_POST['firstName']) AND !empty($_POST['section']) AND !empty($_POST['departement']))
    {
        $idNumber = htmlspecialchars($_POST['idNumber'], ENT_QUOTES, 'UTF-8');
        $name = htmlspecialchars($_POST['name'], ENT_QUOTES, 'UTF-8');
        $firstName = htmlspecialchars($_POST['firstName'], ENT_QUOTES, 'UTF-8');
        $section = htmlspecialchars($_POST['section'], ENT_QUOTES, 'UTF-8');
        $departement = htmlspecialchars($_POST['departement'], ENT_QUOTES, 'UTF-8');
        try
        {
            $sql = "INSERT INTO Student(idNumber,`name`,firstName,section,department) VALUES(:idNumber, :name, :firstName, :section, :departement)";
            $stmt = $pdo->prepare($sql);
            $stmt->execute([
                ':idNumber' => $idNumber,
                ':name' => $name,
                ':firstName' => $firstName,
                ':section' => $section,
                ':departement' => $departement,
            ]);
            echo "true";
        }catch(Exception $e)
        {
            error_log($e->getMessage());
            echo "error"; // message detaille journalise cote serveur uniquement (ne pas exposer au client)
        }
    }
