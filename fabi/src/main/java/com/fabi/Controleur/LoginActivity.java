package com.fabi.Controleur;

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

import com.fabi.Model.Session;
import com.fabi.Model.UpdateDialog;
import com.fabi.Model.UtilisateurTable;
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

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        /* Masquer le action bar */
        getSupportActionBar().hide();

        /* Initialisation des attributs menbre */
        mEditMatricule = findViewById(R.id.EditMatricule);
        mEditPasse = findViewById(R.id.EditPasse);
        mTextInscrire = findViewById(R.id.TextInscrire);
        mButtonConnect = findViewById(R.id.ButtonConnect);
        mTextAide = findViewById(R.id.TextAide);
        mTextErr = findViewById(R.id.TextErr);
        mCirculaire = findViewById(R.id.progress_circularLogin);
        mSession = new Session(this);
        mUtilisateur = new UtilisateurTable(this);
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
        mButtonConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mEditMatricule.getText().toString().equals("") && mEditPasse.getText().toString().equals(""))
                {
                    mTextErr.setText("Votre matricule et mot de passe svp");
                    mEditMatricule.setBackground(getResources().getDrawable(R.drawable.input_err));
                    mEditPasse.setBackground(getResources().getDrawable(R.drawable.input_err));
                }
                if(mEditMatricule.getText().toString().equals("") && !mEditPasse.getText().toString().equals("")){
                    mTextErr.setText("Votre matricule svp");
                    mEditMatricule.setBackground(getResources().getDrawable(R.drawable.input_err));
                    mEditPasse.setBackground(getResources().getDrawable(R.drawable.forme_white_radus_10dp));
                }

                if(!mEditMatricule.getText().toString().equals("") && mEditPasse.getText().toString().equals(""))
                {
                    mTextErr.setText("Votre mot de passe svp");
                    mEditMatricule.setBackground(getResources().getDrawable(R.drawable.forme_white_radus_10dp));
                    mEditPasse.setBackground(getResources().getDrawable(R.drawable.input_err));
                }
                if(!mEditMatricule.getText().toString().equals("") && !mEditPasse.getText().toString().equals(""))
                {
                    mCirculaire.setVisibility(View.VISIBLE);
                    mButtonConnect.setText("Connexion...");
                    Http http = new Http();
                    http.execute("http://192.168.43.1:2222/fabi/android/login.php");
                }
            }
        });

        /* En Cliquant sur le TextView d' inscription */
        mTextInscrire.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent inscription = new Intent(LoginActivity.this , RegesterActivity.class);
                startActivity(inscription);
            }
        });

        /* En Cliquant sur le TextView d' aide */
        mTextAide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    if(mTextAide.getText().equals("j'ai oublié mon mot de passe"))
                    {
                        Intent changePasseWord = new Intent(LoginActivity.this,ChangePasswordActivity.class);
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
                        .addFormDataPart("matricule",mEditMatricule.getText().toString())
                        .addFormDataPart("motdepasse",mEditPasse.getText().toString())
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
                    mTextErr.setText("Ce compte n' existe pas");
                    mEditMatricule.setBackground(getResources().getDrawable(R.drawable.input_err));
                    mEditPasse.setBackground(getResources().getDrawable(R.drawable.input_err));
                    mCirculaire.setVisibility(View.INVISIBLE);
                    mButtonConnect.setText("Connexion");

                }
                else {
                    if (jsonData.equals("true")) {
                        mEditMatricule.setBackground(getResources().getDrawable(R.drawable.forme_white_radus_10dp));
                        mTextErr.setText("Mot de passe incorrect");
                        mTextAide.setText("j'ai oublié mon mot de passe");
                        mTextAide.setTextColor(Color.parseColor("#E6FD1010"));
                        mEditPasse.setBackground(getResources().getDrawable(R.drawable.input_err));
                        mTextAide.setTextSize(15);
                        mCirculaire.setVisibility(View.INVISIBLE);
                        mButtonConnect.setText("Connexion");
                    } else {
                        if (jsonData.equals("update")) {
                            Update();
                            mCirculaire.setVisibility(View.INVISIBLE);
                            mButtonConnect.setText("Connexion");
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
                                        jsonObject.getString("statusUt"));
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
                mTextErr.setText("Aucune conexion");
                mCirculaire.setVisibility(View.INVISIBLE);
                mButtonConnect.setText("Connexion");
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
    private EditText mEditMatricule;
    private EditText mEditPasse;
    private Button mButtonConnect;
    private TextView mTextInscrire;
    private TextView mTextErr;
    private TextView mTextAide;
    private Session mSession;
    private UtilisateurTable mUtilisateur;
    private String mJeton;
    private ProgressBar mCirculaire;
}