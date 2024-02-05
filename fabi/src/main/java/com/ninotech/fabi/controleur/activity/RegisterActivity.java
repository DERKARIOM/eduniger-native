package com.ninotech.fabi.controleur.activity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
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
import com.ninotech.fabi.controleur.dialog.UpdateDialog;
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

public class RegisterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        getSupportActionBar().hide();


        /* Initialisation des attributs membre(propiete) */
       mIdNumberEditText = findViewById(R.id.edit_textRegister_activity_id_number);
       mEmailEditText = findViewById(R.id.edit_text_register_email);
       mPasswordEditText = findViewById(R.id.edit_text_register_password);
       mPasswordConfirmEditText = findViewById(R.id.edit_text_register_password_confirm);
       mConnectionButton = findViewById(R.id.button_register_connection);
       mLoginTextView = findViewById(R.id.text_view_register_login);
       mErrorTextView = findViewById(R.id.text_view_register_error);
       mConnectionProgressBar = findViewById(R.id.progress_bar_register_connection);
       mJeton = "null";

        /* Generation de jeton FireBase */
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            Log.w("TAG", "Erreur de generation du jeton", task.getException());
                            return;
                        }
                        // Generation du nouveau jeton
                        mJeton = task.getResult();
                    }
                });

        /* En cliquant sur le boutton de connection2 */
       mConnectionButton.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               mAccount = new Account(
                       mIdNumberEditText.getText().toString(),
                       mEmailEditText.getText().toString(),
                       mPasswordEditText.getText().toString(),
                       "ras");
               switch (mAccount.inputControl(mPasswordConfirmEditText.getText().toString()))
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
                       mConnectionProgressBar.setVisibility(View.VISIBLE);
                       mConnectionButton.setText(R.string.register_succes_1111);
                       RegisterSyn registerSyn = new RegisterSyn();
                       registerSyn.execute(
                               getResources().getString(R.string.ip_server_android) + "Register.php",
                               mAccount.getIdNumber(),
                               mAccount.getEmail(),
                               mAccount.getPassword()
                       );
                       break;
               }
           }
       });

       /* En cliquant sur le TextView ce connecter */
       mLoginTextView.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               Intent login = new Intent(RegisterActivity.this, LoginActivity.class);
               startActivity(login);
           }
       });
    }

    public void inputControl(int idNumberForm , int emailForm , int passwordForm , int passwordConfirmForm , int message)
    {
        mIdNumberEditText.setBackground(getResources().getDrawable(idNumberForm));
        mEmailEditText.setBackground(getResources().getDrawable(emailForm));
        mPasswordEditText.setBackground(getResources().getDrawable(passwordForm));
        mPasswordConfirmEditText.setBackground(getResources().getDrawable(passwordConfirmForm));
        mErrorTextView.setText(message);
    }
    public void dataControl(int idNumberIco , int emailIco , int passwordIco , int passwordConfirmIco , int message)
    {
        inputControl(idNumberIco,emailIco,passwordIco,passwordConfirmIco,message);
        mConnectionProgressBar.setVisibility(View.INVISIBLE);
        mConnectionButton.setText(R.string.button_text_connection);
    }

    // Methode pour la requette okhttp enfin de creer un compte a un utilisateur
    private class RegisterSyn extends AsyncTask<String,Void,String> {
        @Override
        protected String doInBackground(String... params) {

            try {
                OkHttpClient client = new OkHttpClient();
                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("idNumber",params[1])
                        .addFormDataPart("email",params[2])
                        .addFormDataPart("password",params[3])
                        .addFormDataPart("token",mJeton)
                        .addFormDataPart("version", getResources().getString(R.string.app_version))
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
                    Toast.makeText(RegisterActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
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
                case "0111_1":
                        dataControl(
                                R.drawable.forme_white_radius_100dp_border_rouge,
                                R.drawable.forme_white_radius_10dp,
                                R.drawable.forme_white_radius_10dp,
                                R.drawable.forme_white_radius_10dp,
                                R.string.register_error_0111_1_data
                        );
                    break;
                case "1011":
                    dataControl(
                            R.drawable.forme_white_radius_10dp,
                            R.drawable.forme_white_radius_100dp_border_rouge,
                            R.drawable.forme_white_radius_10dp,
                            R.drawable.forme_white_radius_10dp,
                            R.string.register_error_1011_data
                    );
                    break;
                case "0111_0":
                    dataControl(
                            R.drawable.forme_white_radius_100dp_border_rouge,
                            R.drawable.forme_white_radius_10dp,
                            R.drawable.forme_white_radius_10dp,
                            R.drawable.forme_white_radius_10dp,
                            R.string.register_error_0111_0_data
                    );
                    break;
                case "update":
                    Update();
                    mConnectionProgressBar.setVisibility(View.INVISIBLE);
                    mConnectionButton.setText(R.string.button_text_connection);
                    break;
                case "1111":
                    JSONObject jsonObject = null;
                    try {
                        jsonObject = new JSONObject(jsonData);
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                    try {
                        if(mAccount.register(getApplicationContext(), jsonObject.getString("name"), jsonObject.getString("firstName") ,jsonObject.getString("department"), jsonObject.getString("section"),mAccount.getEmail(),jsonObject.getString("profile")))
                        {
                            if(mAccount.login(getApplicationContext()))
                            {
                                Intent  home= new Intent(RegisterActivity.this , MainActivity.class);
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
                default:
                    mErrorTextView.setText(R.string.no_connection);
                    mConnectionProgressBar.setVisibility(View.INVISIBLE);
                    mConnectionButton.setText(R.string.button_text_connection);
                    break;
            }
        }
    }
    private void Update(){
        UpdateDialog updateDialog = new UpdateDialog(this);
        updateDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        updateDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        updateDialog.build();
    }
    private EditText mIdNumberEditText;
    private EditText mPasswordEditText;
    private EditText mPasswordConfirmEditText;
    private Button mConnectionButton;
    private TextView mLoginTextView;
    private TextView mErrorTextView;
    private EditText mEmailEditText;
    private String mJeton;
    private ProgressBar mConnectionProgressBar;
    private Account mAccount;
}