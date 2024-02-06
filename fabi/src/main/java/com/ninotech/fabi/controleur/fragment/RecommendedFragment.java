package com.ninotech.fabi.controleur.fragment;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.ninotech.fabi.controleur.adapter.BookAdapter;
import com.ninotech.fabi.model.data.Book;
import com.ninotech.fabi.controleur.animation.RoundedTransformation;
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
        mPub = view.findViewById(R.id.img_welcom);
        mBookList = new ArrayList<>();
        ArrayList<String> monPubList = null;
        monPubList = new ArrayList<>();
        monPubList.add("pub1.jpg");
        monPubList.add("pub2.jpg");
        Picasso.with(view.getContext())
                .load(getString(R.string.ip_server) + "pub/pub1.jpg")
                .transform(new RoundedTransformation(200,10))
                .resize(6200,3333)
                .into(mPub);
        Handler handlerOut = new Handler();
        Handler handlerIn = new Handler();
        int delayMillis = 5000; // 5 secondes
        Runnable runnableOut = new Runnable() {
            @Override
            public void run() {
                YoYo.with(Techniques.SlideOutLeft)
                        .duration(1000)
                        .onEnd(animator -> {
                            // Changez la source de l'image après l'animation
//                            Picasso.with(view.getContext())
//                                    .load("http://192.168.43.1:2222/fabi/pub/pub2.jpg")
//                                    .transform(new RoundedTransformation(200,10))
//                                    .resize(6200,3333)
//                                    .into(mPub);
                            //currentIndex = (currentIndex + 1) % imagesList.size();
                            // Répétez l'animation après un délai
                            handlerOut.postDelayed(this, delayMillis);
                        })
                        .playOn(mPub);
            }
        };
        Runnable runnableIn = new Runnable() {
            @Override
            public void run() {
                // Utilisez YoYo pour animer le changement d'image
                YoYo.with(Techniques.SlideInRight)
                        .duration(1000)
                        .onEnd(animator -> {
                            // Changez la source de l'image après l'animation
//                            Picasso.with(view.getContext())
//                                    .load("http://192.168.43.1:2222/fabi/pub/pub2.jpg")
//                                    .transform(new RoundedTransformation(200,10))
//                                    .resize(6200,3333)
//                                    .into(mPub);
                            //currentIndex = (currentIndex + 1) % imagesList.size();
                            // Répétez l'animation après un délai
                            handlerIn.postDelayed(this, delayMillis);
                        })
                        .playOn(mPub);
            }
        };
        handlerOut.postDelayed(runnableOut,delayMillis);
        handlerIn.postDelayed(runnableIn,delayMillis+1000);
        RecommendedSyn recommendedSyn = new RecommendedSyn();
        recommendedSyn.execute(getString(R.string.ip_server_android) + "Recommended.php", session.getIdNumber());
        mPub.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                YoYo.with(Techniques.SlideOutLeft).duration(700).repeat(0).playOn(mPub);
            }
        });
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
                        ArrayList<String> category = new ArrayList<>();
                        category.add(jsonArray.getJSONObject(i).getString("categoryTitle"));
                        mBookList.add(new Book(jsonArray.getJSONObject(i).getString("idBook"),jsonArray.getJSONObject(i).getString("blanket"),jsonArray.getJSONObject(i).getString("bookTitle"),category,jsonArray.getJSONObject(i).getString("isPhysic"),jsonArray.getJSONObject(i).getString("electronic"),jsonArray.getJSONObject(i).getString("isAudio"),Integer.parseInt(jsonArray.getJSONObject(i).getString("numberLike")),0));
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                }
                BookAdapter bookAdapter = new BookAdapter(mBookList);
                mBookRecommendedRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                mBookRecommendedRecyclerView.setAdapter(bookAdapter);
            }
        }
    }
    private RecyclerView mBookRecommendedRecyclerView;
    private ArrayList<Book> mBookList;
    private ImageView mPub;
}