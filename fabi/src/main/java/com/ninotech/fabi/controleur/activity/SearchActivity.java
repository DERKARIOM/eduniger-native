package com.ninotech.fabi.controleur.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
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
import com.ninotech.fabi.controleur.adapter.AudioBookAdapter;
import com.ninotech.fabi.controleur.adapter.BookAdapter;
import com.ninotech.fabi.controleur.adapter.ElectronicBookAdapter;
import com.ninotech.fabi.controleur.adapter.NoConnectionAdapter;
import com.ninotech.fabi.controleur.adapter.VoidContainerAdapter;
import com.ninotech.fabi.model.data.AudioBook;
import com.ninotech.fabi.model.data.Connection;
import com.ninotech.fabi.model.data.ElectronicBook;
import com.ninotech.fabi.model.data.OnlineBook;
import com.ninotech.fabi.model.data.VoidContainer;
import com.ninotech.fabi.model.table.AudioTable;
import com.ninotech.fabi.model.table.ElectronicTable;
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
                        if(!mOnlineBooks.isEmpty())
                            filterOnlineBook(s.toString());
                        break;
                    case "ELECTRONIC_BOOK":
                        if(!mElectronicBooks.isEmpty())
                            filterElectronicBook(s.toString());
                        break;
                    case "AUDIO_BOOK":
                        if(!mAudioBooks.isEmpty())
                            filterAudioBook(s.toString());
                        break;
                }
            }
        });
        switch (Objects.requireNonNull(searchIntent.getStringExtra("search_key")))
        {
            case "ONLINE_BOOK":
                mOnlineBooks = new ArrayList<>();
                mFilteredOnlineBookList = new ArrayList<>();
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
            case "ELECTRONIC_BOOK":
                searchElectronicBook();
                break;
            case "AUDIO_BOOK":
                searchAudioBook();
                break;
            case "LOAND_BOOK":
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
                        mOnlineBooks.add(new OnlineBook(jsonArray.getJSONObject(i).getString("idBook"),jsonArray.getJSONObject(i).getString("blanket"),jsonArray.getJSONObject(i).getString("bookTitle"),jsonArray.getJSONObject(i).getString("categoryTitle"),jsonArray.getJSONObject(i).getString("isPhysic"),jsonArray.getJSONObject(i).getString("electronic"),jsonArray.getJSONObject(i).getString("isAudio"),Integer.parseInt(jsonArray.getJSONObject(i).getString("numberLike")),Integer.parseInt(jsonArray.getJSONObject(i).getString("numberLike"))));
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                }
                mBookAdapter = new BookAdapter(mOnlineBooks);
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
                            mOnlineBooks.add(new OnlineBook(jsonArray.getJSONObject(i).getString("idBook"),jsonArray.getJSONObject(i).getString("blanket"),jsonArray.getJSONObject(i).getString("bookTitle"),jsonArray.getJSONObject(i).getString("categoryTitle"),jsonArray.getJSONObject(i).getString("isPhysic"),jsonArray.getJSONObject(i).getString("electronic"),jsonArray.getJSONObject(i).getString("isAudio"),0,0));
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    mBookAdapter = new BookAdapter(mOnlineBooks);
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
    public void searchElectronicBook()
    {
        try {
            mElectronicBooks = new ArrayList<>();
            mFilteredElectronicBooks = new ArrayList<>();
            ElectronicTable electronicTable = new ElectronicTable(this);
            Cursor electronicCursor = electronicTable.getData(mSession.getIdNumber());
            electronicCursor.moveToFirst();
            do {
                mElectronicBooks.add(new ElectronicBook(electronicCursor.getString(2),electronicCursor.getString(5),electronicCursor.getString(8),electronicCursor.getString(7),electronicCursor.getString(4),electronicCursor.getString(6)));
            }while(electronicCursor.moveToNext());
            mElectronicBookAdapter = new ElectronicBookAdapter(mElectronicBooks);
            mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            mRecyclerView.setAdapter(mElectronicBookAdapter);
        }catch (Exception e)
        {
            voidContainer(R.drawable.img_telecharge_local,getString(R.string.no_electronic_book));
            Log.e("ErrorElectronic",e.getMessage());
        }
    }
    private void filterOnlineBook(String text) {
        mFilteredOnlineBookList.clear();
        for (OnlineBook item : mOnlineBooks) {
            if (item.getTitle().toLowerCase().contains(text.toLowerCase())) {
                mFilteredOnlineBookList.add(item);
            }
        }
        mBookAdapter.filterList(mFilteredOnlineBookList);
    }
    private void filterElectronicBook(String text) {
        mFilteredElectronicBooks.clear();
        for (ElectronicBook item : mElectronicBooks) {
            if (item.getTitle().toLowerCase().contains(text.toLowerCase())) {
                mFilteredElectronicBooks.add(item);
            }
        }
        mElectronicBookAdapter.filterList(mFilteredElectronicBooks);
    }
    private void filterAudioBook(String text) {
        mFilteredAudioBook.clear();
        for (AudioBook item : mAudioBooks) {
            if (item.getTitle().toLowerCase().contains(text.toLowerCase())) {
                mFilteredAudioBook.add(item);
            }
        }
        mAudioBookAdapter.filterList(mFilteredAudioBook);
    }
    public void searchAudioBook()
    {
        try {
            mAudioBooks = new ArrayList<>();
            mFilteredAudioBook = new ArrayList<>();
            AudioTable audioTable = new AudioTable(this);
            Cursor audioCursor = audioTable.getData(mSession.getIdNumber());
            audioCursor.moveToFirst();
            do {
                mAudioBooks.add(new AudioBook(audioCursor.getString(2),audioCursor.getString(5),audioCursor.getString(8),audioCursor.getString(4),audioCursor.getString(11),audioCursor.getString(6)));
            }while (audioCursor.moveToNext());
            mAudioBookAdapter = new AudioBookAdapter(mAudioBooks);
            mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            mRecyclerView.setAdapter(mAudioBookAdapter);
        }catch (Exception e)
        {
            voidContainer(R.drawable.img_playliste_local,getString(R.string.no_audio_book));
            Log.e("ErrorAudio",e.getMessage());
        }
    }
    public void voidContainer(int image , String message)
    {
        ArrayList<VoidContainer> voidContainers = new ArrayList<>();
        voidContainers.add(new VoidContainer(image,message));
        VoidContainerAdapter voidContainerAdapter = new VoidContainerAdapter(voidContainers);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(voidContainerAdapter);
    }
    private RecyclerView mRecyclerView;
    private ArrayList<OnlineBook> mOnlineBooks;
    private NoConnectionAdapter mNoConnectionAdapter;
    private Session mSession;
    private EditText mSearchEditText;
    private ArrayList<OnlineBook> mFilteredOnlineBookList;
    private BookAdapter mBookAdapter;
    private ArrayList<ElectronicBook> mFilteredElectronicBooks;
    private ArrayList<ElectronicBook> mElectronicBooks;
    private ElectronicBookAdapter mElectronicBookAdapter;
    private ArrayList<AudioBook> mAudioBooks;
    private ArrayList<AudioBook> mFilteredAudioBook;
    private AudioBookAdapter mAudioBookAdapter;
}