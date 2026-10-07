package com.naniger.elim.controleur.player;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.naniger.elim.R;
import com.naniger.elim.controleur.activity.AudioPlayerActivity;
import com.naniger.elim.controleur.animation.RoundedTransformation;
import com.naniger.elim.model.data.Track;
import com.naniger.elim.model.service.AudioPlayerService;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.List;

/**
 * Pilote le mini lecteur flottant affiché au-dessus de la navigation principale
 * (comportement façon Spotify/Deezer).
 *
 * Il se lie au {@link AudioPlayerService} SANS le démarrer (flags = 0) : si aucune
 * lecture n'est en cours, le Service n'existe pas encore et le mini lecteur reste
 * simplement masqué — aucun risque de déclencher une lecture ou un service en trop.
 */
public class MiniPlayerController implements AudioPlayerService.PlayerCallback {

    private final Activity   mActivity;
    private final View       mRoot;
    private final ImageView  mCover;
    private final TextView   mTitle;
    private final TextView   mAuthor;
    private final ImageView  mPlay;
    private final ImageView  mNext;
    private final ImageView  mSkipBack;
    private final ProgressBar mProgress;

    private AudioPlayerService mService;
    private boolean mBound = false;

    private final ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            mService = ((AudioPlayerService.AudioBinder) binder).getService();
            mBound = true;
            mService.addCallback(MiniPlayerController.this);
            syncWithService();
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBound = false;
            mService = null;
            mRoot.setVisibility(View.GONE);
        }
    };

    public MiniPlayerController(Activity activity, View includedRootView) {
        mActivity = activity;
        mRoot     = includedRootView;
        mCover    = mRoot.findViewById(R.id.image_view_mini_player_cover);
        mTitle    = mRoot.findViewById(R.id.text_view_mini_player_title);
        mAuthor   = mRoot.findViewById(R.id.text_view_mini_player_author);
        mPlay     = mRoot.findViewById(R.id.image_view_mini_player_play);
        mNext     = mRoot.findViewById(R.id.image_view_mini_player_next);
        mSkipBack = mRoot.findViewById(R.id.image_view_mini_player_skip_back);
        mProgress = mRoot.findViewById(R.id.progress_bar_mini_player);

        mRoot.setOnClickListener(v -> openFullPlayer());
        mPlay.setOnClickListener(v -> { if (mBound) mService.togglePlayPause(); });
        mNext.setOnClickListener(v -> { if (mBound) mService.next(); });
        mSkipBack.setOnClickListener(v -> { if (mBound) mService.previous(); });

        applyBottomInset();
    }

    /**
     * Zone de sécurité : sur Android 15+ (targetSdk 35) la fenêtre s'étend sous la
     * barre de navigation. On ajoute donc l'inset système à la marge basse définie
     * dans le layout (@dimen/mini_player_bottom_offset) pour que le mini lecteur ne
     * se retrouve jamais sous la barre gestuelle.
     */
    private void applyBottomInset() {
        ViewGroup.LayoutParams lp = mRoot.getLayoutParams();
        if (!(lp instanceof ViewGroup.MarginLayoutParams)) return;
        final int baseBottomMargin = ((ViewGroup.MarginLayoutParams) lp).bottomMargin;
        final int baseSideMargin   = ((ViewGroup.MarginLayoutParams) lp).leftMargin;

        ViewCompat.setOnApplyWindowInsetsListener(mRoot, (v, windowInsets) -> {
            Insets bars = windowInsets.getInsets(
                    WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout());
            ViewGroup.LayoutParams params = v.getLayoutParams();
            if (params instanceof ViewGroup.MarginLayoutParams) {
                ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) params;
                mlp.bottomMargin = baseBottomMargin + bars.bottom;
                mlp.leftMargin   = baseSideMargin + bars.left;
                mlp.rightMargin  = baseSideMargin + bars.right;
                v.setLayoutParams(mlp);
            }
            return windowInsets; // on ne consomme pas : la navbar en a aussi besoin
        });
        ViewCompat.requestApplyInsets(mRoot);
    }

    /** À appeler depuis onStart() de l'Activity hôte. */
    public void onStart() {
        Intent intent = new Intent(mActivity, AudioPlayerService.class);
        // flags = 0 : ne PAS créer/démarrer le Service, seulement s'y raccorder s'il tourne déjà.
        mActivity.bindService(intent, mConnection, 0);
    }

    /** À appeler depuis onStop() de l'Activity hôte. */
    public void onStop() {
        if (mBound) {
            if (mService != null) mService.removeCallback(this);
            try { mActivity.unbindService(mConnection); } catch (Exception ignored) { }
            mBound = false;
        }
    }

    private void openFullPlayer() {
        if (!mBound || mService == null) return;
        List<Track> tracks = mService.getTracks();
        if (tracks == null || tracks.isEmpty()) return;
        mActivity.startActivity(new Intent(mActivity, AudioPlayerActivity.class));
    }

    private void syncWithService() {
        if (!mBound || mService == null) return;
        List<Track> tracks = mService.getTracks();
        int position = mService.getPosition();
        if (tracks == null || tracks.isEmpty() || position < 0 || position >= tracks.size()) {
            mRoot.setVisibility(View.GONE);
            return;
        }
        Track track = tracks.get(position);
        mRoot.setVisibility(View.VISIBLE);
        mTitle.setText(track.getTitle());
        mAuthor.setText(track.getArtist());
        loadCover(track.getCover());
        onPlaybackStateChanged(mService.isPlaying());
        onProgressChanged(mService.getCurrentMs(), mService.getDurationMs());
    }

    private void loadCover(String coverPath) {
        if (coverPath == null || coverPath.isEmpty()) return;
        Picasso.get().load(new File(coverPath))
                .placeholder(R.drawable.img_wait_cover_book)
                .error(R.drawable.img_wait_cover_book)
                .transform(new RoundedTransformation(10, 0))
                .resize(120, 120)
                .centerCrop()
                .into(mCover);
    }

    // ── PlayerCallback ────────────────────────────────────────────────────────

    @Override
    public void onPlaybackStateChanged(boolean isPlaying) {
        mActivity.runOnUiThread(() -> {
            mRoot.setVisibility(View.VISIBLE);
            mPlay.setImageResource(isPlaying ? R.drawable.vector_black3_pause : R.drawable.vector_black3_play);
        });
    }

    @Override
    public void onTrackChanged(int position) {
        mActivity.runOnUiThread(this::syncWithService);
    }

    @Override
    public void onProgressChanged(int currentMs, int durationMs) {
        mActivity.runOnUiThread(() -> {
            if (durationMs > 0) mProgress.setProgress((int) (1000L * currentMs / durationMs));
        });
    }
}
