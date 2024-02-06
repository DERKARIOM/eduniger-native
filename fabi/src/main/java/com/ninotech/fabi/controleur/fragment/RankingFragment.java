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

import com.ninotech.fabi.controleur.adapter.BookAdapter;
import com.ninotech.fabi.model.data.Book;
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

public class RankingFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
       View view = inflater.inflate(R.layout.fragment_classement, container, false);
        Session session = new Session(getContext());
       mBookRecyclerView = view.findViewById(R.id.recycler_view_ranking);
        mBookList = new ArrayList<>();
        RankingSyn rankingSyn = new RankingSyn();
        rankingSyn.execute(getString(R.string.ip_server_android) + "Ranking.php", session.getIdNumber());
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
                        mBookList.add(new Book(jsonArray.getJSONObject(i).getString("idBook"),jsonArray.getJSONObject(i).getString("blanket"),jsonArray.getJSONObject(i).getString("bookTitle"),category,jsonArray.getJSONObject(i).getString("isPhysic"),jsonArray.getJSONObject(i).getString("electronic"),jsonArray.getJSONObject(i).getString("isAudio"),0,0));
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                }
                BookAdapter bookAdapter = new BookAdapter(mBookList);
                mBookRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                mBookRecyclerView.setAdapter(bookAdapter);
            }
        }
    }
    private RecyclerView mBookRecyclerView;
    private ArrayList<Book> mBookList;
}