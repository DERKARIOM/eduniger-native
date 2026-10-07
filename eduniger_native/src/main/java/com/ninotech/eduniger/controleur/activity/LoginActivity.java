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
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
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

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import com.ninotech.eduniger.model.net.ApiClient;
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

    // ✅ AJOUTÉ : bouton toggle + état visibilité mot de passe
    private ImageButton mTogglePasswordButton;
    private boolean mIsPasswordVisible = false;

    // Data
    private Account mAccount;
    private String mToken;
    private OkHttpClient mHttpClient;
    private static final int RC_SIGN_IN = 9001;
    private GoogleSignInClient mGoogleSignInClient;
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

        // ✅ AJOUTÉ : récupération du bouton toggle
        mTogglePasswordButton = findViewById(R.id.image_button_toggle_password);

        TextView registerTextView = findViewById(R.id.text_view_login_pass_register);
        TextView forgetPasswordTextView = findViewById(R.id.text_view_activity_login_forget_password);

        // Underline text views
        registerTextView.setPaintFlags(registerTextView.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        forgetPasswordTextView.setPaintFlags(forgetPasswordTextView.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
    }

    private void initializeData() {
        mToken = DEFAULT_TOKEN;
        mHttpClient = ApiClient.getInstance(this);
        mIsNightMode = isNightMode();
        setupGoogleSignIn();
    }

    private void setupGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestProfile()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
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

        // Google sign-in
        mGoogleLinearLayout.setOnClickListener(v -> launchGoogleSignIn());

        // ✅ AJOUTÉ : toggle affichage mot de passe
        mTogglePasswordButton.setOnClickListener(v -> {
            mIsPasswordVisible = !mIsPasswordVisible;

            if (mIsPasswordVisible) {
                // Afficher le mot de passe en clair
                mPasswordEditText.setInputType(
                        InputType.TYPE_CLASS_TEXT |
                                InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                );
                mTogglePasswordButton.setImageResource(R.drawable.ic_visibility_on);
            } else {
                // Masquer le mot de passe
                mPasswordEditText.setInputType(
                        InputType.TYPE_CLASS_TEXT |
                                InputType.TYPE_TEXT_VARIATION_PASSWORD
                );
                mTogglePasswordButton.setImageResource(R.drawable.ic_visibility_off);
            }

            // Replacer le curseur à la fin du texte saisi
            mPasswordEditText.setSelection(mPasswordEditText.getText().length());
        });
    }

    private void launchGoogleSignIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleGoogleSignInResult(task);
        }
    }

    private void handleGoogleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount googleAccount = completedTask.getResult(ApiException.class);
            performGoogleLogin(googleAccount);
        } catch (ApiException e) {
            Log.w(TAG, "Google sign-in failed, code: " + e.getStatusCode());
            mErrorTextView.setText(R.string.no_connection);
        }
    }

    private void performGoogleLogin(GoogleSignInAccount googleAccount) {
        setLoadingState(true);

        String googleId  = googleAccount.getId();
        String email     = googleAccount.getEmail()      != null ? googleAccount.getEmail()      : "";
        String name      = googleAccount.getFamilyName() != null ? googleAccount.getFamilyName() : "";
        String firstName = googleAccount.getGivenName()  != null ? googleAccount.getGivenName()  : "";

        new GoogleLoginTask(this).execute(
                Server.getUrlApi(getApplicationContext()) + "login_google.php",
                googleId,
                email,
                name,
                firstName
        );
    }

    private void setupAnimations() {
        Animation slideAnimation = AnimationUtils.loadAnimation(
                getApplicationContext(), R.anim.slide_down_up);
        mLoginImageView.startAnimation(slideAnimation);
    }

    private void retrieveFirebaseToken() {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.w(TAG, "Failed to get Firebase token", task.getException());
                        return;
                    }
                    mToken = task.getResult();
                    Log.d(TAG, "Firebase token pré-chargé : " + mToken);
                });
    }

    // ==================== Login Logic ====================

    private void handleLoginClick() {
        String idNumber = mIdNumberEditText.getText().toString().trim();
        String password = mPasswordEditText.getText().toString();

        mAccount = new Account(idNumber, PasswordUtil.hashPassword(password));
        mIsNightMode = isNightMode();

        String validationResult = mAccount.inputControl();

        if ("11".equals(validationResult)) {
            // ✅ Attendre le token FCM avant de lancer le login
            setLoadingState(true);
            retrieveFirebaseTokenThenLogin();
        } else {
            handleValidation(validationResult);
        }
    }

    private void retrieveFirebaseTokenThenLogin() {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        mToken = task.getResult();
                        Log.d(TAG, "Token FCM récupéré : " + mToken);
                    } else {
                        // Token non disponible → on envoie "null" quand même
                        mToken = DEFAULT_TOKEN;
                        Log.w(TAG, "Token FCM indisponible, login sans token");
                    }
                    // Lance le login dans tous les cas
                    performLogin();
                });
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
                // Ne devrait plus être appelé depuis handleLoginClick
                // mais conservé par sécurité
                setLoadingState(true);
                retrieveFirebaseTokenThenLogin();
                break;
        }
    }


    private void performLogin() {
        // Ancien : Server.getUrlApi(...) + "login.php" (script PHP historique du serveur
        // "fabi", hors de portee de l'audit/API Laravel "eduniger" ; retournait une reponse
        // HTML corrompue - voir UserAuthController::login cote serveur pour la contrepartie
        // Laravel reelle, deja utilisee ici).
        new LoginTask(this).execute(
                Server.getUrlHostProd(getApplicationContext()) + "/api/user/login",
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
            case "locked":
                handleAccountLocked();
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

    private void handleAccountLocked() {
        setLoadingState(false);
        mErrorTextView.setText(R.string.account_locked);
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
            // SECURITE : l'ancien "Log.d("310726", jsonData)" ecrivait l'integralite de la
            // reponse de connexion (dont le hash du mot de passe, et desormais les tokens
            // d'authentification) dans les logs systeme Android, lisibles via logcat par
            // toute application disposant de la permission READ_LOGS sur les anciennes
            // versions d'Android, ou via un simple "adb logcat" sur un appareil connecte.
            // Supprime : aucun secret ne doit jamais apparaitre dans les logs.
            // Reponse Laravel reelle (UserAuthController::login) : { message, user: {...}, token }
            // - et non plus le format hypothetique attendu par l'ancien code (champs a plat,
            // couple accessToken/refreshToken) qui ne correspondait a aucun endpoint reel.
            JSONObject root = new JSONObject(jsonData);
            JSONObject user = root.getJSONObject("user");

            mAccount.setName(user.optString("name", ""));
            mAccount.setFirstName(user.optString("firstName", ""));
            mAccount.setEmail(user.optString("email", ""));
            mAccount.setPhoneNumber(user.optString("phoneNumber", mAccount.getIdNumber()));
            mAccount.setProfession(user.optLong("profession", 0));
            // Le mot de passe (hache) n'est jamais renvoye par l'API (champ "hidden" cote
            // Laravel) : mAccount.getPassword() garde deja la valeur hachee localement par
            // handleLoginClick() avant l'appel reseau, inutile de la relire ici.

            // Sauvegarde securisee du token d'authentification Sanctum, voir TokenStore.java.
            // Sanctum n'emet ici qu'un seul token (pas de couple access/refresh, et
            // config/sanctum.php a 'expiration' => null : il n'expire pas cote serveur). On
            // le stocke comme access ET refresh token pour rester compatible avec
            // TokenStore.isLoggedIn() (qui teste la presence d'un refresh token) : il n'existe
            // pas de veritable endpoint de refresh cote Laravel pour les lecteurs, un futur
            // 401 effacera simplement la session locale (voir AuthAuthenticator).
            String token = root.getString("token");
            com.ninotech.eduniger.model.data.TokenStore tokenStore =
                    new com.ninotech.eduniger.model.data.TokenStore(getApplicationContext());
            tokenStore.saveTokens(token, token, java.util.concurrent.TimeUnit.DAYS.toSeconds(3650));
            com.ninotech.eduniger.controleur.fragment.LibraryFragment
                    .saveMemberSince(getApplicationContext(), user);

            boolean isAdmin = user.optBoolean("isAdmin", false);
            if (mAccount.register(getApplicationContext(), String.valueOf(isAdmin))) {
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

    private static class GoogleLoginTask extends AsyncTask<String, Void, String> {
        private final WeakReference<LoginActivity> activityRef;

        GoogleLoginTask(LoginActivity activity) {
            this.activityRef = new WeakReference<>(activity);
        }

        @Override
        protected String doInBackground(String... params) {
            LoginActivity activity = activityRef.get();
            if (activity == null || activity.mHttpClient == null) return null;

            try {
                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("google_id", params[1])
                        .addFormDataPart("email",     params[2])
                        .addFormDataPart("name",      params[3])
                        .addFormDataPart("firstName", params[4])
                        .addFormDataPart("token",     "ras")
                        .addFormDataPart("version",
                                activity.getResources().getString(R.string.app_version))
                        .build();

                Request request = new Request.Builder()
                        .url(params[0])
                        .post(requestBody)
                        .build();

                try (Response response = activity.mHttpClient.newCall(request).execute()) {
                    if (response.body() != null) {
                        Log.e("GooglePass", response.body().string());
                        return response.body().string();
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Google login network request failed", e);
                }
            } catch (Exception e) {
                Log.e(TAG, "Unexpected error in Google login task", e);
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
                // L'ecran de connexion mobile ne collecte qu'un "numero" (telephone) : on
                // l'envoie comme phoneNumber, UserAuthController::login accepte email OU
                // phoneNumber. Le mot de passe est deja hache cote client (PasswordUtil).
                JSONObject payload = new JSONObject();
                payload.put("phoneNumber", params[1]);
                payload.put("password", params[2]);
                // Token FCM (notifications push), deja recupere/attendu par
                // retrieveFirebaseTokenThenLogin() avant l'appel a performLogin() : on
                // l'envoie seulement s'il s'agit d'un vrai token (pas le sentinel
                // DEFAULT_TOKEN="null" utilise quand Firebase n'a pas repondu a temps).
                if (activity.mToken != null && !DEFAULT_TOKEN.equals(activity.mToken)) {
                    payload.put("fcmToken", activity.mToken);
                }

                RequestBody requestBody = RequestBody.create(
                        MediaType.parse("application/json; charset=utf-8"),
                        payload.toString());

                Request request = new Request.Builder()
                        .url(params[0])
                        .post(requestBody)
                        .build();

                try (Response response = activity.mHttpClient.newCall(request).execute()) {
                    String body = response.body() != null ? response.body().string() : null;

                    // Traduction des codes HTTP Laravel vers le protocole existant
                    // (Account.dataControl), pour reutiliser l'affichage d'erreur deja en
                    // place sans dupliquer la logique d'UI.
                    if (response.code() == 404) {
                        return "accountNotExist";
                    }
                    if (response.code() == 401) {
                        return "incorrectPassword";
                    }
                    if (response.code() == 429) {
                        return "accountLocked";
                    }
                    if (response.isSuccessful() && body != null) {
                        return body;
                    }
                    // 422 (validation) ou 5xx : pas de cas dedie dans l'ancien protocole,
                    // on retombe sur le message generique "pas de connexion".
                    return "false";
                } catch (IOException e) {
                    Log.e(TAG, "Network request failed", e);
                }
            } catch (org.json.JSONException e) {
                Log.e(TAG, "Failed to build login JSON payload", e);
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