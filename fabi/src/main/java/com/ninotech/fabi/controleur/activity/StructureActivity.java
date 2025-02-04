package com.ninotech.fabi.controleur.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.ninotech.fabi.R;
import com.ninotech.fabi.controleur.animation.RoundedTransformation;
import com.squareup.picasso.Picasso;

import java.util.Objects;

public class StructureActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_structure);
        Objects.requireNonNull(getSupportActionBar()).hide();
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        mWelcomeImageView = findViewById(R.id.image_view_structure_activity_welcome);
        mProfileImageView = findViewById(R.id.image_view_structure_activity_profile);
        Picasso.get()
                .load(getString(R.string.ip_server) + "ressources/baniere/openlab.png")
                .transform(new RoundedTransformation(200,10))
                .resize(6200,2222)
                .into(mWelcomeImageView);
        mWelcomeImageView.setVisibility(View.VISIBLE);
        Picasso.get()
                .load(getResources().getString(R.string.ip_server) + "ressources/cover/openlab.png")
                .placeholder(R.drawable.img_default_book)
                .error(R.drawable.img_default_book)
                .transform(new RoundedTransformation(1000,4))
                .resize(284,284)
                .into(mProfileImageView);
    }
    private ImageView mWelcomeImageView;
    private ImageView mProfileImageView;
}