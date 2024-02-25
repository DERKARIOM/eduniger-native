package com.ninotech.fabi.controleur.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.ninotech.fabi.model.table.DigitalPrintTable;
import com.ninotech.fabi.R;

import java.util.Objects;

public class DigitalPrintActivity extends AppCompatActivity {
    private FingerprintManager fingerprintManager;
    private CancellationSignal cancellationSignal;
    private ImageView mDigitalPrintImageView;
    private TextView mMessageTextView;
    private Handler handler;
    private Runnable runnable;
    private Runnable home;
    private DigitalPrintTable mDigitalPrintTable;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_digital_print);
        Objects.requireNonNull(getSupportActionBar()).hide();
        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                // Redemander à l'utilisateur de Toucher le capteur d'empreinte digitale
                authenticate();
            }
        };
        home = new Runnable() {
            @Override
            public void run() {
                Home();
            }
        };

        mDigitalPrintImageView = findViewById(R.id.image_view_activity_digital_print);
        mDigitalPrintTable = new DigitalPrintTable(this);
        mMessageTextView = findViewById(R.id.text_view_activity_digital_print_message);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            fingerprintManager = (FingerprintManager) getSystemService(FINGERPRINT_SERVICE);
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
            Log.e("ErrDigitalPrint",getString(R.string.authorization_not_granted));
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!fingerprintManager.isHardwareDetected()) {
                Log.e("ErrDigitalPrint",getString(R.string.no_digital_fingerprints_listed));
            } else if (!fingerprintManager.hasEnrolledFingerprints()) {
                Log.e("ErrDigitalPrint",getString(R.string.no_registered_fingerprints));
            } else {
                startFingerprintAuth();
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
                    Log.e("ErrDigitalPrint", (String) errString);
                }

                @Override
                public void onAuthenticationHelp(int helpCode, CharSequence helpString) {
                    Log.e("ErrDigitalPrint", (String) helpString);
                }

                @Override
                public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
                    mDigitalPrintImageView.setImageResource(R.drawable.vector_vert_success);
                    mMessageTextView.setText("");
                    mDigitalPrintTable.onUpdate("1");
                    handler.postDelayed(home, 1);
                }

                @Override
                public void onAuthenticationFailed() {
                    mDigitalPrintImageView.setImageResource(R.drawable.vector_rouge_error);
                    mMessageTextView.setText(R.string.unrecognized_fingerprint);
                    handler.postDelayed(runnable, 1000);
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
    private void authenticate()
    {
        mDigitalPrintImageView.setImageResource(R.drawable.vector_purple2_200_digitale);
        mMessageTextView.setText(R.string.touch_the_fingerprint_sensor);
    }
    private void Home()
    {
        Intent mainA = new Intent(DigitalPrintActivity.this, MainActivity.class);
        startActivity(mainA);
        finish();
    }
}