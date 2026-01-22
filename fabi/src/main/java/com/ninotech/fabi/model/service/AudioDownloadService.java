package com.ninotech.fabi.model.service;
import android.app.*;
import android.content.Context;
import android.content.Intent;
import android.os.*;

import androidx.core.app.NotificationCompat;

import com.ninotech.fabi.model.data.AudioBook;
import com.ninotech.fabi.model.data.DownloadFile;
import com.ninotech.fabi.model.data.Server;
import com.ninotech.fabi.model.table.AudioTable;

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
        String[] fileNames = intent.getStringArrayExtra("fileNames");
        new Thread(() -> startDownload(fileNames)).start();
        return START_STICKY;
    }

    private void startDownload(String[] names) {
        try {
            AudioBook audioBook = new AudioBook();
            DownloadFile downloadFile = new DownloadFile(this);
            audioBook.setCover(downloadFile.start(Server.getUrlServer(this) + "ressources/cover/" + names[0], names[0], this::updateProgress));
            audioBook.setCoverCategory(downloadFile.start(Server.getUrlServer(this) + "ressources/cover/" + names[2], names[2], this::updateProgress));
            audioBook.setProfileAuthor(downloadFile.start(Server.getUrlServer(this) + "ressources/profile/" + names[3], names[3], this::updateProgress));
            audioBook.setAudio(downloadFile.start(Server.getUrlServer(this) + "ressources/audio/" + names[4], names[4], this::updateProgress));

            AudioTable audioTable = new AudioTable(getApplicationContext());
            audioTable.insert(names[5], names[6], names[7], names[8],audioBook.getCover(),audioBook.getAudio(), names[9], names[10],audioBook.getCoverCategory(),audioBook.getProfileAuthor(),names[11]);
            notificationBuilder.setContentText("Téléchargement terminé").setProgress(0, 0, false).setSmallIcon(android.R.drawable.stat_sys_download_done);
            notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());

            // Envoyer une notification de fin de téléchargement
            Intent finishIntent = new Intent("ACTION_FINISH_DOWNLOAD");
            finishIntent.putExtra("format", "audio");
            sendBroadcast(finishIntent);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            stopForeground(true);
            stopSelf();
        }
    }

    private void updateProgress(int progress) {
        notificationBuilder.setProgress(100, progress, false).setContentText("Progression : " + progress + "%");
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
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