package com.ninotech.fabi.model.data;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import com.ninotech.fabi.model.table.ElectronicTable;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ImageDownloader extends AsyncTask<String, Void, Bitmap> {
    @Override
    protected Bitmap doInBackground(String... urls) {
        String imageURL = urls[0];
        Bitmap bitmap = null;
        try {
            URL url = new URL(imageURL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            bitmap = BitmapFactory.decodeStream(input);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    @SuppressLint("WrongThread")
    @Override
    protected void onPostExecute(Bitmap result) {
        if (result != null) {
            // Convertir l'image Bitmap en un tableau d'octets
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            result.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] bytes = stream.toByteArray();
            ElectronicTable electronicTable = new ElectronicTable(mContext);
            electronicTable.insert(mIdNumber,mBook.getId(),mBook.getDescription(),mBook.getAuthor(),bytes,mBook.getElectronic(),mBook.getCategory().get(0),mBook.getTitle(),"ras","ras");
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
    private Context mContext;
    private String mIdNumber;
    private Book mBook;
}