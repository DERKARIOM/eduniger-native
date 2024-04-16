package com.ninotech.fabi.controleur.activity;

import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.ninotech.fabi.controleur.custo.StatusBarCusto;
import com.ninotech.fabi.R;
import com.pspdfkit.PSPDFKit;
import com.pspdfkit.ui.PdfActivity;

import java.util.Objects;


public class LicenceActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_licence);
        Objects.requireNonNull(getSupportActionBar()).hide();
    }
}