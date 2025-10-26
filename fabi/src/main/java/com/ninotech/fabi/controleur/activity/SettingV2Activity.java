package com.ninotech.fabi.controleur.activity;

import android.app.UiModeManager;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ninotech.fabi.R;
import com.ninotech.fabi.controleur.adapter.SettingAdapter;
import com.ninotech.fabi.controleur.adapter.StatusBarAdapter;
import com.ninotech.fabi.model.data.Setting;
import com.ninotech.fabi.model.data.Themes;

import java.util.ArrayList;

public class SettingV2Activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_v2);
        RecyclerView mSettingRecyclerView = findViewById(R.id.recycler_view_activity_setting2);
        ArrayList<Setting> settings = new ArrayList<>();
        UiModeManager uiModeManager = null;
        switch (Themes.getName(getApplicationContext()))
        {
            case "system":
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    uiModeManager = (UiModeManager) getSystemService(Context.UI_MODE_SERVICE);
                }
                int currentMode = uiModeManager.getNightMode();
                if (currentMode == UiModeManager.MODE_NIGHT_YES) {
                    settings.add(new Setting(R.drawable.user,getString(R.string.account),null));
                    settings.add(new Setting(R.drawable.vector_white_sombre_print_digital,getString(R.string.digital_print),null));
                    settings.add(new Setting(R.drawable.vector_white_sombre_phone_connected,"Serveur connecté",null));
                    settings.add(new Setting(R.drawable.vector_white_sombre_mode_night,"Thème appliqué","Mode Systémes"));
                    settings.add(new Setting(R.drawable.vector_white_sombre_message,getString(R.string.send_suggestion),null));
                    settings.add(new Setting(R.drawable.vector_white_sombre_grade,getString(R.string.evaluate_us),null));
                    settings.add(new Setting(R.drawable.vector_white_sombre_phone,"Contactez-nous",null));
                    //settings.add(new Setting(R.drawable.vector_black3_off_subscribe,"Suivez-nous",null));
                    settings.add(new Setting(R.drawable.vector_white_sombre_video,getString(R.string.how_it_works),null));
                    settings.add(new Setting(R.drawable.vector_white_sombre_help,getString(R.string.app_information),null));
                    settings.add(new Setting(R.drawable.vector_white_sombre_new,"Quoi de neuf ?",null));
                } else {
                    StatusBarAdapter statusBarAdapter = new StatusBarAdapter(this,getWindow());
                    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                    settings.add(new Setting(R.drawable.user,getString(R.string.account),null));
                    settings.add(new Setting(R.drawable.vector_black3_print_digital,getString(R.string.digital_print),null));
                    settings.add(new Setting(R.drawable.vector_black3_phone_connected,"Serveur connecté",null));
                    settings.add(new Setting(R.drawable.vector_theme_default,"Thème appliqué","Mode Systémes"));
                    settings.add(new Setting(R.drawable.vector_black3_message,getString(R.string.send_suggestion),null));
                    settings.add(new Setting(R.drawable.vector_black3_grade,getString(R.string.evaluate_us),null));
                    settings.add(new Setting(R.drawable.vector_purple_200_phone,"Contactez-nous",null));
                    //settings.add(new Setting(R.drawable.vector_black3_off_subscribe,"Suivez-nous",null));
                    settings.add(new Setting(R.drawable.vector_black3_video,getString(R.string.how_it_works),null));
                    settings.add(new Setting(R.drawable.vector_black3_help,getString(R.string.app_information),null));
                    settings.add(new Setting(R.drawable.vector_new,"Quoi de neuf ?",null));
                }
                break;
            case "notNight":
                StatusBarAdapter statusBarAdapter = new StatusBarAdapter(this,getWindow());
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                settings.add(new Setting(R.drawable.user,getString(R.string.account),null));
                settings.add(new Setting(R.drawable.vector_black3_print_digital,getString(R.string.digital_print),null));
                settings.add(new Setting(R.drawable.vector_black3_phone_connected,"Serveur connecté",null));
                settings.add(new Setting(R.drawable.vector_theme_default,"Thème appliqué","Mode Clair"));
                settings.add(new Setting(R.drawable.vector_black3_message,getString(R.string.send_suggestion),null));
                settings.add(new Setting(R.drawable.vector_black3_grade,getString(R.string.evaluate_us),null));
                settings.add(new Setting(R.drawable.vector_purple_200_phone,"Contactez-nous",null));
                //settings.add(new Setting(R.drawable.vector_black3_off_subscribe,"Suivez-nous",null));
                settings.add(new Setting(R.drawable.vector_black3_video,getString(R.string.how_it_works),null));
                settings.add(new Setting(R.drawable.vector_black3_help,getString(R.string.app_information),null));
                settings.add(new Setting(R.drawable.vector_new,"Quoi de neuf ?",null));
                break;
            case "night":
                settings.add(new Setting(R.drawable.user,getString(R.string.account),null));
                settings.add(new Setting(R.drawable.vector_white_sombre_print_digital,getString(R.string.digital_print),null));
                settings.add(new Setting(R.drawable.vector_white_sombre_phone_connected,"Serveur connecté",null));
                settings.add(new Setting(R.drawable.vector_white_sombre_mode_night,"Thème appliqué","Mode Sombre"));
                settings.add(new Setting(R.drawable.vector_white_sombre_message,getString(R.string.send_suggestion),null));
                settings.add(new Setting(R.drawable.vector_white_sombre_grade,getString(R.string.evaluate_us),null));
                settings.add(new Setting(R.drawable.vector_white_sombre_phone,"Contactez-nous",null));
                //settings.add(new Setting(R.drawable.vector_black3_off_subscribe,"Suivez-nous",null));
                settings.add(new Setting(R.drawable.vector_white_sombre_video,getString(R.string.how_it_works),null));
                settings.add(new Setting(R.drawable.vector_white_sombre_help,getString(R.string.app_information),null));
                settings.add(new Setting(R.drawable.vector_white_sombre_new,"Quoi de neuf ?",null));
                break;
        }
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