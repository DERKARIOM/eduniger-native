package com.ninotech.eduniger.model.service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AudioReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Intent serviceIntent = new Intent(context, AudioService.class);

        if ("PLAY".equals(action)) {
            serviceIntent.setAction("PLAY");
        } else if ("PAUSE".equals(action)) {
            serviceIntent.setAction("PAUSE");
        } else if ("STOP".equals(action)) {
            serviceIntent.setAction("STOP");
        }
        context.startService(serviceIntent);
    }
}
