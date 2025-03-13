package com.ninotech.fabi.model.service;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.ninotech.fabi.R;
import com.ninotech.fabi.controleur.activity.AudioPlayerActivity;

import java.io.IOException;

public class AudioService extends Service {
    private static final String CHANNEL_ID = "AUDIO_PLAYER_CHANNEL";
    private MediaPlayer mediaPlayer;
    private final IBinder binder = new AudioBinder();

    public class AudioBinder extends Binder {
        public AudioService getService() {
            return AudioService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        mediaPlayer = new MediaPlayer();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public void playAudio(String audioPath) {
        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(audioPath);
            mediaPlayer.prepare();
            mediaPlayer.start();
            showNotification(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void pauseAudio() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            showNotification(false);
        }
    }

    public void resumeAudio() {
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.start();
            showNotification(true);
        }
    }

    public void stopAudio() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }
        stopForeground(true);
    }

    private void showNotification(boolean isPlaying) {
        Intent intent = new Intent(this, AudioPlayerActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.img_default_book);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Lecture audio")
                .setContentText(isPlaying ? "Lecture en cours" : "Lecture en pause")
                .setSmallIcon(R.drawable.vector_black3_play)
                .setLargeIcon(icon)
                .setContentIntent(pendingIntent)
                .addAction(isPlaying ? R.drawable.vector_black3_pause : R.drawable.vector_black3_play,
                        isPlaying ? "Pause" : "Lecture",
                        getPlaybackAction(isPlaying ? "PAUSE" : "PLAY"))
                .addAction(R.drawable.vector_black3_play, "Arrêter", getPlaybackAction("STOP"))
                .setOngoing(isPlaying)
                .setPriority(NotificationCompat.PRIORITY_HIGH);
        startForeground(1, notificationBuilder.build());
    }

    private PendingIntent getPlaybackAction(String action) {
        Intent intent = new Intent(this, AudioReceiver.class);
        intent.setAction(action);
        return PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Audio Player", NotificationManager.IMPORTANCE_LOW);
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mediaPlayer.release();
    }
}