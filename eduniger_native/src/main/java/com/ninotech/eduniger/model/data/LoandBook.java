package com.ninotech.eduniger.model.data;

public class LoandBook extends Book {
    public LoandBook(String idLoand, String cover, String title, String dateStart, String dateEnd) {
        super(null,title,null);
        mIdLoand = idLoand;
        mCover = cover;
        mDateStart = dateStart;
        mDateEnd = dateEnd;
    }
    public LoandBook(String cover, String title , String dateStart , String dateEnd , long percentage)
    {
        super(null,title);
        mCover = cover;
        mDateStart = dateStart;
        mDateEnd = dateEnd;
        mPercentage = percentage;
    }

    public String getIdLoand() {
        return mIdLoand;
    }
    public void setIdLoand(String idLoand) {
        mIdLoand = idLoand;
    }
    public String getDateStart() {
        return mDateStart;
    }
    public void setDateStart(String dateStart) {
        mDateStart = dateStart;
    }
    public String getDateEnd() {
        return mDateEnd;
    }
    public void setDateEnd(String dateEnd) {
        mDateEnd = dateEnd;
    }
    private String mIdLoand;
    private String mDateStart;
    private String mDateEnd;
    public long getPercentage() {
        return mPercentage;
    }
    public void setPercentage(int percentage) {
        mPercentage = percentage;
    }
    private long mPercentage;
}
