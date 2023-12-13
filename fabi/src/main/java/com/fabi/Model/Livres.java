package com.fabi.Model;
public class Livres {
    public Livres(String id, String couverture, String titre, String categorie, String isPysique, String nomPdf, String nomAudio, String nbrLike, String nbrVue) {
        mId = id;
        mCouverture = couverture;
        mTitre = titre;
        mCategorie = categorie;
        this.isPysique = isPysique;
        this.nomPdf = nomPdf;
        this.nomAudio = nomAudio;
        mNbrLike = nbrLike;
        mNbrVue = nbrVue;
    }

    private String mId;

    public String getId() {
        return mId;
    }

    public void setId(String id) {
        mId = id;
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

    public String getIsPysique() {
        return isPysique;
    }

    public void setIsPysique(String isPysique) {
        this.isPysique = isPysique;
    }

    public String getNomPdf() {
        return nomPdf;
    }

    public void setNomPdf(String nomPdf) {
        this.nomPdf = nomPdf;
    }

    public String isAudio() {
        return nomAudio;
    }

    public void setNomAudio(String nomAudio) {
        this.nomAudio = nomAudio;
    }

    public String getNbrLike() {
        return mNbrLike;
    }

    public void setNbrLike(String nbrLike) {
        mNbrLike = nbrLike;
    }

    public String getNbrVue() {
        return mNbrVue;
    }

    public void setNbrVue(String nbrVue) {
        mNbrVue = nbrVue;
    }

    private String mCouverture;
    private String mTitre;
    private String mCategorie;
    private String isPysique;
    private String nomPdf;
    private String nomAudio;
    private String mNbrLike;
    private String mNbrVue;

}
