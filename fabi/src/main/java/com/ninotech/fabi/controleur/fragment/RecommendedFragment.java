package com.ninotech.fabi.controleur.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ninotech.fabi.controleur.adapter.OnlineBookAdapter;
import com.ninotech.fabi.controleur.adapter.NoConnectionAdapter;
import com.ninotech.fabi.model.data.OnlineBook;
import com.ninotech.fabi.controleur.animation.RoundedTransformation;
import com.ninotech.fabi.model.data.Connection;
import com.ninotech.fabi.model.table.Session;
import com.ninotech.fabi.R;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RecommendedFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recommended, container, false);
        Session session = new Session(getContext());
        mBookRecommendedRecyclerView = view.findViewById(R.id.recycler_view_ranking);
        mPub = view.findViewById(R.id.image_view_fragment_recommended_welcome);
        mOnlineBookList = new ArrayList<>();
        Picasso.get()
                .load(R.drawable.pub1)
                .transform(new RoundedTransformation(200,10))
                .resize(6200,3333)
                .into(mPub);
        BroadcastReceiver receiverNoConnectionAdapter = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if ("RECOMMENDED_FRAGMENT".equals(intent.getAction())) {
                    try {
                        ArrayList<Connection> list = new ArrayList<>();
                        list.add(new Connection(getString(R.string.wait),"RECOMMENDED_FRAGMENT",true));
                        NoConnectionAdapter noConnectionAdapter = new NoConnectionAdapter(list);
                        mBookRecommendedRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                        mBookRecommendedRecyclerView.setAdapter(noConnectionAdapter);
                        RecommendedSyn recommendedSyn = new RecommendedSyn();
                        recommendedSyn.execute(getString(R.string.ip_server_android) + "Recommended.php", session.getIdNumber());
                    }catch (Exception e)
                    {
                        Log.e("errRecommendedFragment",e.getMessage());
                    }

                }
            }
        };
        getContext().registerReceiver(receiverNoConnectionAdapter, new IntentFilter("RECOMMENDED_FRAGMENT")); /* Appel de la fonction cregisterReceviver */
        ArrayList<Connection> list = new ArrayList<>();
        list.add(new Connection(getString(R.string.wait),null,true));
       mNoConnectionAdapter = new NoConnectionAdapter(list);
        mBookRecommendedRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mBookRecommendedRecyclerView.setAdapter(mNoConnectionAdapter);
        RecommendedSyn recommendedSyn = new RecommendedSyn();
        recommendedSyn.execute(getString(R.string.ip_server_android) + "Recommended.php", session.getIdNumber());
        return view;
    }
    private class RecommendedSyn extends AsyncTask<String,Void,String> {
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
                mPub.setVisibility(View.VISIBLE);
                OnlineBookAdapter onlineBookAdapter = new OnlineBookAdapter(mOnlineBookList);
                mBookRecommendedRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                mBookRecommendedRecyclerView.setAdapter(onlineBookAdapter);
            }
            else
            {
                mPub.setVisibility(View.INVISIBLE);
                ArrayList<Connection> list = new ArrayList<>();
                list.add(new Connection(getString(R.string.no_connection_available),"RECOMMENDED_FRAGMENT",false));
                NoConnectionAdapter noConnectionAdapter = new NoConnectionAdapter(list);
                mBookRecommendedRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                mBookRecommendedRecyclerView.setAdapter(noConnectionAdapter);
            }
        }
    }
    private RecyclerView mBookRecommendedRecyclerView;
    private ArrayList<OnlineBook> mOnlineBookList;
    private ImageView mPub;
    private NoConnectionAdapter mNoConnectionAdapter;

}