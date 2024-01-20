package com.ninotech.fabi.controleur.activity;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.ninotech.fabi.model.data.Account;
import com.ninotech.fabi.model.table.Session;
import com.ninotech.fabi.controleur.dialog.UpdateDialog;
import com.ninotech.fabi.model.table.UserTable;
import com.ninotech.fabi.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

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

        /* Initialisation des attributs menbre */
        mDataBase = openOrCreateDatabase("data.db",MODE_PRIVATE,null);
        mIdNumberEditText = findViewById(R.id.edit_text_login_id_number);
        mPassewordEditText = findViewById(R.id.edit_text_login_password);
        mRegisterTextView = findViewById(R.id.text_view_login_pass_register);
        mConnectionButton = findViewById(R.id.button_login_connection);
        mHelperTextView = findViewById(R.id.text_view_login_helper);
        mErrorTextView = findViewById(R.id.text_view_login_error);
        mConnectionProgressBar = findViewById(R.id.progress_bar_login_connection);
        mSession = new Session(this);
        mUtilisateur = new UserTable(this);
        mJeton="null";

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
                mAccount = new Account(mIdNumberEditText.getText().toString(),mPassewordEditText.getText().toString());
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
                        loginSyn.execute(getResources().getString(R.string.ip_server) + "Login.php",mAccount.getIdNumber(),mAccount.getPassword());
                        break;
                }
            }
        });

        /* En Cliquant sur le TextView d' inscription */
        mRegisterTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent inscription = new Intent(LoginActivity.this , RegisterActivity.class);
                startActivity(inscription);
            }
        });

        /* En Cliquant sur le TextView d' aide */
        mHelperTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    if(mHelperTextView.getText().equals(getResources().getString(R.string.forgot_password)))
                    {
                        Intent changePasseWord = new Intent(LoginActivity.this, ChangePasswordActivity.class);
                        startActivity(changePasseWord);
                    }else
                        Toast.makeText(LoginActivity.this, "En cours de developement", Toast.LENGTH_SHORT).show();
                }catch (Exception e)
                {
                    Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    public void inputData(int idNumberForm , int passwordForm , int message)
    {
        mIdNumberEditText.setBackground(getResources().getDrawable(idNumberForm));
        mPassewordEditText.setBackground(getResources().getDrawable(passwordForm));
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
                        .addFormDataPart("matricule", params[1])
                        .addFormDataPart("motdepasse", params[2])
                        .addFormDataPart("jeton",mJeton)
                        .addFormDataPart("version",getResources().getString(R.string.app_version))
                        .build();
                Request request = new Request.Builder()
                        .url(params[0])
                        .post(requestBody)
                        .build();
                try {
                    Response response = client.newCall(request).execute();
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
            switch (mAccount.dataControl(jsonData))
            {
                case "00":
                    dataControl(
                            R.drawable.forme_white_radius_100dp_border_rouge,
                            R.drawable.forme_white_radius_100dp_border_rouge,
                            R.string.account_not_exist
                    );
                    break;
                case "10":
                    dataControl(
                            R.drawable.forme_white_radius_10dp,
                            R.drawable.forme_white_radius_100dp_border_rouge,
                            R.string.incorrect_password
                    );
                    mHelperTextView.setText("j'ai oublié mon mot de passe");
                    mHelperTextView.setTextColor(Color.parseColor("#E6FD1010"));
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
                    mConnectionButton.setText("Connexion");
                    break;
                default:
                    JSONObject jsonObject = null;
                    try {
                        jsonObject = new JSONObject(jsonData);
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                    try {
                        if(mAccount.connection(getApplicationContext(), jsonObject.getString("nomUt"), jsonObject.getString("prenomUt"), jsonObject.getString("statusUt"),jsonObject.getString("email")))
                        {
                            mSession.onCreate(mDataBase);
                            mSession.insert(mAccount.getIdNumber());
                            Intent  home= new Intent(LoginActivity.this , MainActivity.class);
                            startActivity(home);
                            finish();
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
                String url = "http://192.168.43.1:2222/build/fastpv.apk"; // Remplacez ceci par l'URL que vous souhaitez ouvrir
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
    private TextView mRegisterTextView;
    private SQLiteDatabase mDataBase;
    private TextView mErrorTextView;
    private TextView mHelperTextView;
    private Session mSession;
    private UserTable mUtilisateur;
    private String mJeton;
    private ProgressBar mConnectionProgressBar;
    private Account mAccount;
}