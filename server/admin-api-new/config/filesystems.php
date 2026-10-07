<?php

return [

    /*
    |--------------------------------------------------------------------------
    | Default Filesystem Disk
    |--------------------------------------------------------------------------
    |
    | Here you may specify the default filesystem disk that should be used
    | by the framework. The "local" disk, as well as a variety of cloud
    | based disks are available to your application for file storage.
    |
    */

    'default' => env('FILESYSTEM_DISK', 'local'),

    /*
    |--------------------------------------------------------------------------
    | Filesystem Disks
    |--------------------------------------------------------------------------
    |
    | Below you may configure as many filesystem disks as necessary, and you
    | may even configure multiple disks for the same driver. Examples for
    | most supported storage drivers are configured here for reference.
    |
    | Supported drivers: "local", "ftp", "sftp", "s3"
    |
    */

    'disks' => [

        'local' => [
            'driver' => 'local',
            'root' => storage_path('app/private'),
            'serve' => true,
            'throw' => false,
            'lock' => 0,
            'report' => false,
        ],

        'public' => [
            'driver' => 'local',
            'root' => storage_path('app/public'),
            'url' => env('APP_URL').'/storage',
            'visibility' => 'public',
            'throw' => false,
            'lock' => 0,
            'report' => false,
        ],

        's3' => [
            'driver' => 's3',
            'key' => env('AWS_ACCESS_KEY_ID'),
            'secret' => env('AWS_SECRET_ACCESS_KEY'),
            'region' => env('AWS_DEFAULT_REGION'),
            'bucket' => env('AWS_BUCKET'),
            'url' => env('AWS_URL'),
            'endpoint' => env('AWS_ENDPOINT'),
            'use_path_style_endpoint' => env('AWS_USE_PATH_STYLE_ENDPOINT', false),
            'throw' => false,
            'report' => false,
        ],
        'nextcloud' => [
            'driver' => 'webdav',
            // SECURITE : l'URL, le nom d'utilisateur et le mot de passe etaient auparavant codes
            // en dur ici (identifiants "root" en clair, identiques a ceux trouves ailleurs dans le
            // projet) et doivent etre consideres comme compromis : changez ce mot de passe cote
            // serveur NextCloud et renseignez les variables d'environnement ci-dessous dans .env
            // (jamais commite). Voir aussi app/Services/NextCloudStorage.php qui utilise deja env().
            'baseUri' => env('NEXTCLOUD_URI', 'http://78.46.46.154/remote.php/webdav/'),
            'userName' => env('NEXTCLOUD_USERNAME'),
            'password' => env('NEXTCLOUD_PASSWORD'),
        ],
        'hetzner' => [
            'driver' => 'sftp',
            'host' => env('HETZNER_SFTP_HOST'),
            'username' => env('HETZNER_SFTP_USERNAME'),
            'password' => env('HETZNER_SFTP_PASSWORD'),
            'port' => (int) env('HETZNER_SFTP_PORT', 22),
            'root' => env('HETZNER_SFTP_ROOT', '/srv/books'),
            'timeout' => 30,
        ],
        'private' => [
        'driver' => 'local',
        'root' => storage_path('app/private'), // ou tout autre chemin hors de public
        'visibility' => 'private',
        'throw' => false,
        'lock' => 0,
    ],

    ],

    /*
    |--------------------------------------------------------------------------
    | Symbolic Links
    |--------------------------------------------------------------------------
    |
    | Here you may configure the symbolic links that will be created when the
    | `storage:link` Artisan command is executed. The array keys should be
    | the locations of the links and the values should be their targets.
    |
    */

    'links' => [
        public_path('storage') => storage_path('app/public'),
    ],

];
