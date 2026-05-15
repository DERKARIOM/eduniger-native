package com.ninotech.eduniger.model.data;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.session.MediaSession;
import android.os.Build;
import android.support.v4.media.session.MediaSessionCompat;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.ninotech.eduniger.R;
import com.ninotech.eduniger.model.service.NotificationActionService;

import java.io.File;

public class CreateNotification {
    public static final String CHANNEL_ID = "channel1";
    public static final String ACTION_PREVIOUS = "actionprevious";
    public static final String ACTION_PLAY = "actionplay";
    public static final String ACTION_NEXT = "actionnext";

    public static Notification notification;

    public static void createNotification(Context context, Track track, int playbutton, int pos, int size) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(context);

            NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
            MediaSession mediaSession = new MediaSession(context, "tag");

            // Vérifier la permission sur Android 13+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
            }

            // ── Charger la vraie pochette depuis le chemin fichier ──────────────
            Bitmap coverBitmap = loadCoverBitmap(context, track);

            // ── Bouton Précédent ────────────────────────────────────────────────
            PendingIntent pendingIntentPrevious = null;
            int drv_previous = 0;
            if (pos != 0) {
                Intent intentPrevious = new Intent(context, NotificationActionService.class)
                        .setAction(ACTION_PREVIOUS);
                pendingIntentPrevious = PendingIntent.getBroadcast(context, 0, intentPrevious,
                        PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
                drv_previous = R.drawable.vector_black2_audio_player_back;
            }

            // ── Bouton Play/Pause ───────────────────────────────────────────────
            Intent intentPlay = new Intent(context, NotificationActionService.class)
                    .setAction(ACTION_PLAY);
            PendingIntent pendingIntentPlay = PendingIntent.getBroadcast(context, 0, intentPlay,
                    PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

            // ── Bouton Suivant ──────────────────────────────────────────────────
            PendingIntent pendingIntentNext = null;
            int drv_next = 0;
            if (pos != size) {
                Intent intentNext = new Intent(context, NotificationActionService.class)
                        .setAction(ACTION_NEXT);
                pendingIntentNext = PendingIntent.getBroadcast(context, 0, intentNext,
                        PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
                drv_next = R.drawable.vector_black2_audio_player_next;
            }

            // ── Construire la notification ──────────────────────────────────────
            notification = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.mipmap.ic_v2)
                    .setContentTitle(track.getTitle())
                    .setContentText(track.getArtist())
                    .setLargeIcon(coverBitmap)          // ← vraie pochette
                    .setOnlyAlertOnce(true)
                    .setShowWhen(false)
                    .addAction(drv_previous, "Précédent", pendingIntentPrevious)
                    .addAction(playbutton, "Lecture", pendingIntentPlay)
                    .addAction(drv_next, "Suivant", pendingIntentNext)
                    .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                            .setShowActionsInCompactView(0, 1, 2)
                            .setMediaSession(MediaSessionCompat.Token.fromToken(
                                    mediaSession.getSessionToken())))
                    .setPriority(NotificationCompat.PRIORITY_LOW)
                    .build();

            notificationManagerCompat.notify(1, notification);
        }
    }

    // ── Charger la pochette : chemin fichier en priorité, fallback drawable ──
    private static Bitmap loadCoverBitmap(Context context, Track track) {
        // 1. Essayer le chemin fichier (cover stockée localement)
        String coverPath = track.getCover();
        if (coverPath != null && !coverPath.isEmpty()) {
            File file = new File(coverPath);
            if (file.exists()) {
                // Décoder en taille réduite pour la notification (256×256 suffit)
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(coverPath, options);
                options.inSampleSize = calculateInSampleSize(options, 256, 256);
                options.inJustDecodeBounds = false;
                Bitmap bmp = BitmapFactory.decodeFile(coverPath, options);
                if (bmp != null) return bmp;
            }
        }
        // 2. Fallback : drawable générique
        return BitmapFactory.decodeResource(context.getResources(), R.drawable.img_wait_cover_book);
    }

    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        int height = options.outHeight;
        int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            int halfHeight = height / 2;
            int halfWidth = width / 2;
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    public static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Audio Player",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Canal pour la lecture audio");
            notificationManager.createNotificationChannel(channel);
        }
    }
}