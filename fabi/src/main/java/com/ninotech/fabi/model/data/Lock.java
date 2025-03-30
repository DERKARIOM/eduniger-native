package com.ninotech.fabi.model.data;
import android.content.Context;
import android.content.SharedPreferences;

public class Lock {
    private static final String PREFS_NAME = "lock_prefs";
    private static final String KEY_PASS = "pass";
    private static final String KEY_DIGITAL_PRINT = "digital_print";
    public Lock()
    {

    }

    public static void saveLock(Context context, int pass , int digitalPrint) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(KEY_PASS, pass);
        editor.putInt(KEY_DIGITAL_PRINT,digitalPrint);
        editor.apply();
    }
    public static void savePass(Context context, int pass) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(KEY_PASS, pass);
        editor.apply();
    }
    public static void saveDiditalPrint(Context context, int digitalPrint) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(KEY_DIGITAL_PRINT, digitalPrint);
        editor.apply();
    }

    public static int getLastKnownPass(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getInt(KEY_PASS, 0);
    }
    public static int getLastKnownDigitalPrint(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getInt(KEY_DIGITAL_PRINT, 0);
    }
}