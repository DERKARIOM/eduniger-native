package com.ninotech.eduniger.model.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import androidx.annotation.Nullable;

import com.ninotech.eduniger.R;
import com.ninotech.eduniger.model.data.CreateNotification;
import com.ninotech.eduniger.model.data.Track;

import java.io.IOException;
import java.util.List;

public class AudioPlayerService extends Service {

    private static final String TAG = "AudioPlayerService";
    public  static final String ACTION_TRACKS = "TRACKS_TRACKS";

    // Binder pour que l'Activity se connecte au Service
    public class AudioBinder extends Binder {
        public AudioPlayerService getService() { return AudioPlayerService.this; }
    }
    private final IBinder mBinder = new AudioBinder();

    // Callback vers l'Activity (null quand l'Activity est détachée)
    public interface PlayerCallback {
        void onPlaybackStateChanged(boolean isPlaying);
        void onTrackChanged(int position);
        void onProgressChanged(int currentMs, int durationMs);
    }
    private PlayerCallback mCallback;

    // Media
    private MediaPlayer        mMediaPlayer;
    private MediaSessionCompat mMediaSession;

    // Data
    private List<Track> mTracks;
    private int         mPosition  = 0;
    private boolean     mIsPlaying = false;

    // BroadcastReceiver pour les boutons de notification
    private BroadcastReceiver mNotificationReceiver;

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    @Override
    public void onCreate() {
        super.onCreate();
        mMediaSession = new MediaSessionCompat(this, "EduNigerPlayer");
        mMediaSession.setActive(true);
        registerNotificationReceiver();
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
        releaseAll();
    }

    // ── API publique appelée par AudioPlayerActivity ──────────────────────────

    public void setCallback(PlayerCallback callback) { mCallback = callback; }

    public void setTracks(List<Track> tracks, int position) {
        mTracks   = tracks;
        mPosition = position;
    }

    public void play() {
        if (mMediaPlayer != null && !mIsPlaying) {
            mMediaPlayer.start();
            mIsPlaying = true;
            updateNotification(R.drawable.vector_black3_play);
            if (mCallback != null) mCallback.onPlaybackStateChanged(true);
        }
    }

    public void pause() {
        if (mMediaPlayer != null && mIsPlaying) {
            mMediaPlayer.pause();
            mIsPlaying = false;
            updateNotification(R.drawable.vector_black3_pause);
            if (mCallback != null) mCallback.onPlaybackStateChanged(false);
        }
    }

    public void togglePlayPause() {
        if (mIsPlaying) pause(); else play();
    }

    public void next() {
        if (mTracks == null || mTracks.isEmpty()) return;
        mPosition = (mPosition == mTracks.size() - 1) ? 0 : mPosition + 1;
        prepareAndPlay();
    }

    public void previous() {
        if (mTracks == null || mTracks.isEmpty()) return;
        mPosition = (mPosition == 0) ? mTracks.size() - 1 : mPosition - 1;
        prepareAndPlay();
    }

    public void seekTo(int ms) {
        if (mMediaPlayer != null) mMediaPlayer.seekTo(ms);
    }

    public void prepareAndPlay() {
        releaseMediaPlayer();
        if (mTracks == null || mPosition >= mTracks.size()) return;

        try {
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setDataSource(mTracks.get(mPosition).getAudio());
            mMediaPlayer.prepare();
            mMediaPlayer.setOnCompletionListener(mp -> next());
            mIsPlaying = true;
            mMediaPlayer.start();
            updateNotification(R.drawable.vector_black3_play);
            if (mCallback != null) mCallback.onTrackChanged(mPosition);
            if (mCallback != null) mCallback.onPlaybackStateChanged(true);
        } catch (IOException e) {
            Log.e(TAG, "Error preparing track", e);
        }
    }
    public List<Track> getTracks() { return mTracks; }
    public boolean isPlaying()    { return mIsPlaying; }
    public int     getPosition()  { return mPosition; }
    public int     getCurrentMs() { return mMediaPlayer != null ? mMediaPlayer.getCurrentPosition() : 0; }
    public int     getDurationMs(){ return mMediaPlayer != null ? mMediaPlayer.getDuration() : 0; }

    // Appelé chaque seconde par le thread de l'Activity
    public void tickProgress() {
        if (mMediaPlayer != null && mIsPlaying) {
            int cur = mMediaPlayer.getCurrentPosition();
            int dur = mMediaPlayer.getDuration();
            if (mCallback != null) mCallback.onProgressChanged(cur, dur);
            updateNotification(R.drawable.vector_black3_play);
        }
    }

    // ── Notification + MediaSession ───────────────────────────────────────────

    private void updateNotification(int playPauseIcon) {
        if (mMediaSession == null || mTracks == null || mTracks.isEmpty()) return;

        Track track   = mTracks.get(mPosition);
        long  dur     = (mMediaPlayer != null) ? mMediaPlayer.getDuration()        : 0L;
        long  pos     = (mMediaPlayer != null) ? mMediaPlayer.getCurrentPosition() : 0L;
        float speed   = mIsPlaying ? 1.0f : 0.0f;

        // Durée totale → seekbar animée
        mMediaSession.setMetadata(new MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE,  track.getTitle())
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, track.getArtist())
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, dur)
                .build());

        // Position courante + vitesse
        mMediaSession.setPlaybackState(new PlaybackStateCompat.Builder()
                .setActions(PlaybackStateCompat.ACTION_PLAY
                        | PlaybackStateCompat.ACTION_PAUSE
                        | PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                        | PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
                        | PlaybackStateCompat.ACTION_SEEK_TO)
                .setState(mIsPlaying ? PlaybackStateCompat.STATE_PLAYING
                        : PlaybackStateCompat.STATE_PAUSED, pos, speed)
                .build());

        // Notification foreground (maintient le Service vivant)
        CreateNotification.createNotification(
                this, track, playPauseIcon,
                mPosition, mTracks.size() - 1,
                mMediaSession.getSessionToken());

        startForeground(1, CreateNotification.notification);
    }

    // ── BroadcastReceiver boutons notification ────────────────────────────────

    private void registerNotificationReceiver() {
        mNotificationReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getStringExtra("actionname");
                if (action == null) return;
                switch (action) {
                    case CreateNotification.ACTION_PREVIOUS: previous();        break;
                    case CreateNotification.ACTION_NEXT:     next();            break;
                    case CreateNotification.ACTION_PLAY:     togglePlayPause(); break;
                    case CreateNotification.ACTION_CLOSE:
                        stopForeground(true);
                        stopSelf();
                        break;
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
            if (mMediaPlayer.isPlaying()) mMediaPlayer.stop();
            mMediaPlayer.reset();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    private void releaseAll() {
        try { unregisterReceiver(mNotificationReceiver); } catch (Exception ignored) {}
        if (mMediaSession != null) {
            mMediaSession.setActive(false);
            mMediaSession.release();
            mMediaSession = null;
        }
        releaseMediaPlayer();
    }
}