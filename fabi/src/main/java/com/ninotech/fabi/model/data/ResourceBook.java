package com.ninotech.fabi.model.data;

import android.graphics.Bitmap;

public class ResourceBook {
    public Bitmap getBitmap() {
        return mBitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        mBitmap = bitmap;
    }

    public byte[] getBytes() {
        return mBytes;
    }

    public void setBytes(byte[] bytes) {
        mBytes = bytes;
    }

    public ResourceBook() {
        mBitmap = null;
        mBytes = null;
    }

    private Bitmap mBitmap;
    private byte[] mBytes;
}
