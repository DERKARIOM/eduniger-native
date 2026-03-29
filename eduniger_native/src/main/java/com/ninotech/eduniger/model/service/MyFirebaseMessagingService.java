package com.ninotech.eduniger.model.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.RingtoneManager;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.ninotech.eduniger.R;
import com.ninotech.eduniger.controleur.activity.BookActivity;
import com.ninotech.eduniger.controleur.activity.MainActivity;
import com.ninotech.eduniger.model.data.Server;
import com.ninotech.eduniger.model.table.Session;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.io.IOException;

import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG        = "FCMService";
    private static final String CHANNEL_ID = "eduniger_channel";

    // ================================================================
    // TOKEN RENOUVELÉ PAR FIREBASE
    // ================================================================

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.d(TAG, "Token FCM renouvelé : " + token);

        // Mettre à jour le token sur le serveur si l'utilisateur est connecté
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

        // Récupération du titre et message
        String title   = "";
        String message = "";

        if (remoteMessage.getNotification() != null) {
            title   = remoteMessage.getNotification().getTitle() != null
                    ? remoteMessage.getNotification().getTitle() : "";
            message = remoteMessage.getNotification().getBody() != null
                    ? remoteMessage.getNotification().getBody() : "";
        }

        // Récupération des données custom
        String type      = remoteMessage.getData().get("type")      != null
                ? remoteMessage.getData().get("type")      : "";
        String extraData = remoteMessage.getData().get("extraData") != null
                ? remoteMessage.getData().get("extraData") : "";

        Log.d(TAG, "Notification reçue — type: " + type + " | extraData: " + extraData);

        // Créer le canal (Android 8+)
        createNotificationChannel();

        // Afficher la notification
        showNotification(title, message, type, extraData);
    }

    // ================================================================
    // AFFICHAGE DE LA NOTIFICATION
    // ================================================================

    private void showNotification(String title, String message,
                                  String type, String extraData) {

        // Intent selon le type
        Intent intent = buildIntentByType(type, extraData);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                (int) System.currentTimeMillis(), // requestCode unique
                intent,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setSmallIcon(R.drawable.img_default_book)
                        .setContentTitle(title)
                        .setContentText(message)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                        .setAutoCancel(true)
                        .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                        .setContentIntent(pendingIntent)
                        .setPriority(NotificationCompat.PRIORITY_HIGH);

        NotificationManagerCompat notificationManager =
                NotificationManagerCompat.from(this);

        // Vérification permission POST_NOTIFICATIONS (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this,
                    android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                Log.w(TAG, "Permission POST_NOTIFICATIONS non accordée");
                return;
            }
        }

        // ID unique pour chaque notification
        notificationManager.notify((int) System.currentTimeMillis(), builder.build());
    }

    // ================================================================
    // ROUTING PAR TYPE
    // ================================================================

    private Intent buildIntentByType(String type, String extraData) {
        Intent intent;

        switch (type) {
            case "reservation":
            case "book":
                // Ouvrir directement le livre concerné
                intent = new Intent(this, BookActivity.class);
                intent.putExtra("intent_adapter_book_id", extraData);
                break;

            default:
                // Ouvrir l'accueil par défaut
                intent = new Intent(this, MainActivity.class);
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