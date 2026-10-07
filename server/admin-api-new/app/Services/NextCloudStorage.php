<?php

namespace App\Services;

use League\Flysystem\Filesystem;
use League\Flysystem\WebDAV\WebDAVAdapter;
use Sabre\DAV\Client;
use Illuminate\Support\Facades\Storage;
use Illuminate\Support\Facades\Log;

class NextcloudStorage
{
    protected $filesystem;

    public function __construct()
    {
        // SECURITE : le nom d'utilisateur et le mot de passe ne doivent jamais avoir de valeur
        // par defaut codee en dur (l'ancien mot de passe "root" expose ici est compromis et doit
        // etre change cote serveur NextCloud). Configurez NEXTCLOUD_URI / NEXTCLOUD_USER /
        // NEXTCLOUD_PASS dans votre fichier .env (jamais commite dans Git).
        $client = new Client([
            'baseUri'  => env('NEXTCLOUD_URI', 'http://78.46.46.154/remote.php/webdav/'),
            'userName' => env('NEXTCLOUD_USER'),
            'password' => env('NEXTCLOUD_PASS'),
        ]);

        $adapter = new WebDAVAdapter($client);
        $this->filesystem = new Filesystem($adapter);
    }

    /**
     * Envoie un fichier dans Nextcloud
     */
    public function put(string $path, string $content): bool
    {
        try {
            $result = $this->filesystem->write($path, $content);
            if ($result !== false) {
                Log::info('Fichier envoyé avec succès dans Nextcloud', ['path' => $path]);
                return true;
            } else {
                Log::error('Échec de l\'envoi du fichier dans Nextcloud', ['path' => $path]);
                return false;
            }
        } catch (\Exception $e) {
            Log::error('Erreur lors de l\'envoi du fichier dans Nextcloud', ['path' => $path, 'error' => $e->getMessage()]);
            return false;
        }
    }

    /**
     * Récupère un fichier de Nextcloud
     */
    public function get(string $path): string
    {
        try {
            $content = $this->filesystem->read($path);
            Log::info('Fichier récupéré avec succès depuis Nextcloud', ['path' => $path]);
            return $content;
        } catch (\Exception $e) {
            Log::error('Erreur lors de la récupération du fichier depuis Nextcloud', ['path' => $path, 'error' => $e->getMessage()]);
            throw $e; // Relancer car essentiel
        }
    }

    /**
     * Supprime un fichier de Nextcloud
     */
    public function delete(string $path): bool
    {
        try {
            $result = (bool) $this->filesystem->delete($path);
            if ($result) {
                Log::info('Fichier supprimé avec succès depuis Nextcloud', ['path' => $path]);
            } else {
                Log::error('Échec de la suppression du fichier depuis Nextcloud', ['path' => $path]);
            }
            return $result;
        } catch (\Exception $e) {
            Log::error('Erreur lors de la suppression du fichier depuis Nextcloud', ['path' => $path, 'error' => $e->getMessage()]);
            return false;
        }
    }
}
