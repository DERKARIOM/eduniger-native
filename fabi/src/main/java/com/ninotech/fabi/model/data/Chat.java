package com.ninotech.fabi.model.data;

import android.content.Context;
import android.database.Cursor;

import com.ninotech.fabi.model.table.StudentTable;

public class Chat {
    public Chat(String profile, String userName, String message, boolean isChat) {
        mProfile = profile;
        mUserName = userName;
        mMessage = message;
        this.isChat = isChat;
    }
    public Chat(String idNumber , Context context , String message)
    {
        StudentTable studentTable = new StudentTable(context);
        Cursor cursor = studentTable.getData(idNumber);
        cursor.moveToFirst();
        mUserName = cursor.getString(1) + " " +cursor.getString(2);
        mProfile = cursor.getString(6);
        mMessage = message;
    }
    public String getProfile() {
        return mProfile;
    }

    public void setProfile(String profile) {
        mProfile = profile;
    }

    public String getUserName() {
        return mUserName;
    }

    public void setUserName(String userName) {
        mUserName = userName;
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
    private String mUserName;
    private String mMessage;
    private boolean isChat;
}
