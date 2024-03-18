package com.ninotech.fabi.model.data;
public class Connection {
    public Connection(String message, String source, boolean isWait) {
        mMessage = message;
        mSource = source;
        mIsWait = isWait;
    }

    public String getMessage() {
        return mMessage;
    }

    public void setMessage(String message) {
        mMessage = message;
    }

    public String getSource() {
        return mSource;
    }

    public void setSource(String source) {
        mSource = source;
    }

    public boolean isWait() {
        return mIsWait;
    }

    public void setWait(boolean wait) {
        mIsWait = wait;
    }

    private String mMessage;
    private String mSource;
    private boolean mIsWait;
}
