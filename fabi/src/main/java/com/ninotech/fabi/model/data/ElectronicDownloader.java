package com.ninotech.fabi.model.data;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import com.ninotech.fabi.R;
import com.ninotech.fabi.model.table.ElectronicTable;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ElectronicDownloader extends AsyncTask<String, Void, ElectronicBook> {
    @Override
    protected ElectronicBook doInBackground(String... name) {
        ElectronicBook electronicBook = new ElectronicBook();
        DownloadFile downloadFile = new DownloadFile(mContext);
        try {
            electronicBook.setCover(downloadFile.start(mContext.getString(R.string.ip_server) + "ressources/cover/" + name[0],name[0]));
            electronicBook.setPdf(downloadFile.start(mContext.getString(R.string.ip_server) + "ressources/pdf/" + name[1],name[1]));
            electronicBook.setCoverCategory(downloadFile.start(mContext.getString(R.string.ip_server) + "ressources/cover/" + name[2],name[2]));
            electronicBook.setProfileAuthor(downloadFile.start(mContext.getString(R.string.ip_server) + "ressources/profile/" + name[3],name[3]));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return electronicBook;
    }

    @SuppressLint("WrongThread")
    @Override
    protected void onPostExecute(ElectronicBook result) {
        if (result != null) {
            ElectronicTable electronicTable = new ElectronicTable(mContext);
            electronicTable.insert(mIdNumber, mOnlineBook.getId(), mOnlineBook.getDescription(), mOnlineBook.getAuthor(),result.getCover(),result.getPdf(), mOnlineBook.getCategory(), mOnlineBook.getTitle(),result.getCoverCategory(),result.getProfileAuthor());
        }
        // Sauvegarder l'image dans la base de données SQLite
        // Utilisez votre DatabaseHelper pour insérer l'image dans la base de données
    }
    public ElectronicDownloader(Context context , String idNumber , OnlineBook onlineBook)
    {
        mContext = context;
        mIdNumber = idNumber;
        mOnlineBook = onlineBook;
    }
    private Context mContext;
    private String mIdNumber;
    private OnlineBook mOnlineBook;
}