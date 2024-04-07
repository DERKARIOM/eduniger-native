package com.ninotech.fabi.model.data;

import android.content.Context;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadFile {
    public DownloadFile(Context context)
    {
        mContext = context;
    }
    public String start(String url , String name) throws IOException {
        URL audioUrl = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) audioUrl.openConnection();
        connection.connect();
        InputStream input = connection.getInputStream();

        // Obtenez le répertoire de stockage interne de l'application
        File internalStorageDir = mContext.getFilesDir();

        // Créez un fichier dans le répertoire de stockage interne pour enregistrer le fichier audio
        File audioFile = new File(internalStorageDir, name);
        FileOutputStream output = new FileOutputStream(audioFile);

        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = input.read(buffer)) != -1) {
            output.write(buffer, 0, bytesRead);
        }

        output.close();
        input.close();
        return audioFile.getAbsolutePath();
    }
    private Context mContext;
}
