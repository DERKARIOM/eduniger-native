package com.ninotech.fabi.controleur.activity;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.NotificationCompat;

import com.ninotech.fabi.Playable;
import com.ninotech.fabi.R;
import com.ninotech.fabi.model.data.CreateNotification;
import com.ninotech.fabi.model.data.Track;
import com.ninotech.fabi.model.service.OnClearFromRecentService;
import com.ninotech.fabi.model.table.AudioTable;
import com.ninotech.fabi.model.table.Session;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class AudioPlayerActivity extends AppCompatActivity implements Playable {

    private static final String CHANNEL_ID = "AUDIO_PLAYER_CHANNEL";
    private static final int NOTIFICATION_ID = 1;

    private NotificationManager notificationManager;

    private Session mSession;
    private TextView mTitleTextView, mAuthorTextView, mDurationTotalTextView, mDurationCurrentTextView;
    private ImageView mCoverImageView, mPlayImageView, mBackImageView, mReplayImageView;
    private SeekBar mSeekBar;
    private Handler mHandler;
    private MediaPlayer mMediaPlayer;
    private String audioPath;
    private List<Track> mTracks;
    private int position=0;
    private boolean isPlaying = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_player);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        Objects.requireNonNull(getSupportActionBar()).hide();
        mPlayImageView = findViewById(R.id.image_view_activity_audio_player_play);
        popluateTracks();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChanel();
            registerReceiver(broadcastReceiver,new IntentFilter("TRACKS_TRACKS"));
            startService(new Intent(getBaseContext(), OnClearFromRecentService.class));
        }
        mPlayImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPlaying)
                {
                    onTrackPause();
                }else
                    onTrackPlay();
                CreateNotification.createNotification(AudioPlayerActivity.this,mTracks.get(1),R.drawable.vector_black3_pause,1,mTracks.size()-1);
            }
        });
        //initUI();
        //setupAudioPlayer();
    }

    private void initUI() {
        mSession = new Session(this);
        mHandler = new Handler();

        mTitleTextView = findViewById(R.id.text_view_activity_audio_player_title);
        mAuthorTextView = findViewById(R.id.text_view_activity_audio_player_author);
        mDurationTotalTextView = findViewById(R.id.text_view_activity_audio_player_duration_total);
        mDurationCurrentTextView = findViewById(R.id.text_view_activity_audio_player_duration_current);
        mCoverImageView = findViewById(R.id.image_view_activity_audio_player_cover);
        mPlayImageView = findViewById(R.id.image_view_activity_audio_player_play);
        mBackImageView = findViewById(R.id.image_view_activity_audio_player_back);
        mReplayImageView = findViewById(R.id.image_view_activity_audio_player_replay);
        mSeekBar = findViewById(R.id.seek_bar_activity_audio_player);

        mBackImageView.setOnClickListener(v -> onBackPressed());
        mPlayImageView.setOnClickListener(v -> togglePlayPause());
        mReplayImageView.setOnClickListener(v -> replayAudio());

        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && mMediaPlayer != null) {
                    mMediaPlayer.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

    }

    private void setupAudioPlayer() {
        Intent intent = getIntent();
        String idBook = intent.getStringExtra("key_adapter_audio_book_id");
        AudioTable audioTable = new AudioTable(this);

        try {
            Cursor audioCursor = audioTable.getData(mSession.getIdNumber(), idBook);
            if (audioCursor.moveToFirst()) {
                mTitleTextView.setText(audioCursor.getString(8));
                mAuthorTextView.setText(audioCursor.getString(4));
                mDurationTotalTextView.setText(audioCursor.getString(11));
                audioPath = audioCursor.getString(6);

                File coverFile = new File(audioCursor.getString(5));
                Picasso.get().load(coverFile)
                        .placeholder(R.drawable.img_default_book)
                        .error(R.drawable.img_default_book)
                        .resize(280, 330)
                        .into(mCoverImageView);

                initializeMediaPlayer();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Erreur lors du chargement de l'audio", Toast.LENGTH_SHORT).show();
            Log.e("AudioPlayerActivity", "Error: " + e.getMessage());
        }
    }

    private void initializeMediaPlayer() {
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
        }

        mMediaPlayer = new MediaPlayer();
        try {
            mMediaPlayer.setDataSource(audioPath);
            mMediaPlayer.prepare();
            mSeekBar.setMax(mMediaPlayer.getDuration());

            mMediaPlayer.setOnCompletionListener(mp -> stopAudio());
            updateSeekBar();
            startAudio();
        } catch (Exception e) {
            Log.e("MediaPlayerError", "Erreur: " + e.getMessage());
        }
    }
    private void startAudio() {
        if (mMediaPlayer != null && !mMediaPlayer.isPlaying()) {
            mMediaPlayer.start();
            mPlayImageView.setImageResource(R.drawable.vector_black3_pause);
            isPlaying = true;
            updateNotification();
        }
    }

    private void togglePlayPause() {
        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
            mPlayImageView.setImageResource(R.drawable.vector_black3_play);
            isPlaying = false;
        } else {
            mMediaPlayer.start();
            mPlayImageView.setImageResource(R.drawable.vector_black3_pause);
            isPlaying = true;
        }
        updateNotification();
    }

    private void replayAudio() {
        if (mMediaPlayer != null) {
            mMediaPlayer.seekTo(0);
            mMediaPlayer.start();
        }
    }

    private void stopAudio() {
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
            isPlaying = false;
        }
        updateNotification();
    }

    private void updateSeekBar() {
        new Thread(() -> {
            while (mMediaPlayer != null) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                mHandler.post(() -> {
                    if (mMediaPlayer != null) {
                        int currentTime = mMediaPlayer.getCurrentPosition();
                        mSeekBar.setProgress(currentTime);
                        mDurationCurrentTextView.setText(convertedDurationToString(currentTime));
                    }
                });
            }
        }).start();
    }

    private String convertedDurationToString(int duration) {
        return String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(duration),
                TimeUnit.MILLISECONDS.toSeconds(duration) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration)));
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Audio Player", NotificationManager.IMPORTANCE_LOW);
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    private void updateNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_v2)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.img_default_book))
                .setContentTitle(mTitleTextView.getText())
                .setContentText(isPlaying ? "Lecture en cours" : "En pause")
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(isPlaying);

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify(NOTIFICATION_ID, builder.build());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            notificationManager.cancelAll();
        }
        unregisterReceiver(broadcastReceiver);
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }
    private void popluateTracks(){
        mTracks = new ArrayList<>();
        mTracks.add(new Track("Track 1","Artist 1",R.id.relative_layout_activity_declaration_img));
        mTracks.add(new Track("Track 2","Artist 2",R.id.relative_layout_activity_declaration_img));
        mTracks.add(new Track("Track 3","Artist 3",R.id.relative_layout_activity_declaration_img));

    }
    private void createChanel(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CreateNotification.CHANNEL_ID,
                    "Kod Dev",NotificationManager.IMPORTANCE_LOW);
            notificationManager = getSystemService(NotificationManager.class);
            if(notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }


    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getExtras().getString("actionname");
            switch (action)
            {
                case CreateNotification.ACTION_PREVIUOS:
                    onTrackPrevious();
                    break;
                case CreateNotification.ACTION_NEXT:
                    if (isPlaying)
                        onTrackPause();
                    else
                        onTrackPlay();
                    break;
                case CreateNotification.ACTION_PLAY:
                    onTrackNext();
                    break;
            }
        }
    };
    @Override
    public void onTrackPrevious() {
        position--;
        CreateNotification.createNotification(AudioPlayerActivity.this,mTracks.get(position),
                R.drawable.vector_back,position,mTracks.size()-1);
    }

    @Override
    public void onTrackPlay() {
        CreateNotification.createNotification(AudioPlayerActivity.this,mTracks.get(position),
                R.drawable.vector_black3_play,position,mTracks.size()-1);
        mPlayImageView.setImageResource(R.drawable.vector_black3_play);
        isPlaying = true;
    }

    @Override
    public void onTrackPause() {
        CreateNotification.createNotification(AudioPlayerActivity.this,mTracks.get(position),
                R.drawable.vector_black3_pause,position,mTracks.size()-1);
        mPlayImageView.setImageResource(R.drawable.vector_black3_pause);
        isPlaying = false;
    }

    @Override
    public void onTrackNext() {
        position++;
        CreateNotification.createNotification(AudioPlayerActivity.this,mTracks.get(position),
                R.drawable.vector_black3_play,position,mTracks.size()-1);
    }
}