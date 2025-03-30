package com.ninotech.fabi.controleur.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.ninotech.fabi.controleur.adapter.StatusBarAdapter;
import com.ninotech.fabi.controleur.dialog.DigitalPrintConfirmDialog;
import com.ninotech.fabi.model.table.DigitalPrintTable;
import com.ninotech.fabi.controleur.dialog.SucceSuggesionDialog;
import com.ninotech.fabi.R;

import java.util.Objects;

public class FingerPrintActivity extends AppCompatActivity {

    private FingerprintManager fingerprintManager;
    private CancellationSignal cancellationSignal;
    private DigitalPrintTable mDigitalPrintTable;
    private Switch mSwitch;
    private SQLiteDatabase mDb;
    private Handler handler;
    private Runnable runnable;
    private Runnable mActif;
    private RadioGroup mRadioGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finger_print);
        StatusBarAdapter statusBarAdapter = new StatusBarAdapter(this,getWindow());
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        mDb = openOrCreateDatabase("data.db",MODE_PRIVATE,null);
        mDigitalPrintTable = new DigitalPrintTable(this);
        mSwitch = findViewById(R.id.switch1);
        mRadioGroup = findViewById(R.id.RadioGroup);
        try {
            Log.e("msgFingerPrint",mDigitalPrintTable.getPass());
            mSwitch.setChecked(true);
            mRadioGroup.setVisibility(View.VISIBLE);
        }catch (Exception e){ mSwitch.setChecked(false);}

        mSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b)
                {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (!fingerprintManager.isHardwareDetected()) {
                            sorryDialog();
                        } else if (!fingerprintManager.hasEnrolledFingerprints()) {
                            Intent intent = new Intent(Settings.ACTION_SECURITY_SETTINGS);
                            startActivity(intent);
                        } else {
                            DigitalPrintConfirm();
                        }
                    }
                }
                else
                {
                    mDigitalPrintTable.onUpgrade(mDb,1,1);
                    mRadioGroup.setVisibility(View.INVISIBLE);
                }
            }
        });
//        getSupportActionBar().hide();
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            fingerprintManager = (FingerprintManager) getSystemService(FINGERPRINT_SERVICE);
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
            Log.e("errorFingerPrint",getString(R.string.authorization_not_granted));
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!fingerprintManager.isHardwareDetected()) {
                Log.e("errorFingerPrint",getString(R.string.no_digital_fingerprints_listed));
            } else if (!fingerprintManager.hasEnrolledFingerprints()) {
                Log.e("errorFingerPrint",getString(R.string.no_registered_fingerprints));
            }

        }
    }
    private void startFingerprintAuth() {
        cancellationSignal = new CancellationSignal();

        FingerprintManager.AuthenticationCallback authenticationCallback = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            authenticationCallback = new FingerprintManager.AuthenticationCallback() {
                @Override
                public void onAuthenticationError(int errorCode, CharSequence errString) {
                    Toast.makeText(FingerPrintActivity.this, "Authentication error: " + errString, Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onAuthenticationHelp(int helpCode, CharSequence helpString) {
                    Toast.makeText(FingerPrintActivity.this, "Authentication help: " + helpString, Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
                    Toast.makeText(FingerPrintActivity.this, "Authentication succeeded", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onAuthenticationFailed() {
                    Toast.makeText(FingerPrintActivity.this, "Authentication failed", Toast.LENGTH_SHORT).show();
                }
            };
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            fingerprintManager.authenticate(null, cancellationSignal, 0, authenticationCallback, null);
        }
    }
    @Override
    protected void onStop() {
        super.onStop();
        if (cancellationSignal != null) {
            cancellationSignal.cancel();
        }
    }
    public void DigitalPrintConfirm() {
        DigitalPrintConfirmDialog digitalPrintDialog = new DigitalPrintConfirmDialog(this);
        digitalPrintDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        ImageView digitalPrintImageView = digitalPrintDialog.findViewById(R.id.EdigitaleConfirme);
        TextView messageTextView = digitalPrintDialog.findViewById(R.id.messageDigitaleConfirmer);
        cancellationSignal = new CancellationSignal();
        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                digitalPrintImageView.setImageResource(R.drawable.vector_purple2_200_digitale);
                messageTextView.setText(R.string.touch_the_fingerprint_sensor);
            }
        };
        mActif = new Runnable() {
            @Override
            public void run() {
                mDigitalPrintTable.insert("0");
                digitalPrintDialog.cancel();
                mRadioGroup.setVisibility(View.VISIBLE);
            }
        };
        FingerprintManager.AuthenticationCallback authenticationCallback = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            authenticationCallback = new FingerprintManager.AuthenticationCallback() {
                @Override
                public void onAuthenticationError(int errorCode, CharSequence errString) {
                    Log.e("errorFingerPrint",(String) errString);
                }

                @Override
                public void onAuthenticationHelp(int helpCode, CharSequence helpString) {
                    Log.e("errorFingerPrint",(String) helpString);
                }

                @Override
                public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
                    digitalPrintImageView.setImageResource(R.drawable.vector_vert_success);
                    mDigitalPrintTable.onCreate(mDb);
                    mDigitalPrintTable.insert("0");
                    handler.postDelayed(mActif,1000);
                }

                @Override
                public void onAuthenticationFailed() {
                    digitalPrintImageView.setImageResource(R.drawable.vector_rouge_error);
                    messageTextView.setText(R.string.unrecognized_fingerprint);
                    handler.postDelayed(runnable,1000);
                }
            };
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            fingerprintManager.authenticate(null, cancellationSignal, 0, authenticationCallback, null);
        }
        digitalPrintDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        digitalPrintDialog.build();
    }
    private void sorryDialog(){
        SucceSuggesionDialog succeSuggesionDialog = new SucceSuggesionDialog(this);
        succeSuggesionDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        succeSuggesionDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        ImageView ico = succeSuggesionDialog.findViewById(R.id.image_view_dialog_simple_ok_icon);
        TextView title = succeSuggesionDialog.findViewById(R.id.text_view_dialog_simple_ok_title);
        TextView message = succeSuggesionDialog.findViewById(R.id.text_view_dialog_simple_ok_message);
        TextView ok = succeSuggesionDialog.findViewById(R.id.text_view_dialog_simple_ok);
        ico.setImageResource(R.drawable.vector_purple_200_desole);
        title.setText("Désolé");
        message.setText("Je n'ai pas trouvé de capteur d'empreinte digitale sur votre téléphone.");
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSwitch.setChecked(false);
                succeSuggesionDialog.cancel();
            }
        });
        succeSuggesionDialog.build();
    }
}