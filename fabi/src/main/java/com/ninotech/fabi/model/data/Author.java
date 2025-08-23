package com.ninotech.fabi.model.data;

public class Author extends User {
    public Author(String idNumber, String name, String firstName , String profile , String profession , String call , String email , String whatsapp) {
        super(idNumber, name, firstName);
        mProfile = profile;
        mProfession = profession;
        mCall = call;
        mEmail = email;
        mWhatsapp = whatsapp;
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
    public void setProfile(int profile) {
        this.profile = profile;
    }

    public String getProfession() {
        return mProfession;
    }

    public void setProfession(String profession) {
        mProfession = profession;
    }

    public int getPhoto()
    {
        return profile;
    }
    private String mProfile;
    private int profile;

    private String mProfession;

    public String getCall() {
        return mCall;
    }

    public void setCall(String mCall) {
        this.mCall = mCall;
    }

    public String getEmail() {
        return mEmail;
    }

    public void setEmail(String mEmail) {
        this.mEmail = mEmail;
    }

    public String getWhatsapp() {
        return mWhatsapp;
    }

    public void setmWhatsapp(String mWhatsapp) {
        this.mWhatsapp = mWhatsapp;
    }

    private String mCall;
    private String mEmail;
    private String mWhatsapp;
}
