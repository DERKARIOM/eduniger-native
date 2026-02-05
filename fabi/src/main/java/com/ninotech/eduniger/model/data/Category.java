package com.ninotech.eduniger.model.data;

public class Category {
    public Category(String cover, String title)
    {
        mCover = cover;
        mTitle = title;
        mNameStruct = null;
    }
    public Category(String cover, String title , String nameStruct)
    {
        mCover = cover;
        mTitle = title;
        mNameStruct = nameStruct;
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
    public String getNameStruct() {
        return mNameStruct;
    }

    public void setNameStruct(String mNameStruct) {
        this.mNameStruct = mNameStruct;
    }

    private String mId;
    private String mTitle;
    private String mCover;
    private String mNameStruct;
}
