package com.ninotech.fabi.controleur.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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

import com.ninotech.fabi.controleur.dialog.SimpleOkDialog;
import com.ninotech.fabi.model.data.Account;
import com.ninotech.fabi.model.table.Session;
import com.ninotech.fabi.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Objects;

import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ChangeEmailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_email);
        Objects.requireNonNull(getSupportActionBar()).hide();
        mIdNumberEditText = findViewById(R.id.edit_text_activity_change_email_id_number);
        mPasswordEditText = findViewById(R.id.edit_text_activity_change_email_password);
        mEmailEditText = findViewById(R.id.EditMail3);
        mConnectionButton = findViewById(R.id.button_activity_change_email_connection);
        mChangePasswordTextView = findViewById(R.id.text_view_activity_change_email_change_password);
        mErrorTextView = findViewById(R.id.text_view_activity_change_email_error);
        mConnectionProgressBar = findViewById(R.id.progress_bar_change_email_connection);
        mSession = new Session(this);
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
        mConnectionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAccount = new Account(mIdNumberEditText.getText().toString(),mEmailEditText.getText().toString(),mPasswordEditText.getText().toString(),null);
                switch (mAccount.inputControl())
                {
                    case "00":
                        inputControl(
                                R.drawable.forme_white_radius_100dp_border_rouge,
                                R.drawable.forme_white_radius_100dp_border_rouge,
                                R.drawable.forme_white_radius_10dp,
                                R.string.grab_id_number_and_password
                        );
                    case "11":
                        mConnectionProgressBar.setVisibility(View.VISIBLE);
                        mConnectionButton.setText(R.string.register_succes_1111);
                        ChangeEmailSyn changeEmailSyn = new ChangeEmailSyn();
                        changeEmailSyn.execute(getString(R.string.ip_server_android) + "ChangeEmail.php",mAccount.getIdNumber(),mAccount.getPassword(),mAccount.getEmail());
                        break;
                }

            }
        });

        /* En cliquant sur le TextView ce connecter */
        mChangePasswordTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent email = new Intent(ChangeEmailActivity.this, ChangePasswordActivity.class);
                startActivity(email);
            }
        });
    }
    public void inputControl(int idNumberForm , int passwordForm ,  int emailForm , int message)
    {
        mIdNumberEditText.setBackground(getResources().getDrawable(idNumberForm));
        mEmailEditText.setBackground(getResources().getDrawable(emailForm));
        mPasswordEditText.setBackground(getResources().getDrawable(passwordForm));
        mErrorTextView.setText(message);
    }
    public void inputClean()
    {
        mIdNumberEditText.setBackground(getResources().getDrawable(R.drawable.forme_white_radius_10dp));
        mEmailEditText.setBackground(getResources().getDrawable(R.drawable.forme_white_radius_10dp));
        mPasswordEditText.setBackground(getResources().getDrawable(R.drawable.forme_white_radius_10dp));
        mErrorTextView.setText("");
        mIdNumberEditText.setText("");
        mEmailEditText.setText("");
        mPasswordEditText.setText("");
    }
    private class ChangeEmailSyn extends AsyncTask<String,Void,String> {
        @Override
        protected String doInBackground(String... params) {
            try {
                OkHttpClient client = new OkHttpClient();
                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("idNumber",params[1])
                        .addFormDataPart("password",params[2])
                        .addFormDataPart("emailNew",params[3])
                        .build();
                Request request = new Request.Builder()
                        .url(params[0])
                        .post(requestBody)
                        .build();
                try {
                    Response response = client.newCall(request).execute();
                    assert response.body() != null;
                    return response.body().string();
                }catch (IOException e)
                {
                    Log.e("errorChangePassword",e.getMessage());
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
                case "00":
                    dataControl(
                            R.drawable.forme_white_radius_100dp_border_rouge,
                            R.drawable.forme_white_radius_100dp_border_rouge,
                            R.drawable.forme_white_radius_10dp,
                            R.string.no_found_id_number_or_email
                    );
                    break;
                case "1111":
                    inputClean();
                    mSession.setPassword(mAccount.getPassword());
                    successChangePasswordDialog();
                    break;
                default:
                    mErrorTextView.setText(R.string.no_connection);
                    mConnectionProgressBar.setVisibility(View.INVISIBLE);
                    mConnectionButton.setText(R.string.button_text_connection);
                    break;
            }
        }
    }
    public void dataControl(int idNumberIco , int passwordIco , int emailIco , int message)
    {
        inputControl(idNumberIco,passwordIco,emailIco,message);
        mConnectionProgressBar.setVisibility(View.GONE);
        mConnectionButton.setText(R.string.button_text_connection);
    }
    private void successChangePasswordDialog(){
        SimpleOkDialog simpleOkDialog = new SimpleOkDialog(this);
        simpleOkDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        simpleOkDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        TextView messageTextView = simpleOkDialog.findViewById(R.id.text_view_dialog_simple_ok_message);
        TextView okTextView = simpleOkDialog.findViewById(R.id.text_view_dialog_simple_ok);
        messageTextView.setText(R.string.ms_change_email);
        okTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mConnectionProgressBar.setVisibility(View.GONE);
                mConnectionButton.setText(R.string.button_text_connection);
                simpleOkDialog.cancel();
            }
        });
        simpleOkDialog.build();
    }
    private EditText mIdNumberEditText;
    private EditText mPasswordEditText;
    private Button mConnectionButton;
    private TextView mChangePasswordTextView;
    private TextView mErrorTextView;
    private EditText mEmailEditText;
    private SQLiteDatabase data;
    private Session mSession;
    private String mJeton;
    private Account mAccount;
    private ProgressBar mConnectionProgressBar;
}