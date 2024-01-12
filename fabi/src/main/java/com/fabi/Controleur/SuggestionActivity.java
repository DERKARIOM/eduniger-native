package com.fabi.Controleur;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.fabi.Model.Session;
import com.fabi.Model.SucceSuggesionDialog;
import com.example.fabi.R;

import java.io.IOException;

import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SuggestionActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggestion);
        getSupportActionBar().hide();
        // StatusBarCusto statusBarCusto = new StatusBarCusto(this,getWindow());
        // Activer le bouton de retour de l'action barre
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mSession = new Session(this);
        mEnvoyerSuggestion = findViewById(R.id.EnvoyerJuggestion);
        mObjetJuggestion = findViewById(R.id.ObjetsJuggestion);
        mSuggestion = findViewById(R.id.Suggestion);
        mErr = findViewById(R.id.TextErrSugg);
        mPhone = findViewById(R.id.infoPhone);
        mCirculaire = findViewById(R.id.progress_circularSuggestion);
        mModel = Build.MODEL;
        mVersion = Build.VERSION.RELEASE;
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.options_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mObjetJuggestion.setAdapter(adapter);
        mEnvoyerSuggestion.setOnClickListener(new View.OnClickListener()  {
            @Override
            public void onClick(View view) {
                if(mSuggestion.getText().toString().equals(""))
                    mErr.setText("Veuillez remplir ces champs svp");
                else
                {
                    mCirculaire.setVisibility(View.VISIBLE);
                    mEnvoyerSuggestion.setText("");
                    Http http = new Http();
                    http.execute("http://192.168.43.1:2222/android/suggestion.php");
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Gérer les clics sur les éléments de l'action barre
        switch (item.getItemId()) {
            case android.R.id.home:
                // Appeler onBackPressed() lorsque le bouton de retour de l'action barre est pressé
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        // Revenir en arrière tout simplement
        super.onBackPressed();
    }

    private class Http extends AsyncTask<String,Void,String> {
        @Override
        protected String doInBackground(String... params) {
            try {
                OkHttpClient client = new OkHttpClient();
                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("matricule",mSession.getMatricule())
                        .addFormDataPart("objet", mObjetJuggestion.getSelectedItem().toString())
                        .addFormDataPart("suggestion",mSuggestion.getText().toString())
                        .addFormDataPart("model",mModel)
                        .addFormDataPart("version",mVersion)
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
                   // Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                }

            }catch (Exception e)
            {
                //Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
            return null;
        }
        @Override
        protected void onPostExecute(String response){
            //Toast.makeText(NotificationService.this, response, Toast.LENGTH_SHORT).show();
            if(response != null)
            {
                if(response.equals("true"))
                {
                    SucceSuggestionDialog();
                    mCirculaire.setVisibility(View.INVISIBLE);
                    mEnvoyerSuggestion.setText("Envoyer");
                }
            }
            else
            {
                mErr.setText("Aucune conexion");
                mCirculaire.setVisibility(View.INVISIBLE);
                mEnvoyerSuggestion.setText("Envoyer");
            }

        }
    }
    private void SucceSuggestionDialog(){
        SucceSuggesionDialog succeSuggesionDialog = new SucceSuggesionDialog(this);
        succeSuggesionDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        succeSuggesionDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        TextView ok = succeSuggesionDialog.findViewById(R.id.ok);
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSuggestion.setText("");
                succeSuggesionDialog.cancel();
            }
        });
        succeSuggesionDialog.build();
    }
    private Button mEnvoyerSuggestion;
    private EditText mSuggestion;
    private Session mSession;
    private Spinner mObjetJuggestion;
    private TextView mErr;
    private String mModel;
    private String mVersion;
    private CheckBox mPhone;
    private ProgressBar mCirculaire;
}