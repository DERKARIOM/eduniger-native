package com.ninotech.eduniger.model.service;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.ninotech.eduniger.R;
import com.ninotech.eduniger.controleur.activity.BookActivity;
import com.ninotech.eduniger.controleur.activity.ContainerActivity;
import com.ninotech.eduniger.controleur.activity.MainActivity;
import com.ninotech.eduniger.controleur.activity.NotificationActivity;
import com.ninotech.eduniger.model.data.DownloadFile;
import com.ninotech.eduniger.model.data.Server;
import com.ninotech.eduniger.model.table.LoandTable;
import com.ninotech.eduniger.model.table.NotificationTable;
import com.ninotech.eduniger.model.table.Session;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG        = "FCMService";
    private static final String CHANNEL_ID = "eduniger_channel";
    private static final String ACTION_UPDATE_BADGE = "ACTION_UPDATE_NOTIFICATION_BADGE";

    // ================================================================
    // TOKEN RENOUVELÉ PAR FIREBASE
    // ================================================================

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.d(TAG, "Token FCM renouvelé : " + token);

        Session session = new Session(getApplicationContext());
        String idNumber = session.getIdNumber();

        if (idNumber != null && !idNumber.isEmpty() && !"null".equals(idNumber)) {
            sendTokenToServer(idNumber, token);
        }
    }

    // ================================================================
    // RÉCEPTION D'UNE NOTIFICATION
    // ================================================================

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        String title   = "";
        String message = "";

        if (remoteMessage.getNotification() != null) {
            title   = remoteMessage.getNotification().getTitle()  != null
                    ? remoteMessage.getNotification().getTitle()  : "";
            message = remoteMessage.getNotification().getBody()   != null
                    ? remoteMessage.getNotification().getBody()   : "";
        }

        String type      = remoteMessage.getData().get("type")      != null
                ? remoteMessage.getData().get("type")      : "";
        String extraData = remoteMessage.getData().get("extraData") != null
                ? remoteMessage.getData().get("extraData") : "";

        Log.d(TAG, "Notification reçue — type: " + type + " | extraData: " + extraData);

        createNotificationChannel();

        // type = "5" → synchroniser les loands non vus
        if ("5".equals(type)) {
            syncUnreadLoands(title, message);
        } else {
            showNotification(title, message, type, extraData);
        }
    }


    // ================================================================
    // SYNCHRONISATION DES LOANDS NON VUS
    // ================================================================

    private void syncUnreadLoands(String notifTitle, String notifMessage) {
        new Thread(() -> {
            try {
                Session session  = new Session(getApplicationContext());
                String idUser    = session.getIdNumber();

                if (idUser == null || idUser.isEmpty() || "null".equals(idUser)) {
                    Log.w(TAG, "Utilisateur non connecté, sync loand annulée");
                    return;
                }

                OkHttpClient client = new OkHttpClient();

                // ── 1. Récupérer les loands non vus ─────────────────────────
                RequestBody bodyGet = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("idUser", idUser)
                        .build();

                Request requestGet = new Request.Builder()
                        .url(Server.getUrlApi(getApplicationContext()) + "get_unread_loands.php")
                        .post(bodyGet)
                        .build();

                String jsonResponse;
                try (Response response = client.newCall(requestGet).execute()) {
                    if (response.body() == null) {
                        Log.e(TAG, "Réponse vide de get_unread_loands.php");
                        return;
                    }
                    jsonResponse = response.body().string();
                }

                Log.d(TAG, "get_unread_loands response : " + jsonResponse);

                JSONObject jsonObject = new JSONObject(jsonResponse);

                if (!jsonObject.getBoolean("success")) {
                    Log.e(TAG, "Erreur API : " + jsonObject.optString("error"));
                    return;
                }

                int count = jsonObject.getInt("count");
                if (count == 0) {
                    Log.d(TAG, "Aucun loand non vu");
                    showNotification(notifTitle, notifMessage, "loand", "");
                    return;
                }

                JSONArray data        = jsonObject.getJSONArray("data");
                LoandTable loandTable = new LoandTable(getApplicationContext());
                DownloadFile downloader = new DownloadFile(getApplicationContext());

                // ── 2. Traiter chaque loand ──────────────────────────────────
                for (int i = 0; i < data.length(); i++) {
                    JSONObject loand = data.getJSONObject(i);

                    String idLoand        = loand.getString("idLoand");
                    String idBook         = loand.getString("idBook");
                    String bookTitle      = loand.optString("bookTitle",  "");
                    String bookCover      = loand.optString("bookCover",  "");
                    String dateLoand      = loand.optString("dateLoand",  "");
                    String realReturnDate = loand.optString("realReturnDate", "");

                    // ── 3. Télécharger la couverture en local ────────────────
                    String localCoverPath = "";
                    if (!bookCover.isEmpty() && !"null".equals(bookCover)) {
                        try {
                            String coverUrl = Server.getUrlServer(getApplicationContext())
                                    + "admin-api/storage/app/private/structures/1/blankets/"
                                    + bookCover;

                            localCoverPath = downloader.start(
                                    coverUrl,
                                    bookCover,
                                    null
                            );
                            Log.d(TAG, "Couverture téléchargée : " + localCoverPath);

                        } catch (Exception e) {
                            Log.e(TAG, "Erreur téléchargement couverture : " + e.getMessage());
                            localCoverPath = bookCover;
                        }
                    }

                    // ── 4. Insérer dans la BDD locale ────────────────────────
                    boolean inserted = loandTable.insert(
                            idLoand,
                            idUser,
                            localCoverPath,
                            bookTitle,
                            dateLoand,
                            realReturnDate
                    );

                    Log.d(TAG, "Loand " + idLoand + " inséré en local : " + inserted);

                    // ── 5. Marquer le loand comme vu sur le serveur ──────────
                    markLoandAsViewed(client, idLoand, idUser);
                }

                // ── 6. Afficher la notification ──────────────────────────────
                showNotification(notifTitle, notifMessage, "loand", "");

            } catch (Exception e) {
                Log.e(TAG, "Erreur syncUnreadLoands : " + e.getMessage(), e);
            }
        }).start();
    }

    // ================================================================
    // MARQUER UN LOAND COMME VU
    // ================================================================

    private void markLoandAsViewed(OkHttpClient client, String idLoand, String idUser) {
        try {
            RequestBody body = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("idLoand", idLoand)
                    .addFormDataPart("idUser",  idUser)
                    .build();

            Request request = new Request.Builder()
                    .url(Server.getUrlApi(getApplicationContext()) + "mark_loand_viewed.php")
                    .post(body)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                Log.d(TAG, "Loand " + idLoand + " marqué comme vu : " + response.code());
            }
        } catch (IOException e) {
            Log.e(TAG, "Erreur markLoandAsViewed : " + e.getMessage());
        }
    }

    // ================================================================
    // AFFICHAGE DE LA NOTIFICATION
    // ================================================================

    private void showNotification(String title, String message,
                                  String type, String extraData) {

        Intent intent = buildIntentByType(type, extraData);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                (int) System.currentTimeMillis(),
                intent,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setSmallIcon(R.drawable.eduniger)
                        .setContentTitle(title)
                        .setContentText(message)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                        .setAutoCancel(true)
                        .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                        .setContentIntent(pendingIntent)
                        .setPriority(NotificationCompat.PRIORITY_HIGH);

        NotificationManagerCompat notificationManager =
                NotificationManagerCompat.from(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                Log.w(TAG, "Permission POST_NOTIFICATIONS non accordée");
                return;
            }
        }

        // ================================================================
        // SAUVEGARDE EN BASE LOCALE + MISE À JOUR DU BADGE
        // ================================================================
        try {
            Session session = new Session(getApplicationContext());
            String idNumber = session.getIdNumber();

            if (idNumber != null && !idNumber.isEmpty() && !"null".equals(idNumber)) {
                String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                        .format(new Date());

                NotificationTable notifTable = new NotificationTable(getApplicationContext());
                notifTable.insert(idNumber, title, date, message, null, extraData, type);
                Log.d(TAG, "Notification sauvegardée en local");

                // Compter toutes les notifications et envoyer le broadcast
                int badgeCount = countNotifications(notifTable, idNumber);
                sendBadgeBroadcast(badgeCount);
            }
        } catch (Exception e) {
            Log.e(TAG, "Erreur sauvegarde notification locale : " + e.getMessage());
        }
        // ================================================================

        notificationManager.notify((int) System.currentTimeMillis(), builder.build());
    }

    // ================================================================
    // COMPTER LES NOTIFICATIONS EN BASE LOCALE
    // ================================================================

    private int countNotifications(NotificationTable table, String idNumber) {
        try {
            Cursor cursor = table.getData(idNumber);
            int count = (cursor != null) ? cursor.getCount() : 0;
            if (cursor != null) cursor.close();
            return count;
        } catch (Exception e) {
            Log.e(TAG, "Erreur comptage notifications : " + e.getMessage());
            return 0;
        }
    }

    // ================================================================
    // ENVOI DU BROADCAST POUR METTRE À JOUR LE BADGE
    // ================================================================

    private void sendBadgeBroadcast(int count) {
        Intent badgeIntent = new Intent(ACTION_UPDATE_BADGE);
        badgeIntent.putExtra("number", count);
        sendBroadcast(badgeIntent);
        Log.d(TAG, "Broadcast badge envoyé : count=" + count);
    }

    // ================================================================
    // ROUTING PAR TYPE
    // ================================================================

    private Intent buildIntentByType(String type, String extraData) {
        Intent intent;
        switch (type) {
            case "2":
                intent = new Intent(this, BookActivity.class);
                intent.putExtra("intent_adapter_book_id", extraData);
                break;
            case "loand":
                intent = new Intent(this, ContainerActivity.class);
                intent.putExtra("id", 3);
                break;
            default:
                intent = new Intent(this, NotificationActivity.class);
                break;
        }
        return intent;
    }

    // ================================================================
    // CRÉATION DU CANAL (Android 8+)
    // ================================================================

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "EduNiger Notifications",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Notifications de l'application EduNiger");
            channel.enableVibration(true);
            channel.setShowBadge(true);

            NotificationManager manager =
                    (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    // ================================================================
    // ENVOI DU TOKEN AU SERVEUR
    // ================================================================

    private void sendTokenToServer(String idNumber, String token) {
        new Thread(() -> {
            try {
                OkHttpClient client = new OkHttpClient();

                RequestBody body = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("idNumber", idNumber)
                        .addFormDataPart("fcmToken", token)
                        .build();

                Request request = new Request.Builder()
                        .url(Server.getUrlApi(getApplicationContext()) + "update_token.php")
                        .post(body)
                        .build();

                try (Response response = client.newCall(request).execute()) {
                    Log.d(TAG, "Token mis à jour sur le serveur : " + response.code());
                }

            } catch (IOException e) {
                Log.e(TAG, "Erreur réseau lors de la mise à jour du token : " + e.getMessage());
            } catch (Exception e) {
                Log.e(TAG, "Erreur inattendue : " + e.getMessage());
            }
        }).start();
    }
}