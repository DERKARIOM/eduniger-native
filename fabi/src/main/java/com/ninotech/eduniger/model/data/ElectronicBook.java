package com.ninotech.eduniger.model.data;

public class ElectronicBook extends Book {
    public ElectronicBook(String id, String cover, String tile, String category, String author, String pdf) {
        super(id,tile,category,author,null);
        mCover = cover;
        mPdf = pdf;
    }
    public ElectronicBook()
    {
        super(null,null);
    }

    public String getId() {
        return mId;
    }

    public void setId(String id) {
        mId = id;
    }

    public String getPdf() {
        return mPdf;
    }

    public void setPdf(String pdf) {
        mPdf = pdf;
    }

    private String mPdf;

}
