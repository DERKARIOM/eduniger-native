package com.ninotech.fabi.model.data;
import android.content.Context;
import android.content.SharedPreferences;

import com.ninotech.fabi.R;

public class Server {
    private static final String PREFS_NAME = "server_prefs";
    private static final String IP_SERVER = "https://telesafe.net/fabi/";
    private static final String IP_SERVER_ANDROID = "https://telesafe.net/fabi/android/";
    private static final String IS_ACTIVATE = "pass";
    public Server()
    {

    }

    public static void saveServer(Context context, String ip_server , String ip_server_android) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(IP_SERVER, ip_server);
        editor.putString(IP_SERVER_ANDROID,ip_server_android);
        editor.apply();
    }
    public static void saveIpServer(Context context, String ip_server) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(IP_SERVER, ip_server);
        editor.apply();
    }
    public static void saveIpServerAndroid(Context context, String ip_server_android) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(IP_SERVER_ANDROID, ip_server_android);
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
    public static String getIpServer(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(IP_SERVER, context.getString(R.string.ip_server));
    }
    public static String getIpServerAndroid(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(IP_SERVER_ANDROID, context.getString(R.string.ip_server_android));
    }
}