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
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.audiofx.Equalizer;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
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
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.widget.ImageViewCompat;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.ninotech.eduniger.Playable;
import com.ninotech.eduniger.R;
import com.ninotech.eduniger.controleur.animation.RoundedTransformation;
import com.ninotech.eduniger.model.data.Track;
import com.ninotech.eduniger.model.service.AudioPlayerService;
import com.ninotech.eduniger.model.table.AudioTable;
import com.ninotech.eduniger.model.table.Session;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
            mBound = true;
            mService.addCallback(AudioPlayerActivity.this);

            // Retour via notification : mTracks est vide, on récupère depuis le Service
            if (mTracks.isEmpty()) {
                List<Track> serviceTracks = mService.getTracks();
                if (serviceTracks != null && !serviceTracks.isEmpty()) {
                    mTracks = serviceTracks;
                }
            }

            if (mTracks.isEmpty() || !mService.isPlaying()) {
                mService.setTracks(mTracks, mPosition);
                mService.prepareAndPlay();
            }

            syncUiWithService();
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBound = false;
        }
    };

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent); // mettre à jour l'intent courant
        if (mBound) syncUiWithService();
    }

    // Views
    private TextView  mTitleTextView;
    private TextView  mAuthorTextView;
    private TextView  mDurationTotalTextView;
    private TextView  mDurationCurrentTextView;
    private TextView  mSpeedTextView;
    private TextView  mSleepRemainingTextView;
    private TextView  mRepeatOneBadgeTextView;
    private ImageView mCoverImageView;
    private ImageView mPlayImageView;
    private ImageView mBackImageView;
    private ImageView mVolumeImageView;
    private ImageView mTonesImageView;
    private ImageView mPlayListImageView;
    private ImageView mFavoriteImageView;
    private ImageView mRandomImageView;
    private ImageView mBackPlayImageView;
    private ImageView mNextPlayImageView;
    private ImageView mRepeatImageView;
    private ImageView mSleepTimerImageView;
    private View      mSkipBackFrame;
    private View      mSkipForwardFrame;
    private SeekBar   mSeekBar;
    private boolean   mIsSeeking = false;

    // Data
    private List<Track> mTracks;
    private Session     mSession;
    private int         mPosition  = 0;
    private String      mListSource;

    // BroadcastReceiver playlist
    private BroadcastReceiver mPlaylistReceiver;

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_player);
        Objects.requireNonNull(getSupportActionBar()).hide();

        applyStatusBarStyle();
        applyWindowInsets();
        initializeData();
        initializeViews();
        setupSeekBar();
        setupClickListeners();
        registerPlaylistReceiver();
        requestNotificationPermission();
        startAndBindService();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try { if (mPlaylistReceiver != null) unregisterReceiver(mPlaylistReceiver); }
        catch (Exception e) { Log.e(TAG, "unregister error", e); }
        if (mBound) {
            mService.removeCallback(this); // détacher le callback, le Service continue en arrière-plan
            unbindService(mConnection);
            mBound = false;
        }
    }

    // ── Setup ─────────────────────────────────────────────────────────────────

    /** Respecte le thème clair/sombre de l'application (au lieu d'un mode sombre forcé). */
    private void applyStatusBarStyle() {
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.player_bg));
        boolean isNightMode = (getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            View decor = window.getDecorView();
            int flags = decor.getSystemUiVisibility();
            if (!isNightMode) flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            else flags &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            decor.setSystemUiVisibility(flags);
        }
    }

    /**
     * Zones de sécurité (Safe Area).
     *
     * L'app cible targetSdk 35 : sur Android 15+ le mode edge-to-edge est imposé,
     * la fenêtre s'étend donc SOUS la barre d'état et sous la barre de navigation.
     * Sans ce traitement, la toolbar passe sous l'horloge et les boutons du bas
     * sous la barre gestuelle.
     *
     * On applique les insets en PADDING (et non en marge) pour que le fond dégradé
     * continue de courir jusqu'aux bords — rendu edge-to-edge, contenu protégé.
     * Les insets latéraux (encoche en paysage, barre de nav verticale) sont
     * appliqués à la racine.
     */
    private void applyWindowInsets() {
        final View root      = findViewById(R.id.constraint_root_audio_player);
        final View toolbar   = findViewById(R.id.toolbar_activity_audio_player);
        final View bottomBar = findViewById(R.id.bloc_controls_row2);
        final View controls1 = findViewById(R.id.bloc_controls_row1);
        if (root == null) return;

        final int toolbarPadTop    = toolbar   != null ? toolbar.getPaddingTop()      : 0;
        final int bottomPadBottom  = bottomBar != null ? bottomBar.getPaddingBottom() : 0;
        final int controls1PadBot  = controls1 != null ? controls1.getPaddingBottom() : 0;

        ViewCompat.setOnApplyWindowInsetsListener(root, (v, windowInsets) -> {
            Insets bars = windowInsets.getInsets(
                    WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout());

            // Insets latéraux sur la racine (encoche / barre de nav en paysage)
            v.setPadding(bars.left, 0, bars.right, 0);

            if (toolbar != null) {
                toolbar.setPadding(toolbar.getPaddingLeft(), toolbarPadTop + bars.top,
                        toolbar.getPaddingRight(), toolbar.getPaddingBottom());
            }

            // En portrait la rangée du bas porte l'inset ; en paysage elle est
            // masquée (View de compatibilité), c'est donc la rangée de contrôles
            // qui doit s'écarter de la barre de navigation.
            if (bottomBar != null && bottomBar.getVisibility() != View.GONE) {
                bottomBar.setPadding(bottomBar.getPaddingLeft(), bottomBar.getPaddingTop(),
                        bottomBar.getPaddingRight(), bottomPadBottom + bars.bottom);
            } else if (controls1 != null) {
                controls1.setPadding(controls1.getPaddingLeft(), controls1.getPaddingTop(),
                        controls1.getPaddingRight(), controls1PadBot + bars.bottom);
            }
            return WindowInsetsCompat.CONSUMED;
        });
        ViewCompat.requestApplyInsets(root);
    }

    private void initializeData() {
        mSession  = new Session(this);
        mTracks   = new ArrayList<>();

        Intent intent = getIntent();
        String idBook = intent.getStringExtra("key_adapter_audio_book_id");
        mListSource   = intent.getStringExtra("list_audio_source");
        populateTracks(idBook, mListSource);
    }

    private void initializeViews() {
        mTitleTextView           = findViewById(R.id.text_view_activity_audio_player_title);
        mAuthorTextView          = findViewById(R.id.text_view_activity_audio_player_author);
        mDurationTotalTextView   = findViewById(R.id.text_view_activity_audio_player_duration_total);
        mDurationCurrentTextView = findViewById(R.id.text_view_activity_audio_player_duration_current);
        mSpeedTextView           = findViewById(R.id.text_view_activity_audio_player_speed);
        mSleepRemainingTextView  = findViewById(R.id.text_view_activity_audio_player_sleep_remaining);
        mRepeatOneBadgeTextView  = findViewById(R.id.text_view_activity_audio_player_repeat_one_badge);
        mCoverImageView          = findViewById(R.id.image_view_activity_audio_player_cover);
        mPlayImageView           = findViewById(R.id.image_view_activity_audio_player_play);
        mVolumeImageView         = findViewById(R.id.image_view_activity_audio_player_volume);
        mSeekBar                 = findViewById(R.id.seek_bar_activity_audio_player);
        mBackImageView           = findViewById(R.id.image_view_activity_audio_player_back);
        mTonesImageView          = findViewById(R.id.image_view_activity_audio_player_tones);
        mPlayListImageView       = findViewById(R.id.image_view_activity_audio_player_list);
        mFavoriteImageView       = findViewById(R.id.image_view_activity_audio_player_favorite);
        mRandomImageView         = findViewById(R.id.image_view_activity_audio_player_random);
        mBackPlayImageView       = findViewById(R.id.image_view_activity_audio_player_back_player);
        mNextPlayImageView       = findViewById(R.id.image_view_activity_audio_player_next_play);
        mRepeatImageView         = findViewById(R.id.image_view_activity_audio_player_auto_play);
        mSleepTimerImageView     = findViewById(R.id.image_view_activity_audio_player_sleep_timer);
        mSkipBackFrame           = findViewById(R.id.frame_activity_audio_player_skip_back);
        mSkipForwardFrame        = findViewById(R.id.frame_activity_audio_player_skip_forward);
        updateTrackInfo();
    }

    private void setupSeekBar() {
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) mDurationCurrentTextView.setText(formatDuration(progress));
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) { mIsSeeking = true; }
            @Override public void onStopTrackingTouch(SeekBar seekBar) {
                mIsSeeking = false;
                if (mBound) mService.seekTo(seekBar.getProgress());
            }
        });
    }

    private void setupClickListeners() {
        mBackImageView.setOnClickListener(v -> onBackPressed());
        mPlayImageView.setOnClickListener(v -> { if (mBound) mService.togglePlayPause(); });
        mBackPlayImageView.setOnClickListener(v -> onTrackPrevious());
        mNextPlayImageView.setOnClickListener(v -> onTrackNext());
        mVolumeImageView.setOnClickListener(v -> showVolumeControl());
        mTonesImageView.setOnClickListener(v -> openEqualizer());
        mPlayListImageView.setOnClickListener(v -> openPlaylist());

        mFavoriteImageView.setOnClickListener(v -> { if (mBound) mService.toggleFavoriteCurrent(); });

        mRandomImageView.setOnClickListener(v -> {
            if (!mBound) return;
            boolean enabled = !mService.isShuffleEnabled();
            mService.setShuffleEnabled(enabled);
            Toast.makeText(this, enabled ? R.string.player_shuffle_on : R.string.player_shuffle_off,
                    Toast.LENGTH_SHORT).show();
        });

        mRepeatImageView.setOnClickListener(v -> {
            if (!mBound) return;
            AudioPlayerService.RepeatMode mode = mService.cycleRepeatMode();
            int msg = mode == AudioPlayerService.RepeatMode.OFF ? R.string.player_repeat_off
                    : mode == AudioPlayerService.RepeatMode.ALL ? R.string.player_repeat_all
                    : R.string.player_repeat_one;
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        });

        mSkipBackFrame.setOnClickListener(v -> { if (mBound) mService.skipBackward(AudioPlayerService.SKIP_SHORT_MS); });
        mSkipForwardFrame.setOnClickListener(v -> { if (mBound) mService.skipForward(AudioPlayerService.SKIP_LONG_MS); });

        mSpeedTextView.setOnClickListener(v -> showSpeedDialog());
        mSleepTimerImageView.setOnClickListener(v -> showSleepTimerDialog());
    }

    private void startAndBindService() {
        Intent serviceIntent = new Intent(this, AudioPlayerService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            startForegroundService(serviceIntent);
        else
            startService(serviceIntent);
        bindService(serviceIntent, mConnection, Context.BIND_AUTO_CREATE);
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

    // ── Dialogues : vitesse de lecture / minuteur de sommeil ──────────────────

    private void showSpeedDialog() {
        if (!mBound) return;
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_playback_speed, null);
        dialog.setContentView(view);

        float[] speeds = {0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 1.75f, 2.0f, 2.5f, 3.0f};
        int[] chipIds = {
                R.id.chip_speed_0_5, R.id.chip_speed_0_75, R.id.chip_speed_1_0, R.id.chip_speed_1_25,
                R.id.chip_speed_1_5, R.id.chip_speed_1_75, R.id.chip_speed_2_0, R.id.chip_speed_2_5,
                R.id.chip_speed_3_0
        };
        float current = mService.getSpeed();
        for (int i = 0; i < speeds.length; i++) {
            float speed = speeds[i];
            TextView chip = view.findViewById(chipIds[i]);
            if (chip == null) continue;
            highlightChip(chip, Math.abs(current - speed) < 0.01f);
            chip.setOnClickListener(v -> {
                if (mBound) mService.setSpeed(speed);
                dialog.dismiss();
            });
        }
        dialog.show();
    }

    private void showSleepTimerDialog() {
        if (!mBound) return;
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_sleep_timer, null);
        dialog.setContentView(view);

        Map<Integer, Long> options = new LinkedHashMap<>();
        options.put(R.id.chip_sleep_off, 0L);
        options.put(R.id.chip_sleep_5, 5L);
        options.put(R.id.chip_sleep_10, 10L);
        options.put(R.id.chip_sleep_15, 15L);
        options.put(R.id.chip_sleep_30, 30L);
        options.put(R.id.chip_sleep_45, 45L);
        options.put(R.id.chip_sleep_60, 60L);

        for (Map.Entry<Integer, Long> entry : options.entrySet()) {
            TextView chip = view.findViewById(entry.getKey());
            if (chip == null) continue;
            long minutes = entry.getValue();
            chip.setOnClickListener(v -> {
                if (mBound) {
                    if (minutes <= 0) {
                        mService.cancelSleepTimer();
                        Toast.makeText(this, R.string.player_sleep_timer_cancelled, Toast.LENGTH_SHORT).show();
                    } else {
                        mService.setSleepTimer(minutes);
                        Toast.makeText(this, getString(R.string.player_sleep_timer_set, (int) minutes),
                                Toast.LENGTH_SHORT).show();
                    }
                }
                dialog.dismiss();
            });
        }
        dialog.show();
    }

    private void highlightChip(TextView chip, boolean active) {
        if (chip == null || chip.getBackground() == null) return;
        int bg = ContextCompat.getColor(this, active ? R.color.player_accent : R.color.player_surface);
        int text = ContextCompat.getColor(this, active ? R.color.white : R.color.player_text_primary);
        chip.getBackground().mutate().setTint(bg);
        chip.setTextColor(text);
    }

    // ── PlayerCallback (appelé depuis AudioPlayerService sur le main thread) ──

    @Override
    public void onPlaybackStateChanged(boolean isPlaying) {
        runOnUiThread(() -> {
            mPlayImageView.setImageResource(isPlaying ? R.drawable.vector_black3_pause : R.drawable.vector_black3_play);
            mPlayImageView.animate().cancel();
            mPlayImageView.setScaleX(0.82f);
            mPlayImageView.setScaleY(0.82f);
            mPlayImageView.animate().scaleX(1f).scaleY(1f).setDuration(180).start();
        });
    }

    @Override
    public void onTrackChanged(int position) {
        runOnUiThread(() -> {
            mPosition = position;
            updateTrackInfo();
            if (mBound) {
                mSeekBar.setMax(Math.max(mService.getDurationMs(), 1));
                mSeekBar.setProgress(0);
            }
        });
    }

    @Override
    public void onProgressChanged(int currentMs, int durationMs) {
        runOnUiThread(() -> {
            if (mIsSeeking) return;
            mSeekBar.setMax(Math.max(durationMs, 1));
            mSeekBar.setProgress(currentMs);
            mDurationCurrentTextView.setText(formatDuration(currentMs));
            mDurationTotalTextView.setText("-" + formatDuration(Math.max(durationMs - currentMs, 0)));
        });
    }

    @Override
    public void onShuffleChanged(boolean enabled) {
        runOnUiThread(() -> tintImage(mRandomImageView, enabled));
    }

    @Override
    public void onRepeatModeChanged(AudioPlayerService.RepeatMode mode) {
        runOnUiThread(() -> {
            tintImage(mRepeatImageView, mode != AudioPlayerService.RepeatMode.OFF);
            mRepeatOneBadgeTextView.setVisibility(
                    mode == AudioPlayerService.RepeatMode.ONE ? View.VISIBLE : View.GONE);
        });
    }

    @Override
    public void onSpeedChanged(float speed) {
        runOnUiThread(() -> mSpeedTextView.setText(formatSpeed(speed)));
    }

    @Override
    public void onSleepTimerChanged(long remainingMs) {
        runOnUiThread(() -> {
            if (remainingMs < 0) {
                mSleepRemainingTextView.setVisibility(View.GONE);
            } else {
                mSleepRemainingTextView.setVisibility(View.VISIBLE);
                mSleepRemainingTextView.setText(
                        getString(R.string.player_sleep_timer_set_short, formatDuration((int) remainingMs)));
            }
        });
    }

    @Override
    public void onFavoriteChanged(boolean isFavorite) {
        runOnUiThread(() -> mFavoriteImageView.setImageResource(
                isFavorite ? R.drawable.vector_purple2_200_on_like : R.drawable.vector_black3_off_like));
    }

    // ── Playable interface ────────────────────────────────────────────────────

    @Override public void onTrackPlay()     { if (mBound) mService.play(); }
    @Override public void onTrackPause()    { if (mBound) mService.pause(); }
    @Override public void onTrackPrevious() { if (mBound) mService.previous(); }
    @Override public void onTrackNext()     { if (mBound) mService.next(); }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void syncUiWithService() {
        if (!mBound || mService == null) return;

        if (mTracks.isEmpty() && mService.getTracks() != null) {
            mTracks = mService.getTracks();
        }

        mPosition = mService.getPosition();
        updateTrackInfo();
        mSeekBar.setMax(Math.max(mService.getDurationMs(), 1));
        mSeekBar.setProgress(mService.getCurrentMs());
        onPlaybackStateChanged(mService.isPlaying());
        onShuffleChanged(mService.isShuffleEnabled());
        onRepeatModeChanged(mService.getRepeatMode());
        onSpeedChanged(mService.getSpeed());
        onSleepTimerChanged(mService.getSleepTimerRemainingMs());
        if (!mTracks.isEmpty() && mPosition < mTracks.size()) {
            onFavoriteChanged(mService.isFavorite(mTracks.get(mPosition).getIdBook()));
        }
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
        mCoverImageView.animate().cancel();
        mCoverImageView.setAlpha(0.35f);
        mCoverImageView.animate().alpha(1f).setDuration(280).start();

        File file = new File(coverPath);
        Picasso.get()
                .load(file)
                .placeholder(R.drawable.img_wait_cover_book)
                .error(R.drawable.img_wait_cover_book)
                .transform(new RoundedTransformation(24, 4))
                .resize(700, 700)
                .centerCrop()
                .into(mCoverImageView);
    }

    private void tintImage(ImageView imageView, boolean active) {
        if (imageView == null) return;
        int color = ContextCompat.getColor(this, active ? R.color.player_accent : R.color.player_control_inactive);
        ImageViewCompat.setImageTintList(imageView, ColorStateList.valueOf(color));
    }

    private String formatSpeed(float speed) {
        String formatted = (speed % 1 == 0) ? String.format("%.1f", speed) : String.format("%.2f", speed);
        return formatted + "x";
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

                ArrayList<String> orderedIds = intent.getStringArrayListExtra("ordered_ids");
                String selectedId = intent.getStringExtra("selected_id");

                if (orderedIds != null && !orderedIds.isEmpty()) {
                    // La file a potentiellement été réordonnée (glisser-déposer) : on reflète
                    // fidèlement l'ordre affiché plutôt qu'un simple index dans l'ancien ordre.
                    List<Track> reordered = reorderTracks(orderedIds);
                    if (!reordered.isEmpty()) mTracks = reordered;
                    mPosition = 0;
                    for (int i = 0; i < mTracks.size(); i++) {
                        String id = mTracks.get(i).getIdBook();
                        if (id != null && id.equals(selectedId)) { mPosition = i; break; }
                    }
                } else {
                    mPosition = intent.getIntExtra("position", 0);
                }

                if (mBound) {
                    mService.setTracks(mTracks, mPosition);
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

    /** Réordonne mTracks selon la liste d'identifiants reçue (ordre affiché dans la file). */
    private List<Track> reorderTracks(List<String> orderedIds) {
        Map<String, Track> byId = new HashMap<>();
        for (Track t : mTracks) {
            if (t.getIdBook() != null) byId.put(t.getIdBook(), t);
        }
        List<Track> result = new ArrayList<>();
        for (String id : orderedIds) {
            Track t = byId.remove(id);
            if (t != null) result.add(t);
        }
        result.addAll(byId.values()); // sécurité : pistes non retrouvées dans l'ordre reçu
        return result;
    }

    private String formatDuration(int durationMs) {
        if (durationMs < 0) durationMs = 0;
        long totalSeconds = TimeUnit.MILLISECONDS.toSeconds(durationMs);
        long hours   = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;
        return hours > 0
                ? String.format("%d:%02d:%02d", hours, minutes, seconds)
                : String.format("%d:%02d", minutes, seconds);
    }

    private void populateTracks(String idBook, String listSource) {
        // ← Garder si le Service a déjà les tracks (retour via notification)
        if (listSource == null) return;

        AudioTable audioTable = new AudioTable(this);
        Cursor cursor = null;
        switch (listSource) {
            case LIST_SOURCE_ALL:
                cursor = audioTable.getData(mSession.getIdNumber()); break;
            case LIST_SOURCE_CATEGORY:
                cursor = audioTable.getDataC(mSession.getIdNumber(), getIntent().getStringExtra("type")); break;
            case LIST_SOURCE_AUTHOR:
                cursor = audioTable.getDataA(mSession.getIdNumber(), getIntent().getStringExtra("type")); break;
            default: return; // ← sécurité pour toute valeur inattendue
        }
        if (cursor != null && cursor.moveToFirst()) {
            int index = 0;
            do {
                mTracks.add(new Track(
                        cursor.getString(2), cursor.getString(5), cursor.getString(8),
                        cursor.getString(4), cursor.getString(6), cursor.getString(11),
                        R.id.relative_layout_activity_declaration_img));
                if (idBook != null && cursor.getString(2).equals(idBook)) mPosition = index;
                index++;
            } while (cursor.moveToNext());
            cursor.close();
        }
    }
}
