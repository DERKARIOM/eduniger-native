package com.fabi.Service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.fabi.R;
import com.fabi.Controleur.NotificationActivity;
import com.fabi.Model.NotificationTable;
import com.fabi.Model.Session;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;

import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class NotificationService extends Service {
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mSession = new Session(this);
        mNotificationTable = new NotificationTable(this);
        try {
            mMatricule = mSession.getMatricule();
        }catch (Exception e)
        {
            mMatricule = "0";
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        final int delay = 5000; // 5 secondes
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable(){
            public void run(){
                ReservationService reservationService = new ReservationService();
                reservationService.execute("http://192.168.43.1:2222/fabi/android/reservationService.php");
                handler.postDelayed(this, delay);
            }
        }, delay);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // Nettoyage de votre service
    }

    private class ReservationService extends AsyncTask<String,Void,String> {
        @Override
        protected String doInBackground(String... params) {

            try {
                OkHttpClient client = new OkHttpClient();
                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("matricule",mMatricule)
                        .build();
                Request request = new Request.Builder()
                        .url(params[0])
                        .post(requestBody)
                        .build();
                try {
                    Response response = client.newCall(request).execute();
                    return response.body().string();
                }catch (IOException e)
                {
                    e.printStackTrace();
                }

            }catch (Exception e)
            {
                Toast.makeText(NotificationService.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
            return null;
        }
        @Override
        protected void onPostExecute(String response){
            if(response != null)
            {
                Log.e("Kader",response);
                JSONArray jsonArray = null;
                try {
                    jsonArray = new JSONArray(response);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
                int i2=0;
                for(int i=0 ; i<jsonArray.length() ; i++)
                {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        NotificationChannel channel = new NotificationChannel("channel_id", "Nom du canal", NotificationManager.IMPORTANCE_DEFAULT);
                        NotificationManager notificationManager = getSystemService(NotificationManager.class);
                        notificationManager.createNotificationChannel(channel);
                    }
                    String message;
                    try {
                        if(jsonArray.getJSONObject(i).getString("etat").equals("1"))
                            message = "Votre réservation du livre " + jsonArray.getJSONObject(i).getString("titreLivre") + " a été traitée avec succès. Vous pouvez maintenant passer le récupérer. Merci de choisir notre service!\"";
                        else
                            message = "Nous regrettons de vous informer que votre réservation pour " + jsonArray.getJSONObject(i).getString("titreLivre") + " a été rejetée. Si vous avez des questions ou besoin d'assistance, n'hésitez pas à nous contacter. Merci de votre compréhension.";
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                    try {
                        mNotificationTable.insert(mMatricule,"Traitement de Réservation",message,"10:00");
                    }catch (Exception e)
                    {
                        Log.e("ErrInsertNotification",e.getMessage());
                    }
                    try {
                        Intent intent = new Intent(getApplicationContext(), NotificationActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);
                        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), "channel_id")
                                .setSmallIcon(R.drawable.item)
                                .setContentTitle("Traitement de Réservation")
                                .setContentText(message)
                                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                                .setContentIntent(pendingIntent)
                                .setAutoCancel(true);
                        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());
                        notificationManager.notify(i, builder.build());
                    }catch (Exception e){
                        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), "channel_id")
                                .setSmallIcon(R.drawable.scol)
                                .setContentTitle("Traitement de Réservation")
                                .setContentText(message)
                                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
                        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());
                        notificationManager.notify(i2, builder.build());
                    }
                    Intent intent3 = new Intent("ACTION_UPDATE_BADGE");
                    intent3.putExtra("notificationCount", i2+1); // Nombre de nouvelles notifications à afficher
                    i2++;
                    sendBroadcast(intent3);
                }

            }
        }
    }
    private Session mSession;
    private String mMatricule;
    private NotificationTable mNotificationTable;
}