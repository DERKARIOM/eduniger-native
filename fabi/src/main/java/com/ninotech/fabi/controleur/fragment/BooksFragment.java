package com.ninotech.fabi.controleur.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ninotech.fabi.controleur.adapter.OnlineBookAdapter;
import com.ninotech.fabi.controleur.adapter.NoConnectionAdapter;
import com.ninotech.fabi.model.data.OnlineBook;
import com.ninotech.fabi.model.data.Connection;
import com.ninotech.fabi.model.data.Server;
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

public class BooksFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
       View view = inflater.inflate(R.layout.fragment_books, container, false);
        Session session = new Session(getContext());
       mBookRecyclerView = view.findViewById(R.id.recycler_view_ranking);
       mWaitRecyclerView = view.findViewById(R.id.recycler_view_fragment_books_wait);
        mOnlineBookList = new ArrayList<>();
        ArrayList<Connection> list = new ArrayList<>();
        list.add(new Connection(getString(R.string.wait),null,true));
        mNoConnectionAdapter = new NoConnectionAdapter(list);
        mWaitRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mWaitRecyclerView.setAdapter(mNoConnectionAdapter);
        BroadcastReceiver receiverNoConnectionAdapter = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if ("RANKING_FRAGMENT".equals(intent.getAction())) {
                    try {
                        ArrayList<Connection> list = new ArrayList<>();
                        list.add(new Connection(getString(R.string.wait),"RANKING_FRAGMENT",true));
                        NoConnectionAdapter noConnectionAdapter = new NoConnectionAdapter(list);
                        mWaitRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                        mWaitRecyclerView.setAdapter(noConnectionAdapter);
                        RankingSyn rankingSyn = new RankingSyn();
                        rankingSyn.execute(Server.getIpServerAndroid(getContext()) + "Ranking.php", session.getIdNumber());
                    }catch (Exception e)
                    {
                        Log.e("errRankingFragment",e.getMessage());
                    }

                }
            }
        };
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getContext().registerReceiver(receiverNoConnectionAdapter, new IntentFilter("RANKING_FRAGMENT"),Context.RECEIVER_EXPORTED);
        }
        RankingSyn rankingSyn = new RankingSyn();
        rankingSyn.execute(Server.getIpServerAndroid(getContext()) + "Ranking.php", session.getIdNumber());
        return view;
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
                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
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
                mWaitRecyclerView.setVisibility(View.GONE);
                mBookRecyclerView.setVisibility(View.VISIBLE);
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
                            mOnlineBookList.add(new OnlineBook(jsonArray.getJSONObject(i).getString("idBook"),jsonArray.getJSONObject(i).getString("blanket"),jsonArray.getJSONObject(i).getString("bookTitle"),jsonArray.getJSONObject(i).getString("nameStruct") + " : " +jsonArray.getJSONObject(i).getString("categoryTitle"),jsonArray.getJSONObject(i).getString("isPhysic"),jsonArray.getJSONObject(i).getString("electronic"),jsonArray.getJSONObject(i).getString("isAudio"),Integer.parseInt(jsonArray.getJSONObject(i).getString("numberLike")),Integer.parseInt(jsonArray.getJSONObject(i).getString("numberLike"))));
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    OnlineBookAdapter onlineBookAdapter = new OnlineBookAdapter(mOnlineBookList);
                    mBookRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                    mBookRecyclerView.setAdapter(onlineBookAdapter);
                }
            }
            else
            {
                ArrayList<Connection> list = new ArrayList<>();
                list.add(new Connection(getString(R.string.no_connection_available),"RANKING_FRAGMENT",false));
                NoConnectionAdapter noConnectionAdapter = new NoConnectionAdapter(list);
                mWaitRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                mWaitRecyclerView.setAdapter(noConnectionAdapter);
            }
        }
    }
    private RecyclerView mBookRecyclerView;
    private RecyclerView mWaitRecyclerView;
    private ArrayList<OnlineBook> mOnlineBookList;
    private NoConnectionAdapter mNoConnectionAdapter;
}