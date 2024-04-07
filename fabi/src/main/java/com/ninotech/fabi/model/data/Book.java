package com.ninotech.fabi.model.data;

public class Book {
    public Book(String id, String title , String category , String author , String description) {
        mId = id;
        mTitle = title;
        mCategory = category;
        mAuthor = author;
        mDescription = description;
    }
    public Book(String id, String title) {
        mId = id;
        mTitle = title;
    }
    public Book(String id, String title , String category) {
        mId = id;
        mTitle = title;
        mCategory = category;
    }
    public Book(String id) {
        mId = id;
    }

    public String getId() {
        return mId;
    }

    public void setId(String id) {
        mId = id;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }
    public String getCategory() {
        return mCategory;
    }

    public void setCategory(String category) {
        mCategory = category;
    }
    public String getAuthor() {
        return mAuthor;
    }

    public void setAuthor(String author) {
        mAuthor = author;
    }
    public String getDescription() {
        return mDescription;
    }

    public void setDescription(String description) {
        mDescription = description;
    }
    public String getCover() {
        return mCover;
    }

    public void setCover(String cover) {
        mCover = cover;
    }

    public String getCoverCategory() {
        return mCoverCategory;
    }

    public void setCoverCategory(String coverCategory) {
        mCoverCategory = coverCategory;
    }

    public String getProfileAuthor() {
        return mProfileAuthor;
    }

    public void setProfileAuthor(String profileAuthor) {
        mProfileAuthor = profileAuthor;
    }

    protected String mId;
    protected String mTitle;
    protected String mCover;
    protected String mCategory;
    protected String mAuthor;
    protected String mDescription;
    protected String mCoverCategory;
    protected String mProfileAuthor;
}
