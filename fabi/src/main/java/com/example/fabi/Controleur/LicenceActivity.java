package com.example.fabi.Controleur;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.fabi.Controleur.Model.StatusBarCusto;
import com.example.fabi.R;

public class LicenceActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_licence);
        StatusBarCusto statusBarCusto = new StatusBarCusto(this,getWindow());

    }
}