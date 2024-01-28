package com.ninotech.fabi.controleur.activity;

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

import com.ninotech.fabi.controleur.adapter.TalksAdapter;
import com.ninotech.fabi.controleur.dialog.ReservationDialog;
import com.ninotech.fabi.model.data.Book;
import com.ninotech.fabi.model.data.Talks;
import com.ninotech.fabi.model.table.ElectroniqueTable;
import com.ninotech.fabi.model.data.Recenmment;
import com.ninotech.fabi.controleur.adapter.SimulaireAdapter;
import com.ninotech.fabi.controleur.animation.RoundedTransformation;
import com.ninotech.fabi.model.table.Session;
import com.ninotech.fabi.model.data.Tones;
import com.ninotech.fabi.controleur.adapter.TonesAdapter;
import com.ninotech.fabi.R;
import com.ninotech.fabi.controleur.dialog.SucceReservationDialog;
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
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class BookActivity extends AppCompatActivity {
    private static final int STORAGE_PERMISSION_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book);
        Objects.requireNonNull(getSupportActionBar()).hide();
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        Intent intentBook = getIntent();
        mSession = new Session(this);
        mBook = new Book(intentBook.getStringExtra("intent_adapter_book_id"));
        mTalksList = new ArrayList<>();
        mList2 = new ArrayList<>();
        mListTones = new ArrayList<>();
        mCommentaireRecyclerView = findViewById(R.id.recycler_view_activity_book_Comments);
        mSimilarRecyclerView = findViewById(R.id.recycler_view_activity_book_similar);
        mTonesRecyclerView = findViewById(R.id.recycler_view_activity_book_tones);
        mBlanketImageView = findViewById(R.id.image_view_activity_book_blanket);
        mTitleTextView = findViewById(R.id.text_view_activity_book_title);
        mCategoryTextView = findViewById(R.id.text_view_activity_book_category);
        mDescriptionTextView = findViewById(R.id.text_view_activity_book_description);
        Button reservationButton = findViewById(R.id.button_activity_book_reservation);
        Button audioButton = findViewById(R.id.button_activity_book_audio);
        Button openPDFButton = findViewById(R.id.button_activity_book_open_pdf);
        Button downloadPDFButton = findViewById(R.id.button_activity_book_download_pdf);
        mMessageTextView = findViewById(R.id.text_view_activity_book_message);
        ImageView addCommentsImageView = findViewById(R.id.image_view_activity_book_add_comments);
        LinearLayout likeLinearLayout = findViewById(R.id.linear_layout_activiry_book_like);
        LinearLayout noLikeLinearLayout = findViewById(R.id.linear_layout_activiry_book_nolike);
        mLikeImageView = findViewById(R.id.image_view_activity_book_like);
        mNoLikeImageView = findViewById(R.id.image_view_activity_book_nolike);
        mPlayerImageView = findViewById(R.id.image_view_activity_book_player);
        ImageView stopImageView = findViewById(R.id.image_view_activity_book_stop);
        mSeekBar = findViewById(R.id.seekbar_activity_book);
        mReservationLinearLayout = findViewById(R.id.linear_layout_activity_book_reservation);
        mAudioLinearLayout = findViewById(R.id.linear_layout_activity_book_audio);
        mElectronicLinearLayout = findViewById(R.id.linear_layout_activity_book_electronic);
        mReservationDialog = new ReservationDialog(this);
        mElectronicTable = new ElectroniqueTable(this);
        tmp_position=0;
        mMediaPlayer = new MediaPlayer();
        mIsLike =false;
        mIsNoLike =false;
        Handler handler = new Handler();
        mTimer = new Timer();
        BroadcastReceiver receiverNote = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if ("ACTION_AUDIO".equals(intent.getAction())) {
                    mAudio = intent.getStringExtra("intent_adapter_tones_title");
                    int position = intent.getIntExtra("intent_adapter_tones_position",0);
                    url = getString(R.string.ip_server) + "audio/" + mAudio;
                    try {
                        if(tmp_position != position)
                            mListTones.get(tmp_position).setPlaying(false);
                        mTonesRecyclerView.setAdapter(mTonesAdapter);
                        mMediaPlayer.reset();
                        mMediaPlayer.setDataSource(url);
                        mMediaPlayer.prepare();
                        mSeekBar.setMax(mMediaPlayer.getDuration());
                        mMediaPlayer.start();
                        tmp_position = position;
                        mPlayerImageView.setImageResource(R.drawable.vector_black3_play);
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
        reservationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ReservationDialog();
            }
        });

        openPDFButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Vérifier et demander la permission d'écriture externe si nécessaire
                if (ContextCompat.checkSelfPermission(getApplicationContext(),
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {

                    ActivityCompat.requestPermissions(BookActivity.this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            STORAGE_PERMISSION_REQUEST_CODE);
                } else {
                    // Si la permission est déjà accordée, télécharger et ouvrir le PDF
                    downloadAndOpenPDF(mNomPdf);
                }
            }
        });

        downloadPDFButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mElectronicTable.insert(mSession.getIdNumber(),mIdLivre, mDescriptionTextView.getText().toString(),mAuteur,mNomCouverture,mNomPdf, mCategoryTextView.getText().toString(), mTitleTextView.getText().toString(),mNomCouvertureCat,mProfilAuteur))
                    succeDowloadPDFDialog();
            }
        });
        mPlayerImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mMediaPlayer.isPlaying())
                {
                    mPlayerImageView.setImageResource(R.drawable.vector_black3_plause);
                    mMediaPlayer.pause();
                }
                else
                {
                    mPlayerImageView.setImageResource(R.drawable.vector_black3_play);
                    mMediaPlayer.start();
                }
            }
        });

        stopImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMediaPlayer.stop();
                mPlayerImageView.setImageResource(R.drawable.vector_black3_plause);
                mTimer.cancel(); // Arrête le Timer lors de la destruction de l'activité
               mSeekBar.setProgress(0);
            }
        });
        likeLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!mIsLike)
                {
                    mIsLike = true;
                    mLikeImageView.setImageResource(R.drawable.vector_purple2_200_on_like);
                }
                else
                {
                    mIsLike = false;
                    mLikeImageView.setImageResource(R.drawable.vector_black3_off_like);

                }
            }
        });

        noLikeLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!mIsNoLike)
                {
                    mIsNoLike = true;
                    mNoLikeImageView.setImageResource(R.drawable.vector_rouge_on_nolike);
                }
                else
                {
                    mIsNoLike = false;
                    mNoLikeImageView.setImageResource(R.drawable.vector_black3_off_nolike);

                }
            }
        });
        RecoveryBook recoveryBook = new RecoveryBook();
        PullCommentaire pullCommentaire = new PullCommentaire();
        Similaires similaires = new Similaires();
        PullSon pullSon = new PullSon();
        recoveryBook.execute(getString(R.string.ip_server_android) + "Book.php",mSession.getIdNumber(),mBook.getId());
        pullCommentaire.execute(getString(R.string.ip_server_android) + "ReceiveComments.php");
        similaires.execute(getString(R.string.ip_server_android) + "SimilarBook.php");
        pullSon.execute(getString(R.string.ip_server_android) + "Tones.php");
        addCommentsImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!mMessageTextView.getText().toString().equals("null"))
                {
                    PushCommentaire pushCommentaire = new PushCommentaire();
                    pushCommentaire.execute(getString(R.string.ip_server_android) + "SendComments.php");
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
                    String imgUrl = getString(R.string.ip_server) + "couverture/" + nomImg;
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
                    String pdfUrl = getString(R.string.ip_server) + "pdf/" + nomPdf;
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

    private class RecoveryBook extends AsyncTask<String,Void,String> {
        @Override
        protected String doInBackground(String... params) {

            try {
                OkHttpClient client = new OkHttpClient();
                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("matricule",params[1])
                        .addFormDataPart("idLivre",params[2])
                        .build();
                Request request = new Request.Builder()
                        .url(params[0])
                        .post(requestBody)
                        .build();
                try {
                    Response response = client.newCall(request).execute();
                    assert response.body() != null;
                    return response.body().string();
                }catch (IOException e)
                {
                    Toast.makeText(BookActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }

            }catch (Exception e)
            {
                return null;
            }
            return null;
        }
        @Override
        protected void onPostExecute(String jsonData){
            if(jsonData != null)
            {
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(jsonData);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
                try {
                    mBook.setBlanket(jsonObject.getString("couverture"));
                    mNomCouvertureCat = jsonObject.getString("nomCouverture");
                    mProfilAuteur = jsonObject.getString("profilAuteur");
                    Picasso.with(getApplicationContext())
                            .load(getString(R.string.ip_server) + "couverture/" + mBook.getBlanket())
                            .placeholder(R.drawable.img_default_livre)
                            .error(R.drawable.img_default_livre)
                            .transform(new RoundedTransformation(15,4))
                            .resize(200,334)
                            .into(mBlanketImageView);
                    if(jsonObject.getString("estPhysique").equals("1"))
                        mReservationLinearLayout.setVisibility(View.VISIBLE);
                    if(jsonObject.getString("estAudio").equals("1"))
                        mAudioLinearLayout.setVisibility(View.VISIBLE);
                    if(!jsonObject.getString("documentElec").equals("null"))
                    {
                        mElectronicLinearLayout.setVisibility(View.VISIBLE);
                        mNomPdf = jsonObject.getString("documentElec");
                        mAuteur = jsonObject.getString("auteur");
                    }
                    mTitleTextView.setText(jsonObject.getString("titreLivre"));
                    mCategoryTextView.setText(jsonObject.getString("nomCat"));
                    mDescriptionTextView.setText(jsonObject.getString("descLivre"));
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
                        .addFormDataPart("matricule",mSession.getIdNumber())
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
                    Toast.makeText(BookActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }

            }catch (Exception e)
            {
                return null;
            }
            return null;
        }
        @Override
        protected void onPostExecute(String jsonData){
            //Toast.makeText(NotificationService.this, response, Toast.LENGTH_SHORT).show();
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
                            mTalksList.add(new Talks(jsonArray.getJSONObject(i).getString("profil"),jsonArray.getJSONObject(i).getString("nomUt"),jsonArray.getJSONObject(i).getString("message")));
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    TalksAdapter talksAdapter = new TalksAdapter(mTalksList);
                    mCommentaireRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                    mCommentaireRecyclerView.setAdapter(talksAdapter);
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
                        .addFormDataPart("matricule",mSession.getIdNumber())
                        .addFormDataPart("idLivre",mIdLivre)
                        .addFormDataPart("message", mMessageTextView.getText().toString())
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
                    Toast.makeText(BookActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }

            }catch (Exception e)
            {
                return null;
            }
            return null;
        }
        @Override
        protected void onPostExecute(String jsonData){
            //Toast.makeText(NotificationService.this, response, Toast.LENGTH_SHORT).show();
            if(jsonData != null)
            {
                if(jsonData.equals("true"))
                {
                    mMessageTextView.setText("");
                }
                Toast.makeText(BookActivity.this, jsonData, Toast.LENGTH_SHORT).show();
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
                        .addFormDataPart("matricule",mSession.getIdNumber())
                        .addFormDataPart("nomCategorie", mCategoryTextView.getText().toString())
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
                    Toast.makeText(BookActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }

            }catch (Exception e)
            {
                return null;
            }
            return null;
        }
        @Override
        protected void onPostExecute(String jsonData){
            //Toast.makeText(NotificationService.this, response, Toast.LENGTH_SHORT).show();
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
                    mSimilarRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext(),LinearLayoutManager.HORIZONTAL,false));
                    mSimilarRecyclerView.setAdapter(mRecenmmentAdapter);
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
                        .addFormDataPart("matricule",mSession.getIdNumber())
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
                    Toast.makeText(BookActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }

            }catch (Exception e)
            {
                return null;
            }
            return null;
        }
        @Override
        protected void onPostExecute(String jsonData){
            //Toast.makeText(NotificationService.this, response, Toast.LENGTH_SHORT).show();
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
                            mListTones.add(new Tones(i+1,jsonArray.getJSONObject(i).getString("audio"),jsonArray.getJSONObject(i).getString("titre"),0,false));
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
                    mTonesAdapter = new TonesAdapter(mListTones);
                    mTonesRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                    mTonesRecyclerView.setAdapter(mTonesAdapter);
                }
            }
            else
            {

            }

        }
    }
    public void ReservationDialog() {
        Spinner delaitReservation = mReservationDialog.findViewById(R.id.DelaitReservation);
        CheckBox checkBox = mReservationDialog.findViewById(R.id.estConsulte);
        Button bttEnvoi = mReservationDialog.findViewById(R.id.reservation_envoi);
        EditText reservPassword = mReservationDialog.findViewById(R.id.reservation_password);
        TextView reservErr = mReservationDialog.findViewById(R.id.err_reservation);
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
//                    Toast.makeText(BookActivity.this, "OK", Toast.LENGTH_SHORT).show();
//                else
//                    Toast.makeText(BookActivity.this, "KO", Toast.LENGTH_SHORT).show();
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
        mReservationDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mReservationDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        mReservationDialog.build();
    }


    private class Reservation extends AsyncTask<String,Void,String> {
        @Override
        protected String doInBackground(String... params) {

            try {
                OkHttpClient client = new OkHttpClient();
                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("matricule",mSession.getIdNumber())
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
                    Toast.makeText(BookActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }

            }catch (Exception e)
            {
                return null;
            }
            return null;
        }
        @Override
        protected void onPostExecute(String jsonData){
            //Toast.makeText(NotificationService.this, response, Toast.LENGTH_SHORT).show();
            if(jsonData != null)
            {
                Toast.makeText(BookActivity.this, jsonData, Toast.LENGTH_SHORT).show();
                if(jsonData.equals("true"))
                {
                    mReservationDialog.cancel();
                    succeReservationDialog();
                }

                //Toast.makeText(BookActivity.this, jsonData, Toast.LENGTH_SHORT).show();
            }
            else
            {

            }

        }
    }

    private void succeReservationDialog(){
        SucceReservationDialog succeReservationDialog = new SucceReservationDialog(this);
        succeReservationDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        succeReservationDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        TextView message = succeReservationDialog.findViewById(R.id.popo_message);
        TextView ok = succeReservationDialog.findViewById(R.id.ok);
        message.setText("Merci d'avoir réservé \"" + mTitleTextView.getText().toString() + "\" sur fabi; nous traitons votre demande et vous confirmerons la disponibilité bientôt.");
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                mSuggestion.setText("");
                succeReservationDialog.cancel();
            }
        });
        succeReservationDialog.build();
    }

    private void succeDowloadPDFDialog(){
        SucceReservationDialog succeReservationDialog = new SucceReservationDialog(this);
        succeReservationDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        succeReservationDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        TextView message = succeReservationDialog.findViewById(R.id.popo_message);
        TextView ok = succeReservationDialog.findViewById(R.id.ok);
        message.setText("Le livre " + mTitleTextView.getText().toString() + " format PDF a été téléchargé avec succès. N'hésitez pas à explorer son contenu dans l'application et contactez-nous en cas de besoin.");
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                mSuggestion.setText("");
                succeReservationDialog.cancel();
            }
        });
        succeReservationDialog.build();
    }
    private LinearLayout mReservationLinearLayout;
    private LinearLayout mAudioLinearLayout;
    private LinearLayout mElectronicLinearLayout;
    private RecyclerView mCommentaireRecyclerView;
    private ArrayList<Talks> mTalksList;

    private RecyclerView mSimilarRecyclerView;
    private RecyclerView mTonesRecyclerView;
    private TonesAdapter mTonesAdapter;
    private List<Tones> mListTones;
    private SimulaireAdapter mRecenmmentAdapter;
    private List<Recenmment> mList2;
    private String mIdLivre;
    private Session mSession;
    private ImageView mBlanketImageView;
    private TextView mTitleTextView;
    private TextView mCategoryTextView;
    private TextView mDescriptionTextView;
    private EditText mMessageTextView;
    private ImageView mLikeImageView;
    private ImageView mNoLikeImageView;
    private boolean mIsLike;
    private boolean mIsNoLike;
    private ImageView mPlayerImageView;
    private MediaPlayer mMediaPlayer;
    private SeekBar mSeekBar;
    private Timer mTimer;
    private RelativeLayout mRelativeLayout;
    private String url;
    private String mAudio;
    private int tmp_position;
    private ReservationDialog mReservationDialog;
    private String mNomPdf;
    private ElectroniqueTable mElectronicTable;
    private String mAuteur;
    private String mNomCouverture;
    private String mNomCouvertureCat;
    private String mProfilAuteur;
    private String mNbrJour;
    private Book mBook;
}