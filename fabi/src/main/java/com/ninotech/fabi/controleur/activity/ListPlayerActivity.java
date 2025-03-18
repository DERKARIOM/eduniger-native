package com.ninotech.fabi.controleur.activity;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.ninotech.fabi.R;
import com.ninotech.fabi.controleur.adapter.AuthorLocalAdapter;
import com.ninotech.fabi.controleur.adapter.AudioBookAdapter;
import com.ninotech.fabi.controleur.adapter.CategoryLocalAdapter;
import com.ninotech.fabi.controleur.adapter.ElectronicBookAdapter;
import com.ninotech.fabi.controleur.adapter.VoidContainerAdapter;
import com.ninotech.fabi.controleur.adapter.LoandBookAdapter;
import com.ninotech.fabi.model.data.Author;
import com.ninotech.fabi.model.data.AudioBook;
import com.ninotech.fabi.model.data.Category;
import com.ninotech.fabi.model.data.ElectronicBook;
import com.ninotech.fabi.model.data.LoandBook;
import com.ninotech.fabi.model.data.VoidContainer;
import com.ninotech.fabi.model.table.AudioTable;
import com.ninotech.fabi.model.table.ElectronicTable;
import com.ninotech.fabi.model.table.LoandTable;
import com.ninotech.fabi.model.table.Session;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ListPlayerActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_container);
        ActionBar ab = getSupportActionBar();
        assert ab != null;
        ab.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.white)));
        ab.setHomeAsUpIndicator(R.drawable.vector_back);
        ab.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        ab.setCustomView(R.layout.custom_action_bar);
        ab.setDisplayHomeAsUpEnabled(true);
        TextView actionBarTitle = ab.getCustomView().findViewById(R.id.action_bar_title);
        mRecyclerView = findViewById(R.id.recycler_view_activity_container);
        mSession = new Session(this);
        Intent libraryIntent = getIntent();
        try {
            actionBarTitle.setText("File de lecture");
            ArrayList<AudioBook> audioBooks = new ArrayList<>();
            mAudioTable = new AudioTable(this);
            Cursor audioCursor = mAudioTable.getData(mSession.getIdNumber());
            audioCursor.moveToFirst();
            do {
                if (!audioCursor.getString(6).equals(getIntent().getStringExtra("audio")))
                    audioBooks.add(new AudioBook(audioCursor.getString(2),audioCursor.getString(5),audioCursor.getString(8),audioCursor.getString(4),audioCursor.getString(11),audioCursor.getString(6),false,true));
                else
                    audioBooks.add(new AudioBook(audioCursor.getString(2),audioCursor.getString(5),audioCursor.getString(8),audioCursor.getString(4),audioCursor.getString(11),audioCursor.getString(6),true,true));
            }while (audioCursor.moveToNext());
            mAudioBookAdapter = new AudioBookAdapter(audioBooks);
            mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            registerForContextMenu(mRecyclerView);
            mRecyclerView.setAdapter(mAudioBookAdapter);
        }catch (Exception e)
        {
            voidContainer(R.drawable.img_playliste_local,getString(R.string.no_audio_book));
            Log.e("ErrorAudio",e.getMessage());
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);
        return true;
    }
    public void voidContainer(int image , String message)
    {
        ArrayList<VoidContainer> voidContainers = new ArrayList<>();
        voidContainers.add(new VoidContainer(image,message));
        VoidContainerAdapter voidContainerAdapter = new VoidContainerAdapter(voidContainers);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(voidContainerAdapter);
    }
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_item,menu);
        mAudioBookSelect = mAudioBookAdapter.getItem(mAudioBookAdapter.getPosition());
    }
    @Override
    public boolean onContextItemSelected(MenuItem item){
        switch (item.getItemId())
        {
            case R.id.menu_item_delete:
                File audioFile = new File(mAudioBookSelect.getAudio());
                File coverAudioFile = new File(mAudioBookSelect.getCover());
                if(audioFile.exists() && coverAudioFile.exists())
                {
                    if(audioFile.delete() && coverAudioFile.delete())
                    {
                        mAudioTable.remove(mSession.getIdNumber(),mAudioBookSelect.getId());
                        mAudioBookAdapter.Remove(mAudioBookAdapter.getPosition());
                    }
                    else
                    {
                        Toast.makeText(this, "Une erreur s'est produite lors de la suppression", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            default:
                return super.onContextItemSelected(item);
        }
        return false;
    }
    private RecyclerView mRecyclerView;
    private Session mSession;
    private AudioBook mAudioBookSelect;
    private AudioBookAdapter mAudioBookAdapter;
    private AudioTable mAudioTable;
}