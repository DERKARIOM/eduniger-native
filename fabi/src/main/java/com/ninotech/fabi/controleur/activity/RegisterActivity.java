package com.ninotech.fabi.controleur.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
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
import com.ninotech.fabi.model.data.PasswordUtil;
import com.ninotech.fabi.model.data.Server;

import java.io.IOException;
import java.util.Objects;

import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RegisterActivity extends AppCompatActivity {

    @SuppressLint({"MissingInflatedId", "CutPasteId"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        Objects.requireNonNull(getSupportActionBar()).hide();


        /* Initialisation des attributs membre(propiete) */
        mNameEditText = findViewById(R.id.edit_text_activity_register_name);
        mFirstNameEditText = findViewById(R.id.edit_text_activity_register_first_name);
        mProfessionSpinner = findViewById(R.id.spinner_activity_register_profession);
       mIdNumberEditText = findViewById(R.id.edit_text_activity_register_id_number);
       mEmailEditText = findViewById(R.id.edit_text_activity_register_email);
       mPasswordEditText = findViewById(R.id.edit_text_activity_register_password);
       mPasswordConfirmEditText = findViewById(R.id.edit_text_activity_register_password);
       mConnectionButton = findViewById(R.id.button_activity_register_connection);
       mLoginTextView = findViewById(R.id.text_view_activity_register_login);
       mErrorTextView = findViewById(R.id.text_view_activity_register_error);
       mConnectionProgressBar = findViewById(R.id.progress_bar_activity_register_connection);
       mJeton = "null";
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.profesion_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mProfessionSpinner.setAdapter(adapter);
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
                       mNameEditText.getText().toString(),
                       mFirstNameEditText.getText().toString(),
                       mEmailEditText.getText().toString(),
                       PasswordUtil.hashPassword(mPasswordEditText.getText().toString()), null,
                       mProfessionSpinner.getSelectedItemId());
               switch (mAccount.inputControl(PasswordUtil.hashPassword(mPasswordConfirmEditText.getText().toString())))
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
                       if(mProfessionSpinner.getSelectedItemPosition() != 0)
                       {
                           mConnectionProgressBar.setVisibility(View.VISIBLE);
                           mConnectionButton.setText(R.string.register_succes_1111);
                           RegisterSyn registerSyn = new RegisterSyn();
                           registerSyn.execute(
                                   Server.getIpServerAndroid(getApplicationContext()) + "Register.php",
                                   mAccount.getIdNumber(),
                                   mAccount.getName(),
                                   mAccount.getFirstName(),
                                   mAccount.getEmail(),
                                   mAccount.getPassword(),
                                   String.valueOf(mAccount.getProfession())
                           );
                       }
                       else
                           mErrorTextView.setText("Votre profession svp ?");
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
                        .addFormDataPart("idUser",params[1])
                        .addFormDataPart("name",params[2])
                        .addFormDataPart("firstName",params[3])
                        .addFormDataPart("email",params[4])
                        .addFormDataPart("password",params[5])
                        .addFormDataPart("profession",params[6])
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
                case "update":
                    Update();
                    mConnectionProgressBar.setVisibility(View.INVISIBLE);
                    mConnectionButton.setText(R.string.button_text_connection);
                    break;
                case "1111":
                    if(mAccount.register(getApplicationContext(),"no"))
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
                String url = "http://eduniger.com"; // Remplacez ceci par l'URL que vous souhaitez ouvrir
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intent);
            }
        });

        updateDialog.build();
    }
    private EditText mNameEditText;
    private EditText mFirstNameEditText;
    private Spinner mProfessionSpinner;
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