package com.fabi.Service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;

public class NoteService extends Service {
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        final int delay = 5000; // 5 secondes
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable(){
            public void run(){
            }
        }, delay);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // Nettoyage de votre service
    }
}