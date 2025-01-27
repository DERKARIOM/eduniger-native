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
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ninotech.fabi.R;
import com.ninotech.fabi.controleur.adapter.NoConnectionAdapter;
import com.ninotech.fabi.controleur.adapter.StructureAdapter;
import com.ninotech.fabi.model.data.Connection;
import com.ninotech.fabi.model.data.Structure;
import com.ninotech.fabi.model.table.Session;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class StructureFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_category, container, false);
        mStructureRecyclerView = view.findViewById(R.id.recycler_view_fragment_category);
        Session session = new Session(getContext());
        mStructures = new ArrayList<>();
        ArrayList<Connection> list = new ArrayList<>();
        list.add(new Connection(getString(R.string.wait),null,true));
        mNoConnectionAdapter = new NoConnectionAdapter(list);
        mStructureRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mStructureRecyclerView.setAdapter(mNoConnectionAdapter);
        StructAdapter = new StructureAdapter(mStructures);
        BroadcastReceiver receiverNoConnectionAdapter = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if ("CATEGORY_FRAGMENT".equals(intent.getAction())) {
                    try {
                        ArrayList<Connection> list = new ArrayList<>();
                        list.add(new Connection(getString(R.string.wait),"CATEGORY_FRAGMENT",true));
                        NoConnectionAdapter noConnectionAdapter = new NoConnectionAdapter(list);
                        mStructureRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                        mStructureRecyclerView.setAdapter(noConnectionAdapter);
                        StructureSyn structureSyn = new StructureSyn();
                        structureSyn.execute(getString(R.string.ip_server_android) + "Structure.php", session.getIdNumber());
                        StructureSyn2 structureSyn2 = new StructureSyn2();
                        structureSyn2.execute(getString(R.string.ip_server_android) + "Structure2.php", session.getIdNumber());
                    }catch (Exception e)
                    {
                        Log.e("errCategoryFragment",e.getMessage());
                    }

                }
            }
        };
        getContext().registerReceiver(receiverNoConnectionAdapter, new IntentFilter("CATEGORY_FRAGMENT"));
        StructureSyn structureSyn = new StructureSyn();
        structureSyn.execute(getString(R.string.ip_server_android) + "Structure.php", session.getIdNumber());
        StructureSyn2 structureSyn2 = new StructureSyn2();
        structureSyn2.execute(getString(R.string.ip_server_android) + "Structure2.php", session.getIdNumber());
        return view;
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
                    mStructureRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                    mStructureRecyclerView.setAdapter(StructAdapter);
                }
            }
            else {
                ArrayList<Connection> list = new ArrayList<>();
                list.add(new Connection(getString(R.string.no_connection_available),"CATEGORY_FRAGMENT",false));
                NoConnectionAdapter noConnectionAdapter = new NoConnectionAdapter(list);
                mStructureRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
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
                        if(!isExistsS(mStructures,jsonArray.getJSONObject(i).getString("id")))
                            mStructures.add(new Structure(jsonArray.getJSONObject(i).getString("id"),jsonArray.getJSONObject(i).getString("logo"),jsonArray.getJSONObject(i).getString("nameStruct"),"RAS",false));
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                }
                mStructureRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                mStructureRecyclerView.setAdapter(StructAdapter);
            }
            else {
                ArrayList<Connection> list = new ArrayList<>();
                list.add(new Connection(getString(R.string.no_connection_available),"CATEGORY_FRAGMENT",false));
                NoConnectionAdapter noConnectionAdapter = new NoConnectionAdapter(list);
                mStructureRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                mStructureRecyclerView.setAdapter(noConnectionAdapter);
            }
        }
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
    private RecyclerView mStructureRecyclerView;
    private ArrayList<Structure> mStructures;
    private NoConnectionAdapter mNoConnectionAdapter;
    private StructureAdapter StructAdapter;
}