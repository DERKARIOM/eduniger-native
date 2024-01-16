package com.fabi.Controleur;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
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

import com.fabi.Model.Account;
import com.fabi.Model.Session;
import com.fabi.Model.UpdateDialog;
import com.fabi.Model.UserTable;
import com.example.fabi.R;
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
       mSession = new Session(this);
       mUserTable = new UserTable(this);
       mJeton = "null";
       mDataBase = openOrCreateDatabase("data.db",MODE_PRIVATE,null);

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
               mAccount = new Account(mIdNumberEditText.getText().toString(), mEmailEditText.getText().toString(), mPasswordEditText.getText().toString(),"ras");
               switch (mAccount.register(mPasswordConfirmEditText.getText().toString()))
               {
                   case "0000":
                       mErrorTextView.setText("Veuillez remplir ces champs svp");
                       mIdNumberEditText.setBackground(getResources().getDrawable(R.drawable.forme_white_radius_100dp_border_rouge));
                       mEmailEditText.setBackground(getResources().getDrawable(R.drawable.forme_white_radius_100dp_border_rouge));
                       mPasswordEditText.setBackground(getResources().getDrawable(R.drawable.forme_white_radius_100dp_border_rouge));
                       mPasswordConfirmEditText.setBackground(getResources().getDrawable(R.drawable.forme_white_radius_100dp_border_rouge));
                       break;
                   case "0111":
                       mErrorTextView.setText("Votre matricule svp");
                       mIdNumberEditText.setBackground(getResources().getDrawable(R.drawable.forme_white_radius_100dp_border_rouge));
                       mEmailEditText.setBackground(getResources().getDrawable(R.drawable.forme_white_radius_10dp));
                       mPasswordEditText.setBackground(getResources().getDrawable(R.drawable.forme_white_radius_10dp));
                       mPasswordConfirmEditText.setBackground(getResources().getDrawable(R.drawable.forme_white_radius_10dp));
                       break;
                   case "1011":
                       mErrorTextView.setText("Votre email svp");
                       mEmailEditText.setBackground(getResources().getDrawable(R.drawable.forme_white_radius_100dp_border_rouge));
                       mIdNumberEditText.setBackground(getResources().getDrawable(R.drawable.forme_white_radius_10dp));
                       mPasswordEditText.setBackground(getResources().getDrawable(R.drawable.forme_white_radius_10dp));
                       mPasswordConfirmEditText.setBackground(getResources().getDrawable(R.drawable.forme_white_radius_10dp));
                       break;
                   case "1111":
                       mConnectionProgressBar.setVisibility(View.VISIBLE);
                       mConnectionButton.setText("Connexion...");
                       Http http = new Http();
                       http.execute("http://192.168.43.1:2222/fabi/android/register.php",mAccount.getIdNumber(),mAccount.getEmail(),mAccount.getPassword());
                       break;
                   case "1110":
                       mErrorTextView.setText("Erreur de confirmation");
                       mIdNumberEditText.setBackground(getResources().getDrawable(R.drawable.forme_white_radius_10dp));
                       mEmailEditText.setBackground(getResources().getDrawable(R.drawable.forme_white_radius_10dp));
                       mPasswordEditText.setBackground(getResources().getDrawable(R.drawable.forme_white_radius_100dp_border_rouge));
                       mPasswordConfirmEditText.setBackground(getResources().getDrawable(R.drawable.forme_white_radius_100dp_border_rouge));
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

    // Methode pour la requette okhttp enfin de creer un compte a un utilisateur
    private class Http extends AsyncTask<String,Void,String> {
        @Override
        protected String doInBackground(String... params) {

            try {
                OkHttpClient client = new OkHttpClient();
                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("matricule",params[1])
                        .addFormDataPart("email",params[2])
                        .addFormDataPart("motdepasse",params[3])
                        .addFormDataPart("jeton",mJeton)
                        .addFormDataPart("version","1.0.0")
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
            if(jsonData != null)
            {
                if(jsonData.equals("matriculeEx")) {
                    mErrorTextView.setText("Ce compte existe");
                    mIdNumberEditText.setBackground(getResources().getDrawable(R.drawable.forme_white_radius_100dp_border_rouge));
                    mEmailEditText.setBackground(getResources().getDrawable(R.drawable.forme_white_radius_10dp));
                    mPasswordEditText.setBackground(getResources().getDrawable(R.drawable.forme_white_radius_10dp));
                    mPasswordConfirmEditText.setBackground(getResources().getDrawable(R.drawable.forme_white_radius_10dp));
                    mConnectionProgressBar.setVisibility(View.INVISIBLE);
                    mConnectionButton.setText("Connexion");
                }
                else
                {
                    if(jsonData.equals("emailEx")) {
                        mErrorTextView.setText("L'adresse email existe déjà");
                        mIdNumberEditText.setBackground(getResources().getDrawable(R.drawable.forme_white_radius_10dp));
                        mEmailEditText.setBackground(getResources().getDrawable(R.drawable.forme_white_radius_100dp_border_rouge));
                        mPasswordEditText.setBackground(getResources().getDrawable(R.drawable.forme_white_radius_10dp));
                        mPasswordConfirmEditText.setBackground(getResources().getDrawable(R.drawable.forme_white_radius_10dp));
                        mConnectionProgressBar.setVisibility(View.INVISIBLE);
                        mConnectionButton.setText("Connexion");
                    }
                    else
                    {
                        if(jsonData.equals("matriculeIn"))
                        {
                            mErrorTextView.setText("Matricule introuvable");
                            mIdNumberEditText.setBackground(getResources().getDrawable(R.drawable.forme_white_radius_100dp_border_rouge));
                            mEmailEditText.setBackground(getResources().getDrawable(R.drawable.forme_white_radius_10dp));
                            mPasswordEditText.setBackground(getResources().getDrawable(R.drawable.forme_white_radius_10dp));
                            mPasswordConfirmEditText.setBackground(getResources().getDrawable(R.drawable.forme_white_radius_10dp));
                            mConnectionProgressBar.setVisibility(View.INVISIBLE);
                            mConnectionButton.setText("Connexion");
                        }
                        else
                        {
                            if(jsonData.equals("update"))
                            {
                                Update();
                                mConnectionProgressBar.setVisibility(View.INVISIBLE);
                                mConnectionButton.setText("Connexion");
                            }
                            else
                            {
                                JSONObject jsonObject = null;
                                try {
                                    jsonObject = new JSONObject(jsonData);
                                } catch (JSONException e) {
                                    throw new RuntimeException(e);
                                }
                                try {
                                    mUserTable.insert(jsonObject.getString("matriculeUt"),
                                            jsonObject.getString("nomUt"),
                                            jsonObject.getString("prenomUt"),
                                            jsonObject.getString("statusUt"));
                                } catch (JSONException e) {
                                    throw new RuntimeException(e);
                                }
                                mSession.onCreate(mDataBase);
                                try {
                                    mSession.insert(jsonObject.getString("matriculeUt"));
                                } catch (JSONException e) {
                                    throw new RuntimeException(e);
                                }
                                Intent  home= new Intent(RegisterActivity.this , MainActivity.class);
                                startActivity(home);
                                finish();
                            }
                        }
                    }
                }
            }
            else
            {
                mErrorTextView.setText("Aucune conexion");
                mConnectionProgressBar.setVisibility(View.INVISIBLE);
                mConnectionButton.setText("Connexion");
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
    private SQLiteDatabase mDataBase;
    private UserTable mUserTable;
    private Session mSession;
    private String mJeton;
    private ProgressBar mConnectionProgressBar;
    private Account mAccount;
}