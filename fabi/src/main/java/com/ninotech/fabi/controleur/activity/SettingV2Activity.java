package com.ninotech.fabi.controleur.activity;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ninotech.fabi.R;
import com.ninotech.fabi.controleur.adapter.SettingAdapter;
import com.ninotech.fabi.controleur.adapter.StatusBarAdapter;
import com.ninotech.fabi.model.data.Setting;

import java.util.ArrayList;

public class SettingV2Activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_v2);
        StatusBarAdapter statusBarAdapter = new StatusBarAdapter(this,getWindow());
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        RecyclerView mSettingRecyclerView = findViewById(R.id.recycler_view_activity_setting2);
        ArrayList<Setting> settings = new ArrayList<>();
        settings.add(new Setting(R.drawable.user,getString(R.string.account),null));
        settings.add(new Setting(R.drawable.vector_black3_print_digital,getString(R.string.digital_print),null));
        settings.add(new Setting(R.drawable.vector_black3_message,getString(R.string.send_suggestion),null));
        settings.add(new Setting(R.drawable.vector_black3_grade,getString(R.string.evaluate_us),null));
        settings.add(new Setting(R.drawable.vector_black3_video,getString(R.string.how_it_works),null));
        settings.add(new Setting(R.drawable.vector_black3_help,getString(R.string.app_information),null));
        settings.add(new Setting(R.drawable.vector_new,"Quoi de neuf ?",null));
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