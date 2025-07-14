package com.ninotech.fabi.model.data;
import android.content.Context;
import android.content.SharedPreferences;

import com.ninotech.fabi.R;

public class Themes {
    private static final String PREFS_NAME = "themes_prefs";
    private static final String NAME = "system";
    public Themes()
    {

    }

    public static void saveTheme(Context context, String name) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(NAME,name);
        editor.apply();
    }
    public static String getName(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(NAME, "system");
    }
}