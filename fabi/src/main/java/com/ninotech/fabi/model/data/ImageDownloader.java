package com.ninotech.fabi.model.data;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import com.ninotech.fabi.R;
import com.ninotech.fabi.model.table.ElectronicTable;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class ImageDownloader extends AsyncTask<String, Void, ResourceBook> {
    @Override
    protected ResourceBook doInBackground(String... name) {
        String blanketBookURL = mContext.getString(R.string.ip_server) + "ressources/cover/" + name[0];
        String pdfURL = mContext.getString(R.string.ip_server) + "ressources/pdf/" + name[1];
        ResourceBook resourceBook = new ResourceBook();
        try {
            // Download Blanket
            resourceBook.setBitmap(downloadIMG(blanketBookURL));

            // Download Book
            URL urlPdf = new URL(pdfURL);
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
                resourceBook.setBytes(outputStream.toByteArray());
                inputStream.close();
                outputStream.close();
                connectionPDF.disconnect();
            }

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
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            result.getBitmap().compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] bytes = stream.toByteArray();
            ElectronicTable electronicTable = new ElectronicTable(mContext);
            electronicTable.insert(mIdNumber,mBook.getId(),mBook.getDescription(),mBook.getAuthor(),bytes,result.getBytes(),mBook.getCategory().get(0),mBook.getTitle(),"ras","ras");
            //Log.e("CAARAMOO",mBytes.toString());
        }
        // Sauvegarder l'image dans la base de données SQLite
        // Utilisez votre DatabaseHelper pour insérer l'image dans la base de données
    }
    public ImageDownloader(Context context , String idNumber , Book book)
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
    private Context mContext;
    private String mIdNumber;
    private Book mBook;
}