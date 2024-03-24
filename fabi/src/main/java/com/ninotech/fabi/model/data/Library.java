package com.ninotech.fabi.model.data;

public class Library {

    public Library(int id, int icon , String label, int number) {
        mId = id;
        mIcon = icon;
        mLabel = label;
        mNumber = number;
    }

    public int getId() {
        return mId;
    }

    public void setId(int id) {
        mId = id;
    }

    public String getLabel() {
        return mLabel;
    }

    public void setLabel(String label) {
        mLabel = label;
    }

    public int getNumber() {
        return mNumber;
    }

    public void setNumber(int number) {
        mNumber = number;
    }

    private int mId;

    public int getIcon() {
        return mIcon;
    }

    public void setIcon(int icon) {
        mIcon = icon;
    }

    private int mIcon;

    private String mLabel;
    private int mNumber;
}
