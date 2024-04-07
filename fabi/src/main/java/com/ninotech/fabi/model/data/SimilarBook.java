package com.ninotech.fabi.model.data;

import android.graphics.Bitmap;

public class SimilarBook {
    public SimilarBook(String id, String cover, String PDF) {
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

    public String getCover() {
        return mCover;
    }

    public void setCover(String cover) {
        mCover = cover;
    }

    public String getPDF() {
        return mPDF;
    }

    public void setPDF(String PDF) {
        mPDF = PDF;
    }

    private String mId;
    private String mCover;
    private  String mPDF;

}
