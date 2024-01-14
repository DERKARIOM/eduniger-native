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


        /* Initialisation des attributs menbre */
       mEditTextIdNumber = findViewById(R.id.EditMatricule2);
       mEditTextEmail = findViewById(R.id.EditMail);
       mEditTextPassword = findViewById(R.id.EditPasse2);
       mEditTextConfirmPassword = findViewById(R.id.EditConf);
       mButtonConnection = findViewById(R.id.ButtonConnect2);
       mTextViewLogin = findViewById(R.id.TextLogin);
       mTextViewError = findViewById(R.id.TextErr2);
       mProgressBarCirculaire = findViewById(R.id.progress_circularRegester);
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
       mButtonConnection.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               Account account = new Account(mEditTextIdNumber.getText().toString(), mEditTextEmail.getText().toString(), mEditTextPassword.getText().toString(),"ras");
               switch (account.register(mEditTextConfirmPassword.getText().toString()))
               {
                   case "0000":
                       mTextViewError.setText("Veuillez remplir ces champs svp");
                       mEditTextIdNumber.setBackground(getResources().getDrawable(R.drawable.forme_white_radius_100dp_border_rouge));
                       mEditTextEmail.setBackground(getResources().getDrawable(R.drawable.forme_white_radius_100dp_border_rouge));
                       mEditTextPassword.setBackground(getResources().getDrawable(R.drawable.forme_white_radius_100dp_border_rouge));
                       mEditTextConfirmPassword.setBackground(getResources().getDrawable(R.drawable.forme_white_radius_100dp_border_rouge));
                       break;
                   case "0111":
                       mTextViewError.setText("Votre matricule svp");
                       mEditTextIdNumber.setBackground(getResources().getDrawable(R.drawable.forme_white_radius_100dp_border_rouge));
                       mEditTextEmail.setBackground(getResources().getDrawable(R.drawable.forme_white_radius_10dp));
                       mEditTextPassword.setBackground(getResources().getDrawable(R.drawable.forme_white_radius_10dp));
                       mEditTextConfirmPassword.setBackground(getResources().getDrawable(R.drawable.forme_white_radius_10dp));
                       break;
                   case "1011":
                       mTextViewError.setText("Votre email svp");
                       mEditTextEmail.setBackground(getResources().getDrawable(R.drawable.forme_white_radius_100dp_border_rouge));
                       mEditTextIdNumber.setBackground(getResources().getDrawable(R.drawable.forme_white_radius_10dp));
                       mEditTextPassword.setBackground(getResources().getDrawable(R.drawable.forme_white_radius_10dp));
                       mEditTextConfirmPassword.setBackground(getResources().getDrawable(R.drawable.forme_white_radius_10dp));
                       break;
                   case "1111":
                       mProgressBarCirculaire.setVisibility(View.VISIBLE);
                       mButtonConnection.setText("Connexion...");
                       Http http = new Http();
                       http.execute("http://192.168.43.1:2222/fabi/android/register.php",account.getIdNumber(),account.getEmail(),account.getPassword());
                       break;
                   case "1110":
                       mTextViewError.setText("Erreur de confirmation");
                       mEditTextIdNumber.setBackground(getResources().getDrawable(R.drawable.forme_white_radius_10dp));
                       mEditTextEmail.setBackground(getResources().getDrawable(R.drawable.forme_white_radius_10dp));
                       mEditTextPassword.setBackground(getResources().getDrawable(R.drawable.forme_white_radius_100dp_border_rouge));
                       mEditTextConfirmPassword.setBackground(getResources().getDrawable(R.drawable.forme_white_radius_100dp_border_rouge));
                       break;
               }
           }
       });

       /* En cliquant sur le TextView ce connecter */
       mTextViewLogin.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               Intent login = new Intent(RegisterActivity.this, LoginActivity.class);
               startActivity(login);
           }
       });
    }

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
            //Toast.makeText(NotificationService.this, response, Toast.LENGTH_SHORT).show();
            if(jsonData != null)
            {
                if(jsonData.equals("matriculeEx")) {
                    mTextViewError.setText("Ce compte existe");
                    mEditTextIdNumber.setBackground(getResources().getDrawable(R.drawable.forme_white_radius_100dp_border_rouge));
                    mEditTextEmail.setBackground(getResources().getDrawable(R.drawable.forme_white_radius_10dp));
                    mEditTextPassword.setBackground(getResources().getDrawable(R.drawable.forme_white_radius_10dp));
                    mEditTextConfirmPassword.setBackground(getResources().getDrawable(R.drawable.forme_white_radius_10dp));
                    mProgressBarCirculaire.setVisibility(View.INVISIBLE);
                    mButtonConnection.setText("Connexion");
                }
                else
                {
                    if(jsonData.equals("emailEx")) {
                        mTextViewError.setText("L'adresse email existe déjà");
                        mEditTextIdNumber.setBackground(getResources().getDrawable(R.drawable.forme_white_radius_10dp));
                        mEditTextEmail.setBackground(getResources().getDrawable(R.drawable.forme_white_radius_100dp_border_rouge));
                        mEditTextPassword.setBackground(getResources().getDrawable(R.drawable.forme_white_radius_10dp));
                        mEditTextConfirmPassword.setBackground(getResources().getDrawable(R.drawable.forme_white_radius_10dp));
                        mProgressBarCirculaire.setVisibility(View.INVISIBLE);
                        mButtonConnection.setText("Connexion");
                    }
                    else
                    {
                        if(jsonData.equals("matriculeIn"))
                        {
                            mTextViewError.setText("Matricule introuvable");
                            mEditTextIdNumber.setBackground(getResources().getDrawable(R.drawable.forme_white_radius_100dp_border_rouge));
                            mEditTextEmail.setBackground(getResources().getDrawable(R.drawable.forme_white_radius_10dp));
                            mEditTextPassword.setBackground(getResources().getDrawable(R.drawable.forme_white_radius_10dp));
                            mEditTextConfirmPassword.setBackground(getResources().getDrawable(R.drawable.forme_white_radius_10dp));
                            mProgressBarCirculaire.setVisibility(View.INVISIBLE);
                            mButtonConnection.setText("Connexion");
                        }
                        else
                        {
                            if(jsonData.equals("update"))
                            {
                                Update();
                                mProgressBarCirculaire.setVisibility(View.INVISIBLE);
                                mButtonConnection.setText("Connexion");
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
                mTextViewError.setText("Aucune conexion");
                mProgressBarCirculaire.setVisibility(View.INVISIBLE);
                mButtonConnection.setText("Connexion");
            }

        }
    }
    private void Update(){
        UpdateDialog updateDialog = new UpdateDialog(this);
        updateDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        updateDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        updateDialog.build();
    }
    private EditText mEditTextIdNumber;
    private EditText mEditTextPassword;
    private EditText mEditTextConfirmPassword;
    private Button mButtonConnection;
    private TextView mTextViewLogin;
    private TextView mTextViewError;
    private EditText mEditTextEmail;
    private SQLiteDatabase mDataBase;
    private UserTable mUserTable;
    private Session mSession;
    private String mJeton;
    private ProgressBar mProgressBarCirculaire;
}