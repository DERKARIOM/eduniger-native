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

    // Origines autorisées, séparées par des virgules, depuis .env (CORS_ALLOWED_ORIGINS).
    // Production : https://admin.kawtal-consulting.com. Valeurs par défaut = environnement local.
    'allowed_origins' => array_values(array_filter(array_map('trim', explode(',', env(
        'CORS_ALLOWED_ORIGINS',
        'http://localhost:8080,http://localhost:8081,http://localhost:8085,http://192.168.49.1:8090,http://172.20.10.10:8090'
    ))))),

    'allowed_origins_patterns' => [],
    
    'allowed_headers' => ['*'],

    'exposed_headers' => [],

    'max_age' => 0,

    'supports_credentials' => true,

];
