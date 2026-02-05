package com.ninotech.eduniger.controleur.activity;

import android.app.UiModeManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RenderEffect;
import android.graphics.Shader;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.ninotech.eduniger.R;
import com.ninotech.eduniger.controleur.dialog.UpdateDialog;
import com.ninotech.eduniger.model.data.Account;
import com.ninotech.eduniger.model.data.PasswordUtil;
import com.ninotech.eduniger.model.data.Server;
import com.ninotech.eduniger.model.data.Themes;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.WeakReference;

import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private static final String DEFAULT_TOKEN = "null";
    private static final String UPDATE_URL = "https://eduniger.com";

    // Views
    private EditText mIdNumberEditText;
    private EditText mPasswordEditText;
    private Button mConnectionButton;
    private TextView mErrorTextView;
    private ProgressBar mConnectionProgressBar;
    private LinearLayout mGoogleLinearLayout;
    private ImageView mLoginImageView;
    private LinearLayout mIdNumberLinearLayout;

    // Data
    private Account mAccount;
    private String mToken;
    private OkHttpClient mHttpClient;
    private boolean mIsNightMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        hideActionBar();
        setupStatusBar();
        initializeViews();
        initializeData();
        setupListeners();
        setupAnimations();
        retrieveFirebaseToken();
    }

    private void hideActionBar() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
    }

    private void setupStatusBar() {
        // Blur effect for Android 12+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            View backgroundView = findViewById(R.id.backgroundView);
            if (backgroundView != null) {
                RenderEffect blurEffect = RenderEffect.createBlurEffect(
                        2000f, 2000f, Shader.TileMode.CLAMP);
                backgroundView.setRenderEffect(blurEffect);
            }
        }

        // Transparent status bar
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.TRANSPARENT);
            getWindow().setFlags(
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
            );
        }

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        );
    }

    private void initializeViews() {
        mIdNumberEditText = findViewById(R.id.edit_text_login_id_number);
        mPasswordEditText = findViewById(R.id.edit_text_login_password);
        mConnectionButton = findViewById(R.id.button_login_connection);
        mErrorTextView = findViewById(R.id.text_view_login_error);
        mConnectionProgressBar = findViewById(R.id.progress_bar_register_connection);
        mGoogleLinearLayout = findViewById(R.id.linear_layout_activity_login_google);
        mLoginImageView = findViewById(R.id.activity_login_image_view);
        mIdNumberLinearLayout = findViewById(R.id.activity_login_linear_layout_number);

        TextView registerTextView = findViewById(R.id.text_view_login_pass_register);
        TextView forgetPasswordTextView = findViewById(R.id.text_view_activity_login_forget_password);

        // Underline text views
        registerTextView.setPaintFlags(registerTextView.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        forgetPasswordTextView.setPaintFlags(forgetPasswordTextView.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
    }

    private void initializeData() {
        mToken = DEFAULT_TOKEN;
        mHttpClient = new OkHttpClient();
        mIsNightMode = isNightMode();
    }

    private void setupListeners() {
        // Connection button
        mConnectionButton.setOnClickListener(v -> handleLoginClick());

        // Register text
        TextView registerTextView = findViewById(R.id.text_view_login_pass_register);
        registerTextView.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        // Forget password text
        TextView forgetPasswordTextView = findViewById(R.id.text_view_activity_login_forget_password);
        forgetPasswordTextView.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, ChangePasswordActivity.class);
            startActivity(intent);
        });

        // Google sync (placeholder)
        mGoogleLinearLayout.setOnClickListener(v -> {
            Intent intent = new Intent(android.provider.Settings.ACTION_SYNC_SETTINGS);
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            }
        });
    }

    private void setupAnimations() {
        Animation slideAnimation = AnimationUtils.loadAnimation(
                getApplicationContext(), R.anim.slide_down_up);
        mLoginImageView.startAnimation(slideAnimation);
    }

    private void retrieveFirebaseToken() {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "Failed to get Firebase token", task.getException());
                            return;
                        }
                        mToken = task.getResult();
                        Log.d(TAG, "Firebase token retrieved successfully");
                    }
                });
    }

    // ==================== Login Logic ====================

    private void handleLoginClick() {
        String idNumber = mIdNumberEditText.getText().toString().trim();
        String password = mPasswordEditText.getText().toString();

        mAccount = new Account(idNumber, PasswordUtil.hashPassword(password));
        mIsNightMode = isNightMode();

        String validationResult = mAccount.inputControl();
        handleValidation(validationResult);
    }

    private void handleValidation(String validationCode) {
        switch (validationCode) {
            case "00":
                showInputError(
                        getFormBackground(true),
                        getFormBackground(true),
                        R.string.login_error_00
                );
                break;
            case "01":
                showInputError(
                        getFormBackground(true),
                        getFormBackground(false),
                        R.string.register_error_0111
                );
                break;
            case "10":
                showInputError(
                        getFormBackground(false),
                        getFormBackground(true),
                        R.string.register_error_1101
                );
                break;
            case "11":
                performLogin();
                break;
        }
    }

    private void performLogin() {
        setLoadingState(true);
        new LoginTask(this).execute(
                Server.getUrlApi(getApplicationContext()) + "login.php",
                mAccount.getIdNumber(),
                mAccount.getPassword()
        );
    }

    // ==================== UI State Management ====================

    private void setLoadingState(boolean loading) {
        if (loading) {
            mConnectionProgressBar.setVisibility(View.VISIBLE);
            mConnectionButton.setText(R.string.register_succes_1111);
            mConnectionButton.setEnabled(false);
        } else {
            mConnectionProgressBar.setVisibility(View.INVISIBLE);
            mConnectionButton.setText(R.string.button_text_connection);
            mConnectionButton.setEnabled(true);
        }
    }

    private void showInputError(int idNumberBackground, int passwordBackground, int messageRes) {
        mIdNumberLinearLayout.setBackground(getDrawable(idNumberBackground));
        mPasswordEditText.setBackground(getDrawable(passwordBackground));
        mErrorTextView.setText(messageRes);
    }

    private void showDataControlError(int idNumberBackground, int passwordBackground, int messageRes) {
        showInputError(idNumberBackground, passwordBackground, messageRes);
        setLoadingState(false);
    }

    // ==================== Theme Helpers ====================

    private boolean isNightMode() {
        String themeName = Themes.getName(getApplicationContext());

        switch (themeName) {
            case "night":
                return true;
            case "notNight":
                return false;
            case "system":
            default:
                return isSystemInNightMode();
        }
    }

    private boolean isSystemInNightMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            UiModeManager uiModeManager = (UiModeManager) getSystemService(Context.UI_MODE_SERVICE);
            return uiModeManager.getNightMode() == UiModeManager.MODE_NIGHT_YES;
        }

        // Fallback for older versions
        int currentNightMode = getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK;
        return currentNightMode == Configuration.UI_MODE_NIGHT_YES;
    }

    private int getFormBackground(boolean isError) {
        if (mIsNightMode) {
            return isError
                    ? R.drawable.forme_black3_radius_100dp_border_rouge
                    : R.drawable.forme_black3_radius_10dp;
        } else {
            return isError
                    ? R.drawable.forme_white_radius_100dp_border_rouge
                    : R.drawable.forme_white_radius_10dp;
        }
    }

    // ==================== Response Handling ====================

    private void handleLoginResponse(String jsonData) {
        String controlResult = mAccount.dataControl(jsonData);

        switch (controlResult) {
            case "00":
                handleAccountNotExist();
                break;
            case "10":
                handleIncorrectPassword();
                break;
            case "update":
                handleUpdateRequired();
                break;
            case "noConnection":
                handleNoConnection();
                break;
            default:
                handleSuccessfulLogin(jsonData);
                break;
        }
    }

    private void handleAccountNotExist() {
        showDataControlError(
                getFormBackground(true),
                getFormBackground(true),
                R.string.account_not_exist
        );
    }

    private void handleIncorrectPassword() {
        showDataControlError(
                getFormBackground(false),
                getFormBackground(true),
                R.string.incorrect_password
        );
    }

    private void handleUpdateRequired() {
        setLoadingState(false);
        showUpdateDialog();
    }

    private void handleNoConnection() {
        mErrorTextView.setText(R.string.no_connection);
        setLoadingState(false);
    }

    private void handleSuccessfulLogin(String jsonData) {
        try {
            JSONObject jsonObject = new JSONObject(jsonData);

            mAccount.setName(jsonObject.getString("name"));
            mAccount.setFirstName(jsonObject.getString("firstName"));
            mAccount.setEmail(jsonObject.getString("email"));
            mAccount.setPassword(jsonObject.getString("password"));
            mAccount.setProfession(Long.parseLong(jsonObject.getString("profession")));

            if (mAccount.register(getApplicationContext(), jsonObject.getString("isAdmin"))) {
                if (mAccount.login(getApplicationContext())) {
                    navigateToMainActivity();
                } else {
                    handleLoginRegistrationError();
                }
            } else {
                handleLoginRegistrationError();
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error parsing login response", e);
            mErrorTextView.setText(R.string.no_connection);
            setLoadingState(false);
        }
    }

    private void handleLoginRegistrationError() {
        mErrorTextView.setText(R.string.no_connection);
        setLoadingState(false);
    }

    private void navigateToMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    // ==================== Update Dialog ====================

    private void showUpdateDialog() {
        UpdateDialog dialog = new UpdateDialog(this);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;

        TextView cancelButton = dialog.findViewById(R.id.annuler);
        TextView installButton = dialog.findViewById(R.id.installer);

        cancelButton.setOnClickListener(v -> dialog.dismiss());
        installButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(UPDATE_URL));
            startActivity(intent);
        });

        dialog.build();
    }

    // ==================== AsyncTask ====================

    private static class LoginTask extends AsyncTask<String, Void, String> {
        private final WeakReference<LoginActivity> activityRef;

        LoginTask(LoginActivity activity) {
            this.activityRef = new WeakReference<>(activity);
        }

        @Override
        protected String doInBackground(String... params) {
            LoginActivity activity = activityRef.get();
            if (activity == null || activity.mHttpClient == null) return null;

            try {
                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("id_number", params[1])
                        .addFormDataPart("password", params[2])
                        .addFormDataPart("token", activity.mToken)
                        .addFormDataPart("version",
                                activity.getResources().getString(R.string.app_version))
                        .build();

                Request request = new Request.Builder()
                        .url(params[0])
                        .post(requestBody)
                        .build();

                try (Response response = activity.mHttpClient.newCall(request).execute()) {
                    if (response.body() != null) {
                        return response.body().string();
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Network request failed", e);
                }
            } catch (Exception e) {
                Log.e(TAG, "Unexpected error in login task", e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            LoginActivity activity = activityRef.get();
            if (activity != null) {
                activity.handleLoginResponse(result);
            }
        }
    }
}