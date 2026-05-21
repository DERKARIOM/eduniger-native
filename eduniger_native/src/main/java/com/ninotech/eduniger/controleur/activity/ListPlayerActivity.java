package com.ninotech.eduniger.controleur.activity;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ninotech.eduniger.R;
import com.ninotech.eduniger.controleur.adapter.AudioBookAdapter;
import com.ninotech.eduniger.controleur.adapter.VoidContainerAdapter;
import com.ninotech.eduniger.model.data.AudioBook;
import com.ninotech.eduniger.model.data.VoidContainer;
import com.ninotech.eduniger.model.table.AudioTable;
import com.ninotech.eduniger.model.table.Session;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;

public class ListPlayerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_container);

        ActionBar ab = getSupportActionBar();
        assert ab != null;
        ab.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        ab.setCustomView(R.layout.custom_action_bar_player);  // nouveau layout custom
        ab.setDisplayHomeAsUpEnabled(false);                   // on gère le retour manuellement

        View customView = ab.getCustomView();
        TextView actionBarTitle = customView.findViewById(R.id.action_bar_title);
        ImageView btnBack       = customView.findViewById(R.id.action_bar_btn_back);

        actionBarTitle.setText("File de lecture");
        actionBarTitle.setTextSize(15);
        actionBarTitle.setTextColor(getResources().getColor(R.color.white));

        // Bouton retour animé : scale down → rebond → finish
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

        mRecyclerView = findViewById(R.id.recycler_view_activity_container);
        mSession = new Session(this);

        try {
            ArrayList<AudioBook> audioBooks = new ArrayList<>();
            mAudioTable = new AudioTable(this);
            Intent audioIntent = getIntent();
            Cursor audioCursor = null;

            switch (Objects.requireNonNull(audioIntent.getStringExtra("list_audio_source"))) {
                case "all":
                    audioCursor = mAudioTable.getData(mSession.getIdNumber());
                    break;
                case "category":
                    audioCursor = mAudioTable.getDataC(mSession.getIdNumber(), audioIntent.getStringExtra("type"));
                    break;
                case "author":
                    audioCursor = mAudioTable.getDataA(mSession.getIdNumber(), audioIntent.getStringExtra("type"));
                    break;
            }

            assert audioCursor != null;
            audioCursor.moveToFirst();
            do {
                if (!audioCursor.getString(6).equals(getIntent().getStringExtra("audio")))
                    audioBooks.add(new AudioBook(audioCursor.getString(2), audioCursor.getString(5),
                            audioCursor.getString(8), audioCursor.getString(4),
                            audioCursor.getString(11), audioCursor.getString(6), false, true));
                else
                    audioBooks.add(new AudioBook(audioCursor.getString(2), audioCursor.getString(5),
                            audioCursor.getString(8), audioCursor.getString(4),
                            audioCursor.getString(11), audioCursor.getString(6), true, true));
            } while (audioCursor.moveToNext());

            mAudioBookAdapter = new AudioBookAdapter(audioBooks);
            mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            registerForContextMenu(mRecyclerView);
            mRecyclerView.setAdapter(mAudioBookAdapter);

            // ── Drag & drop ──────────────────────────────────────────────────
            ItemTouchHelper.Callback dragCallback = new ItemTouchHelper.SimpleCallback(
                    ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0) {

                @Override
                public boolean onMove(RecyclerView recyclerView,
                                      RecyclerView.ViewHolder viewHolder,
                                      RecyclerView.ViewHolder target) {
                    mAudioBookAdapter.moveItem(
                            viewHolder.getAdapterPosition(),
                            target.getAdapterPosition());
                    return true;
                }

                @Override
                public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {}

                @Override
                public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
                    super.onSelectedChanged(viewHolder, actionState);
                    if (actionState == ItemTouchHelper.ACTION_STATE_DRAG && viewHolder != null)
                        viewHolder.itemView.setAlpha(0.85f);
                }

                @Override
                public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                    super.clearView(recyclerView, viewHolder);
                    viewHolder.itemView.setAlpha(1.0f);
                }
            };

            ItemTouchHelper itemTouchHelper = new ItemTouchHelper(dragCallback);
            itemTouchHelper.attachToRecyclerView(mRecyclerView);
            mAudioBookAdapter.setItemTouchHelper(itemTouchHelper);
            // ─────────────────────────────────────────────────────────────────

        } catch (Exception e) {
            voidContainer(R.drawable.img_playliste_local, getString(R.string.no_audio_book));
            Log.e("ErrorAudio", e.getMessage());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);
        return true;
    }

    public void voidContainer(int image, String message) {
        ArrayList<VoidContainer> voidContainers = new ArrayList<>();
        voidContainers.add(new VoidContainer(image, message));
        VoidContainerAdapter voidContainerAdapter = new VoidContainerAdapter(voidContainers);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(voidContainerAdapter);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_item, menu);
        mAudioBookSelect = mAudioBookAdapter.getItem(mAudioBookAdapter.getPosition());
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_delete:
                File audioFile      = new File(mAudioBookSelect.getAudio());
                File coverAudioFile = new File(mAudioBookSelect.getCover());
                if (audioFile.exists() && coverAudioFile.exists()) {
                    if (audioFile.delete() && coverAudioFile.delete()) {
                        mAudioTable.remove(mSession.getIdNumber(), mAudioBookSelect.getId());
                        mAudioBookAdapter.Remove(mAudioBookAdapter.getPosition());
                    } else {
                        Toast.makeText(this, "Une erreur s'est produite lors de la suppression",
                                Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            default:
                return super.onContextItemSelected(item);
        }
        return false;
    }

    private RecyclerView      mRecyclerView;
    private Session           mSession;
    private AudioBook         mAudioBookSelect;
    private AudioBookAdapter  mAudioBookAdapter;
    private AudioTable        mAudioTable;
}