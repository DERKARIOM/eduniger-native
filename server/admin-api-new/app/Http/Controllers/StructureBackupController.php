<?php

namespace App\Http\Controllers;

use Illuminate\Http\Request;
use Illuminate\Support\Facades\Storage;
use App\Models\Structure;
use Illuminate\Support\Facades\Auth;
use Illuminate\Support\Facades\Log;

class StructureBackupController extends Controller
{
    /**
     * Vérifie que l'utilisateur authentifié appartient bien à la structure.
     */
    private function authorizeStructureAccess(int $idStruct): void
    {
        $user = Auth::user();
        if ($user && $user->role === 2) {
            return;
        }
        if (!$user || $user->idStruct != $idStruct) {
            abort(403, 'Accès non autorisé à cette structure.');
        }
    }

    /**
     * Crée une nouvelle sauvegarde.
     */
    public function backup(int $idStruct)
    {
        //$this->authorizeStructureAccess($idStruct);

        $structure = Structure::findOrFail($idStruct);

        // 1. Récupérer la limite de stockage et l’espace utilisé
        $storageController = new StorageController();
        $usageResponse = $storageController->usage($idStruct);
        $usageData = json_decode($usageResponse->getContent(), true);

        if (isset($usageData['error'])) {
            return response()->json(['message' => 'Impossible de calculer l’espace utilisé.'], 500);
        }

        $currentUsed = $usageData['used'] ?? 0;
        $storageLimit = $usageData['storage_limit'] ?? (15 * 1024 * 1024 * 1024); // 15 Go par défaut

        // 2. Calculer la taille totale des dossiers à sauvegarder (hors backups)
        $foldersToBackup = ['blankets', 'pdfs', 'audios', 'banners', 'logos'];
        $totalBackupSize = 0;

        foreach ($foldersToBackup as $folder) {
            $folderSize = $this->getFolderSize($idStruct, $folder);
            $totalBackupSize += $folderSize;
        }

        // 3. Vérifier la limite de stockage de la structure
        if ($currentUsed + $totalBackupSize > $storageLimit) {
            return response()->json([
                'message' => 'Impossible de créer la sauvegarde : la limite de stockage de la structure serait dépassée.',
                'details' => [
                    'utilisé' => $this->formatBytes($currentUsed),
                    'limite' => $this->formatBytes($storageLimit),
                    'taille_sauvegarde' => $this->formatBytes($totalBackupSize)
                ]
            ], 400);
        }

        // 4. Vérifier l’espace disque réel
        $storagePath = storage_path('app/private');
        $freeSpace = disk_free_space($storagePath);

        if ($freeSpace === false) {
            Log::warning('Impossible de déterminer l’espace disque libre.');
            // on continue, la vérification ne sera pas fiable
        } elseif ($totalBackupSize > $freeSpace) {
            return response()->json([
                'message' => 'Espace disque insuffisant pour créer la sauvegarde.',
                'details' => [
                    'taille_sauvegarde' => $this->formatBytes($totalBackupSize),
                    'espace_libre' => $this->formatBytes($freeSpace)
                ]
            ], 400);
        }

        // 5. Créer l’archive ZIP
        $timestamp = time();
        $backupPath = 'structures/'.$idStruct.'/backups/backup_' . $timestamp . '.zip';
        $fullPath = storage_path('app/private/' . $backupPath);

        // Créer le dossier de backups s'il n'existe pas
        $backupDir = dirname($fullPath);
        if (!is_dir($backupDir)) {
            if (!mkdir($backupDir, 0755, true)) {
                return response()->json(['message' => 'Impossible de créer le dossier de sauvegarde.'], 500);
            }
        }

        $zip = new \ZipArchive();
        if ($zip->open($fullPath, \ZipArchive::CREATE) !== true) {
            return response()->json(['message' => 'Impossible de créer l\'archive.'], 500);
        }

        $this->addFolderToZip($zip, storage_path('app/private/structures/'.$idStruct.'/blankets'), 'blankets');
        $this->addFolderToZip($zip, storage_path('app/private/structures/'.$idStruct.'/pdfs'), 'pdfs');
        $this->addFolderToZip($zip, storage_path('app/private/structures/'.$idStruct.'/audios'), 'audios');
        $this->addFolderToZip($zip, storage_path('app/private/structures/'.$idStruct.'/banners'), 'banners');
        $this->addFolderToZip($zip, storage_path('app/private/structures/'.$idStruct.'/logos'), 'logos');

        $zip->close();

        return response()->json([
            'message' => 'Sauvegarde créée avec succès.',
            //'backup_url' => route('backup.download', ['idStruct' => $idStruct, 'filename' => 'backup_' . $timestamp . '.zip'])
        ]);
    }

