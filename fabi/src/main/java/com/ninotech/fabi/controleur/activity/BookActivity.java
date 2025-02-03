package com.ninotech.fabi.controleur.activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.core.app.NotificationCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ninotech.fabi.controleur.adapter.NoConnectionAdapter;
import com.ninotech.fabi.controleur.adapter.TalksAdapter;
import com.ninotech.fabi.controleur.dialog.ReservationDialog;
import com.ninotech.fabi.model.data.AudioDownloader;
import com.ninotech.fabi.model.data.Author;
import com.ninotech.fabi.model.data.OnlineBook;
import com.ninotech.fabi.model.data.Category;
import com.ninotech.fabi.model.data.Chat;
import com.ninotech.fabi.model.data.Connection;
import com.ninotech.fabi.model.data.ElectronicDownloader;
import com.ninotech.fabi.model.data.Talks;
import com.ninotech.fabi.model.syn.SendComments;
import com.ninotech.fabi.model.table.ElectronicTable;
import com.ninotech.fabi.model.data.SimilarBook;
import com.ninotech.fabi.controleur.animation.RoundedTransformation;
import com.ninotech.fabi.model.table.Session;
import com.ninotech.fabi.model.data.Tones;
import com.ninotech.fabi.controleur.adapter.TonesAdapter;
import com.ninotech.fabi.R;
import com.ninotech.fabi.controleur.dialog.SimpleOkDialog;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.concurrent.TimeUnit;

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
        mOnlineBook = new OnlineBook(intentBook.getStringExtra("intent_adapter_book_id"));
        mTalksList = new ArrayList<>();
        mListSimilar = new ArrayList<>();
        mListTones = new ArrayList<>();
        mNestedScrollView = findViewById(R.id.nested_scroll_view_activity_book);
        mCommentsRecyclerView = findViewById(R.id.recycler_view_activity_book_Comments);
        mNoConnectionRecyclerView = findViewById(R.id.recycler_view_activity_book_no_connection);
        mBlanketImageView = findViewById(R.id.image_view_adapter_book_simple_cover);
        mTitleTextView = findViewById(R.id.text_view_adapter_book_simple_title);
        mCategoryTextView = findViewById(R.id.text_view_adapter_description_category);
        mDescriptionTextView = findViewById(R.id.text_view_activity_book_description);
        mTimeNowTextView = findViewById(R.id.text_view_activity_book_time_now);
        mReservationButton = findViewById(R.id.button_activity_book_reservation);
        audioButton = findViewById(R.id.button_activity_book_audio);
        Button openPDFButton = findViewById(R.id.button_activity_book_open_pdf);
        downloadPDFButton = findViewById(R.id.button_activity_book_download_pdf);
        mMessageTextView = findViewById(R.id.text_view_activity_book_message);
        mNumberLikeTextView = findViewById(R.id.text_view_activity_book_number_like);
        mNumberNoLikeTextView = findViewById(R.id.text_view_activity_book_number_no_like);
        mNumberSubscribeTextView = findViewById(R.id.text_view_activity_book_number_subscribe);
        mCote = findViewById(R.id.text_view_adapter_book_simple_id_book);
        ImageView addCommentsImageView = findViewById(R.id.image_view_activity_book_add_comments);
        ImageView stopImageView = findViewById(R.id.image_view_activity_book_stop);
        LinearLayout likeLinearLayout = findViewById(R.id.linear_layout_activity_book_like);
        LinearLayout noLikeLinearLayout = findViewById(R.id.linear_layout_activiry_book_nolike);
        LinearLayout subscribeLinearLayout = findViewById(R.id.linear_layout_activity_book_subscribe);
        mLikeImageView = findViewById(R.id.image_view_activity_book_like);
        mNoLikeImageView = findViewById(R.id.image_view_activity_book_no_like);
        mPlayerImageView = findViewById(R.id.image_view_activity_book_player);
        mSubscribeImageView = findViewById(R.id.image_view_activity_book_subscribe);
        mSeekBar = findViewById(R.id.seekbar_activity_book);
        mReservationLinearLayout = findViewById(R.id.linear_layout_activity_book_reservation);
        mAudioLinearLayout = findViewById(R.id.linear_layout_activity_book_audio);
        mElectronicLinearLayout = findViewById(R.id.linear_layout_activity_book_electronic);
        mBackImageView = findViewById(R.id.image_view_toolbar_book);
        mNameAuthor = findViewById(R.id.text_view_adapter_book_simple_author_name);
        downloadAudioProgressBar = findViewById(R.id.progress_bar_download_audio);
        downloadPdfProgressBar = findViewById(R.id.progress_bar_download_pdf);
        mReservationDialog = new ReservationDialog(this);
        mElectronicTable = new ElectronicTable(this);
        mHandler = new Handler();
        mMediaPlayer = new MediaPlayer();
        mTimer = new Timer();

        BroadcastReceiver finishDownloadReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if ("ACTION_FINISH_DOWNLOAD".equals(intent.getAction())) {
                    String formatString = intent.getStringExtra("format");
                    switch (formatString)
                    {
                        case "audio":
                            succeDowloadAudioDialog("Le livre " + mTitleTextView.getText().toString() + " format audio a été téléchargé avec succès. N'hésitez pas à explorer son contenu dans l'application et contactez-nous en cas de besoin.");
                            break;
                        case "pdf":
                            succeDowloadPDFDialog("Le livre " + mTitleTextView.getText().toString() + " format PDF a été téléchargé avec succès. N'hésitez pas à explorer son contenu dans l'application et contactez-nous en cas de besoin.");
                            break;
                    }
                }
            }
        };
        registerReceiver(finishDownloadReceiver, new IntentFilter("ACTION_FINISH_DOWNLOAD"));
        mBackImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
       // createNotificationChannel();
        ArrayList<Connection> list = new ArrayList<>();
        list.add(new Connection(getString(R.string.wait),null,true));
        mNoConnectionAdapter = new NoConnectionAdapter(list);
        mNoConnectionRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        mNoConnectionRecyclerView.setAdapter(mNoConnectionAdapter);
        BroadcastReceiver receiverNoConnectionAdapter = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if ("BOOK_ACTIVITY".equals(intent.getAction())) {
                    try {
                        finish();
                        startActivity(getIntent());
                    }catch (Exception e)
                    {
                        Log.e("errRankingFragment",e.getMessage());
                    }

                }
            }
        };
        registerReceiver(receiverNoConnectionAdapter, new IntentFilter("BOOK_ACTIVITY"));
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

        mReservationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                if(mReservationButton.getText().toString().equals(getString(R.string.reservation_book)))
                    ReservationDialog();
                else
                {
                    if(mReservationButton.getText().toString().equals(getString(R.string.cancel_reservation)))
                    {
                        CancelReservationSyn cancelReservationSyn = new CancelReservationSyn();
                        cancelReservationSyn.execute(getString(R.string.ip_server_android) + "CancelReservation.php",mSession.getIdNumber(), mOnlineBook.getId());
                    }
                }
            }
        });
        openPDFButton.setVisibility(View.GONE);
