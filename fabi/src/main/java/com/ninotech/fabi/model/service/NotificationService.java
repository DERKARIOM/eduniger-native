//package com.ninotech.fabi.model.service;
//
//import android.app.NotificationChannel;
//import android.app.NotificationManager;
//import android.app.PendingIntent;
//import android.app.Service;
//import android.content.Intent;
//import android.os.AsyncTask;
//import android.os.Build;
//import android.os.Handler;
//import android.os.IBinder;
//import android.util.Log;
//import android.widget.Toast;
//
//import androidx.core.app.NotificationCompat;
//import androidx.core.app.NotificationManagerCompat;
//
//import com.ninotech.fabi.R;
//import com.ninotech.fabi.controleur.activity.NotificationActivity;
//import com.ninotech.fabi.model.data.AudioDownloader;
//import com.ninotech.fabi.model.data.DownloadFile;
//import com.ninotech.fabi.model.data.Server;
//import com.ninotech.fabi.model.table.LoandTable;
//import com.ninotech.fabi.model.table.NotificationTable;
//import com.ninotech.fabi.model.table.Session;
//
//import org.json.JSONArray;
//import org.json.JSONException;
//
//import java.io.IOException;
//
//import okhttp3.MultipartBody;
//import okhttp3.OkHttpClient;
//import okhttp3.Request;
//import okhttp3.RequestBody;
//import okhttp3.Response;
//
//public class NotificationService extends Service {
//    @Override
//    public IBinder onBind(Intent intent) {
//        return null;
//    }
//
//    @Override
//    public void onCreate() {
//        super.onCreate();
//        Session session = new Session(this);
//        mNotificationTable = new NotificationTable(this);
//        mLoandTable = new LoandTable(this);
//        try {
//            mIdNumber = session.getIdNumber();
//        }catch (Exception e)
//        {
//            mIdNumber = "0";
//            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
//        }
//
//    }
//
//    @Override
//    public int onStartCommand(Intent intent, int flags, int startId) {
//        final int delay = 5000; // 5 secondes
//        final Handler handler = new Handler();
//        handler.postDelayed(new Runnable(){
//            public void run(){
//                ReservationService reservationService = new ReservationService();
//                NotifService notifService = new NotifService();
//                LoandService loandService = new LoandService();
//                LoandClosingService loandClosingService = new LoandClosingService();
//                notifService.execute(Server.getIpServerAndroid(getApplicationContext()) + "NotifService.php",mIdNumber);
//                reservationService.execute(Server.getIpServerAndroid(getApplicationContext()) + "ReservationService.php",mIdNumber);
//                loandService.execute(Server.getIpServerAndroid(getApplicationContext()) + "LoandSyn.php",mIdNumber);
//                loandClosingService.execute(Server.getIpServerAndroid(getApplicationContext()) + "LoandClosing.php",mIdNumber);
//                handler.postDelayed(this, delay);
//            }
//        }, delay);
//        return START_STICKY;
//    }
//
//    @Override
//    public void onDestroy() {
//        super.onDestroy();
//
//        // Nettoyage de votre service
//    }
//
//    private class ReservationService extends AsyncTask<String,Void,String> {
//        @Override
//        protected String doInBackground(String... params) {
//
//            try {
//                OkHttpClient client = new OkHttpClient();
//                RequestBody requestBody = new MultipartBody.Builder()
//                        .setType(MultipartBody.FORM)
//                        .addFormDataPart("idNumber", params[1])
//                        .build();
//                Request request = new Request.Builder()
//                        .url(params[0])
//                        .post(requestBody)
//                        .build();
//                try {
//                    Response response = client.newCall(request).execute();
//                    assert response.body() != null;
//                    return response.body().string();
//                }catch (IOException e)
//                {
//                    e.printStackTrace();
//                }
//
//            }catch (Exception e)
//            {
//                Toast.makeText(NotificationService.this, e.getMessage(), Toast.LENGTH_SHORT).show();
//            }
//            return null;
//        }
//        @Override
//        protected void onPostExecute(String response){
//            if(response != null)
//            {
//                JSONArray jsonArray = null;
//                try {
//                    jsonArray = new JSONArray(response);
//                } catch (JSONException e) {
//                    throw new RuntimeException(e);
//                }
//                int i2=0;
//                for(int i=0 ; i<jsonArray.length() ; i++)
//                {
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                        NotificationChannel channel = new NotificationChannel("channel_id", "Nom du canal", NotificationManager.IMPORTANCE_DEFAULT);
//                        NotificationManager notificationManager = getSystemService(NotificationManager.class);
//                        notificationManager.createNotificationChannel(channel);
//                    }
//                    String message;
//                    try {
//                        if(jsonArray.getJSONObject(i).getString("state").equals("1"))
//                            message = "Votre réservation du livre " + jsonArray.getJSONObject(i).getString("title") + " a été traitée avec succès. Vous pouvez maintenant passer le récupérer. Merci de choisir notre service!\"";
//                        else
//                            message = "Nous regrettons de vous informer que votre réservation pour " + jsonArray.getJSONObject(i).getString("title") + " a été rejetée. Si vous avez des questions ou besoin d'assistance, n'hésitez pas à nous contacter. Merci de votre compréhension.";
//                    } catch (JSONException e) {
//                        throw new RuntimeException(e);
//                    }
//                    try {
//                        mNotificationTable.insert(mIdNumber,"Traitement de Réservation",message,"10:00");
//                    }catch (Exception e)
//                    {
//                        Log.e("ErrInsertNotification",e.getMessage());
//                    }
//                    try {
//                        Intent intent = new Intent(getApplicationContext(), NotificationActivity.class);
//                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);
//                        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), "channel_id")
//                                .setSmallIcon(R.drawable.img_default_book)
//                                .setContentTitle("Traitement de Réservation")
//                                .setContentText(message)
//                                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
//                                .setContentIntent(pendingIntent)
//                                .setAutoCancel(true);
//                        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());
//                        notificationManager.notify(i, builder.build());
//                    }catch (Exception e){
//                        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), "channel_id")
//                                .setSmallIcon(R.drawable.img_default_book)
//                                .setContentTitle("Traitement de Réservation")
//                                .setContentText(message)
//                                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
//                        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());
//                        notificationManager.notify(i2, builder.build());
//                    }
//                    Intent intent3 = new Intent("ACTION_UPDATE_BADGE");
//                    intent3.putExtra("notificationCount", i2+1); // Nombre de nouvelles notifications à afficher
//                    i2++;
//                    sendBroadcast(intent3);
//                }
//
//            }
//        }
//    }
//
//    private class NotifService extends AsyncTask<String,Void,String> {
//        @Override
//        protected String doInBackground(String... params) {
//
//            try {
//                OkHttpClient client = new OkHttpClient();
//                RequestBody requestBody = new MultipartBody.Builder()
//                        .setType(MultipartBody.FORM)
//                        .addFormDataPart("idNumber", params[1])
//                        .build();
//                Request request = new Request.Builder()
//                        .url(params[0])
//                        .post(requestBody)
//                        .build();
//                try {
//                    Response response = client.newCall(request).execute();
//                    assert response.body() != null;
//                    return response.body().string();
//                }catch (IOException e)
//                {
//                    e.printStackTrace();
//                }
//
//            }catch (Exception e)
//            {
//                Toast.makeText(NotificationService.this, e.getMessage(), Toast.LENGTH_SHORT).show();
//            }
//            return null;
//        }
//        @Override
//        protected void onPostExecute(String response){
//            if(response != null)
//            {
//                JSONArray jsonArray = null;
//                try {
//                    jsonArray = new JSONArray(response);
//                } catch (JSONException e) {
//                    throw new RuntimeException(e);
//                }
//                int i2=0;
//                for(int i=0 ; i<jsonArray.length() ; i++)
//                {
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                        NotificationChannel channel = new NotificationChannel("channel_id", "Nom du canal", NotificationManager.IMPORTANCE_DEFAULT);
//                        NotificationManager notificationManager = getSystemService(NotificationManager.class);
//                        notificationManager.createNotificationChannel(channel);
//                    }
//                    String message = null;
//                    try {
//                        switch (jsonArray.getJSONObject(i).getString("type"))
//                        {
//                            case "1":
//                                message = "Une version audio du livre \"" + jsonArray.getJSONObject(i).getString("title") + "\" est désormais disponible";
//                                break;
//                            case "2":
//                                message = "Une version electronique du livre \"" + jsonArray.getJSONObject(i).getString("title") + "\" est désormais disponible";
//                                break;
//                            case "3":
//                                message = "Une version physique du livre \"" + jsonArray.getJSONObject(i).getString("title") + "\" est désormais disponible";
//                                break;
//                            default:
//                                break;
//                        }
//                    } catch (JSONException e) {
//                        throw new RuntimeException(e);
//                    }
//                    try {
//                        mNotificationTable.insert(mIdNumber,"Nouvelles version",message,"10:00");
//                    }catch (Exception e)
//                    {
//                        Log.e("ErrInsertNotification",e.getMessage());
//                    }
//                    try {
//                        Intent intent = new Intent(getApplicationContext(), NotificationActivity.class);
//                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);
//                        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), "channel_id")
//                                .setSmallIcon(R.drawable.img_default_book)
//                                .setContentTitle("Nouvelles version")
//                                .setContentText(message)
//                                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
//                                .setContentIntent(pendingIntent)
//                                .setAutoCancel(true);
//                        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());
//                        notificationManager.notify(i, builder.build());
//                    }catch (Exception e){
//                        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), "channel_id")
//                                .setSmallIcon(R.drawable.img_default_book)
//                                .setContentTitle("Nouvelles version")
//                                .setContentText(message)
//                                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
//                        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());
//                        notificationManager.notify(i2, builder.build());
//                    }
//                    Intent intent3 = new Intent("ACTION_UPDATE_BADGE");
//                    intent3.putExtra("notificationCount", i2+1); // Nombre de nouvelles notifications à afficher
//                    i2++;
//                    sendBroadcast(intent3);
//                }
//
//            }
//        }
//    }
//
//    private class LoandService extends AsyncTask<String,Void,String> {
//        @Override
//        protected String doInBackground(String... params) {
//
//            try {
//                OkHttpClient client = new OkHttpClient();
//                RequestBody requestBody = new MultipartBody.Builder()
//                        .setType(MultipartBody.FORM)
//                        .addFormDataPart("idNumber", params[1])
//                        .build();
//                Request request = new Request.Builder()
//                        .url(params[0])
//                        .post(requestBody)
//                        .build();
//                try {
//                    Response response = client.newCall(request).execute();
//                    assert response.body() != null;
//                    return response.body().string();
//                }catch (IOException e)
//                {
//                    e.printStackTrace();
//                }
//
//            }catch (Exception e)
//            {
//                Toast.makeText(NotificationService.this, e.getMessage(), Toast.LENGTH_SHORT).show();
//            }
//            return null;
//        }
//        @Override
//        protected void onPostExecute(String response){
//            if(response != null)
//            {
//                JSONArray jsonArray = null;
//                try {
//                    jsonArray = new JSONArray(response);
//                } catch (JSONException e) {
//                    throw new RuntimeException(e);
//                }
//                int i2=0;
//                for(int i=0 ; i<jsonArray.length() ; i++)
//                {
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                        NotificationChannel channel = new NotificationChannel("channel_id", "Nom du canal", NotificationManager.IMPORTANCE_DEFAULT);
//                        NotificationManager notificationManager = getSystemService(NotificationManager.class);
//                        notificationManager.createNotificationChannel(channel);
//                    }
//                    String message=null;
//                    try {
//                        message = "Nous tenons à vous rappeler que le livre " + jsonArray.getJSONObject(i).getString("title" ) + " que vous avez emprunté doit être retourné dans le(s) " + jsonArray.getJSONObject(i).getString("deliveryDate") + " prochains jours, conformément à nos conditions de prêt";
//                        mNotificationTable.insert(mIdNumber,"Nouvelles version",message,"10:00");
//                        DownloadFile downloadFile = new DownloadFile(getApplicationContext());
//                        //String coverPath = downloadFile.start(getString(R.string.ip_server) + "ressources/cover/" + jsonArray.getJSONObject(i).getString("blanket"),jsonArray.getJSONObject(i).getString("blanket"));
//                        //mLoandTable.insert(jsonArray.getJSONObject(i).getString("idLoand"),jsonArray.getJSONObject(i).getString("idNumber"),coverPath,jsonArray.getJSONObject(i).getString("title"),jsonArray.getJSONObject(i).getString("dateLoand"),jsonArray.getJSONObject(i).getString("realReturnDate"));
//                    } catch (JSONException e) {
//                        throw new RuntimeException(e);
//                    }
//                    try {
//                        Intent intent = new Intent(getApplicationContext(), NotificationActivity.class);
//                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);
//                        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), "channel_id")
//                                .setSmallIcon(R.drawable.img_default_book)
//                                .setContentTitle("Rappeler d' Emprunter")
//                                .setContentText(message)
//                                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
//                                .setContentIntent(pendingIntent)
//                                .setAutoCancel(true);
//                        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());
//                        notificationManager.notify(i, builder.build());
//                    }catch (Exception e){
//                        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), "channel_id")
//                                .setSmallIcon(R.drawable.img_default_book)
//                                .setContentTitle("Rappeler d' Emprunter")
//                                .setContentText(message)
//                                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
//                        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());
//                        notificationManager.notify(i2, builder.build());
//                    }
//                    Intent intent3 = new Intent("ACTION_UPDATE_BADGE");
//                    intent3.putExtra("notificationCount", i2+1); // Nombre de nouvelles notifications à afficher
//                    i2++;
//                    sendBroadcast(intent3);
//                }
//
//            }
//        }
//    }
//
//    private class LoandClosingService extends AsyncTask<String,Void,String> {
//        @Override
//        protected String doInBackground(String... params) {
//
//            try {
//                OkHttpClient client = new OkHttpClient();
//                RequestBody requestBody = new MultipartBody.Builder()
//                        .setType(MultipartBody.FORM)
//                        .addFormDataPart("idNumber", params[1])
//                        .build();
//                Request request = new Request.Builder()
//                        .url(params[0])
//                        .post(requestBody)
//                        .build();
//                try {
//                    Response response = client.newCall(request).execute();
//                    assert response.body() != null;
//                    return response.body().string();
//                }catch (IOException e)
//                {
//                    e.printStackTrace();
//                }
//
//            }catch (Exception e)
//            {
//                Toast.makeText(NotificationService.this, e.getMessage(), Toast.LENGTH_SHORT).show();
//            }
//            return null;
//        }
//        @Override
//        protected void onPostExecute(String response){
//            if(response != null)
//            {
//                JSONArray jsonArray = null;
//                try {
//                    jsonArray = new JSONArray(response);
//                } catch (JSONException e) {
//                    throw new RuntimeException(e);
//                }
//                int i2=0;
//                for(int i=0 ; i<jsonArray.length() ; i++)
//                {
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                        NotificationChannel channel = new NotificationChannel("channel_id", "Nom du canal", NotificationManager.IMPORTANCE_DEFAULT);
//                        NotificationManager notificationManager = getSystemService(NotificationManager.class);
//                        notificationManager.createNotificationChannel(channel);
//                    }
//                    String message=null;
//                    try {
//                        message = "Nous espérons que vous avez apprécié votre emprunt du livre " + jsonArray.getJSONObject(i).getString("title" ) + " de notre bibliothèque! Nous apprécions votre soutien continu et nous espérons que le livre a répondu à vos attentes. N'hésitez pas à nous faire part de vos commentaires ou suggestions pour améliorer notre service. Nous sommes impatients de vous accueillir à nouveau à la bibliothèque bientôt!";
//                        mNotificationTable.insert(mIdNumber,"Fermeture  de l' emprunt",message,"10:00");
//                        mLoandTable.remove(jsonArray.getJSONObject(i).getString("idLoand"));
//                    } catch (JSONException e) {
//                        throw new RuntimeException(e);
//                    }
//                    try {
//                        Intent intent = new Intent(getApplicationContext(), NotificationActivity.class);
//                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);
//                        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), "channel_id")
//                                .setSmallIcon(R.drawable.img_default_book)
//                                .setContentTitle("Fermeture  de l' emprunt")
//                                .setContentText(message)
//                                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
//                                .setContentIntent(pendingIntent)
//                                .setAutoCancel(true);
//                        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());
//                        notificationManager.notify(i, builder.build());
//                    }catch (Exception e){
//                        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), "channel_id")
//                                .setSmallIcon(R.drawable.img_default_book)
//                                .setContentTitle("Fermeture  de l' emprunt")
//                                .setContentText(message)
//                                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
//                        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());
//                        notificationManager.notify(i2, builder.build());
//                    }
//                    Intent intent3 = new Intent("ACTION_UPDATE_BADGE");
//                    intent3.putExtra("notificationCount", i2+1); // Nombre de nouvelles notifications à afficher
//                    i2++;
//                    sendBroadcast(intent3);
//                }
//
//            }
//        }
//    }
//
//    private String mIdNumber;
//    private NotificationTable mNotificationTable;
//    private LoandTable mLoandTable;
//}