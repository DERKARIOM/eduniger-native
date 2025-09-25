package com.ninotech.fabi.controleur.activity;

import android.app.UiModeManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RenderEffect;
import android.graphics.Shader;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.ninotech.fabi.controleur.adapter.StatusBarAdapter;
import com.ninotech.fabi.model.data.Account;
import com.ninotech.fabi.controleur.dialog.UpdateDialog;
import com.ninotech.fabi.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;
import com.ninotech.fabi.model.data.PasswordUtil;
import com.ninotech.fabi.model.data.Server;
import com.ninotech.fabi.model.data.Themes;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        /* Masquer le action bar */
        getSupportActionBar().hide();
        View backgroundView = findViewById(R.id.backgroundView);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) { // Android 12+
            RenderEffect blurEffect = RenderEffect.createBlurEffect(2000f, 2000f, Shader.TileMode.CLAMP);
            backgroundView.setRenderEffect(blurEffect);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.TRANSPARENT); // rendre la status bar transparente
        }
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        );

        /* Initialisation des attributs menbre */
        mIdNumberEditText = findViewById(R.id.edit_text_login_id_number);
        mPassewordEditText = findViewById(R.id.edit_text_login_password);
        TextView registerTextView = findViewById(R.id.text_view_login_pass_register);
        TextView forgetPasswordTextView = findViewById(R.id.text_view_activity_login_forget_password);
        mConnectionButton = findViewById(R.id.button_login_connection);
        mErrorTextView = findViewById(R.id.text_view_login_error);
        mConnectionProgressBar = findViewById(R.id.progress_bar_register_connection);
        mGoogleLinearLayout = findViewById(R.id.linear_layout_activity_login_google);
        mLoginImageView = findViewById(R.id.activity_login_image_view);
        mIdNumberLinearLayout = findViewById(R.id.activity_login_linear_layout_number);
        mJeton="null";
        Animation pulseAnimImg = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_down_up);
        mLoginImageView.startAnimation(pulseAnimImg);
        registerTextView.setPaintFlags(registerTextView.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        forgetPasswordTextView.setPaintFlags(forgetPasswordTextView.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        forgetPasswordTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent forgetPassword = new Intent(LoginActivity.this, ChangePasswordActivity.class);
                startActivity(forgetPassword);
            }
        });
        /* Generation de jeton FireBase */
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            Log.w("TAG", "Erreur de generation", task.getException());
                            return;
                        }
                        // recuperation du nouveau jeton
                        mJeton = task.getResult();
                    }
                });

        /* En Cliquant sur le boutton de connexion */
        mConnectionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAccount = new Account(
                        mIdNumberEditText.getText().toString(),
                        PasswordUtil.hashPassword(mPassewordEditText.getText().toString()));
                UiModeManager uiModeManager = null;
                switch (Themes.getName(getApplicationContext()))
                {
                    case "system":
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            uiModeManager = (UiModeManager) getSystemService(Context.UI_MODE_SERVICE);
                        }
                        int currentMode = uiModeManager.getNightMode();
                        if (currentMode == UiModeManager.MODE_NIGHT_NO) {
                            // code mode jour
                            inputNoNight();
                        }
                        else
                        {
                            // code mode nuit
                            inputNight();
                        }
                        break;
                    case "notNight":
                        // code mode jour
                        inputNoNight();
                        break;
                    case "night":
                        inputNight();
                        break;
                }
            }
        });

        /* En Cliquant sur le TextView d' inscription */
        registerTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent inscription = new Intent(LoginActivity.this , RegisterActivity.class);
                startActivity(inscription);
            }
        });

        mGoogleLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentYoutube = new Intent(Settings.ACTION_SYNC_SETTINGS);
                if (intentYoutube.resolveActivity(getPackageManager()) != null) {
                   startActivity(intentYoutube);
                }
            }
        });

        /* En Cliquant sur le TextView d' aide */

    }
    public void inputNoNight()
    {
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
                mConnectionProgressBar.setVisibility(View.VISIBLE);
                mConnectionButton.setText(R.string.register_succes_1111);
                LoginSyn loginSyn = new LoginSyn();
                loginSyn.execute(Server.getIpServerAndroid(getApplicationContext()) + "Login.php",mAccount.getIdNumber(),mAccount.getPassword());
                break;
        }
    }
    public void inputNight()
    {
        switch (mAccount.inputControl())
        {
            case "00":
                inputData(
                        R.drawable.forme_black3_radius_100dp_border_rouge,
                        R.drawable.forme_black3_radius_100dp_border_rouge,
                        R.string.login_error_00
                );
                break;
            case "01":
                inputData(
                        R.drawable.forme_black3_radius_100dp_border_rouge,
                        R.drawable.forme_black3_radius_10dp,
                        R.string.register_error_0111
                );
                break;
            case "10":
                inputData(
                        R.drawable.forme_black3_radius_10dp,
                        R.drawable.forme_black3_radius_100dp_border_rouge,
                        R.string.register_error_1101
                );
                break;
            case "11":
                mConnectionProgressBar.setVisibility(View.VISIBLE);
                mConnectionButton.setText(R.string.register_succes_1111);
                LoginSyn loginSyn = new LoginSyn();
                loginSyn.execute(Server.getIpServerAndroid(getApplicationContext()) + "Login.php",mAccount.getIdNumber(),mAccount.getPassword());
                break;
        }
    }
    public void inputData(int idNumberForm , int passwordForm , int message)
    {
        mIdNumberLinearLayout.setBackground(getDrawable(idNumberForm));
        mPassewordEditText.setBackground(getDrawable(passwordForm));
        mErrorTextView.setText(message);
    }
    public void dataControl(int idNumberForm , int passwordForm , int message)
    {
        inputData(idNumberForm,passwordForm,message);
        mConnectionProgressBar.setVisibility(View.INVISIBLE);
        mConnectionButton.setText(R.string.button_text_connection);
    }
    /* Les methode de la Classe LoginActivity */
    private class LoginSyn extends AsyncTask<String,Void,String> {
        @Override
        protected String doInBackground(String... params) {

            try {
                OkHttpClient client = new OkHttpClient();
                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("idNumber", params[1])
                        .addFormDataPart("password", params[2])
                        .addFormDataPart("token",mJeton)
                        .addFormDataPart("version",getResources().getString(R.string.app_version))
                        .build();
                Request request = new Request.Builder()
                        .url(params[0])
                        .post(requestBody)
                        .build();
                try {
                    Response response = client.newCall(request).execute();
                    assert response.body() != null;
                    return response.body().string();
                }catch (IOException e)
                {
                    Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }

            }catch (Exception e)
            {
                return null;
            }
            return null;
        }
        @Override
        protected void onPostExecute(String jsonData){
            UiModeManager uiModeManager = null;
            switch (mAccount.dataControl(jsonData))
            {
                case "00":
                    switch (Themes.getName(getApplicationContext()))
                    {
                        case "system":
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                uiModeManager = (UiModeManager) getSystemService(Context.UI_MODE_SERVICE);
                            }
                            int currentMode = uiModeManager.getNightMode();
                            if (currentMode == UiModeManager.MODE_NIGHT_NO) {
                                // code mode jour
                                dataControl(
                                        R.drawable.forme_white_radius_100dp_border_rouge,
                                        R.drawable.forme_white_radius_100dp_border_rouge,
                                        R.string.account_not_exist
                                );
                            }
                            else
                            {
                                // code mode nuit
                                dataControl(
                                        R.drawable.forme_black3_radius_100dp_border_rouge,
                                        R.drawable.forme_black3_radius_100dp_border_rouge,
                                        R.string.account_not_exist
                                );
                            }
                            break;
                        case "notNight":
                            // code mode jour
                            dataControl(
                                    R.drawable.forme_white_radius_100dp_border_rouge,
                                    R.drawable.forme_white_radius_100dp_border_rouge,
                                    R.string.account_not_exist
                            );
                            break;
                        case "night":
                            dataControl(
                                    R.drawable.forme_black3_radius_100dp_border_rouge,
                                    R.drawable.forme_black3_radius_100dp_border_rouge,
                                    R.string.account_not_exist
                            );
                            break;
                    }
                    break;
                case "10":
                    switch (Themes.getName(getApplicationContext())) {
                        case "system":
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                uiModeManager = (UiModeManager) getSystemService(Context.UI_MODE_SERVICE);
                            }
                            int currentMode = uiModeManager.getNightMode();
                            if (currentMode == UiModeManager.MODE_NIGHT_NO) {
                                // code mode jour
                                dataControl(
                                        R.drawable.forme_white_radius_10dp,
                                        R.drawable.forme_white_radius_100dp_border_rouge,
                                        R.string.incorrect_password
                                );
                            } else {
                                        // code mode nuit
                                dataControl(
                                        R.drawable.forme_black3_radius_10dp,
                                        R.drawable.forme_black3_radius_100dp_border_rouge,
                                        R.string.incorrect_password
                                );
                            }
                            break;
                            case "notNight":
                                // code mode jour
                                dataControl(
                                        R.drawable.forme_white_radius_10dp,
                                        R.drawable.forme_white_radius_100dp_border_rouge,
                                        R.string.incorrect_password
                                );
                                break;
                        case "night":
                            dataControl(
                                    R.drawable.forme_black3_radius_10dp,
                                    R.drawable.forme_black3_radius_100dp_border_rouge,
                                    R.string.incorrect_password
                            );
                            break;
                    }
                mConnectionProgressBar.setVisibility(View.INVISIBLE);
            break;
            case "update":
                    Update();
                    mConnectionProgressBar.setVisibility(View.INVISIBLE);
                    mConnectionButton.setText(R.string.button_text_connection);
                    break;
                case "noConnection":
                    mErrorTextView.setText(R.string.no_connection);
                    mConnectionProgressBar.setVisibility(View.INVISIBLE);
                    mConnectionButton.setText(R.string.button_text_connection);
                    break;
                default:
                    JSONObject jsonObject = null;
                    try {
                        jsonObject = new JSONObject(jsonData);
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                    try {
                        mAccount.setName(jsonObject.getString("name"));
                        mAccount.setFirstName(jsonObject.getString("firstName"));
                        mAccount.setEmail(jsonObject.getString("email"));
                        mAccount.setPassword(jsonObject.getString("password"));
                        mAccount.setProfession(Long.parseLong(jsonObject.getString("profession")));
                        if(mAccount.register(getApplicationContext(),jsonObject.getString("isAdmin")))
                        {
                            if(mAccount.login(getApplicationContext()))
                            {
                                Intent  home= new Intent(LoginActivity.this , MainActivity.class);
                                startActivity(home);
                                finish();
                            }
                        }
                        else
                        {
                            mErrorTextView.setText(R.string.no_connection);
                            mConnectionProgressBar.setVisibility(View.INVISIBLE);
                            mConnectionButton.setText(R.string.button_text_connection);
                        }
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                    break;
            }
        }
    }
    private void Update(){
        UpdateDialog updateDialog = new UpdateDialog(this);
        updateDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        updateDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        TextView annuler = updateDialog.findViewById(R.id.annuler);
        TextView installer = updateDialog.findViewById(R.id.installer);
        annuler.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateDialog.cancel();
            }
        });

        installer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String url = "http://eduniger.com"; // Remplacez ceci par l'URL que vous souhaitez ouvrir
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intent);
            }
        });

        updateDialog.build();
    }

    /* Les attributs menbre */
    private EditText mIdNumberEditText;
    private EditText mPassewordEditText;
    private Button mConnectionButton;
    private TextView mErrorTextView;
    private TextView mHelperTextView;
    private String mJeton;
    private ProgressBar mConnectionProgressBar;
    private Account mAccount;
    private LinearLayout mGoogleLinearLayout;
    private ImageView mLoginImageView;
    private LinearLayout mIdNumberLinearLayout;
}