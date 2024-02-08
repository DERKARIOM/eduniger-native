package com.ninotech.fabi.controleur.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.ninotech.fabi.model.table.Session;
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

public class EmailChangeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_change);
        //StatusBarCusto statusBarCusto = new StatusBarCusto(this,getWindow());
        getSupportActionBar().hide();
        mEditMatricule4 = findViewById(R.id.EditMatricule4);
        mEditPasse4 = findViewById(R.id.EditPasse4);
        mEditMail3 = findViewById(R.id.EditMail3);
        mButtonConnect4 = findViewById(R.id.ButtonConnect4);
        mChamgePasse = findViewById(R.id.changepasse);
        mTextErr4 = findViewById(R.id.TextErr4);
        mSession = new Session(this);
//        data = openOrCreateDatabase("data.db",MODE_PRIVATE,null);
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

        /* En cliquant sur le boutton de connection3 */
        mButtonConnect4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mEditMatricule4.getText().toString().equals("") && mEditMail3.getText().toString().equals("") && mEditPasse4.getText().toString().equals(""))
                {
                    mTextErr4.setText("Veuillez remplir ces champs svp");
                    mEditMatricule4.setBackground(getResources().getDrawable(R.drawable.forme_white_radius_100dp_border_rouge));
                    mEditMail3.setBackground(getResources().getDrawable(R.drawable.forme_white_radius_100dp_border_rouge));
                    mEditPasse4.setBackground(getResources().getDrawable(R.drawable.forme_white_radius_100dp_border_rouge));
                }
                else
                {
                    if(mEditMatricule4.getText().toString().equals(""))
                    {
                        mTextErr4.setText("Votre matricule svp");
                        mEditMatricule4.setBackground(getResources().getDrawable(R.drawable.forme_white_radius_100dp_border_rouge));
                        mEditMail3.setBackground(getResources().getDrawable(R.drawable.forme_white_radius_10dp));
                        mEditPasse4.setBackground(getResources().getDrawable(R.drawable.forme_white_radius_10dp));
                    }
                    try {
                        http();
                    }catch (Exception e)
                    {
                        mTextErr4.setText("Aucune connexion");
                    }
                }
            }
        });

        /* En cliquant sur le TextView ce connecter */
        mChamgePasse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent email = new Intent(EmailChangeActivity.this, ChangePasswordActivity.class);
                startActivity(email);
            }
        });
    }
    private void http() throws IOException, JSONException {
        OkHttpClient client = new OkHttpClient();
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("matricule", mEditMatricule4.getText().toString())
                .addFormDataPart("email", mEditMail3.getText().toString())
                .addFormDataPart("motdepasse", mEditPasse4.getText().toString())
                .addFormDataPart("jeton",mJeton)
                .build();
        Request request = new Request.Builder()
                .url("http://192.168.43.1:2222/android/ChangeEmail.php")
                .post(requestBody)
                .build();
        Response response = client.newCall(request).execute();
        if(response.isSuccessful())
        {
            String jsonData = response.body().string();
            if(jsonData.equals("false"))
            {
                mTextErr4.setText("Matricule ou mot de passe incorrect");
                mEditMatricule4.setBackground(getResources().getDrawable(R.drawable.forme_white_radius_100dp_border_rouge));
                mEditMail3.setBackground(getResources().getDrawable(R.drawable.forme_white_radius_10dp));
                mEditPasse4.setBackground(getResources().getDrawable(R.drawable.forme_white_radius_100dp_border_rouge));
            }
            else
            {
                Toast.makeText(this,"Email change avec succès ", Toast.LENGTH_SHORT).show();
                mSession.insert(mEditMatricule4.getText().toString(),"ras");
                JSONObject jsonObject = new JSONObject(jsonData);
                Intent home = new Intent(EmailChangeActivity.this, MainActivity.class);
                startActivity(home);
                finish();
            }
        }
        else
            mTextErr4.setText("Aucune connexion");
    }
    private EditText mEditMatricule4;
    private EditText mEditPasse4;
    private Button mButtonConnect4;
    private TextView mChamgePasse;
    private TextView mTextErr4;
    private EditText mEditMail3;
    private SQLiteDatabase data;
    private Session mSession;
    private String mJeton;
}