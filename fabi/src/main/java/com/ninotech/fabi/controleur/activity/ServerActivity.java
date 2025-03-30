package com.ninotech.fabi.controleur.activity;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.ninotech.fabi.R;
import com.ninotech.fabi.controleur.adapter.StatusBarAdapter;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.Nullable;

import java.util.Locale;

public class ServerActivity extends AppCompatActivity {
    private Spinner spinnerLanguage;
    private String[] languages = {"Français", "English", "العربية"};
    private String[] languageCodes = {"fr", "en", "ar"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       // loadLocale();
        setContentView(R.layout.activity_server);
        StatusBarAdapter statusBarAdapter = new StatusBarAdapter(this,getWindow());
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Charger la langue sauvegardée

        spinnerLanguage = findViewById(R.id.spinner_language);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, languages);
        spinnerLanguage.setAdapter(adapter);

        // Sélectionner la langue actuelle
        String currentLanguage = getSharedPreferences("Settings", MODE_PRIVATE).getString("My_Lang", "fr");
        int index = getIndexForLanguage(currentLanguage);
        spinnerLanguage.setSelection(index);

        // Changer la langue lors de la sélection
        spinnerLanguage.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                setLocale(languageCodes[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private int getIndexForLanguage(String langCode) {
        for (int i = 0; i < languageCodes.length; i++) {
            if (languageCodes[i].equals(langCode)) {
                return i;
            }
        }
        return 0; // Par défaut "français"
    }

    private void setLocale(String langCode) {
        Locale locale = new Locale(langCode);
        Locale.setDefault(locale);
        Resources resources = getResources();
        Configuration config = new Configuration(resources.getConfiguration());
        config.setLocale(locale);
        resources.updateConfiguration(config, resources.getDisplayMetrics());

        // Sauvegarder la langue sélectionnée
        SharedPreferences.Editor editor = getSharedPreferences("Settings", MODE_PRIVATE).edit();
        editor.putString("My_Lang", langCode);
        editor.apply();

        // Redémarrer l'activité pour appliquer les changements
        Intent intent = getIntent();
        finish();
        startActivity(intent);
    }

    private void loadLocale() {
        SharedPreferences prefs = getSharedPreferences("Settings", MODE_PRIVATE);
        String language = prefs.getString("My_Lang", "fr");
        setLocale(language);
    }
}