<?php

namespace App\Http\Controllers;

use Illuminate\Support\Facades\Storage;
use Symfony\Component\HttpFoundation\BinaryFileResponse;
use Symfony\Component\HttpFoundation\ResponseHeaderBag;
use Symfony\Component\HttpKernel\Exception\NotFoundHttpException;
use Illuminate\Support\Facades\Log;
use App\Models\Book;
use App\Models\Audio;
use App\Models\Author;
use App\Models\Structure;
use App\Models\Category;
use Exception;

class FileController extends Controller
{
    /**
     * Sert un fichier sécurisé en fonction du type et de la structure.
     *
     * @param int    $idStruct  Identifiant de la structure (si applicable)
     * @param string $type      Type de fichier (blanket, pdf, audio, etc.)
     * @param string $filename  Nom du fichier
     * @return BinaryFileResponse
     * @throws NotFoundHttpException
     */
    public function secure(int $idStruct, string $type, string $filename)
    {
        try {
            // Nettoyage du nom du fichier
            $filename = $this->sanitizePath($filename);
            
            // Récupération du chemin absolu en fonction du type
            $path = $this->getPathForType($idStruct, $type, $filename);

            // Vérification de l'existence du fichier
            $disk = Storage::disk('private');
            if (!$disk->exists($path)) {
                Log::warning('Fichier non trouvé', [
                    'idStruct' => $idStruct,
                    'type'     => $type,
                    'filename' => $filename,
                    'path'     => $path,
                ]);
                throw new NotFoundHttpException();
            }

            $fullPath = $disk->path($path);
            
            // Détection du type MIME
            $mimeType = mime_content_type($fullPath) ?: 'application/octet-stream';

            // Création de la réponse
            $response = new BinaryFileResponse($fullPath);
            $response->headers->set('Content-Type', $mimeType);

            // Définition du comportement d'affichage
            if ($mimeType === 'application/pdf') {
                $response->setContentDisposition(ResponseHeaderBag::DISPOSITION_INLINE, $filename);
            } else {
                $response->setContentDisposition(ResponseHeaderBag::DISPOSITION_ATTACHMENT, $filename);
            }

            // Gestion du cache
            $response->setPrivate();
            $response->setMaxAge(86400); // 24h
            $response->headers->addCacheControlDirective('must-revalidate', true);
            $response->setEtag(md5_file($fullPath));
            $response->setLastModified(\DateTime::createFromFormat('U', filemtime($fullPath)));

            // Vérification de la condition 304 (non modifié)
            if ($response->isNotModified(request())) {
                return $response;
            }

            return $response;
        } catch (NotFoundHttpException $e) {
            // Laisser l'exception se propager pour que Laravel gère la 404
            throw $e;
        } catch (Exception $e) {
            Log::error('Erreur lors de la récupération du fichier', [
                'idStruct' => $idStruct,
                'type'     => $type,
                'filename' => $filename,
                'message'  => $e->getMessage(),
                'trace'    => $e->getTraceAsString(),
            ]);
            abort(500, 'Impossible de récupérer le fichier.');
        }
    }

