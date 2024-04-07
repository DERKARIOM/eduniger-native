package com.ninotech.fabi.model.data;

public class AudioBook extends Book {
    public AudioBook(String id , String cover, String title, String author, String duration, String audio) {
       super(id,title,null,author,null);
        mCover = cover;
        mDuration = duration;
        mAudio = audio;
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
    private String mDuration;
    private String mAudio;
}
