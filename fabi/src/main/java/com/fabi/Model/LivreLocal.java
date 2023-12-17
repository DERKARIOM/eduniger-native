package com.fabi.Model;

public class LivreLocal {

    public LivreLocal(String matricule, String couverture, String titre, String categorie, String auteur, String documentElec) {
        mMatricule = matricule;
        mCouverture = couverture;
        mTitre = titre;
        mCategorie = categorie;
        mAuteur = auteur;
        mDocumentElec = documentElec;
    }

    public String getMatricule() {
        return mMatricule;
    }

    public void setMatricule(String matricule) {
        mMatricule = matricule;
    }

    public String getCouverture() {
        return mCouverture;
    }

    public void setCouverture(String couverture) {
        mCouverture = couverture;
    }

    public String getTitre() {
        return mTitre;
    }

    public void setTitre(String titre) {
        mTitre = titre;
    }

    public String getCategorie() {
        return mCategorie;
    }

    public void setCategorie(String categorie) {
        mCategorie = categorie;
    }

    public String getAuteur() {
        return mAuteur;
    }

    public void setAuteur(String auteur) {
        mAuteur = auteur;
    }

    public String getDocumentElec() {
        return mDocumentElec;
    }

    public void setDocumentElec(String documentElec) {
        mDocumentElec = documentElec;
    }

    private String mMatricule;
    private String mCouverture;
    private String mTitre;
    private String mCategorie;
    private String mAuteur;
    private String mDocumentElec;
}
