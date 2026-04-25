package com.ninotech.eduniger.model.data;

public class Category {

    // ==================== Constructeurs existants (inchangés) ====================

    public Category(String cover, String title) {
        mCover           = cover;
        mTitle           = title;
        mNameStruct      = null;
        mDate            = null;
        mNumberSubscribe = 0;
    }

    public Category(String cover, String title, String nameStruct) {
        mCover           = cover;
        mTitle           = title;
        mNameStruct      = nameStruct;
        mDate            = null;
        mNumberSubscribe = 0;
    }

    // ==================== Nouveau constructeur complet (API) ====================

    public Category(String id, String cover, String title, String date, int numberSubscribe) {
        mId              = id;
        mCover           = cover;
        mTitle           = title;
        mNameStruct      = null;
        mDate            = date;
        mNumberSubscribe = numberSubscribe;
    }

    // ==================== Getters / Setters existants (inchangés) ====================

    public String getId() { return mId; }
    public void setId(String id) { mId = id; }

    public String getTitle() { return mTitle; }
    public void setTitle(String title) { mTitle = title; }

    public String getCover() { return mCover; }
    public void setCover(String cover) { mCover = cover; }

    public String getNameStruct() { return mNameStruct; }
    public void setNameStruct(String mNameStruct) { this.mNameStruct = mNameStruct; }

    // ==================== Nouveaux getters / Setters ====================

    public String getDate() { return mDate; }
    public void setDate(String date) { mDate = date; }

    public int getNumberSubscribe() { return mNumberSubscribe; }
    public void setNumberSubscribe(int numberSubscribe) { mNumberSubscribe = numberSubscribe; }

    // ==================== Champs ====================

    private String mId;
    private String mTitle;
    private String mCover;
    private String mNameStruct;
    private String mDate;
    private int    mNumberSubscribe;
}