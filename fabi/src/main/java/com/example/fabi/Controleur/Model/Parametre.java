package com.example.fabi.Controleur.Model;

public class Parametre {
    public int getIcone() {
        return mIcone;
    }

    public void setIcone(int icone) {
        mIcone = icone;
    }

    public String getTritre() {
        return mTritre;
    }

    public void setTritre(String tritre) {
        mTritre = tritre;
    }

    public String getSousTritre() {
        return mSousTritre;
    }

    public void setSousTritre(String sousTritre) {
        mSousTritre = sousTritre;
    }


    public Parametre(int icone, String tritre, String sousTritre) {
        mIcone = icone;
        mTritre = tritre;
        mSousTritre = sousTritre;
    }

    private int mIcone;
    private String mTritre;
    private String mSousTritre;
}
