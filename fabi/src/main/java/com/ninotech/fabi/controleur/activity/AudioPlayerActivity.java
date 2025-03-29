package com.ninotech.fabi.controleur.activity;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.audiofx.Equalizer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.ninotech.fabi.Playable;
import com.ninotech.fabi.R;
import com.ninotech.fabi.controleur.adapter.NoConnectionAdapter;
import com.ninotech.fabi.controleur.animation.RoundedTransformation;
import com.ninotech.fabi.controleur.custo.StatusBarCusto;
import com.ninotech.fabi.controleur.fragment.HomeFragment;
import com.ninotech.fabi.model.data.Connection;
import com.ninotech.fabi.model.data.CreateNotification;
import com.ninotech.fabi.model.data.Track;
import com.ninotech.fabi.model.service.OnClearFromRecentService;
import com.ninotech.fabi.model.table.AudioTable;
import com.ninotech.fabi.model.table.Session;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class AudioPlayerActivity extends AppCompatActivity implements Playable {

    private static final String CHANNEL_ID = "AUDIO_PLAYER_CHANNEL";
    private static final int NOTIFICATION_ID = 1;
    private static final int REQUEST_CODE_PASS = 1 ;

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
    private Intent audioBookIntent;
    private String mListSource;

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_player);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        Objects.requireNonNull(getSupportActionBar()).hide();
        StatusBarCusto statusBarCusto = new StatusBarCusto(this,getWindow());
        audioBookIntent = getIntent();
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
        mTonesImageView = findViewById(R.id.image_view_activity_audio_player_tones);
        mPlayListImageView = findViewById(R.id.image_view_activity_audio_player_list);
        mLoveImageView = findViewById(R.id.image_view_activity_audio_player_love);
        mAddImageView = findViewById(R.id.image_view_activity_audio_player_add);
        mRandomImageView = findViewById(R.id.image_view_activity_audio_player_random);
        mBackPlayImageView = findViewById(R.id.image_view_activity_audio_player_back_player);
        mNextPlayImageView = findViewById(R.id.image_view_activity_audio_player_next_play);
        mAutoPlayImageView = findViewById(R.id.image_view_activity_audio_player_auto_play);
        mHandler = new Handler();
        BroadcastReceiver receiverNoConnectionAdapter = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if ("SELECT_LIST_PLAYER".equals(intent.getAction())) {
                    Toast.makeText(context,String.valueOf(intent.getIntExtra("position",0)), Toast.LENGTH_SHORT).show();
                    onTrackPlayPosition(intent.getIntExtra("position",0));
                }
            }
        };
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            registerReceiver(receiverNoConnectionAdapter, new IntentFilter("SELECT_LIST_PLAYER"),Context.RECEIVER_EXPORTED);
        }

        String idBook = audioBookIntent.getStringExtra("key_adapter_audio_book_id");
        mListSource = audioBookIntent.getStringExtra("list_audio_source");
        assert mListSource != null;
        popluateTracks(idBook, mListSource);
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
        setRessourceBook();
        mMediaPlayer = new MediaPlayer();
        try {
            mMediaPlayer.setDataSource(mTracks.get(position).getAudio());
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        try {
            mMediaPlayer.prepare();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        mSeekBar.setMax(mMediaPlayer.getDuration());
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ActivityCompat.requestPermissions(AudioPlayerActivity.this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }
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
                                    mDurationTotalTextView.setText(convertedDurationToString(mMediaPlayer.getDuration()-currentTime));
                                    if ((mMediaPlayer.getDuration()-3000) <= currentTime)
                                        onTrackNext();
                                }
                            }
                        });
                    }
                }
            }).start();
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
        mBackPlayImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onTrackPrevious();
            }
        });
        mNextPlayImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onTrackNext();
            }
        });
        mReplayImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMediaPlayer.seekTo(0);
            }
        });

        mVolumeImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
                if (audioManager != null) {
                    // Afficher la barre de volume
                    audioManager.adjustVolume(AudioManager.ADJUST_SAME, AudioManager.FLAG_SHOW_UI);
                }
            }
        });

        mTonesImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent intent = new Intent(Intent.ACTION_MAIN);
                    intent.setClassName("com.android.settings", "com.android.settings.SoundSettings");
                    startActivity(intent);
                } catch (Exception e) {
                    // Si l'application native d'égaliseur n'est pas disponible, on peut lancer un intent standard
                    Intent intent = new Intent(Equalizer.ACTION_OPEN_AUDIO_EFFECT_CONTROL_SESSION);
                    intent.putExtra(Equalizer.EXTRA_AUDIO_SESSION, 0);
                    intent.putExtra(Equalizer.EXTRA_PACKAGE_NAME, getPackageName());
                    startActivity(intent);
                }
            }
        });

        mPlayListImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(AudioPlayerActivity.this, "Player Liste", Toast.LENGTH_SHORT).show();
                Intent local = new Intent(AudioPlayerActivity.this, ListPlayerActivity.class);
                local.putExtra("id",6);
                local.putExtra("audio",mTracks.get(position).getAudio());
                local.putExtra("list_audio_source",mListSource);
                local.putExtra("type",audioBookIntent.getStringExtra("type"));
                startActivity(local);
            }
        });

        mLoveImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(AudioPlayerActivity.this, "Favori", Toast.LENGTH_SHORT).show();
            }
        });

        mAddImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(AudioPlayerActivity.this, "ADD", Toast.LENGTH_SHORT).show();
            }
        });

        mRandomImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(AudioPlayerActivity.this, "Random", Toast.LENGTH_SHORT).show();
            }
        });

        mAutoPlayImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(AudioPlayerActivity.this, "Auto", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void setRessourceBook() {
        mTitleTextView.setText(mTracks.get(position).getTitle());
        mAuthorTextView.setText(mTracks.get(position).getArtist());
        mDurationTotalTextView.setText(mTracks.get(position).getTime());
        File file = new File(mTracks.get(position).getCover());
        Picasso.get().load(file)
                .placeholder(R.drawable.img_wait_cover_book)
                .error(R.drawable.img_wait_cover_book)
                .transform(new RoundedTransformation(15,4))
                .resize(356,568)
                .into(mCoverImageView);
    }

    private String convertedDurationToString(int duration) {
        return String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(duration),
                TimeUnit.MILLISECONDS.toSeconds(duration) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration)));
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
    private void popluateTracks(String idBook , String listSource){
        AudioTable audioTable = new AudioTable(this);
        mTracks = new ArrayList<>();
        Cursor audioCursor = null;
        switch (listSource)
        {
            case "all":
                audioCursor = audioTable.getData(mSession.getIdNumber());
                break;
            case "category":
                audioCursor = audioTable.getDataC(mSession.getIdNumber(),audioBookIntent.getStringExtra("type"));
                break;
            case "author":
                audioCursor = audioTable.getDataA(mSession.getIdNumber(),audioBookIntent.getStringExtra("type"));
                break;
        }
        assert audioCursor != null;
        audioCursor.moveToFirst();
        do {
            mTracks.add(new Track(audioCursor.getString(5),audioCursor.getString(8),audioCursor.getString(4),audioCursor.getString(6),audioCursor.getString(11),R.id.relative_layout_activity_declaration_img));
            if (audioCursor.getString(2).equals(idBook))
                position = mTracks.size()-1;
        }while (audioCursor.moveToNext());
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
                case CreateNotification.ACTION_PREVIOUS:
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
        mMediaPlayer.reset();
        if(!(position == 0))
            position--;
        else
            position=(mTracks.size() - 1);
        setRessourceBook();
        try {
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setDataSource(mTracks.get(position).getAudio());
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        try {
            mMediaPlayer.prepare();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        mSeekBar.setMax(mMediaPlayer.getDuration());
        onTrackPlay();
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
        mMediaPlayer.reset();
        if(!(position == mTracks.size() - 1))
            position++;
        else
            position=0;
        setRessourceBook();
        try {
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setDataSource(mTracks.get(position).getAudio());
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        try {
            mMediaPlayer.prepare();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        mSeekBar.setMax(mMediaPlayer.getDuration());
        onTrackPlay();
        CreateNotification.createNotification(AudioPlayerActivity.this,mTracks.get(position),
                R.drawable.vector_black3_play,position,mTracks.size()-1);
    }
    public void onTrackPlayPosition(int posi) {
        mMediaPlayer.reset();
        position = posi;
        setRessourceBook();
        try {
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setDataSource(mTracks.get(position).getAudio());
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        try {
            mMediaPlayer.prepare();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        mSeekBar.setMax(mMediaPlayer.getDuration());
        onTrackPlay();
        CreateNotification.createNotification(AudioPlayerActivity.this,mTracks.get(position),
                R.drawable.vector_black3_play,position,mTracks.size()-1);
    }
}