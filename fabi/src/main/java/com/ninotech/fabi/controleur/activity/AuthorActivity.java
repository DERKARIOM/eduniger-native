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
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ninotech.fabi.R;
import com.ninotech.fabi.controleur.adapter.AuthorFormatBookAdapter;
import com.ninotech.fabi.controleur.adapter.AuthorHorizontaleAdapter;
import com.ninotech.fabi.controleur.adapter.HorizontaleAdapter;
import com.ninotech.fabi.controleur.adapter.NoConnectionAdapter;
import com.ninotech.fabi.controleur.animation.RoundedTransformation;
import com.ninotech.fabi.model.data.Author;
import com.ninotech.fabi.model.data.Connection;
import com.ninotech.fabi.model.data.Library;
import com.ninotech.fabi.model.data.OnlineBook;
import com.ninotech.fabi.model.data.Server;
import com.ninotech.fabi.model.table.Session;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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

    private static final String TAG = "AuthorActivity";
    private static final String ACTION_AUTHOR = "AUTHOR_ACTIVITY";
    private static final String RESPONSE_RAS = "RAS";
    private static final int REQUEST_CALL_PERMISSION = 1;
    private static final String WHATSAPP_PACKAGE = "com.whatsapp";
    private static final String WHATSAPP_BUSINESS_PACKAGE = "com.whatsapp.w4b";

    // Views
    private NestedScrollView mNestedScrollView;
    private RecyclerView mWaitRecyclerView;
    private RecyclerView mBooksRecyclerView;
    private RecyclerView mAuthorRecyclerView;
    private RecyclerView mAuthorFormatBookRecyclerView;
    private ImageView mProfileImageView;
    private ImageView mBackImageView;
    private ImageView mAppelImageView;
    private ImageView mEmailImageView;
    private ImageView mWhatsAppImageView;
    private TextView mUsernameTextView;
    private TextView mProfessionTextView;
    private EditText mSearchEditText;
    private LinearLayout mContactsLinearLayout;

    // Data
    private final List<OnlineBook> mOnlineBookList = new ArrayList<>();
    private final List<Author> mAuthorArrayList = new ArrayList<>();
    private Author mAuthor;
    private Session mSession;
    private String mNumberAuthor;

    // Utils
    private OkHttpClient mHttpClient;
    private BroadcastReceiver mNoConnectionReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_author);
        Objects.requireNonNull(getSupportActionBar()).hide();

        initializeComponents();
        initializeViews();
        setupRecyclerViews();
        configureContactVisibility();
        setupClickListeners();
        loadAuthorImage();
        registerBroadcastReceiver();
        loadAuthorData();
    }

    private void initializeComponents() {
        mSession = new Session(this);
        mHttpClient = new OkHttpClient();
        mAuthor = extractAuthorFromIntent();
    }

    private Author extractAuthorFromIntent() {
        Intent intent = getIntent();
        return new Author(
                intent.getStringExtra("intent_author_adapter_id"),
                intent.getStringExtra("intent_author_adapter_name"),
                intent.getStringExtra("intent_author_adapter_first_name"),
                intent.getStringExtra("intent_author_adapter_profile"),
                intent.getStringExtra("intent_author_adapter_profession"),
                intent.getStringExtra("intent_author_adapter_call"),
                intent.getStringExtra("intent_author_adapter_email"),
                intent.getStringExtra("intent_author_adapter_whatsapp")
        );
    }

    private void initializeViews() {
        mWaitRecyclerView = findViewById(R.id.recycler_view_activity_author_wait);
        mNestedScrollView = findViewById(R.id.nested_scroll_view_activity_author);
        mProfileImageView = findViewById(R.id.image_view_author_activity_profile);
        mUsernameTextView = findViewById(R.id.text_view_activity_author_username);
        mProfessionTextView = findViewById(R.id.text_view_activity_author_profession);
        mBooksRecyclerView = findViewById(R.id.recycler_view_activity_author_books);
        mAuthorRecyclerView = findViewById(R.id.recycler_view_activity_author);
        mBackImageView = findViewById(R.id.image_view_toolbar_search);
        mSearchEditText = findViewById(R.id.edit_text_toolbar_search);
        mContactsLinearLayout = findViewById(R.id.linear_layout_activity_author_contacts);
        mAppelImageView = findViewById(R.id.image_view_activity_author_appel);
        mEmailImageView = findViewById(R.id.image_view_activity_author_email);
        mWhatsAppImageView = findViewById(R.id.image_view_activity_author_whatsapp);
        mAuthorFormatBookRecyclerView = findViewById(R.id.recycler_view_activity_author_format_books);

        configureSearchField();
        updateAuthorInfo();
    }

    private void configureSearchField() {
        mSearchEditText.setVisibility(View.GONE);
        mSearchEditText.setSelectAllOnFocus(false);
        mSearchEditText.setFocusable(false);
        mSearchEditText.setHint("  Recherche mes livres");
    }

    private void updateAuthorInfo() {
        mUsernameTextView.setText(mAuthor.getFirstName() + " " + mAuthor.getName());
        mProfessionTextView.setText(mAuthor.getProfession());
    }

    private void setupRecyclerViews() {
        List<Connection> waitList = new ArrayList<>();
        waitList.add(new Connection(getString(R.string.wait), null, true));

        NoConnectionAdapter adapter = new NoConnectionAdapter(waitList);
        mWaitRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mWaitRecyclerView.setAdapter(adapter);

        mAuthorRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAuthorRecyclerView.setAdapter(adapter);
    }

    private void configureContactVisibility() {
        int hiddenCount = 0;

        if ("null".equals(mAuthor.getCall())) {
            mAppelImageView.setVisibility(View.GONE);
            hiddenCount++;
        }

        if ("null".equals(mAuthor.getEmail())) {
            mEmailImageView.setVisibility(View.GONE);
            hiddenCount++;
        }

        if ("null".equals(mAuthor.getWhatsapp())) {
            mWhatsAppImageView.setVisibility(View.GONE);
            hiddenCount++;
        }

        if (hiddenCount == 3) {
            mContactsLinearLayout.setVisibility(View.GONE);
        }
    }

    private void setupClickListeners() {
        mBackImageView.setOnClickListener(v -> onBackPressed());

        mSearchEditText.setOnClickListener(v -> navigateToSearch());

        mAppelImageView.setOnClickListener(v -> {
            mNumberAuthor = mAuthor.getCall();
            initiateCall(mNumberAuthor);
        });

        mEmailImageView.setOnClickListener(v -> sendEmail(
                mAuthor.getEmail(),
                "Sujet : ",
                "Bonjour " + mAuthor.getName() + " " + mAuthor.getFirstName() + ","
        ));

        mWhatsAppImageView.setOnClickListener(v -> sendWhatsAppMessage(
                mAuthor.getWhatsapp(),
                "Bonjour " + mAuthor.getName() + ","
        ));
    }

    private void navigateToSearch() {
        Intent intent = new Intent(this, SearchActivity.class);
        intent.putExtra("search_key", "ONLINE_BOOK");
        intent.putExtra("online_book_key", "AUTHOR_ACTIVITY");
        intent.putExtra("id_author_key", mAuthor.getIdNumber());
        startActivity(intent);
    }

    private void loadAuthorImage() {
        Picasso.get()
                .load(Server.getIpServer(this) + "ressources/profile/" + mAuthor.getProfile())
                .placeholder(R.drawable.img_wait_profile)
                .error(R.drawable.img_wait_profile)
                .transform(new RoundedTransformation(1000, 4))
                .resize(384, 384)
                .into(mProfileImageView);
    }

    private void registerBroadcastReceiver() {
        mNoConnectionReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (ACTION_AUTHOR.equals(intent.getAction())) {
                    handleBroadcastReceived();
                }
            }
        };

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            registerReceiver(mNoConnectionReceiver,
                    new IntentFilter(ACTION_AUTHOR),
                    Context.RECEIVER_EXPORTED);
        }
    }

    private void handleBroadcastReceived() {
        try {
            showLoadingState();
            loadAuthorData();
        } catch (Exception e) {
            Log.e(TAG, "Error handling broadcast", e);
        }
    }

    private void showLoadingState() {
        List<Connection> list = new ArrayList<>();
        list.add(new Connection(getString(R.string.wait), ACTION_AUTHOR, true));

        NoConnectionAdapter adapter = new NoConnectionAdapter(list);
        mWaitRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mWaitRecyclerView.setAdapter(adapter);
    }

    private void loadAuthorData() {
        String baseUrl = Server.getIpServerAndroid(this);
        String idNumber = mSession.getIdNumber();
        String authorId = mAuthor.getIdNumber();

        new AuthorBookSyn().execute(baseUrl + "AuthorBook.php", idNumber, authorId);
        new AuthorSyn().execute(baseUrl + "AuthorSimular.php", authorId);
    }

    // ==================== AsyncTask Classes ====================

    private class AuthorBookSyn extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            return executePostRequest(params[0],
                    new MultipartBody.Builder()
                            .setType(MultipartBody.FORM)
                            .addFormDataPart("idNumber", params[1])
                            .addFormDataPart("idAuthor", params[2])
                            .build());
        }

        @Override
        protected void onPostExecute(String jsonData) {
            if (jsonData != null) {
                processAuthorBooks(jsonData);
            } else {
                showNoConnectionError();
            }
        }

        private void processAuthorBooks(String jsonData) {
            BookStats stats = new BookStats();

            if (!RESPONSE_RAS.equals(jsonData)) {
                mWaitRecyclerView.setVisibility(View.GONE);
                mNestedScrollView.setVisibility(View.VISIBLE);
                mSearchEditText.setVisibility(View.VISIBLE);

                try {
                    JSONArray jsonArray = new JSONArray(jsonData);
                    mOnlineBookList.clear();

                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject obj = jsonArray.getJSONObject(i);

                        OnlineBook book = new OnlineBook(
                                obj.getString("idBook"),
                                obj.getString("blanket"),
                                obj.getString("bookTitle"),
                                obj.getString("categoryTitle"),
                                obj.getString("isPhysic"),
                                obj.getString("electronic"),
                                obj.getString("isAudio"),
                                Integer.parseInt(obj.getString("numberLike")),
                                Integer.parseInt(obj.getString("numberNoLike"))
                        );

                        mOnlineBookList.add(book);
                        stats.updateStats(book);
                    }

                    updateBooksRecyclerView();

                } catch (JSONException e) {
                    Log.e(TAG, "Error parsing author books", e);
                }
            }

            updateFormatBooksRecyclerView(stats);
        }

        private void updateBooksRecyclerView() {
            HorizontaleAdapter adapter = new HorizontaleAdapter(mOnlineBookList);
            mBooksRecyclerView.setLayoutManager(
                    new LinearLayoutManager(AuthorActivity.this,
                            LinearLayoutManager.HORIZONTAL, false));
            mBooksRecyclerView.setAdapter(adapter);
        }

        private void updateFormatBooksRecyclerView(BookStats stats) {
            List<Library> libraryList = new ArrayList<>();
            libraryList.add(new Library(
                    1,
                    R.drawable.fichier_pdf,
                    "Mes livres électroniques",
                    stats.electronic,
                    mAuthor.getIdNumber()
            ));
            libraryList.add(new Library(
                    2,
                    R.drawable.audio,
                    "Mes livres audios",
                    stats.audio,
                    mAuthor.getIdNumber()
            ));
            libraryList.add(new Library(
                    3,
                    R.drawable.books_emp,
                    "Mes livres physiques",
                    stats.physical,
                    mAuthor.getIdNumber()
            ));

            AuthorFormatBookAdapter adapter = new AuthorFormatBookAdapter(libraryList);
            mAuthorFormatBookRecyclerView.setLayoutManager(new LinearLayoutManager(AuthorActivity.this));
            mAuthorFormatBookRecyclerView.setAdapter(adapter);
        }
    }

    private class AuthorSyn extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            return executePostRequest(params[0],
                    new MultipartBody.Builder()
                            .setType(MultipartBody.FORM)
                            .addFormDataPart("idUser", params[1])
                            .build());
        }

        @Override
        protected void onPostExecute(String jsonData) {
            if (jsonData != null) {
                processSimilarAuthors(jsonData);
            } else {
                showAuthorLoadError();
            }
        }

        private void processSimilarAuthors(String jsonData) {
            if (!RESPONSE_RAS.equals(jsonData)) {
                try {
                    JSONArray jsonArray = new JSONArray(jsonData);
                    mAuthorArrayList.clear();

                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject obj = jsonArray.getJSONObject(i);
                        mAuthorArrayList.add(new Author(
                                obj.getString("idAuthor"),
                                obj.getString("name"),
                                obj.getString("firstName"),
                                obj.getString("profile"),
                                obj.getString("profession"),
                                obj.getString("call"),
                                obj.getString("email"),
                                obj.getString("whatsapp")
                        ));
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "Error parsing similar authors", e);
                }
            }

            updateAuthorsRecyclerView();
        }

        private void updateAuthorsRecyclerView() {
            AuthorHorizontaleAdapter adapter = new AuthorHorizontaleAdapter(mAuthorArrayList);
            mAuthorRecyclerView.setLayoutManager(
                    new LinearLayoutManager(AuthorActivity.this,
                            LinearLayoutManager.HORIZONTAL, false));
            mAuthorRecyclerView.setAdapter(adapter);
        }

        private void showAuthorLoadError() {
            List<Connection> list = new ArrayList<>();
            list.add(new Connection(getString(R.string.no_connection_available),
                    ACTION_AUTHOR, false));
            NoConnectionAdapter adapter = new NoConnectionAdapter(list);
            mAuthorRecyclerView.setLayoutManager(
                    new LinearLayoutManager(AuthorActivity.this,
                            LinearLayoutManager.HORIZONTAL, false));
            mAuthorRecyclerView.setAdapter(adapter);
        }
    }

    // ==================== Contact Methods ====================

    private void initiateCall(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isEmpty()) {
            Toast.makeText(this, "Numéro invalide", Toast.LENGTH_SHORT).show();
            return;
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CALL_PHONE}, REQUEST_CALL_PERMISSION);
        } else {
            makeCall(phoneNumber);
        }
    }

    private void makeCall(String phoneNumber) {
        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phoneNumber));
        startActivity(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CALL_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (mNumberAuthor != null) {
                    makeCall(mNumberAuthor);
                }
            } else {
                Toast.makeText(this, "Permission d'appel refusée", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void sendEmail(String recipient, String subject, String message) {
        Uri uri = Uri.parse("mailto:" + recipient);
        Intent intent = new Intent(Intent.ACTION_SENDTO, uri);
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_TEXT, message);

        try {
            startActivity(Intent.createChooser(intent, "Choisir une application de messagerie"));
        } catch (android.content.ActivityNotFoundException e) {
            Toast.makeText(this, "Aucune application email installée", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendWhatsAppMessage(String phoneNumber, String message) {
        try {
            String url = "https://wa.me/" + phoneNumber + "?text=" + Uri.encode(message);
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));

            if (isPackageInstalled(WHATSAPP_PACKAGE)) {
                intent.setPackage(WHATSAPP_PACKAGE);
            } else if (isPackageInstalled(WHATSAPP_BUSINESS_PACKAGE)) {
                intent.setPackage(WHATSAPP_BUSINESS_PACKAGE);
            } else {
                Toast.makeText(this, "Aucune version de WhatsApp n'est installée",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            startActivity(intent);

        } catch (Exception e) {
            Toast.makeText(this, "Erreur lors de l'ouverture de WhatsApp",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isPackageInstalled(String packageName) {
        try {
            getPackageManager().getPackageInfo(packageName, 0);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // ==================== Helper Methods ====================

    private String executePostRequest(String url, RequestBody requestBody) {
        try {
            Request request = new Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .build();

            try (Response response = mHttpClient.newCall(request).execute()) {
                if (response.body() != null) {
                    return response.body().string();
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "Network error: " + e.getMessage(), e);
        } catch (Exception e) {
            Log.e(TAG, "Unexpected error: " + e.getMessage(), e);
        }
        return null;
    }

    private void showNoConnectionError() {
        List<Connection> list = new ArrayList<>();
        list.add(new Connection(getString(R.string.no_connection_available),
                ACTION_AUTHOR, false));
        NoConnectionAdapter adapter = new NoConnectionAdapter(list);
        mWaitRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mWaitRecyclerView.setAdapter(adapter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mNoConnectionReceiver != null) {
            try {
                unregisterReceiver(mNoConnectionReceiver);
            } catch (Exception e) {
                Log.e(TAG, "Error unregistering receiver", e);
            }
        }
    }

    // ==================== Helper Classes ====================

    private static class BookStats {
        int electronic = 0;
        int audio = 0;
        int physical = 0;

        void updateStats(OnlineBook book) {
            if (!"null".equals(book.getElectronic())) {
                electronic++;
            }
            if ("1".equals(book.getIsAudio())) {
                audio++;
            }
            if ("1".equals(book.getIsPhysic())) {
                physical++;
            }
        }
    }
}