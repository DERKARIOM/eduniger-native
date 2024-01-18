package com.ninotech.fabi.model.data;

public class User {
    public User(String idNumer, String name, String firstName, String status) {
        mIdNumer = idNumer;
        mName = name;
        mFirstName = firstName;
        mStatus = status;
    }

    public String getIdNumer() {
        return mIdNumer;
    }

    public void setIdNumer(String idNumer) {
        mIdNumer = idNumer;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getFirstName() {
        return mFirstName;
    }

    public void setFirstName(String firstName) {
        mFirstName = firstName;
    }

    public String getStatus() {
        return mStatus;
    }

    public void setStatus(String status) {
        mStatus = status;
    }

    private String mIdNumer;
    private  String mName;
    private String mFirstName;
    private String mStatus;

}
