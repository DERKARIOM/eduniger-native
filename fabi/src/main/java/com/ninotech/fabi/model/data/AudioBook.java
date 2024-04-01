package com.ninotech.fabi.model.data;

import android.graphics.Bitmap;

public class AudioBook {
    public AudioBook(String id , Bitmap cover, String title, String author, String time) {
        mId = id;
        mCover = cover;
        mTitle = title;
        mAuthor = author;
        mTime = time;
    }

    public Bitmap getCover() {
        return mCover;
    }

    public void setCover(Bitmap cover) {
        mCover = cover;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public String getAuthor() {
        return mAuthor;
    }

    public void setAuthor(String author) {
        mAuthor = author;
    }

    public String getTime() {
        return mTime;
    }

    public void setTime(String time) {
        mTime = time;
    }

    public String getId() {
        return mId;
    }

    public void setId(String id) {
        mId = id;
    }

    private String mId;
    private Bitmap mCover;
    private String mTitle;
    private String mAuthor;
    private String mTime;
}
