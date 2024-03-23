package com.ninotech.fabi.model.data;

import android.graphics.Bitmap;

public class ResourceBook {
    public Bitmap getCoverBookBitmap() {
        return mCoverBookBitmap;
    }

    public void setCoverBookBitmap(Bitmap coverBookBitmap) {
        mCoverBookBitmap = coverBookBitmap;
    }

    public byte[] getPdfBytes() {
        return mPdfBytes;
    }

    public void setPdfBytes(byte[] pdfBytes) {
        mPdfBytes = pdfBytes;
    }

    public ResourceBook() {
        mCoverBookBitmap = null;
        mCoverCategoryBitmap = null;
        mProfileAuthorBitmap = null;
        mPdfBytes = null;
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
    private byte[] mPdfBytes;
}
