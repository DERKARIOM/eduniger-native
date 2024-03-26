package com.ninotech.fabi.model.data;

import android.graphics.Bitmap;

public class AuthorLocal {
    public AuthorLocal(Bitmap profile, String name) {
        mProfile = profile;
        mName = name;
    }

    public Bitmap getProfile() {
        return mProfile;
    }

    public void setProfile(Bitmap profile) {
        mProfile = profile;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    private Bitmap mProfile;
    private String mName;
}
