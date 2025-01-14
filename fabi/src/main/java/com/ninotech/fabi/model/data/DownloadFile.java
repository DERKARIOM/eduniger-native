package com.ninotech.fabi.model.data;

import android.content.Context;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadFile {

    private Context mContext;

    public DownloadFile(Context context) {
        this.mContext = context;
    }

    public String start(String fileUrl, String fileName, ProgressCallback callback) throws Exception {
        InputStream input = null;
        OutputStream output = null;
        HttpURLConnection connection = null;

        try {
            URL url = new URL(fileUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();

            // Vérifier si la connexion est valide
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new Exception("Erreur : Serveur retourné HTTP " + connection.getResponseCode() + " " + connection.getResponseMessage());
            }

            // Taille totale du fichier
            int fileLength = connection.getContentLength();

            // Créer le fichier local
            String filePath = mContext.getExternalFilesDir(null) + "/" + fileName;
            input = connection.getInputStream();
            output = new java.io.FileOutputStream(filePath);

            byte[] buffer = new byte[4096];
            long total = 0;
            int count;
            while ((count = input.read(buffer)) != -1) {
                if (Thread.currentThread().isInterrupted()) {
                    throw new Exception("Téléchargement annulé");
                }
                total += count;

                // Écrire les données dans le fichier
                output.write(buffer, 0, count);

                // Appeler le callback pour mettre à jour la progression
                if (fileLength > 0 && callback != null) {
                    int progress = (int) (total * 100 / fileLength);
                    callback.onProgress(progress);
                }
            }

            return filePath; // Retourner le chemin du fichier téléchargé
        } finally {
            if (output != null) {
                output.close();
            }
            if (input != null) {
                input.close();
            }
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    public interface ProgressCallback {
        void onProgress(int progress);
    }
}