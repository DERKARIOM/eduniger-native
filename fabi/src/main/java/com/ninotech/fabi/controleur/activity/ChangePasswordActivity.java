package com.ninotech.fabi.controleur.activity;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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

public class ChangePasswordActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);
        //StatusBarCusto statusBarCusto = new StatusBarCusto(this,getWindow());
        getSupportActionBar().hide();
        mEditMatricule3 = findViewById(R.id.EditMatricule3);
        mEditMail2 = findViewById(R.id.EditMail2);
        mEditPasse3 = findViewById(R.id.EditPasse3);
        mEditConf2 = findViewById(R.id.EditConf2);
        mButtonConnect3 = findViewById(R.id.ButtonConnect3);
        mTextLogin2 = findViewById(R.id.TextAide2);
        mTextErr3 = findViewById(R.id.TextErr3);
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
        mButtonConnect3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mEditMatricule3.getText().toString().equals("") && mEditMail2.getText().toString().equals("") && mEditPasse3.getText().toString().equals("") && mEditConf2.getText().toString().equals(""))
                {
                    mTextErr3.setText("Veuillez remplir ces champs svp");
                    mEditMatricule3.setBackground(getResources().getDrawable(R.drawable.forme_white_radius_100dp_border_rouge));
                    mEditMail2.setBackground(getResources().getDrawable(R.drawable.forme_white_radius_100dp_border_rouge));
                    mEditPasse3.setBackground(getResources().getDrawable(R.drawable.forme_white_radius_100dp_border_rouge));
                    mEditConf2.setBackground(getResources().getDrawable(R.drawable.forme_white_radius_100dp_border_rouge));
                }
                else
                {
                    if(mEditMatricule3.getText().toString().equals(""))
                    {
                        mTextErr3.setText("Votre matricule svp");
                        mEditMatricule3.setBackground(getResources().getDrawable(R.drawable.forme_white_radius_100dp_border_rouge));
                        mEditMail2.setBackground(getResources().getDrawable(R.drawable.forme_white_radius_10dp));
                        mEditPasse3.setBackground(getResources().getDrawable(R.drawable.forme_white_radius_10dp));
                        mEditConf2.setBackground(getResources().getDrawable(R.drawable.forme_white_radius_10dp));
                    }
                    if(mEditPasse3.getText().toString().equals(mEditConf2.getText().toString()))
                    {
                       Http http = new Http();
                       http.execute("http://192.168.43.1:2222/android/changePassWord.php");
                    }else
                    {
                        mTextErr3.setText("Erreur de confirmation");
                        mEditMatricule3.setBackground(getResources().getDrawable(R.drawable.forme_white_radius_10dp));
                        mEditMail2.setBackground(getResources().getDrawable(R.drawable.forme_white_radius_10dp));
                        mEditPasse3.setBackground(getResources().getDrawable(R.drawable.forme_white_radius_100dp_border_rouge));
                        mEditConf2.setBackground(getResources().getDrawable(R.drawable.forme_white_radius_100dp_border_rouge));
                    }
                }
            }
        });

        /* En cliquant sur le TextView ce connecter */
        mTextLogin2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent email = new Intent(ChangePasswordActivity.this, EmailChangeActivity.class);
                startActivity(email);
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
                        .addFormDataPart("matricule", mEditMatricule3.getText().toString())
                        .addFormDataPart("email", mEditMail2.getText().toString())
                        .addFormDataPart("motdepasse", mEditPasse3.getText().toString())
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
                    mTextErr3.setText("Matricule ou Email incorrect");
                    mEditMatricule3.setBackground(getResources().getDrawable(R.drawable.forme_white_radius_100dp_border_rouge));
                    mEditMail2.setBackground(getResources().getDrawable(R.drawable.forme_white_radius_100dp_border_rouge));
                    mEditPasse3.setBackground(getResources().getDrawable(R.drawable.forme_white_radius_10dp));
                    mEditConf2.setBackground(getResources().getDrawable(R.drawable.forme_white_radius_10dp));
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
                        mSession.insert(jsonObject.getString("matricule"));
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                    Intent home = new Intent(ChangePasswordActivity.this, MainActivity.class);
                    startActivity(home);
                    finish();
                }
            }
            else
                mTextErr3.setText("Aucune connexion");
        }
    }
    private EditText mEditMatricule3;
    private EditText mEditPasse3;
    private EditText mEditConf2;
    private Button mButtonConnect3;
    private TextView mTextLogin2;
    private TextView mTextErr3;
    private EditText mEditMail2;
    private SQLiteDatabase data;
    private Session mSession;
    private String mJeton;
}