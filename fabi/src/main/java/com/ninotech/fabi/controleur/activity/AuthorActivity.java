package com.ninotech.fabi.controleur.activity;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ninotech.fabi.R;
import com.ninotech.fabi.controleur.adapter.AuthorFormatBookAdapter;
import com.ninotech.fabi.controleur.adapter.AuthorHorizontaleAdapter;
import com.ninotech.fabi.controleur.adapter.ElectronicAdapter;
import com.ninotech.fabi.controleur.adapter.HorizontaleAdapter;
import com.ninotech.fabi.controleur.adapter.NoConnectionAdapter;
import com.ninotech.fabi.controleur.adapter.VoidContainerAdapter;
import com.ninotech.fabi.controleur.animation.RoundedTransformation;
import com.ninotech.fabi.model.data.Author;
import com.ninotech.fabi.model.data.Connection;
import com.ninotech.fabi.model.data.Library;
import com.ninotech.fabi.model.data.OnlineBook;
import com.ninotech.fabi.model.data.Server;
import com.ninotech.fabi.model.data.VoidContainer;
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

    private static final int REQUEST_CALL_PERMISSION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_author);
        Objects.requireNonNull(getSupportActionBar()).hide();
        //AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        mWaitRecyclerView = findViewById(R.id.recycler_view_activity_author_wait);
        mNestedScrollView = findViewById(R.id.nested_scroll_view_activity_author);
        mProfileImageView = findViewById(R.id.image_view_author_activity_profile);
        mUsernameTextView = findViewById(R.id.text_view_activity_author_username);
        mProfessionTextView = findViewById(R.id.text_view_activity_author_profession);
        mBooksRecyclerView = findViewById(R.id.recycler_view_activity_author_books);
        mAuthorRecyclerView = findViewById(R.id.recycler_view_activity_author);
        mBackImageView = findViewById(R.id.image_view_toolbar_search);
        mSearchEditText = findViewById(R.id.edit_text_toolbar_search);
        mAppelImageView = findViewById(R.id.image_view_activity_author_appel);
        mEmailImageView = findViewById(R.id.image_view_activity_author_email);
        mWhatsAppImageView = findViewById(R.id.image_view_activity_author_whatsapp);
        mSession = new Session(getApplicationContext());
        mAuthorFormatBookRecyclerView = findViewById(R.id.recycler_view_activity_author_format_books);
        mAuthorArrayList = new ArrayList<>();
        mOnlineBookList = new ArrayList<>();
        Intent authorIntent = getIntent();
        BroadcastReceiver receiverNoConnectionAdapter = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if ("AUTHOR_ACTIVITY".equals(intent.getAction())) {
                    try {
                        ArrayList<Connection> list = new ArrayList<>();
                        list.add(new Connection(getString(R.string.wait),"STRUCTURE_ACTIVITY",true));
                        NoConnectionAdapter noConnectionAdapter = new NoConnectionAdapter(list);
                        mWaitRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                        mWaitRecyclerView.setAdapter(noConnectionAdapter);
                        AuthorBookSyn authorBookSyn = new AuthorBookSyn();
                        authorBookSyn.execute(Server.getIpServerAndroid(getApplicationContext()) + "AuthorBook.php",mSession.getIdNumber(),mAuthor.getIdNumber());
                        AuthorSyn authorSyn = new AuthorSyn();
                        authorSyn.execute(Server.getIpServerAndroid(getApplicationContext()) + "AuthorSimular.php", mAuthor.getIdNumber());
                    }catch (Exception e)
                    {
                        Log.e("errRankingFragment",e.getMessage());
                    }

                }
            }
        };
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            registerReceiver(receiverNoConnectionAdapter, new IntentFilter("AUTHOR_ACTIVITY"),Context.RECEIVER_EXPORTED);
        }
        ArrayList<Connection> list = new ArrayList<>();
        mSearchEditText.setVisibility(View.GONE);
        list.add(new Connection(getString(R.string.wait),null,true));
        NoConnectionAdapter noConnectionAdapter = new NoConnectionAdapter(list);
        mWaitRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        mWaitRecyclerView.setAdapter(noConnectionAdapter);

        mAuthorRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        mAuthorRecyclerView.setAdapter(noConnectionAdapter);
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
                searchIntent.putExtra("online_book_key", "AUTHOR_ACTIVITY");
                searchIntent.putExtra("id_author_key",mAuthor.getIdNumber());
                startActivity(searchIntent);
            }
        });
        mAppelImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(AuthorActivity.this, "Appel Telephonique", Toast.LENGTH_SHORT).show();
                lancerAppel("+22794961793");
            }
        });

        mEmailImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(AuthorActivity.this, "Email", Toast.LENGTH_SHORT).show();
                envoyerEmail("derkariom@gmail.com",
                        "Sujet : ",
                        "Bonjour Bachir Abdoul Kader, ",
                        AuthorActivity.this);

            }
        });

        mWhatsAppImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(AuthorActivity.this, "WhatsApp", Toast.LENGTH_SHORT).show();
                envoyerMessageWhatsApp("+22794961793",
                        "Bonjour Bachir Abdoul Kader,",
                        AuthorActivity.this);

            }
        });
        Picasso.get()
                .load(Server.getIpServer(getApplicationContext()) + "ressources/profile/" + mAuthor.getProfile())
                .placeholder(R.drawable.img_wait_profile)
                .error(R.drawable.img_wait_profile)
                .transform(new RoundedTransformation(1000,4))
                .resize(384,384)
                .into(mProfileImageView);
        mUsernameTextView.setText(mAuthor.getFirstName() + " " + mAuthor.getName());
        mProfessionTextView.setText(mAuthor.getProfession());
        AuthorBookSyn authorBookSyn = new AuthorBookSyn();
        authorBookSyn.execute(Server.getIpServerAndroid(getApplicationContext()) + "AuthorBook.php",mSession.getIdNumber(),mAuthor.getIdNumber());
        AuthorSyn authorSyn = new AuthorSyn();
        authorSyn.execute(Server.getIpServerAndroid(getApplicationContext()) + "AuthorSimular.php", mAuthor.getIdNumber());
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
                if (!jsonData.equals("RAS"))
                {
                    mWaitRecyclerView.setVisibility(View.GONE);
                    mNestedScrollView.setVisibility(View.VISIBLE);
                    mSearchEditText.setVisibility(View.VISIBLE);
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
                }
                List<Library> libraryList = new ArrayList<>();
                AuthorFormatBookAdapter authorFormatBookAdapter = new AuthorFormatBookAdapter(libraryList);
                libraryList.add(new Library(1,R.drawable.img_electronic_book,"Mes livres électroniques", nbrElectronic,mAuthor.getIdNumber()));
                libraryList.add(new Library(2,R.drawable.img_audio_book,"Mes livres audios",nbrAudio,mAuthor.getIdNumber()));
                libraryList.add(new Library(3,R.drawable.img_loand_book,"Mes livres physiques",nbrPhysique,mAuthor.getIdNumber()));
                mAuthorFormatBookRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                mAuthorFormatBookRecyclerView.setAdapter(authorFormatBookAdapter);
            } else {
                ArrayList<Connection> list = new ArrayList<>();
                list.add(new Connection(getString(R.string.no_connection_available), "AUTHOR_ACTIVITY", false));
                NoConnectionAdapter noConnectionAdapter = new NoConnectionAdapter(list);
                mWaitRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                mWaitRecyclerView.setAdapter(noConnectionAdapter);
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
                            mAuthorArrayList.add(new Author(jsonArray.getJSONObject(i).getString("idAuthor"),jsonArray.getJSONObject(i).getString("name"),jsonArray.getJSONObject(i).getString("firstName"),jsonArray.getJSONObject(i).getString("profile"),jsonArray.getJSONObject(i).getString("profession")));
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
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
    private void lancerAppel(String numero) {
        if (numero == null || numero.isEmpty()) {
            Toast.makeText(this, "Numéro invalide", Toast.LENGTH_SHORT).show();
            return;
        }

        // Vérifier si la permission est déjà accordée
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
                != PackageManager.PERMISSION_GRANTED) {
            // Demander la permission
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CALL_PHONE}, REQUEST_CALL_PERMISSION);
        } else {
            // Lancer directement l'appel
            String dial = "tel:" + numero;
            startActivity(new Intent(Intent.ACTION_CALL, Uri.parse(dial)));
        }
    }

    // Résultat de la demande de permission
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CALL_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission accordée → relancer l'appel
                lancerAppel(mNumberAuthor);
            } else {
                // Permission refusée
                Toast.makeText(this, "Permission d'appel refusée", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private void envoyerEmail(String destinataire, String sujet, String message, Context context) {
        // Construire l'URI avec "mailto"
        Uri uri = Uri.parse("mailto:" + destinataire);

        // Créer l'intent
        Intent intent = new Intent(Intent.ACTION_SENDTO, uri);
        intent.putExtra(Intent.EXTRA_SUBJECT, sujet);
        intent.putExtra(Intent.EXTRA_TEXT, message);

        try {
            context.startActivity(Intent.createChooser(intent, "Choisir une application de messagerie"));
        } catch (android.content.ActivityNotFoundException e) {
            Toast.makeText(context, "Aucune application email installée", Toast.LENGTH_SHORT).show();
        }
    }
    private void envoyerMessageWhatsApp(String numero, String message, Context context) {
        try {
            // Construire l'URL
            String url = "https://wa.me/" + numero + "?text=" + Uri.encode(message);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));

            // Vérifier si WhatsApp normal est installé
            if (isPackageInstalled("com.whatsapp", context)) {
                intent.setPackage("com.whatsapp");
            }
            // Sinon vérifier WhatsApp Business
            else if (isPackageInstalled("com.whatsapp.w4b", context)) {
                intent.setPackage("com.whatsapp.w4b");
            } else {
                Toast.makeText(context, "Aucune version de WhatsApp n'est installée", Toast.LENGTH_SHORT).show();
                return;
            }

            context.startActivity(intent);

        } catch (Exception e) {
            Toast.makeText(context, "Erreur lors de l'ouverture de WhatsApp", Toast.LENGTH_SHORT).show();
        }
    }

    // Vérifie si un package est installé
    private boolean isPackageInstalled(String packageName, Context context) {
        try {
            context.getPackageManager().getPackageInfo(packageName, 0);
            return true;
        } catch (Exception e) {
            return false;
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
    private RecyclerView mWaitRecyclerView;
    private NestedScrollView mNestedScrollView;
    private ImageView mAppelImageView;
    private ImageView mEmailImageView;
    private ImageView mWhatsAppImageView;
    private String mNumberAuthor=null;
}