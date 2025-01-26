package com.ninotech.fabi.model.data;

public class Structure {
    public Structure(String id, String cover, String name, String description, Boolean isAdhere) {
        mId = id;
        mCover = cover;
        mName = name;
        mDescription = description;
        this.isAdhere = isAdhere;
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

    private String mId;
    private String mCover;
    private String mName;
    private String mDescription;
    private Boolean isAdhere;
}
