package com.naniger.elim.controleur.activity;

import android.app.UiModeManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.naniger.elim.R;
import com.naniger.elim.controleur.adapter.StatusBarAdapter;
import com.naniger.elim.model.data.Server;
import com.naniger.elim.model.data.Themes;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;

import java.util.Objects;

public class ServerActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       // loadLocale();
        setContentView(R.layout.activity_server);
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
        mSwitch = findViewById(R.id.switch_activity_server);
        mRadioGroup = findViewById(R.id.radio_group_activity_server);
        mUrlEditText = findViewById(R.id.edit_text_activity_server_ip);

        // Configuration serveur de reference (Laravel / API EduNiger) : jusqu'ici aucun
        // ecran ne permettait de la modifier, seule l'ancienne config (ci-dessous) l'etait
        // - cf. audit mobile, section 'double configuration serveur'.
        mLaravelUrlEditText = findViewById(R.id.edit_text_activity_server_laravel_url);
        mLaravelUrlEditText.setText(Server.getUrlHostProd(getApplicationContext()));
        mLaravelUrlEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                Server.saveUrlHostProd(getApplicationContext(), s.toString());
            }
        });
        if (Server.getPass(getApplicationContext()) == 1)
        {
            mSwitch.setChecked(true);
            mRadioGroup.setVisibility(View.VISIBLE);
        }
        else
        {
            mSwitch.setChecked(false);
            mRadioGroup.setVisibility(View.GONE);
            Server.saveUrlServer(getApplicationContext(),getString(R.string.url_server));
            Server.saveUrlApi(getApplicationContext(),getString(R.string.url_api));
        }
        mSwitch.setChecked(Server.getPass(getApplicationContext()) == 1);
        switch (Server.getUrlServer(getApplicationContext()))
        {
            case Server.PRESET_INTERNET:
                mUrlEditText.setVisibility(View.GONE);
                RadioButton radioButton1 = (RadioButton) mRadioGroup.getChildAt(1);
                radioButton1.setChecked(true);
                break;
            case Server.PRESET_DEVHUB:
                mUrlEditText.setVisibility(View.GONE);
                RadioButton radioButton2 = (RadioButton) mRadioGroup.getChildAt(2);
                radioButton2.setChecked(true);
                break;
            default:
                RadioButton radioButton3 = (RadioButton) mRadioGroup.getChildAt(3);
                radioButton3.setChecked(true);
                mUrlEditText.setText(Server.getUrlServer(getApplicationContext()));
                mUrlEditText.setVisibility(View.VISIBLE);
                break;
        }
        mUrlEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                Server.saveServer(getApplicationContext(),mUrlEditText.getText().toString(),mUrlEditText.getText().toString() + "android/");
            }
        });
        mSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b)
                {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        mSwitch.setChecked(true);
                        Server.saveActivate(getApplicationContext(),1);
                        mRadioGroup.setVisibility(View.VISIBLE);
                    }
                }
                else
                {
                    mSwitch.setChecked(false);
                    Server.saveActivate(getApplicationContext(),0);
                    mRadioGroup.setVisibility(View.GONE);
                    mUrlEditText.setVisibility(View.GONE);
                    Server.saveUrlServer(getApplicationContext(),getString(R.string.url_server));
                    Server.saveUrlApi(getApplicationContext(),getString(R.string.url_api));
                    RadioButton radioButton1 = (RadioButton) mRadioGroup.getChildAt(1);
                    radioButton1.setChecked(true);
                }
            }
        });
        mRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton selectedRadioButton = findViewById(checkedId);
                int position =  group.indexOfChild(selectedRadioButton);
                switch (position)
                {
                    case 1:
                        mUrlEditText.setVisibility(View.GONE);
                        Server.saveServer(getApplicationContext(),Server.PRESET_INTERNET,Server.PRESET_INTERNET + "android/");
                    break;
                    case 2:
                        mUrlEditText.setVisibility(View.GONE);
                        Server.saveServer(getApplicationContext(),Server.PRESET_DEVHUB,Server.PRESET_DEVHUB + "android/");
                        break;
                    case 3:
                        mUrlEditText.setVisibility(View.VISIBLE);

                }
            }
        });

    }
    private Switch mSwitch;
    private RadioGroup mRadioGroup;
    private EditText mUrlEditText;
    private EditText mLaravelUrlEditText;
}