    /**
     * Sert un fichier PUBLIC (sans authentification), utilisé à la fois par le
     * catalogue public du Web et par l'application mobile pour afficher les
     * couvertures/PDF/audio des livres consultés par un LECTEUR (pas un agent).
     *
     * Pourquoi pas /secure ici : /secure exige un agent authentifié rattaché à la
     * structure (middleware structure.scope), or les lecteurs (Web public et mobile)
     * n'ont pas de compte agent. Comme il n'y a pas d'authentification pour limiter
     * l'accès, on vérifie à la place que le fichier demandé correspond réellement à
     * un enregistrement existant (livre/auteur) rattaché à cette structure, plutôt
     * que de servir n'importe quel nom de fichier "qui a l'air valide" - ça évite de
     * transformer cette route en listing arbitraire du dossier de stockage.
     *
     * Types volontairement exclus (jamais servis ici, même avec un nom valide) :
     * pub, backup - réservés à /secure (usage admin/agent). category a ete ajoute
     * aux types publics : les couvertures de categorie sont affichees dans le
     * catalogue lecteur (Web public + mobile), au meme titre que logo/banner.
     * logo et banner sont également publics (logos/bannières de structure, affichés
     * sans authentification côté Web comme côté mobile).
     *
     * @param int    $idStruct
     * @param string $type      blanket | pdf | audio | profil | logo | banner
     * @param string $filename
     * @return BinaryFileResponse
     */
    public function publicShow(int $idStruct, string $type, string $filename)
    {
        try {
            $filename = $this->sanitizePath($filename);

            $publicTypes = ['blanket', 'pdf', 'audio', 'profil', 'logo', 'banner', 'category'];
            if (!in_array($type, $publicTypes)) {
                abort(404);
            }

            $exists = match ($type) {
                'blanket' => Book::where('blanket', $filename)
                    ->whereHas('structures', fn ($q) => $q->where('idStruct', $idStruct))
                    ->exists(),
                'pdf' => Book::where('electronic', $filename)
                    ->whereHas('structures', fn ($q) => $q->where('idStruct', $idStruct))
                    ->exists(),
                'audio' => (function () use ($idStruct, $filename) {
                    $audio = Audio::where('audio', $filename)->first();
                    return $audio && Book::where('idBook', $audio->idBook)
                        ->whereHas('structures', fn ($q) => $q->where('idStruct', $idStruct))
                        ->exists();
                })(),
                'profil' => Author::where('profile', $filename)->exists(),
                'logo' => Structure::where('id', $idStruct)
                    ->where('logo', $filename)
                    ->exists(),
                'banner' => Structure::where('id', $idStruct)
                    ->where('banner', $filename)
                    ->exists(),
                'category' => Category::where('blanket', $filename)->exists(),
                default => false,
            };

            if (!$exists) {
                Log::warning('Accès public refusé : fichier non rattaché à un enregistrement existant', [
                    'idStruct' => $idStruct,
                    'type'     => $type,
                    'filename' => $filename,
                ]);
                abort(404);
            }

            $path = $this->getPathForType($idStruct, $type, $filename);
            $disk = Storage::disk('private');
            if (!$disk->exists($path)) {
                throw new NotFoundHttpException();
            }

            $fullPath = $disk->path($path);
            $mimeType = mime_content_type($fullPath) ?: 'application/octet-stream';

            $response = new BinaryFileResponse($fullPath);
            $response->headers->set('Content-Type', $mimeType);
            $response->setContentDisposition(
                $mimeType === 'application/pdf'
                    ? ResponseHeaderBag::DISPOSITION_INLINE
                    : ResponseHeaderBag::DISPOSITION_ATTACHMENT,
                $filename
            );

            $response->setPublic();
            $response->setMaxAge(3600);
            $response->setEtag(md5_file($fullPath));
            $response->setLastModified(\DateTime::createFromFormat('U', filemtime($fullPath)));

            if ($response->isNotModified(request())) {
                return $response;
            }

            return $response;
        } catch (NotFoundHttpException $e) {
            throw $e;
        } catch (Exception $e) {
            Log::error('Erreur lors de la récupération du fichier public', [
                'idStruct' => $idStruct,
                'type'     => $type,
                'filename' => $filename,
                'message'  => $e->getMessage(),
            ]);
            abort(500, 'Impossible de récupérer le fichier.');
        }
    }

    /**
     * Nettoie le nom du fichier pour éviter les tentatives d'élévation de répertoire.
     *
     * @param string $path
     * @return string
     */
    private function sanitizePath(string $path): string
    {
        // Suppression des slashs initiaux
        $path = ltrim($path, '/\\');
        // Suppression des tentatives de remontée (..)
        $path = str_replace(['../', '..\\'], '', $path);
        // Suppression de tout autre séparateur non autorisé (on ne garde que le nom)
        $path = basename($path);
        return $path;
    }

    /**
     * Détermine le chemin de stockage en fonction du type de fichier.
     *
     * @param int    $idStruct
     * @param string $type
     * @param string $filename
     * @return string
     */
    private function getPathForType(int $idStruct, string $type, string $filename): string
    {
        // Liste des types autorisés
        $allowedTypes = ['blanket', 'profil', 'pdf', 'audio', 'banner', 'pub', 'logo', 'backup', 'category'];
        
        if (!in_array($type, $allowedTypes)) {
            Log::warning('Type de fichier non autorisé', ['type' => $type]);
            abort(404);
        }

        return match ($type) {
            'blanket'  => 'structures/' . $idStruct . '/blankets/' . $filename,
            'profil'   => 'profils/' . $filename,
            'pdf'      => 'structures/' . $idStruct . '/pdfs/' . $filename,
            'audio'    => 'structures/' . $idStruct . '/audios/' . $filename,
            'banner'   => 'structures/' . $idStruct . '/banners/' . $filename,
            'pub'      => 'pubs/' . $filename,
            'logo'     => 'structures/' . $idStruct . '/logos/' . $filename,
            'backup'   => 'backups/' . $idStruct . '/backups/' . $filename,
            'category' => 'categories/' . $filename,
            default    => abort(404),
        };
    }
}
