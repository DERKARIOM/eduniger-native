package com.ninotech.fabi.controleur.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ninotech.fabi.R;
import com.ninotech.fabi.controleur.adapter.AuthorHorizontaleAdapter;
import com.ninotech.fabi.controleur.adapter.HorizontaleAdapter;
import com.ninotech.fabi.controleur.adapter.NoConnectionAdapter;
import com.ninotech.fabi.controleur.adapter.StructureAdapter;
import com.ninotech.fabi.controleur.animation.RoundedTransformation;
import com.ninotech.fabi.controleur.dialog.SimpleOkDialog;
import com.ninotech.fabi.controleur.dialog.StructDeleteDialog;
import com.ninotech.fabi.controleur.fragment.BooksFragment;
import com.ninotech.fabi.model.data.Author;
import com.ninotech.fabi.model.data.Connection;
import com.ninotech.fabi.model.data.OnlineBook;
import com.ninotech.fabi.model.data.PasswordUtil;
import com.ninotech.fabi.model.data.Structure;
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
        Intent intentStructure = getIntent();
        mBackImageView = findViewById(R.id.image_view_toolbar_search);
        mSearchEditText = findViewById(R.id.edit_text_toolbar_search);
        mWelcomeImageView = findViewById(R.id.image_view_structure_activity_welcome);
        mProfileImageView = findViewById(R.id.image_view_structure_activity_profile);
        mAuthorRecyclerView = findViewById(R.id.recycler_view_activity_structure_author);
        mNameTextView = findViewById(R.id.text_view_structure_activity_name);
        mAuthorTextView = findViewById(R.id.text_view_activity_structure_author);
        mNumberTextView = findViewById(R.id.image_view_activity_structure_number);
        mDescriptionTextView = findViewById(R.id.text_view_activity_structure_description);
        mMoreDescTextView = findViewById(R.id.text_view_activity_structure_more_desc);
        mReduceTextView = findViewById(R.id.text_view_activity_structure_reduce_desc);
        mMoreBookTextView = findViewById(R.id.text_view_activity_structure_more_books);
        mMoreAuthorTextView = findViewById(R.id.text_view_activity_structure_more_author);
        mAdhererButton = findViewById(R.id.button_activity_structure_adherer);
        mBookRecommendedRecyclerView = findViewById(R.id.recycler_view_activity_structure_books);
        mSession = new Session(getApplicationContext());
        mOnlineBookList = new ArrayList<>();
        mAuthorArrayList = new ArrayList<>();
        mStructure = new Structure(
                intentStructure.getStringExtra("intent_structure_adapter_id"),
                intentStructure.getStringExtra("intent_structure_adapter_logo"),
                intentStructure.getStringExtra("intent_structure_adapter_name"),
                intentStructure.getStringExtra("intent_structure_adapter_description"),
                intentStructure.getBooleanExtra("intent_structure_adapter_is_adhere",false),
                intentStructure.getStringExtra("intent_structure_adapter_banner"),
                intentStructure.getStringExtra("intent_structure_adapter_author"),
                intentStructure.getStringExtra("intent_structure_adapter_adherer_number"),
                intentStructure.getStringExtra("intent_structure_adapter_book_number"),
                intentStructure.getStringExtra("intent_structure_adapter_admin")
        );
        BroadcastReceiver receiverNoConnectionAdapter = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if ("STRUCTURE_ACTIVITY".equals(intent.getAction())) {
                    try {
                        ArrayList<Connection> list = new ArrayList<>();
                        list.add(new Connection(getString(R.string.wait),"STRUCTURE_ACTIVITY",true));
                        NoConnectionAdapter noConnectionAdapter = new NoConnectionAdapter(list);
                        mBookRecommendedRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                        mBookRecommendedRecyclerView.setAdapter(noConnectionAdapter);
                        StructBookSyn structBookSyn = new StructBookSyn();
                        structBookSyn.execute(getString(R.string.ip_server_android) + "StructBook.php", mSession.getIdNumber(),mStructure.getId());
                        AuthorSyn authorSyn = new AuthorSyn();
                        authorSyn.execute(getString(R.string.ip_server_android) + "AuthorTop.php", mSession.getIdNumber());
                    }catch (Exception e)
                    {
                        Log.e("errRankingFragment",e.getMessage());
                    }

                }
            }
        };
        registerReceiver(receiverNoConnectionAdapter, new IntentFilter("STRUCTURE_ACTIVITY"));
        mBackImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        mSearchEditText.setSelectAllOnFocus(false);
        mSearchEditText.setFocusable(false);
        mSearchEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent searchIntent = new Intent(getApplicationContext(), SearchActivity.class);
                searchIntent.putExtra("search_key", "ONLINE_BOOK");
                searchIntent.putExtra("online_book_key", "STRUCTURE_ACTIVITY");
                searchIntent.putExtra("id_struct_key",mStructure.getId());
                startActivity(searchIntent);
            }
        });
        mNameTextView.setText(mStructure.getName());
        mAuthorTextView.setText("@" + mStructure.getAuthor());
        mNumberTextView.setText(mStructure.getAdhererNumber() + " Adhérents ° " + mStructure.getBookNumber() + " Livres");
        mDescriptionTextView.setText("Bienvenue sur la struture " + mStructure.getName() + "!");
        if(mStructure.isAdhere())
        {
            mAdhererButton.setText("Détacher");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                mAdhererButton.setBackgroundTintList(ColorStateList.valueOf(getApplicationContext().getColor(R.color.black3)));
            }
        }
        mMoreDescTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDescriptionTextView.setText(mStructure.getDescription());
                mMoreDescTextView.setVisibility(View.GONE);
                mReduceTextView.setVisibility(View.VISIBLE);
            }
        });
        mReduceTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mReduceTextView.setVisibility(View.GONE);
                mMoreDescTextView.setVisibility(View.VISIBLE);
                mDescriptionTextView.setText("Bienvenue sur la struture " + mStructure.getName() + "!");
            }
        });
        mAdhererButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (mStructure.getId())
                {
                    case "1","3":
                        if (mAdhererButton.getText().toString().equals("Détacher"))
                            structDelete(mStructure.getId());
                        else
                        {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                mAdhererButton.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.black3)));
                            }
                            mAdhererButton.setText("Détacher");
                            mStructure.setAdhere(false);
                            DetachStructSyn detachStructSyn = new DetachStructSyn();
                            detachStructSyn.execute(getString(R.string.ip_server_android) + "AdhererStruct.php",mSession.getIdNumber(),mStructure.getId());
                        }
                        break;
                    case "2":
                        simpleOkDialog(R.drawable.vector_purple_200_desole,"Structure Exclusive","Cette structure est exclusivement réservée aux étudiants de la FAST UAM. Veuillez vérifier que vous remplissez les critères d'adhésion puis contacter les numéro suivante :\n+22796627534 / +22794961793.");
                        break;

                }
            }
        });

        mMoreBookTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent searchIntent = new Intent(getApplicationContext(), SearchActivity.class);
                searchIntent.putExtra("search_key", "ONLINE_BOOK");
                searchIntent.putExtra("online_book_key", "STRUCTURE_ACTIVITY");
                searchIntent.putExtra("id_struct_key",mStructure.getId());
                startActivity(searchIntent);
            }
        });
        mMoreAuthorTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent searchIntent = new Intent(getApplicationContext(), SearchActivity.class);
                searchIntent.putExtra("search_key", "AUTHOR_ONLINE");
                searchIntent.putExtra("online_book_key", "MAIN_ACTIVITY");
                startActivity(searchIntent);
            }
        });
        Picasso.get()
                .load(getString(R.string.ip_server) + "ressources/baniere/" + mStructure.getBanner())
                .transform(new RoundedTransformation(200,10))
                .resize(6200,2222)
                .into(mWelcomeImageView);
        mWelcomeImageView.setVisibility(View.VISIBLE);
        Picasso.get()
                .load(getResources().getString(R.string.ip_server) + "ressources/cover/" + mStructure.getCover())
                .placeholder(R.drawable.img_default_book)
                .error(R.drawable.img_default_book)
                .transform(new RoundedTransformation(1000,4))
                .resize(284,284)
                .into(mProfileImageView);
        StructBookSyn structBookSyn = new StructBookSyn();
        structBookSyn.execute(getString(R.string.ip_server_android) + "StructBook.php", mSession.getIdNumber(),mStructure.getId());
        AuthorSyn authorSyn = new AuthorSyn();
        authorSyn.execute(getString(R.string.ip_server_android) + "AuthorTop.php", mSession.getIdNumber());
    }
    private class StructBookSyn extends AsyncTask<String,Void,String> {
        @Override
        protected String doInBackground(String... params) {

            try {
                OkHttpClient client = new OkHttpClient();
                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("idNumber", params[1])
                        .addFormDataPart("idStruct", params[2])
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
                if (!jsonData.equals("RAS")) {
                    JSONArray jsonArray = null;
                    try {
                        jsonArray = new JSONArray(jsonData);
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                    if(mStructure.isAdhere() && mStructure.getAdmin().equals("1"))
                        mOnlineBookList.add(new OnlineBook("add", "addbook.png", "Ajouter un livre", "EduNiger", "oui","oui", "oui", 9,9));
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
                list.add(new Connection(getString(R.string.no_connection_available), "STRUCTURE_ACTIVITY", false));
                NoConnectionAdapter noConnectionAdapter = new NoConnectionAdapter(list);
                mBookRecommendedRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                mBookRecommendedRecyclerView.setAdapter(noConnectionAdapter);
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
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
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
                        mAuthorArrayList.add(new Author(jsonArray.getJSONObject(i).getString("idAuthor"),jsonArray.getJSONObject(i).getString("name"),jsonArray.getJSONObject(i).getString("firstName"),jsonArray.getJSONObject(i).getString("profile")));
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                }
                AuthorHorizontaleAdapter authorHorizontaleAdapter = new AuthorHorizontaleAdapter(mAuthorArrayList);
                mAuthorRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext(),LinearLayoutManager.HORIZONTAL,false));
                mAuthorRecyclerView.setAdapter(authorHorizontaleAdapter);
            }
            else {
                ArrayList<Connection> list = new ArrayList<>();
                list.add(new Connection(getString(R.string.no_connection_available),"CATEGORY_FRAGMENT",false));
                NoConnectionAdapter noConnectionAdapter = new NoConnectionAdapter(list);
                mAuthorRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext(),LinearLayoutManager.HORIZONTAL,false));
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
    private void structDelete(String id){
        StructDeleteDialog structDeleteDialog = new StructDeleteDialog(this);
        structDeleteDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        structDeleteDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        TextView no = structDeleteDialog.findViewById(R.id.no);
        TextView yes = structDeleteDialog.findViewById(R.id.yes);
        EditText password = structDeleteDialog.findViewById(R.id.edit_text_dialog_struct_delete_password);
        no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                structDeleteDialog.cancel();
            }
        });

        yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!password.getText().toString().isEmpty())
                {
                    if (Objects.equals(PasswordUtil.hashPassword(password.getText().toString()), mSession.getPassword()))
                    {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            structDeleteDialog.cancel();
                            mAdhererButton.setBackgroundTintList(ColorStateList.valueOf(getApplicationContext().getColor(R.color.purple_200)));
                            mAdhererButton.setText("Adhérer");
                            DetachStructSyn detachStructSyn = new DetachStructSyn();
                            detachStructSyn.execute(getString(R.string.ip_server_android) + "DetachStruct.php",mSession.getIdNumber(),id);
                        }
                    }
                }
                password.setBackground(getApplicationContext().getDrawable(R.drawable.forme_white_radius_100dp_border_rouge));
            }
        });
        structDeleteDialog.build();
    }
    private class DetachStructSyn extends AsyncTask<String,Void,String> {
        @Override
        protected String doInBackground(String... params) {

            try {
                OkHttpClient client = new OkHttpClient();
                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("idUser",params[1])
                        .addFormDataPart("idStruct",params[2])
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
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
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
                if(!jsonData.equals("RAS"))
                {
                    if(jsonData.equals("true"))
                    {
                        Toast.makeText(getApplicationContext(), "Structure détacher", Toast.LENGTH_SHORT);
                    }
                }
            }
        }
    }
    private class AdhererStructSyn extends AsyncTask<String,Void,String> {
        @Override
        protected String doInBackground(String... params) {

            try {
                OkHttpClient client = new OkHttpClient();
                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("idUser",params[1])
                        .addFormDataPart("idStruct",params[2])
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
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
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
                if(!jsonData.equals("RAS"))
                {
                    if(jsonData.equals("true"))
                    {
                        Toast.makeText(getApplicationContext(), "Structure Adhérer", Toast.LENGTH_SHORT);
                    }
                }
            }
        }
    }
    private void simpleOkDialog(int ico , String title , String message){
        SimpleOkDialog simpleOkDialog = new SimpleOkDialog((Activity) getApplicationContext());
        simpleOkDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        simpleOkDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        ImageView icoImageView = simpleOkDialog.findViewById(R.id.image_view_dialog_simple_ok_icon);
        TextView titleTextView = simpleOkDialog.findViewById(R.id.text_view_dialog_simple_ok_title);
        TextView messageTextView = simpleOkDialog.findViewById(R.id.text_view_dialog_simple_ok_message);
        TextView okTextView = simpleOkDialog.findViewById(R.id.text_view_dialog_simple_ok);
        icoImageView.setImageResource(ico);
        titleTextView.setText(title);
        messageTextView.setText(message);
        okTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                simpleOkDialog.cancel();
            }
        });
        simpleOkDialog.build();
    }
    private ImageView mWelcomeImageView;
    private ImageView mProfileImageView;
    private TextView mNameTextView;
    private RecyclerView mBookRecommendedRecyclerView;
    private ArrayList<OnlineBook> mOnlineBookList;
    private ArrayList<Author> mAuthorArrayList;
    private RecyclerView mAuthorRecyclerView;
    private Session mSession;
    private Structure mStructure;
    private TextView mAuthorTextView;
    private TextView mNumberTextView;
    private TextView mDescriptionTextView;
    private TextView mMoreDescTextView;
    private TextView mReduceTextView;
    private TextView mMoreBookTextView;
    private TextView mMoreAuthorTextView;
    private Button mAdhererButton;
    private ImageView mBackImageView;
    private EditText mSearchEditText;
}