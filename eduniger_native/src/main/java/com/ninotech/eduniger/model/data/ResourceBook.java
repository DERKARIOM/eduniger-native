package com.ninotech.eduniger.model.data;

import android.graphics.Bitmap;

public class ResourceBook {
    public Bitmap getCoverBookBitmap() {
        return mCoverBookBitmap;
    }

    public void setCoverBookBitmap(Bitmap coverBookBitmap) {
        mCoverBookBitmap = coverBookBitmap;
    }



    public ResourceBook() {
        mCoverBookBitmap = null;
        mCoverCategoryBitmap = null;
        mProfileAuthorBitmap = null;
        mPDF = null;
    }

    public Bitmap getCoverCategoryBitmap() {
        return mCoverCategoryBitmap;
    }

    public void setCoverCategoryBitmap(Bitmap coverCategoryBitmap) {
        mCoverCategoryBitmap = coverCategoryBitmap;
    }

    public Bitmap getProfileAuthorBitmap() {
        return mProfileAuthorBitmap;
    }

    public void setProfileAuthorBitmap(Bitmap profileAuthorBitmap) {
        mProfileAuthorBitmap = profileAuthorBitmap;
    }

    private Bitmap mCoverBookBitmap;
    private Bitmap mCoverCategoryBitmap;
    private Bitmap mProfileAuthorBitmap;


    public String getAudio() {
        return mAudio;
    }

    public void setAudio(String audio) {
        mAudio = audio;
    }

    public String getPDF() {
        return mPDF;
    }

    public void setPDF(String PDF) {
        mPDF = PDF;
    }

    private String mPDF;

    private String mAudio;
}
