package com.ninotech.fabi.model.data;

import android.graphics.Bitmap;

public class ElectronicBook extends Book {
    public ElectronicBook(String id, Bitmap cover, String tile, String category, String author, String pdf) {
        super(id,tile,category,author,null);
        mCover = cover;
        mPdf = pdf;
    }

    public String getId() {
        return mId;
    }

    public void setId(String id) {
        mId = id;
    }

    public Bitmap getCover() {
        return mCover;
    }

    public void setCover(Bitmap cover) {
        mCover = cover;
    }

    public String getPdf() {
        return mPdf;
    }

    public void setPdf(String pdf) {
        mPdf = pdf;
    }
    private Bitmap mCover;

    private String mPdf;

}
