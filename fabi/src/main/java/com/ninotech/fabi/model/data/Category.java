package com.ninotech.fabi.model.data;

public class Category {
    public Category(String id, String title, String cover) {
        mId = id;
        mTitle = title;
        mCover = cover;
    }
    public Category(String cover, String title)
    {
        mCover = cover;
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

    public String getCover() {
        return mCover;
    }

    public void setCover(String cover) {
        mCover = cover;
    }

    private String mId;
    private String mTitle;
    private String mCover;
}
