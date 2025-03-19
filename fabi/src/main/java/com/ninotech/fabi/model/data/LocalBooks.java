package com.ninotech.fabi.model.data;

public class LocalBooks {
    public LocalBooks(String id, String cover, String Ressource, String format) {
        mId = id;
        mCover = cover;
        mRessource = Ressource;
        mFormat = format;
    }

    public LocalBooks(String id, String cover, String title , String category , String author , String Ressource , String description , String format) {
        mId = id;
        mCover = cover;
        mTitle = title;
        mCategory = category;
        mAuthor = author;
        mRessource = Ressource;
        mDescription = description;
        mFormat = format;
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

    public String getRessource() {
        return mRessource;
    }

    public void setRessource(String ressource) {
        mRessource = ressource;
    }
    public String getFormat() {
        return mFormat;
    }

    public void setFormat(String format) {
        this.mFormat = format;
    }
    public String getDescription() {
        return mDescription;
    }

    public void setDescription(String description) {
        mDescription = description;
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
    private String mId;
    private String mCover;
    private String mTitle;
    private String mCategory;
    private String mAuthor;
    private  String mRessource;
    private String mDescription;
    private String mFormat;
}
