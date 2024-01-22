package com.ninotech.fabi.controleur.fragment;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ninotech.fabi.controleur.adapter.CategorieAdapter;
import com.ninotech.fabi.model.data.Categorie;
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

public class CategorieFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_categorie, container, false);
        mRecyclerView = view.findViewById(R.id.recylerCategorie);
        mSession = new Session(getContext());
        mList = new ArrayList<>();
        Http http = new Http();
        http.execute("http://192.168.43.1:2222/fabi/android/categorie.php");
        return view;
    }

    private class Http extends AsyncTask<String,Void,String> {
        @Override
        protected String doInBackground(String... params) {

            try {
                OkHttpClient client = new OkHttpClient();
                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("matricule",mSession.getIdNumber())
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
            //Toast.makeText(NotificationService.this, response, Toast.LENGTH_SHORT).show();
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
                        mList.add(new Categorie(jsonArray.getJSONObject(i).getString("nomCouverture"),jsonArray.getJSONObject(i).getString("nomCat")));
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                }
                mCategorieAdapter = new CategorieAdapter(mList);
                mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                mRecyclerView.setAdapter(mCategorieAdapter);

                //Toast.makeText(getContext(), jsonData, Toast.LENGTH_SHORT).show();
            }
            else
            {

            }

        }
    }
    private RecyclerView mRecyclerView;
    private CategorieAdapter mCategorieAdapter;
    private ArrayList<Categorie> mList;
    private Session mSession;
}