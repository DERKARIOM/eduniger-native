package com.ninotech.fabi.controleur.activity;

import android.app.UiModeManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ninotech.fabi.controleur.adapter.OnlineBookAdapter;
import com.ninotech.fabi.controleur.adapter.NoConnectionAdapter;
import com.ninotech.fabi.controleur.adapter.VoidContainerAdapter;
import com.ninotech.fabi.model.data.OnlineBook;
import com.ninotech.fabi.model.data.Connection;
import com.ninotech.fabi.model.data.Server;
import com.ninotech.fabi.model.data.Themes;
import com.ninotech.fabi.model.data.VoidContainer;
import com.ninotech.fabi.model.table.Session;
import com.ninotech.fabi.R;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;

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
        mSession = new Session(this);
        mRecyclerView = findViewById(R.id.recylerCategorie2);
        mWaitRecyclerView = findViewById(R.id.recycler_view_activity_category_wait);
        mList = new ArrayList<>();
        Intent intent = getIntent();
        ActionBar ab = getSupportActionBar();
        assert ab != null;
        ab.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        ab.setCustomView(R.layout.custom_action_bar);
        ab.setDisplayHomeAsUpEnabled(true);
        TextView actionBarTitle = ab.getCustomView().findViewById(R.id.action_bar_title);

        UiModeManager uiModeManager = null;
        switch (Themes.getName(getApplicationContext()))
        {
            case "system":
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    uiModeManager = (UiModeManager) getSystemService(Context.UI_MODE_SERVICE);
                }
                int currentMode = uiModeManager.getNightMode();
                if (currentMode == UiModeManager.MODE_NIGHT_NO) {
                    // code mode jour
                    ab.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.white)));
                    ab.setHomeAsUpIndicator(R.drawable.vector_back);
                }
                else
                {
                    // code mode nuit
                    ab.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.black)));
                    ab.setHomeAsUpIndicator(R.drawable.vector_white_sombre_back);
                    actionBarTitle.setTextColor(getResources().getColor(R.color.whiteSombre));
                }
                break;
            case "notNight":
                // code mode jour
                ab.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.white)));
                ab.setHomeAsUpIndicator(R.drawable.vector_back);
                break;
            case "night":
                ab.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.black)));
                ab.setHomeAsUpIndicator(R.drawable.vector_white_sombre_back);
                actionBarTitle.setTextColor(getResources().getColor(R.color.whiteSombre));
                break;
        }
        mCategorie=null;
        mNameStruct=null;
        if (intent != null && intent.hasExtra("intent_adapter_category_title")) {
            mCategorie = intent.getStringExtra("intent_adapter_category_title");
            if (intent.hasExtra("intent_adapter_category_name_struct")) {
                mNameStruct = intent.getStringExtra("intent_adapter_category_name_struct");
            }
        }
        actionBarTitle.setText(mCategorie);
        ArrayList<Connection> list = new ArrayList<>();
        list.add(new Connection(getString(R.string.wait),null,true));
        mNoConnectionAdapter = new NoConnectionAdapter(list);
        mWaitRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        mWaitRecyclerView.setAdapter(mNoConnectionAdapter);
        BroadcastReceiver receiverNoConnectionAdapter = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if ("CATEGORY_ACTIVITY".equals(intent.getAction())) {
                    try {
                        ArrayList<Connection> list = new ArrayList<>();
                        list.add(new Connection(getString(R.string.wait),"CATEGORY_ACTIVITY",true));
                        NoConnectionAdapter noConnectionAdapter = new NoConnectionAdapter(list);
                        mWaitRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                        mWaitRecyclerView.setAdapter(noConnectionAdapter);
                        CategoryInSyn categoryInSyn = new CategoryInSyn();
                        categoryInSyn.execute(Server.getIpServerAndroid(getApplicationContext()) + "CategoryIn.php",mSession.getIdNumber(),mCategorie);
                    }catch (Exception e)
                    {
                        Log.e("errRankingFragment",e.getMessage());
                    }

                }
            }
        };
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            registerReceiver(receiverNoConnectionAdapter, new IntentFilter("CATEGORY_ACTIVITY"),Context.RECEIVER_EXPORTED);
        }
        if (mNameStruct == null)
        {
            CategoryInSyn categoryInSyn = new CategoryInSyn();
            categoryInSyn.execute(Server.getIpServerAndroid(getApplicationContext()) + "CategoryIn.php",mSession.getIdNumber(),mCategorie);
        }
        else
        {
            StructCategoryInSyn structCategoryInSyn = new StructCategoryInSyn();
            structCategoryInSyn.execute(Server.getIpServerAndroid(getApplicationContext()) + "StructCategoryIn.php",mSession.getIdNumber(),mCategorie,mNameStruct);
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
                mWaitRecyclerView.setVisibility(View.GONE);
                mRecyclerView.setVisibility(View.VISIBLE);
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
                            mList.add(new OnlineBook(jsonArray.getJSONObject(i).getString("idBook"),jsonArray.getJSONObject(i).getString("blanket"),jsonArray.getJSONObject(i).getString("bookTitle"),jsonArray.getJSONObject(i).getString("nameStruct") + " : " + jsonArray.getJSONObject(i).getString("categoryTitle"),jsonArray.getJSONObject(i).getString("isPhysic"),jsonArray.getJSONObject(i).getString("electronic"),jsonArray.getJSONObject(i).getString("isAudio"),Integer.parseInt(jsonArray.getJSONObject(i).getString("numberLike")),Integer.parseInt(jsonArray.getJSONObject(i).getString("numberView"))));
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    mOnlineBookAdapter = new OnlineBookAdapter(mList);
                    mRecyclerView.setLayoutManager(new LinearLayoutManager(CategoryActivity.this));
                    mRecyclerView.setAdapter(mOnlineBookAdapter);
                }
                else
                {
                    voidContainer(R.drawable.img_telecharge_local,"Aucun livre de categorie " + mCategorie);
                }
                //Toast.makeText(getContext(), jsonData, Toast.LENGTH_SHORT).show();
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

    private class StructCategoryInSyn extends AsyncTask<String,Void,String> {
        @Override
        protected String doInBackground(String... params) {

            try {
                OkHttpClient client = new OkHttpClient();
                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("idNumber",params[1])
                        .addFormDataPart("categoryTitle",params[2])
                        .addFormDataPart("structName",params[3])
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
                mWaitRecyclerView.setVisibility(View.GONE);
                mRecyclerView.setVisibility(View.VISIBLE);
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
                            mList.add(new OnlineBook(jsonArray.getJSONObject(i).getString("idBook"),jsonArray.getJSONObject(i).getString("blanket"),jsonArray.getJSONObject(i).getString("bookTitle"),jsonArray.getJSONObject(i).getString("nameStruct") + " : " + jsonArray.getJSONObject(i).getString("categoryTitle"),jsonArray.getJSONObject(i).getString("isPhysic"),jsonArray.getJSONObject(i).getString("electronic"),jsonArray.getJSONObject(i).getString("isAudio"),Integer.parseInt(jsonArray.getJSONObject(i).getString("numberLike")),Integer.parseInt(jsonArray.getJSONObject(i).getString("numberView"))));
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    mOnlineBookAdapter = new OnlineBookAdapter(mList);
                    mRecyclerView.setLayoutManager(new LinearLayoutManager(CategoryActivity.this));
                    mRecyclerView.setAdapter(mOnlineBookAdapter);
                }
                else
                {
                    voidContainer(R.drawable.img_telecharge_local,"Aucun livre de categorie " + mCategorie);
                }
                //Toast.makeText(getContext(), jsonData, Toast.LENGTH_SHORT).show();
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
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id)
        {
            case android.R.id.home:
                onBackPressed(); // Appel de la méthode onBackPressed() pour simuler le comportement du bouton retour
                return true;
            case R.id.item_menu_search:
                Intent searchIntent = new Intent(CategoryActivity.this,SearchActivity.class);
                searchIntent.putExtra("search_key","ONLINE_BOOK");
                searchIntent.putExtra("online_book_key","CATEGORY_ACTIVITY");
                searchIntent.putExtra("title_category",mCategorie);
                startActivity(searchIntent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    public void voidContainer(int image , String message)
    {
        ArrayList<VoidContainer> voidContainers = new ArrayList<>();
        voidContainers.add(new VoidContainer(image,message));
        VoidContainerAdapter voidContainerAdapter = new VoidContainerAdapter(voidContainers);
        mWaitRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mWaitRecyclerView.setAdapter(voidContainerAdapter);
    }
    private RecyclerView mRecyclerView;
    private OnlineBookAdapter mOnlineBookAdapter;
    private ArrayList<OnlineBook> mList;
    private Session mSession;
    private String mCategorie;
    private NoConnectionAdapter mNoConnectionAdapter;
    private RecyclerView mWaitRecyclerView;
    private String mNameStruct;
}