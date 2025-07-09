package com.ninotech.fabi.model.data;

import android.content.Context;
import android.database.Cursor;

import com.ninotech.fabi.model.table.StudentTable;

public class User {
    public User(String idNumber, String name, String firstName) {
        mIdNumber = idNumber;
        mName = name;
        mFirstName = firstName;
    }
    public String getIdNumber() {
        return mIdNumber;
    }

    public void setIdNumber(String idNumber) {
        mIdNumber = idNumber;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getFirstName() {
        return mFirstName;
    }

    public void setFirstName(String firstName) {
        mFirstName = firstName;
    }

    protected String mIdNumber;
    protected String mName;
    protected String mFirstName;
}
