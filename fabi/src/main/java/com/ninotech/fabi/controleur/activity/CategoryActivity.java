package com.ninotech.fabi.controleur.activity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ninotech.fabi.controleur.adapter.BookAdapter;
import com.ninotech.fabi.model.data.Book;
import com.ninotech.fabi.model.table.Session;
import com.ninotech.fabi.R;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class CategoryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);
        Objects.requireNonNull(getSupportActionBar()).hide();
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        mToolbar = findViewById(R.id.relative_layout_toolbar);
        mSession = new Session(this);
        mRecyclerView = findViewById(R.id.recylerCategorie2);
        mList = new ArrayList<>();
        Intent intent = getIntent();
        mCategorie=null;
        if (intent != null && intent.hasExtra("intent_adapter_category_title")) {
            mCategorie = intent.getStringExtra("intent_adapter_category_title");
        }
        mToolbar.setTitle(mCategorie);
        CategoryInSyn categoryInSyn = new CategoryInSyn();
        categoryInSyn.execute(getString(R.string.ip_server_android) + "CategoryIn.php",mSession.getIdNumber(),mCategorie);
    }

    private class CategoryInSyn extends AsyncTask<String,Void,String> {
        @Override
        protected String doInBackground(String... params) {

            try {
                OkHttpClient client = new OkHttpClient();
                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("idNumber",params[1])
                        .addFormDataPart("categoryTitle",params[2])
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
                    Toast.makeText(CategoryActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
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
                            ArrayList<String> category = new ArrayList<>();
                            category.add(jsonArray.getJSONObject(i).getString("categoryTitle"));
                            mList.add(new Book(jsonArray.getJSONObject(i).getString("idBook"),jsonArray.getJSONObject(i).getString("blanket"),jsonArray.getJSONObject(i).getString("bookTitle"),category,jsonArray.getJSONObject(i).getString("isPhysic"),jsonArray.getJSONObject(i).getString("electronic"),jsonArray.getJSONObject(i).getString("isAudio"),0,0));
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    mBookAdapter = new BookAdapter(mList);
                    mRecyclerView.setLayoutManager(new LinearLayoutManager(CategoryActivity.this));
                    mRecyclerView.setAdapter(mBookAdapter);
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
    private BookAdapter mBookAdapter;
    private ArrayList<Book> mList;
    private Session mSession;
    private String mCategorie;
}