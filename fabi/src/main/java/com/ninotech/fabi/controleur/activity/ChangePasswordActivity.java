package com.ninotech.fabi.controleur.activity;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
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
import com.ninotech.fabi.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Objects;

import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ChangePasswordActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);
        Objects.requireNonNull(getSupportActionBar()).hide();
        mIdNumberEditText = findViewById(R.id.edit_text_activity_change_password_id_number);
        mMailEditText = findViewById(R.id.edit_text_activity_change_password_mail);
        mPasswordEditText = findViewById(R.id.edit_text_activity_change_password_password);
        mConfirmPassword = findViewById(R.id.edit_text_activity_change_password_confirm_password);
        mConnectionButton = findViewById(R.id.button_activity_change_password_connection);
        mChangeMailTextView = findViewById(R.id.text_view_activity_change_password_change_mail);
        mErrorTextView = findViewById(R.id.text_view_activity_change_password_error);
        mConnectionProgressBar = findViewById(R.id.progress_bar_change_password_connection);
        mSession = new Session(this);
        mJeton = "null";

        /* Generation de jeton FireBase */
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            Log.e("ErrChangePasswordJeton", "Erreur de generation du jeton", task.getException());
                            return;
                        }
                        // Generation du nouveau jeton
                        mJeton = task.getResult();
                    }
                });

        /* En cliquant sur le boutton de connection */
        mConnectionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAccount = new Account(mIdNumberEditText.getText().toString(),mMailEditText.getText().toString(),mPasswordEditText.getText().toString(),null);
                switch (mAccount.inputControl(mConfirmPassword.getText().toString()))
                {
                    case "0000":
                        inputControl(
                                R.drawable.forme_white_radius_100dp_border_rouge,
                                R.drawable.forme_white_radius_100dp_border_rouge,
                                R.drawable.forme_white_radius_100dp_border_rouge,
                                R.drawable.forme_white_radius_100dp_border_rouge,
                                R.string.register_error_0000
                        );
                        break;
                    case "0111":
                        inputControl(
                                R.drawable.forme_white_radius_100dp_border_rouge,
                                R.drawable.forme_white_radius_10dp,
                                R.drawable.forme_white_radius_10dp,
                                R.drawable.forme_white_radius_10dp,
                                R.string.register_error_0111
                        );
                        break;
                    case "1011":
                        inputControl(
                                R.drawable.forme_white_radius_10dp,
                                R.drawable.forme_white_radius_100dp_border_rouge,
                                R.drawable.forme_white_radius_10dp,
                                R.drawable.forme_white_radius_10dp,
                                R.string.register_error_1011
                        );
                        break;
                    case "1101":
                        inputControl(
                                R.drawable.forme_white_radius_10dp,
                                R.drawable.forme_white_radius_10dp,
                                R.drawable.forme_white_radius_100dp_border_rouge,
                                R.drawable.forme_white_radius_10dp,
                                R.string.register_error_1101
                        );
                        break;
                    case "1110":
                        inputControl(
                                R.drawable.forme_white_radius_10dp,
                                R.drawable.forme_white_radius_10dp,
                                R.drawable.forme_white_radius_10dp,
                                R.drawable.forme_white_radius_100dp_border_rouge,
                                R.string.register_error_1110
                        );
                        break;
                    case "1100":
                        inputControl(
                                R.drawable.forme_white_radius_10dp,
                                R.drawable.forme_white_radius_10dp,
                                R.drawable.forme_white_radius_100dp_border_rouge,
                                R.drawable.forme_white_radius_100dp_border_rouge,
                                R.string.register_error_1100
                        );
                        break;
                    case "1111":
                        Toast.makeText(ChangePasswordActivity.this, "OK", Toast.LENGTH_SHORT).show();
                        mConnectionProgressBar.setVisibility(View.VISIBLE);
                        mConnectionButton.setText(R.string.register_succes_1111);
//                        RegisterActivity.RegisterSyn registerSyn = new RegisterActivity.RegisterSyn();
//                        registerSyn.execute(
//                                getResources().getString(R.string.ip_server_android) + "Register.php",
//                                mAccount.getIdNumber(),
//                                mAccount.getEmail(),
//                                mAccount.getPassword()
                        break;
                }
            }
        });

        /* En cliquant sur le TextView ce connecter */
        mChangeMailTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent email = new Intent(ChangePasswordActivity.this, EmailChangeActivity.class);
                startActivity(email);
            }
        });
    }

    public void inputControl(int idNumberForm , int emailForm , int passwordForm , int passwordConfirmForm , int message)
    {
        mIdNumberEditText.setBackground(getResources().getDrawable(idNumberForm));
        mMailEditText.setBackground(getResources().getDrawable(emailForm));
        mPasswordEditText.setBackground(getResources().getDrawable(passwordForm));
        mConfirmPassword.setBackground(getResources().getDrawable(passwordConfirmForm));
        mErrorTextView.setText(message);
    }
    private class Http extends AsyncTask<String,Void,String> {
        @Override
        protected String doInBackground(String... params) {

            try {
                OkHttpClient client = new OkHttpClient();
                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("matricule", mIdNumberEditText.getText().toString())
                        .addFormDataPart("email", mMailEditText.getText().toString())
                        .addFormDataPart("motdepasse", mPasswordEditText.getText().toString())
                        .addFormDataPart("jeton",mJeton)
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
                    Toast.makeText(ChangePasswordActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }

            }catch (Exception e)
            {
                return null;
            }
            return null;
        }
        @Override
        protected void onPostExecute(String jsonData){
            //Toast.makeText(NotificationService.this, response, Toast.LENGTH_SHORT).show();
            if(jsonData != null)
            {
                if(jsonData.equals("false"))
                {
                    mErrorTextView.setText("Matricule ou Email incorrect");
                    mIdNumberEditText.setBackground(getResources().getDrawable(R.drawable.forme_white_radius_100dp_border_rouge));
                    mMailEditText.setBackground(getResources().getDrawable(R.drawable.forme_white_radius_100dp_border_rouge));
                    mPasswordEditText.setBackground(getResources().getDrawable(R.drawable.forme_white_radius_10dp));
                    mConfirmPassword.setBackground(getResources().getDrawable(R.drawable.forme_white_radius_10dp));
                }
                else
                {
                    Toast.makeText(getApplicationContext(),"Heureux de vous revoir ", Toast.LENGTH_SHORT).show();
                    JSONObject jsonObject = null;
                    try {
                        jsonObject = new JSONObject(jsonData);
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                    try {
                        mSession.insert(jsonObject.getString("matricule"),"ras");
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                    Intent home = new Intent(ChangePasswordActivity.this, MainActivity.class);
                    startActivity(home);
                    finish();
                }
            }
            else
                mErrorTextView.setText("Aucune connexion");
        }
    }
    private EditText mIdNumberEditText;
    private EditText mPasswordEditText;
    private EditText mConfirmPassword;
    private Button mConnectionButton;
    private TextView mChangeMailTextView;
    private TextView mErrorTextView;
    private EditText mMailEditText;
    private SQLiteDatabase data;
    private Session mSession;
    private String mJeton;
    private Account mAccount;
    private ProgressBar mConnectionProgressBar;
}