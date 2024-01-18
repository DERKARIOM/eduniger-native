package com.fabi.controleur.activity;

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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.fabi.model.table.EmpreiteTable;
import com.fabi.controleur.custo.StatusBarCusto;
import com.example.fabi.R;

public class EmpreinteActivity extends AppCompatActivity {
    private FingerprintManager fingerprintManager;
    private CancellationSignal cancellationSignal;
    private ImageView mEdigitale;
    private TextView mMessageDigitale;
    private Handler handler;
    private Runnable runnable;
    private Runnable home;
    private EmpreiteTable mEmpreiteTable;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_empreinte);
        getSupportActionBar().hide();
        StatusBarCusto statusBarCusto = new StatusBarCusto(this,getWindow());
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

        mEdigitale = findViewById(R.id.Edigitale);
        mEmpreiteTable = new EmpreiteTable(this);
        mMessageDigitale = findViewById(R.id.messageDigitale);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            fingerprintManager = (FingerprintManager) getSystemService(FINGERPRINT_SERVICE);
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "autorisation non accorde", Toast.LENGTH_SHORT).show();
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!fingerprintManager.isHardwareDetected()) {
                Toast.makeText(this, "Pas d' empreintes numerique inscrites", Toast.LENGTH_SHORT).show();
            } else if (!fingerprintManager.hasEnrolledFingerprints()) {
                Toast.makeText(this, "Pas d' empreintes digital inscrites", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(EmpreinteActivity.this, "Authentication error: " + errString, Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onAuthenticationHelp(int helpCode, CharSequence helpString) {
                    Toast.makeText(EmpreinteActivity.this, "Authentication help: " + helpString, Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
                    mEdigitale.setImageResource(R.drawable.vector_vert_success);
                    mMessageDigitale.setText("");
                    mEmpreiteTable.onUpdate("1");
                    handler.postDelayed(home, 1);
                }

                @Override
                public void onAuthenticationFailed() {
                    mEdigitale.setImageResource(R.drawable.vector_rouge_error);
                    mMessageDigitale.setText("Empreinte non reconnue");
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
        mEdigitale.setImageResource(R.drawable.vector_purple2_200_digitale);
        mMessageDigitale.setText("Toucher le capteur d'empreinte");
    }
    private void Home()
    {
        Intent mainA = new Intent(EmpreinteActivity.this, MainActivity.class);
        startActivity(mainA);
        finish();
    }
}