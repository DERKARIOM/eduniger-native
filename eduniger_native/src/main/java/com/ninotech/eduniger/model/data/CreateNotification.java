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
import android.os.Build;
import android.support.v4.media.session.MediaSessionCompat;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.ninotech.eduniger.R;
import com.ninotech.eduniger.controleur.activity.AudioPlayerActivity;
import com.ninotech.eduniger.model.service.NotificationActionService;

import java.io.File;

public class CreateNotification {
    public static final String CHANNEL_ID    = "channel1";
    public static final String ACTION_PREVIOUS = "actionprevious";
    public static final String ACTION_PLAY     = "actionplay";
    public static final String ACTION_NEXT     = "actionnext";
    public static final String ACTION_LOVE     = "actionlove";
    public static final String ACTION_CLOSE    = "actionclose";

    public static Notification notification;

    // ── Le token vient maintenant de l'extérieur (AudioPlayerActivity) ──────
    public static void createNotification(Context context, Track track, int playbutton,
                                          int pos, int size,
                                          MediaSessionCompat.Token sessionToken) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(context);

            NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                        != PackageManager.PERMISSION_GRANTED) return;
            }

            Bitmap coverBitmap = loadCoverBitmap(context, track);

            // Tap → AudioPlayerActivity
            Intent openIntent = new Intent(context, AudioPlayerActivity.class);
            openIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            PendingIntent contentPendingIntent = PendingIntent.getActivity(
                    context, 0, openIntent,
                    PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

            // ❤️
            PendingIntent pendingIntentLove = PendingIntent.getBroadcast(context, 0,
                    new Intent(context, NotificationActionService.class).setAction(ACTION_LOVE),
                    PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
            // ⏮
            PendingIntent pendingIntentPrevious = PendingIntent.getBroadcast(context, 1,
                    new Intent(context, NotificationActionService.class).setAction(ACTION_PREVIOUS),
                    PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
            // ▶/⏸
            PendingIntent pendingIntentPlay = PendingIntent.getBroadcast(context, 2,
                    new Intent(context, NotificationActionService.class).setAction(ACTION_PLAY),
                    PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
            // ⏭
            PendingIntent pendingIntentNext = PendingIntent.getBroadcast(context, 3,
                    new Intent(context, NotificationActionService.class).setAction(ACTION_NEXT),
                    PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
            // ✕
            PendingIntent pendingIntentClose = PendingIntent.getBroadcast(context, 4,
                    new Intent(context, NotificationActionService.class).setAction(ACTION_CLOSE),
                    PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

            notification = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.mipmap.ic_v2)
                    .setContentTitle(track.getTitle())
                    .setContentText(track.getArtist())
                    .setLargeIcon(coverBitmap)
                    .setContentIntent(contentPendingIntent)
                    .setAutoCancel(false)
                    .setOnlyAlertOnce(true)
                    .setShowWhen(false)
                    .addAction(R.drawable.vector_black2_favorite,             "Favori",    pendingIntentLove)
                    .addAction(R.drawable.vector_black2_audio_player_back,    "Précédent", pendingIntentPrevious)
                    .addAction(playbutton,                                     "Lecture",   pendingIntentPlay)
                    .addAction(R.drawable.vector_black2_audio_player_next,    "Suivant",   pendingIntentNext)
                    .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Fermer",    pendingIntentClose)
                    .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                            .setShowActionsInCompactView(0, 1, 2, 3, 4)
                            .setMediaSession(sessionToken))          // ← token persistant
                    .setPriority(NotificationCompat.PRIORITY_LOW)
                    .build();

            notificationManagerCompat.notify(1, notification);
        }
    }

    private static Bitmap loadCoverBitmap(Context context, Track track) {
        String coverPath = track.getCover();
        if (coverPath != null && !coverPath.isEmpty()) {
            File file = new File(coverPath);
            if (file.exists()) {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(coverPath, options);
                options.inSampleSize = calculateInSampleSize(options, 256, 256);
                options.inJustDecodeBounds = false;
                Bitmap bmp = BitmapFactory.decodeFile(coverPath, options);
                if (bmp != null) return bmp;
            }
        }
        return BitmapFactory.decodeResource(context.getResources(), R.drawable.img_wait_cover_book);
    }

    private static int calculateInSampleSize(BitmapFactory.Options options, int reqW, int reqH) {
        int h = options.outHeight, w = options.outWidth, s = 1;
        if (h > reqH || w > reqW) {
            int hh = h / 2, hw = w / 2;
            while ((hh / s) >= reqH && (hw / s) >= reqW) s *= 2;
        }
        return s;
    }

    public static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager nm =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, "Audio Player", NotificationManager.IMPORTANCE_LOW);
            channel.setDescription("Canal pour la lecture audio");
            nm.createNotificationChannel(channel);
        }
    }
}