package com.ninotech.fabi.model.data;

import android.graphics.Bitmap;

public class AudioBook {
    public AudioBook(String id , Bitmap cover, String title, String author, String duration, String audio) {
        mId = id;
        mCover = cover;
        mTitle = title;
        mAuthor = author;
        mDuration = duration;
        mAudio = audio;
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

    public String getDuration() {
        return mDuration;
    }

    public void setDuration(String duration) {
        mDuration = duration;
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
    private String mDuration;

    public String getAudio() {
        return mAudio;
    }

    public void setAudio(String audio) {
        mAudio = audio;
    }

    private String mAudio;
}
