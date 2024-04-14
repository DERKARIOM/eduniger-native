package com.ninotech.fabi.controleur.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ninotech.fabi.R;
import com.ninotech.fabi.controleur.adapter.BookAdapter;
import com.ninotech.fabi.controleur.adapter.NoConnectionAdapter;
import com.ninotech.fabi.model.data.Connection;
import com.ninotech.fabi.model.data.OnlineBook;
import com.ninotech.fabi.model.table.Session;

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

public class SearchActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        Objects.requireNonNull(getSupportActionBar()).hide();
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        mRecyclerView = findViewById(R.id.recycler_view_activity_search);
        mSearchEditText = findViewById(R.id.edit_text_toolbar_search);
        mSession = new Session(this);
        mSearchEditText.requestFocus();
        Intent searchIntent = getIntent();
        mSearchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                switch (Objects.requireNonNull(searchIntent.getStringExtra("search_key")))
                {
                    case "ONLINE_BOOK":
                        filterOnlineBook(s.toString());
                        break;
                    case "CATEGORY_ACTIVITY":
                       // filterOnlineBook(s.toString());
                        break;
                }
            }
        });
        switch (Objects.requireNonNull(searchIntent.getStringExtra("search_key")))
        {
            case "ONLINE_BOOK":
                mOnlineBookList = new ArrayList<>();
                mFilteredList = new ArrayList<>();
                waitConnection();
                switch (Objects.requireNonNull(searchIntent.getStringExtra("online_book_key")))
                {
                    case "MAIN_ACTIVITY":
                        searchOnLineBook();
                        break;
                    case "CATEGORY_ACTIVITY":
                        onLineBookSwitchCategory(searchIntent.getStringExtra("title_category"));
                        break;
                }

                break;
            case "CATEGORY_ACTIVITY":
                break;
        }
    }
    public void waitConnection()
    {
        ArrayList<Connection> list = new ArrayList<>();
        list.add(new Connection(getString(R.string.wait),null,true));
        mNoConnectionAdapter = new NoConnectionAdapter(list);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(mNoConnectionAdapter);
    }
    public void searchOnLineBook()
    {
        BroadcastReceiver receiverNoConnectionAdapter = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if ("RANKING_FRAGMENT".equals(intent.getAction())) {
                    try {
                        ArrayList<Connection> list = new ArrayList<>();
                        list.add(new Connection(getString(R.string.wait),"RANKING_FRAGMENT",true));
                        NoConnectionAdapter noConnectionAdapter = new NoConnectionAdapter(list);
                        mRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                        mRecyclerView.setAdapter(noConnectionAdapter);
                        RankingSyn rankingSyn = new RankingSyn();
                        rankingSyn.execute(getString(R.string.ip_server_android) + "Ranking.php", mSession.getIdNumber());
                    }catch (Exception e)
                    {
                        Log.e("errRankingFragment",e.getMessage());
                    }

                }
            }
        };
        registerReceiver(receiverNoConnectionAdapter, new IntentFilter("RANKING_FRAGMENT"));
        RankingSyn rankingSyn = new RankingSyn();
        rankingSyn.execute(getString(R.string.ip_server_android) + "Ranking.php", mSession.getIdNumber());
    }
    public void onLineBookSwitchCategory(String category)
    {
        BroadcastReceiver receiverNoConnectionAdapter = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if ("CATEGORY_ACTIVITY".equals(intent.getAction())) {
                    try {
                        ArrayList<Connection> list = new ArrayList<>();
                        list.add(new Connection(getString(R.string.wait),"CATEGORY_ACTIVITY",true));
                        NoConnectionAdapter noConnectionAdapter = new NoConnectionAdapter(list);
                        mRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                        mRecyclerView.setAdapter(noConnectionAdapter);
                        CategoryInSyn categoryInSyn = new CategoryInSyn();
                        categoryInSyn.execute(getString(R.string.ip_server_android) + "CategoryIn.php",mSession.getIdNumber(),category);
                    }catch (Exception e)
                    {
                        Log.e("errRankingFragment",e.getMessage());
                    }

                }
            }
        };
        registerReceiver(receiverNoConnectionAdapter, new IntentFilter("CATEGORY_ACTIVITY"));
        CategoryInSyn categoryInSyn = new CategoryInSyn();
        categoryInSyn.execute(getString(R.string.ip_server_android) + "CategoryIn.php",mSession.getIdNumber(),category);
    }
    private class RankingSyn extends AsyncTask<String,Void,String> {
        @Override
        protected String doInBackground(String... params) {

            try {
                OkHttpClient client = new OkHttpClient();
                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("idNumber",params[1])
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
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
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
                JSONArray jsonArray = null;
                try {
                    jsonArray = new JSONArray(jsonData);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
                for (int i=0;i<jsonArray.length();i++) {
                    try {
                        mOnlineBookList.add(new OnlineBook(jsonArray.getJSONObject(i).getString("idBook"),jsonArray.getJSONObject(i).getString("blanket"),jsonArray.getJSONObject(i).getString("bookTitle"),jsonArray.getJSONObject(i).getString("categoryTitle"),jsonArray.getJSONObject(i).getString("isPhysic"),jsonArray.getJSONObject(i).getString("electronic"),jsonArray.getJSONObject(i).getString("isAudio"),Integer.parseInt(jsonArray.getJSONObject(i).getString("numberLike")),Integer.parseInt(jsonArray.getJSONObject(i).getString("numberLike"))));
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                }
                mBookAdapter = new BookAdapter(mOnlineBookList);
                mRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                mRecyclerView.setAdapter(mBookAdapter);
            }
            else
            {
                ArrayList<Connection> list = new ArrayList<>();
                list.add(new Connection(getString(R.string.no_connection_available),"RANKING_FRAGMENT",false));
                NoConnectionAdapter noConnectionAdapter = new NoConnectionAdapter(list);
                mRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                mRecyclerView.setAdapter(noConnectionAdapter);
            }
        }
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
                    Toast.makeText(SearchActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
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
                            mOnlineBookList.add(new OnlineBook(jsonArray.getJSONObject(i).getString("idBook"),jsonArray.getJSONObject(i).getString("blanket"),jsonArray.getJSONObject(i).getString("bookTitle"),jsonArray.getJSONObject(i).getString("categoryTitle"),jsonArray.getJSONObject(i).getString("isPhysic"),jsonArray.getJSONObject(i).getString("electronic"),jsonArray.getJSONObject(i).getString("isAudio"),0,0));
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    mBookAdapter = new BookAdapter(mOnlineBookList);
                    mRecyclerView.setLayoutManager(new LinearLayoutManager(SearchActivity.this));
                    mRecyclerView.setAdapter(mBookAdapter);
                }
            }
            else
            {
                ArrayList<Connection> list = new ArrayList<>();
                list.add(new Connection(getString(R.string.no_connection_available),"CATEGORY_ACTIVITY",false));
                NoConnectionAdapter noConnectionAdapter = new NoConnectionAdapter(list);
                mRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                mRecyclerView.setAdapter(noConnectionAdapter);
            }

        }
    }
    private void filterOnlineBook(String text) {
        mFilteredList.clear();
        for (OnlineBook item : mOnlineBookList) {
            if (item.getTitle().toLowerCase().contains(text.toLowerCase())) {
                mFilteredList.add(item);
            }
        }
        mBookAdapter.filterList(mFilteredList);
    }
    private RecyclerView mRecyclerView;
    private ArrayList<OnlineBook> mOnlineBookList;
    private NoConnectionAdapter mNoConnectionAdapter;
    private Session mSession;
    private EditText mSearchEditText;
    private ArrayList<OnlineBook> mFilteredList;
    private BookAdapter mBookAdapter;
}