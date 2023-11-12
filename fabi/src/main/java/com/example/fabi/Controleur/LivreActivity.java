package com.example.fabi.Controleur;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fabi.Controleur.Model.Disscution;
import com.example.fabi.Controleur.Model.DisscutionAdapter;
import com.example.fabi.Controleur.Model.Recenmment;
import com.example.fabi.Controleur.Model.RecenmmentAdapter;
import com.example.fabi.Controleur.Model.RoundedTransformation;
import com.example.fabi.Controleur.Model.Session;
import com.example.fabi.Controleur.Model.Son;
import com.example.fabi.Controleur.Model.SonAdapter;
import com.example.fabi.R;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
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
        mBttPDF = findViewById(R.id.btt_pdf);
        mMessage = findViewById(R.id.messageCommentaire);
        mAdd = findViewById(R.id.addCommentaire);
        mBlike = findViewById(R.id.like);
        mBNolike = findViewById(R.id.nolike);
        mBttLike = findViewById(R.id.ico1);
        mBttNoLike = findViewById(R.id.ico2);
        mPlay = findViewById(R.id.paly);
        mStop = findViewById(R.id.stop);
        mSeekBar = findViewById(R.id.seekbar);
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
                    Picasso.with(getApplicationContext())
                            .load("http://192.168.43.1:2222/fabi/couverture/" + jsonObject.getString("couverture"))
                            .placeholder(R.drawable.item)
                            .error(R.drawable.item)
                            .transform(new RoundedTransformation(15,4))
                            .resize(200,334)
                            .into(mCouverture);
                    if(jsonObject.getString("estPhysique").equals("1"))
                        mBttReservation.setVisibility(View.VISIBLE);
                    if(jsonObject.getString("estAudio").equals("1"))
                        mBttAudio.setVisibility(View.VISIBLE);
                    if(!jsonObject.getString("documentElec").equals("null"))
                        mBttPDF.setVisibility(View.VISIBLE);
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
                            mList2.add(new Recenmment(mIdLivre,jsonArray.getJSONObject(i).getString("couverture")));
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    mRecenmmentAdapter = new RecenmmentAdapter(mList2);
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
    private RecyclerView mRecyclerView;
    private DisscutionAdapter mDisscutionAdapter;
    private ArrayList<Disscution> mList;

    private RecyclerView mRecyclerView2;
    private RecyclerView mRecyclerSon;
    private SonAdapter mSonAdapter;
    private List<Son> mListSon;
    private RecenmmentAdapter mRecenmmentAdapter;
    private List<Recenmment> mList2;
    private String mIdLivre;
    private Session mSession;
    private ImageView mCouverture;
    private TextView mTitre;
    private TextView mCategorie;
    private TextView mDesc;
    private Button mBttReservation;
    private Button mBttAudio;
    private  Button mBttPDF;
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

}