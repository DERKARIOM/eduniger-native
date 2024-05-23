package com.ninotech.fabi.controleur.fragment;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.ninotech.fabi.R;
import com.ninotech.fabi.controleur.activity.SuggestionActivity;
import com.ninotech.fabi.controleur.dialog.SimpleOkDialog;
import com.ninotech.fabi.model.data.Phone;
import com.ninotech.fabi.model.data.Suggestion;
import com.ninotech.fabi.model.table.Session;

import java.io.IOException;

import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SuggestionFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_suggestion, container, false);
        mSession = new Session(getContext());
        mSuggestionSendButton = view.findViewById(R.id.button_activity_suggestion_send);
        mObjetSpinner = view.findViewById(R.id.spinner_activity_suggestion_object);
        mMessageEditText = view.findViewById(R.id.edit_text_activity_suggestion_message);
        mErrorTextView = view.findViewById(R.id.text_view_activity_suggestion_error);
        CheckBox phoneCheckBox = view.findViewById(R.id.check_box_activity_suggestion_information_phone);
        mConnectionProgressBar = view.findViewById(R.id.progress_bar_activity_suggestion_connection);
        mPhone = new Phone(Build.MODEL,Build.VERSION.RELEASE);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(), R.array.options_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mObjetSpinner.setAdapter(adapter);
        mSuggestionSendButton.setOnClickListener(new View.OnClickListener()  {
            @Override
            public void onClick(View view) {
                mSuggestion = new Suggestion(mSession.getIdNumber(),mObjetSpinner.getSelectedItem().toString(),mMessageEditText.getText().toString());
                if(mSuggestion.getMessage().equals(""))
                    mErrorTextView.setText(R.string.register_error_0000);
                else
                {
                    mConnectionProgressBar.setVisibility(View.VISIBLE);
                    mSuggestionSendButton.setText("");
                    SuggestionSyn suggestionSyn = new SuggestionSyn();
                    suggestionSyn.execute(getString(R.string.ip_server_android ) + "Suggestion.php",mSuggestion.getIdNumber(),mSuggestion.getObjet(),mSuggestion.getMessage(),mPhone.getModel(),mPhone.getVersion());
                }
            }
        });
        return view;
    }
    private class SuggestionSyn extends AsyncTask<String,Void,String> {
        @Override
        protected String doInBackground(String... params) {
            try {
                OkHttpClient client = new OkHttpClient();
                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("idNumber",params[1])
                        .addFormDataPart("objet", params[2])
                        .addFormDataPart("message", params[3])
                        .addFormDataPart("model",params[4])
                        .addFormDataPart("version",params[5])
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
                    Log.e("errorSuggestionActivity",e.getMessage());
                }

            }catch (Exception e)
            {
                Log.e("errorSuggestionActivity",e.getMessage());
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
                    SuccessSuggestionDialog();
                    mConnectionProgressBar.setVisibility(View.INVISIBLE);
                    mSuggestionSendButton.setText(R.string.send);
                }
            }
            else
            {
                mErrorTextView.setText(R.string.no_connection);
                mConnectionProgressBar.setVisibility(View.INVISIBLE);
                mSuggestionSendButton.setText(R.string.send);
            }
        }
    }
    private void SuccessSuggestionDialog(){
        SimpleOkDialog simpleOkDialog = new SimpleOkDialog(getActivity());
        simpleOkDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        simpleOkDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        TextView okTextView = simpleOkDialog.findViewById(R.id.text_view_dialog_simple_ok);
        TextView messageTextView = simpleOkDialog.findViewById(R.id.text_view_dialog_simple_ok_message);
        messageTextView.setText(R.string.suggestion_message_dialog);
        okTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMessageEditText.setText("");
                simpleOkDialog.cancel();
            }
        });
        simpleOkDialog.build();
    }
    private Button mSuggestionSendButton;
    private EditText mMessageEditText;
    private Suggestion mSuggestion;
    private Session mSession;
    private Spinner mObjetSpinner;
    private TextView mErrorTextView;
    private Phone mPhone;
    private ProgressBar mConnectionProgressBar;
    private ImageView mBackImageView;
}