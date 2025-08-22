package com.ninotech.fabi.controleur.activity;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.ninotech.fabi.R;
import com.ninotech.fabi.model.table.ElectronicTable;
import com.pspdfkit.configuration.activity.PdfActivityConfiguration;
import com.pspdfkit.configuration.page.PageScrollDirection;
import com.pspdfkit.configuration.page.PageScrollMode;
import com.pspdfkit.configuration.settings.SettingsMenuItemType;
import com.pspdfkit.configuration.sharing.ShareFeatures;
import com.pspdfkit.ui.PdfActivity;

import org.w3c.dom.Text;

import java.io.File;
import java.util.EnumSet;

public class InfosActivity extends AppCompatActivity {
    private Button mButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_infos);
        getSupportActionBar().hide();
        mButton = findViewById(R.id.licence);
        TextView versionTextView = findViewById(R.id.version);
        //.setText("Version " + getString(R.string.app_version));
        Uri data = getIntent().getData();
        if (data != null) {
            String id = data.getQueryParameter("id");
            String name = data.getQueryParameter("name");
            versionTextView.setText("Lien reçu : " + data.toString() +
                    "\nID = " + id +
                    "\nNom = " + name);
        } else {
            versionTextView.setText("Aucun lien reçu");
        }
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent licence = new Intent(InfosActivity.this, LicenceActivity.class);
                startActivity(licence);
            }
        });
    }
}