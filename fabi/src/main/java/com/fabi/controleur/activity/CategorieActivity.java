package com.fabi.controleur.activity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fabi.controleur.adapter.ClassementAdapter;
import com.fabi.model.data.Livres;
import com.fabi.model.table.Session;
import com.example.fabi.R;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class CategorieActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_categorie);
        getSupportActionBar().hide();
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        mToolbar = findViewById(R.id.toolbar2);
        mSession = new Session(this);
        mRecyclerView = findViewById(R.id.recylerCategorie2);
        mList = new ArrayList<>();
        Intent intent = getIntent();
        mCategorie=null;
        if (intent != null && intent.hasExtra("nomCat")) {
            mCategorie = intent.getStringExtra("nomCat");
        }
        mToolbar.setTitle(mCategorie);
        Http http = new Http();
        http.execute("http://192.168.43.1:2222/fabi/android/categorie2.php");
    }

    private class Http extends AsyncTask<String,Void,String> {
        @Override
        protected String doInBackground(String... params) {

            try {
                OkHttpClient client = new OkHttpClient();
                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("matricule",mSession.getMatricule())
                        .addFormDataPart("nomCategorie",mCategorie)
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
                    Toast.makeText(CategorieActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
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
                if(!jsonData.equals("RAS"))
                {
                    JSONArray jsonArray = null;
                    try {
                        jsonArray = new JSONArray(jsonData);
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                    for (int i=0;i<jsonArray.length();i++) {
                        try {
                            mList.add(new Livres(jsonArray.getJSONObject(i).getString("idLivre"),jsonArray.getJSONObject(i).getString("couverture"),jsonArray.getJSONObject(i).getString("titreLivre"),jsonArray.getJSONObject(i).getString("nomCat"),jsonArray.getJSONObject(i).getString("estPhysique"),jsonArray.getJSONObject(i).getString("documentElec"),jsonArray.getJSONObject(i).getString("estAudio"),"0","0"));
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    mClassementAdapter = new ClassementAdapter(mList);
                    mRecyclerView.setLayoutManager(new LinearLayoutManager(CategorieActivity.this));
                    mRecyclerView.setAdapter(mClassementAdapter);
                }
                //Toast.makeText(getContext(), jsonData, Toast.LENGTH_SHORT).show();
            }
            else
            {

            }

        }
    }
    private Toolbar mToolbar;
    private RecyclerView mRecyclerView;
    private ClassementAdapter mClassementAdapter;
    private ArrayList<Livres> mList;
    private Session mSession;
    private String mCategorie;
}