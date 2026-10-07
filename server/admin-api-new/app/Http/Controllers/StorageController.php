<?php

namespace App\Http\Controllers;

use Illuminate\Support\Facades\Storage;
use App\Models\Structure;

class StorageController extends Controller
{
    public function usage(int $idStruct)
    {
        try {
            
        // Chemin de base pour la structure
            $basePath = "structures/".$idStruct;
            
            $disk = Storage::disk('private'); // ou 'private' selon config
            
            $folders = ['blankets', 'pdfs', 'audios','banners','backups','logos'];
            $breakdown = [];
            $totalUsed = 0;
            $storageLimit = Structure::find($idStruct)->storageLimit ?? (15 * 1024 * 1024 * 1024); // 15 Go par défaut

            foreach ($folders as $folder) {
                $size = $this->folderSize($disk, $basePath.'/'.$folder);
                $breakdown[$folder] = $size;
                $totalUsed += $size;
            }

            // Capacité totale (par exemple 10 Go fixe ou paramétrable)
            $total = 10 * 1024 * 1024 * 1024; 

            return response()->json([
                'total' => $total,
                'used' => $totalUsed,
                'breakdown' => $breakdown,
                'storage_limit' => $storageLimit,
                'over_limit' => $totalUsed > $storageLimit,
            ]);
        } catch (\Exception $e) {
            return response()->json(['error' => 'Unable to calculate storage usage', 'message' => $e->getMessage()], 500);
        }
    }

    private function folderSize($disk, $path)
    {
        if (!$disk->exists($path)) return 0;
        $files = $disk->allFiles($path);
        $size = 0;
        foreach ($files as $file) {
            $size += $disk->size($file);
        }
        return $size;
    }
}