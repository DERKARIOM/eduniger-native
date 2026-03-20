package com.ninotech.eduniger.model.data;

public class StructureStory {
    private String mId;
    private String mLogo;
    private String mName;
    private boolean mHasNewStory;

    public StructureStory(String id, String logo, String name, boolean hasNewStory) {
        mId = id;
        mLogo = logo;
        mName = name;
        mHasNewStory = hasNewStory;
    }

    public String getId()          { return mId; }
    public String getLogo()        { return mLogo; }
    public String getName()        { return mName; }
    public boolean hasNewStory()   { return mHasNewStory; }
}