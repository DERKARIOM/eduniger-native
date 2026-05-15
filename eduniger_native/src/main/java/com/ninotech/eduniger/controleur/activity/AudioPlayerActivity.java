package com.ninotech.eduniger.controleur.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.audiofx.Equalizer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.ninotech.eduniger.Playable;
import com.ninotech.eduniger.R;
import com.ninotech.eduniger.controleur.animation.RoundedTransformation;
import com.ninotech.eduniger.model.data.Track;
import com.ninotech.eduniger.model.service.AudioPlayerService;
import com.ninotech.eduniger.model.service.OnClearFromRecentService;
import com.ninotech.eduniger.model.table.AudioTable;
import com.ninotech.eduniger.model.table.Session;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class AudioPlayerActivity extends AppCompatActivity implements Playable,
        AudioPlayerService.PlayerCallback {

    private static final String TAG = "AudioPlayerActivity";
    private static final String ACTION_SELECT_PLAYER = "SELECT_LIST_PLAYER";
    private static final String LIST_SOURCE_ALL      = "all";
    private static final String LIST_SOURCE_CATEGORY = "category";
    private static final String LIST_SOURCE_AUTHOR   = "author";
    private static final int    PERMISSION_REQUEST_CODE = 101;

    // ── Service ───────────────────────────────────────────────────────────────
    private AudioPlayerService mService;
    private boolean            mBound = false;

    private final ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            mService = ((AudioPlayerService.AudioBinder) binder).getService();
            mBound   = true;
            mService.setCallback(AudioPlayerActivity.this);

            // Passer les tracks au Service et démarrer si premier lancement
            if (!mService.isPlaying()) {
                mService.setTracks(mTracks, mPosition);
                mService.prepareAndPlay();
            }
            // Synchroniser l'UI avec l'état du Service (si on revient sur l'activity)
            syncUiWithService();
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBound = false;
        }
    };

    // Views
    private TextView  mTitleTextView;
    private TextView  mAuthorTextView;
    private TextView  mDurationTotalTextView;
    private TextView  mDurationCurrentTextView;
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
    private SeekBar   mSeekBar;

    // Data
    private List<Track> mTracks;
    private Session     mSession;
    private int         mPosition  = 0;
    private String      mListSource;

    // Thread UI
    private Handler mHandler;
    private Thread  mUpdateThread;

    // BroadcastReceiver playlist
    private BroadcastReceiver mPlaylistReceiver;

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_player);
        Objects.requireNonNull(getSupportActionBar()).hide();

        applyDarkTheme();
        initializeData();
        initializeViews();
        setupSeekBar();
        setupClickListeners();
        registerPlaylistReceiver();
        requestNotificationPermission();
        startAndBindService();
        startProgressThread();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mUpdateThread != null) mUpdateThread.interrupt();
        try { if (mPlaylistReceiver != null) unregisterReceiver(mPlaylistReceiver); }
        catch (Exception e) { Log.e(TAG, "unregister error", e); }
        if (mBound) {
            mService.setCallback(null); // détacher le callback, le Service continue
            unbindService(mConnection);
            mBound = false;
        }
    }

    // ── Setup ─────────────────────────────────────────────────────────────────

    private void applyDarkTheme() {
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(Color.parseColor("#0D1318"));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            window.getDecorView().setSystemUiVisibility(0);
    }

    private void initializeData() {
        mSession  = new Session(this);
        mHandler  = new Handler();
        mTracks   = new ArrayList<>();

        Intent intent = getIntent();
        String idBook = intent.getStringExtra("key_adapter_audio_book_id");
        mListSource   = intent.getStringExtra("list_audio_source");
        populateTracks(idBook, mListSource);
    }

    private void initializeViews() {
        mTitleTextView          = findViewById(R.id.text_view_activity_audio_player_title);
        mAuthorTextView         = findViewById(R.id.text_view_activity_audio_player_author);
        mDurationTotalTextView  = findViewById(R.id.text_view_activity_audio_player_duration_total);
        mDurationCurrentTextView= findViewById(R.id.text_view_activity_audio_player_duration_current);
        mCoverImageView         = findViewById(R.id.image_view_activity_audio_player_cover);
        mPlayImageView          = findViewById(R.id.image_view_activity_audio_player_play);
        mReplayImageView        = findViewById(R.id.image_view_activity_audio_player_replay);
        mVolumeImageView        = findViewById(R.id.image_view_activity_audio_player_volume);
        mSeekBar                = findViewById(R.id.seek_bar_activity_audio_player);
        mBackImageView          = findViewById(R.id.image_view_activity_audio_player_back);
        mTonesImageView         = findViewById(R.id.image_view_activity_audio_player_tones);
        mPlayListImageView      = findViewById(R.id.image_view_activity_audio_player_list);
        mLoveImageView          = findViewById(R.id.image_view_activity_audio_player_love);
        mAddImageView           = findViewById(R.id.image_view_activity_audio_player_add);
        mRandomImageView        = findViewById(R.id.image_view_activity_audio_player_random);
        mBackPlayImageView      = findViewById(R.id.image_view_activity_audio_player_back_player);
        mNextPlayImageView      = findViewById(R.id.image_view_activity_audio_player_next_play);
        mAutoPlayImageView      = findViewById(R.id.image_view_activity_audio_player_auto_play);
        updateTrackInfo();
    }

    private void setupSeekBar() {
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && mBound) mService.seekTo(progress);
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void setupClickListeners() {
        mBackImageView.setOnClickListener(v -> onBackPressed());
        mPlayImageView.setOnClickListener(v -> { if (mBound) mService.togglePlayPause(); });
        mBackPlayImageView.setOnClickListener(v -> onTrackPrevious());
        mNextPlayImageView.setOnClickListener(v -> onTrackNext());
        mReplayImageView.setOnClickListener(v -> { if (mBound) mService.seekTo(0); });
        mVolumeImageView.setOnClickListener(v -> showVolumeControl());
        mTonesImageView.setOnClickListener(v -> openEqualizer());
        mPlayListImageView.setOnClickListener(v -> openPlaylist());
        mLoveImageView.setOnClickListener(v -> Toast.makeText(this, "Ajouté aux favoris",   Toast.LENGTH_SHORT).show());
        mAddImageView.setOnClickListener(v ->  Toast.makeText(this, "Ajouté à la playlist", Toast.LENGTH_SHORT).show());
        mRandomImageView.setOnClickListener(v -> Toast.makeText(this, "Mode aléatoire",     Toast.LENGTH_SHORT).show());
        mAutoPlayImageView.setOnClickListener(v -> Toast.makeText(this, "Lecture automatique", Toast.LENGTH_SHORT).show());
    }

    private void startAndBindService() {
        Intent serviceIntent = new Intent(this, AudioPlayerService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            startForegroundService(serviceIntent);
        else
            startService(serviceIntent);
        bindService(serviceIntent, mConnection, Context.BIND_AUTO_CREATE);
    }

    private void startProgressThread() {
        mUpdateThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try { Thread.sleep(1000); }
                catch (InterruptedException e) { Thread.currentThread().interrupt(); break; }
                if (mBound) mHandler.post(() -> mService.tickProgress());
            }
        });
        mUpdateThread.start();
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        PERMISSION_REQUEST_CODE);
            }
        }
    }

    // ── PlayerCallback (appelé depuis AudioPlayerService sur le main thread) ──

    @Override
    public void onPlaybackStateChanged(boolean isPlaying) {
        runOnUiThread(() -> {
            if (isPlaying) {
                mPlayImageView.setImageResource(R.drawable.vector_black3_play);
            } else {
                mPlayImageView.setImageResource(R.drawable.vector_black3_pause);
            }
            mPlayImageView.setColorFilter(Color.BLACK);
        });
    }

    @Override
    public void onTrackChanged(int position) {
        runOnUiThread(() -> {
            mPosition = position;
            updateTrackInfo();
            if (mBound) mSeekBar.setMax(mService.getDurationMs());
        });
    }

    @Override
    public void onProgressChanged(int currentMs, int durationMs) {
        runOnUiThread(() -> {
            mSeekBar.setMax(durationMs);
            mSeekBar.setProgress(currentMs);
            mDurationCurrentTextView.setText(formatDuration(currentMs));
            mDurationTotalTextView.setText(formatDuration(durationMs - currentMs));
        });
    }

    // ── Playable interface ────────────────────────────────────────────────────

    @Override public void onTrackPlay()     { if (mBound) mService.play(); }
    @Override public void onTrackPause()    { if (mBound) mService.pause(); }
    @Override public void onTrackPrevious() { if (mBound) mService.previous(); }
    @Override public void onTrackNext()     { if (mBound) mService.next(); }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void syncUiWithService() {
        if (!mBound) return;
        mPosition = mService.getPosition();
        updateTrackInfo();
        mSeekBar.setMax(mService.getDurationMs());
        mSeekBar.setProgress(mService.getCurrentMs());
        onPlaybackStateChanged(mService.isPlaying());
    }

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
                .transform(new RoundedTransformation(20, 4))
                .resize(600, 600)
                .centerCrop()
                .into(mCoverImageView);
    }

    private void showVolumeControl() {
        AudioManager am = (AudioManager) getSystemService(AUDIO_SERVICE);
        if (am != null) am.adjustVolume(AudioManager.ADJUST_SAME, AudioManager.FLAG_SHOW_UI);
    }

    private void openEqualizer() {
        try {
            Intent i = new Intent(Intent.ACTION_MAIN);
            i.setClassName("com.android.settings", "com.android.settings.SoundSettings");
            startActivity(i);
        } catch (Exception e) {
            try {
                Intent i = new Intent(Equalizer.ACTION_OPEN_AUDIO_EFFECT_CONTROL_SESSION);
                i.putExtra(Equalizer.EXTRA_AUDIO_SESSION, 0);
                i.putExtra(Equalizer.EXTRA_PACKAGE_NAME, getPackageName());
                startActivity(i);
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

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    private void registerPlaylistReceiver() {
        mPlaylistReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (!ACTION_SELECT_PLAYER.equals(intent.getAction())) return;
                int pos = intent.getIntExtra("position", 0);
                if (mBound) {
                    mService.setTracks(mTracks, pos);
                    mService.prepareAndPlay();
                }
            }
        };
        IntentFilter filter = new IntentFilter(ACTION_SELECT_PLAYER);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            registerReceiver(mPlaylistReceiver, filter, Context.RECEIVER_EXPORTED);
        else
            registerReceiver(mPlaylistReceiver, filter);
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
                cursor = audioTable.getData(mSession.getIdNumber()); break;
            case LIST_SOURCE_CATEGORY:
                cursor = audioTable.getDataC(mSession.getIdNumber(), getIntent().getStringExtra("type")); break;
            case LIST_SOURCE_AUTHOR:
                cursor = audioTable.getDataA(mSession.getIdNumber(), getIntent().getStringExtra("type")); break;
        }
        if (cursor != null && cursor.moveToFirst()) {
            int index = 0;
            do {
                mTracks.add(new Track(
                        cursor.getString(2), cursor.getString(5), cursor.getString(8),
                        cursor.getString(4), cursor.getString(6), cursor.getString(11),
                        R.id.relative_layout_activity_declaration_img));
                if (cursor.getString(2).equals(idBook)) mPosition = index;
                index++;
            } while (cursor.moveToNext());
            cursor.close();
        }
    }
}