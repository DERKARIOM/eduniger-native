package com.ninotech.eduniger.model.data;

public class Phone {
    public Phone(String model, String version) {
        mModel = model;
        this.version = version;
    }

    public String getModel() {
        return mModel;
    }

    public void setModel(String model) {
        mModel = model;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    private String mModel;
    private String version;

}
