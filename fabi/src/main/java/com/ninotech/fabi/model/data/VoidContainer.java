package com.ninotech.fabi.model.data;

public class VoidContainer {
    public VoidContainer(int image, String message) {
        mImage = image;
        mMessage = message;
    }

    public int getImage() {
        return mImage;
    }

    public void setImage(int image) {
        mImage = image;
    }

    public String getMessage() {
        return mMessage;
    }

    public void setMessage(String message) {
        mMessage = message;
    }

    private int mImage;
    private String mMessage;
}
