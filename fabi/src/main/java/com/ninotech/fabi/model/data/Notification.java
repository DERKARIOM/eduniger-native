package com.ninotech.fabi.model.data;

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
    public Notification(String id ,String type, String title, String date, String message , String latitude , String longitude) {
        mId = id;
        mType = type;
        mTitle = title;
        mDate = date;
        mMessage = message;
        mLatitude = latitude;
        mLongitude = longitude;
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

    public String getLatitude() {
        return mLatitude;
    }

    public void setLatitude(String latitude) {
        mLatitude = latitude;
    }

    public String getLongitude() {
        return mLongitude;
    }

    public void setLongitude(String longitude) {
        mLongitude = longitude;
    }

    public String getId() {
        return mId;
    }

    public void setId(String id) {
        mId = id;
    }

    private String mId;
    private String mType;
    private String mTitle;
    private String mDate;
    private String mMessage;
    private String mLatitude;
    private String mLongitude;
}
