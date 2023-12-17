package com.fabi.Model;

public class Recenmment {
    public Recenmment(String idLivre, String couverteur, String PDF) {
        mIdLivre = idLivre;
        mCouverteur = couverteur;
        mPDF = PDF;
    }

    public String getIdLivre() {
        return mIdLivre;
    }

    public void setIdLivre(String idLivre) {
        mIdLivre = idLivre;
    }

    public String getCouverteur() {
        return mCouverteur;
    }

    public void setCouverteur(String couverteur) {
        mCouverteur = couverteur;
    }

    public String getPDF() {
        return mPDF;
    }

    public void setPDF(String PDF) {
        mPDF = PDF;
    }

    private String mIdLivre;
    private String mCouverteur;
    private  String mPDF;

}
