package com.fabi.controleur.activity;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.fabi.controleur.custo.StatusBarCusto;
import com.example.fabi.R;

public class LicenceActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_licence);
        StatusBarCusto statusBarCusto = new StatusBarCusto(this,getWindow());

    }
}