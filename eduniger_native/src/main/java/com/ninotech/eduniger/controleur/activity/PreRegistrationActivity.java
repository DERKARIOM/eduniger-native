package com.ninotech.eduniger.controleur.activity;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.ninotech.eduniger.R;
import com.ninotech.eduniger.controleur.dialog.SimpleOkDialog;
import com.ninotech.eduniger.model.data.Server;
import com.ninotech.eduniger.model.table.Session;
import com.ninotech.eduniger.model.table.StudentTable;

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
        mErrorTextView = findViewById(R.id.text_view_activity_pre_register_error);
        mProgressBar = findViewById(R.id.progress_bar_activity_pre_register_connection);
        mSession = new Session(getApplicationContext());
        mStudentTable = new StudentTable(getApplicationContext());
        mSectionEditText.setText(mStudentTable.getSection(mSession.getIdNumber()));
        mDepartementEditText.setText(mStudentTable.getDepartement(mSession.getIdNumber()));
        mRegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PreRegisterSyn preRegisterSyn = new PreRegisterSyn();
                switch (inputControl())
                {
                    case "00000":
                        inputControlView(
                                R.drawable.forme_white_radius_100dp_border_rouge,
                                R.drawable.forme_white_radius_100dp_border_rouge,
                                R.drawable.forme_white_radius_100dp_border_rouge,
                                R.drawable.forme_white_radius_100dp_border_rouge,
                                R.drawable.forme_white_radius_100dp_border_rouge,
                                R.string.register_error_0000
                        );
                        break;
                    case "01111":
                        inputControlView(
                                R.drawable.forme_white_radius_100dp_border_rouge,
                                R.drawable.forme_white_radius_10dp,
                                R.drawable.forme_white_radius_10dp,
                                R.drawable.forme_white_radius_10dp,
                                R.drawable.forme_white_radius_10dp,
                                R.string.register_error_01111
                        );
                        break;
                    case "10111":
                        inputControlView(
                                R.drawable.forme_white_radius_10dp,
                                R.drawable.forme_white_radius_100dp_border_rouge,
                                R.drawable.forme_white_radius_10dp,
                                R.drawable.forme_white_radius_10dp,
                                R.drawable.forme_white_radius_10dp,
                                R.string.register_error_10111
                        );
                        break;
                    case "11011":
                        inputControlView(
                                R.drawable.forme_white_radius_10dp,
                                R.drawable.forme_white_radius_10dp,
                                R.drawable.forme_white_radius_100dp_border_rouge,
                                R.drawable.forme_white_radius_10dp,
                                R.drawable.forme_white_radius_10dp,
                                R.string.register_error_11011
                        );
                        break;
                    case "11101":
                        inputControlView(
                                R.drawable.forme_white_radius_10dp,
                                R.drawable.forme_white_radius_10dp,
                                R.drawable.forme_white_radius_10dp,
                                R.drawable.forme_white_radius_100dp_border_rouge,
                                R.drawable.forme_white_radius_10dp,
                                R.string.register_error_11101
                        );
                        break;
                    case "11110":
                        inputControlView(
                                R.drawable.forme_white_radius_10dp,
                                R.drawable.forme_white_radius_10dp,
                                R.drawable.forme_white_radius_10dp,
                                R.drawable.forme_white_radius_10dp,
                                R.drawable.forme_white_radius_100dp_border_rouge,
                                R.string.register_error_11101
                        );
                        break;
                    case "11111":
                        mProgressBar.setVisibility(View.VISIBLE);
                        mRegisterButton.setText("Enregistrement.");
                        preRegisterSyn.execute(Server.getUrlApi(getApplicationContext()) + "PreRegister.php",
                                mIdNumberEditText.getText().toString(),
                                mNameEditText.getText().toString(),
                                mFirstNameEditText.getText().toString(),
                                mSectionEditText.getText().toString(),
                                mDepartementEditText.getText().toString());
                        break;

                }
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
            if(jsonData != null)
            {
                if(jsonData.equals("true"))
                {
                    mProgressBar.setVisibility(View.INVISIBLE);
                    mRegisterButton.setText("Enregistrer");
                    simpleDialogOk(R.drawable.vector_vert_success_2,"Préinscription réussie","La préinscription de l'étudiant avec le numéro de matricule " + mIdNumberEditText.getText().toString() +" a été effectuée avec succès.");
                    mIdNumberEditText.setText("");
                    mNameEditText.setText("");
                    mFirstNameEditText.setText("");
                }
                else
                {
                    mProgressBar.setVisibility(View.INVISIBLE);
                    mRegisterButton.setText("Enregistrer");
                    simpleDialogOk(R.drawable.vector_sorry_2,"Préinscription déjà existante","L'étudiant avec le numéro de matricule " + mIdNumberEditText.getText().toString() + " a déjà une préinscription enregistrée.");
                    mIdNumberEditText.setText("");
                    mNameEditText.setText("");
                    mFirstNameEditText.setText("");
                }
            }
            else
            {
                simpleDialogOk(R.drawable.vector_rouge_error2,"Problème de connexion","Il semble que vous n'êtes pas connecté à Internet. Veuillez vérifier votre connexion et réessayer.");
            }
        }
    }
    public String inputControl()
    {
        if(mIdNumberEditText.getText().toString().isEmpty() && mNameEditText.getText().toString().isEmpty() && mFirstNameEditText.getText().toString().isEmpty() && mSectionEditText.getText().toString().isEmpty() && mDepartementEditText.getText().toString().isEmpty())
            return "00000"; // "Veuillez remplir ces champs svp"
        else
        {
            if(mIdNumberEditText.getText().toString().isEmpty())
                return "01111"; // Votre matricule svp
            if(mNameEditText.getText().toString().isEmpty())
                return "10111"; // Votre email svp
            if(mFirstNameEditText.getText().toString().isEmpty())
                return "11011";
            if(mSectionEditText.getText().toString().isEmpty())
                return "11101";
            if(mDepartementEditText.getText().toString().isEmpty())
                return "11110"; // Connexion...
            else
                return "11111"; // ok
        }
    }
    public void inputControlView(int idNumberForm , int nameForm , int firstNameForm , int sectionForm , int departementForm , int message)
    {
        mIdNumberEditText.setBackground(getResources().getDrawable(idNumberForm));
        mNameEditText.setBackground(getResources().getDrawable(nameForm));
        mFirstNameEditText.setBackground(getResources().getDrawable(firstNameForm));
        mSectionEditText.setBackground(getResources().getDrawable(sectionForm));
        mDepartementEditText.setBackground(getResources().getDrawable(departementForm));
        mErrorTextView.setText(message);
    }
    private void simpleDialogOk(int ico , String title , String message){
        SimpleOkDialog simpleOkDialog = new SimpleOkDialog(this);
        Objects.requireNonNull(simpleOkDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        simpleOkDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        ImageView icoImageView = simpleOkDialog.findViewById(R.id.image_view_dialog_simple_ok_icon);
        TextView titleTextView = simpleOkDialog.findViewById(R.id.text_view_dialog_simple_ok_title);
        TextView messageTextView = simpleOkDialog.findViewById(R.id.text_view_dialog_simple_ok_message);
        TextView okTextView = simpleOkDialog.findViewById(R.id.text_view_dialog_simple_ok);
        icoImageView.setImageResource(ico);
        titleTextView.setText(title);
        messageTextView.setText(message);
        okTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                simpleOkDialog.cancel();
            }
        });
        simpleOkDialog.build();
    }
    private EditText mIdNumberEditText;
    private EditText mNameEditText;
    private EditText mFirstNameEditText;
    private EditText mSectionEditText;
    private EditText mDepartementEditText;
    private Button mRegisterButton;
    private StudentTable mStudentTable;
    private Session mSession;
    private TextView mErrorTextView;
    private ProgressBar mProgressBar;
}