//        openPDFButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//            }
//        });

        downloadPDFButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(BookActivity.this, "Téléchargement démarrer", Toast.LENGTH_SHORT).show();
                //showProgressNotification(mTitleTextView.getText().toString() + " Format PDF");
               downloadPDFButton.setText("");
                downloadPdfProgressBar.setVisibility(View.VISIBLE);
                ElectronicDownloader electronicDownloader = new ElectronicDownloader(getApplicationContext(),mSession.getIdNumber(), mOnlineBook);
               // Toast.makeText(BookActivity.this, mOnlineBook.getAuthor(), Toast.LENGTH_SHORT).show();
                electronicDownloader.execute(mOnlineBook.getCover(), mOnlineBook.getElectronic(),mCategory.getCover(),mAuthor.getProfile());
            }
        });
        audioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                audioButton.setText("");
                downloadAudioProgressBar.setVisibility(View.VISIBLE);
                AudioDownloader audioDownloader = new AudioDownloader(getApplicationContext(),mSession.getIdNumber(), mOnlineBook,mTones);
                audioDownloader.execute(mOnlineBook.getCover(), mOnlineBook.getElectronic(),mCategory.getCover(),mAuthor.getProfile(),mTones.getAudio());
            }
        });
        mPlayerImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mMediaPlayer.isPlaying())
                {
                    mPlayerImageView.setImageResource(R.drawable.vector_black3_pause);
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
                if(mMediaPlayer != null)
                {
                    mPlayerImageView.setImageResource(R.drawable.vector_black3_pause);
                    mSeekBar.setProgress(0);
                    mMediaPlayer.pause();
                    mTimeNowTextView.setText(R.string.default_time);
                }
            }
        });
        likeLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                InsertLikeSyn insertLikeSyn = new InsertLikeSyn();
                insertLikeSyn.execute(getString(R.string.ip_server_android) + "InsertLike.php",mSession.getIdNumber(), mOnlineBook.getId());
            }
        });

        noLikeLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                InsertNoLikeSyn insertNoLikeSyn = new InsertNoLikeSyn();
                insertNoLikeSyn.execute(getString(R.string.ip_server_android) + "InsertNoLike.php",mSession.getIdNumber(), mOnlineBook.getId());
            }
        });

        subscribeLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                InsertSubscribeBookSyn insertSubscribeBookSyn = new InsertSubscribeBookSyn();
                insertSubscribeBookSyn.execute(getString(R.string.ip_server_android) + "InsertSubscribeBook.php",mSession.getIdNumber(), mOnlineBook.getId());
            }
        });
        RecoveryBook recoveryBook = new RecoveryBook();
        ReceiveComments receiveComments = new ReceiveComments();
        RecoveryTones recoveryTones = new RecoveryTones();
        IsLikeSyn isLikeSyn = new IsLikeSyn();
        IsNoLikeSyn isNoLikeSyn = new IsNoLikeSyn();
        IsSubscribeBookSyn isSubscribeBookSyn = new IsSubscribeBookSyn();
        InsertViewSyn insertViewSyn = new InsertViewSyn();
        IsReservationSyn isReservationSyn = new IsReservationSyn();
        recoveryBook.execute(getString(R.string.ip_server_android) + "Book.php",mSession.getIdNumber(), mOnlineBook.getId());
        isReservationSyn.execute(getString(R.string.ip_server_android) + "IsReservation.php",mSession.getIdNumber(), mOnlineBook.getId());
        insertViewSyn.execute(getString(R.string.ip_server_android) + "InsertView.php",mSession.getIdNumber(), mOnlineBook.getId());
        isSubscribeBookSyn.execute(getString(R.string.ip_server_android) + "IsSubscribeBook.php",mSession.getIdNumber(), mOnlineBook.getId());
        isLikeSyn.execute(getString(R.string.ip_server_android) + "IsLike.php",mSession.getIdNumber(), mOnlineBook.getId());
        isNoLikeSyn.execute(getString(R.string.ip_server_android) + "IsNoLike.php",mSession.getIdNumber(), mOnlineBook.getId());
        receiveComments.execute(getString(R.string.ip_server_android) + "ReceiveComments.php",mSession.getIdNumber(), mOnlineBook.getId());
        recoveryTones.execute(getString(R.string.ip_server_android) + "Tones.php");
        addCommentsImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!mMessageTextView.getText().toString().equals("null"))
                {
                    Chat chat = new Chat(mSession.getIdNumber(),getApplicationContext(),mMessageTextView.getText().toString());
                    mMessageTextView.setText("");
                    mTalksList.add(new Talks("user.png",chat.getUserName(),chat.getMessage()));
                    TalksAdapter talksAdapter = new TalksAdapter(mTalksList);
                    mCommentsRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                    mCommentsRecyclerView.setAdapter(talksAdapter);
                    mCommentsRecyclerView.smoothScrollToPosition(talksAdapter.getItemCount()-1);
                    SendComments sendComments = new SendComments();
                    sendComments.execute(getString(R.string.ip_server_android) + "SendComments.php",mSession.getIdNumber(), mOnlineBook.getId(),chat.getMessage());
                }
            }
        });
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        mMediaPlayer.release();
        mTimer.cancel();
        mMediaPlayer = null;
    }

    private class RecoveryBook extends AsyncTask<String,Void,String> {
        @Override
        protected String doInBackground(String... params) {

            try {
                OkHttpClient client = new OkHttpClient();
                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("idNumber",params[1])
                        .addFormDataPart("idBook",params[2])
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
                mNoConnectionRecyclerView.setVisibility(View.GONE);
                mNestedScrollView.setVisibility(View.VISIBLE);
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(jsonData);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
                try {
                    mOnlineBook.setCover(jsonObject.getString("bookBlanket"));
                    mOnlineBook.setTitle(jsonObject.getString("bookTitle"));
                    mOnlineBook.setIsPhysic(jsonObject.getString("isPhysic"));
                    mOnlineBook.setIsAudio(jsonObject.getString("isAudio"));
                    mOnlineBook.setElectronic(jsonObject.getString("electronic"));
                    mOnlineBook.setDescription(jsonObject.getString("description"));
                    mOnlineBook.setCategory(jsonObject.getString("categoryTitle"));
                    mOnlineBook.setIsAvailable(jsonObject.getString("available"));
                    mOnlineBook.setNumberLikes(Integer.parseInt(jsonObject.getString("numberLike")));
                    mOnlineBook.setNumberNoLikes(Integer.parseInt(jsonObject.getString("numberNoLike")));
                    mOnlineBook.setNumberSubscribe(Integer.parseInt(jsonObject.getString("numberSubscribe")));
                    mCategory = new Category(jsonObject.getString("categoryBlanket"),jsonObject.getString("categoryTitle"));
                    mAuthor = new Author(jsonObject.getString("idAuthor"),jsonObject.getString("name"),jsonObject.getString("firstName"),jsonObject.getString("profile"));
                    Picasso.get()
                            .load(getString(R.string.ip_server) + "ressources/cover/" + mOnlineBook.getCover())
                            .placeholder(R.drawable.img_default_book)
                            .error(R.drawable.img_default_book)
                            .transform(new RoundedTransformation(15,4))
                            .resize(270,404)
                            .into(mBlanketImageView);
                    mNumberLikeTextView.setText(String.valueOf(mOnlineBook.getNumberLikes()));
                    mNumberNoLikeTextView.setText(String.valueOf(mOnlineBook.getNumberNoLikes()));
                    mNumberSubscribeTextView.setText(String.valueOf(mOnlineBook.getNumberSubscribe()));
                    if(mOnlineBook.getIsPhysic().equals("1"))
                    {
                        mReservationLinearLayout.setVisibility(View.VISIBLE);
                        if(mOnlineBook.getIsAvailable().equals("0"))
                        {
                            mReservationButton.setText("En cours de consultation");
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                mReservationButton.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.whiteSombre)));
                                mReservationButton.setEnabled(false);
                            }
                        }
                    }
                    if(mOnlineBook.getIsAudio().equals("1"))
                        mAudioLinearLayout.setVisibility(View.VISIBLE);
                    if(!mOnlineBook.getElectronic().equals("null"))
                        mElectronicLinearLayout.setVisibility(View.VISIBLE);
                    mTitleTextView.setText(mOnlineBook.getTitle());
                    mNameAuthor.setText("De " + jsonObject.getString("name") + " " + jsonObject.getString("firstName"));
                    mCote.setText("Cote : " + mOnlineBook.getId());
                    mCategoryTextView.setText("Catégorie : " + mOnlineBook.getCategory());
                    mDescriptionTextView.setText(mOnlineBook.getDescription());
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
            else
            {
                mNestedScrollView.setVisibility(View.GONE);
                mNoConnectionRecyclerView.setVisibility(View.VISIBLE);
                ArrayList<Connection> list = new ArrayList<>();
                list.add(new Connection(getString(R.string.no_connection_available),"BOOK_ACTIVITY",false));
                NoConnectionAdapter noConnectionAdapter = new NoConnectionAdapter(list);
                mNoConnectionRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                mNoConnectionRecyclerView.setAdapter(noConnectionAdapter);
            }
        }
    }

    private class ReceiveComments extends AsyncTask<String,Void,String> {
        @Override
        protected String doInBackground(String... params) {

            try {
                OkHttpClient client = new OkHttpClient();
                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("idNumber",params[1])
                        .addFormDataPart("idBook",params[2])
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
                            mTalksList.add(new Talks(jsonArray.getJSONObject(i).getString("profile"),jsonArray.getJSONObject(i).getString("name") + " " + jsonArray.getJSONObject(i).getString("firstName"),jsonArray.getJSONObject(i).getString("message")));
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    TalksAdapter talksAdapter = new TalksAdapter(mTalksList);
                    mCommentsRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                    mCommentsRecyclerView.setAdapter(talksAdapter);
                }
            }
        }
    }

    private class InsertLikeSyn extends AsyncTask<String,Void,String> {
        @Override
        protected String doInBackground(String... params) {

            try {
                OkHttpClient client = new OkHttpClient();
                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("idNumber",params[1])
                        .addFormDataPart("idBook",params[2])
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
            //Toast.makeText(NotificationService.this, response, Toast.LENGTH_SHORT).show();
            if(jsonData != null)
            {
                if(!jsonData.equals("RAS"))
                {
                    if(jsonData.equals("true"))
                    {
                        mOnlineBook.like();
                        mLikeImageView.setImageResource(R.drawable.vector_purple2_200_on_like);
                        mNoLikeImageView.setImageResource(R.drawable.vector_black3_off_no_like);
                    }
                    else
                    {
                        mOnlineBook.disLike();
                        mLikeImageView.setImageResource(R.drawable.vector_black3_off_like);
                    }
                    mNumberLikeTextView.setText(String.valueOf(mOnlineBook.getNumberLikes()));
                }
            }
        }
    }

    private class InsertNoLikeSyn extends AsyncTask<String,Void,String> {
        @Override
        protected String doInBackground(String... params) {

            try {
                OkHttpClient client = new OkHttpClient();
                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("idNumber",params[1])
                        .addFormDataPart("idBook",params[2])
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
            //Toast.makeText(NotificationService.this, response, Toast.LENGTH_SHORT).show();
            if(jsonData != null)
            {
                if(!jsonData.equals("RAS"))
                {
                    if(jsonData.equals("true"))
                    {
                        mOnlineBook.noLike();
                        mNoLikeImageView.setImageResource(R.drawable.vector_rouge_on_nolike);
                        mLikeImageView.setImageResource(R.drawable.vector_black3_off_like);
                    }
                    else
                    {
                        mOnlineBook.disNoLike();
                        mNoLikeImageView.setImageResource(R.drawable.vector_black3_off_no_like);
                    }
                    mNumberNoLikeTextView.setText(String.valueOf(mOnlineBook.getNumberNoLikes()));
                }
            }
        }
    }

    private class InsertSubscribeBookSyn extends AsyncTask<String,Void,String> {
        @Override
        protected String doInBackground(String... params) {

            try {
                OkHttpClient client = new OkHttpClient();
                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("idNumber",params[1])
                        .addFormDataPart("idBook",params[2])
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
                if(!jsonData.equals("RAS"))
                {
                    if(jsonData.equals("true"))
                    {
                        mOnlineBook.subscribe();
                        mSubscribeImageView.setImageResource(R.drawable.vector_purple2_200_suscribe);
                    }
                    else
                    {
                        mOnlineBook.desSubscribe();
                        mSubscribeImageView.setImageResource(R.drawable.vector_black3_off_subscribe);
                    }
                    mNumberSubscribeTextView.setText(String.valueOf(mOnlineBook.getNumberSubscribe()));
                }
            }
        }
    }

    private class InsertViewSyn extends AsyncTask<String,Void,String> {
        @Override
        protected String doInBackground(String... params) {

            try {
                OkHttpClient client = new OkHttpClient();
                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("idNumber",params[1])
                        .addFormDataPart("idBook",params[2])
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

        }
    }

    private class IsNoLikeSyn extends AsyncTask<String,Void,String> {
        @Override
        protected String doInBackground(String... params) {

            try {
                OkHttpClient client = new OkHttpClient();
                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("idNumber",params[1])
                        .addFormDataPart("idBook",params[2])
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
            //Toast.makeText(NotificationService.this, response, Toast.LENGTH_SHORT).show();
            if(jsonData != null)
            {
                if(!jsonData.equals("RAS"))
                {
                    if(jsonData.equals(mSession.getIdNumber()))
                        mNoLikeImageView.setImageResource(R.drawable.vector_rouge_on_nolike);
                    else
                        mNoLikeImageView.setImageResource(R.drawable.vector_black3_off_no_like);
                }
            }
        }
    }

    private class IsLikeSyn extends AsyncTask<String,Void,String> {
        @Override
        protected String doInBackground(String... params) {

            try {
                OkHttpClient client = new OkHttpClient();
                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("idNumber",params[1])
                        .addFormDataPart("idBook",params[2])
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
            //Toast.makeText(NotificationService.this, response, Toast.LENGTH_SHORT).show();
            if(jsonData != null)
            {
                if(!jsonData.equals("RAS"))
                {
                    if(jsonData.equals(mSession.getIdNumber()))
                        mLikeImageView.setImageResource(R.drawable.vector_purple2_200_on_like);
                    else
                        mLikeImageView.setImageResource(R.drawable.vector_black3_off_like);
                }
            }
        }
    }

    private class IsReservationSyn extends AsyncTask<String,Void,String> {
        @Override
        protected String doInBackground(String... params) {

            try {
                OkHttpClient client = new OkHttpClient();
                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("idNumber",params[1])
                        .addFormDataPart("idBook",params[2])
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
            //Toast.makeText(NotificationService.this, response, Toast.LENGTH_SHORT).show();
            if(jsonData != null)
            {
                if(!jsonData.equals("ras"))
                {
                    JSONObject jsonObject = null;
                    try {
                        jsonObject = new JSONObject(jsonData);
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                    try {
                        if(jsonObject.getString("state").equals("1") && jsonObject.getString("treat").equals("1"))
                        {
                            mReservationButton.setText(R.string.cancel_reservation);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                mReservationButton.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.rouge)));
                            }
                        }
                        else
                        {
                            if(jsonObject.getString("state").equals("2") && jsonObject.getString("treat").equals("1"))
                            {
                                mReservationButton.setText("En cours de consultation");
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    mReservationButton.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.whiteSombre)));
                                    mReservationButton.setEnabled(false);
                                }
                            }
                        }
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }

    private class IsSubscribeBookSyn extends AsyncTask<String,Void,String> {
        @Override
        protected String doInBackground(String... params) {

            try {
                OkHttpClient client = new OkHttpClient();
                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("idNumber",params[1])
                        .addFormDataPart("idBook",params[2])
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
            //Toast.makeText(NotificationService.this, response, Toast.LENGTH_SHORT).show();
            if(jsonData != null)
            {
                if(!jsonData.equals("RAS"))
                {
                    if(jsonData.equals(mSession.getIdNumber()))
                        mSubscribeImageView.setImageResource(R.drawable.vector_purple2_200_suscribe);
                    else
                        mSubscribeImageView.setImageResource(R.drawable.vector_black3_off_subscribe);
                }
            }
        }
    }

    private class CancelReservationSyn extends AsyncTask<String,Void,String> {
        @Override
        protected String doInBackground(String... params) {

            try {
                OkHttpClient client = new OkHttpClient();
                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("idNumber",params[1])
                        .addFormDataPart("idBook",params[2])
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
            //Toast.makeText(NotificationService.this, response, Toast.LENGTH_SHORT).show();
            if(jsonData != null)
            {
                if(!jsonData.equals("RAS"))
                {
                    if(jsonData.equals("true"))
                    {
                        mReservationButton.setText(R.string.reservation_book);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            mReservationButton.setBackgroundTintList(getColorStateList(R.color.black3));
                        }
                    }
                }
            }
        }
    }

    private class RecoveryTones extends AsyncTask<String,Void,String> {
        @Override
        protected String doInBackground(String... params) {

            try {
                OkHttpClient client = new OkHttpClient();
                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("idNumber",mSession.getIdNumber())
                        .addFormDataPart("idBook", mOnlineBook.getId())
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
                            mListTones.add(new Tones(i+1,jsonArray.getJSONObject(i).getString("audio"),jsonArray.getJSONObject(i).getString("title"),0,false));
                            if(i==0)
                            {
                                mTones = new Tones(0,jsonArray.getJSONObject(i).getString("audio"));
                            }
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    url = getString(R.string.ip_server) + "ressources/audio/" + mTones.getAudio();
                    try {
                        mMediaPlayer.setDataSource(url);
                        mMediaPlayer.prepare();
                        mSeekBar.setMax(mMediaPlayer.getDuration());
                        mTones.setDuration(convertedDurationToString(mMediaPlayer.getDuration()));
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                while (mMediaPlayer != null) {
                                    try {
                                        Thread.sleep(1000);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                    mHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (mMediaPlayer != null && mMediaPlayer.isPlaying())
                                            {
                                                int currentTime = mMediaPlayer.getCurrentPosition();
                                                mSeekBar.setProgress(currentTime);
                                                mTimeNowTextView.setText(convertedDurationToString(currentTime));
                                            }
                                        }
                                    });
                                }
                            }
                        }).start();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }

    public void ReservationDialog() {
        Spinner timeLimitSpinner = mReservationDialog.findViewById(R.id.spinner_dialog_reservation_time_limit);
        CheckBox LocalConsultationCheckBox = mReservationDialog.findViewById(R.id.check_box_dialog_reservation_local_consultation);
        Button sendButton = mReservationDialog.findViewById(R.id.button_dialog_reservation_send);
        EditText passwordEditText = mReservationDialog.findViewById(R.id.edit_text_dialog_reservation_password);
        TextView errorTextView = mReservationDialog.findViewById(R.id.text_view_dialog_reservation_error);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.delait_reservation, android.R.layout.simple_spinner_item);
        ArrayAdapter<CharSequence> adapterConsult = ArrayAdapter.createFromResource(this, R.array.delait_cosultation, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        adapterConsult.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        timeLimitSpinner.setAdapter(adapter);
        LocalConsultationCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                timeLimitSpinner.setEnabled(!LocalConsultationCheckBox.isChecked());
            }
        });
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(passwordEditText.getText().toString().equals(""))
                {
                    errorTextView.setText(R.string.edit_text_hint_password);
                    passwordEditText.setBackground(getDrawable(R.drawable.forme_white_radius_100dp_border_rouge));
                }
                else
                {
                    if(!passwordEditText.getText().toString().equals(mSession.getPassword()))
                    {
                        errorTextView.setText(R.string.incorrect_password);
                        passwordEditText.setBackground(getDrawable(R.drawable.forme_white_radius_100dp_border_rouge));
                    }
                    else
                    {
                        if(timeLimitSpinner.isEnabled())
                            mNbrJour = String.valueOf(timeLimitSpinner.getSelectedItemPosition() + 1);
                        else
                            mNbrJour = String.valueOf(-1);
                        Reservation reservation = new Reservation();
                        reservation.execute(getString(R.string.ip_server_android) + "Reservation.php",mSession.getIdNumber(), mOnlineBook.getId(),mNbrJour);
                    }
                }
            }
        });
        mReservationDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mReservationDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        mReservationDialog.build();
    }
    private void simpleDialogOk(int ico , String title , String message){
        SimpleOkDialog simpleOkDialog = new SimpleOkDialog(this);
        Objects.requireNonNull(simpleOkDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        simpleOkDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        ImageView icoImageView = simpleOkDialog.findViewById(R.id.image_view_dialog_simple_ok_icon);
        TextView titleTextView = simpleOkDialog.findViewById(R.id.text_view_dialog_simple_ok_title);
        TextView messageTextView = simpleOkDialog.findViewById(R.id.text_view_dialog_simple_ok_message);
        TextView okTextView = simpleOkDialog.findViewById(R.id.text_view_dialog_simple_ok);
        icoImageView.setImageResource(ico);
        titleTextView.setText(title);
        messageTextView.setText(message);
        okTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                simpleOkDialog.cancel();
            }
        });
        simpleOkDialog.build();
    }
    private class Reservation extends AsyncTask<String,Void,String> {
        @Override
        protected String doInBackground(String... params) {

            try {
                OkHttpClient client = new OkHttpClient();
                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("idNumber",params[1])
                        .addFormDataPart("idBook",params[2])
                        .addFormDataPart("numberOfDay",params[3])
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
                if(jsonData.equals("true"))
                {
                    mReservationDialog.cancel();
                    successReservationDialog("Merci d'avoir réservé \"" + mTitleTextView.getText().toString() + "\" sur fabi; nous traitons votre demande et vous confirmerons la disponibilité bientôt.");
                    mReservationButton.setText(R.string.cancel_reservation);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        mReservationButton.setBackgroundTintList(getColorStateList(R.color.rouge));
                    }
                }
            }

        }
    }

    private void successReservationDialog(String message){
        SimpleOkDialog simpleOkDialog = new SimpleOkDialog(this);
        simpleOkDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        simpleOkDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        TextView messageTextView = simpleOkDialog.findViewById(R.id.text_view_dialog_simple_ok_message);
        TextView okTextView = simpleOkDialog.findViewById(R.id.text_view_dialog_simple_ok);
        messageTextView.setText(message);
        okTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                mSuggestion.setText("");
                simpleOkDialog.cancel();
            }
        });
        simpleOkDialog.build();
    }

    private void succeDowloadPDFDialog(String message){
        SimpleOkDialog simpleOkDialog = new SimpleOkDialog(this);
        simpleOkDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        simpleOkDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        TextView messageTextView = simpleOkDialog.findViewById(R.id.text_view_dialog_simple_ok_message);
        TextView ok = simpleOkDialog.findViewById(R.id.text_view_dialog_simple_ok);
        messageTextView.setText(message);
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                downloadPDFButton.setText("Format PDF");
                downloadPdfProgressBar.setVisibility(View.INVISIBLE);
                simpleOkDialog.cancel();
            }
        });
        simpleOkDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                downloadPDFButton.setText("Format PDF");
                downloadPdfProgressBar.setVisibility(View.INVISIBLE);
                simpleOkDialog.cancel();
            }
        });
        simpleOkDialog.build();
    }
    private void succeDowloadAudioDialog(String message){
        SimpleOkDialog simpleOkDialog = new SimpleOkDialog(this);
        simpleOkDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        simpleOkDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        TextView messageTextView = simpleOkDialog.findViewById(R.id.text_view_dialog_simple_ok_message);
        TextView ok = simpleOkDialog.findViewById(R.id.text_view_dialog_simple_ok);
        messageTextView.setText(message);
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                audioButton.setText("Format Audio");
                downloadAudioProgressBar.setVisibility(View.INVISIBLE);
                simpleOkDialog.cancel();
            }
        });
        simpleOkDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                audioButton.setText("Format Audio");
                downloadAudioProgressBar.setVisibility(View.INVISIBLE);
                simpleOkDialog.cancel();
            }
        });
        simpleOkDialog.build();
    }
    public String convertedDurationToString(int duration)
    {
        return String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(duration),
                TimeUnit.MILLISECONDS.toSeconds(duration) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration)));
    }



    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Channel Progress";
            String description = "Channel for progress notification";
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        } else {
            notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }
    }

    private void showProgressNotification(String title) {
        final NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_fabi)
                .setContentTitle(title)
                .setContentText("Téléchargement en cours")
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setProgress(100, 0, false);

        if (notificationManager != null) {
            notificationManager.notify(notificationId, builder.build());
        }

        // Simuler une progression
        Handler handler = new Handler(Looper.getMainLooper());
        new Thread(() -> {
            for (int progress = 0; progress <= 100; progress += 10) {
                int finalProgress = progress;
                handler.post(() -> {
                    builder.setProgress(100, finalProgress, false);
                    if (notificationManager != null) {
                        notificationManager.notify(notificationId, builder.build());
                    }
                });

                try {
                    Thread.sleep(500); // Pause de 500 ms entre chaque progression
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            // Notification terminée
            handler.post(() -> {
                builder.setContentText("Téléchargement terminé")
                        .setProgress(100, 0, false);
                if (notificationManager != null) {
                    notificationManager.notify(notificationId, builder.build());
                }
            });
        }).start();
    }
    private LinearLayout mReservationLinearLayout;
    private LinearLayout mAudioLinearLayout;
    private LinearLayout mElectronicLinearLayout;
    private RecyclerView mCommentsRecyclerView;
    private ArrayList<Talks> mTalksList;

    private RecyclerView mSimilarRecyclerView;
    private RecyclerView mTonesRecyclerView;
    private RecyclerView mNoConnectionRecyclerView;
    private TonesAdapter mTonesAdapter;
    private List<Tones> mListTones;
    private Tones mTones;
    private List<SimilarBook> mListSimilar;
    private Session mSession;
    private ImageView mBlanketImageView;
    private TextView mTitleTextView;
    private TextView mCategoryTextView;
    private TextView mDescriptionTextView;
    private TextView mNumberLikeTextView;
    private TextView mNumberNoLikeTextView;
    private TextView mNumberSubscribeTextView;
    private TextView mTimeNowTextView;
    private TextView mNameAuthor;
    private TextView mCote;
    private EditText mMessageTextView;
    private ImageView mLikeImageView;
    private ImageView mNoLikeImageView;
    private ImageView mSubscribeImageView;
    private Button mReservationButton;
    private ImageView mPlayerImageView;
    private MediaPlayer mMediaPlayer;
    private SeekBar mSeekBar;
    private Timer mTimer;
    private RelativeLayout mRelativeLayout;
    private String url;
    private String mAudio;
    private ReservationDialog mReservationDialog;
    private ElectronicTable mElectronicTable;
    private String mNbrJour;
    private OnlineBook mOnlineBook;
    private Category mCategory;
    private Handler mHandler;
    private NestedScrollView mNestedScrollView;
    private NoConnectionAdapter mNoConnectionAdapter;
    private Author mAuthor;
    private ImageView mBackImageView;
    private ProgressBar downloadAudioProgressBar;
    private ProgressBar downloadPdfProgressBar;
    private Button downloadPDFButton;
    private Button audioButton;

    private static final String CHANNEL_ID = "progress_channel";
    private NotificationManager notificationManager;
    private int notificationId = 1;
}