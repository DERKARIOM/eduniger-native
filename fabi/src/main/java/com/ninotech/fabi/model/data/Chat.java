package com.ninotech.fabi.model.data;

import android.content.Context;
import android.database.Cursor;

import com.ninotech.fabi.model.table.StudentTable;

public class Chat {
    public Chat(String profile, String nom, String message, boolean isChat) {
        mProfile = profile;
        mNom = nom;
        mMessage = message;
        this.isChat = isChat;
    }
    public Chat(String idNumber , Context context , String message)
    {
        StudentTable studentTable = new StudentTable(context);
        Cursor cursor = studentTable.getData(idNumber);
        cursor.moveToFirst();
        mNom = cursor.getString(1) + " " +cursor.getString(2);
        mProfile = cursor.getString(6);
        mMessage = message;
    }
    public String getProfile() {
        return mProfile;
    }

    public void setProfile(String profile) {
        mProfile = profile;
    }

    public String getNom() {
        return mNom;
    }

    public void setNom(String nom) {
        mNom = nom;
    }

    public String getMessage() {
        return mMessage;
    }

    public void setMessage(String message) {
        mMessage = message;
    }

    public boolean isChat() {
        return isChat;
    }

    public void setChat(boolean chat) {
        isChat = chat;
    }

    private String mProfile;
    private String mNom;
    private String mMessage;
    private boolean isChat;
}
