package com.naniger.elim.model.service;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.*;
import android.util.Log;

import java.io.File;


import com.naniger.elim.model.data.AudioBook;
import com.naniger.elim.model.data.DownloadFile;
import com.naniger.elim.model.data.Server;
import com.naniger.elim.model.table.AudioTable;

public class AudioDownloadService extends Service {
    public static final String CHANNEL_ID = DownloadNotifier.CHANNEL_ID;
    /** IDs distincts de PdfDownloadService : un PDF et un audio peuvent se télécharger en même temps. */
    private static final int NOTIFICATION_ID = 2;
    private static final int RESULT_NOTIFICATION_ID = 1002;

    private DownloadNotifier mNotifier;

    @Override
    public void onCreate() {
        super.onCreate();
        mNotifier = new DownloadNotifier(this, NOTIFICATION_ID, RESULT_NOTIFICATION_ID,
                "ACTION_AUDIO_DOWNLOAD_PROGRESS");
        startForeground(NOTIFICATION_ID, mNotifier.initialNotification());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Intent null : le systeme relance le service apres l'avoir tue (START_STICKY).
        // Sans les noms de fichiers, rien a reprendre : on s'arrete proprement au lieu de
        // planter (NullPointerException).
        String[] fileNames = intent != null ? intent.getStringArrayExtra("fileNames") : null;
        if (fileNames == null) {
            stopForeground(true);
            stopSelf();
            return START_NOT_STICKY;
        }
        new Thread(() -> startDownload(fileNames)).start();
        return START_NOT_STICKY;
    }

    private void startDownload(String[] names) {
        try {
            AudioBook audioBook = new AudioBook();
            DownloadFile downloadFile = new DownloadFile(this);
            // Bug releve dans l'audit telechargement (meme cause que PdfDownloadService) :
            // "1" etait un idStruct code en dur, cassant le telechargement pour toute
            // structure != 1 puisque FileController::publicShow verifie que le fichier
            // appartient reellement a CETTE structure. names[13] transporte desormais le
            // vrai idStruct du livre (cf. BookActivity.startAudioDownloadService()).
            String idStruct = names.length > 13 ? names[13] : null;
            if (idStruct == null || idStruct.isEmpty()) {
                throw new Exception("idStruct manquant : impossible de construire une URL de telechargement securisee.");
            }
            String host = Server.getUrlHostProd(this);
            mNotifier.setTitle(names[10]);

            // Une seule progression pour tout le livre : petits fichiers d'abord (0-8 %),
            // puis le fichier audio, qui représente l'essentiel du volume (8-100 %).
            audioBook.setCover(downloadFile.start(host + "/api/public/resource/" + idStruct + "/blanket/" + names[0], names[0], mNotifier.phase(0, 4)).path);

            // Icône de catégorie et photo d'auteur : décoratives, échec non bloquant.
            audioBook.setCoverCategory(downloadOptionalAsset(downloadFile, host, idStruct, "category", names[2], 4, 6));
            audioBook.setProfileAuthor(downloadOptionalAsset(downloadFile, host, idStruct, "profil", names[3], 6, 8));

            audioBook.setAudio(downloadFile.start(host + "/api/public/resource/" + idStruct + "/audio/" + names[4], names[4], mNotifier.phase(8, 100)).path);

            AudioTable audioTable = new AudioTable(getApplicationContext());
            audioTable.insert(names[5], names[6], names[7], names[8],audioBook.getCover(),audioBook.getAudio(), names[9], names[10],audioBook.getCoverCategory(),audioBook.getProfileAuthor(),names[11]);
            mNotifier.finish(true);

            // Envoyer une notification de fin de téléchargement
            Intent finishIntent = new Intent("ACTION_FINISH_DOWNLOAD");
            finishIntent.putExtra("format", "audio");
            finishIntent.putExtra("success", true);
            finishIntent.setPackage(getPackageName());
            sendBroadcast(finishIntent);
        } catch (Exception e) {
            e.printStackTrace();
            // Echec du telechargement : informer l'utilisateur au lieu de laisser la
            // notification et l'interface bloquees sur "en cours" indefiniment.
            mNotifier.finish(false);
            Intent failIntent = new Intent("ACTION_FINISH_DOWNLOAD");
            failIntent.putExtra("format", "audio");
            failIntent.putExtra("success", false);
            failIntent.setPackage(getPackageName());
            sendBroadcast(failIntent);
        } finally {
            stopForeground(true);
            stopSelf();
        }
    }

    /**
     * Telecharge un fichier accessoire (icone de categorie, photo d'auteur). Ne leve
     * jamais d'exception et renvoie toujours le chemin local attendu (comme
     * PdfDownloadService) : si le fichier est absent, l'interface affiche l'image par defaut.
     */
    private String downloadOptionalAsset(DownloadFile downloadFile, String host, String idStruct,
                                         String type, String fileName, int from, int to) {
        if (fileName == null || fileName.isEmpty() || "null".equals(fileName)) {
            mNotifier.advanceTo(to);
            return "";
        }
        String localPath = getExternalFilesDir(null) + "/" + fileName;
        File local = new File(localPath);
        if (local.exists() && local.length() > 0) {
            mNotifier.advanceTo(to);
            return localPath;
        }
        try {
            return downloadFile.start(host + "/api/public/resource/" + idStruct + "/" + type + "/" + fileName,
                    fileName, mNotifier.phase(from, to)).path;
        } catch (Exception e) {
            Log.w("AudioDownloadService", "Fichier accessoire indisponible (" + type + "/" + fileName
                    + ") ignore, le livre audio reste telecharge : " + e.getMessage());
            return localPath;
        } finally {
            mNotifier.advanceTo(to);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}