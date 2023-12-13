package com.fabi.Model;

public class Electronique {
    public String getLabel() {
        return mLabel;
    }

    public void setLabel(String label) {
        mLabel = label;
    }

    public int getNbrLivre() {
        return mNbrLivre;
    }

    public void setNbrLivre(int nbrLivre) {
        mNbrLivre = nbrLivre;
    }

    public Electronique(String label, int nbrLivre) {
        mLabel = label;
        mNbrLivre = nbrLivre;
    }

    private String mLabel;
    int mNbrLivre;
}
