package com.ninotech.fabi.controleur.activity;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
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
import java.util.concurrent.TimeUnit;

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
        mSeekBar = findViewById(R.id.seek_bar_activity_audio_player);
        mBackImageView = findViewById(R.id.image_view_activity_audio_player_back);
        mHandler = new Handler();
        String idBook = audioBookIntent.getStringExtra("key_adapter_audio_book_id");
        AudioTable audioTable = new AudioTable(this);
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (b) {
                    mMediaPlayer.seekTo(i);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        mBackImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        try {
            Cursor audioCursor = audioTable.getData(mSession.getIdNumber(),idBook);
            audioCursor.moveToFirst();
            mTitleTextView.setText(audioCursor.getString(8));
            mAuthorTextView.setText(audioCursor.getString(4));
            mDurationTotalTextView.setText(audioCursor.getString(11));
            File file = new File(audioCursor.getString(5));
            Picasso.get().load(file)
                    .placeholder(R.drawable.img_default_book)
                    .error(R.drawable.img_default_book)
                    .transform(new RoundedTransformation(15,4))
                    .resize(280,330)
                    .into(mCoverImageView);
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setDataSource(audioCursor.getString(6));
            mMediaPlayer.prepare();
            mSeekBar.setMax(mMediaPlayer.getDuration());
            mMediaPlayer.start();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (mMediaPlayer != null) {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (mMediaPlayer != null && mMediaPlayer.isPlaying())
                                {
                                    int currentTime = mMediaPlayer.getCurrentPosition();
                                    mSeekBar.setProgress(currentTime);
                                    mDurationCurrentTextView.setText(convertedDurationToString(currentTime));
                                }
                            }
                        });
                    }
                }
            }).start();
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
    @Override
    public void onDestroy() {
        super.onDestroy();
        mMediaPlayer.release();
        mMediaPlayer = null;
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
    public String convertedDurationToString(int duration)
    {
        return String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(duration),
                TimeUnit.MILLISECONDS.toSeconds(duration) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration)));
    }
    private Session mSession;
    private TextView mTitleTextView;
    private TextView mAuthorTextView;
    private ImageView mCoverImageView;
    private TextView mDurationTotalTextView;
    private TextView mDurationCurrentTextView;
    private MediaPlayer mMediaPlayer;
    private ImageView mPlayImageView;
    private SeekBar mSeekBar;
    private Handler mHandler;
    private ImageView mBackImageView;
}