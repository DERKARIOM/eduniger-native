package com.ninotech.fabi.controleur.activity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ninotech.fabi.R;
import com.ninotech.fabi.controleur.adapter.AuthorFormatBookAdapter;
import com.ninotech.fabi.controleur.adapter.AuthorHorizontaleAdapter;
import com.ninotech.fabi.controleur.adapter.ElectronicAdapter;
import com.ninotech.fabi.controleur.adapter.HorizontaleAdapter;
import com.ninotech.fabi.controleur.adapter.NoConnectionAdapter;
import com.ninotech.fabi.controleur.animation.RoundedTransformation;
import com.ninotech.fabi.model.data.Author;
import com.ninotech.fabi.model.data.Connection;
import com.ninotech.fabi.model.data.Library;
import com.ninotech.fabi.model.data.OnlineBook;
import com.ninotech.fabi.model.table.Session;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AuthorActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_author);
        Objects.requireNonNull(getSupportActionBar()).hide();
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        mProfileImageView = findViewById(R.id.image_view_author_activity_profile);
        mUsernameTextView = findViewById(R.id.text_view_activity_author_username);
        mProfessionTextView = findViewById(R.id.text_view_activity_author_profession);
        mBooksRecyclerView = findViewById(R.id.recycler_view_activity_author_books);
        mAuthorRecyclerView = findViewById(R.id.recycler_view_activity_author);
        mBackImageView = findViewById(R.id.image_view_toolbar_search);
        mSearchEditText = findViewById(R.id.edit_text_toolbar_search);
        mSession = new Session(getApplicationContext());
        mAuthorFormatBookRecyclerView = findViewById(R.id.recycler_view_activity_author_format_books);
        mAuthorArrayList = new ArrayList<>();
        mOnlineBookList = new ArrayList<>();
        Intent authorIntent = getIntent();
        mAuthor = new Author(
                authorIntent.getStringExtra("intent_author_adapter_id"),
                authorIntent.getStringExtra("intent_author_adapter_name"),
                authorIntent.getStringExtra("intent_author_adapter_first_name"),
                authorIntent.getStringExtra("intent_author_adapter_profile"),
                authorIntent.getStringExtra("intent_author_adapter_profession")
        );
        mBackImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        mSearchEditText.setSelectAllOnFocus(false);
        mSearchEditText.setFocusable(false);
        mSearchEditText.setHint("  Recherche mes livres");
        mSearchEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent searchIntent = new Intent(getApplicationContext(), SearchActivity.class);
                searchIntent.putExtra("search_key", "ONLINE_BOOK");
                searchIntent.putExtra("online_book_key", "STRUCTURE_ACTIVITY");
                searchIntent.putExtra("id_struct_key",1);
                startActivity(searchIntent);
            }
        });
        Picasso.get()
                .load(getString(R.string.ip_server) + "ressources/profile/" + mAuthor.getProfile())
                .placeholder(R.drawable.img_default_book)
                .error(R.drawable.img_default_book)
                .transform(new RoundedTransformation(1000,4))
                .resize(384,384)
                .into(mProfileImageView);
        mUsernameTextView.setText(mAuthor.getFirstName() + " " + mAuthor.getName());
        mProfessionTextView.setText(mAuthor.getProfession());
        AuthorBookSyn authorBookSyn = new AuthorBookSyn();
        authorBookSyn.execute(getString(R.string.ip_server_android) + "AuthorBook.php",mSession.getIdNumber(),mAuthor.getIdNumber());
        AuthorSyn authorSyn = new AuthorSyn();
        authorSyn.execute(getString(R.string.ip_server_android) + "AuthorTop.php", "94961793");
    }
    private class AuthorBookSyn extends AsyncTask<String,Void,String> {
        @Override
        protected String doInBackground(String... params) {

            try {
                OkHttpClient client = new OkHttpClient();
                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("idNumber", params[1])
                        .addFormDataPart("idAuthor", params[2])
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
                int nbrElectronic=0,nbrAudio=0,nbrPhysique=0;
                if(!jsonData.equals("RAS"))
                {
                    JSONArray jsonArray = null;
                    try {
                        jsonArray = new JSONArray(jsonData);
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                    for (int i = 0; i < jsonArray.length(); i++) {
                        try {
                            mOnlineBookList.add(new OnlineBook(jsonArray.getJSONObject(i).getString("idBook"), jsonArray.getJSONObject(i).getString("blanket"), jsonArray.getJSONObject(i).getString("bookTitle"), jsonArray.getJSONObject(i).getString("categoryTitle"), jsonArray.getJSONObject(i).getString("isPhysic"), jsonArray.getJSONObject(i).getString("electronic"), jsonArray.getJSONObject(i).getString("isAudio"), Integer.parseInt(jsonArray.getJSONObject(i).getString("numberLike")), Integer.parseInt(jsonArray.getJSONObject(i).getString("numberNoLike"))));
                            if(!mOnlineBookList.get(i).getElectronic().equals("null"))
                                nbrElectronic++;
                            if (mOnlineBookList.get(i).getIsAudio().equals("1"))
                                nbrAudio++;
                            if (mOnlineBookList.get(i).getIsPhysic().equals("1"))
                                nbrPhysique++;
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    HorizontaleAdapter horizontaleAdapter = new HorizontaleAdapter(mOnlineBookList);
                    mBooksRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false));
                    mBooksRecyclerView.setAdapter(horizontaleAdapter);
                }
                List<Library> libraryList = new ArrayList<>();
                AuthorFormatBookAdapter electronicAdapter = new AuthorFormatBookAdapter(libraryList);
                libraryList.add(new Library(1,R.drawable.img_electronic_book,"Mes livres électronique", nbrElectronic));
                libraryList.add(new Library(2,R.drawable.img_audio_book,"Mes livres audio",nbrAudio));
                libraryList.add(new Library(3,R.drawable.img_loand_book,"Mes livres physique",nbrPhysique));
                mAuthorFormatBookRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                mAuthorFormatBookRecyclerView.setAdapter(electronicAdapter);
            } else {
                ArrayList<Connection> list = new ArrayList<>();
                list.add(new Connection(getString(R.string.no_connection_available), "RECOMMENDED_FRAGMENT", false));
                NoConnectionAdapter noConnectionAdapter = new NoConnectionAdapter(list);
                mBooksRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                mBooksRecyclerView.setAdapter(noConnectionAdapter);
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
                        mAuthorArrayList.add(new Author(jsonArray.getJSONObject(i).getString("idAuthor"),jsonArray.getJSONObject(i).getString("name"),jsonArray.getJSONObject(i).getString("firstName"),jsonArray.getJSONObject(i).getString("profile"),jsonArray.getJSONObject(i).getString("profession")));
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
    private ImageView mProfileImageView;
    private TextView mUsernameTextView;
    private Author mAuthor;
    private RecyclerView mBooksRecyclerView;
    private ArrayList<OnlineBook> mOnlineBookList;
    private ArrayList<Author> mAuthorArrayList;
    private RecyclerView mAuthorRecyclerView;
    private Session mSession;
    private RecyclerView mAuthorFormatBookRecyclerView;
    private ImageView mBackImageView;
    private EditText mSearchEditText;
    private TextView mProfessionTextView;
}