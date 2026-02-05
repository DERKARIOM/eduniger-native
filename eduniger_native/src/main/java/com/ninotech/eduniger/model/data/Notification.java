package com.ninotech.eduniger.model.data;

public class Notification {
    public Notification() {
        mType = null;
        mTitle = null;
        mDate = null;
        mMessage = null;
    }
    public Notification(String id ,String type, String title, String date, String message) {
        mId = id;
        mType = type;
        mTitle = title;
        mDate = date;
        mMessage = message;
    }
    public Notification(String id ,String type, String title, String date, String message , String link , String idLink) {
        mId = id;
        mType = type;
        mTitle = title;
        mDate = date;
        mMessage = message;
        mLink = link;
        mIdLink = idLink;
    }

    public String getType() {
        return mType;
    }

    public void setType(String type) {
        mType = type;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public String getDate() {
        return mDate;
    }

    public void setDate(String date) {
        mDate = date;
    }

    public String getMessage() {
        return mMessage;
    }

    public void setMessage(String message) {
        mMessage = message;
    }

    public String getId() {
        return mId;
    }

    public void setId(String id) {
        mId = id;
    }
    public String getLink() {
        return mLink;
    }

    public void setLink(String mLink) {
        this.mLink = mLink;
    }
    public String getIdLink() {
        return mIdLink;
    }

    public void setIdLink(String mIdLink) {
        this.mIdLink = mIdLink;
    }

    private String mId;
    private String mType;
    private String mTitle;
    private String mDate;
    private String mMessage;
    private String mLink;
    private String mIdLink;
}
