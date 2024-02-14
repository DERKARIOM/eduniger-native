package com.ninotech.fabi.model.data;

public class Physical {
    public Physical(String idLoand, String blanket, String title, String dateStart, String dateEnd) {
        mIdLoand = idLoand;
        mBlanket = blanket;
        mTitle = title;
        mDateStart = dateStart;
        mDateEnd = dateEnd;
    }
    public  Physical(String blanket , String title , String dateStart , String dateEnd)
    {
        mBlanket = blanket;
        mTitle = title;
        mDateStart = dateStart;
        mDateEnd = dateEnd;
    }

    public String getIdLoand() {
        return mIdLoand;
    }

    public void setIdLoand(String idLoand) {
        mIdLoand = idLoand;
    }

    public String getBlanket() {
        return mBlanket;
    }

    public void setBlanket(String blanket) {
        mBlanket = blanket;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
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
    private String mBlanket;
    private String mTitle;
    private String mDateStart;
    private String mDateEnd;

    public int getPercentage() {
        return mPercentage;
    }

    public void setPercentage(int percentage) {
        mPercentage = percentage;
    }

    private int mPercentage;
}
