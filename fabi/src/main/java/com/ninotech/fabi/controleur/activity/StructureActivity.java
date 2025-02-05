package com.ninotech.fabi.controleur.activity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ninotech.fabi.R;
import com.ninotech.fabi.controleur.adapter.HorizontaleAdapter;
import com.ninotech.fabi.controleur.adapter.NoConnectionAdapter;
import com.ninotech.fabi.controleur.animation.RoundedTransformation;
import com.ninotech.fabi.controleur.fragment.HomeFragment;
import com.ninotech.fabi.model.data.Connection;
import com.ninotech.fabi.model.data.OnlineBook;
import com.ninotech.fabi.model.table.Session;
import com.squareup.picasso.Picasso;

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

public class StructureActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_structure);
        Objects.requireNonNull(getSupportActionBar()).hide();
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        mWelcomeImageView = findViewById(R.id.image_view_structure_activity_welcome);
        mProfileImageView = findViewById(R.id.image_view_structure_activity_profile);
        mBookRecommendedRecyclerView = findViewById(R.id.recycler_view_activity_structure_books);
        mSession = new Session(getApplicationContext());
        mOnlineBookList = new ArrayList<>();
        Picasso.get()
                .load(getString(R.string.ip_server) + "ressources/baniere/openlab.png")
                .transform(new RoundedTransformation(200,10))
                .resize(6200,2222)
                .into(mWelcomeImageView);
        mWelcomeImageView.setVisibility(View.VISIBLE);
        Picasso.get()
                .load(getResources().getString(R.string.ip_server) + "ressources/cover/openlab.png")
                .placeholder(R.drawable.img_default_book)
                .error(R.drawable.img_default_book)
                .transform(new RoundedTransformation(1000,4))
                .resize(284,284)
                .into(mProfileImageView);
        RecommendedSyn recommendedSyn = new RecommendedSyn();
        recommendedSyn.execute(getString(R.string.ip_server_android) + "Recommended.php", mSession.getIdNumber(),getString(R.string.app_version));
    }
    private class RecommendedSyn extends AsyncTask<String,Void,String> {
        @Override
        protected String doInBackground(String... params) {

            try {
                OkHttpClient client = new OkHttpClient();
                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("idNumber", params[1])
                        .addFormDataPart("version", params[2])
                        .build();
                Request request = new Request.Builder()
                        .url(params[0])
                        .post(requestBody)
                        .build();
                try {
                    Response response = client.newCall(request).execute();
                    assert response.body() != null;
                    return response.body().string();
                } catch (IOException e) {
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                }

            } catch (Exception e) {
                return null;
            }
            return null;
        }

        @Override
        protected void onPostExecute(String jsonData) {
            if (jsonData != null) {
                if (!jsonData.equals("expiresVersion")) {
                    JSONArray jsonArray = null;
                    try {
                        jsonArray = new JSONArray(jsonData);
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                    for (int i = 0; i < jsonArray.length(); i++) {
                        try {
                            mOnlineBookList.add(new OnlineBook(jsonArray.getJSONObject(i).getString("idBook"), jsonArray.getJSONObject(i).getString("blanket"), jsonArray.getJSONObject(i).getString("bookTitle"), jsonArray.getJSONObject(i).getString("categoryTitle"), jsonArray.getJSONObject(i).getString("isPhysic"), jsonArray.getJSONObject(i).getString("electronic"), jsonArray.getJSONObject(i).getString("isAudio"), Integer.parseInt(jsonArray.getJSONObject(i).getString("numberLike")), Integer.parseInt(jsonArray.getJSONObject(i).getString("numberLike"))));
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    HorizontaleAdapter horizontaleAdapter = new HorizontaleAdapter(mOnlineBookList);
                    mBookRecommendedRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false));
                    mBookRecommendedRecyclerView.setAdapter(horizontaleAdapter);
                }
            } else {
                ArrayList<Connection> list = new ArrayList<>();
                list.add(new Connection(getString(R.string.no_connection_available), "RECOMMENDED_FRAGMENT", false));
                NoConnectionAdapter noConnectionAdapter = new NoConnectionAdapter(list);
                mBookRecommendedRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                mBookRecommendedRecyclerView.setAdapter(noConnectionAdapter);
            }
        }
    }
    private ImageView mWelcomeImageView;
    private ImageView mProfileImageView;
    private RecyclerView mBookRecommendedRecyclerView;
    private ArrayList<OnlineBook> mOnlineBookList;
    private Session mSession;
}