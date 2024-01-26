package com.ninotech.fabi.model.data;

public class Category {
    public Category(String id, String title, String blanket) {
        mId = id;
        mTitle = title;
        mBlanket = blanket;
    }
    public Category(String blanket , String title)
    {
        mBlanket = blanket;
        mTitle = title;
    }
    public String getId() {
        return mId;
    }

    public void setId(String id) {
        mId = id;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public String getBlanket() {
        return mBlanket;
    }

    public void setBlanket(String blanket) {
        mBlanket = blanket;
    }

    private String mId;
    private String mTitle;
    private String mBlanket;
}
