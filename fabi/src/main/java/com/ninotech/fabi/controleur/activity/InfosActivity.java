package com.ninotech.fabi.controleur.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.ninotech.fabi.R;
import com.pspdfkit.configuration.activity.PdfActivityConfiguration;
import com.pspdfkit.ui.PdfActivity;

import java.io.File;

public class InfosActivity extends AppCompatActivity {
    private Button mButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_infos);
        getSupportActionBar().hide();
        mButton = findViewById(R.id.licence);
        File file = new File(getFilesDir(),"NINO0001.pdf");
        Toast.makeText(this, Uri.fromFile(file).toString(), Toast.LENGTH_SHORT).show();
        Uri uri = Uri.parse(Uri.fromFile(file).toString());
        PdfActivityConfiguration config = new PdfActivityConfiguration.Builder(this).build();
        PdfActivity.showDocument(this,uri,config);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(InfosActivity.this, getFilesDir().toString(), Toast.LENGTH_SHORT).show();
//                Intent licence = new Intent(InfosActivity.this, LicenceActivity.class);
//                startActivity(licence);
            }
        });
    }
}