package com.ninotech.eduniger.model.worker;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.ninotech.eduniger.R;
import com.ninotech.eduniger.controleur.activity.NotificationActivity;
import com.ninotech.eduniger.model.data.NotifNumber;
import com.ninotech.eduniger.model.data.Notification;
import com.ninotech.eduniger.model.data.Server;
import com.ninotech.eduniger.model.table.NotificationTable;
import com.ninotech.eduniger.model.table.Session;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class NetworkCheckWorker extends Worker {

    private static final String TAG = "NetworkCheckWorker";
    private static final String CHANNEL_ID = "fabi_notification_channel";
    private static final String CHANNEL_NAME = "Notifications Fabi";
    private static final String RESPONSE_RAS = "ras";
    private static final String ACTION_UPDATE_BADGE = "ACTION_UPDATE_NOTIFICATION_BADGE";

    private Session mSession;
    private OkHttpClient mHttpClient;

    public NetworkCheckWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        mHttpClient = new OkHttpClient();
        mSession = new Session(context);
    }

    @NonNull
    @Override
    public Result doWork() {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager == null) {
            Log.e(TAG, "ConnectivityManager is null");
            return Result.failure();
        }

        NetworkRequest networkRequest = new NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build();

        connectivityManager.registerNetworkCallback(networkRequest, new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(@NonNull Network network) {
                super.onAvailable(network);
                handleNetworkAvailable();
            }

            @Override
            public void onLost(@NonNull Network network) {
                super.onLost(network);
                Log.d(TAG, "Network connection lost");
            }
        });

        return Result.success();
    }

    private void handleNetworkAvailable() {
        try {
            Log.d(TAG, "Network available, checking notifications for user: " + mSession.getIdNumber());
            new NotificationSyn().execute(
                    Server.getUrlApi(getApplicationContext()) + "NotificationSyn.php",
                    mSession.getIdNumber()
            );
        } catch (Exception e) {
            Log.e(TAG, "Error handling network availability", e);
        }
    }

    // ==================== AsyncTask ====================

    private class NotificationSyn extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            return executePostRequest(params[0], params[1]);
        }

        @Override
        protected void onPostExecute(String response) {
            if (response != null && !RESPONSE_RAS.equals(response)) {
                processNotifications(response);
            } else {
                Log.d(TAG, "No new notifications");
            }
        }

        private void processNotifications(String response) {
            try {
                JSONArray jsonArray = new JSONArray(response);

                for (int i = 0; i < jsonArray.length(); i++) {
                    processNotification(jsonArray.getJSONObject(i));
                }
            } catch (JSONException e) {
                Log.e(TAG, "Error parsing notifications", e);
            }
        }

        private void processNotification(JSONObject jsonObject) {
            try {
                Notification notification = parseNotification(jsonObject);
                saveNotification(notification);
                showNotification(notification);
                updateNotificationBadge();
            } catch (JSONException e) {
                Log.e(TAG, "Error processing notification", e);
            }
        }

        private Notification parseNotification(JSONObject jsonObject) throws JSONException {
            Notification notification = new Notification();
            notification.setType(jsonObject.getString("type"));
            notification.setMessage(jsonObject.getString("message"));
            notification.setLink(jsonObject.getString("link"));
            notification.setIdLink(jsonObject.getString("idBookLink"));
            notification.setDate(jsonObject.getString("2"));
            notification.setTitle(jsonObject.getString("title"));
            return notification;
        }

        private void saveNotification(Notification notification) {
            NotificationTable notificationTable = new NotificationTable(getApplicationContext());
            notificationTable.insert(
                    mSession.getIdNumber(),
                    notification.getTitle(),
                    notification.getDate(),
                    notification.getMessage(),
                    notification.getLink(),
                    notification.getIdLink(),
                    notification.getType()
            );
        }

        private void showNotification(Notification notification) {
            createNotificationChannel();

            if (!hasNotificationPermission()) {
                Log.w(TAG, "Notification permission not granted");
                return;
            }

            int notificationId = getNextNotificationId();
            android.app.Notification androidNotification = buildNotification(notification);

            NotificationManagerCompat notificationManager =
                    NotificationManagerCompat.from(getApplicationContext());
            if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            notificationManager.notify(notificationId, androidNotification);
        }

        private void createNotificationChannel() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel(
                        CHANNEL_ID,
                        CHANNEL_NAME,
                        NotificationManager.IMPORTANCE_DEFAULT
                );
                channel.setDescription("Notifications de l'application Fabi");

                NotificationManager notificationManager =
                        getApplicationContext().getSystemService(NotificationManager.class);
                if (notificationManager != null) {
                    notificationManager.createNotificationChannel(channel);
                }
            }
        }

        private boolean hasNotificationPermission() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                return ActivityCompat.checkSelfPermission(
                        getApplicationContext(),
                        Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED;
            }
            return true;
        }

        private int getNextNotificationId() {
            int currentCount = NotifNumber.getLastKnownLocation(getApplicationContext());
            int nextId = currentCount + 1;
            NotifNumber.saveLocation(getApplicationContext(), nextId);
            return nextId;
        }

        private android.app.Notification buildNotification(Notification notification) {
            Intent intent = new Intent(getApplicationContext(), NotificationActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            PendingIntent pendingIntent = PendingIntent.getActivity(
                    getApplicationContext(),
                    0,
                    intent,
                    PendingIntent.FLAG_IMMUTABLE
            );

            return new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                    .setSmallIcon(R.mipmap.ic_v2)
                    .setContentTitle(notification.getTitle())
                    .setContentText(notification.getMessage())
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .build();
        }

        private void updateNotificationBadge() {
            int count = NotifNumber.getLastKnownLocation(getApplicationContext());
            Intent intent = new Intent(ACTION_UPDATE_BADGE);
            intent.putExtra("number", count);
            getApplicationContext().sendBroadcast(intent);
        }
    }

    // ==================== Helper Methods ====================

    private String executePostRequest(String url, String idNumber) {
        try {
            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("idNumber", idNumber)
                    .build();

            Request request = new Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .build();

            try (Response response = mHttpClient.newCall(request).execute()) {
                if (response.body() != null) {
                    return response.body().string();
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "Network error: " + e.getMessage(), e);
        } catch (Exception e) {
            Log.e(TAG, "Unexpected error: " + e.getMessage(), e);
        }
        return null;
    }
}