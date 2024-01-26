package com.ninotech.fabi.model.data;

public class Tones {
    public Tones(int number, String audio, String title, int icoDownload, boolean isPlaying) {
        mNumber = number;
        mAudio = audio;
        mTitle = title;
        mIcoDownload = icoDownload;
        mIsPlaying = isPlaying;
    }

    public int getNumber() {
        return mNumber;
    }

    public void setNumber(int number) {
        mNumber = number;
    }

    public String getAudio() {
        return mAudio;
    }

    public void setAudio(String audio) {
        mAudio = audio;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public int getIcoDownload() {
        return mIcoDownload;
    }

    public void setIcoDownload(int icoDownload) {
        mIcoDownload = icoDownload;
    }
    public boolean isPlaying() {
        return mIsPlaying;
    }

    public void setPlaying(boolean playing) {
        mIsPlaying = playing;
    }

    private int mNumber;
    private String mAudio;
    private String mTitle;
    private int mIcoDownload;
    private boolean mIsPlaying;
}
