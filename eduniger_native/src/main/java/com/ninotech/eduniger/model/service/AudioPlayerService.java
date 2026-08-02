package com.ninotech.eduniger.model.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.PlaybackParams;
import android.os.Binder;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import androidx.annotation.Nullable;

import com.ninotech.eduniger.R;
import com.ninotech.eduniger.model.data.CreateNotification;
import com.ninotech.eduniger.model.data.Track;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Moteur de lecture audio d'EduNiger.
 *
 * Fondations modernisées (v2) :
 *  - Gestion du focus audio (pause/duck lors d'appels, autres apps, etc.)
 *  - Callback MediaSession pour les boutons matériels (casque Bluetooth/filaire, Android Auto)
 *  - Pause automatique quand le casque est débranché ("becoming noisy")
 *  - Vitesse de lecture réglable (0.5x → 3x)
 *  - Avance/retour rapide (±10s / ±30s)
 *  - Lecture aléatoire (shuffle) et répétition (aucune / toute la file / piste courante)
 *  - Minuteur d'arrêt (sleep timer)
 *  - Reprise automatique de la position d'écoute (par livre)
 *  - Favoris (bouton "Favori" de la notification, désormais fonctionnel)
 *  - Support de plusieurs auditeurs simultanés (écran plein écran + mini lecteur)
 */
public class AudioPlayerService extends Service {

    private static final String TAG = "AudioPlayerService";
    public  static final String ACTION_TRACKS = "TRACKS_TRACKS";

    private static final String PREFS_NAME        = "audio_player_prefs";
    private static final String KEY_POSITION_PREFIX = "resume_pos_";
    private static final String KEY_FAVORITE_PREFIX = "favorite_";

    public static final long SKIP_SHORT_MS = 10_000L; // ±10s
    public static final long SKIP_LONG_MS  = 30_000L; // ±30s
    public static final float MIN_SPEED = 0.5f;
    public static final float MAX_SPEED = 3.0f;

    public enum RepeatMode { OFF, ALL, ONE }

    // Binder pour que l'Activity / le mini lecteur se connectent au Service
    public class AudioBinder extends Binder {
        public AudioPlayerService getService() { return AudioPlayerService.this; }
    }
    private final IBinder mBinder = new AudioBinder();

    // Callback(s) vers l'UI (Activity plein écran + mini lecteur peuvent être liés en même temps)
    public interface PlayerCallback {
        void onPlaybackStateChanged(boolean isPlaying);
        void onTrackChanged(int position);
        void onProgressChanged(int currentMs, int durationMs);
        default void onShuffleChanged(boolean enabled) {}
        default void onRepeatModeChanged(RepeatMode mode) {}
        default void onSpeedChanged(float speed) {}
        default void onSleepTimerChanged(long remainingMs) {}
        default void onQueueChanged() {}
        default void onFavoriteChanged(boolean isFavorite) {}
    }
    private final Set<PlayerCallback> mCallbacks = new CopyOnWriteArraySet<>();

    // Media
    private MediaPlayer        mMediaPlayer;
    private MediaSessionCompat mMediaSession;
    private AudioManager       mAudioManager;
    private AudioFocusRequest  mAudioFocusRequest; // API 26+ (minSdk 28)
    private SharedPreferences  mPrefs;

    // Data / file d'attente
    private List<Track>    mTracks    = new ArrayList<>();
    private final List<Integer> mPlayOrder = new ArrayList<>(); // permutation d'indices (shuffle-aware)
    private int         mPosition  = 0;
    private boolean     mIsPlaying = false;
    private boolean     mShuffle   = false;
    private RepeatMode  mRepeatMode = RepeatMode.OFF;
    private float       mSpeed = 1.0f;
    private int         mTickCount = 0;

    private boolean mResumePlaybackOnFocusGain = false;

    // Identifiant du livre actuellement CHARGÉ dans mMediaPlayer (peut différer brièvement de
    // mTracks.get(mPosition) pendant une transition, d'où ce suivi dédié pour la reprise de position).
    private String mLoadedTrackId;

    // Minuteur d'arrêt
    private CountDownTimer mSleepTimer;
    private long mSleepTimerRemainingMs = -1;

    // BroadcastReceivers
    private BroadcastReceiver mNotificationReceiver;
    private BroadcastReceiver mNoisyReceiver;

    // Ticker de progression : appartient désormais au Service (et non à l'Activity), afin que
    // la progression, la notification et le mini lecteur restent à jour même sans UI liée.
    private final Handler mTickHandler = new Handler(Looper.getMainLooper());
    private final Runnable mTickRunnable = new Runnable() {
        @Override public void run() {
            tickProgress();
            if (mIsPlaying) mTickHandler.postDelayed(this, 1000);
        }
    };
    private void startTicking() {
        mTickHandler.removeCallbacks(mTickRunnable);
        mTickHandler.postDelayed(mTickRunnable, 1000);
    }
    private void stopTicking() { mTickHandler.removeCallbacks(mTickRunnable); }

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    @Override
    public void onCreate() {
        super.onCreate();
        mPrefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        mMediaSession = new MediaSessionCompat(this, "EduNigerPlayer");
        mMediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS
                | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        mMediaSession.setCallback(mSessionCallback);
        mMediaSession.setActive(true);

        registerNotificationReceiver();
        registerNoisyReceiver();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) { return mBinder; }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY; // le Service redémarre si Android le tue
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        cancelSleepTimer();
        saveResumePosition();
        releaseAll();
    }

    // ── API publique appelée par l'UI (Activity plein écran + mini lecteur) ───

    public void addCallback(PlayerCallback callback) { if (callback != null) mCallbacks.add(callback); }
    public void removeCallback(PlayerCallback callback) { mCallbacks.remove(callback); }

    public void setTracks(List<Track> tracks, int position) {
        mTracks   = tracks != null ? tracks : new ArrayList<>();
        mPosition = position;
        rebuildPlayOrder(mPosition);
        for (PlayerCallback cb : mCallbacks) cb.onQueueChanged();
    }

    public void play() {
        if (mMediaPlayer == null) return;
        if (!requestAudioFocus()) return;
        if (!mIsPlaying) {
            mMediaPlayer.start();
            mIsPlaying = true;
            startTicking();
            updateNotification();
            for (PlayerCallback cb : mCallbacks) cb.onPlaybackStateChanged(true);
        }
    }

    public void pause() {
        if (mMediaPlayer != null && mIsPlaying) {
            mMediaPlayer.pause();
            mIsPlaying = false;
            stopTicking();
            saveResumePosition();
            updateNotification();
            for (PlayerCallback cb : mCallbacks) cb.onPlaybackStateChanged(false);
        }
    }

    public void togglePlayPause() {
        if (mIsPlaying) pause(); else play();
    }

    /** Piste suivante (appui explicite de l'utilisateur). */
    public void next() {
        if (mTracks == null || mTracks.isEmpty()) return;
        advance(true, false);
    }

    /**
     * Piste précédente. Convention moderne : si plus de 3s se sont écoulées,
     * on revient au début de la piste courante plutôt que de changer de piste.
     */
    public void previous() {
        if (mTracks == null || mTracks.isEmpty()) return;
        if (getCurrentMs() > 3000) { seekTo(0); return; }
        advance(false, false);
    }

    public void seekTo(int ms) {
        if (mMediaPlayer != null) mMediaPlayer.seekTo(ms);
    }

    /** Avance rapide de {@code ms} millisecondes (utiliser SKIP_SHORT_MS / SKIP_LONG_MS). */
    public void skipForward(long ms)  { seekRelative(ms); }
    /** Retour rapide de {@code ms} millisecondes. */
    public void skipBackward(long ms) { seekRelative(-ms); }

    private void seekRelative(long deltaMs) {
        if (mMediaPlayer == null) return;
        int target = (int) Math.max(0, Math.min(getDurationMs(), getCurrentMs() + deltaMs));
        seekTo(target);
    }

    public void prepareAndPlay() {
        saveResumePosition();
        releaseMediaPlayer();
        if (mTracks == null || mPosition < 0 || mPosition >= mTracks.size()) return;
        if (!requestAudioFocus()) return;

        try {
            Track track = mTracks.get(mPosition);
            mLoadedTrackId = track.getIdBook();
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setDataSource(track.getAudio());
            mMediaPlayer.setOnPreparedListener(mp -> {
                applySpeed();
                int resumeMs = getResumePosition(track.getIdBook());
                if (resumeMs > 0 && resumeMs < mp.getDuration() - 3000) mp.seekTo(resumeMs);
                mIsPlaying = true;
                mp.start();
                startTicking();
                updateNotification();
                for (PlayerCallback cb : mCallbacks) cb.onTrackChanged(mPosition);
                for (PlayerCallback cb : mCallbacks) cb.onPlaybackStateChanged(true);
                for (PlayerCallback cb : mCallbacks) cb.onFavoriteChanged(isFavorite(track.getIdBook()));
            });
            mMediaPlayer.setOnCompletionListener(mp -> handleTrackCompletion());
            mMediaPlayer.setOnErrorListener((mp, what, extra) -> {
                Log.e(TAG, "MediaPlayer error what=" + what + " extra=" + extra);
                return true; // évite le crash silencieux ; l'utilisateur peut relancer
            });
            mMediaPlayer.prepareAsync();
        } catch (IOException | IllegalStateException e) {
            Log.e(TAG, "Error preparing track", e);
        }
    }

    private void handleTrackCompletion() {
        clearResumePosition(mLoadedTrackId);

        if (mRepeatMode == RepeatMode.ONE) {
            seekTo(0);
            play();
            return;
        }
        advance(true, true);
    }

    /**
     * Avance dans la file selon l'ordre de lecture courant (shuffle-aware).
     * @param forward true = piste suivante, false = piste précédente
     * @param isAutoAdvance true si appelé après la fin naturelle d'une piste (autoplay)
     */
    private void advance(boolean forward, boolean isAutoAdvance) {
        int cursor = mPlayOrder.indexOf(mPosition);
        if (cursor < 0) { rebuildPlayOrder(mPosition); cursor = mPlayOrder.indexOf(mPosition); }

        int target = cursor + (forward ? 1 : -1);
        if (target < 0) {
            if (mRepeatMode == RepeatMode.ALL) target = mPlayOrder.size() - 1;
            else { seekTo(0); return; }
        } else if (target >= mPlayOrder.size()) {
            if (mRepeatMode == RepeatMode.ALL) {
                if (mShuffle) rebuildPlayOrder(null);
                target = 0;
            } else {
                // Fin de la file d'attente : on s'arrête proprement (pas de bouclage silencieux)
                if (isAutoAdvance) stopAtEndOfQueue();
                return;
            }
        }
        mPosition = mPlayOrder.get(target);
        prepareAndPlay();
    }

    private void stopAtEndOfQueue() {
        mIsPlaying = false;
        stopTicking();
        releaseMediaPlayer();
        for (PlayerCallback cb : mCallbacks) cb.onPlaybackStateChanged(false);
        updateNotification();
    }

    public List<Track> getTracks() { return mTracks; }
    public boolean isPlaying()    { return mIsPlaying; }
    public int     getPosition()  { return mPosition; }
    public int     getCurrentMs() { return mMediaPlayer != null ? mMediaPlayer.getCurrentPosition() : 0; }
    public int     getDurationMs(){ return mMediaPlayer != null ? mMediaPlayer.getDuration() : 0; }

    // ── Vitesse de lecture ────────────────────────────────────────────────────

    public void setSpeed(float speed) {
        mSpeed = Math.max(MIN_SPEED, Math.min(MAX_SPEED, speed));
        applySpeed();
        for (PlayerCallback cb : mCallbacks) cb.onSpeedChanged(mSpeed);
    }
    public float getSpeed() { return mSpeed; }

    private void applySpeed() {
        if (mMediaPlayer == null) return;
        try {
            boolean wasPlaying = mIsPlaying;
            PlaybackParams params = mMediaPlayer.getPlaybackParams();
            params.setSpeed(mSpeed);
            mMediaPlayer.setPlaybackParams(params);
            if (!wasPlaying && mMediaPlayer.isPlaying()) mMediaPlayer.pause();
        } catch (Exception e) {
            Log.e(TAG, "setSpeed failed", e);
        }
    }

    // ── Shuffle / Repeat ──────────────────────────────────────────────────────

    public void setShuffleEnabled(boolean enabled) {
        mShuffle = enabled;
        rebuildPlayOrder(mPosition);
        for (PlayerCallback cb : mCallbacks) cb.onShuffleChanged(mShuffle);
    }
    public boolean isShuffleEnabled() { return mShuffle; }

    public void setRepeatMode(RepeatMode mode) {
        mRepeatMode = mode;
        for (PlayerCallback cb : mCallbacks) cb.onRepeatModeChanged(mode);
    }
    public RepeatMode getRepeatMode() { return mRepeatMode; }

    /** Fait défiler OFF → ALL → ONE → OFF. Renvoie le nouveau mode. */
    public RepeatMode cycleRepeatMode() {
        RepeatMode next = mRepeatMode == RepeatMode.OFF ? RepeatMode.ALL
                        : mRepeatMode == RepeatMode.ALL ? RepeatMode.ONE
                        : RepeatMode.OFF;
        setRepeatMode(next);
        return next;
    }

    private void rebuildPlayOrder(Integer keepFirst) {
        mPlayOrder.clear();
        int size = mTracks == null ? 0 : mTracks.size();
        for (int i = 0; i < size; i++) mPlayOrder.add(i);
        if (mShuffle) {
            Collections.shuffle(mPlayOrder);
            if (keepFirst != null && mPlayOrder.remove(keepFirst)) {
                mPlayOrder.add(0, keepFirst);
            }
        }
    }

    // ── Minuteur d'arrêt (sleep timer) ───────────────────────────────────────

    public void setSleepTimer(long minutes) {
        cancelSleepTimerInternal();
        if (minutes <= 0) { notifySleepTimer(-1); return; }
        long ms = minutes * 60_000L;
        mSleepTimer = new CountDownTimer(ms, 1000) {
            @Override public void onTick(long millisUntilFinished) {
                mSleepTimerRemainingMs = millisUntilFinished;
                notifySleepTimer(millisUntilFinished);
            }
            @Override public void onFinish() {
                mSleepTimerRemainingMs = 0;
                pause();
                notifySleepTimer(0);
            }
        };
        mSleepTimer.start();
    }

    public void cancelSleepTimer() {
        cancelSleepTimerInternal();
        notifySleepTimer(-1);
    }

    private void cancelSleepTimerInternal() {
        if (mSleepTimer != null) { mSleepTimer.cancel(); mSleepTimer = null; }
        mSleepTimerRemainingMs = -1;
    }

    private void notifySleepTimer(long remainingMs) {
        for (PlayerCallback cb : mCallbacks) cb.onSleepTimerChanged(remainingMs);
    }

    public long getSleepTimerRemainingMs() { return mSleepTimerRemainingMs; }

    // ── Reprise de position / Favoris (persistés en local, par livre) ─────────

    private String currentIdBook() {
        if (mTracks == null || mPosition < 0 || mPosition >= mTracks.size()) return null;
        return mTracks.get(mPosition).getIdBook();
    }

    private void saveResumePosition() {
        String id = mLoadedTrackId;
        if (id == null || mMediaPlayer == null) return;
        int pos, dur;
        try {
            pos = mMediaPlayer.getCurrentPosition();
            dur = mMediaPlayer.getDuration();
        } catch (IllegalStateException e) { return; }
        SharedPreferences.Editor editor = mPrefs.edit();
        if (dur > 0 && pos > 3000 && pos < dur - 3000) {
            editor.putInt(KEY_POSITION_PREFIX + id, pos);
        } else {
            editor.remove(KEY_POSITION_PREFIX + id);
        }
        editor.apply();
    }

    private int getResumePosition(String idBook) {
        return idBook == null ? 0 : mPrefs.getInt(KEY_POSITION_PREFIX + idBook, 0);
    }

    private void clearResumePosition(String idBook) {
        if (idBook != null) mPrefs.edit().remove(KEY_POSITION_PREFIX + idBook).apply();
    }

    public boolean isFavorite(String idBook) {
        return idBook != null && mPrefs.getBoolean(KEY_FAVORITE_PREFIX + idBook, false);
    }

    public boolean toggleFavoriteCurrent() {
        String id = currentIdBook();
        if (id == null) return false;
        boolean newVal = !isFavorite(id);
        mPrefs.edit().putBoolean(KEY_FAVORITE_PREFIX + id, newVal).apply();
        for (PlayerCallback cb : mCallbacks) cb.onFavoriteChanged(newVal);
        return newVal;
    }

    // Appelé chaque seconde par le thread de l'Activity
    public void tickProgress() {
        if (mMediaPlayer != null && mIsPlaying) {
            int cur = mMediaPlayer.getCurrentPosition();
            int dur = mMediaPlayer.getDuration();
            for (PlayerCallback cb : mCallbacks) cb.onProgressChanged(cur, dur);
            // On rafraîchit la session (position pour le seek du lock screen) sans reconstruire
            // toute la notification à chaque tick, et on persiste la position ~ toutes les 5s.
            updatePlaybackState();
            if (++mTickCount % 5 == 0) saveResumePosition();
        }
    }

    // ── Focus audio / interruptions ───────────────────────────────────────────

    private boolean requestAudioFocus() {
        if (mAudioManager == null) return true;
        AudioAttributes attrs = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build();
        mAudioFocusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(attrs)
                .setOnAudioFocusChangeListener(mAudioFocusListener)
                .setWillPauseWhenDucked(false)
                .build();
        int result = mAudioManager.requestAudioFocus(mAudioFocusRequest);
        return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
    }

    private void abandonAudioFocus() {
        if (mAudioManager != null && mAudioFocusRequest != null) {
            mAudioManager.abandonAudioFocusRequest(mAudioFocusRequest);
        }
    }

    private final AudioManager.OnAudioFocusChangeListener mAudioFocusListener = focusChange -> {
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_LOSS:
                mResumePlaybackOnFocusGain = false;
                pause();
                abandonAudioFocus();
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                mResumePlaybackOnFocusGain = mIsPlaying;
                pause();
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                if (mMediaPlayer != null) mMediaPlayer.setVolume(0.2f, 0.2f);
                break;
            case AudioManager.AUDIOFOCUS_GAIN:
                if (mMediaPlayer != null) mMediaPlayer.setVolume(1f, 1f);
                if (mResumePlaybackOnFocusGain) {
                    mResumePlaybackOnFocusGain = false;
                    play();
                }
                break;
            default: break;
        }
    };

    private void registerNoisyReceiver() {
        mNoisyReceiver = new BroadcastReceiver() {
            @Override public void onReceive(Context context, Intent intent) {
                if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intent.getAction()) && mIsPlaying) {
                    pause(); // ex : casque débranché
                }
            }
        };
        registerReceiver(mNoisyReceiver, new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY));
    }

    // ── Boutons matériels (casque filaire/Bluetooth, Android Auto, lock screen) ─

    private final MediaSessionCompat.Callback mSessionCallback = new MediaSessionCompat.Callback() {
        @Override public void onPlay()             { play(); }
        @Override public void onPause()            { pause(); }
        @Override public void onSkipToNext()       { next(); }
        @Override public void onSkipToPrevious()   { previous(); }
        @Override public void onSeekTo(long pos)   { seekTo((int) pos); }
        @Override public void onFastForward()      { skipForward(SKIP_LONG_MS); }
        @Override public void onRewind()           { skipBackward(SKIP_SHORT_MS); }
        @Override public void onStop()             { pause(); }
    };

    // ── Notification + MediaSession ───────────────────────────────────────────

    private void updateNotification() {
        if (mMediaSession == null || mTracks == null || mTracks.isEmpty()
                || mPosition < 0 || mPosition >= mTracks.size()) return;

        Track track = mTracks.get(mPosition);

        mMediaSession.setMetadata(new MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE,  track.getTitle())
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, track.getArtist())
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, getDurationMs())
                .build());

        updatePlaybackState();

        // playbutton = icône affichée dans la notif : "pause" pendant la lecture (tap = mettre en pause)
        int playbutton = mIsPlaying ? R.drawable.vector_black3_pause : R.drawable.vector_black3_play;

        CreateNotification.createNotification(
                this, track, playbutton,
                mPosition, mTracks.size() - 1,
                mMediaSession.getSessionToken(),
                isFavorite(track.getIdBook()));

        startForeground(1, CreateNotification.notification);
    }

    private void updatePlaybackState() {
        if (mMediaSession == null) return;
        long actions = PlaybackStateCompat.ACTION_PLAY
                | PlaybackStateCompat.ACTION_PAUSE
                | PlaybackStateCompat.ACTION_PLAY_PAUSE
                | PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                | PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
                | PlaybackStateCompat.ACTION_SEEK_TO
                | PlaybackStateCompat.ACTION_FAST_FORWARD
                | PlaybackStateCompat.ACTION_REWIND
                | PlaybackStateCompat.ACTION_SET_PLAYBACK_SPEED;

        mMediaSession.setPlaybackState(new PlaybackStateCompat.Builder()
                .setActions(actions)
                .setState(mIsPlaying ? PlaybackStateCompat.STATE_PLAYING : PlaybackStateCompat.STATE_PAUSED,
                        getCurrentMs(), mIsPlaying ? mSpeed : 0f)
                .build());
    }

    // ── BroadcastReceiver boutons notification ────────────────────────────────

    private void registerNotificationReceiver() {
        mNotificationReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getStringExtra("actionname");
                if (action == null) return;
                switch (action) {
                    case CreateNotification.ACTION_PREVIOUS: previous();               break;
                    case CreateNotification.ACTION_NEXT:     next();                   break;
                    case CreateNotification.ACTION_PLAY:     togglePlayPause();        break;
                    case CreateNotification.ACTION_LOVE:     toggleFavoriteCurrent();  break;
                    case CreateNotification.ACTION_CLOSE:
                        stopForeground(true);
                        stopSelf();
                        break;
                    default: break;
                }
            }
        };
        IntentFilter filter = new IntentFilter(ACTION_TRACKS);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            registerReceiver(mNotificationReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        else
            registerReceiver(mNotificationReceiver, filter);
    }

    // ── Release ───────────────────────────────────────────────────────────────

    private void releaseMediaPlayer() {
        if (mMediaPlayer != null) {
            try {
                if (mMediaPlayer.isPlaying()) mMediaPlayer.stop();
            } catch (IllegalStateException ignored) { }
            mMediaPlayer.reset();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    private void releaseAll() {
        stopTicking();
        try { unregisterReceiver(mNotificationReceiver); } catch (Exception ignored) { }
        try { unregisterReceiver(mNoisyReceiver); } catch (Exception ignored) { }
        abandonAudioFocus();
        if (mMediaSession != null) {
            mMediaSession.setActive(false);
            mMediaSession.release();
            mMediaSession = null;
        }
        releaseMediaPlayer();
        mCallbacks.clear();
    }
}
