package com.ninotech.eduniger.model.data;

public class Suggestion {
    public Suggestion(String idNumber, String objet, String message) {
        mIdNumber = idNumber;
        mObjet = objet;
        mMessage = message;
    }

    public String getIdNumber() {
        return mIdNumber;
    }

    public void setIdNumber(String idNumber) {
        mIdNumber = idNumber;
    }

    public String getObjet() {
        return mObjet;
    }

    public void setObjet(String objet) {
        mObjet = objet;
    }

    public String getMessage() {
        return mMessage;
    }

    public void setMessage(String message) {
        mMessage = message;
    }

    private String mIdNumber;
    private String mObjet;
    private String mMessage;
}
