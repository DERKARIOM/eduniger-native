package com.ninotech.eduniger.controleur.activity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.ninotech.eduniger.R;

public class InfosActivity extends AppCompatActivity {
    private Button mButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_infos);
        getSupportActionBar().hide();
        mButton = findViewById(R.id.licence);
        TextView versionTextView = findViewById(R.id.version);
    }
}