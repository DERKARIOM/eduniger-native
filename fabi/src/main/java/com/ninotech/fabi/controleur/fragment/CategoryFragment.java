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

import com.ninotech.fabi.controleur.adapter.CategoryAdapter;
import com.ninotech.fabi.model.data.Category;
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

public class CategoryFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_category, container, false);
        mCategoryRecyclerView = view.findViewById(R.id.recylerCategorie);
        Session session = new Session(getContext());
        mCategoryList = new ArrayList<>();
        CategorySyn categorySyn = new CategorySyn();
        categorySyn.execute(getString(R.string.ip_server_android) + "Category.php", session.getIdNumber());
        return view;
    }

    private class CategorySyn extends AsyncTask<String,Void,String> {
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
                        mCategoryList.add(new Category(jsonArray.getJSONObject(i).getString("blanket"),jsonArray.getJSONObject(i).getString("title")));
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                }
                CategoryAdapter categoryAdapter = new CategoryAdapter(mCategoryList);
                mCategoryRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                mCategoryRecyclerView.setAdapter(categoryAdapter);
            }
        }
    }
    private RecyclerView mCategoryRecyclerView;
    private ArrayList<Category> mCategoryList;
}