package com.ninotech.fabi.model.data;

public class Talks {
    public Talks(String profil, String username, String message) {
        this.profil = profil;
        mUsername = username;
        mMessage = message;
    }

    public String getProfil() {
        return profil;
    }

    public void setProfil(String profil) {
        this.profil = profil;
    }

    public String getUsername() {
        return mUsername;
    }

    public void setUsername(String username) {
        mUsername = username;
    }

    public String getMessage() {
        return mMessage;
    }

    public void setMessage(String message) {
        mMessage = message;
    }

    private String profil;
    private String mUsername;
    private String mMessage;
}
