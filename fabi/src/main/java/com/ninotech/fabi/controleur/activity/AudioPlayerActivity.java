package com.ninotech.fabi.controleur.activity;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.ninotech.fabi.R;
import com.ninotech.fabi.controleur.animation.RoundedTransformation;
import com.ninotech.fabi.controleur.custo.StatusBarCusto;
import com.ninotech.fabi.model.data.Tones;
import com.ninotech.fabi.model.table.AudioTable;
import com.ninotech.fabi.model.table.Session;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
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
        mDurationTotalTextView = findViewById(R.id.text_view_activity_audio_player_duration_total);
        mDurationCurrentTextView = findViewById(R.id.text_view_activity_audio_player_duration_current);
        mCoverImageView = findViewById(R.id.image_view_activity_audio_player_cover);
        mPlayImageView = findViewById(R.id.image_view_activity_audio_player_play);
        String idBook = audioBookIntent.getStringExtra("key_adagitpter_audio_book_id");
        AudioTable audioTable = new AudioTable(this);
        try {
            Cursor audioCursor = audioTable.getData(mSession.getIdNumber(),idBook);
            audioCursor.moveToFirst();
            mTitleTextView.setText(audioCursor.getString(8));
            mAuthorTextView.setText(audioCursor.getString(4));
            mDurationTotalTextView.setText(audioCursor.getString(11));
            byte[] imageBytes = audioCursor.getBlob(5);
            // Convertir le tableau d'octets en Bitmap
            Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
            File file = bitmapToFile(this, "image.png", bitmap);
            Picasso.get().load(file)
                    .placeholder(R.drawable.img_default_book)
                    .error(R.drawable.img_default_book)
                    .transform(new RoundedTransformation(15,4))
                    .resize(280,330)
                    .into(mCoverImageView);
            mMediaPlayer = new MediaPlayer();
            // Spécifie le chemin d'accès au fichier audio dans le stockage interne
                mMediaPlayer.setDataSource(audioCursor.getString(6));
                mMediaPlayer.prepare();
                mMediaPlayer.start();
        }catch (Exception e)
        {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e("ErrorAudioCursor", Objects.requireNonNull(e.getMessage()));
        }
        mPlayImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mMediaPlayer.isPlaying())
                {
                    mPlayImageView.setImageResource(R.drawable.vector_black3_pause);
                    mMediaPlayer.pause();
                }
                else
                {
                    mPlayImageView.setImageResource(R.drawable.vector_black3_play);
                    mMediaPlayer.start();
                }
            }
        });

    }
    public File bitmapToFile(Context context, String filename, Bitmap bitmap) {
        // Créer un fichier dans le répertoire de cache de l'application
        File file = new File(context.getCacheDir(), filename);
        try {
            // Convertir le Bitmap en un fichier de sortie
            file.createNewFile();
            FileOutputStream ostream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, ostream);
            ostream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file;
    }
    private Session mSession;
    private TextView mTitleTextView;
    private TextView mAuthorTextView;
    private ImageView mCoverImageView;
    private TextView mDurationTotalTextView;
    private TextView mDurationCurrentTextView;
    private MediaPlayer mMediaPlayer;
    private ImageView mPlayImageView;
}