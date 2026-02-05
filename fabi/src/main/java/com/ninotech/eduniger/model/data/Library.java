package com.ninotech.eduniger.model.data;

public class Library {

    public Library(int id, int icon , String label, int number) {
        mId = id;
        mIcon = icon;
        mLabel = label;
        mNumber = number;
    }

    public Library(int id, int icon , String label, int number, String idAuthor) {
        mId = id;
        mIcon = icon;
        mLabel = label;
        mNumber = number;
        mIdAuthor = idAuthor;
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
    public String getIdAuthor() {
        return mIdAuthor;
    }

    public void setIdAuthor(String idAuthor) {
        mIdAuthor = idAuthor;
    }

    private int mIcon;

    private String mLabel;
    private int mNumber;
    private String mIdAuthor;
}
