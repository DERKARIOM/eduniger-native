package com.ninotech.fabi.controleur.activity;

import android.database.sqlite.SQLiteDatabase;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.ninotech.fabi.R;
import com.ninotech.fabi.controleur.adapter.StatusBarAdapter;
import com.ninotech.fabi.model.data.Server;
import com.ninotech.fabi.model.table.DigitalPrintTable;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.CancellationSignal;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Switch;

import androidx.annotation.Nullable;

import java.util.Locale;
import java.util.Objects;

public class ServerActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       // loadLocale();
        setContentView(R.layout.activity_server);
        StatusBarAdapter statusBarAdapter = new StatusBarAdapter(this,getWindow());
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        mSwitch = findViewById(R.id.switch_activity_server);
        mRadioGroup = findViewById(R.id.radio_group_activity_server);
        switch (Server.getIpServer(getApplicationContext()))
        {
            case "https://telesafe.net/fabi/":
                RadioButton radioButton1 = (RadioButton) mRadioGroup.getChildAt(1);
                radioButton1.setChecked(true);
                break;
            case "http://192.168.1.25:2222/fabi/":
                RadioButton radioButton2 = (RadioButton) mRadioGroup.getChildAt(2);
                radioButton2.setChecked(true);
                break;
            default:
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
                        Server.saveServer(getApplicationContext(),"https://telesafe.net/fabi/","https://telesafe.net/fabi/android/");
                    break;
                    case 2:
                        Server.saveServer(getApplicationContext(),"http://192.168.1.25:2222/fabi/","http://192.168.1.25:2222/fabi/android/");
                        break;

                }
            }
        });

    }
    private Switch mSwitch;
    private RadioGroup mRadioGroup;
}