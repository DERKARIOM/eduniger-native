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
        DownloadFile downloadFile = new DownloadFile(mContext);
        try {
            audioBook.setCover(downloadFile.start(mContext.getString(R.string.ip_server) + "ressources/cover/" + name[0],name[0]));
            audioBook.setCoverCategory(downloadFile.start(mContext.getString(R.string.ip_server) + "ressources/cover/" + name[2],name[2]));
            audioBook.setProfileAuthor(downloadFile.start(mContext.getString(R.string.ip_server) + "ressources/profile/" + name[3],name[3]));
            audioBook.setAudio(downloadFile.start(mContext.getString(R.string.ip_server) + "ressources/audio/" + name[4],name[4]));
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
            audioTable.insert(mIdNumber, mOnlineBook.getId(), mOnlineBook.getDescription(), "Auteur",result.getCover(),result.getAudio(), mOnlineBook.getCategory(), mOnlineBook.getTitle(),result.getCoverCategory(),result.getProfileAuthor(),mTones.getDuration());
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
    private final Context mContext;
    private final String mIdNumber;
    private final OnlineBook mOnlineBook;
    private Tones mTones;
}