package com.ninotech.fabi.model.worker;

import static androidx.core.content.ContextCompat.getSystemService;

import android.Manifest;
import android.app.Activity;
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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.ninotech.fabi.R;
import com.ninotech.fabi.controleur.activity.NotificationActivity;
import com.ninotech.fabi.model.data.NotifNumber;
import com.ninotech.fabi.model.data.Notification;
import com.ninotech.fabi.model.data.Server;
import com.ninotech.fabi.model.table.NotificationTable;
import com.ninotech.fabi.model.table.Session;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.util.Objects;

import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class NetworkCheckWorker extends Worker {

    public NetworkCheckWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkRequest networkRequest = new NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build();

        connectivityManager.registerNetworkCallback(networkRequest, new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(@NonNull Network network) {
                super.onAvailable(network);
                mSession = new Session(getApplicationContext());
                try {
                    Log.e("OK WORKER",mSession.getIdNumber());
                   // Toast.makeText(getApplicationContext(), "OK WORKER", Toast.LENGTH_SHORT).show();
                    NotificationSyn notificationSyn = new NotificationSyn();
                    notificationSyn.execute(Server.getIpServerAndroid(getApplicationContext()) + "NotificationSyn.php",mSession.getIdNumber());
//                    Intent telesafeServiceIntent = new Intent(getApplicationContext(), TeleSafeService.class);
//                    getApplicationContext().startService(telesafeServiceIntent);
                }catch (Exception e)
                {
                    Log.e("err", Objects.requireNonNull(e.getMessage()));
                }
            }

            @Override
            public void onLost(@NonNull Network network) {
                super.onLost(network);
                // Optionnel : Affiche un message lorsque la connexion est perdue
            }
        });

        return Result.success();
    }
    private class NotificationSyn extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {

            try {
                OkHttpClient client = new OkHttpClient();
                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("idNumber", params[1])
                        .build();
                Request request = new Request.Builder()
                        .url(params[0])
                        .post(requestBody)
                        .build();
                try {
                    Response response = client.newCall(request).execute();
                    assert response.body() != null;
                    return response.body().string();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String response) {
           Log.e("JsonTest",response);
            Toast.makeText(getApplicationContext(), response, Toast.LENGTH_SHORT).show();
            if(!response.equals("ras"))
            {
                JSONArray jsonArray = null;
                try {
                    jsonArray = new JSONArray(response);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
                int i2=0;
                for(int i=0 ; i<jsonArray.length() ; i++)
                {
                    Notification notification = new Notification();
                    NotificationTable notificationTable = new NotificationTable(getApplicationContext());
                    try {
                        notification.setType(jsonArray.getJSONObject(i).getString("type"));
                        notification.setMessage(jsonArray.getJSONObject(i).getString("message"));
                        notification.setDate(jsonArray.getJSONObject(i).getString("2"));
                        notification.setTitle("Inormation");
//                        if(notification.getType().equals("1"))
//                        {
//                            notification.setTitle("Alerte TELESAFE");
//                            notification.setLatitude(jsonArray.getJSONObject(i).getString("latitude"));
//                            notification.setLongitude(jsonArray.getJSONObject(i).getString("longitude"));
//                        }
                        notificationTable.insert(mSession.getIdNumber(),
                                notification.getTitle(),
                                notification.getDate(),
                                notification.getMessage(),
                                notification.getType());
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                    int numberNotification = NotifNumber.getLastKnownLocation(getApplicationContext())+1;
                    NotifNumber.saveLocation(getApplicationContext(),numberNotification);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        NotificationChannel channel = new NotificationChannel(
                                "channel_id",
                                "Nom du canal",
                                NotificationManager.IMPORTANCE_DEFAULT
                        );
                        NotificationManager notificationManager = getApplicationContext().getSystemService(NotificationManager.class);
                        notificationManager.createNotificationChannel(channel);
                    }

// Demande de permission pour Android 13 et plus
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions((Activity) getApplicationContext(), new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);
                            return;
                        }
                    }

                    Intent intent = new Intent(getApplicationContext(), NotificationActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    PendingIntent pendingIntent = PendingIntent.getActivity(
                            getApplicationContext(),
                            0,
                            intent,
                            PendingIntent.FLAG_IMMUTABLE
                    );

                    NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), "channel_id")
                            .setSmallIcon(R.mipmap.ic_v2)
                            .setContentTitle(notification.getTitle())
                            .setContentText(notification.getMessage())
                            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                            .setContentIntent(pendingIntent)
                            .setAutoCancel(true);

                    NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());

// Afficher la notification
                    notificationManager.notify(numberNotification, builder.build());

                    Intent updateBadgeIntent = new Intent("ACTION_UPDATE_NOTIFICATION_BADGE");
                    updateBadgeIntent.putExtra("number",numberNotification);
                    getApplicationContext().sendBroadcast(updateBadgeIntent);
                }
            }
        }
    }
    private Session mSession;
}