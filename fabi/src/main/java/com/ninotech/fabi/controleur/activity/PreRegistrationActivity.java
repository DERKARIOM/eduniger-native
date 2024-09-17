package com.ninotech.fabi.controleur.activity;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.ninotech.fabi.R;
import com.ninotech.fabi.model.table.Session;
import com.ninotech.fabi.model.table.StudentTable;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Objects;

import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class PreRegistrationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_pre_registration);
        Objects.requireNonNull(getSupportActionBar()).hide();
        mIdNumberEditText = findViewById(R.id.edit_text_activity_pre_register_id_number);
        mNameEditText = findViewById(R.id.edit_text_activity_pre_register_name);
        mFirstNameEditText = findViewById(R.id.edit_text_activity_pre_register_first_name);
        mSectionEditText = findViewById(R.id.edit_text_activity_pre_register_section);
        mDepartementEditText = findViewById(R.id.edit_text_activity_pre_register_departement);
        mRegisterButton = findViewById(R.id.button_activity_pre_register_connection);
        mSession = new Session(getApplicationContext());
        mStudentTable = new StudentTable(getApplicationContext());
        mSectionEditText.setText(mStudentTable.getSection(mSession.getIdNumber()));
        mDepartementEditText.setText(mStudentTable.getDepartement(mSession.getIdNumber()));
        mRegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PreRegisterSyn preRegisterSyn = new PreRegisterSyn();
                preRegisterSyn.execute(getString(R.string.ip_server_android) + "PreRegister.php",
                        mIdNumberEditText.getText().toString(),
                        mNameEditText.getText().toString(),
                        mFirstNameEditText.getText().toString(),
                        mSectionEditText.getText().toString(),
                        mDepartementEditText.getText().toString());
            }
        });
    }
    private class PreRegisterSyn extends AsyncTask<String,Void,String> {
        @Override
        protected String doInBackground(String... params) {

            try {
                OkHttpClient client = new OkHttpClient();
                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("idNumber",params[1])
                        .addFormDataPart("name",params[2])
                        .addFormDataPart("firstName",params[3])
                        .addFormDataPart("section",params[4])
                        .addFormDataPart("departement", params[5])
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
                    Toast.makeText(PreRegistrationActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }

            }catch (Exception e)
            {
                return null;
            }
            return null;
        }
        @Override
        protected void onPostExecute(String jsonData){
            Toast.makeText(PreRegistrationActivity.this, jsonData, Toast.LENGTH_SHORT).show();
        }
    }
    private EditText mIdNumberEditText;
    private EditText mNameEditText;
    private EditText mFirstNameEditText;
    private EditText mSectionEditText;
    private EditText mDepartementEditText;
    private Button mRegisterButton;
    private StudentTable mStudentTable;
    private Session mSession;
}