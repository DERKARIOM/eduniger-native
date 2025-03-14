package com.ninotech.fabi.controleur.activity;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
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
import com.ninotech.fabi.controleur.animation.RoundedTransformation;
import com.ninotech.fabi.controleur.custo.StatusBarCusto;
import com.ninotech.fabi.model.data.CreateNotification;
import com.ninotech.fabi.model.data.Track;
import com.ninotech.fabi.model.service.OnClearFromRecentService;
import com.ninotech.fabi.model.table.AudioTable;
import com.ninotech.fabi.model.table.Session;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class AudioPlayerActivity extends AppCompatActivity implements Playable {

    private static final String CHANNEL_ID = "AUDIO_PLAYER_CHANNEL";
    private static final int NOTIFICATION_ID = 1;

    private NotificationManager notificationManager;
    private Session mSession;
    private TextView mTitleTextView;
    private TextView mAuthorTextView;
    private ImageView mCoverImageView;
    private TextView mDurationTotalTextView;
    private TextView mDurationCurrentTextView;
    private MediaPlayer mMediaPlayer;
    private ImageView mPlayImageView;
    private SeekBar mSeekBar;
    private Handler mHandler;
    private ImageView mBackImageView;
    private ImageView mReplayImageView;
    private ImageView mVolumeImageView;
    private ImageView mTonesImageView;
    private ImageView mPlayListImageView;
    private ImageView mLoveImageView;
    private ImageView mAddImageView;
    private ImageView mRandomImageView;
    private ImageView mBackPlayImageView;
    private ImageView mNextPlayImageView;
    private ImageView mAutoPlayImageView;
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
        StatusBarCusto statusBarCusto = new StatusBarCusto(this,getWindow());
        Intent audioBookIntent = getIntent();
        mSession = new Session(this);
        mTitleTextView = findViewById(R.id.text_view_activity_audio_player_title);
        mAuthorTextView = findViewById(R.id.text_view_activity_audio_player_author);
        mDurationTotalTextView = findViewById(R.id.text_view_activity_audio_player_duration_total);
        mDurationCurrentTextView = findViewById(R.id.text_view_activity_audio_player_duration_current);
        mCoverImageView = findViewById(R.id.image_view_activity_audio_player_cover);
        mPlayImageView = findViewById(R.id.image_view_activity_audio_player_play);
        mReplayImageView = findViewById(R.id.image_view_activity_audio_player_replay);
        mVolumeImageView = findViewById(R.id.image_view_activity_audio_player_volume);
        mSeekBar = findViewById(R.id.seek_bar_activity_audio_player);
        mBackImageView = findViewById(R.id.image_view_activity_audio_player_back);
        mTonesImageView = findViewById(R.id.image_view_activity_audio_player_volume);
        mPlayListImageView = findViewById(R.id.image_view_activity_audio_player_list);
        mLoveImageView = findViewById(R.id.image_view_activity_audio_player_love);
        mAddImageView = findViewById(R.id.image_view_activity_audio_player_add);
        mRandomImageView = findViewById(R.id.image_view_activity_audio_player_random);
        mBackPlayImageView = findViewById(R.id.image_view_activity_audio_player_back_player);
        mNextPlayImageView = findViewById(R.id.image_view_activity_audio_player_next_play);
        mAutoPlayImageView = findViewById(R.id.image_view_activity_audio_player_auto_play);
        mHandler = new Handler();
        popluateTracks();
        String idBook = audioBookIntent.getStringExtra("key_adapter_audio_book_id");
        AudioTable audioTable = new AudioTable(this);
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
        mBackImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        try {
            Cursor audioCursor = audioTable.getData(mSession.getIdNumber(),idBook);
            audioCursor.moveToFirst();
            mTitleTextView.setText(audioCursor.getString(8));
            mAuthorTextView.setText(audioCursor.getString(4));
            mDurationTotalTextView.setText(audioCursor.getString(11));
            File file = new File(audioCursor.getString(5));
            Picasso.get().load(file)
                    .placeholder(R.drawable.img_default_book)
                    .error(R.drawable.img_default_book)
                    .transform(new RoundedTransformation(15,4))
                    .resize(280,330)
                    .into(mCoverImageView);
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setDataSource(audioCursor.getString(6));
            mMediaPlayer.prepare();
            mSeekBar.setMax(mMediaPlayer.getDuration());
            onTrackPlay();

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
                                    mDurationCurrentTextView.setText(convertedDurationToString(currentTime));
                                }
                            }
                        });
                    }
                }
            }).start();
        }catch (Exception e)
        {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e("ErrorAudioCursor", Objects.requireNonNull(e.getMessage()));
        }

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
            }
        });
        mReplayImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMediaPlayer.seekTo(0);
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
                    onTrackNext();
                    break;
                case CreateNotification.ACTION_PLAY:
                    if (isPlaying)
                        onTrackPause();
                    else
                        onTrackPlay();
                    break;
            }
        }
    };
    @Override
    public void onTrackPrevious() {
        position--;
        CreateNotification.createNotification(AudioPlayerActivity.this,mTracks.get(position),
                R.drawable.vector_black3_play,position,mTracks.size()-1);
    }

    @Override
    public void onTrackPlay() {
        CreateNotification.createNotification(AudioPlayerActivity.this,mTracks.get(position),
                R.drawable.vector_black3_play,position,mTracks.size()-1);
        mPlayImageView.setImageResource(R.drawable.vector_black3_play);
        mMediaPlayer.start();
        isPlaying = true;
    }

    @Override
    public void onTrackPause() {
        CreateNotification.createNotification(AudioPlayerActivity.this,mTracks.get(position),
                R.drawable.vector_black3_pause,position,mTracks.size()-1);
        mPlayImageView.setImageResource(R.drawable.vector_black3_pause);
        mMediaPlayer.pause();
        isPlaying = false;
    }

    @Override
    public void onTrackNext() {
        position++;
        CreateNotification.createNotification(AudioPlayerActivity.this,mTracks.get(position),
                R.drawable.vector_black3_play,position,mTracks.size()-1);
    }
}