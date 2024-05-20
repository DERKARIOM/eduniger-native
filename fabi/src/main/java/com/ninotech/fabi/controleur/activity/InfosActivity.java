package com.ninotech.fabi.controleur.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.ninotech.fabi.R;
import com.pspdfkit.configuration.activity.PdfActivityConfiguration;
import com.pspdfkit.ui.PdfActivity;

public class InfosActivity extends AppCompatActivity {
    private Button mButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_infos);
        getSupportActionBar().hide();
        mButton = findViewById(R.id.licence);
        Uri uri = Uri.parse("file:///android_asset/sample.pdf");
        PdfActivityConfiguration config = new PdfActivityConfiguration.Builder(this).build();
        PdfActivity.showDocument(this,uri,config);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent licence = new Intent(InfosActivity.this, LicenceActivity.class);
                startActivity(licence);
            }
        });
    }
}