package com.ninotech.eduniger.model.data;

public class Structure {
    public Structure(String id, String cover, String name, String description, Boolean isAdhere, String banner, String author, String adhererNumber , String bookNumber, String admin) {
        mId = id;
        mCover = cover;
        mName = name;
        mDescription = description;
        this.isAdhere = isAdhere;
        mBanner = banner;
        mAuthor = author;
        mAdhererNumber = adhererNumber;
        mBookNumber = bookNumber;
        mAdmin = admin;
    }

    public String getId() {
        return mId;
    }

    public void setId(String id) {
        mId = id;
    }

    public String getCover() {
        return mCover;
    }

    public void setCover(String cover) {
        mCover = cover;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getDescription() {
        return mDescription;
    }

    public void setDescription(String description) {
        mDescription = description;
    }

    public Boolean isAdhere() {
        return isAdhere;
    }

    public void setAdhere(Boolean adhere) {
        isAdhere = adhere;
    }
    public String getBanner() {
        return mBanner;
    }

    public void setBanner(String banner) {
        mBanner = banner;
    }
    public String getAuthor() {
        return mAuthor;
    }

    public void setAuthor(String author) {
        mAuthor = author;
    }

    public String getAdhererNumber() {
        return mAdhererNumber;
    }

    public void setAdhererNumber(String adhererNumber) {
        mAdhererNumber = adhererNumber;
    }

    public String getBookNumber() {
        return mBookNumber;
    }

    public void setBookNumber(String bookNumber) {
        mBookNumber = bookNumber;
    }
    public String getAdmin() {
        return mAdmin;
    }

    public void setAdmin(String admin) {
        mAdmin = admin;
    }

    private String mId;
    private String mCover;
    private String mName;
    private String mDescription;
    private Boolean isAdhere;
    private String mBanner;
    private String mAuthor;
    private String mAdhererNumber;
    private String mBookNumber;
    private String mAdmin;
}
