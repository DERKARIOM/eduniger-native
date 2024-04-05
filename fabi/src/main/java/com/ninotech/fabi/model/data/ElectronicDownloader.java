package com.ninotech.fabi.model.data;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import com.ninotech.fabi.R;
import com.ninotech.fabi.model.table.ElectronicTable;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ElectronicDownloader extends AsyncTask<String, Void, ResourceBook> {
    @Override
    protected ResourceBook doInBackground(String... name) {
        ResourceBook resourceBook = new ResourceBook();
        try {
            resourceBook.setCoverBookBitmap(downloadIMG(mContext.getString(R.string.ip_server) + "ressources/cover/" + name[0]));
            resourceBook.setPDF(downloadPDF(mContext.getString(R.string.ip_server) + "ressources/pdf/" + name[1],name[1]));
            resourceBook.setCoverCategoryBitmap(downloadIMG(mContext.getString(R.string.ip_server) + "ressources/cover/" + name[2]));
            resourceBook.setProfileAuthorBitmap(downloadIMG(mContext.getString(R.string.ip_server) + "ressources/profile/" + name[3]));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resourceBook;
    }

    @SuppressLint("WrongThread")
    @Override
    protected void onPostExecute(ResourceBook result) {
        if (result != null) {
            // Convertir l'image Bitmap en un tableau d'octets
            ByteArrayOutputStream coverBookStream = new ByteArrayOutputStream();
            ByteArrayOutputStream coverCategoryStream = new ByteArrayOutputStream();
            ByteArrayOutputStream profileAuthorStream = new ByteArrayOutputStream();
            result.getCoverBookBitmap().compress(Bitmap.CompressFormat.JPEG ,50, coverBookStream);
            result.getCoverCategoryBitmap().compress(Bitmap.CompressFormat.PNG, 50, coverCategoryStream);
            result.getProfileAuthorBitmap().compress(Bitmap.CompressFormat.JPEG, 50, profileAuthorStream);
            byte[] coverBookBytes = coverBookStream.toByteArray();
            byte[] coverCategoryBytes = coverCategoryStream.toByteArray();
            byte[] profileAuthorBytes = profileAuthorStream.toByteArray();
            ElectronicTable electronicTable = new ElectronicTable(mContext);
            electronicTable.insert(mIdNumber,mBook.getId(),mBook.getDescription(),mBook.getAuthor(),coverBookBytes,result.getPDF(),mBook.getCategory(),mBook.getTitle(),coverCategoryBytes,profileAuthorBytes);
        }
        // Sauvegarder l'image dans la base de données SQLite
        // Utilisez votre DatabaseHelper pour insérer l'image dans la base de données
    }
    public ElectronicDownloader(Context context , String idNumber , Book book)
    {
        mContext = context;
        mIdNumber = idNumber;
        mBook = book;
    }
    public Bitmap downloadIMG(String url) throws IOException {
        URL urlImage = new URL(url);
        HttpURLConnection connectionImage = (HttpURLConnection) urlImage.openConnection();
        connectionImage.setDoInput(true);
        connectionImage.connect();
        InputStream inputImage = connectionImage.getInputStream();
        return BitmapFactory.decodeStream(inputImage);
    }
    private byte[] compressImage(Bitmap imageBitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        // Compression de l'image avec une qualité de 50 (modifiable)
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }
    public String downloadPDF(String url , String name) throws IOException {
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
    private String mIdNumber;
    private Book mBook;
    //private Author mAuthor;
}