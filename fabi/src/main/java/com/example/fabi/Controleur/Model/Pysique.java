package com.example.fabi.Controleur.Model;

public class Pysique {
    public Pysique(int couverture, String titre, String dateInit, String dateFinale, String pourcentage) {
        mCouverture = couverture;
        mTitre = titre;
        mDateInit = dateInit;
        mDateFinale = dateFinale;
        mPourcentage = pourcentage;
    }

    public int getCouverture() {
        return mCouverture;
    }

    public void setCouverture(int couverture) {
        mCouverture = couverture;
    }

    public String getTitre() {
        return mTitre;
    }

    public void setTitre(String titre) {
        mTitre = titre;
    }

    public String getDateInit() {
        return mDateInit;
    }

    public void setDateInit(String dateInit) {
        mDateInit = dateInit;
    }

    public String getDateFinale() {
        return mDateFinale;
    }

    public void setDateFinale(String dateFinale) {
        mDateFinale = dateFinale;
    }

    public String getPourcentage() {
        return mPourcentage;
    }

    public void setPourcentage(String pourcentage) {
        mPourcentage = pourcentage;
    }

    private int mCouverture;
    private  String mTitre;
    private  String mDateInit;
    private  String mDateFinale;
    private String mPourcentage;
}
