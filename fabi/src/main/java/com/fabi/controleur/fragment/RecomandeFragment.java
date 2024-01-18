package com.fabi.controleur.fragment;

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
import com.fabi.controleur.adapter.ClassementAdapter;
import com.fabi.model.data.Livres;
import com.fabi.controleur.animation.RoundedTransformation;
import com.fabi.model.table.Session;
import com.example.fabi.R;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RecomandeFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recomande, container, false);
        mSession = new Session(getContext());
        mRecyclerView = view.findViewById(R.id.recylerClassement);
        mPub = view.findViewById(R.id.img_welcom);
        mList = new ArrayList<>();
        mMonPub = new ArrayList<>();
        mMonPub.add("pub1.jpg");
        mMonPub.add("pub2.jpg");
        Picasso.with(view.getContext())
                .load("http://192.168.43.1:2222/fabi/pub/pub1.jpg")
                .transform(new RoundedTransformation(200,10))
                .resize(6200,3333)
                .into(mPub);
        Handler handlerOut = new Handler();
        Handler handlerIn = new Handler();
        int delayMillis = 5000; // 5 secondes
        int currentIndex = 0;

        Runnable runnableOut = new Runnable() {
            @Override
            public void run() {
                // Utilisez YoYo pour animer le changement d'image


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
        Http http = new Http();
        http.execute("http://192.168.43.1:2222/fabi/android/recomande.php");
        mPub.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                YoYo.with(Techniques.SlideOutLeft).duration(700).repeat(0).playOn(mPub);
            }
        });
        return view;
    }
    private class Http extends AsyncTask<String,Void,String> {
        @Override
        protected String doInBackground(String... params) {

            try {
                OkHttpClient client = new OkHttpClient();
                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("matricule",mSession.getMatricule())
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
                        mList.add(new Livres(jsonArray.getJSONObject(i).getString("idLivre"),jsonArray.getJSONObject(i).getString("couverture"),jsonArray.getJSONObject(i).getString("titreLivre"),jsonArray.getJSONObject(i).getString("nomCat"),jsonArray.getJSONObject(i).getString("estPhysique"),jsonArray.getJSONObject(i).getString("documentElec"),jsonArray.getJSONObject(i).getString("estAudio"),jsonArray.getJSONObject(i).getString("nbrLikes"),"0"));
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                }
                mClassementAdapter = new ClassementAdapter(mList);
                mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                mRecyclerView.setAdapter(mClassementAdapter);
                //Toast.makeText(getContext(), jsonData, Toast.LENGTH_SHORT).show();
            }
            else
            {

            }

        }
    }
    private RecyclerView mRecyclerView;
    private ClassementAdapter mClassementAdapter;
    private ArrayList<Livres> mList;
    private Session mSession;
    private ImageView mPub;
    private List<String> mMonPub;
}