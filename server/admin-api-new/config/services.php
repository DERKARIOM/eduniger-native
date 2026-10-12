<?php

return [

    /*
    |--------------------------------------------------------------------------
    | Third Party Services
    |--------------------------------------------------------------------------
    |
    | This file is for storing the credentials for third party services such
    | as Mailgun, Postmark, AWS and more. This file provides the de facto
    | location for this type of information, allowing packages to have
    | a conventional file to locate the various service credentials.
    |
    */

    'postmark' => [
        'token' => env('POSTMARK_TOKEN'),
    ],

    'ses' => [
        'key' => env('AWS_ACCESS_KEY_ID'),
        'secret' => env('AWS_SECRET_ACCESS_KEY'),
        'region' => env('AWS_DEFAULT_REGION', 'us-east-1'),
    ],

    'resend' => [
        'key' => env('RESEND_KEY'),
    ],

    'slack' => [
        'notifications' => [
            'bot_user_oauth_token' => env('SLACK_BOT_USER_OAUTH_TOKEN'),
            'channel' => env('SLACK_BOT_USER_DEFAULT_CHANNEL'),
        ],
    ],

    // URL de l'API PHP legacy (api/), utilisee pour l'envoi de notifications push
    // depuis SendApiController. Centralisee ici (au lieu d'etre codee en dur dans
    // le controleur) pour permettre de changer d'environnement (dev/prod) via .env,
    // comme demande dans l'audit (coherence avec la configuration centralisee).
    // Connexion Google (application mobile). GOOGLE_CLIENT_IDS = identifiant(s) du client
    // OAuth « Application Web » du projet Firebase elim-16f44, separes par des virgules.
    // C'est l'audience (aud) attendue dans l'ID token envoye par l'application.
    'google' => [
        'client_ids' => array_values(array_filter(array_map('trim',
            explode(',', (string) env('GOOGLE_CLIENT_IDS', ''))))),
    ],

    'legacy_api' => [
        'url' => env('LEGACY_API_URL', 'https://api.eduniger.com'),
    ],

];
