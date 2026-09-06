package com.ninotech.eduniger.model.data;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.ninotech.eduniger.model.table.AudioTable;

public class AudioDownloader extends AsyncTask<String, Integer, AudioBook> {

    private Context mContext;
    private String mIdNumber;
    private OnlineBook mOnlineBook;
    private Tones mTones;
    private NotificationManager notificationManager;
    private NotificationCompat.Builder notificationBuilder;
    private static final int NOTIFICATION_ID = 1;

    public AudioDownloader(Context context, String idNumber, OnlineBook onlineBook , Tones tones) {
        mContext = context;
        mIdNumber = idNumber;
        mOnlineBook = onlineBook;
        mTones = tones;

        // Configurer le NotificationManager
        notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);

        // Créer un canal de notification pour les appareils Android 8.0 et plus
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = "download_channel";
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Téléchargements",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Canal pour les notifications de téléchargement");
            notificationManager.createNotificationChannel(channel);
        }

        // Initialiser la notification
        notificationBuilder = new NotificationCompat.Builder(mContext, "download_channel")
                .setSmallIcon(android.R.drawable.stat_sys_download)
                .setContentTitle("Téléchargement en cours")
                .setContentText("Téléchargement en cours...")
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setProgress(100, 0, false);
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        // Mettre à jour la barre de progression dans la notification
        int progress = values[0];
        notificationBuilder.setProgress(100, progress, false)
                .setContentText("Progression : " + progress + "%");
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
    }

    @Override
    protected AudioBook doInBackground(String... names) {
        AudioBook audioBook = new AudioBook();
        DownloadFile downloadFile = new DownloadFile(mContext);
        try {
            // Télécharger les fichiers avec progression
            audioBook.setCover(downloadFile.start(Server.getUrlHostProd(mContext) + "/api/public/resource/1/blanket/" + names[12],
                    names[0],
                    progress -> publishProgress(progress)
            ));

            audioBook.setCoverCategory(downloadFile.start(
                    Server.getUrlHost(mContext) + "/ressources/cover/" + names[2],
                    names[2],
                    progress -> publishProgress(progress)
            ));

            audioBook.setProfileAuthor(downloadFile.start(
                    Server.getUrlHost(mContext) + "/fabi/ressources/profile/" + names[3],
                    names[3],
                    progress -> publishProgress(progress)
            ));

            audioBook.setAudio(downloadFile.start(
                    Server.getUrlHostProd(mContext.getApplicationContext()) + "/api/public/resource/1/audio/" + names[4],
                    names[4],
                    progress -> publishProgress(progress)
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return null; // signale l'echec a onPostExecute au lieu de renvoyer un objet incomplet
        }
        return audioBook;
    }
    @Override
    protected void onPostExecute(AudioBook result) {
        boolean success = result != null;
        if (success) {
            // Convertir l'image Bitmap en un tableau d'octets
            AudioTable audioTable = new AudioTable(mContext);
            audioTable.insert(mIdNumber, mOnlineBook.getId(), mOnlineBook.getDescription(), mOnlineBook.getAuthor(),result.getCover(),result.getAudio(), mOnlineBook.getCategory(), mOnlineBook.getTitle(),result.getCoverCategory(),result.getProfileAuthor(),mTones.getDuration());
            notificationBuilder.setContentText("Téléchargement terminé")
                    .setProgress(0, 0, false)
                    .setSmallIcon(android.R.drawable.stat_sys_download_done);
        } else {
            notificationBuilder.setContentText("Échec du téléchargement")
                    .setProgress(0, 0, false)
                    .setSmallIcon(android.R.drawable.stat_sys_warning);
        }
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
        notificationManager.cancel(NOTIFICATION_ID);
        Intent finishDownloadIntent = new Intent("ACTION_FINISH_DOWNLOAD");
        finishDownloadIntent.putExtra("format","audio");
        finishDownloadIntent.putExtra("success", success);
        mContext.sendBroadcast(finishDownloadIntent);
    }
}