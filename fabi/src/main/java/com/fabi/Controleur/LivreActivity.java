package com.fabi.Controleur;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fabi.Model.DialogReservationCusto;
import com.fabi.Model.Disscution;
import com.fabi.Model.DisscutionAdapter;
import com.fabi.Model.ElectroniqueTable;
import com.fabi.Model.Recenmment;
import com.fabi.Model.SimulaireAdapter;
import com.fabi.Model.RoundedTransformation;
import com.fabi.Model.Session;
import com.fabi.Model.Son;
import com.fabi.Model.SonAdapter;
import com.example.fabi.R;
import com.fabi.Model.SucceReservation;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LivreActivity extends AppCompatActivity {
    private static final int STORAGE_PERMISSION_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_livre);
        getSupportActionBar().hide();
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        Intent intentLivre = getIntent();
        mSession = new Session(this);
        mIdLivre = intentLivre.getStringExtra("idLivre");
        mList = new ArrayList<>();
        mList2 = new ArrayList<>();
        mListSon = new ArrayList<>();
        mRecyclerView = findViewById(R.id.recylerCommentaire);
        mRecyclerView2 = findViewById(R.id.recylerSimilaire);
        mRecyclerSon = findViewById(R.id.recylerAudio2);
        mCouverture = findViewById(R.id.couverture_livre2);
        mTitre = findViewById(R.id.title_livre2);
        mCategorie = findViewById(R.id.categorie_livre2);
        mDesc = findViewById(R.id.desc_livre);
        mBttReservation = findViewById(R.id.btt_reservation);
        mBttAudio = findViewById(R.id.btt_audio);
        mBttOpenPDF = findViewById(R.id.btt_pdf_ouvrir);
        mBttDowloadPDF = findViewById(R.id.btt_pdf);
        mMessage = findViewById(R.id.messageCommentaire);
        mAdd = findViewById(R.id.addCommentaire);
        mBlike = findViewById(R.id.like);
        mBNolike = findViewById(R.id.nolike);
        mBttLike = findViewById(R.id.ico1);
        mBttNoLike = findViewById(R.id.ico2);
        mPlay = findViewById(R.id.paly);
        mStop = findViewById(R.id.stop);
        mSeekBar = findViewById(R.id.seekbar);
        mBReservation = findViewById(R.id.bReservation);
        mBAudio = findViewById(R.id.bAudio);
        mBPDF = findViewById(R.id.bPDF);
        mDialogReservationCusto = new DialogReservationCusto(this);
        mElectroniqueTable = new ElectroniqueTable(this);
        tmp_position=0;
        mMediaPlayer = new MediaPlayer();
        isLike=false;
        isNoLike=false;
        handler = new Handler();
        mTimer = new Timer();
        BroadcastReceiver receiverNote = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if ("ACTION_AUDIO".equals(intent.getAction())) {
                    mAudio = intent.getStringExtra("nomAudio");
                    int position = intent.getIntExtra("position",0);
                    url = "http://192.168.43.1:2222/fabi/audio/" + mAudio;
                    try {
                        if(tmp_position != position)
                            mListSon.get(tmp_position).setPlaying(false);
                        mRecyclerSon.setAdapter(mSonAdapter);
                        mMediaPlayer.reset();
                        mMediaPlayer.setDataSource(url);
                        mMediaPlayer.prepare();
                        mSeekBar.setMax(mMediaPlayer.getDuration());
                        mMediaPlayer.start();
                        tmp_position = position;
                        mPlay.setImageResource(R.drawable.play);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                   Toast.makeText(context,String.valueOf(position), Toast.LENGTH_SHORT).show();


                }
            }
        };
        registerReceiver(receiverNote, new IntentFilter("ACTION_AUDIO")); /* Appel de la fonction cregisterReceviver */
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mSeekBar.setProgress(mMediaPlayer.getCurrentPosition());
                    }
                });
            }
        }, 0, 1000); // Met à jour la SeekBar chaque seconde (1000 millisecondes)

        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (b) {
                    mMediaPlayer.seekTo(i);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        mBttReservation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ReservationDialog();
            }
        });

        mBttOpenPDF.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Vérifier et demander la permission d'écriture externe si nécessaire
                if (ContextCompat.checkSelfPermission(getApplicationContext(),
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {

                    ActivityCompat.requestPermissions(LivreActivity.this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            STORAGE_PERMISSION_REQUEST_CODE);
                } else {
                    // Si la permission est déjà accordée, télécharger et ouvrir le PDF
                    downloadAndOpenPDF(mNomPdf);
                }
            }
        });

        mBttDowloadPDF.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mElectroniqueTable.insert(mSession.getMatricule(),mIdLivre,mDesc.getText().toString(),mAuteur,mNomCouverture,mNomPdf,mCategorie.getText().toString(),mTitre.getText().toString(),mNomCouvertureCat,mProfilAuteur))
                    succeDowloadPDFDialog();
            }
        });
        mPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mMediaPlayer.isPlaying())
                {
                    mPlay.setImageResource(R.drawable.plause);
                    mMediaPlayer.pause();
                }
                else
                {
                    mPlay.setImageResource(R.drawable.play);
                    mMediaPlayer.start();
                }
            }
        });

        mStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMediaPlayer.stop();
                mPlay.setImageResource(R.drawable.plause);
                mTimer.cancel(); // Arrête le Timer lors de la destruction de l'activité
               mSeekBar.setProgress(0);
            }
        });
        mBlike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!isLike)
                {
                    isLike = true;
                    mBttLike.setImageResource(R.drawable.on_like);
                }
                else
                {
                    isLike = false;
                    mBttLike.setImageResource(R.drawable.off_like);

                }
            }
        });

        mBNolike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!isNoLike)
                {
                    isNoLike = true;
                    mBttNoLike.setImageResource(R.drawable.on_nolike);
                }
                else
                {
                    isNoLike = false;
                    mBttNoLike.setImageResource(R.drawable.off_nolike);

                }
            }
        });
        Http http = new Http();
        PullCommentaire pullCommentaire = new PullCommentaire();
        Similaires similaires = new Similaires();
        PullSon pullSon = new PullSon();
        http.execute("http://192.168.43.1:2222/fabi/android/livre.php");
        pullCommentaire.execute("http://192.168.43.1:2222/fabi/android/pull_commentaire.php");
        similaires.execute("http://192.168.43.1:2222/fabi/android/similaires.php");
        pullSon.execute("http://192.168.43.1:2222/fabi/android/son.php");
        mAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!mMessage.getText().toString().equals("null"))
                {
                    PushCommentaire pushCommentaire = new PushCommentaire();
                    pushCommentaire.execute("http://192.168.43.1:2222/fabi/android/push_commentaire.php");
                }
            }
        });
    }

    // Fonction pour enregistrer l'image localement
    private void downloadImg(String nomImg) {
        new AsyncTask<Void, Void, File>() {
            @Override
            protected File doInBackground(Void... voids) {
                try {
                    // URL du PDF distant
                    String imgUrl = "http://192.168.43.1:2222/fabi/couverture/" + nomImg;
                    URL url = new URL(imgUrl);

                    // Ouvrir la connexion
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("GET");

                    // Télécharger le PDF dans le répertoire de téléchargement
                    File pdfFile = new File(Environment.getExternalStoragePublicDirectory(
                            Environment.DIRECTORY_DOWNLOADS), nomImg);

                    InputStream inputStream = urlConnection.getInputStream();
                    FileOutputStream outputStream = new FileOutputStream(pdfFile);

                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }

                    outputStream.close();
                    inputStream.close();

                    return pdfFile;

                } catch (IOException e) {
                    Log.e("DownloadTask", "Error while downloading PDF", e);
                    return null;
                }
            }
            @Override
            protected void onPostExecute(File pdfFile) {
                super.onPostExecute(pdfFile);

                if (pdfFile != null) {
                    // Ouvrir le PDF avec Adobe PDF Reader
//                    openPDFWithAdobeReader(pdfFile);
                } else {
                    Log.e("DownloadTask", "PDF file is null");
                }
            }
        }.execute();
    }

    private void downloadAndOpenPDF(String nomPdf) {
        new AsyncTask<Void, Void, File>() {
            @Override
            protected File doInBackground(Void... voids) {
                try {
                    // URL du PDF distant
                    String pdfUrl = "http://192.168.43.1:2222/fabi/pdf/" + nomPdf;
                    URL url = new URL(pdfUrl);

                    // Ouvrir la connexion
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("GET");

                    // Télécharger le PDF dans le répertoire de téléchargement
                    File pdfFile = new File(Environment.getExternalStoragePublicDirectory(
                            Environment.DIRECTORY_DOWNLOADS), nomPdf);

                    InputStream inputStream = urlConnection.getInputStream();
                    FileOutputStream outputStream = new FileOutputStream(pdfFile);

                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }

                    outputStream.close();
                    inputStream.close();

                    return pdfFile;

                } catch (IOException e) {
                    Log.e("DownloadTask", "Error while downloading PDF", e);
                    return null;
                }
            }

            @Override
            protected void onPostExecute(File pdfFile) {
                super.onPostExecute(pdfFile);

                if (pdfFile != null) {
                    // Ouvrir le PDF avec Adobe PDF Reader
                    openPDFWithAdobeReader(pdfFile);
                } else {
                    Log.e("DownloadTask", "PDF file is null");
                }
            }
        }.execute();
    }

    private void openPDFWithAdobeReader(File pdfFile) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri pdfUri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider", pdfFile);
        intent.setDataAndType(pdfUri, "application/pdf");
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        try {
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "OpenPDF : " + e, Toast.LENGTH_LONG).show();

        }
    }

        @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == STORAGE_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission accordée, télécharger et ouvrir le PDF
                downloadAndOpenPDF(mNomPdf);
            } else {
                // Permission refusée, gérer en conséquence
                Log.e("Permission", "Storage permission denied");
            }
        }
    }
