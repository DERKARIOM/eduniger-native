package com.ninotech.fabi.model.data;

import android.graphics.Bitmap;

public class SimilarBook {
    public SimilarBook(String id, Bitmap cover, String PDF) {
        mId = id;
        mCover = cover;
        mPDF = PDF;
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

    public String getPDF() {
        return mPDF;
    }

    public void setPDF(String PDF) {
        mPDF = PDF;
    }

    private String mId;
    private Bitmap mCover;
    private  String mPDF;

}
