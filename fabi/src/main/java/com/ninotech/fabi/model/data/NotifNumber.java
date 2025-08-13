package com.ninotech.fabi.model.data;
import android.content.Context;
import android.content.SharedPreferences;

public class NotifNumber {
    private static final String PREFS_NAME = "location_prefs";
    private static final String KEY_NUMBER = "number";
    public NotifNumber()
    {

    }

    public static void saveLocation(Context context, int number) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(KEY_NUMBER, number);
        editor.apply();
    }

    public static int getLastKnownLocation(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getInt(KEY_NUMBER, 0);
    }
}