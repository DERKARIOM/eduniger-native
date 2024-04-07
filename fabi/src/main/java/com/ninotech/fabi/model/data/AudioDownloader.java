package com.ninotech.fabi.model.data;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import com.ninotech.fabi.R;
import com.ninotech.fabi.model.table.AudioTable;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class AudioDownloader extends AsyncTask<String, Void, AudioBook> {
    @Override
    protected AudioBook doInBackground(String... name) {
        AudioBook audioBook = new AudioBook();
        try {
            audioBook.setCover(downloadAudio(mContext.getString(R.string.ip_server) + "ressources/cover/" + name[0],name[0]));
            audioBook.setCoverCategory(downloadAudio(mContext.getString(R.string.ip_server) + "ressources/cover/" + name[2],name[2]));
            audioBook.setProfileAuthor(downloadAudio(mContext.getString(R.string.ip_server) + "ressources/profile/" + name[3],name[3]));
            audioBook.setAudio(downloadAudio(mContext.getString(R.string.ip_server) + "ressources/audio/" + name[4],name[4]));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return audioBook;
    }

    @SuppressLint("WrongThread")
    @Override
    protected void onPostExecute(AudioBook result) {
        if (result != null) {
            // Convertir l'image Bitmap en un tableau d'octets
            AudioTable audioTable = new AudioTable(mContext);
            audioTable.insert(mIdNumber, mOnlineBook.getId(), mOnlineBook.getDescription(), mOnlineBook.getAuthor(),result.getCover(),result.getAudio(), mOnlineBook.getCategory(), mOnlineBook.getTitle(),result.getCoverCategory(),result.getProfileAuthor(),mTones.getDuration());
        }
        // Sauvegarder l'image dans la base de données SQLite
        // Utilisez votre DatabaseHelper pour insérer l'image dans la base de données
    }
    public AudioDownloader(Context context , String idNumber , OnlineBook onlineBook, Tones tones)
    {
        mContext = context;
        mIdNumber = idNumber;
        mOnlineBook = onlineBook;
        mTones = tones;
    }
    public Bitmap downloadIMG(String url) throws IOException {
        URL urlImage = new URL(url);
        HttpURLConnection connectionImage = (HttpURLConnection) urlImage.openConnection();
        connectionImage.setDoInput(true);
        connectionImage.connect();
        InputStream inputImage = connectionImage.getInputStream();
        return BitmapFactory.decodeStream(inputImage);
    }

    public byte[] downloadPDF(String url) throws IOException {
        URL urlPdf = new URL(url);
        byte[] bytes=null;
        HttpURLConnection connectionPDF = (HttpURLConnection) urlPdf.openConnection();
        connectionPDF.connect();
        if (connectionPDF.getResponseCode() == HttpURLConnection.HTTP_OK) {
            InputStream inputStream = new BufferedInputStream(connectionPDF.getInputStream());
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            bytes = outputStream.toByteArray();
            inputStream.close();
            outputStream.close();
            connectionPDF.disconnect();
        }
        return bytes;
    }
    public String downloadAudio(String url , String name) throws IOException {
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
    private byte[] compressImage(Bitmap imageBitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        // Compression de l'image avec une qualité de 50 (modifiable)
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }
    private final Context mContext;
    private final String mIdNumber;
    private final OnlineBook mOnlineBook;
    private Tones mTones;
}