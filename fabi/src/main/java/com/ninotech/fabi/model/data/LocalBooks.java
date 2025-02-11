package com.ninotech.fabi.model.data;

public class LocalBooks {
    public LocalBooks(String id, String cover, String PDF, String format) {
        mId = id;
        mCover = cover;
        mPDF = PDF;
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

    public String getPDF() {
        return mPDF;
    }

    public void setPDF(String PDF) {
        mPDF = PDF;
    }
    public String getFormat() {
        return mFormat;
    }

    public void setFormat(String format) {
        this.mFormat = format;
    }
    private String mId;
    private String mCover;
    private  String mPDF;
    private String mFormat;

}