    /**
     * Liste toutes les sauvegardes d'une structure.
     */
    public function index(int $idStruct)
    {
        //$this->authorizeStructureAccess($idStruct);

        $path = 'structures/'.$idStruct.'/backups';

        if (!Storage::disk('private')->exists($path)) {
            return response()->json([]);
        }

        $files = Storage::disk('private')->files($path);
        $backups = [];

        foreach ($files as $file) {
            if (pathinfo($file, PATHINFO_EXTENSION) === 'zip') {
                $backups[] = [
                    'name' => basename($file),
                    'size' => $this->formatBytes(Storage::disk('private')->size($file)),
                    'last_modified' => Storage::disk('private')->lastModified($file),
                    'created_at' => date('Y-m-d H:i:s', Storage::disk('private')->lastModified($file)),
                ];
            }
        }

        usort($backups, fn($a, $b) => $b['last_modified'] - $a['last_modified']);

        return response()->json($backups);
    }

    /**
     * Supprime une sauvegarde.
     */
    public function destroy(int $idStruct, string $filename)
    {
        //$this->authorizeStructureAccess($idStruct);

        $filename = basename($filename);
        $filePath = 'structures/'.$idStruct.'/backups/'.$filename;

        if (!Storage::disk('private')->exists($filePath)) {
            return response()->json(['message' => 'Fichier introuvable.'], 404);
        }

        Storage::disk('private')->delete($filePath);

        return response()->json(['message' => 'Sauvegarde supprimée avec succès.']);
    }

    /**
     * Télécharge une sauvegarde.
     */
    public function download(int $idStruct, string $filename)
    {
        //$this->authorizeStructureAccess($idStruct);

        $filename = basename($filename);
        $filePath = 'structures/'.$idStruct.'/backups/'.$filename;

        if (!Storage::disk('private')->exists($filePath)) {
            return response()->json(['message' => 'Fichier introuvable.'], 404);
        }

        return response()->download(storage_path('app/private/' . $filePath), $filename);
    }

    /**
     * Calcule la taille totale d’un dossier (en octets) pour une structure donnée.
     */
    private function getFolderSize(int $idStruct, string $folder): int
    {
        $path = 'structures/'.$idStruct.'/'.$folder;

        if (!Storage::disk('private')->exists($path)) {
            return 0;
        }

        $files = Storage::disk('private')->allFiles($path);
        $size = 0;
        foreach ($files as $file) {
            $size += Storage::disk('private')->size($file);
        }
        return $size;
    }

    /**
     * Ajoute récursivement un dossier à l'archive ZIP.
     */
    private function addFolderToZip($zip, $folderPath, $folderName)
    {
        if (!is_dir($folderPath)) {
            return;
        }

        $files = new \RecursiveIteratorIterator(
            new \RecursiveDirectoryIterator($folderPath, \RecursiveDirectoryIterator::SKIP_DOTS),
            \RecursiveIteratorIterator::LEAVES_ONLY
        );

        foreach ($files as $file) {
            $filePath = $file->getRealPath();
            $relativePath = $folderName . '/' . $file->getFilename();
            $zip->addFile($filePath, $relativePath);
        }
    }

    /**
     * Convertit des octets en format lisible.
     */
    private function formatBytes($bytes, $precision = 2)
    {
        $units = ['o', 'Ko', 'Mo', 'Go', 'To'];
        $bytes = max($bytes, 0);
        $pow = floor(($bytes ? log($bytes) : 0) / log(1024));
        $pow = min($pow, count($units) - 1);
        $bytes /= pow(1024, $pow);
        return round($bytes, $precision) . ' ' . $units[$pow];
    }
}