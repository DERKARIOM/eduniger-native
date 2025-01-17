package com.ninotech.fabi.model.data;

public class Author extends User {
    public Author(String idNumber, String name, String firstName , String profile) {
        super(idNumber, name, firstName);
        mProfile = profile;
    }
    public Author(String profile , String name)
    {
        super(null,name,null);
        mProfile = profile;
    }
    public Author(int photo , String name)
    {
        super(null,name,null);
        profile = photo;
    }

    public String getProfile() {
        return mProfile;
    }

    public void setProfile(String profile) {
        mProfile = profile;
    }


    private String mProfile;
    private int profile;
    public int getPhoto()
    {
        return profile;
    }
}
