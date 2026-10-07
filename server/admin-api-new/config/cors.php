<?php

return [

    /*
    |--------------------------------------------------------------------------
    | Cross-Origin Resource Sharing (CORS) Configuration
    |--------------------------------------------------------------------------
    |
    | Here you may configure your settings for cross-origin resource sharing
    | or "CORS". This determines what cross-origin operations may execute
    | in web browsers. You are free to adjust these settings as needed.
    |
    | To learn more: https://developer.mozilla.org/en-US/docs/Web/HTTP/CORS
    |
    */

    'paths' => ['api/*', 'sanctum/csrf-cookie', 'storage/*','broadcasting/auth',
        'api/broadcasting/auth'],

    'allowed_methods' => ['*'],

    // 'allowed_origins' => ['https://uamlib.eu','https://admin.baseuam.org'],
    'allowed_origins' => [
    'http://localhost:8080',
    'http://localhost:8081',
    'http://localhost:8085',
    // Deploiement de test sur le reseau local (telephone Samsung / Termux+proot-distro).
    // Web (eduniger-admin, statique) servi sur ce port depuis le meme serveur.
    'http://192.168.49.1:8090',
    'http://172.20.10.10:8090',
],

    'allowed_origins_patterns' => [],
    
    'allowed_headers' => ['*'],

    'exposed_headers' => [],

    'max_age' => 0,

    'supports_credentials' => true,

];
