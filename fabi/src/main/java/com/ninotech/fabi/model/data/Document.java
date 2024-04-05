package com.ninotech.fabi.model.data;

public class Document {
    public Document(String id, String title) {
        mId = id;
        mTitle = title;
    }
    public Document(String id) {
        mId = id;
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

    protected String mId;
    protected String mTitle;
}
