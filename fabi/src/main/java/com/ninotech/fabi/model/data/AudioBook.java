package com.ninotech.fabi.model.data;

public class AudioBook extends Book {
    public AudioBook(String id , String cover, String title, String author, String duration, String audio) {
       super(id,title,null,author,null);
        mCover = cover;
        mDuration = duration;
        mAudio = audio;
        mIsPlayer =false;
        mIsPlayerList=false;
    }

    public AudioBook(String id , String cover, String title, String author, String duration, String audio,boolean isPlayer, boolean isPlayerList) {
        super(id,title,null,author,null);
        mCover = cover;
        mDuration = duration;
        mAudio = audio;
        mIsPlayer = isPlayer;
        mIsPlayerList=isPlayerList;
    }

    public AudioBook()
    {
        super(null,null);
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public String getDuration() {
        return mDuration;
    }

    public void setDuration(String duration) {
        mDuration = duration;
    }

    public String getId() {
        return mId;
    }

    public void setId(String id) {
        mId = id;
    }
    public String getAudio() {
        return mAudio;
    }

    public void setAudio(String audio) {
        mAudio = audio;
    }
    public boolean isPlayer() {
        return mIsPlayer;
    }

    public void setPlayer(boolean player) {
        mIsPlayer = player;
    }
    public boolean isPlayerList() {
        return mIsPlayerList;
    }

    public void setPlayerList(boolean playerList) {
        mIsPlayerList = playerList;
    }
    private String mDuration;
    private String mAudio;
    private boolean mIsPlayer;
    private boolean mIsPlayerList;
}
