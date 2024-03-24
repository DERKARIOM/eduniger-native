package com.ninotech.fabi.model.data;

import android.graphics.Bitmap;

public class CategoryLocal {
    public CategoryLocal(Bitmap cover, String title) {
        mCover = cover;
        mTitle = title;
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

    private Bitmap mCover;
    private String mTitle;
}
