package com.ninotech.eduniger.controleur.activity;

import android.annotation.SuppressLint;
import android.app.UiModeManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.ninotech.eduniger.model.data.Account;
import com.ninotech.eduniger.controleur.dialog.UpdateDialog;
import com.ninotech.eduniger.R;
import com.google.firebase.messaging.FirebaseMessaging;
import com.ninotech.eduniger.model.data.PasswordUtil;
import com.ninotech.eduniger.model.data.Server;
import com.ninotech.eduniger.model.data.Themes;

import java.io.IOException;
import java.util.Objects;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import com.ninotech.eduniger.model.net.ApiClient;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import org.json.JSONException;
import org.json.JSONObject;

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

    // ✅ AJOUTÉ : boutons toggle + états visibilité
    private ImageButton mTogglePasswordButton;
    private ImageButton mTogglePasswordConfirmButton;
    private boolean mIsPasswordVisible = false;
    private boolean mIsPasswordConfirmVisible = false;

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

        mHttpClient = ApiClient.getInstance(this);
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

        // ✅ AJOUTÉ : récupération des boutons toggle
        mTogglePasswordButton = findViewById(R.id.image_button_toggle_password);
        mTogglePasswordConfirmButton = findViewById(R.id.image_button_toggle_password_confirm);
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

        // ✅ AJOUTÉ : toggle mot de passe
        mTogglePasswordButton.setOnClickListener(v -> {
            mIsPasswordVisible = !mIsPasswordVisible;

            if (mIsPasswordVisible) {
                mPasswordEditText.setInputType(
                        InputType.TYPE_CLASS_TEXT |
                                InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                );
                mTogglePasswordButton.setImageResource(R.drawable.ic_visibility_on);
            } else {
                mPasswordEditText.setInputType(
                        InputType.TYPE_CLASS_TEXT |
                                InputType.TYPE_TEXT_VARIATION_PASSWORD
                );
                mTogglePasswordButton.setImageResource(R.drawable.ic_visibility_off);
            }
            // Replacer le curseur à la fin
            mPasswordEditText.setSelection(mPasswordEditText.getText().length());
        });

        // ✅ AJOUTÉ : toggle confirmer mot de passe
        mTogglePasswordConfirmButton.setOnClickListener(v -> {
            mIsPasswordConfirmVisible = !mIsPasswordConfirmVisible;

            if (mIsPasswordConfirmVisible) {
                mPasswordConfirmEditText.setInputType(
                        InputType.TYPE_CLASS_TEXT |
                                InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                );
                mTogglePasswordConfirmButton.setImageResource(R.drawable.ic_visibility_on);
            } else {
                mPasswordConfirmEditText.setInputType(
                        InputType.TYPE_CLASS_TEXT |
                                InputType.TYPE_TEXT_VARIATION_PASSWORD
                );
                mTogglePasswordConfirmButton.setImageResource(R.drawable.ic_visibility_off);
            }
            // Replacer le curseur à la fin
            mPasswordConfirmEditText.setSelection(mPasswordConfirmEditText.getText().length());
        });
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
        } else {
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
                // Ancien : Server.getUrlApi(...) + "register.php" (script PHP historique du
                // serveur "fabi", hors de portee de l'audit/API Laravel "eduniger"). On utilise
                // desormais le vrai endpoint Laravel deja en place cote serveur.
                String serverUrl = Server.getUrlHostProd(getApplicationContext()) + "/api/user/register";

                // Le champ "numero" (mIdNumberEditText) ne correspond a aucune colonne du
                // modele User cote Laravel (idUser est un UUID genere serveur) : on l'envoie
                // comme phoneNumber, colonne existante et deja utilisee pour la connexion.
                JSONObject payload = new JSONObject();
                payload.put("name", mAccount.getName());
                payload.put("firstName", mAccount.getFirstName());
                payload.put("email", mAccount.getEmail());
                payload.put("phoneNumber", mAccount.getIdNumber());
                payload.put("profession", mAccount.getProfession());
                payload.put("password", mAccount.getPassword());
                // Token FCM (notifications push), recupere en tache de fond des onCreate()
                // (voir initializeFirebaseToken()) : envoye seulement s'il s'agit d'un vrai
                // token (pas le sentinel "null" utilise quand Firebase n'a pas encore
                // repondu au moment de la soumission du formulaire).
                if (mJeton != null && !"null".equals(mJeton)) {
                    payload.put("fcmToken", mJeton);
                }

                RequestBody requestBody = RequestBody.create(
                        MediaType.parse("application/json; charset=utf-8"),
                        payload.toString());

                Request request = new Request.Builder()
                        .url(serverUrl)
                        .post(requestBody)
                        .build();

                try (Response response = mHttpClient.newCall(request).execute()) {
                    String body = response.body() != null ? response.body().string() : null;
                    String sentinel = translateRegisterResponse(response.code(), body);
                    runOnUiThread(() -> handleRegistrationResponse(sentinel));
                }

            } catch (IOException e) {
                runOnUiThread(() -> {
                    Toast.makeText(RegisterActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    resetConnectionButton();
                });
            } catch (JSONException e) {
                Log.e(TAG, "Failed to build/parse registration payload", e);
                runOnUiThread(this::showConnectionError);
            }
        }).start();
    }

    /**
     * Traduit le code HTTP + corps de reponse Laravel vers le protocole existant
     * (Account.dataControl / handleRegistrationResponse), pour reutiliser l'affichage
     * d'erreur deja en place sans dupliquer la logique d'UI.
     *  - 2xx : on retransmet le JSON brut (sera parse par handleSuccessfulRegistration).
     *  - 422 : erreur de validation ; on regarde quel champ est en cause pour choisir un
     *    message pertinent (email deja utilise vs numero deja utilise).
     *  - 409 : doublon detecte au niveau base (conditions de concurrence), meme traitement.
     *  - autre / pas de reponse : message generique "pas de connexion".
     */
    private String translateRegisterResponse(int code, String body) {
        if (code >= 200 && code < 300 && body != null) {
            return body;
        }
        if (code == 422 && body != null) {
            // 422 = erreur de validation Laravel : on ne mappe VERS "existingEmail" /
            // "existingAccount" (messages "compte deja utilise") que si l'erreur concerne
            // reellement le champ email/numero en doublon. Toute autre cause de validation
            // (mot de passe trop court, champ manquant...) ne doit pas afficher un message
            // de doublon trompeur : on retombe sur le message generique "false"
            // (noConnection) plutot que d'inventer une cause fausse.
            try {
                JSONObject json = new JSONObject(body);
                JSONObject errors = json.optJSONObject("errors");
                if (errors != null && errors.has("email")) {
                    return "existingEmail";
                }
                if (errors != null && errors.has("phoneNumber")) {
                    return "existingAccount";
                }
            } catch (JSONException ignored) {
                // Corps non-JSON ou inattendu : on retombe sur "false" ci-dessous.
            }
            return "false";
        }
        if (code == 409) {
            // 409 = doublon detecte au niveau base (conditions de concurrence, message
            // generique cote serveur sans detail de champ) : pas d'ambiguite possible ici,
            // c'est bien un compte deja existant.
            return "existingAccount";
        }
        return "false";
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
                handleSuccessfulRegistration(jsonData);
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

    private void handleSuccessfulRegistration(String jsonData) {
        // Sauvegarde du token Sanctum emis par UserAuthController::register, au meme titre
        // que pour la connexion (voir LoginActivity.handleSuccessfulLogin pour le detail du
        // choix de stockage access==refresh token, Sanctum n'emettant qu'un seul token ici).
        try {
            JSONObject root = new JSONObject(jsonData);
            String token = root.getString("token");
            com.ninotech.eduniger.model.data.TokenStore tokenStore =
                    new com.ninotech.eduniger.model.data.TokenStore(getApplicationContext());
            tokenStore.saveTokens(token, token, java.util.concurrent.TimeUnit.DAYS.toSeconds(3650));
            com.ninotech.eduniger.controleur.fragment.LibraryFragment
                    .saveMemberSince(getApplicationContext(), root.optJSONObject("user"));
        } catch (JSONException e) {
            Log.e(TAG, "Failed to parse token from registration response", e);
        }

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
            // Ancien : lien vers la fiche Play Store de com.ninotech.fabi, reliquat du
            // rebranding Fabi -> EduNiger. Le vrai applicationId de cette app est
            // com.ninotech.eduniger (build.gradle).
            String url = "https://play.google.com/store/apps/details?id=com.ninotech.eduniger";
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        });

        updateDialog.build();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHttpClient = null;
    }
}