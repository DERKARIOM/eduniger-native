package com.naniger.elim.model.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.SystemClock;

import androidx.core.app.NotificationCompat;

import com.naniger.elim.model.data.DownloadFile;

/**
 * Progression et notification d'un téléchargement de livre (PDF ou audio), partagées par
 * PdfDownloadService et AudioDownloadService.
 *
 * Un livre = plusieurs fichiers (couverture, icône de catégorie, photo d'auteur, PDF ou
 * MP3). Avant, chaque fichier repartait de 0 % : la barre montait puis retombait trois ou
 * quatre fois. Ici chaque fichier reçoit une tranche de la progression globale
 * ({@link #phase(int, int)}), et la valeur affichée ne recule jamais.
 *
 * Les mises à jour sont aussi limitées (au plus ~4 par seconde, et seulement quand le
 * pourcentage change). DownloadFile signale la progression tous les 8 Ko : sans limite,
 * Android ignore une partie des notify() trop rapprochés, et la notification pouvait
 * rester figée sur « Téléchargement en cours » une fois le livre téléchargé.
 */
final class DownloadNotifier {

    static final String CHANNEL_ID = "DownloadChannel";
    private static final long MIN_INTERVAL_MS = 250;

    private final Service mService;
    private final NotificationManager mManager;
    private final NotificationCompat.Builder mBuilder;
    private final int mOngoingId;
    private final int mResultId;
    private final String mProgressAction;

    private String mTitle = "";
    private int mOverall = 0;
    private int mLastPublished = -1;
    private long mLastPublishAt = 0;

    DownloadNotifier(Service service, int ongoingId, int resultId, String progressAction) {
        mService = service;
        mOngoingId = ongoingId;
        mResultId = resultId;
        mProgressAction = progressAction;
        mManager = (NotificationManager) service.getSystemService(Context.NOTIFICATION_SERVICE);
        createChannel(service);
        mBuilder = new NotificationCompat.Builder(service, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.stat_sys_download)
                .setContentTitle("Téléchargement en cours")
                .setContentText("Préparation…")
                .setProgress(100, 0, true)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setPriority(NotificationCompat.PRIORITY_LOW);
    }

    /** Notification initiale du service au premier plan. */
    Notification initialNotification() {
        return mBuilder.build();
    }

    void setTitle(String title) {
        mTitle = title != null ? title : "";
        mBuilder.setContentTitle(mTitle.isEmpty() ? "Téléchargement en cours" : mTitle);
    }

    /**
     * Callback de progression d'un fichier, ramené dans la tranche [from, to] de la
     * progression globale. Un fichier de taille inconnue (-1) ne fait pas reculer la barre.
     */
    DownloadFile.ProgressCallback phase(int from, int to) {
        return (percent, bytesDownloaded, totalBytes) -> {
            if (percent < 0) return;
            int value = from + (to - from) * Math.min(percent, 100) / 100;
            publish(value, false);
        };
    }

    /** Positionne la progression globale (ex. fin d'une phase sautée car fichier déjà présent). */
    void advanceTo(int value) {
        publish(value, false);
    }

    private synchronized void publish(int value, boolean force) {
        // Jamais de recul, plafond à 100.
        mOverall = Math.min(Math.max(value, mOverall), 100);
        if (mOverall == mLastPublished) return;
        long now = SystemClock.elapsedRealtime();
        if (!force && mOverall < 100 && now - mLastPublishAt < MIN_INTERVAL_MS) return;
        mLastPublished = mOverall;
        mLastPublishAt = now;

        mBuilder.setProgress(100, mOverall, false).setContentText(mOverall + " %");
        mManager.notify(mOngoingId, mBuilder.build());

        Intent progress = new Intent(mProgressAction);
        progress.putExtra("progress", mOverall);
        progress.setPackage(mService.getPackageName());
        mService.sendBroadcast(progress);
    }

    /**
     * Fin du téléchargement : retire la notification « en cours » (liée au service au
     * premier plan) et affiche une notification de résultat distincte, non permanente.
     */
    void finish(boolean success) {
        if (success) publish(100, true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            mService.stopForeground(Service.STOP_FOREGROUND_REMOVE);
        } else {
            mService.stopForeground(true);
        }
        mManager.cancel(mOngoingId);

        NotificationCompat.Builder result = new NotificationCompat.Builder(mService, CHANNEL_ID)
                .setSmallIcon(success ? android.R.drawable.stat_sys_download_done
                        : android.R.drawable.stat_sys_warning)
                .setContentTitle(success ? "Téléchargement terminé" : "Échec du téléchargement")
                .setContentText(success
                        ? (mTitle.isEmpty() ? "Disponible hors connexion" : mTitle + " est disponible hors connexion")
                        : (mTitle.isEmpty() ? "Réessayez depuis la page du livre" : mTitle + " : réessayez depuis la page du livre"))
                .setAutoCancel(true)
                .setOngoing(false)
                .setPriority(NotificationCompat.PRIORITY_LOW);
        mManager.notify(mResultId, result.build());
    }

    private static void createChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Téléchargements",
                    NotificationManager.IMPORTANCE_LOW);
            channel.setDescription("Téléchargement des livres pour la lecture hors connexion");
            context.getSystemService(NotificationManager.class).createNotificationChannel(channel);
        }
    }
}
