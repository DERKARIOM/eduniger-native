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

    public String getCover() {
        return mCover;
    }

    public void setCover(String cover) {
        mCover = cover;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public String getAuthor() {
        return mAuthor;
    }

    public void setAuthor(String author) {
        mAuthor = author;
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
    public String getCoverCategory() {
        return mCoverCategory;
    }

    public void setCoverCategory(String coverCategory) {
        mCoverCategory = coverCategory;
    }

    public String getProfileAuthor() {
        return mProfileAuthor;
    }

    public void setProfileAuthor(String profileAuthor) {
        mProfileAuthor = profileAuthor;
    }
    private String mCover;
    private String mAuthor;
    private String mDuration;
    private String mAudio;
    private String mCoverCategory;
    private String mProfileAuthor;
}
