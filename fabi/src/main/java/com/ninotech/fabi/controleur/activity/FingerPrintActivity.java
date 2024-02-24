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
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.ninotech.fabi.controleur.dialog.EmpreinteConfirmerDialog;
import com.ninotech.fabi.model.table.EmpreiteTable;
import com.ninotech.fabi.controleur.dialog.SucceSuggesionDialog;
import com.ninotech.fabi.R;

public class FingerPrintActivity extends AppCompatActivity {

    private FingerprintManager fingerprintManager;
    private CancellationSignal cancellationSignal;
    private EmpreiteTable mEmpreiteTable;
    private Switch mSwitch;
    private SQLiteDatabase mDb;
    private Handler handler;
    private Runnable runnable;
    private Runnable activer;
    private RadioGroup mRadioGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finger_print);
        getSupportActionBar().hide();
        //StatusBarCusto statusBarCusto = new StatusBarCusto(this,getWindow());
        mDb = openOrCreateDatabase("data.db",MODE_PRIVATE,null);
        mEmpreiteTable = new EmpreiteTable(this);
        mSwitch = findViewById(R.id.switch1);
        mRadioGroup = findViewById(R.id.RadioGroup);
        try {
            Toast.makeText(this,mEmpreiteTable.getPasse(), Toast.LENGTH_SHORT);
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
                            DesoleDialog();
                        } else if (!fingerprintManager.hasEnrolledFingerprints()) {
                            Intent intent = new Intent(Settings.ACTION_SECURITY_SETTINGS);
                            startActivity(intent);
                        } else {
                            EmpreinteConfirmer();
                        }
                    }
                }
                else
                {
                    mEmpreiteTable.onUpgrade(mDb,1,1);
                    Toast.makeText(FingerPrintActivity.this, "Desactiver", Toast.LENGTH_SHORT).show();
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
            Toast.makeText(this, "autorisation non accorde", Toast.LENGTH_SHORT).show();
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!fingerprintManager.isHardwareDetected()) {

                Toast.makeText(this, "Pas d' empreintes numerique inscrites", Toast.LENGTH_SHORT);
            } else if (!fingerprintManager.hasEnrolledFingerprints()) {
                Toast.makeText(this, "Pas d' empreintes digital inscrites", Toast.LENGTH_SHORT);
            } else {
//                startFingerprintAuth();
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
    public void EmpreinteConfirmer() {
        EmpreinteConfirmerDialog empreinteCusto = new EmpreinteConfirmerDialog(this);
        empreinteCusto.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        ImageView digitaleConfirmer = empreinteCusto.findViewById(R.id.EdigitaleConfirme);
        TextView messageConfirme = empreinteCusto.findViewById(R.id.messageDigitaleConfirmer);
        cancellationSignal = new CancellationSignal();
        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                // Redemander à l'utilisateur de Toucher le capteur d'empreinte digitale
                digitaleConfirmer.setImageResource(R.drawable.vector_purple2_200_digitale);
                messageConfirme.setText("Touchez le capteur d' empreinte");
            }
        };
        activer = new Runnable() {
            @Override
            public void run() {
                mEmpreiteTable.insert("0");
                empreinteCusto.cancel();
                mRadioGroup.setVisibility(View.VISIBLE);
            }
        };
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
                    digitaleConfirmer.setImageResource(R.drawable.vector_vert_success);
                    mEmpreiteTable.onCreate(mDb);
                    mEmpreiteTable.insert("0");
                    handler.postDelayed(activer,1000);
                }

                @Override
                public void onAuthenticationFailed() {
                    digitaleConfirmer.setImageResource(R.drawable.vector_rouge_error);
                    messageConfirme.setText("Empreinte non reconnue");
                    handler.postDelayed(runnable,1000);
                }
            };
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            fingerprintManager.authenticate(null, cancellationSignal, 0, authenticationCallback, null);
        }
        empreinteCusto.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        empreinteCusto.build();
    }
    private void DesoleDialog(){
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