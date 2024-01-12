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

public class RegesterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_regester);
        getSupportActionBar().hide();


        /* Initialisation des attributs menbre */
       mEditMatricule2 = findViewById(R.id.EditMatricule2);
       mEditMail = findViewById(R.id.EditMail);
       mEditPasse2 = findViewById(R.id.EditPasse2);
       mEditConf = findViewById(R.id.EditConf);
       mButtonConnect2 = findViewById(R.id.ButtonConnect2);
       mTextLogin = findViewById(R.id.TextLogin);
       mTextErr2 = findViewById(R.id.TextErr2);
       mCirculaire = findViewById(R.id.progress_circularRegester);
       mSession = new Session(this);
       mUtilisateur = new UtilisateurTable(this);
       mJeton = "null";
       data = openOrCreateDatabase("data.db",MODE_PRIVATE,null);

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
       mButtonConnect2.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               if(mEditMatricule2.getText().toString().equals("") && mEditMail.getText().toString().equals("") && mEditPasse2.getText().toString().equals("") && mEditConf.getText().toString().equals(""))
               {
                   mTextErr2.setText("Veuillez remplir ces champs svp");
                   mEditMatricule2.setBackground(getResources().getDrawable(R.drawable.input_err));
                   mEditMail.setBackground(getResources().getDrawable(R.drawable.input_err));
                   mEditPasse2.setBackground(getResources().getDrawable(R.drawable.input_err));
                   mEditConf.setBackground(getResources().getDrawable(R.drawable.input_err));
               }
               else
               {
                   if(mEditMatricule2.getText().toString().equals(""))
                   {
                       mTextErr2.setText("Votre matricule svp");
                       mEditMatricule2.setBackground(getResources().getDrawable(R.drawable.input_err));
                       mEditMail.setBackground(getResources().getDrawable(R.drawable.arondie));
                       mEditPasse2.setBackground(getResources().getDrawable(R.drawable.arondie));
                       mEditConf.setBackground(getResources().getDrawable(R.drawable.arondie));
                   }
                   if(mEditPasse2.getText().toString().equals(mEditConf.getText().toString()))
                   {
                       mCirculaire.setVisibility(View.VISIBLE);
                       mButtonConnect2.setText("Connexion...");
                       Http http = new Http();
                       http.execute("http://192.168.43.1:2222/fabi/android/register.php");
                   }else
                   {
                       mTextErr2.setText("Erreur de confirmation");
                       mEditMatricule2.setBackground(getResources().getDrawable(R.drawable.arondie));
                       mEditMail.setBackground(getResources().getDrawable(R.drawable.arondie));
                       mEditPasse2.setBackground(getResources().getDrawable(R.drawable.input_err));
                       mEditConf.setBackground(getResources().getDrawable(R.drawable.input_err));
                   }
               }
           }
       });

       /* En cliquant sur le TextView ce connecter */
       mTextLogin.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               Intent login = new Intent(RegesterActivity.this, LoginActivity.class);
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
                        .addFormDataPart("matricule",mEditMatricule2.getText().toString())
                        .addFormDataPart("email",mEditMail.getText().toString())
                        .addFormDataPart("motdepasse",mEditPasse2.getText().toString())
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
                    Toast.makeText(RegesterActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
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
                    mTextErr2.setText("Ce compte existe");
                    mEditMatricule2.setBackground(getResources().getDrawable(R.drawable.input_err));
                    mEditMail.setBackground(getResources().getDrawable(R.drawable.arondie));
                    mEditPasse2.setBackground(getResources().getDrawable(R.drawable.arondie));
                    mEditConf.setBackground(getResources().getDrawable(R.drawable.arondie));
                    mCirculaire.setVisibility(View.INVISIBLE);
                    mButtonConnect2.setText("Connexion");
                }
                else
                {
                    if(jsonData.equals("emailEx")) {
                        mTextErr2.setText("L'adresse email existe déjà");
                        mEditMatricule2.setBackground(getResources().getDrawable(R.drawable.arondie));
                        mEditMail.setBackground(getResources().getDrawable(R.drawable.input_err));
                        mEditPasse2.setBackground(getResources().getDrawable(R.drawable.arondie));
                        mEditConf.setBackground(getResources().getDrawable(R.drawable.arondie));
                        mCirculaire.setVisibility(View.INVISIBLE);
                        mButtonConnect2.setText("Connexion");
                    }
                    else
                    {
                        if(jsonData.equals("matriculeIn"))
                        {
                            mTextErr2.setText("Matricule introuvable");
                            mEditMatricule2.setBackground(getResources().getDrawable(R.drawable.input_err));
                            mEditMail.setBackground(getResources().getDrawable(R.drawable.arondie));
                            mEditPasse2.setBackground(getResources().getDrawable(R.drawable.arondie));
                            mEditConf.setBackground(getResources().getDrawable(R.drawable.arondie));
                            mCirculaire.setVisibility(View.INVISIBLE);
                            mButtonConnect2.setText("Connexion");
                        }
                        else
                        {
                            if(jsonData.equals("update"))
                            {
                                Update();
                                mCirculaire.setVisibility(View.INVISIBLE);
                                mButtonConnect2.setText("Connexion");
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
                                    mUtilisateur.insert(jsonObject.getString("matriculeUt"),
                                            jsonObject.getString("nomUt"),
                                            jsonObject.getString("prenomUt"),
                                            jsonObject.getString("statusUt"));
                                } catch (JSONException e) {
                                    throw new RuntimeException(e);
                                }
                                mSession.onCreate(data);
                                try {
                                    mSession.insert(jsonObject.getString("matriculeUt"));
                                } catch (JSONException e) {
                                    throw new RuntimeException(e);
                                }
                                Intent  home= new Intent(RegesterActivity.this , MainActivity.class);
                                startActivity(home);
                                finish();
                            }
                        }
                    }
                }
            }
            else
            {
                mTextErr2.setText("Aucune conexion");
                mCirculaire.setVisibility(View.INVISIBLE);
                mButtonConnect2.setText("Connexion");
            }

        }
    }
    private void Update(){
        UpdateDialog updateDialog = new UpdateDialog(this);
        updateDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        updateDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        updateDialog.build();
    }
    private EditText mEditMatricule2;
    private EditText mEditPasse2;
    private EditText mEditConf;
    private Button mButtonConnect2;
    private TextView mTextLogin;
    private TextView mTextErr2;
    private EditText mEditMail;
    private SQLiteDatabase data;
    private UtilisateurTable mUtilisateur;
    private Session mSession;
    private String mJeton;
    private ProgressBar mCirculaire;
}