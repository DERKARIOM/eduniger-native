package com.ninotech.fabi.model;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Scanner;
public class Arm {
    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public int getNumberOfDays() {
        return mNumberOfDays;
    }

    public void setNumberOfDays(int numberOfDays) {
        mNumberOfDays = numberOfDays;
    }
    public Arm()
    {
        mTitle = null;
        mNumberOfDays = 0;
    }
    public boolean containsReservation(String input) {
        Pattern pattern = Pattern.compile("\\b(r[eÃ©]servation|r[eÃ©]serve)\\b", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(input);
        return matcher.find();
    }

    public String extractBookTitle(String input) {
        Pattern pattern = Pattern.compile("'(.*?)'");
        Matcher matcher = pattern.matcher(input);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    public int extractDuration(String input) {
        Pattern pattern = Pattern.compile("\\b\\d+\\b");
        Matcher matcher = pattern.matcher(input);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group());
        }
        return 0;
    }
    private String mTitle;
    private int mNumberOfDays;
}