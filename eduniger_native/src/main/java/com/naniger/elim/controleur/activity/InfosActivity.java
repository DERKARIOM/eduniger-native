package com.naniger.elim.controleur.activity;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.naniger.elim.R;

public class InfosActivity extends AppCompatActivity {
    private Button mButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_infos);
        getSupportActionBar().hide();
        mButton = findViewById(R.id.licence);
        // Ouvre la politique de confidentialite (page web publique, exigee par Google Play)
        mButton.setOnClickListener(v -> {
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.url_privacy))));
            } catch (ActivityNotFoundException e) {
                Toast.makeText(this, "Aucun navigateur disponible.", Toast.LENGTH_SHORT).show();
            }
        });
        TextView versionTextView = findViewById(R.id.version);
    }
}