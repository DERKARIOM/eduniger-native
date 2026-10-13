package com.naniger.elim.model.service;
import android.app.*;
import android.content.Context;
import android.content.Intent;
import android.os.*;
import android.util.Log;

import java.io.File;

import androidx.core.app.NotificationCompat;

import com.naniger.elim.model.data.AudioBook;
import com.naniger.elim.model.data.DownloadFile;
import com.naniger.elim.model.data.Server;
import com.naniger.elim.model.table.AudioTable;

public class AudioDownloadService extends Service {
    public static final String CHANNEL_ID = "DownloadChannel";
    private NotificationManager notificationManager;
    private NotificationCompat.Builder notificationBuilder;
    private static final int NOTIFICATION_ID = 1;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.stat_sys_download)
                .setContentTitle("Téléchargement en cours")
                .setContentText("Téléchargement en cours...")
                .setProgress(100, 0, false)
                .setPriority(NotificationCompat.PRIORITY_LOW);

        startForeground(NOTIFICATION_ID, notificationBuilder.build());
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

            // Couverture du livre et fichier audio : indispensables a la lecture hors ligne.
            audioBook.setCover(downloadFile.start(host + "/api/public/resource/" + idStruct + "/blanket/" + names[0], names[0], this::updateProgress).path);
            audioBook.setAudio(downloadFile.start(host + "/api/public/resource/" + idStruct + "/audio/" + names[4], names[4], this::updateProgress).path);

            // Icone de categorie et photo d'auteur : purement decoratives. Elles etaient
            // encore demandees a l'ancien serveur PHP (/fabi/ressources/...), qui n'existe
            // plus sur Hostinger -> HTTP 404 -> TOUT le livre audio passait en echec alors
            // que le MP3 aurait pu etre telecharge. Meme correctif que PdfDownloadService :
            // route publique Laravel + echec non bloquant.
            audioBook.setCoverCategory(downloadOptionalAsset(downloadFile, host, idStruct, "category", names[2]));
            audioBook.setProfileAuthor(downloadOptionalAsset(downloadFile, host, idStruct, "profil", names[3]));

            AudioTable audioTable = new AudioTable(getApplicationContext());
            audioTable.insert(names[5], names[6], names[7], names[8],audioBook.getCover(),audioBook.getAudio(), names[9], names[10],audioBook.getCoverCategory(),audioBook.getProfileAuthor(),names[11]);
            notificationBuilder.setContentText("Téléchargement terminé").setProgress(0, 0, false).setSmallIcon(android.R.drawable.stat_sys_download_done);
            notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());

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
            notificationBuilder.setContentText("Echec du telechargement")
                    .setProgress(0, 0, false)
                    .setSmallIcon(android.R.drawable.stat_sys_warning);
            notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
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
                                         String type, String fileName) {
        if (fileName == null || fileName.isEmpty() || "null".equals(fileName)) return "";
        String localPath = getExternalFilesDir(null) + "/" + fileName;
        File local = new File(localPath);
        if (local.exists() && local.length() > 0) return localPath;
        try {
            return downloadFile.start(host + "/api/public/resource/" + idStruct + "/" + type + "/" + fileName,
                    fileName, this::updateProgress).path;
        } catch (Exception e) {
            Log.w("AudioDownloadService", "Fichier accessoire indisponible (" + type + "/" + fileName
                    + ") ignore, le livre audio reste telecharge : " + e.getMessage());
            return localPath;
        }
    }

    private void updateProgress(int progress, long bytesDownloaded, long totalBytes) {
        if (progress >= 0) {
            notificationBuilder.setProgress(100, progress, false).setContentText("Progression : " + progress + "%");
        } else {
            notificationBuilder.setProgress(0, 0, true).setContentText("Téléchargement...");
        }
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());

        Intent progressIntent = new Intent("ACTION_AUDIO_DOWNLOAD_PROGRESS");
        progressIntent.putExtra("progress", progress);
        progressIntent.putExtra("bytesDownloaded", bytesDownloaded);
        progressIntent.putExtra("totalBytes", totalBytes);
        progressIntent.setPackage(getPackageName());
        sendBroadcast(progressIntent);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Téléchargements", NotificationManager.IMPORTANCE_LOW);
            channel.setDescription("Canal pour les notifications de téléchargement");
            getSystemService(NotificationManager.class).createNotificationChannel(channel);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}