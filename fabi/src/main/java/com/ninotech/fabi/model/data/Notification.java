package com.ninotech.fabi.model.data;

public class Notification {
    public Notification(String titre, String message, String date) {
        mTitre = titre;
        mMessage = message;
        mDate = date;
    }

    public String getTitre() {
        return mTitre;
    }

    public void setTitre(String titre) {
        mTitre = titre;
    }

    public String getMessage() {
        return mMessage;
    }

    public void setMessage(String message) {
        mMessage = message;
    }

    public String getDate() {
        return mDate;
    }

    public void setDate(String date) {
        mDate = date;
    }

    private String mTitre;
    private String mMessage;
    private String mDate;
}