@Override
    public void onDestroy() {
        super.onDestroy();
        mTimer.cancel(); // Arrête le Timer lors de la destruction de l'activité
        mMediaPlayer.release(); // Libère les ressources du MediaPlayer
    }

    public void CallMedia(View view) {
        Toast.makeText(this, "ok", Toast.LENGTH_SHORT).show();
    }

    private class Http extends AsyncTask<String,Void,String> {
        @Override
        protected String doInBackground(String... params) {

            try {
                OkHttpClient client = new OkHttpClient();
                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("matricule",mSession.getMatricule())
                        .addFormDataPart("idLivre",mIdLivre)
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
                    Toast.makeText(LivreActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }

            }catch (Exception e)
            {
                return null;
            }
            return null;
        }
        @Override
        protected void onPostExecute(String jsonData){
            //Toast.makeText(NoteService.this, response, Toast.LENGTH_SHORT).show();
            if(jsonData != null)
            {
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(jsonData);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
                try {
                    mNomCouverture = jsonObject.getString("couverture");
                    mNomCouvertureCat = jsonObject.getString("nomCouverture");
                    mProfilAuteur = jsonObject.getString("profilAuteur");
                    Picasso.with(getApplicationContext())
                            .load("http://192.168.43.1:2222/fabi/couverture/" + mNomCouverture)
                            .placeholder(R.drawable.default_livre)
                            .error(R.drawable.default_livre)
                            .transform(new RoundedTransformation(15,4))
                            .resize(200,334)
                            .into(mCouverture);
                    if(jsonObject.getString("estPhysique").equals("1"))
                        mBReservation.setVisibility(View.VISIBLE);
                    if(jsonObject.getString("estAudio").equals("1"))
                        mBAudio.setVisibility(View.VISIBLE);
                    if(!jsonObject.getString("documentElec").equals("null"))
                    {
                        mBPDF.setVisibility(View.VISIBLE);
                        mNomPdf = jsonObject.getString("documentElec");
                        mAuteur = jsonObject.getString("auteur");
                    }
                    mTitre.setText(jsonObject.getString("titreLivre"));
                    mCategorie.setText(jsonObject.getString("nomCat"));
                    mDesc.setText(jsonObject.getString("descLivre"));
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
            else
            {

            }

        }
    }

    private class PullCommentaire extends AsyncTask<String,Void,String> {
        @Override
        protected String doInBackground(String... params) {

            try {
                OkHttpClient client = new OkHttpClient();
                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("matricule",mSession.getMatricule())
                        .addFormDataPart("idLivre",mIdLivre)
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
                    Toast.makeText(LivreActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }

            }catch (Exception e)
            {
                return null;
            }
            return null;
        }
        @Override
        protected void onPostExecute(String jsonData){
            //Toast.makeText(NoteService.this, response, Toast.LENGTH_SHORT).show();
            if(jsonData != null)
            {
                if(!jsonData.equals("RAS"))
                {
                    JSONArray jsonArray = null;
                    try {
                        jsonArray = new JSONArray(jsonData);
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                    for (int i=0;i<jsonArray.length();i++) {
                        try {
                            mList.add(new Disscution(jsonArray.getJSONObject(i).getString("profil"),jsonArray.getJSONObject(i).getString("nomUt"),jsonArray.getJSONObject(i).getString("message")));
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    mDisscutionAdapter = new DisscutionAdapter(mList);
                    mRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                    mRecyclerView.setAdapter(mDisscutionAdapter);
                }
            }
            else
            {

            }

        }
    }

    private class PushCommentaire extends AsyncTask<String,Void,String> {
        @Override
        protected String doInBackground(String... params) {

            try {
                OkHttpClient client = new OkHttpClient();
                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("matricule",mSession.getMatricule())
                        .addFormDataPart("idLivre",mIdLivre)
                        .addFormDataPart("message",mMessage.getText().toString())
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
                    Toast.makeText(LivreActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }

            }catch (Exception e)
            {
                return null;
            }
            return null;
        }
        @Override
        protected void onPostExecute(String jsonData){
            //Toast.makeText(NoteService.this, response, Toast.LENGTH_SHORT).show();
            if(jsonData != null)
            {
                if(jsonData.equals("true"))
                {
                    mMessage.setText("");
                }
                Toast.makeText(LivreActivity.this, jsonData, Toast.LENGTH_SHORT).show();
            }
            else
            {

            }

        }
    }


    private class Similaires extends AsyncTask<String,Void,String> {
        @Override
        protected String doInBackground(String... params) {

            try {
                OkHttpClient client = new OkHttpClient();
                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("matricule",mSession.getMatricule())
                        .addFormDataPart("nomCategorie",mCategorie.getText().toString())
                        .addFormDataPart("idLivre",mIdLivre)
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
                    Toast.makeText(LivreActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }

            }catch (Exception e)
            {
                return null;
            }
            return null;
        }
        @Override
        protected void onPostExecute(String jsonData){
            //Toast.makeText(NoteService.this, response, Toast.LENGTH_SHORT).show();
            if(jsonData != null)
            {
                if(!jsonData.equals("RAS"))
                {
                    JSONArray jsonArray = null;
                    try {
                        jsonArray = new JSONArray(jsonData);
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                    for (int i=0;i<jsonArray.length();i++) {
                        try {
                            mList2.add(new Recenmment(mIdLivre,jsonArray.getJSONObject(i).getString("couverture"),null));
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    mRecenmmentAdapter = new SimulaireAdapter(mList2);
                    mRecyclerView2.setLayoutManager(new LinearLayoutManager(getApplicationContext(),LinearLayoutManager.HORIZONTAL,false));
                    mRecyclerView2.setAdapter(mRecenmmentAdapter);
                }
                //Toast.makeText(getContext(), jsonData, Toast.LENGTH_SHORT).show();
            }
            else
            {

            }

        }
    }

    private class PullSon extends AsyncTask<String,Void,String> {
        @Override
        protected String doInBackground(String... params) {

            try {
                OkHttpClient client = new OkHttpClient();
                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("matricule",mSession.getMatricule())
                        .addFormDataPart("idLivre",mIdLivre)
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
                    Toast.makeText(LivreActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }

            }catch (Exception e)
            {
                return null;
            }
            return null;
        }
        @Override
        protected void onPostExecute(String jsonData){
            //Toast.makeText(NoteService.this, response, Toast.LENGTH_SHORT).show();
            if(jsonData != null)
            {
                if(!jsonData.equals("RAS"))
                {
                    JSONArray jsonArray = null;
                    try {
                        jsonArray = new JSONArray(jsonData);
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                    for (int i=0;i<jsonArray.length();i++) {
                        try {
                            mListSon.add(new Son(i+1,jsonArray.getJSONObject(i).getString("audio"),jsonArray.getJSONObject(i).getString("titre"),0,false));
                            if(i==0)
                                mAudio = jsonArray.getJSONObject(i).getString("audio");
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    url = "http://192.168.43.1:2222/fabi/audio/" + mAudio;
                    try {
                        mMediaPlayer.setDataSource(url);
                        mMediaPlayer.prepare();
                        mSeekBar.setMax(mMediaPlayer.getDuration());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    mSonAdapter = new SonAdapter(mListSon);
                    mRecyclerSon.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                    mRecyclerSon.setAdapter(mSonAdapter);
                }
            }
            else
            {

            }

        }
    }
    public void ReservationDialog() {
        Spinner delaitReservation = mDialogReservationCusto.findViewById(R.id.DelaitReservation);
        CheckBox checkBox = mDialogReservationCusto.findViewById(R.id.estConsulte);
        Button bttEnvoi = mDialogReservationCusto.findViewById(R.id.reservation_envoi);
        EditText reservPassword = mDialogReservationCusto.findViewById(R.id.reservation_password);
        TextView reservErr = mDialogReservationCusto.findViewById(R.id.err_reservation);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.delait_reservation, android.R.layout.simple_spinner_item);
        ArrayAdapter<CharSequence> adapterConsult = ArrayAdapter.createFromResource(this, R.array.delait_cosultation, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        adapterConsult.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        delaitReservation.setAdapter(adapter);
        checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(checkBox.isChecked())
                {
                    delaitReservation.setEnabled(false);
                }
                else
                    delaitReservation.setEnabled(true);
            }
        });
        bttEnvoi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                if(delaitReservation.isEnabled())
//                    Toast.makeText(LivreActivity.this, "OK", Toast.LENGTH_SHORT).show();
//                else
//                    Toast.makeText(LivreActivity.this, "KO", Toast.LENGTH_SHORT).show();
                if(reservPassword.getText().toString().equals(""))
                    reservErr.setText("svp votre mot de passe");
                else
                {
                    if(delaitReservation.isEnabled())
                        mNbrJour = String.valueOf(delaitReservation.getSelectedItemPosition() + 1);
                    else
                        mNbrJour = String.valueOf(-1);
                    Reservation reservation = new Reservation();
                    reservation.execute("http://192.168.43.1:2222/fabi/android/reservation.php");
                }

            }
        });
        mDialogReservationCusto.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mDialogReservationCusto.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        mDialogReservationCusto.build();
    }


    private class Reservation extends AsyncTask<String,Void,String> {
        @Override
        protected String doInBackground(String... params) {

            try {
                OkHttpClient client = new OkHttpClient();
                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("matricule",mSession.getMatricule())
                        .addFormDataPart("idLivre",mIdLivre)
                        .addFormDataPart("nbrJour",mNbrJour)
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
                    Toast.makeText(LivreActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }

            }catch (Exception e)
            {
                return null;
            }
            return null;
        }
        @Override
        protected void onPostExecute(String jsonData){
            //Toast.makeText(NoteService.this, response, Toast.LENGTH_SHORT).show();
            if(jsonData != null)
            {
                Toast.makeText(LivreActivity.this, jsonData, Toast.LENGTH_SHORT).show();
                if(jsonData.equals("true"))
                {
                    mDialogReservationCusto.cancel();
                    succeReservationDialog();
                }

                //Toast.makeText(LivreActivity.this, jsonData, Toast.LENGTH_SHORT).show();
            }
            else
            {

            }

        }
    }

    private void succeReservationDialog(){
        SucceReservation succeReservation = new SucceReservation(this);
        succeReservation.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        succeReservation.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        TextView message = succeReservation.findViewById(R.id.popo_message);
        TextView ok = succeReservation.findViewById(R.id.ok);
        message.setText("Merci d'avoir réservé \"" + mTitre.getText().toString() + "\" sur fabi; nous traitons votre demande et vous confirmerons la disponibilité bientôt.");
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                mSuggestion.setText("");
                succeReservation.cancel();
            }
        });
        succeReservation.build();
    }

    private void succeDowloadPDFDialog(){
        SucceReservation succeReservation = new SucceReservation(this);
        succeReservation.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        succeReservation.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        TextView message = succeReservation.findViewById(R.id.popo_message);
        TextView ok = succeReservation.findViewById(R.id.ok);
        message.setText("Le livre " +mTitre.getText().toString() + " format PDF a été téléchargé avec succès. N'hésitez pas à explorer son contenu dans l'application et contactez-nous en cas de besoin.");
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                mSuggestion.setText("");
                succeReservation.cancel();
            }
        });
        succeReservation.build();
    }
    private LinearLayout mBReservation;
    private LinearLayout mBAudio;
    private LinearLayout mBPDF;
    private RecyclerView mRecyclerView;
    private DisscutionAdapter mDisscutionAdapter;
    private ArrayList<Disscution> mList;

    private RecyclerView mRecyclerView2;
    private RecyclerView mRecyclerSon;
    private SonAdapter mSonAdapter;
    private List<Son> mListSon;
    private SimulaireAdapter mRecenmmentAdapter;
    private List<Recenmment> mList2;
    private String mIdLivre;
    private Session mSession;
    private ImageView mCouverture;
    private TextView mTitre;
    private TextView mCategorie;
    private TextView mDesc;
    private Button mBttReservation;
    private Button mBttAudio;
    private  Button mBttOpenPDF;
    private Button mBttDowloadPDF;
    private EditText mMessage;
    private ImageView mAdd;
    private LinearLayout mBlike;
    private LinearLayout mBNolike;
    private ImageView mBttLike;
    private ImageView mBttNoLike;
    private boolean isLike;
    private boolean isNoLike;
    private ImageView mPlay;
    private MediaPlayer mMediaPlayer;
    private SeekBar mSeekBar;
    private Handler handler;
    private Timer mTimer;
    private ImageView mStop;
    private RelativeLayout mRelativeLayout;
    private String url;
    private String mAudio;
    private int tmp_position;
    private DialogReservationCusto mDialogReservationCusto;
    private String mNomPdf;
    private ElectroniqueTable mElectroniqueTable;
    private String mAuteur;
    private String mNomCouverture;
    private String mNomCouvertureCat;
    private String mProfilAuteur;
    private String mNbrJour;
}