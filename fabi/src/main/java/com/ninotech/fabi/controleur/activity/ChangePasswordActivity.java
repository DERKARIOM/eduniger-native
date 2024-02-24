package com.ninotech.fabi.controleur.activity;

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

public class ChangePasswordActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);
        Objects.requireNonNull(getSupportActionBar()).hide();
        mIdNumberEditText = findViewById(R.id.edit_text_activity_change_password_id_number);
        mMailEditText = findViewById(R.id.edit_text_activity_change_password_mail);
        mPasswordEditText = findViewById(R.id.edit_text_activity_change_password_password);
        mConfirmPassword = findViewById(R.id.edit_text_activity_change_password_confirm_password);
        mConnectionButton = findViewById(R.id.button_activity_change_password_connection);
        mChangeMailTextView = findViewById(R.id.text_view_activity_change_password_change_mail);
        mErrorTextView = findViewById(R.id.text_view_activity_change_password_error);
        mConnectionProgressBar = findViewById(R.id.progress_bar_change_password_connection);
        mSession = new Session(this);
        mJeton = "null";

        /* Generation de jeton FireBase */
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            Log.e("ErrChangePasswordJeton", "Erreur de generation du jeton", task.getException());
                            return;
                        }
                        // Generation du nouveau jeton
                        mJeton = task.getResult();
                    }
                });

        /* En cliquant sur le boutton de connection */
        mConnectionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAccount = new Account(mIdNumberEditText.getText().toString(),mMailEditText.getText().toString(),mPasswordEditText.getText().toString(),null);
                switch (mAccount.inputControl(mConfirmPassword.getText().toString()))
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
                        mConnectionProgressBar.setVisibility(View.VISIBLE);
                        mConnectionButton.setText(R.string.register_succes_1111);
                        ChangePassword changePassword = new ChangePassword();
                        changePassword.execute(getResources().getString(R.string.ip_server_android) + "ChangePassword.php",
                                mAccount.getIdNumber(),
                                mAccount.getEmail(),
                                mAccount.getPassword()
                        );
                        break;
                }
            }
        });

        /* En cliquant sur le TextView ce connecter */
        mChangeMailTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent email = new Intent(ChangePasswordActivity.this, EmailChangeActivity.class);
                startActivity(email);
            }
        });
    }

    public void inputControl(int idNumberForm , int emailForm , int passwordForm , int passwordConfirmForm , int message)
    {
        mIdNumberEditText.setBackground(getResources().getDrawable(idNumberForm));
        mMailEditText.setBackground(getResources().getDrawable(emailForm));
        mPasswordEditText.setBackground(getResources().getDrawable(passwordForm));
        mConfirmPassword.setBackground(getResources().getDrawable(passwordConfirmForm));
        mErrorTextView.setText(message);
    }
    public void inputClean()
    {
        mIdNumberEditText.setBackground(getResources().getDrawable(R.drawable.forme_white_radius_10dp));
        mMailEditText.setBackground(getResources().getDrawable(R.drawable.forme_white_radius_10dp));
        mPasswordEditText.setBackground(getResources().getDrawable(R.drawable.forme_white_radius_10dp));
        mConfirmPassword.setBackground(getResources().getDrawable(R.drawable.forme_white_radius_10dp));
        mErrorTextView.setText("");
        mIdNumberEditText.setText("");
        mMailEditText.setText("");
        mPasswordEditText.setText("");
        mConfirmPassword.setText("");
    }
    private class ChangePassword extends AsyncTask<String,Void,String> {
        @Override
        protected String doInBackground(String... params) {

            try {
                OkHttpClient client = new OkHttpClient();
                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("idNumber",params[1])
                        .addFormDataPart("email",params[2])
                        .addFormDataPart("passwordNew",params[3])
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
                case "0011":
                    dataControl(
                            R.drawable.forme_white_radius_100dp_border_rouge,
                            R.drawable.forme_white_radius_100dp_border_rouge,
                            R.drawable.forme_white_radius_10dp,
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
    public void dataControl(int idNumberIco , int emailIco , int passwordIco , int passwordConfirmIco , int message)
    {
        inputControl(idNumberIco,emailIco,passwordIco,passwordConfirmIco,message);
        mConnectionProgressBar.setVisibility(View.INVISIBLE);
        mConnectionButton.setText(R.string.button_text_connection);
    }
    private void successChangePasswordDialog(){
        SimpleOkDialog simpleOkDialog = new SimpleOkDialog(this);
        simpleOkDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        simpleOkDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        TextView messageTextView = simpleOkDialog.findViewById(R.id.text_view_dialog_simple_ok_message);
        TextView okTextView = simpleOkDialog.findViewById(R.id.text_view_dialog_simple_ok);
        messageTextView.setText(R.string.ms_change_password);
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
    private EditText mConfirmPassword;
    private Button mConnectionButton;
    private TextView mChangeMailTextView;
    private TextView mErrorTextView;
    private EditText mMailEditText;
    private SQLiteDatabase data;
    private Session mSession;
    private String mJeton;
    private Account mAccount;
    private ProgressBar mConnectionProgressBar;
}