package com.naniger.elim.model.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import java.io.File;


import com.naniger.elim.model.data.DownloadFile;
import com.naniger.elim.model.data.Server;
import com.naniger.elim.model.table.ElectronicTable;

public class PdfDownloadService extends Service {
    public static final String CHANNEL_ID = DownloadNotifier.CHANNEL_ID;
    /** IDs distincts de AudioDownloadService : un PDF et un audio peuvent se télécharger en même temps. */
    private static final int NOTIFICATION_ID = 1;
    private static final int RESULT_NOTIFICATION_ID = 1001;

    private DownloadNotifier mNotifier;

    @Override
    public void onCreate() {
        super.onCreate();
        mNotifier = new DownloadNotifier(this, NOTIFICATION_ID, RESULT_NOTIFICATION_ID,
                "ACTION_PDF_DOWNLOAD_PROGRESS");
        startForeground(NOTIFICATION_ID, mNotifier.initialNotification());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Intent null : relance du service par le système, sans rien à reprendre.
        String[] fileNames = intent != null ? intent.getStringArrayExtra("fileNames") : null;
        if (fileNames == null) {
            stopForeground(true);
            stopSelf();
            return START_NOT_STICKY;
        }
        new Thread(() -> startDownload(fileNames)).start();
        return START_NOT_STICKY;
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
            mNotifier.setTitle(names[9]);

            // Une seule progression pour tout le livre : petits fichiers d'abord (0-8 %),
            // puis le PDF, qui représente l'essentiel du volume (8-100 %).
            DownloadFile.Result coverResult = downloadFile.start(
                    host + "/api/public/resource/" + idStruct + "/blanket/" + names[0],
                    names[0], mNotifier.phase(0, 4));

            // Icône de catégorie et photo d'auteur : décoratives et partagées entre livres.
            // Un échec (404, réseau) ne doit pas faire échouer le téléchargement du livre.
            downloadOptionalAsset(downloadFile, host, idStruct, "category", names[2], 4, 6);
            downloadOptionalAsset(downloadFile, host, idStruct, "profil", names[3], 6, 8);

            DownloadFile.Result pdfResult = downloadFile.start(
                    host + "/api/public/resource/" + idStruct + "/pdf/" + names[1],
                    names[1], mNotifier.phase(8, 100));

            electronicTable.markCompleted(idNumber, idBook, pdfResult.size, pdfResult.checksum);

            mNotifier.finish(true);

            Intent finishIntent = new Intent("ACTION_FINISH_DOWNLOAD");
            finishIntent.putExtra("format", "pdf");
            finishIntent.putExtra("success", true);
            finishIntent.putExtra("idBook", idBook);
            finishIntent.setPackage(getPackageName());
            sendBroadcast(finishIntent);
        } catch (Exception e) {
            e.printStackTrace();
            // Échec : la ligne DOWNLOADING insérée plus haut est repassée à FAILED (et
            // CONSERVÉE, pas supprimée) pour que "Mes téléchargements" puisse afficher
            // l'échec et proposer de réessayer (cf. audit section 4/6/8).
            electronicTable.markFailed(idNumber, idBook);

            mNotifier.finish(false);
            Intent failIntent = new Intent("ACTION_FINISH_DOWNLOAD");
            failIntent.putExtra("format", "pdf");
            failIntent.putExtra("success", false);
            failIntent.putExtra("idBook", idBook);
            failIntent.setPackage(getPackageName());
            sendBroadcast(failIntent);
        } finally {
            // stopForeground est déjà appelé par mNotifier.finish() ; sans effet s'il est répété.
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
                                        String type, String fileName, int from, int to) {
        if (fileName == null || fileName.isEmpty() || "null".equals(fileName)) {
            mNotifier.advanceTo(to);
            return;
        }
        File local = new File(localPathFor(fileName));
        if (local.exists() && local.length() > 0) {
            mNotifier.advanceTo(to);
            return;
        }
        try {
            downloadFile.start(host + "/api/public/resource/" + idStruct + "/" + type + "/" + fileName,
                    fileName, mNotifier.phase(from, to));
        } catch (Exception e) {
            Log.w("PdfDownloadService", "Échec téléchargement accessoire (" + type + "/" + fileName
                    + ") ignoré - n'affecte pas le téléchargement du livre : " + e.getMessage());
        }
        mNotifier.advanceTo(to);
    }

    private String localPathFor(String fileName) {
        return getExternalFilesDir(null) + "/" + fileName;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
