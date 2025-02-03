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
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ninotech.fabi.R;
import com.ninotech.fabi.controleur.adapter.AudioBookAdapter;
import com.ninotech.fabi.controleur.adapter.AuthorHorizontaleAdapter;
import com.ninotech.fabi.controleur.adapter.AuthorLocalAdapter;
import com.ninotech.fabi.controleur.adapter.AuthorVerticaleAdapter;
import com.ninotech.fabi.controleur.adapter.FabiolaBookAdapter;
import com.ninotech.fabi.controleur.adapter.OnlineBookAdapter;
import com.ninotech.fabi.controleur.adapter.CategoryLocalAdapter;
import com.ninotech.fabi.controleur.adapter.ElectronicBookAdapter;
import com.ninotech.fabi.controleur.adapter.LoandBookAdapter;
import com.ninotech.fabi.controleur.adapter.NoConnectionAdapter;
import com.ninotech.fabi.controleur.adapter.NotificationAdapter;
import com.ninotech.fabi.controleur.adapter.SettingAdapter;
import com.ninotech.fabi.controleur.adapter.StructureAdapter;
import com.ninotech.fabi.controleur.adapter.VoidContainerAdapter;
import com.ninotech.fabi.controleur.fragment.HomeFragment;
import com.ninotech.fabi.controleur.fragment.StructureFragment;
import com.ninotech.fabi.model.data.AudioBook;
import com.ninotech.fabi.model.data.Author;
import com.ninotech.fabi.model.data.Category;
import com.ninotech.fabi.model.data.Connection;
import com.ninotech.fabi.model.data.ElectronicBook;
import com.ninotech.fabi.model.data.LoandBook;
import com.ninotech.fabi.model.data.Notification;
import com.ninotech.fabi.model.data.OnlineBook;
import com.ninotech.fabi.model.data.Setting;
import com.ninotech.fabi.model.data.Structure;
import com.ninotech.fabi.model.data.VoidContainer;
import com.ninotech.fabi.model.table.AudioTable;
import com.ninotech.fabi.model.table.ElectronicTable;
import com.ninotech.fabi.model.table.LoandTable;
import com.ninotech.fabi.model.table.NotificationTable;
import com.ninotech.fabi.model.table.Session;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
        mBackImageView = findViewById(R.id.image_view_toolbar_search);
        mSession = new Session(this);
        mSearchEditText.requestFocus();
        Intent searchIntent = getIntent();
        BroadcastReceiver receiverFabiolaBookAdapter = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if ("ACTION_RECOVER_BOOK".equals(intent.getAction())) {
                    finish();
                }
            }
        };
        registerReceiver(receiverFabiolaBookAdapter, new IntentFilter("ACTION_RECOVER_BOOK"));
        mBackImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
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
                    case "LOAND_BOOK":
                        if(!mLoandBooks.isEmpty())
                            filterLoandBook(s.toString());
                        break;
                    case "FABIOLA_BOOK":
                        filterFabiolaBook(s.toString());
                        break;
                    case "CATEGORY":
                        if(!mCategorys.isEmpty())
                            filterCategory(s.toString());
                        break;
                    case "STRUCTURE":
                        if(!mStructures.isEmpty())
                            filterOnlineStructure(s.toString());
                        break;
                    case "AUTHOR":
                        if(!mAuthors.isEmpty())
                            filterAuthor(s.toString());
                    case "AUTHOR_ONLINE":
                        if(!mAuthors.isEmpty())
                            filterAuthorOnline(s.toString());
                        break;
                    case "NOTIFICATION":
                        if(!mNotifications.isEmpty())
                            filterNotification(s.toString());
                        break;
                    case "SETTING":
                        if(!mSettings.isEmpty())
                            filterSettings(s.toString());
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
            case "STRUCTURE":
                mStructures = new ArrayList<>();
                mFilterStructures = new ArrayList<>();
                StructAdapter = new StructureAdapter(mStructures);
                mSearchEditText.setHint(R.string.search_structure);
                searchOnLineStructure();
                break;
            case "ELECTRONIC_BOOK":
                searchElectronicBook();
                break;
            case "AUDIO_BOOK":
                searchAudioBook();
                break;
            case "LOAND_BOOK":
                searchLoandBook();
                break;
            case "FABIOLA_BOOK":
                mOnlineBooks = new ArrayList<>();
                mFilteredOnlineBookList = new ArrayList<>();
                searchFabiolaBook();
                break;
            case "CATEGORY":
                searchCategory();
                break;
            case "AUTHOR_ONLINE":
                mAuthors = new ArrayList<>();
                mFilteredAuthors = new ArrayList<>();
                mSearchEditText.setHint(R.string.search_author);
                searchAuthorOnline();
                break;
            case "AUTHOR":
                searchAuthor();
                break;
            case "NOTIFICATION":
                searchNotification();
                break;
            case "SETTING":
                searchSetting();
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
                        FabiolaBookSyn rankingSyn = new FabiolaBookSyn();
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

    public void searchOnLineStructure()
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
                        StructureSyn structureSyn = new StructureSyn();
                        structureSyn.execute(getString(R.string.ip_server_android) + "Structure.php", mSession.getIdNumber());
                        StructureSyn2 structureSyn2 = new StructureSyn2();
                        structureSyn2.execute(getString(R.string.ip_server_android) + "Structure2.php", mSession.getIdNumber());
                    }catch (Exception e)
                    {
                        Log.e("errRankingFragment",e.getMessage());
                    }

                }
            }
        };
        registerReceiver(receiverNoConnectionAdapter, new IntentFilter("RANKING_FRAGMENT"));
        StructureSyn structureSyn = new StructureSyn();
        structureSyn.execute(getString(R.string.ip_server_android) + "Structure.php", mSession.getIdNumber());
        StructureSyn2 structureSyn2 = new StructureSyn2();
        structureSyn2.execute(getString(R.string.ip_server_android) + "Structure2.php", mSession.getIdNumber());
    }
    public void searchFabiolaBook()
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
                        FabiolaBookSyn fabiolaBookSyn = new FabiolaBookSyn();
                        fabiolaBookSyn.execute(getString(R.string.ip_server_android) + "FabiolaBook.php", mSession.getIdNumber());
                    }catch (Exception e)
                    {
                        Log.e("errRankingFragment",e.getMessage());
                    }

                }
            }
        };
        registerReceiver(receiverNoConnectionAdapter, new IntentFilter("RANKING_FRAGMENT"));
        FabiolaBookSyn fabiolaBookSyn = new FabiolaBookSyn();
        fabiolaBookSyn.execute(getString(R.string.ip_server_android) + "FabiolaBook.php", mSession.getIdNumber());
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
                mOnlineBookAdapter = new OnlineBookAdapter(mOnlineBooks);
                mRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                mRecyclerView.setAdapter(mOnlineBookAdapter);
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

    private class FabiolaBookSyn extends AsyncTask<String,Void,String> {
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
                mFabiolaBookAdapter = new FabiolaBookAdapter(mOnlineBooks);
                mRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                mRecyclerView.setAdapter(mFabiolaBookAdapter);
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
                    mOnlineBookAdapter = new OnlineBookAdapter(mOnlineBooks);
                    mRecyclerView.setLayoutManager(new LinearLayoutManager(SearchActivity.this));
                    mRecyclerView.setAdapter(mOnlineBookAdapter);
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
        mOnlineBookAdapter.filterList(mFilteredOnlineBookList);
    }
    private void filterOnlineStructure(String text) {
        mFilterStructures.clear();
        for (Structure item : mStructures) {
            if (item.getName().toLowerCase().contains(text.toLowerCase())) {
                mFilterStructures.add(item);
            }
        }
        StructAdapter.filterList(mFilterStructures);
    }
    private void filterFabiolaBook(String text) {
        mFilteredOnlineBookList.clear();
        for (OnlineBook item : mOnlineBooks) {
            if (item.getTitle().toLowerCase().contains(text.toLowerCase())) {
                mFilteredOnlineBookList.add(item);
            }
        }
        mFabiolaBookAdapter.filterList(mFilteredOnlineBookList);
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
    private void filterLoandBook(String text) {
        mFilteredLoandBooks.clear();
        for (LoandBook item : mLoandBooks) {
            if (item.getTitle().toLowerCase().contains(text.toLowerCase())) {
                mFilteredLoandBooks.add(item);
            }
        }
        mLoandBookAdapter.filterList(mFilteredLoandBooks);
    }
    private void filterCategory(String text) {
        mFilteredCategorys.clear();
        for (Category item : mCategorys) {
            if (item.getTitle().toLowerCase().contains(text.toLowerCase())) {
                mFilteredCategorys.add(item);
            }
        }
        mCategoryLocalAdapter.filterList(mFilteredCategorys);
    }
    private void filterAuthor(String text) {
        mFilteredAuthors.clear();
        for (Author item : mAuthors) {
            if (item.getName().toLowerCase().contains(text.toLowerCase())) {
                mFilteredAuthors.add(item);
            }
        }
        mAuthorLocalAdapter.filterList(mFilteredAuthors);
    }
    private void filterAuthorOnline(String text) {
        mFilteredAuthors.clear();
        for (Author item : mAuthors) {
            if (item.getName().toLowerCase().contains(text.toLowerCase())) {
                mFilteredAuthors.add(item);
            }
        }
        mAuthorVerticaleAdapter.filterList(mFilteredAuthors);
    }
    private void filterNotification(String text) {
        mFilteredNotifications.clear();
        for (Notification item : mNotifications) {
            if (item.getMessage().toLowerCase().contains(text.toLowerCase())) {
                mFilteredNotifications.add(item);
            }
        }
        mNotificationAdapter.filterList(mFilteredNotifications);
    }
    private void filterSettings(String text) {
        mFilteredSettings.clear();
        for (Setting item : mSettings) {
            if (item.getSousTritre().toLowerCase().contains(text.toLowerCase())) {
                mFilteredSettings.add(item);
            }
        }
        mSettingAdapter.filterList(mFilteredSettings);
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
    public void searchLoandBook()
    {
        mLoandBooks = new ArrayList<>();
        mFilteredAudioBook = new ArrayList<>();
        LoandTable loandTable = new LoandTable(this);
        Cursor LoandCursor = loandTable.getData();
        LoandCursor.moveToFirst();
        try {
            do {
                mLoandBooks.add(new LoandBook(LoandCursor.getString(2),LoandCursor.getString(3),LoandCursor.getString(4),LoandCursor.getString(5),percentage(converterDate(LoandCursor.getString(4)),converterDate(LoandCursor.getString(5)),getNowDate())));
            }while (LoandCursor.moveToNext());
            mLoandBookAdapter = new LoandBookAdapter(mLoandBooks);
            mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            mRecyclerView.setAdapter(mLoandBookAdapter);
        }catch (Exception e)
        {
            voidContainer(R.drawable.img_physical,getString(R.string.no_loand_book));
        }
    }
    void searchCategory()
    {
        mSearchEditText.setHint(R.string.search_category);
        try {
            mCategorys = new ArrayList<>();
            mFilteredCategorys = new ArrayList<>();
            ElectronicTable electronicTable = new ElectronicTable(this);
            Cursor categoryCursor = electronicTable.getCategoryData(mSession.getIdNumber());
            categoryCursor.moveToFirst();
            do {
                mCategorys.add(new Category(categoryCursor.getString(0),categoryCursor.getString(1)));
            }while (categoryCursor.moveToNext());
            mCategoryLocalAdapter = new CategoryLocalAdapter(mCategorys);
            mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            mRecyclerView.setAdapter(mCategoryLocalAdapter);
        }
        catch (Exception e)
        {
            voidContainer(R.drawable.img_categorie,getString(R.string.no_category));
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
    public void searchAuthor()
    {
        mSearchEditText.setHint(R.string.search_author);
        try {
            mAuthors = new ArrayList<>();
            mFilteredAuthors = new ArrayList<>();
            ElectronicTable electronicTable = new ElectronicTable(this);
            Cursor authorCursor = electronicTable.getAuthorData(mSession.getIdNumber());
            authorCursor.moveToFirst();
            do {
                mAuthors.add(new Author(authorCursor.getString(0),authorCursor.getString(1)));
            }while (authorCursor.moveToNext());
            mAuthorLocalAdapter = new AuthorLocalAdapter(mAuthors);
            mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            mRecyclerView.setAdapter(mAuthorLocalAdapter);
        }catch (Exception e)
        {
            voidContainer(R.drawable.img_auteur_local,getString(R.string.no_author));
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    public void searchNotification()
    {
        mSearchEditText.setHint(R.string.search_notification);
        mNotifications = new ArrayList<Notification>();
        mFilteredNotifications = new ArrayList<>();
        NotificationTable notificationTable = new NotificationTable(this);
        Cursor cursor = notificationTable.getData(mSession.getIdNumber());
        cursor.moveToFirst();
        try {
            do {
                mNotifications.add(new Notification(cursor.getString(0),cursor.getString(2),cursor.getString(3),cursor.getString(4)));
            }while(cursor.moveToNext());
            mNotificationAdapter = new NotificationAdapter(mNotifications);
            mRecyclerView.setLayoutManager(new LinearLayoutManager(this.getApplicationContext()));
            mRecyclerView.setAdapter(mNotificationAdapter);
            mRecyclerView.smoothScrollToPosition(mNotificationAdapter.getItemCount()-1);
        }catch (Exception e)
        {
            Log.e("ErrGetDataNotification",e.getMessage());
            voidContainer(R.drawable.img_message_suggestion,getString(R.string.no_notification));
        }
    }
    public void searchSetting()
    {
        mSearchEditText.setHint(R.string.search_setting);
        mSettings = new ArrayList<>();
        mFilteredSettings = new ArrayList<>();
        mSettings.add(new Setting(R.drawable.vector_purple_200_compte,getString(R.string.account),getString(R.string.change_password)));
        mSettings.add(new Setting(R.drawable.vector_purple_200_digital,getString(R.string.digital_print),getString(R.string.secure_session)));
        mSettings.add(new Setting(R.drawable.vector_purple_200_messagerie,getString(R.string.send_suggestion),getString(R.string.subject_suggestion)));
        mSettings.add(new Setting(R.drawable.vector_purple_200_start,getString(R.string.evaluate_us),getString(R.string.opservation_you)));
        mSettings.add(new Setting(R.drawable.vector_purple_200_phone,getString(R.string.contact_us),getString(R.string.call_number)));
        mSettings.add(new Setting(R.drawable.vector_purple_200_video,getString(R.string.how_it_works),getString(R.string.tutorial_that_explains_you_from_a_z)));
        mSettings.add(new Setting(R.drawable.vector_purple_200_information,getString(R.string.app_information),getString(R.string.sub_app_information)));
        mSettingAdapter = new SettingAdapter(mSettings);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this.getApplicationContext()));
        mRecyclerView.setAdapter(mSettingAdapter);
    }
    public long percentage(long startDate , long endDate , long nowDate)
    {
        return (long) (((float)(nowDate - startDate)/(float) (endDate - startDate))*100);
    }
    public long getNowDate()
    {
        long currentTimeMillis = System.currentTimeMillis();
        long currentTimeSeconds = currentTimeMillis / 1000;

        // Affichez le temps actuel en secondes
        return currentTimeSeconds;
    }
    public long converterDate(String dateString)
    {
//        String dateString = "2024-02-13 12:30:00";
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        long dateInSeconds = 0;

        try {
            // Analyser la chaîne de caractères en objet Date
            Date date = dateFormat.parse(dateString);

            // Convertir la date en millisecondes
            long dateInMillis = date.getTime();

            // Convertir les millisecondes en secondes
            dateInSeconds = dateInMillis / 1000;

            // Afficher la date en secondes
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return dateInSeconds;
    }
    private class StructureSyn extends AsyncTask<String,Void,String> {
        @Override
        protected String doInBackground(String... params) {

            try {
                OkHttpClient client = new OkHttpClient();
                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("idUser",params[1])
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
                if (!jsonData.equals("RAS"))
                {
                    JSONArray jsonArray = null;
                    try {
                        jsonArray = new JSONArray(jsonData);
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                    for (int i=0;i<jsonArray.length();i++) {
                        try {
                            mStructures.add(new Structure(jsonArray.getJSONObject(i).getString("id"),jsonArray.getJSONObject(i).getString("logo"),jsonArray.getJSONObject(i).getString("nameStruct"),"RAS",true));
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    mRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                    mRecyclerView.setAdapter(StructAdapter);
                }
            }
            else {
                ArrayList<Connection> list = new ArrayList<>();
                list.add(new Connection(getString(R.string.no_connection_available),"CATEGORY_FRAGMENT",false));
                NoConnectionAdapter noConnectionAdapter = new NoConnectionAdapter(list);
                mStructureRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                mStructureRecyclerView.setAdapter(noConnectionAdapter);
            }
        }
    }
    private class StructureSyn2 extends AsyncTask<String,Void,String> {
        @Override
        protected String doInBackground(String... params) {

            try {
                OkHttpClient client = new OkHttpClient();
                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("idUser",params[1])
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
                        if(!isExistsS(mStructures,jsonArray.getJSONObject(i).getString("id")))
                            mStructures.add(new Structure(jsonArray.getJSONObject(i).getString("id"),jsonArray.getJSONObject(i).getString("logo"),jsonArray.getJSONObject(i).getString("nameStruct"),"RAS",false));
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                }
                mRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                mRecyclerView.setAdapter(StructAdapter);
            }
            else {
                ArrayList<Connection> list = new ArrayList<>();
                list.add(new Connection(getString(R.string.no_connection_available),"CATEGORY_FRAGMENT",false));
                NoConnectionAdapter noConnectionAdapter = new NoConnectionAdapter(list);
                mRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                mStructureRecyclerView.setAdapter(noConnectionAdapter);
            }
        }
    }

    private void searchAuthorOnline() {
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
                        AuthorSyn authorSyn = new AuthorSyn();
                        authorSyn.execute(getString(R.string.ip_server_android) + "Author.php", mSession.getIdNumber());
                    }catch (Exception e)
                    {
                        Log.e("errRankingFragment",e.getMessage());
                    }

                }
            }
        };
        registerReceiver(receiverNoConnectionAdapter, new IntentFilter("RANKING_FRAGMENT"));
        AuthorSyn authorSyn = new AuthorSyn();
        authorSyn.execute(getString(R.string.ip_server_android) + "Author.php", mSession.getIdNumber());
    }
    public boolean isExistsS(ArrayList<Structure> structures , String id)
    {
        for(int i=0 ; i<structures.size() ; i++)
        {
            if(structures.get(i).getId().equals(id))
                return true;
        }
        return false;
    }

    private class AuthorSyn extends AsyncTask<String,Void,String> {
        @Override
        protected String doInBackground(String... params) {

            try {
                OkHttpClient client = new OkHttpClient();
                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("idUser",params[1])
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
                        mAuthors.add(new Author(jsonArray.getJSONObject(i).getString("idAuthor"),jsonArray.getJSONObject(i).getString("name"),jsonArray.getJSONObject(i).getString("firstName"),jsonArray.getJSONObject(i).getString("profile")));
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                }
                mAuthorVerticaleAdapter = new AuthorVerticaleAdapter(mAuthors);
                mRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                mRecyclerView.setAdapter(mAuthorVerticaleAdapter);
            }
            else {
                ArrayList<Connection> list = new ArrayList<>();
                list.add(new Connection(getString(R.string.no_connection_available),"CATEGORY_FRAGMENT",false));
                NoConnectionAdapter noConnectionAdapter = new NoConnectionAdapter(list);
                mRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                mRecyclerView.setAdapter(noConnectionAdapter);
            }
        }
    }
    private RecyclerView mRecyclerView;
    private ArrayList<OnlineBook> mOnlineBooks;
    private NoConnectionAdapter mNoConnectionAdapter;
    private Session mSession;
    private EditText mSearchEditText;
    private ArrayList<OnlineBook> mFilteredOnlineBookList;
    private OnlineBookAdapter mOnlineBookAdapter;
    private ArrayList<ElectronicBook> mFilteredElectronicBooks;
    private ArrayList<ElectronicBook> mElectronicBooks;
    private ElectronicBookAdapter mElectronicBookAdapter;
    private ArrayList<AudioBook> mAudioBooks;
    private ArrayList<AudioBook> mFilteredAudioBook;
    private AudioBookAdapter mAudioBookAdapter;
    private ArrayList<LoandBook> mLoandBooks;
    private ArrayList<LoandBook> mFilteredLoandBooks;
    private LoandBookAdapter mLoandBookAdapter;
    private RecyclerView mStructureRecyclerView;
    private ArrayList<Structure> mStructures;
    private ArrayList<Structure> mFilterStructures;
    private StructureAdapter StructAdapter;
    private ArrayList<Category> mCategorys;
    private ArrayList<Category> mFilteredCategorys;
    private CategoryLocalAdapter mCategoryLocalAdapter;
    private ArrayList<Author> mAuthors;
    private ArrayList<Author> mFilteredAuthors;
    private AuthorLocalAdapter mAuthorLocalAdapter;
    private ArrayList<Notification> mNotifications;
    private ArrayList<Notification> mFilteredNotifications;
    private NotificationAdapter mNotificationAdapter;
    private ArrayList<Setting> mSettings;
    private ArrayList<Setting> mFilteredSettings;
    private SettingAdapter mSettingAdapter;
    private ImageView mBackImageView;
    private FabiolaBookAdapter mFabiolaBookAdapter;
    private AuthorVerticaleAdapter mAuthorVerticaleAdapter;
}