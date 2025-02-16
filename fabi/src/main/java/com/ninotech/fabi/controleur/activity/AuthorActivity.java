package com.ninotech.fabi.controleur.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.ninotech.fabi.R;
import com.ninotech.fabi.controleur.animation.RoundedTransformation;
import com.ninotech.fabi.model.data.Author;
import com.squareup.picasso.Picasso;

import java.util.Objects;

public class AuthorActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_author);
        Objects.requireNonNull(getSupportActionBar()).hide();
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        mProfileImageView = findViewById(R.id.image_view_author_activity_profile);
        mUsernameTextView = findViewById(R.id.text_view_activity_author_username);
        Intent authorIntent = getIntent();
        mAuthor = new Author(
                authorIntent.getStringExtra("intent_author_adapter_id"),
                authorIntent.getStringExtra("intent_author_adapter_name"),
                authorIntent.getStringExtra("intent_author_adapter_first_name"),
                authorIntent.getStringExtra("intent_author_adapter_first_profile")
        );
        Picasso.get()
                .load(getString(R.string.ip_server) + "ressources/profile/" + mAuthor.getProfile())
                .placeholder(R.drawable.img_default_book)
                .error(R.drawable.img_default_book)
                .transform(new RoundedTransformation(1000,4))
                .resize(384,384)
                .into(mProfileImageView);
        mUsernameTextView.setText(mAuthor.getFirstName() + " " + mAuthor.getName());
    }
    private ImageView mProfileImageView;
    private TextView mUsernameTextView;
    private Author mAuthor;
}