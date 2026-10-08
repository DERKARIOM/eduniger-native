package com.naniger.elim.model.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import java.io.File;

import androidx.core.app.NotificationCompat;

import com.naniger.elim.model.data.DownloadFile;
import com.naniger.elim.model.data.Server;
import com.naniger.elim.model.table.ElectronicTable;

public class PdfDownloadService extends Service {
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
        String[] fileNames = intent.getStringArrayExtra("fileNames");
        new Thread(() -> startDownload(fileNames)).start();
        return START_STICKY;
    }

    /**
     * names[0]=cover, [1]=pdf (electronic), [2]=couverture de catégorie, [3]=photo auteur,
     * [4]=idNumber (utilisateur), [5]=idBook, [6]=description, [7]=nom auteur,
     * [8]=nom catégorie, [9]=titre, [10]=idStruct (structure réelle du livre).
     */
    private void startDownload(String[] names) {
        String idNumber = names[4];
        String idBook = names[5];
        String idStruct = names.length > 10 ? names[10] : null;

        ElectronicTable electronicTable = new ElectronicTable(getApplicationContext());

        // Chemins locaux déterministes (identiques à ceux que DownloadFile.start() écrira) :
        // on les connaît AVANT le téléchargement, ce qui permet d'enregistrer une ligne
        // DOWNLOADING en base avant même le premier appel réseau (cf. audit section 6 :
        // auparavant aucune trace locale n'existait tant que le téléchargement n'était pas
        // intégralement terminé, donc impossible de détecter/reprendre un téléchargement
        // interrompu par la fermeture de l'app).
        String localCoverPath = localPathFor(names[0]);
        String localPdfPath = localPathFor(names[1]);
        String localCoverCategoryPath = localPathFor(names[2]);
        String localProfileAuthorPath = localPathFor(names[3]);

        electronicTable.insertPending(idNumber, idBook, names[6], names[7],
                localCoverPath, localPdfPath, names[8], names[9],
                localCoverCategoryPath, localProfileAuthorPath, idStruct);

        try {
            if (idStruct == null || idStruct.isEmpty()) {
                // Sans idStruct, /api/public/resource/{idStruct}/... ne peut pas vérifier
                // que ce fichier appartient bien à une structure de l'utilisateur (cf. audit
                // sécurité) : on refuse plutôt que d'utiliser un idStruct arbitraire.
                throw new Exception("idStruct manquant : impossible de construire une URL de téléchargement sécurisée.");
            }

            String host = Server.getUrlHostProd(this);
            DownloadFile downloadFile = new DownloadFile(this);

            DownloadFile.Result coverResult = downloadFile.start(
                    host + "/api/public/resource/" + idStruct + "/blanket/" + names[0],
                    names[0], this::updateProgress);

            DownloadFile.Result pdfResult = downloadFile.start(
                    host + "/api/public/resource/" + idStruct + "/pdf/" + names[1],
                    names[1], this::updateProgress);

            // Migration : couverture de catégorie et photo d'auteur étaient encore servies
            // par l'ancien backend PHP legacy (Server.getUrlServer()) alors que le reste du
            // téléchargement utilise déjà le backend Sanctum migré - incohérence relevée
            // dans l'audit (section "nettoyage"/"consistance"). FileController::publicShow
            // gère déjà les types "category" et "profil", donc on bascule ces deux fichiers
            // sur la même route publique sécurisée que la couverture et le PDF.
            //
            // Ces deux fichiers sont "décoratifs" et partagés entre plusieurs livres (icône
            // de catégorie, photo d'auteur) : un échec ici (fichier absent côté serveur,
            // réseau...) ne doit PAS faire échouer le téléchargement du livre lui-même - le
            // PDF et sa couverture, seuls éléments réellement nécessaires pour "Ouvrir" un
            // livre téléchargé, sont déjà sécurisés à ce stade (coverResult/pdfResult
            // ci-dessus). Cf. bug constaté en test réel : l'icône de catégorie d'un livre
            // renvoyait 404, ce qui faisait passer TOUT le téléchargement en FAILED alors
            // que le PDF était intégralement et correctement téléchargé.
            downloadOptionalAsset(downloadFile, host, idStruct, "category", names[2]);
            downloadOptionalAsset(downloadFile, host, idStruct, "profil", names[3]);

            electronicTable.markCompleted(idNumber, idBook, pdfResult.size, pdfResult.checksum);

            notificationBuilder.setContentText("Téléchargement terminé").setProgress(0, 0, false).setSmallIcon(android.R.drawable.stat_sys_download_done);
            notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());

            Intent finishIntent = new Intent("ACTION_FINISH_DOWNLOAD");
            finishIntent.putExtra("format", "pdf");
            finishIntent.putExtra("success", true);
            finishIntent.putExtra("idBook", idBook);
            sendBroadcast(finishIntent);
        } catch (Exception e) {
            e.printStackTrace();
            // Échec : la ligne DOWNLOADING insérée plus haut est repassée à FAILED (et
            // CONSERVÉE, pas supprimée) pour que "Mes téléchargements" puisse afficher
            // l'échec et proposer de réessayer (cf. audit section 4/6/8).
            electronicTable.markFailed(idNumber, idBook);

            notificationBuilder.setContentText("Echec du telechargement")
                    .setProgress(0, 0, false)
                    .setSmallIcon(android.R.drawable.stat_sys_warning);
            notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
            Intent failIntent = new Intent("ACTION_FINISH_DOWNLOAD");
            failIntent.putExtra("format", "pdf");
            failIntent.putExtra("success", false);
            failIntent.putExtra("idBook", idBook);
            sendBroadcast(failIntent);
        } finally {
            stopForeground(true);
            stopSelf();
        }
    }

    /**
     * Téléchargement "best effort" d'un fichier accessoire (icône de catégorie, photo
     * d'auteur) : partagé entre plusieurs livres, jamais requis pour ouvrir le PDF.
     * On ignore le fichier s'il est déjà présent localement (évite de re-télécharger un
     * asset partagé à chaque livre, d'autant que le serveur ne supporte pas la reprise
     * - cf. DownloadFile), et on avale silencieusement (log seulement) tout échec réseau
     * ou HTTP (404 notamment) sans faire échouer le téléchargement du livre.
     */
    private void downloadOptionalAsset(DownloadFile downloadFile, String host, String idStruct,
                                        String type, String fileName) {
        if (fileName == null || fileName.isEmpty()) return;
        File local = new File(localPathFor(fileName));
        if (local.exists() && local.length() > 0) return;
        try {
            downloadFile.start(host + "/api/public/resource/" + idStruct + "/" + type + "/" + fileName,
                    fileName, this::updateProgress);
        } catch (Exception e) {
            Log.w("PdfDownloadService", "Échec téléchargement accessoire (" + type + "/" + fileName
                    + ") ignoré - n'affecte pas le téléchargement du livre : " + e.getMessage());
        }
    }

    private String localPathFor(String fileName) {
        return getExternalFilesDir(null) + "/" + fileName;
    }

    private void updateProgress(int progress, long bytesDownloaded, long totalBytes) {
        if (progress >= 0) {
            notificationBuilder.setProgress(100, progress, false).setContentText("Progression : " + progress + "%");
        } else {
            notificationBuilder.setProgress(0, 0, true).setContentText("Téléchargement...");
        }
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
        Intent progressIntent = new Intent("ACTION_PDF_DOWNLOAD_PROGRESS");
        progressIntent.putExtra("progress", progress);
        progressIntent.putExtra("bytesDownloaded", bytesDownloaded);
        progressIntent.putExtra("totalBytes", totalBytes);
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
