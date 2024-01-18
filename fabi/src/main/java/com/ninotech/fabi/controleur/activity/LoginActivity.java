package com.ninotech.fabi.controleur.activity;

import android.content.Intent;
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
        mIdNumberEditText = findViewById(R.id.edit_text_login_id_number);
        mPassewordEditText = findViewById(R.id.edit_text_login_password);
        mRegisterTextView = findViewById(R.id.text_view_login_pass_register);
        mConnectionButtom = findViewById(R.id.button_login_connection);
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
        mConnectionButtom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mIdNumberEditText.getText().toString().equals("") && mPassewordEditText.getText().toString().equals(""))
                {
                    mErrorTextView.setText("Votre matricule et mot de passe svp");
                    mIdNumberEditText.setBackground(getResources().getDrawable(R.drawable.forme_white_radius_100dp_border_rouge));
                    mPassewordEditText.setBackground(getResources().getDrawable(R.drawable.forme_white_radius_100dp_border_rouge));
                }
                if(mIdNumberEditText.getText().toString().equals("") && !mPassewordEditText.getText().toString().equals("")){
                    mErrorTextView.setText("Votre matricule svp");
                    mIdNumberEditText.setBackground(getResources().getDrawable(R.drawable.forme_white_radius_100dp_border_rouge));
                    mPassewordEditText.setBackground(getResources().getDrawable(R.drawable.forme_white_radius_10dp));
                }

                if(!mIdNumberEditText.getText().toString().equals("") && mPassewordEditText.getText().toString().equals(""))
                {
                    mErrorTextView.setText("Votre mot de passe svp");
                    mIdNumberEditText.setBackground(getResources().getDrawable(R.drawable.forme_white_radius_10dp));
                    mPassewordEditText.setBackground(getResources().getDrawable(R.drawable.forme_white_radius_100dp_border_rouge));
                }
                if(!mIdNumberEditText.getText().toString().equals("") && !mPassewordEditText.getText().toString().equals(""))
                {
                    mConnectionProgressBar.setVisibility(View.VISIBLE);
                    mConnectionButtom.setText("Connexion...");
                    Http http = new Http();
                    http.execute(getResources().getString(R.string.ip_server) + "Login.php");
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
                    if(mHelperTextView.getText().equals("j'ai oublié mon mot de passe"))
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

    /* Les methode de la Classe LoginActivity */
    private class Http extends AsyncTask<String,Void,String> {
        @Override
        protected String doInBackground(String... params) {

            try {
                OkHttpClient client = new OkHttpClient();
                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("matricule", mIdNumberEditText.getText().toString())
                        .addFormDataPart("motdepasse", mPassewordEditText.getText().toString())
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
            //Toast.makeText(NotificationService.this, response, Toast.LENGTH_SHORT).show();
            if(jsonData != null)
            {
                if(jsonData.equals("false"))
                {
                    mErrorTextView.setText("Ce compte n' existe pas");
                    mIdNumberEditText.setBackground(getResources().getDrawable(R.drawable.forme_white_radius_100dp_border_rouge));
                    mPassewordEditText.setBackground(getResources().getDrawable(R.drawable.forme_white_radius_100dp_border_rouge));
                    mConnectionProgressBar.setVisibility(View.INVISIBLE);
                    mConnectionButtom.setText("Connexion");

                }
                else {
                    if (jsonData.equals("true")) {
                        mIdNumberEditText.setBackground(getResources().getDrawable(R.drawable.forme_white_radius_10dp));
                        mErrorTextView.setText("Mot de passe incorrect");
                        mHelperTextView.setText("j'ai oublié mon mot de passe");
                        mHelperTextView.setTextColor(Color.parseColor("#E6FD1010"));
                        mPassewordEditText.setBackground(getResources().getDrawable(R.drawable.forme_white_radius_100dp_border_rouge));
                        mHelperTextView.setTextSize(15);
                        mConnectionProgressBar.setVisibility(View.INVISIBLE);
                        mConnectionButtom.setText("Connexion");
                    } else {
                        if (jsonData.equals("update")) {
                            Update();
                            mConnectionProgressBar.setVisibility(View.INVISIBLE);
                            mConnectionButtom.setText("Connexion");
                        } else {
//                            mSession.insert(mEditMatricule.getText().toString());
                            JSONObject jsonObject = null;
                            try {
                                jsonObject = new JSONObject(jsonData);
                            } catch (JSONException e) {
                                throw new RuntimeException(e);
                            }
                            try {
                                mUtilisateur.insert(jsonObject.getString("matriculeUt"),
                                        jsonObject.getString("nomUt"),
                                        jsonObject.getString("prenomUt"),
                                        jsonObject.getString("statusUt"),jsonObject.getString("email"));
                            } catch (JSONException e) {
                                throw new RuntimeException(e);
                            }
                            try {
                                mSession.insert(jsonObject.getString("matriculeUt"));
                            } catch (JSONException e) {
                                throw new RuntimeException(e);
                            }
                            Intent home = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(home);
                            finish();
                        }
                    }
                }
            }
            else
            {
                mErrorTextView.setText("Aucune conexion");
                mConnectionProgressBar.setVisibility(View.INVISIBLE);
                mConnectionButtom.setText("Connexion");
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
    private Button mConnectionButtom;
    private TextView mRegisterTextView;
    private TextView mErrorTextView;
    private TextView mHelperTextView;
    private Session mSession;
    private UserTable mUtilisateur;
    private String mJeton;
    private ProgressBar mConnectionProgressBar;
}