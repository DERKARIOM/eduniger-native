package com.ninotech.eduniger.model.data;

import android.graphics.Bitmap;

public class MyProfile {
    public MyProfile(Bitmap photo, String username) {
        mPhoto = photo;
        mUsername = username;
    }

    public Bitmap getPhoto() {
        return mPhoto;
    }

    public void setPhoto(Bitmap photo) {
        mPhoto = photo;
    }

    public String getUsername() {
        return mUsername;
    }

    public void setUsername(String username) {
        mUsername = username;
    }

    private Bitmap mPhoto;
    private String mUsername;
}
