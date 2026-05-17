package com.ninotech.eduniger.controleur.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.ninotech.eduniger.controleur.activity.ChatAiActivity;

import org.json.JSONObject;

import java.util.concurrent.TimeUnit;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Audiotranscriptionservice extends Service {

    private static final String TAG           = "AudioService";
    public  static final String ACTION_RESULT = "com.ninotech.eduniger.AUDIO_RESULT";
    private static final String CHANNEL_ID    = "eduna_audio_channel";
    private static final int    NOTIF_ID      = 42;

    private final OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(60,  TimeUnit.SECONDS)
            .writeTimeout(120,   TimeUnit.SECONDS)
            .readTimeout(300,    TimeUnit.SECONDS)
            .build();

    // ── Appelé par Android quand le Service est créé ──────────────────────────
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "✅ onCreate() — Service créé");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "✅ onStartCommand() reçu");

        // ── Sécurité : si Android relance le Service sans Intent ─────────────
        if (intent == null) {
            Log.e(TAG, "❌ Intent null, arrêt");
            stopSelf();
            return START_NOT_STICKY;
        }

        String audioId    = intent.getStringExtra("audio_id");
        String audioTitle = intent.getStringExtra("audio_title");
        String sessionId  = intent.getStringExtra("session_id");
        String idUser     = intent.getStringExtra("id_user");
        String apiUrl     = intent.getStringExtra("api_url");

        Log.d(TAG, "📦 Extras reçus: audioId=" + audioId
                + " | audioTitle=" + audioTitle
                + " | sessionId=" + sessionId
                + " | idUser=" + idUser
                + " | apiUrl=" + apiUrl);

        // ── startForeground OBLIGATOIRE dans les 5s sur Android 8+ ──────────
        try {
            startForeground(NOTIF_ID, buildNotification(audioTitle));
            Log.d(TAG, "✅ startForeground() OK");
        } catch (Exception e) {
            Log.e(TAG, "❌ startForeground() échoué: " + e.getMessage());
            // On continue quand même sans foreground sur vieux Android
        }

        // ── Vérifications de base avant de lancer le thread ──────────────────
        if (apiUrl == null || apiUrl.isEmpty()) {
            Log.e(TAG, "❌ api_url manquante");
            broadcastResult(false, "Configuration API manquante", sessionId);
            stopSelf();
            return START_NOT_STICKY;
        }
        if (audioId == null || audioId.isEmpty()) {
            Log.e(TAG, "❌ audio_id manquant");
            broadcastResult(false, "Identifiant audio manquant", sessionId);
            stopSelf();
            return START_NOT_STICKY;
        }

        final String fAudioId    = audioId;
        final String fAudioTitle = audioTitle != null ? audioTitle : "Audio";
        final String fSessionId  = sessionId;
        final String fIdUser     = idUser != null ? idUser : "";
        final String fApiUrl     = apiUrl;

        // ── Thread de transcription ───────────────────────────────────────────
        new Thread(() -> {
            Log.d(TAG, "🚀 Thread transcription démarré");
            try {
                FormBody body = new FormBody.Builder()
                        .add("id_number",  fIdUser)
                        .add("id_user",    fIdUser)
                        .add("action",     "transcribe_audio")
                        .add("id_book",    fAudioId)
                        .add("message",    "Transcris et résume : " + fAudioTitle)
                        .add("request",    "Transcris et résume : " + fAudioTitle)
                        .add("session_id", fSessionId != null ? fSessionId : "")
                        .build();

                Request request = new Request.Builder()
                        .url(fApiUrl)
                        .post(body)
                        .build();

                Log.d(TAG, "📡 Requête envoyée à: " + fApiUrl);
                Response response = httpClient.newCall(request).execute();
                int httpCode = response.code();
                String rawBody = response.body() != null ? response.body().string() : "";

                Log.d(TAG, "📨 Réponse HTTP " + httpCode
                        + " | body=" + rawBody.substring(0, Math.min(400, rawBody.length())));

                if (httpCode != 200) {
                    broadcastResult(false, "Erreur HTTP " + httpCode + ": " + rawBody.substring(0, Math.min(100, rawBody.length())), fSessionId);
                    return;
                }

                JSONObject json    = new JSONObject(rawBody);
                boolean    success = json.optBoolean("success", false);
                String     resp    = json.optString("response", "");
                String     newSid  = json.optString("session_id", fSessionId);
                String     errMsg  = json.optString("error", "Erreur inconnue");

                Log.d(TAG, "✅ JSON parsé: success=" + success + " | response=" + resp.substring(0, Math.min(100, resp.length())));
                broadcastResult(success, success ? resp : errMsg, newSid);

            } catch (Exception e) {
                Log.e(TAG, "❌ Exception transcription: " + e.getClass().getSimpleName() + ": " + e.getMessage());
                broadcastResult(false, "Erreur: " + e.getMessage(), fSessionId);
            } finally {
                Log.d(TAG, "🏁 Thread terminé, arrêt du Service");
                stopForeground(true);
                stopSelf();
            }
        }).start();

        return START_NOT_STICKY;
    }

    private void broadcastResult(boolean success, String response, String sessionId) {
        Log.d(TAG, "📢 Broadcast: success=" + success + " | response=" + (response != null ? response.substring(0, Math.min(80, response.length())) : "null"));
        Intent broadcast = new Intent(ACTION_RESULT);
        broadcast.putExtra("success",    success);
        broadcast.putExtra("response",   response);
        broadcast.putExtra("session_id", sessionId);
        sendBroadcast(broadcast);
    }

    private Notification buildNotification(String audioTitle) {
        createChannel();
        Intent tapIntent = new Intent(this, ChatAiActivity.class);
        tapIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pi = PendingIntent.getActivity(this, 0, tapIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Eduna — Transcription en cours")
                .setContentText(audioTitle != null ? audioTitle : "Analyse de l'audio...")
                .setSmallIcon(android.R.drawable.ic_media_play)
                .setOngoing(true)
                .setProgress(0, 0, true)
                .setContentIntent(pi)
                .build();
    }

    private void createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, "Transcription Audio", NotificationManager.IMPORTANCE_LOW);
            channel.setDescription("Eduna transcrit vos audios");
            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE))
                    .createNotificationChannel(channel);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) { return null; }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "🔴 onDestroy() — Service détruit");
    }
}