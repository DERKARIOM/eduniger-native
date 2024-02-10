package com.ninotech.fabi.model.syn;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.ninotech.fabi.controleur.activity.BookActivity;

import java.io.IOException;

import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
public class SendComments extends AsyncTask<String,Void,String> {
    @Override
    protected String doInBackground(String... params) {
        try {
            OkHttpClient client = new OkHttpClient();
            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("idNumber",params[1])
                    .addFormDataPart("idBook",params[2])
                    .addFormDataPart("message",params[3])
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
                Log.e("errSendComments",e.getMessage());
            }

        }catch (Exception e)
        {
            return null;
        }
        return null;
    }
    @Override
    protected void onPostExecute(String jsonData){
    }
}

