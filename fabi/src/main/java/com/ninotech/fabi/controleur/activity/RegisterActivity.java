package com.ninotech.fabi.controleur.activity;

import android.annotation.SuppressLint;
import android.app.UiModeManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.ninotech.fabi.model.data.Account;
import com.ninotech.fabi.controleur.dialog.UpdateDialog;
import com.ninotech.fabi.R;
import com.google.firebase.messaging.FirebaseMessaging;
import com.ninotech.fabi.model.data.PasswordUtil;
import com.ninotech.fabi.model.data.Server;
import com.ninotech.fabi.model.data.Themes;

import java.io.IOException;
import java.util.Objects;

import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";

    // UI Components
    private EditText mNameEditText;
    private EditText mFirstNameEditText;
    private Spinner mProfessionSpinner;
    private EditText mIdNumberEditText;
    private EditText mPasswordEditText;
    private EditText mPasswordConfirmEditText;
    private EditText mEmailEditText;
    private Button mConnectionButton;
    private TextView mLoginTextView;
    private TextView mErrorTextView;
    private ProgressBar mConnectionProgressBar;

    // Data
    private Account mAccount;
    private String mJeton = "null";
    private OkHttpClient mHttpClient;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        Objects.requireNonNull(getSupportActionBar()).hide();

        initializeViews();
        setupProfessionSpinner();
        initializeFirebaseToken();
        setupClickListeners();

        // Initialiser OkHttpClient une seule fois
        mHttpClient = new OkHttpClient();
    }

    private void initializeViews() {
        mNameEditText = findViewById(R.id.edit_text_activity_register_name);
        mFirstNameEditText = findViewById(R.id.edit_text_activity_register_first_name);
        mProfessionSpinner = findViewById(R.id.spinner_activity_register_profession);
        mIdNumberEditText = findViewById(R.id.edit_text_activity_register_id_number);
        mEmailEditText = findViewById(R.id.edit_text_activity_register_email);
        mPasswordEditText = findViewById(R.id.edit_text_activity_register_password);
        mPasswordConfirmEditText = findViewById(R.id.edit_text_activity_register_password_confirm);
        mConnectionButton = findViewById(R.id.button_activity_register_connection);
        mLoginTextView = findViewById(R.id.text_view_activity_register_login);
        mErrorTextView = findViewById(R.id.text_view_activity_register_error);
        mConnectionProgressBar = findViewById(R.id.progress_bar_activity_register_connection);
    }

    private void setupProfessionSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.profesion_array,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mProfessionSpinner.setAdapter(adapter);
    }

    private void initializeFirebaseToken() {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.w(TAG, "Erreur de generation du jeton", task.getException());
                        return;
                    }
                    mJeton = task.getResult();
                });
    }

    private void setupClickListeners() {
        mConnectionButton.setOnClickListener(v -> handleRegistration());
        mLoginTextView.setOnClickListener(v -> navigateToLogin());
    }

    private void handleRegistration() {
        mAccount = new Account(
                mIdNumberEditText.getText().toString(),
                mNameEditText.getText().toString(),
                mFirstNameEditText.getText().toString(),
                mEmailEditText.getText().toString(),
                PasswordUtil.hashPassword(mPasswordEditText.getText().toString()),
                null,
                mProfessionSpinner.getSelectedItemId()
        );

        String hashedPasswordConfirm = PasswordUtil.hashPassword(
                mPasswordConfirmEditText.getText().toString()
        );

        if (isDarkMode()) {
            inputNight(hashedPasswordConfirm);
        } else {
            inputNoNight(hashedPasswordConfirm);
        }
    }

    private boolean isDarkMode() {
        String theme = Themes.getName(getApplicationContext());

        if ("night".equals(theme)) {
            return true;
        } else if ("notNight".equals(theme)) {
            return false;
        } else { // "system"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                UiModeManager uiModeManager = (UiModeManager) getSystemService(Context.UI_MODE_SERVICE);
                return uiModeManager.getNightMode() != UiModeManager.MODE_NIGHT_NO;
            }
            return false;
        }
    }

    private void inputNight(String hashedPasswordConfirm) {
        processInputValidation(hashedPasswordConfirm, true);
    }

    private void inputNoNight(String hashedPasswordConfirm) {
        processInputValidation(hashedPasswordConfirm, false);
    }

    private void processInputValidation(String hashedPasswordConfirm, boolean isDarkMode) {
        String validationResult = mAccount.inputControl(hashedPasswordConfirm);

        switch (validationResult) {
            case "0000":
                applyInputControl(isDarkMode, true, true, true, true, R.string.register_error_0000);
                break;
            case "0111":
                applyInputControl(isDarkMode, true, false, false, false, R.string.register_error_0111);
                break;
            case "1011":
                applyInputControl(isDarkMode, false, true, false, false, R.string.register_error_1011);
                break;
            case "1101":
                applyInputControl(isDarkMode, false, false, true, false, R.string.register_error_1101);
                break;
            case "1110":
                applyInputControl(isDarkMode, false, false, false, true, R.string.register_error_1110);
                break;
            case "1100":
                applyInputControl(isDarkMode, false, false, true, true, R.string.register_error_1100);
                break;
            case "1111":
                handleSuccessfulValidation(isDarkMode);
                break;
        }
    }

    private void handleSuccessfulValidation(boolean isDarkMode) {
        if (mProfessionSpinner.getSelectedItemPosition() != 0) {
            mConnectionProgressBar.setVisibility(View.VISIBLE);
            mConnectionButton.setText(R.string.register_succes_1111);
            performRegistration();
        } else {
            applyInputControl(isDarkMode, false, false, false, false, R.string.register_error_1100);
            mErrorTextView.setText("Votre profession svp ?");
        }
    }

    private void applyInputControl(boolean isDarkMode, boolean idError, boolean emailError,
                                   boolean passError, boolean confirmError, int messageResId) {
        int normalDrawable = isDarkMode ?
                R.drawable.forme_black3_radius_10dp : R.drawable.forme_white_radius_10dp;
        int errorDrawable = isDarkMode ?
                R.drawable.forme_black3_radius_100dp_border_rouge : R.drawable.forme_white_radius_100dp_border_rouge;

        mIdNumberEditText.setBackground(getResources().getDrawable(idError ? errorDrawable : normalDrawable));
        mEmailEditText.setBackground(getResources().getDrawable(emailError ? errorDrawable : normalDrawable));
        mPasswordEditText.setBackground(getResources().getDrawable(passError ? errorDrawable : normalDrawable));
        mPasswordConfirmEditText.setBackground(getResources().getDrawable(confirmError ? errorDrawable : normalDrawable));
        mErrorTextView.setText(messageResId);
    }

    private void performRegistration() {
        new Thread(() -> {
            try {
                String serverUrl = Server.getUrlApi(getApplicationContext()) + "register.php";

                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("id_user", mAccount.getIdNumber())
                        .addFormDataPart("name", mAccount.getName())
                        .addFormDataPart("first_name", mAccount.getFirstName())
                        .addFormDataPart("email", mAccount.getEmail())
                        .addFormDataPart("password", mAccount.getPassword())
                        .addFormDataPart("profession", String.valueOf(mAccount.getProfession()))
                        .addFormDataPart("version", getResources().getString(R.string.app_version))
                        .build();

                Request request = new Request.Builder()
                        .url(serverUrl)
                        .post(requestBody)
                        .build();

                Response response = mHttpClient.newCall(request).execute();
                String jsonData = response.body().string();

                runOnUiThread(() -> handleRegistrationResponse(jsonData));

            } catch (IOException e) {
                runOnUiThread(() -> {
                    Toast.makeText(RegisterActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    resetConnectionButton();
                });
            }
        }).start();
    }

    private void handleRegistrationResponse(String jsonData) {
        String dataControlResult = mAccount.dataControl(jsonData);
        boolean isDarkMode = isDarkMode();

        switch (dataControlResult) {
            case "0111_1":
                applyDataControl(isDarkMode, true, false, false, false,
                        R.string.register_error_0111_1_data);
                break;
            case "1011":
                applyDataControl(isDarkMode, false, true, false, false,
                        R.string.register_error_1011_data);
                break;
            case "update":
                showUpdateDialog();
                resetConnectionButton();
                break;
            case "1111":
                handleSuccessfulRegistration();
                break;
            default:
                showConnectionError();
                break;
        }
    }

    private void applyDataControl(boolean isDarkMode, boolean idError, boolean emailError,
                                  boolean passError, boolean confirmError, int messageResId) {
        applyInputControl(isDarkMode, idError, emailError, passError, confirmError, messageResId);
        resetConnectionButton();
    }

    private void handleSuccessfulRegistration() {
        if (mAccount.register(getApplicationContext(), "no")) {
            if (mAccount.login(getApplicationContext())) {
                Intent home = new Intent(RegisterActivity.this, MainActivity.class);
                startActivity(home);
                finish();
            }
        } else {
            showConnectionError();
        }
    }

    private void showConnectionError() {
        mErrorTextView.setText(R.string.no_connection);
        resetConnectionButton();
    }

    private void resetConnectionButton() {
        mConnectionProgressBar.setVisibility(View.INVISIBLE);
        mConnectionButton.setText(R.string.button_text_connection);
    }

    private void navigateToLogin() {
        Intent login = new Intent(RegisterActivity.this, LoginActivity.class);
        startActivity(login);
    }

    private void showUpdateDialog() {
        UpdateDialog updateDialog = new UpdateDialog(this);
        updateDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        updateDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;

        TextView annuler = updateDialog.findViewById(R.id.annuler);
        TextView installer = updateDialog.findViewById(R.id.installer);

        annuler.setOnClickListener(v -> updateDialog.cancel());

        installer.setOnClickListener(v -> {
            String url = "https://play.google.com/store/apps/details?id=com.ninotech.fabi&pcampaignid=web_share";
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        });

        updateDialog.build();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Libérer les ressources si nécessaire
        mHttpClient = null;
    }
}