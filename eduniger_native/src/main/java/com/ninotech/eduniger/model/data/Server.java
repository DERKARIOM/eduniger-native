package com.ninotech.eduniger.model.data;
import android.content.Context;
import android.content.SharedPreferences;

import com.ninotech.eduniger.R;

public class Server {
    private static final String PREFS_NAME = "server_prefs";
    private static final String URL_SERVER = "https://eduniger.com/";
    private static final String URL_API = "https://eduniger.com/api/";
    private static final String IS_ACTIVATE = "pass";
    public Server()
    {

    }

    public static void saveServer(Context context, String urlServer , String urlApi) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(URL_SERVER, urlServer);
        editor.putString(URL_API,urlApi);
        editor.apply();
    }
    public static void saveUrlServer(Context context, String urlServer) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(URL_SERVER, urlServer);
        editor.apply();
    }
    public static void saveUrlApi(Context context, String urlApi) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(URL_API, urlApi);
        editor.apply();
    }

    public static void saveActivate(Context context, int pass) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(IS_ACTIVATE, pass);
        editor.apply();
    }
    public static int getPass(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getInt(IS_ACTIVATE, 0);
    }
    public static String getUrlServer(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(URL_SERVER, context.getString(R.string.url_server));
    }
    public static String getUrlApi(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(URL_API, context.getString(R.string.url_api));
    }
}