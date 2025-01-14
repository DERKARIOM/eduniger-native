package com.ninotech.fabi.model.data;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.ninotech.fabi.R;
import com.ninotech.fabi.model.table.ElectronicTable;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ElectronicDownloader extends AsyncTask<String, Integer, ElectronicBook> {

    private Context mContext;
    private String mIdNumber;
    private OnlineBook mOnlineBook;
    private NotificationManager notificationManager;
    private NotificationCompat.Builder notificationBuilder;
    private static final int NOTIFICATION_ID = 1;

    public ElectronicDownloader(Context context, String idNumber, OnlineBook onlineBook) {
        mContext = context;
        mIdNumber = idNumber;
        mOnlineBook = onlineBook;

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
    protected ElectronicBook doInBackground(String... names) {
        ElectronicBook electronicBook = new ElectronicBook();
        DownloadFile downloadFile = new DownloadFile(mContext);
        try {
            for (int i = 0; i < names.length; i++) {
                String url;
                if (i == 1)
                    url = mContext.getString(R.string.ip_server) + "ressources/pdf/" + names[i];
                else
                    url = mContext.getString(R.string.ip_server) + "ressources/cover/" + names[i];
                String fileName = names[i];

                // Simuler la progression pour chaque fichier
                for (int progress = 0; progress <= 100; progress += 20) {
                    Thread.sleep(500); // Pause de 500 ms pour simuler le téléchargement
                    publishProgress(progress); // Publier la progression
                }

                // Télécharger le fichier (méthode start)
                switch (i) {
                    case 0:
                        electronicBook.setCover(downloadFile.start(url, fileName));
                        break;
                    case 1:
                        electronicBook.setPdf(downloadFile.start(url, fileName));
                        break;
                    case 2:
                        electronicBook.setCoverCategory(downloadFile.start(url, fileName));
                        break;
                    case 3:
                        electronicBook.setProfileAuthor(downloadFile.start(url, fileName));
                        break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return electronicBook;
    }

    @Override
    protected void onPostExecute(ElectronicBook result) {
        // Téléchargement terminé
        if (result != null) {
            ElectronicTable electronicTable = new ElectronicTable(mContext);
            electronicTable.insert(
                    mIdNumber,
                    mOnlineBook.getId(),
                    mOnlineBook.getDescription(),
                    "Auteur",
                    result.getCover(),
                    result.getPdf(),
                    mOnlineBook.getCategory(),
                    mOnlineBook.getTitle(),
                    result.getCoverCategory(),
                    result.getProfileAuthor()
            );
        }

        // Mise à jour de la notification pour indiquer la fin du téléchargement
        notificationBuilder.setContentText("Téléchargement terminé")
                .setProgress(0, 0, false)
                .setSmallIcon(android.R.drawable.stat_sys_download_done);
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
    }
}