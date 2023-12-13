package com.fabi.Model;

public class Notification {
    public String getId() {
        return mId;
    }

    public void setId(String id) {
        mId = id;
    }

    public String getMessage() {
        return mMessage;
    }

    public void setMessage(String message) {
        mMessage = message;
    }

    public String getUser() {
        return mUser;
    }

    public void setUser(String user) {
        mUser = user;
    }

    public String getTime() {
        return mTime;
    }

    public void setTime(String time) {
        mTime = time;
    }

    public String getDate() {
        return mDate;
    }

    public void setDate(String date) {
        mDate = date;
    }

    public Notification(String id,  String user , String moi, String message, String time, String date , String vue) {
        mId = id;
        mUser = user;
        mMoi = moi;
        mMessage = message;
        mTime = time;
        mDate = date;
        mVue = vue;
    }

    public String getMoi() {
        return mMoi;
    }

    public void setMoi(String moi) {
        mMoi = moi;
    }
    public String getVue() {
        return mVue;
    }

    public void setVue(String vue) {
        mVue = vue;
    }


    private String mId;
    private String mUser;
    private String mMoi;
    private String mMessage;
    private String mTime;
    private String mDate;

    private String mVue;
}
