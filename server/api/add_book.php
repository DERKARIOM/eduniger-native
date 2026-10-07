<?php
    require_once("../connectBDD.php");
    require_once(__DIR__ . "/auth/AuthMiddleware.php");

    // SECURITE : endpoint de création de contenu (livres), auparavant accessible sans
    // authentification et vulnérable à l'injection SQL sur plusieurs champs (idAuthor,
    // isPhysique, isPdf, isAudio, idCategory, idStruct concaténés sans validation
    // numérique). Aucun appelant n'a été trouvé dans l'app Android ni dans app-web
    // (probablement du code orphelin), mais il reste joignable publiquement : réservé
    // désormais aux comptes ADMIN, avec requêtes intégralement paramétrées.
    // NOTE : la correspondance positionnelle des colonnes dans les INSERT ci-dessous est
    // conservée à l'IDENTIQUE de l'original (y compris une bizarrerie pré-existante où
    // "available" reçoit la valeur d'is_pdf et "numberLike" reçoit is_audio) — ce n'est pas
    // corrigé ici pour ne pas modifier un comportement métier hors périmètre de cet audit
    // d'authentification ; voir le rapport pour le détail.
    if(!empty($_POST['id_number']) AND !empty($_POST['id_book']) AND !empty($_POST['title']) AND !empty($_POST['description']) AND !empty($_POST['id_author']))
    {
        try
        {
            AuthMiddleware::requireAuth('ADMIN');

            $idBook = htmlspecialchars($_POST['id_book'], ENT_QUOTES, 'UTF-8');
            $title = htmlspecialchars($_POST['title'], ENT_QUOTES, 'UTF-8');
            $description = htmlspecialchars($_POST['description'], ENT_QUOTES, 'UTF-8');
            $idAuthor = (int) $_POST['id_author'];
            $isPhysique = (int) ($_POST['is_physique'] ?? 0);
            $isPdf = (int) ($_POST['is_pdf'] ?? 0);
            $isAudio = (int) ($_POST['is_audio'] ?? 0);
            $idCategory = (int) ($_POST['id_category'] ?? 0);
            $idStruct = (int) ($_POST['id_struct'] ?? 0);
            $pdfSize = htmlspecialchars($_POST['pdf_size'] ?? '', ENT_QUOTES, 'UTF-8');
            $nbrPage = htmlspecialchars($_POST['nbr_page'] ?? '', ENT_QUOTES, 'UTF-8');
            $audioSize = htmlspecialchars($_POST['audio_size'] ?? '', ENT_QUOTES, 'UTF-8');
            $timeMax = htmlspecialchars($_POST['time_max'] ?? '', ENT_QUOTES, 'UTF-8');

            if($isPdf == 1 && $isAudio == 0)
            {
                $sql = "INSERT INTO Book VALUES (:idBook, :title, :description, :idAuthor, :blanket, :electronic, :isPhysic, :isAudio1, :isPdf, :isAudio2, 0,0,0,0, :pdfSize, :nbrPage)";
                $stmt = $pdo->prepare($sql);
                $ok = $stmt->execute([
                    ':idBook' => $idBook,
                    ':title' => $title,
                    ':description' => $description,
                    ':idAuthor' => $idAuthor,
                    ':blanket' => $idBook . '.png',
                    ':electronic' => $idBook . '.pdf',
                    ':isPhysic' => $isPhysique,
                    ':isAudio1' => $isAudio,
                    ':isPdf' => $isPdf,
                    ':isAudio2' => $isAudio,
                    ':pdfSize' => $pdfSize,
                    ':nbrPage' => $nbrPage . ' P',
                ]);
                if(!$ok)
                {
                    echo "false";
                    exit(1);
                }
            }
            if($isPdf == 0 && $isAudio == 1)
            {
                $sql = "INSERT INTO Book VALUES (:idBook, :title, :description, :idAuthor, :blanket, NULL, :isPhysic, :isAudio1, :isPdf, :isAudio2, 0,0,0,0, NULL, NULL)";
                $stmt = $pdo->prepare($sql);
                $ok = $stmt->execute([
                    ':idBook' => $idBook,
                    ':title' => $title,
                    ':description' => $description,
                    ':idAuthor' => $idAuthor,
                    ':blanket' => $idBook . '.png',
                    ':isPhysic' => $isPhysique,
                    ':isAudio1' => $isAudio,
                    ':isPdf' => $isPdf,
                    ':isAudio2' => $isAudio,
                ]);
                if($ok)
                {
                   $sqlAudio = "INSERT INTO Audio VALUES (NULL, :idBook, :fileName, :label, :audioSize, :timeMax)";
                   $stmtAudio = $pdo->prepare($sqlAudio);
                   $okAudio = $stmtAudio->execute([
                       ':idBook' => $idBook,
                       ':fileName' => $idBook . '.mp3',
                       ':label' => $idBook,
                       ':audioSize' => $audioSize,
                       ':timeMax' => $timeMax,
                   ]);
                   if(!$okAudio)
                   {
                        echo "false";
                        exit(1);
                   }
                }
                else
                {
                    echo "false";
                    exit(1);
                }

            }

            $sqlCategory = "INSERT INTO BookCategory VALUES (:idBook, :idCategory, NOW())";
            $stmtCategory = $pdo->prepare($sqlCategory);
            if($stmtCategory->execute([':idBook' => $idBook, ':idCategory' => $idCategory]))
            {
                $sqlStruct = "INSERT INTO StructBook VALUES (:idStruct, :idBook, NOW())";
                $stmtStruct = $pdo->prepare($sqlStruct);
                if($stmtStruct->execute([':idStruct' => $idStruct, ':idBook' => $idBook]))
                    echo "true";
                else
                    echo "false";

            }
            else
                echo "false";

        }catch(Exception $e)
        {
            error_log($e->getMessage());
            echo "error"; // message detaille journalise cote serveur uniquement (ne pas exposer au client)
        }
    }
