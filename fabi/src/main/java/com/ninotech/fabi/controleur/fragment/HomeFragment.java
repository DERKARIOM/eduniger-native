package com.ninotech.fabi.controleur.fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ninotech.fabi.controleur.activity.LoginActivity;
import com.ninotech.fabi.controleur.activity.SearchActivity;
import com.ninotech.fabi.controleur.adapter.AuthorHorizontaleAdapter;
import com.ninotech.fabi.controleur.adapter.HorizontaleAdapter;
import com.ninotech.fabi.controleur.adapter.NoConnectionAdapter;
import com.ninotech.fabi.controleur.adapter.StructureAdapter;
import com.ninotech.fabi.controleur.dialog.UpdateDialog;
import com.ninotech.fabi.model.data.Account;
import com.ninotech.fabi.model.data.Author;
import com.ninotech.fabi.model.data.OnlineBook;
import com.ninotech.fabi.controleur.animation.RoundedTransformation;
import com.ninotech.fabi.model.data.Connection;
import com.ninotech.fabi.model.data.Structure;
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

public class HomeFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        Session session = new Session(getContext());
        mBookRecommendedRecyclerView = view.findViewById(R.id.recycler_view_ranking);
        mWelcomeImageView = view.findViewById(R.id.image_view_fragment_recommended_welcome);
        mBookMoreTextView = view.findViewById(R.id.text_view_recommended_more);
        mStructMoreTextView = view.findViewById(R.id.text_view_recommended_more_structure);
        mAuthorMoreTextView = view.findViewById(R.id.text_view_recommended_more_author);
        mStructureRecyclerView = view.findViewById(R.id.recycler_view_fragment_recommended_structure);
        mAuthorRecyclerView = view.findViewById(R.id.recycler_view_author);
        mWaitRecyclerView = view.findViewById(R.id.recycler_view_fragment_recommended_wait);
        mNestedScrollView = view.findViewById(R.id.nested_scroll_view_fragment_home);
        mOnlineBookList = new ArrayList<>();
        mStructures = new ArrayList<>();
        mAuthorArrayList = new ArrayList<>();
        mAccount = new Account();
        StructAdapter = new StructureAdapter(mStructures);
        BroadcastReceiver receiverNoConnectionAdapter = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if ("HOME_FRAGMENT".equals(intent.getAction())) {
                    try {
                        ArrayList<Connection> list = new ArrayList<>();
                        list.add(new Connection(getString(R.string.wait), "HOME_FRAGMENT", true));
                        NoConnectionAdapter noConnectionAdapter = new NoConnectionAdapter(list);
                        mWaitRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                        mWaitRecyclerView.setAdapter(noConnectionAdapter);
                        RecommendedSyn recommendedSyn = new RecommendedSyn();
                        recommendedSyn.execute(getString(R.string.ip_server_android) + "Recommended.php", session.getIdNumber(),getString(R.string.app_version));
                        StructureSyn structureSyn = new StructureSyn();
                        structureSyn.execute(getString(R.string.ip_server_android) + "Structure.php", session.getIdNumber());
                        StructureSyn2 structureSyn2 = new StructureSyn2();
                        structureSyn2.execute(getString(R.string.ip_server_android) + "Structure2.php", session.getIdNumber());
                        AuthorSyn authorSyn = new AuthorSyn();
                        authorSyn.execute(getString(R.string.ip_server_android) + "AuthorTop.php", session.getIdNumber());
                    } catch (Exception e) {
                        Log.e("errRecommendedFragment", e.getMessage());
                    }

                }
            }
        };
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getContext().registerReceiver(receiverNoConnectionAdapter, new IntentFilter("HOME_FRAGMENT"),Context.RECEIVER_EXPORTED); /* Appel de la fonction cregisterReceviver */
        }

        mBookMoreTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent searchIntent = new Intent(getContext(), SearchActivity.class);
                searchIntent.putExtra("search_key", "ONLINE_BOOK");
                searchIntent.putExtra("online_book_key", "MAIN_ACTIVITY");
                startActivity(searchIntent);
            }
        });

        mStructMoreTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent searchIntent = new Intent(getContext(), SearchActivity.class);
                searchIntent.putExtra("search_key", "STRUCTURE");
                searchIntent.putExtra("online_book_key", "MAIN_ACTIVITY");
                startActivity(searchIntent);
            }
        });

        mAuthorMoreTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent searchIntent = new Intent(getContext(), SearchActivity.class);
                searchIntent.putExtra("search_key", "AUTHOR_ONLINE");
                searchIntent.putExtra("online_book_key", "MAIN_ACTIVITY");
                startActivity(searchIntent);
            }
        });
        ArrayList<Connection> list = new ArrayList<>();
        list.add(new Connection(getString(R.string.wait), null, true));
        mNoConnectionAdapter = new NoConnectionAdapter(list);
        mWaitRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mWaitRecyclerView.setAdapter(mNoConnectionAdapter);
        if (mAccount.isSession(getContext()))
        {
            RecommendedSyn recommendedSyn = new RecommendedSyn();
            recommendedSyn.execute(getString(R.string.ip_server_android) + "Recommended.php", session.getIdNumber(),getString(R.string.app_version));
            StructureSyn structureSyn = new StructureSyn();
            structureSyn.execute(getString(R.string.ip_server_android) + "Structure.php", session.getIdNumber());
            StructureSyn2 structureSyn2 = new StructureSyn2();
            structureSyn2.execute(getString(R.string.ip_server_android) + "Structure2.php", session.getIdNumber());
            AuthorSyn authorSyn = new AuthorSyn();
            authorSyn.execute(getString(R.string.ip_server_android) + "AuthorTop.php", session.getIdNumber());
        }
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
                        .addFormDataPart("version",params[2])
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
                if (isAdded())
                {
                    Picasso.get()
                            .load(getString(R.string.ip_server) + "ressources/pub/p4.png")
                            .transform(new RoundedTransformation(200,10))
                            .resize(6200,3333)
                            .placeholder(R.drawable.img_wait_pub)
                            .into(mWelcomeImageView);
                    mWaitRecyclerView.setVisibility(View.GONE);
                    mNestedScrollView.setVisibility(View.VISIBLE);
                }
                if(!jsonData.equals("expiresVersion"))
                {
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
                                mOnlineBookList.add(new OnlineBook(jsonArray.getJSONObject(i).getString("idBook"),jsonArray.getJSONObject(i).getString("blanket"),jsonArray.getJSONObject(i).getString("bookTitle"),jsonArray.getJSONObject(i).getString("categoryTitle"),jsonArray.getJSONObject(i).getString("isPhysic"),jsonArray.getJSONObject(i).getString("electronic"),jsonArray.getJSONObject(i).getString("isAudio"),Integer.parseInt(jsonArray.getJSONObject(i).getString("numberLike")),Integer.parseInt(jsonArray.getJSONObject(i).getString("numberLike"))));
                            } catch (JSONException e) {
                                throw new RuntimeException(e);
                            }
                        }
                        HorizontaleAdapter horizontaleAdapter = new HorizontaleAdapter(mOnlineBookList);
                        mBookRecommendedRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(),LinearLayoutManager.HORIZONTAL,false));
                        mBookRecommendedRecyclerView.setAdapter(horizontaleAdapter);
                    }
                }
                else
                    update();
            }
            else
            {
                ArrayList<Connection> list = new ArrayList<>();
                list.add(new Connection(getString(R.string.no_connection_available),"HOME_FRAGMENT",false));
                NoConnectionAdapter noConnectionAdapter = new NoConnectionAdapter(list);
                mWaitRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                mWaitRecyclerView.setAdapter(noConnectionAdapter);
            }
        }
        private void update(){
            UpdateDialog updateDialog = new UpdateDialog(getActivity());
            updateDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            updateDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
            TextView annuler = updateDialog.findViewById(R.id.annuler);
            TextView installer = updateDialog.findViewById(R.id.installer);
            Account account = new Account();
            annuler.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(account.logout(getContext()))
                    {
                        Intent loginIntent = new Intent(getContext(), LoginActivity.class);
                        startActivity(loginIntent);
                        getActivity().finish();
                        updateDialog.cancel();
                    }
                }
            });

            installer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String url = getString(R.string.ip_server); // Remplacez ceci par l'URL que vous souhaitez ouvrir
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(intent);
                }
            });
            updateDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    if(account.logout(getContext()))
                    {
                        Intent loginIntent = new Intent(getContext(), LoginActivity.class);
                        startActivity(loginIntent);
                        getActivity().finish();
                        updateDialog.cancel();
                    }
                }
            });
            updateDialog.build();
        }
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
                            if(!isExistsS(mStructures,jsonArray.getJSONObject(i).getString("id")))
                            {
                                mStructures.add(new Structure(
                                        jsonArray.getJSONObject(i).getString("id"),
                                        jsonArray.getJSONObject(i).getString("logo"),
                                        jsonArray.getJSONObject(i).getString("nameStruct"),
                                        jsonArray.getJSONObject(i).getString("description"),true,
                                        jsonArray.getJSONObject(i).getString("banner"),
                                        jsonArray.getJSONObject(i).getString("author"),
                                        jsonArray.getJSONObject(i).getString("adhererNumber"),
                                        jsonArray.getJSONObject(i).getString("bookNumber"),
                                        jsonArray.getJSONObject(i).getString("isAdmin")));
                            }
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
                            mStructures.add(new Structure(
                                    jsonArray.getJSONObject(i).getString("id"),
                                    jsonArray.getJSONObject(i).getString("logo"),
                                    jsonArray.getJSONObject(i).getString("nameStruct"),
                                    jsonArray.getJSONObject(i).getString("description"),false,
                                    jsonArray.getJSONObject(i).getString("banner"),
                                    jsonArray.getJSONObject(i).getString("author"),
                                    jsonArray.getJSONObject(i).getString("adhererNumber"),
                                    jsonArray.getJSONObject(i).getString("bookNumber"),"0"));
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
    private class AuthorSyn extends AsyncTask<String,Void,String> {
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
                        mAuthorArrayList.add(new Author(jsonArray.getJSONObject(i).getString("idAuthor"),jsonArray.getJSONObject(i).getString("name"),jsonArray.getJSONObject(i).getString("firstName"),jsonArray.getJSONObject(i).getString("profile"),jsonArray.getJSONObject(i).getString("profession")));
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                }
                AuthorHorizontaleAdapter authorHorizontaleAdapter = new AuthorHorizontaleAdapter(mAuthorArrayList);
                mAuthorRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(),LinearLayoutManager.HORIZONTAL,false));
                mAuthorRecyclerView.setAdapter(authorHorizontaleAdapter);
            }
            else {
                ArrayList<Connection> list = new ArrayList<>();
                list.add(new Connection(getString(R.string.no_connection_available),"CATEGORY_FRAGMENT",false));
                NoConnectionAdapter noConnectionAdapter = new NoConnectionAdapter(list);
                mAuthorRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(),LinearLayoutManager.HORIZONTAL,false));
                mAuthorRecyclerView.setAdapter(noConnectionAdapter);
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
    private RecyclerView mBookRecommendedRecyclerView;
    private ArrayList<OnlineBook> mOnlineBookList;
    private ArrayList<Author> mAuthorArrayList;
    private RecyclerView mStructureRecyclerView;
    private RecyclerView mAuthorRecyclerView;
    private ArrayList<Structure> mStructures;
    private NoConnectionAdapter mNoConnectionAdapter;
    private ImageView mWelcomeImageView;
    private TextView mBookMoreTextView;
    private TextView mStructMoreTextView;
    private TextView mAuthorMoreTextView;
    private Account mAccount;
    private StructureAdapter StructAdapter;
    private RecyclerView mWaitRecyclerView;
    private NestedScrollView mNestedScrollView;
}