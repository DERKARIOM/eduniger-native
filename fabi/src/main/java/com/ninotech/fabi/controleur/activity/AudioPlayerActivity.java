package com.ninotech.fabi.controleur.activity;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.audiofx.Equalizer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.ninotech.fabi.Playable;
import com.ninotech.fabi.R;
import com.ninotech.fabi.controleur.animation.RoundedTransformation;
import com.ninotech.fabi.model.data.CreateNotification;
import com.ninotech.fabi.model.data.Track;
import com.ninotech.fabi.model.service.OnClearFromRecentService;
import com.ninotech.fabi.model.table.AudioTable;
import com.ninotech.fabi.model.table.Session;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class AudioPlayerActivity extends AppCompatActivity implements Playable {

    private static final String TAG = "AudioPlayerActivity";
    private static final String ACTION_SELECT_PLAYER = "SELECT_LIST_PLAYER";
    private static final String ACTION_TRACKS = "TRACKS_TRACKS";
    private static final String LIST_SOURCE_ALL = "all";
    private static final String LIST_SOURCE_CATEGORY = "category";
    private static final String LIST_SOURCE_AUTHOR = "author";
    private static final int PERMISSION_REQUEST_CODE = 101;
    private static final int AUTO_NEXT_THRESHOLD_MS = 3000;

    // Views
    private TextView mTitleTextView;
    private TextView mAuthorTextView;
    private TextView mDurationTotalTextView;
    private TextView mDurationCurrentTextView;
    private ImageView mCoverImageView;
    private ImageView mPlayImageView;
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
    private SeekBar mSeekBar;

    // Media
    private MediaPlayer mMediaPlayer;
    private Handler mHandler;
    private Thread mUpdateThread;

    // Data
    private List<Track> mTracks;
    private Session mSession;
    private int mPosition = 0;
    private boolean mIsPlaying = false;
    private String mListSource;

    // Notification
    private NotificationManager mNotificationManager;
    private BroadcastReceiver mPlaybackReceiver;
    private BroadcastReceiver mPlaylistReceiver;

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_player);
        Objects.requireNonNull(getSupportActionBar()).hide();

        initializeComponents();
        initializeViews();
        setupSeekBar();
        setupClickListeners();
        loadTracks();
        setupMediaPlayer();
        requestPermissions();
        setupNotifications();
        startPlaybackThread();
    }

    private void initializeComponents() {
        mSession = new Session(this);
        mHandler = new Handler();
        mTracks = new ArrayList<>();

        Intent intent = getIntent();
        String idBook = intent.getStringExtra("key_adapter_audio_book_id");
        mListSource = intent.getStringExtra("list_audio_source");

        populateTracks(idBook, mListSource);
    }

    private void initializeViews() {
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

        updateTrackInfo();
    }

    private void setupSeekBar() {
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && mMediaPlayer != null) {
                    mMediaPlayer.seekTo(progress);
                }
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void setupClickListeners() {
        mBackImageView.setOnClickListener(v -> onBackPressed());
        mPlayImageView.setOnClickListener(v -> togglePlayPause());
        mBackPlayImageView.setOnClickListener(v -> onTrackPrevious());
        mNextPlayImageView.setOnClickListener(v -> onTrackNext());
        mReplayImageView.setOnClickListener(v -> replayTrack());
        mVolumeImageView.setOnClickListener(v -> showVolumeControl());
        mTonesImageView.setOnClickListener(v -> openEqualizer());
        mPlayListImageView.setOnClickListener(v -> openPlaylist());
        mLoveImageView.setOnClickListener(v -> toggleFavorite());
        mAddImageView.setOnClickListener(v -> addToPlaylist());
        mRandomImageView.setOnClickListener(v -> toggleRandom());
        mAutoPlayImageView.setOnClickListener(v -> toggleAutoPlay());
    }

    private void togglePlayPause() {
        if (mIsPlaying) {
            onTrackPause();
        } else {
            onTrackPlay();
        }
    }

    private void replayTrack() {
        if (mMediaPlayer != null) {
            mMediaPlayer.seekTo(0);
        }
    }

    private void showVolumeControl() {
        AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        if (audioManager != null) {
            audioManager.adjustVolume(AudioManager.ADJUST_SAME, AudioManager.FLAG_SHOW_UI);
        }
    }

    private void openEqualizer() {
        try {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.setClassName("com.android.settings", "com.android.settings.SoundSettings");
            startActivity(intent);
        } catch (Exception e) {
            try {
                Intent intent = new Intent(Equalizer.ACTION_OPEN_AUDIO_EFFECT_CONTROL_SESSION);
                intent.putExtra(Equalizer.EXTRA_AUDIO_SESSION, 0);
                intent.putExtra(Equalizer.EXTRA_PACKAGE_NAME, getPackageName());
                startActivity(intent);
            } catch (Exception ex) {
                Toast.makeText(this, "Égaliseur non disponible", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void openPlaylist() {
        Intent intent = new Intent(this, ListPlayerActivity.class);
        intent.putExtra("id", 6);
        intent.putExtra("audio", mTracks.get(mPosition).getAudio());
        intent.putExtra("list_audio_source", mListSource);
        intent.putExtra("type", getIntent().getStringExtra("type"));
        startActivity(intent);
    }

    private void toggleFavorite() {
        Toast.makeText(this, "Ajouté aux favoris", Toast.LENGTH_SHORT).show();
    }

    private void addToPlaylist() {
        Toast.makeText(this, "Ajouté à la playlist", Toast.LENGTH_SHORT).show();
    }

    private void toggleRandom() {
        Toast.makeText(this, "Mode aléatoire", Toast.LENGTH_SHORT).show();
    }

    private void toggleAutoPlay() {
        Toast.makeText(this, "Lecture automatique", Toast.LENGTH_SHORT).show();
    }

    private void loadTracks() {
        registerPlaylistReceiver();
    }

    private void registerPlaylistReceiver() {
        mPlaylistReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (ACTION_SELECT_PLAYER.equals(intent.getAction())) {
                    handlePlaylistSelection(intent);
                }
            }
        };

        IntentFilter filter = new IntentFilter(ACTION_SELECT_PLAYER);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(mPlaylistReceiver, filter, Context.RECEIVER_EXPORTED);
        } else {
            registerReceiver(mPlaylistReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        }
    }

    private void handlePlaylistSelection(Intent intent) {
        int position = intent.getIntExtra("position", 0);
        Intent audioIntent = new Intent(this, AudioPlayerActivity.class);
        audioIntent.putExtra("key_adapter_audio_book_id", mTracks.get(position).getIdBook());
        audioIntent.putExtra("list_audio_source", mListSource);
        startActivity(audioIntent);
        finish();
    }

    private void setupMediaPlayer() {
        try {
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setDataSource(mTracks.get(mPosition).getAudio());
            mMediaPlayer.prepare();
            mSeekBar.setMax(mMediaPlayer.getDuration());
            onTrackPlay();
        } catch (IOException e) {
            Log.e(TAG, "Error setting up MediaPlayer", e);
            Toast.makeText(this, "Erreur lors du chargement de l'audio", Toast.LENGTH_SHORT).show();
        }
    }

    private void requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        PERMISSION_REQUEST_CODE);
            }
        }
    }

    private void setupNotifications() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel();
            registerPlaybackReceiver();
            startService(new Intent(this, OnClearFromRecentService.class));
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CreateNotification.CHANNEL_ID,
                    "Lecteur Audio Fabi",
                    NotificationManager.IMPORTANCE_LOW
            );
            mNotificationManager = getSystemService(NotificationManager.class);
            if (mNotificationManager != null) {
                mNotificationManager.createNotificationChannel(channel);
            }
        }
    }

    private void registerPlaybackReceiver() {
        mPlaybackReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getExtras().getString("actionname");
                handlePlaybackAction(action);
            }
        };

        registerReceiver(mPlaybackReceiver, new IntentFilter(ACTION_TRACKS));
    }

    private void handlePlaybackAction(String action) {
        if (action == null) return;

        switch (action) {
            case CreateNotification.ACTION_PREVIOUS:
                onTrackPrevious();
                break;
            case CreateNotification.ACTION_NEXT:
                onTrackNext();
                break;
            case CreateNotification.ACTION_PLAY:
                togglePlayPause();
                break;
        }
    }

    private void startPlaybackThread() {
        mUpdateThread = new Thread(() -> {
            while (mMediaPlayer != null && !Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }

                mHandler.post(() -> updatePlaybackInfo());
            }
        });
        mUpdateThread.start();
    }

    private void updatePlaybackInfo() {
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            int currentTime = mMediaPlayer.getCurrentPosition();
            int duration = mMediaPlayer.getDuration();

            mSeekBar.setProgress(currentTime);
            mDurationCurrentTextView.setText(formatDuration(currentTime));
            mDurationTotalTextView.setText(formatDuration(duration - currentTime));

            // Auto-next when track is almost finished
            if (duration - currentTime <= AUTO_NEXT_THRESHOLD_MS) {
                onTrackNext();
            }
        }
    }

    // ==================== Playable Interface Implementation ====================

    @Override
    public void onTrackPlay() {
        if (mMediaPlayer != null) {
            mMediaPlayer.start();
            mIsPlaying = true;
            mPlayImageView.setImageResource(R.drawable.vector_black3_play);
            updateNotification(R.drawable.vector_black3_play);
        }
    }

    @Override
    public void onTrackPause() {
        if (mMediaPlayer != null) {
            mMediaPlayer.pause();
            mIsPlaying = false;
            mPlayImageView.setImageResource(R.drawable.vector_black3_pause);
            updateNotification(R.drawable.vector_black3_pause);
        }
    }

    @Override
    public void onTrackPrevious() {
        if (mTracks.isEmpty()) return;

        releaseMediaPlayer();

        mPosition = (mPosition == 0) ? mTracks.size() - 1 : mPosition - 1;

        prepareAndPlayTrack();
    }

    @Override
    public void onTrackNext() {
        if (mTracks.isEmpty()) return;

        releaseMediaPlayer();

        mPosition = (mPosition == mTracks.size() - 1) ? 0 : mPosition + 1;

        prepareAndPlayTrack();
    }

    private void prepareAndPlayTrack() {
        updateTrackInfo();

        try {
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setDataSource(mTracks.get(mPosition).getAudio());
            mMediaPlayer.prepare();
            mSeekBar.setMax(mMediaPlayer.getDuration());
            onTrackPlay();
        } catch (IOException e) {
            Log.e(TAG, "Error preparing track", e);
            Toast.makeText(this, "Erreur lors du chargement de la piste", Toast.LENGTH_SHORT).show();
        }
    }

    private void releaseMediaPlayer() {
        if (mMediaPlayer != null) {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.stop();
            }
            mMediaPlayer.reset();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    public void onTrackPlayPosition(int position) {
        releaseMediaPlayer();
        mPosition = position;
        prepareAndPlayTrack();
    }

    // ==================== Helper Methods ====================

    private void updateTrackInfo() {
        if (mTracks.isEmpty() || mPosition >= mTracks.size()) return;

        Track track = mTracks.get(mPosition);
        mTitleTextView.setText(track.getTitle());
        mAuthorTextView.setText(track.getArtist());
        mDurationTotalTextView.setText(track.getTime());

        loadTrackCover(track.getCover());
    }

    private void loadTrackCover(String coverPath) {
        File file = new File(coverPath);
        Picasso.get()
                .load(file)
                .placeholder(R.drawable.img_wait_cover_book)
                .error(R.drawable.img_wait_cover_book)
                .transform(new RoundedTransformation(15, 4))
                .resize(356, 568)
                .into(mCoverImageView);
    }

    private void updateNotification(int playPauseIcon) {
        CreateNotification.createNotification(
                this,
                mTracks.get(mPosition),
                playPauseIcon,
                mPosition,
                mTracks.size() - 1
        );
    }

    private String formatDuration(int durationMs) {
        return String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(durationMs),
                TimeUnit.MILLISECONDS.toSeconds(durationMs) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(durationMs)));
    }

    private void populateTracks(String idBook, String listSource) {
        AudioTable audioTable = new AudioTable(this);
        Cursor cursor = null;

        switch (listSource) {
            case LIST_SOURCE_ALL:
                cursor = audioTable.getData(mSession.getIdNumber());
                break;
            case LIST_SOURCE_CATEGORY:
                cursor = audioTable.getDataC(
                        mSession.getIdNumber(),
                        getIntent().getStringExtra("type")
                );
                break;
            case LIST_SOURCE_AUTHOR:
                cursor = audioTable.getDataA(
                        mSession.getIdNumber(),
                        getIntent().getStringExtra("type")
                );
                break;
        }

        if (cursor != null && cursor.moveToFirst()) {
            int index = 0;
            do {
                mTracks.add(new Track(
                        cursor.getString(2),  // idBook
                        cursor.getString(5),  // audio path
                        cursor.getString(8),  // title
                        cursor.getString(4),  // artist
                        cursor.getString(6),  // cover
                        cursor.getString(11), // time
                        R.id.relative_layout_activity_declaration_img
                ));

                if (cursor.getString(2).equals(idBook)) {
                    mPosition = index;
                }
                index++;
            } while (cursor.moveToNext());

            cursor.close();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Stop and clear notification
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && mNotificationManager != null) {
            mNotificationManager.cancelAll();
        }

        // Unregister receivers
        try {
            if (mPlaybackReceiver != null) {
                unregisterReceiver(mPlaybackReceiver);
            }
            if (mPlaylistReceiver != null) {
                unregisterReceiver(mPlaylistReceiver);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error unregistering receivers", e);
        }

        // Stop update thread
        if (mUpdateThread != null && mUpdateThread.isAlive()) {
            mUpdateThread.interrupt();
        }

        // Release media player
        releaseMediaPlayer();
    }
}