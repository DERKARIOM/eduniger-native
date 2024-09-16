package com.ninotech.fabi.controleur.activity;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ninotech.fabi.model.data.Account;
import com.ninotech.fabi.R;
import com.ninotech.fabi.model.table.Session;

import java.util.Objects;

public class LockActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_digital_print);
        Objects.requireNonNull(getSupportActionBar()).hide();
        mIdNumberEditText = findViewById(R.id.edit_text_activity_digital_print_id_number);
        mPasswordEditText = findViewById(R.id.edit_text_activity_digital_print_password);
        mConnectionProgressBar = findViewById(R.id.progress_bar_activity_digital_print_connection);
        mConnectionButton = findViewById(R.id.button_activity_digital_print_connection);
        mErrorTextView = findViewById(R.id.text_view_activity_digital_print_error);
        mHelpTextView = findViewById(R.id.text_view_activity_digital_print_helper);
        mLockImageView = findViewById(R.id.image_view_activity_digital_print_lock);
        mSession = new Session(this);
        mIdNumberEditText.setText("+227 " + mSession.getIdNumber());
        mIdNumberEditText.setEnabled(false);
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
                    mLockImageView.setImageResource(R.drawable.vector_rouge_error);
                    mErrorTextView.setText((String) helpString);
                    handler.postDelayed(runnable, 1000);

                }

                @Override
                public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
                    mLockImageView.setImageResource(R.drawable.vector_vert_success);
                    mErrorTextView.setText("");
                    // mDigitalPrintTable.onUpdate("1");
                    handler.postDelayed(home, 1);
                }

                @Override
                public void onAuthenticationFailed() {
                    mLockImageView.setImageResource(R.drawable.vector_rouge_error);
                    mErrorTextView.setText(R.string.unrecognized_fingerprint);
                    handler.postDelayed(runnable, 1000);
                }
            };
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            fingerprintManager.authenticate(null, cancellationSignal, 0, authenticationCallback, null);
        }
        mConnectionButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
            @Override
            public void onClick(View v) {

                if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_PHONE_STATE)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(LockActivity.this,
                            new String[]{Manifest.permission.READ_PHONE_STATE,Manifest.permission.CALL_PHONE,Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.POST_NOTIFICATIONS},
                            1);
                }
                else {

                    mAccount = new Account(mIdNumberEditText.getText().toString(),mPasswordEditText.getText().toString());
                    switch (mAccount.inputControl())
                    {
                        case "00":
                            inputData(
                                    R.drawable.forme_white_radius_100dp_border_rouge,
                                    R.drawable.forme_white_radius_100dp_border_rouge,
                                    R.string.login_error_00
                            );
                            break;
                        case "01":
                            inputData(
                                    R.drawable.forme_white_radius_100dp_border_rouge,
                                    R.drawable.forme_white_radius_10dp,
                                    R.string.register_error_0111
                            );
                            break;
                        case "10":
                            inputData(
                                    R.drawable.forme_white_radius_10dp,
                                    R.drawable.forme_white_radius_100dp_border_rouge,
                                    R.string.register_error_1101
                            );
                            break;
                        case "11":
                            if(mAccount.getPassword().equals(mSession.getPassword()))
                            {
                                mConnectionProgressBar.setVisibility(View.VISIBLE);
                                mConnectionButton.setText(R.string.register_succes_1111);
                                handler.postDelayed(home, 1);
                            }
                            else
                            {
                                inputData(
                                        R.drawable.forme_white_radius_10dp,
                                        R.drawable.forme_white_radius_100dp_border_rouge,
                                        R.string.incorrect_password
                                );
                            }

                            break;
                    }
                }
            }
        });
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
        mLockImageView.setImageResource(R.drawable.img_digital);
        mErrorTextView.setText(R.string.touch_the_fingerprint_sensor);
    }
    private void Home()
    {
        Lock.savePass(getApplicationContext(),1);
        Intent mainA = new Intent(LockActivity.this, MainActivity.class);
        startActivity(mainA);
        finish();
    }
    public void inputData(int idNumberForm , int passwordForm , int message)
    {
        mIdNumberEditText.setBackground(getDrawable(idNumberForm));
        mPasswordEditText.setBackground(getDrawable(passwordForm));
        mErrorTextView.setText(message);
    }
    private EditText mIdNumberEditText;
    private EditText mPasswordEditText;
    private ProgressBar mConnectionProgressBar;
    private Button mConnectionButton;
    private TextView mErrorTextView;
    private ImageView mLockImageView;
    private TextView mHelpTextView;
    private Account mAccount;
    private Session mSession;
    private FingerprintManager fingerprintManager;
    private CancellationSignal cancellationSignal;
    private Handler handler;
    private Runnable runnable;
    private Runnable home;
}