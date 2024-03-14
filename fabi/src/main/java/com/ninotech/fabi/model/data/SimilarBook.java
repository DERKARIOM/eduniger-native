package com.ninotech.fabi.model.data;

public class SimilarBook {
    public SimilarBook(String mId, String mCover, String mPDF) {
        this.mId = mId;
        this.mCover = mCover;
        this.mPDF = mPDF;
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

    private String mId;
    private String mCover;
    private  String mPDF;

}
