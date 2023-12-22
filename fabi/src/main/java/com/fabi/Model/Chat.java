package com.fabi.Model;

public class Chat {
    public Chat(String profile, String nom, String message, boolean isChat) {
        mProfile = profile;
        mNom = nom;
        mMessage = message;
        this.isChat = isChat;
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
