package com.ninotech.fabi.model.data;

public class Student extends User{
    public String getDepartment() {
        return mDepartment;
    }

    public void setDepartment(String department) {
        mDepartment = department;
    }

    public String getSection() {
        return mSection;
    }

    public void setSection(String section) {
        mSection = section;
    }
    public Student(String idNumber, String name, String firstName , String department , String section) {
        super(idNumber, name, firstName);
        mDepartment = department;
        mSection = section;
    }
    private String mDepartment;
    private String mSection;
}
