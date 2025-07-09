package com.ninotech.fabi.model.data;

public class Update {
    public Update(String idNumber, String column, String newValues) {
        mIdNumber = idNumber;
        mColumn = column;
        mNewValues = newValues;
    }

    public String getIdNumber() {
        return mIdNumber;
    }

    public void setIdNumber(String idNumber) {
        mIdNumber = idNumber;
    }

    public String getColumn() {
        return mColumn;
    }

    public void setColumn(String column) {
        mColumn = column;
    }

    public String getNewValues() {
        return mNewValues;
    }

    public void setNewValues(String newValues) {
        mNewValues = newValues;
    }

    private String mIdNumber;
    private String mColumn;
    private String mNewValues;
}
