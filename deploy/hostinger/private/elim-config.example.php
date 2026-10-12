<?php
/**
 * Configuration PRIVÉE de l'API PHP historique d'Elim (Hostinger).
 *
 * À copier en  /home/<utilisateur>/private/elim-config.php  (HORS de public_html),
 * puis remplir. Ce fichier n'est JAMAIS versionné ni accessible depuis le web.
 * Il est chargé par public_html/eduniger/connectBDD.php.
 */
return [
    // Base MySQL créée dans hPanel → Bases de données → MySQL
    'db_host'     => 'localhost',
    'db_name'     => 'uXXXXXXXX_elim',
    'db_user'     => 'uXXXXXXXX_elim',
    'db_password' => 'MOT_DE_PASSE_FORT',

    // Secret de signature des jetons JWT (64 caractères aléatoires minimum).
    // Générer :  php -r "echo bin2hex(random_bytes(32)), PHP_EOL;"
    // ATTENTION : reprendre la valeur du serveur actuel pour ne pas déconnecter
    // tous les utilisateurs déjà connectés.
    'jwt_secret'  => 'A_REMPLACER',
];
