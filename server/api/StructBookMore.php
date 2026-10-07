<?php
    require_once("../connectBDD.php");
    require_once(__DIR__ . "/auth/AuthMiddleware.php");

    // Endpoint réservé aux utilisateurs authentifiés (comme le reste de l'API applicative).
    // idStruct reste un identifiant public de structure ; requête paramétrée (l'ancienne
    // version concaténait idStruct directement dans le SQL — injection SQL possible).
    if (!empty($_POST['idStruct']) AND ctype_digit((string) $_POST['idStruct']))
    {
        try
        {
            AuthMiddleware::requireAuth();
            $idStruct = (int) $_POST['idStruct'];
            $sql = "SELECT Book.idBook,Book.blanket,Book.title AS bookTitle,Category.idCategory,Category.title AS categoryTitle,Book.isPhysic,Book.electronic,Book.isAudio,Book.numberLike,Book.numberView,Structure.nameStruct FROM Book INNER JOIN StructBook ON Book.idBook=StructBook.idBook INNER JOIN Structure ON StructBook.idStruct=Structure.id INNER JOIN BookCategory on Book.idBook=BookCategory.idBook INNER JOIN Category ON BookCategory.idCategory=Category.idCategory WHERE Structure.id = :idStruct ORDER BY StructBook.date DESC";
            $stmt = $pdo->prepare($sql);
            $stmt->execute([':idStruct' => $idStruct]);
            $result = $stmt->fetchAll();
            if($result)
            {
                echo json_encode($result);
            }
            else
                echo "RAS";
        }catch(Exception $e)
        {
            error_log($e->getMessage());
            echo "error"; // message detaille journalise cote serveur uniquement (ne pas exposer au client)
        }
    }
