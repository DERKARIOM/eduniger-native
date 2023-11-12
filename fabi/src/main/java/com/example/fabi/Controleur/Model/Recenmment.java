package com.example.fabi.Controleur.Model;

public class Recenmment {
    public Recenmment(String idLivre, String couverteur) {
        mIdLivre = idLivre;
        mCouverteur = couverteur;
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

    private String mIdLivre;
    private String mCouverteur;
}
