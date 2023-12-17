package com.fabi.Model;

public class Electronique {

    public Electronique(int id, String label, int nbrLivre) {
        mId = id;
        mLabel = label;
        mNbrLivre = nbrLivre;
    }

    public int getId() {
        return mId;
    }

    public void setId(int id) {
        mId = id;
    }

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

    private int mId;

    private String mLabel;
    int mNbrLivre;
}
