package com.ninotech.fabi.model.data;

public class Son {
    public Son(int num, String audio, String titre, int icoTel , boolean isPlaying) {
        mNum = num;
        mAudio = audio;
        mTitre = titre;
        mIcoTel = icoTel;
        mIsPlaying = isPlaying;
    }

    public int getNum() {
        return mNum;
    }

    public void setNum(int num) {
        mNum = num;
    }

    public String getAudio() {
        return mAudio;
    }

    public void setAudio(String audio) {
        mAudio = audio;
    }

    public String getTitre() {
        return mTitre;
    }

    public void setTitre(String titre) {
        mTitre = titre;
    }

    public int getIcoTel() {
        return mIcoTel;
    }

    public void setIcoTel(int icoTel) {
        mIcoTel = icoTel;
    }

    private int mNum;
    private String mAudio;
    private String mTitre;
    private int mIcoTel;

    public boolean isPlaying() {
        return mIsPlaying;
    }

    public void setPlaying(boolean playing) {
        mIsPlaying = playing;
    }

    private boolean mIsPlaying;
}
