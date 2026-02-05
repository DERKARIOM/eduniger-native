package com.ninotech.eduniger.controleur.activity;

import android.annotation.SuppressLint;
import android.app.UiModeManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.ninotech.eduniger.R;
import com.ninotech.eduniger.controleur.adapter.StatusBarAdapter;
import com.ninotech.eduniger.model.data.Themes;

import java.util.Objects;

public class ThemeActivity extends AppCompatActivity {

    @SuppressLint("SwitchIntDef")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_theme);
        UiModeManager uiModeManager = null;
        switch (Themes.getName(getApplicationContext()))
        {
            case "system":
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    uiModeManager = (UiModeManager) getSystemService(Context.UI_MODE_SERVICE);
                }
                int currentMode = uiModeManager.getNightMode();
                if (currentMode == UiModeManager.MODE_NIGHT_NO) {
                    StatusBarAdapter statusBarAdapter = new StatusBarAdapter(this,getWindow());
                }
                break;
            case "notNight":
                StatusBarAdapter statusBarAdapter = new StatusBarAdapter(this,getWindow());
                break;
        }
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        mRadioGroup = findViewById(R.id.radio_group_activity_theme);
        switch (Themes.getName(getApplicationContext()))
        {
            case "notNight":
                RadioButton radioButton1 = (RadioButton) mRadioGroup.getChildAt(1);
                radioButton1.setChecked(true);
                break;
            case "night":
                RadioButton radioButton2 = (RadioButton) mRadioGroup.getChildAt(2);
                radioButton2.setChecked(true);
                break;
            case "system":
                RadioButton radioButton3 = (RadioButton) mRadioGroup.getChildAt(3);
                radioButton3.setChecked(true);
                break;
        }
        mRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton selectedRadioButton = findViewById(checkedId);
                int position =  group.indexOfChild(selectedRadioButton);
                switch (position)
                {
                    case 1:
                        Themes.saveTheme(getApplicationContext(),"notNight");
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                        break;
                    case 2:
                        Themes.saveTheme(getApplicationContext(),"night");
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                        break;
                    case 3:
                        Themes.saveTheme(getApplicationContext(),"system");
                       // AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.getDefaultNightMode());
                        break;

                }
            }
        });
    }
    private RadioGroup mRadioGroup;
}