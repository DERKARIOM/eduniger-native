package com.ninotech.fabi.controleur.activity;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.ninotech.fabi.R;
import com.ninotech.fabi.controleur.custo.StatusBarCusto;
import com.ninotech.fabi.model.data.Tones;
import com.ninotech.fabi.model.table.AudioTable;
import com.ninotech.fabi.model.table.Session;

import java.util.Objects;

public class AudioPlayerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_player);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        Objects.requireNonNull(getSupportActionBar()).hide();
        StatusBarCusto statusBarCusto = new StatusBarCusto(this,getWindow());
        Intent audioBookIntent = getIntent();
        mSession = new Session(this);
        mTitleTextView = findViewById(R.id.text_view_activity_audio_player_title);
        mAuthorTextView = findViewById(R.id.text_view_activity_audio_player_author);
        String idBook = audioBookIntent.getStringExtra("key_adapter_audio_book_id");
        AudioTable audioTable = new AudioTable(this);
        try {
            Cursor audioCursor = audioTable.getData(mSession.getIdNumber(),idBook);
            audioCursor.moveToFirst();
            mTitleTextView.setText(audioCursor.getString(8));
            mAuthorTextView.setText(audioCursor.getString(4));
        }catch (Exception e)
        {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e("ErrorAudioCursor", Objects.requireNonNull(e.getMessage()));
        }

    }
    private Session mSession;
    private TextView mTitleTextView;
    private TextView mAuthorTextView;
}