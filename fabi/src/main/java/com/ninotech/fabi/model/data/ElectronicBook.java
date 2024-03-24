package com.ninotech.fabi.model.data;

import android.graphics.Bitmap;

public class ElectronicBook {
    public ElectronicBook(String id, Bitmap cover, String tile, String category, String author, byte[] electronic) {
        mId = id;
        mCover = cover;
        mTile = tile;
        mCategory = category;
        mAuthor = author;
        mElectronic = electronic;
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

    public String getTile() {
        return mTile;
    }

    public void setTile(String tile) {
        mTile = tile;
    }

    public String getCategory() {
        return mCategory;
    }

    public void setCategory(String category) {
        mCategory = category;
    }

    public String getAuthor() {
        return mAuthor;
    }

    public void setAuthor(String author) {
        mAuthor = author;
    }

    public byte[] getElectronic() {
        return mElectronic;
    }

    public void setElectronic(byte[] electronic) {
        mElectronic = electronic;
    }

    private String mId;
    private Bitmap mCover;
    private String mTile;
    private String mCategory;
    private String mAuthor;
    private byte[] mElectronic;

}
