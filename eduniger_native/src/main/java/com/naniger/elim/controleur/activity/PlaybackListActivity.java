package com.naniger.elim.controleur.activity;

import android.os.Bundle;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.naniger.elim.R;
import com.naniger.elim.controleur.adapter.AudioBookAdapter;
import com.naniger.elim.controleur.adapter.VoidContainerAdapter;
import com.naniger.elim.model.data.AudioBook;
import com.naniger.elim.model.data.ContinueItem;
import com.naniger.elim.model.data.PlaybackRepository;
import com.naniger.elim.model.data.VoidContainer;

import java.util.ArrayList;
import java.util.List;

/**
 * Écran unique servant « Vos Favoris » et « Historique d'écoute ».
 *
 * Les deux listes partagent la même source (PlaybackTable) et le même rendu, seul
 * le filtre change — d'où un seul écran paramétré par EXTRA_MODE plutôt que deux
 * activités quasi identiques.
 *
 * On réutilise AudioBookAdapter (déjà utilisé par la file d'attente) : construit
 * avec isPlayerList=false, un clic ouvre directement AudioPlayerActivity, et la
 * reprise à la bonne position est gérée par le service.
 */
public class PlaybackListActivity extends AppCompatActivity {

    public static final String EXTRA_MODE   = "playback_list_mode";
    public static final String MODE_FAVORITES = "favorites";
    public static final String MODE_HISTORY   = "history";

    private RecyclerView       mRecyclerView;
    private PlaybackRepository mRepository;
    private String             mMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_container);

        mMode = getIntent().getStringExtra(EXTRA_MODE);
        if (mMode == null) mMode = MODE_HISTORY;

        mRecyclerView = findViewById(R.id.recycler_view_activity_container);
        mRepository   = new PlaybackRepository(this);

        setupActionBar();
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Rechargé à chaque retour : la progression ou les favoris ont pu changer
        // depuis le lecteur.
        loadData();
    }

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar == null) return;

        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setCustomView(R.layout.custom_action_bar_player);
        actionBar.setDisplayHomeAsUpEnabled(false);

        View customView = actionBar.getCustomView();
        if (customView == null) return;

        TextView  title   = customView.findViewById(R.id.action_bar_title);
        ImageView btnBack = customView.findViewById(R.id.action_bar_btn_back);

        if (title != null) {
            title.setText(MODE_FAVORITES.equals(mMode)
                    ? R.string.your_favorites : R.string.listening_history);
            title.setTextColor(ContextCompat.getColor(this, R.color.player_text_primary));
        }

        if (btnBack != null) {
            // Même animation de retour que la file de lecture, pour rester cohérent.
            btnBack.setOnClickListener(v -> v.animate()
                    .scaleX(0.65f).scaleY(0.65f)
                    .setDuration(100)
                    .withEndAction(() -> v.animate()
                            .scaleX(1f).scaleY(1f)
                            .setDuration(180)
                            .setInterpolator(new OvershootInterpolator(2.5f))
                            .withEndAction(this::onBackPressed)
                            .start())
                    .start());
        }
    }

    private void loadData() {
        boolean favorites = MODE_FAVORITES.equals(mMode);
        List<ContinueItem> items = favorites
                ? mRepository.getFavorites()
                : mRepository.getHistory();

        if (items.isEmpty()) {
            showEmptyState(favorites);
            return;
        }

        List<AudioBook> audioBooks = new ArrayList<>();
        for (ContinueItem item : items) {
            audioBooks.add(new AudioBook(
                    item.getIdBook(),
                    item.getCover(),
                    item.getTitle(),
                    item.getAuthor(),
                    item.getDurationLabel(),
                    item.getAudio(),
                    false,   // isPlayer : aucune piste mise en avant ici
                    false)); // isPlayerList : clic = ouvrir le lecteur (pas la file)
        }
        mRecyclerView.setAdapter(new AudioBookAdapter(audioBooks));
    }

    private void showEmptyState(boolean favorites) {
        List<VoidContainer> voidContainers = new ArrayList<>();
        voidContainers.add(new VoidContainer(
                R.drawable.img_playliste_local,
                getString(favorites ? R.string.no_favorites : R.string.no_history)));
        mRecyclerView.setAdapter(new VoidContainerAdapter(voidContainers));
    }
}
