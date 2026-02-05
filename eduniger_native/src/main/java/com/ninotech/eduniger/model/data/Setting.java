package com.ninotech.eduniger.model.data;

public class Setting {
    public int getIcone() {
        return mIcone;
    }

    public void setIcone(int icone) {
        mIcone = icone;
    }

    public String getTitle() {
        return mTritre;
    }

    public void setTritre(String tritre) {
        mTritre = tritre;
    }

    public String getSubTitle() {
        return mSousTritre;
    }

    public void setSousTritre(String sousTritre) {
        mSousTritre = sousTritre;
    }


    public Setting(int icone, String tritre, String sousTritre) {
        mIcone = icone;
        mTritre = tritre;
        mSousTritre = sousTritre;
    }

    private int mIcone;
    private String mTritre;
    private String mSousTritre;
}
