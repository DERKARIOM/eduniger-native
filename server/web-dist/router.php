<?php
/**
 * Routeur minimal pour le serveur integre de PHP, afin de servir le build de
 * production de eduniger-admin (SPA React/Vite) correctement :
 *  - si l'URL demandee correspond a un vrai fichier present dans dist/ (JS, CSS,
 *    image, favicon...), on laisse le serveur integre le servir directement
 *    (return false) ;
 *  - sinon (ex: /agents/5, /books, une route geree par React Router côté client),
 *    on retourne index.html, qui laisse React Router afficher la bonne page.
 *
 * Usage : php -S 0.0.0.0:8090 -t . router.php   (execute depuis le dossier dist/)
 */

$path = urldecode(parse_url($_SERVER['REQUEST_URI'], PHP_URL_PATH));
$file = __DIR__ . $path;

if ($path !== '/' && is_file($file)) {
    return false;
}

readfile(__DIR__ . '/index.html');
