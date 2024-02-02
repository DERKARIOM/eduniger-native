package com.ninotech.fabi.model.data;

public class Author extends User {
    public Author(String idNumber, String name, String firstName , String profile , String nationality) {
        super(idNumber, name, firstName);
        mProfile = profile;
        mNationality = nationality;
    }

    public String getProfile() {
        return mProfile;
    }

    public void setProfile(String profile) {
        mProfile = profile;
    }

    public String getNationality() {
        return mNationality;
    }

    public void setNationality(String nationality) {
        mNationality = nationality;
    }
    private String mProfile;
    private String mNationality;
}
