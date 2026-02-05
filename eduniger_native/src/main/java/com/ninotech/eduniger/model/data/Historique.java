package com.ninotech.eduniger.model.data;

public class Historique {
    public Historique(String titre, String sousTitre) {
        mTitre = titre;
        mSousTitre = sousTitre;
    }

    public String getTitre() {
        return mTitre;
    }

    public void setTitre(String titre) {
        mTitre = titre;
    }

    public String getSousTitre() {
        return mSousTitre;
    }

    public void setSousTitre(String sousTitre) {
        mSousTitre = sousTitre;
    }

    private String mTitre;
    private String mSousTitre;

}
