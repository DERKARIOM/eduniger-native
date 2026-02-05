package com.ninotech.eduniger.controleur.activity;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ninotech.eduniger.R;
import com.ninotech.eduniger.controleur.adapter.SettingAdapter;
import com.ninotech.eduniger.controleur.adapter.StatusBarAdapter;
import com.ninotech.eduniger.model.data.Setting;

import java.util.ArrayList;

public class ContactActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);
        StatusBarAdapter statusBarAdapter = new StatusBarAdapter(this,getWindow());
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        RecyclerView mSettingRecyclerView = findViewById(R.id.recycler_view_activity_contact);
        ArrayList<Setting> settings = new ArrayList<>();
        settings.add(new Setting(R.mipmap.ic_v2,"L'équipe EduNiger","227-94961793"));
        settings.add(new Setting(R.drawable.img_ninotech,"L'équipe NinoTech","227-96627534"));

        SettingAdapter mSettingAdapter = new SettingAdapter(settings);
        mSettingRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        mSettingRecyclerView.setAdapter(mSettingAdapter);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Vérifier si l'item sélectionné est le bouton de retour
        if (item.getItemId() == android.R.id.home) {
            // Appeler la méthode onBackPressed pour simuler un back press
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}