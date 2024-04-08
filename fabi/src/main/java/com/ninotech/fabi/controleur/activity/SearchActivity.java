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
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ninotech.fabi.R;
import com.ninotech.fabi.controleur.adapter.BookAdapter;
import com.ninotech.fabi.controleur.adapter.NoConnectionAdapter;
import com.ninotech.fabi.controleur.fragment.RankingFragment;
import com.ninotech.fabi.model.data.Category;
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
        mBookRecyclerView = findViewById(R.id.recycler_view_activity_search);
        mSearchEditText = findViewById(R.id.edit_text_toolbar_search);
        mOnlineBookList = new ArrayList<>();
        ArrayList<Connection> list = new ArrayList<>();
        mFilteredList = new ArrayList<>();
        mSession = new Session(this);
        mSearchEditText.requestFocus();
        list.add(new Connection(getString(R.string.wait),null,true));
        mNoConnectionAdapter = new NoConnectionAdapter(list);
        mBookRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mBookRecyclerView.setAdapter(mNoConnectionAdapter);
        mSearchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                filter(s.toString());
            }
        });
        BroadcastReceiver receiverNoConnectionAdapter = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if ("RANKING_FRAGMENT".equals(intent.getAction())) {
                    try {
                        ArrayList<Connection> list = new ArrayList<>();
                        list.add(new Connection(getString(R.string.wait),"RANKING_FRAGMENT",true));
                        NoConnectionAdapter noConnectionAdapter = new NoConnectionAdapter(list);
                        mBookRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                        mBookRecyclerView.setAdapter(noConnectionAdapter);
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
                mBookRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                mBookRecyclerView.setAdapter(mBookAdapter);
            }
            else
            {
                ArrayList<Connection> list = new ArrayList<>();
                list.add(new Connection(getString(R.string.no_connection_available),"RANKING_FRAGMENT",false));
                NoConnectionAdapter noConnectionAdapter = new NoConnectionAdapter(list);
                mBookRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                mBookRecyclerView.setAdapter(noConnectionAdapter);
            }
        }
    }
    private void filter(String text) {
        mFilteredList.clear();
        for (OnlineBook item : mOnlineBookList) {
            if (item.getTitle().toLowerCase().contains(text.toLowerCase())) {
                mFilteredList.add(item);
            }
        }
        mBookAdapter.filterList(mFilteredList);
    }
    private RecyclerView mBookRecyclerView;
    private ArrayList<OnlineBook> mOnlineBookList;
    private NoConnectionAdapter mNoConnectionAdapter;
    private Session mSession;
    private EditText mSearchEditText;
    private ArrayList<OnlineBook> mFilteredList;
    private BookAdapter mBookAdapter;
}