package com.ninotech.eduniger.controleur.activity;

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
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ninotech.eduniger.R;
import com.ninotech.eduniger.controleur.adapter.AuthorHorizontaleAdapter;
import com.ninotech.eduniger.controleur.adapter.CategoryAdapter;
import com.ninotech.eduniger.controleur.adapter.HorizontaleAdapter;
import com.ninotech.eduniger.controleur.adapter.NoConnectionAdapter;
import com.ninotech.eduniger.controleur.adapter.SemiNoConnectionAdapter;
import com.ninotech.eduniger.controleur.animation.RoundedTransformation;
import com.ninotech.eduniger.controleur.dialog.SimpleOkDialog;
import com.ninotech.eduniger.controleur.dialog.StructDeleteDialog;
import com.ninotech.eduniger.model.data.Author;
import com.ninotech.eduniger.model.data.Category;
import com.ninotech.eduniger.model.data.Connection;
import com.ninotech.eduniger.model.data.OnlineBook;
import com.ninotech.eduniger.model.data.PasswordUtil;
import com.ninotech.eduniger.model.data.Server;
import com.ninotech.eduniger.model.data.Structure;
import com.ninotech.eduniger.model.table.Session;
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

public class StructureActivity extends AppCompatActivity {

    private static final String TAG = "StructureActivity";
    private static final String ACTION_STRUCTURE = "STRUCTURE_ACTIVITY";
    private static final String RESPONSE_RAS = "RAS";
    private static final String EXCLUSIVE_STRUCTURE_ID = "2";

    // Views
    private NestedScrollView mNestedScrollView;
    private RecyclerView mWaitRecyclerView;
    private RecyclerView mBookRecommendedRecyclerView;
    private RecyclerView mAuthorRecyclerView;
    private RecyclerView mCategoryRecyclerView;
    private ImageView mWelcomeImageView;
    private ImageView mProfileImageView;
    private ImageView mBackImageView;
    private TextView mNameTextView;
    private TextView mAuthorTextView;
    private TextView mNumberTextView;
    private TextView mDescriptionTextView;
    private TextView mMoreDescTextView;
    private TextView mReduceTextView;
    private TextView mMoreBookTextView;
    private TextView mMoreCategorie;
    private TextView mMoreAuthorTextView;
    private Button mAdhererButton;
    private EditText mSearchEditText;
    private RelativeLayout mMoreAuthorRelativeLayout;

    // Data
    private final List<OnlineBook> mOnlineBookList = new ArrayList<>();
    private final List<Author> mAuthorArrayList = new ArrayList<>();
    private final List<Category> mCategoryList = new ArrayList<>();
    private Structure mStructure;
    private Session mSession;

    // Utils
    private OkHttpClient mHttpClient;
    private BroadcastReceiver mNoConnectionReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_structure);
        Objects.requireNonNull(getSupportActionBar()).hide();

        initializeComponents();
        initializeViews();
        setupRecyclerViews();
        setupClickListeners();
        loadStructureImages();
        registerBroadcastReceiver();
        loadStructureData();
    }

    private void initializeComponents() {
        mSession = new Session(this);
        mHttpClient = new OkHttpClient();
        mStructure = extractStructureFromIntent();
    }

    private Structure extractStructureFromIntent() {
        Intent intent = getIntent();
        return new Structure(
                intent.getStringExtra("intent_structure_adapter_id"),
                intent.getStringExtra("intent_structure_adapter_logo"),
                intent.getStringExtra("intent_structure_adapter_name"),
                intent.getStringExtra("intent_structure_adapter_description"),
                intent.getBooleanExtra("intent_structure_adapter_is_adhere", false),
                intent.getStringExtra("intent_structure_adapter_banner"),
                intent.getStringExtra("intent_structure_adapter_author"),
                intent.getStringExtra("intent_structure_adapter_adherer_number"),
                intent.getStringExtra("intent_structure_adapter_book_number"),
                intent.getStringExtra("intent_structure_adapter_admin")
        );
    }

    private void initializeViews() {
        mWaitRecyclerView = findViewById(R.id.recycler_view_activity_structure_wait);
        mNestedScrollView = findViewById(R.id.nested_scroll_view_activity_structure);
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
        mMoreCategorie = findViewById(R.id.text_view_activity_structure_more_category);
        mMoreAuthorTextView = findViewById(R.id.text_view_activity_structure_more_author);
        mAdhererButton = findViewById(R.id.button_activity_structure_adherer);
        mBookRecommendedRecyclerView = findViewById(R.id.recycler_view_activity_structure_books);
        mMoreAuthorRelativeLayout = findViewById(R.id.relative_layout_activity_structure_author);
        mCategoryRecyclerView = findViewById(R.id.recycler_view_activity_structure_category);

        updateStructureInfo();
        configureSearchField();
    }

    private void updateStructureInfo() {
        mNameTextView.setText(mStructure.getName());
        mAuthorTextView.setText("@" + mStructure.getAuthor());
        mNumberTextView.setText(
                mStructure.getAdhererNumber() + " Adhérents ° " +
                        mStructure.getBookNumber() + " Livres"
        );
        mDescriptionTextView.setText("Bienvenue sur la structure " + mStructure.getName() + "!");

        if (mStructure.isAdhere()) {
            mAdhererButton.setText("Se détacher");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                mAdhererButton.setBackgroundTintList(
                        ColorStateList.valueOf(ContextCompat.getColor(this, R.color.black3)));
            }
        }
    }

    private void configureSearchField() {
        mSearchEditText.setVisibility(View.GONE);
        mSearchEditText.setSelectAllOnFocus(false);
        mSearchEditText.setFocusable(false);
        mSearchEditText.setHint("  Recherche nos livres");
    }

    private void setupRecyclerViews() {
        List<Connection> waitList = new ArrayList<>();
        waitList.add(new Connection(getString(R.string.wait), null, true));

        NoConnectionAdapter noConnectionAdapter = new NoConnectionAdapter(waitList);
        mWaitRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mWaitRecyclerView.setAdapter(noConnectionAdapter);

        SemiNoConnectionAdapter semiNoConnectionAdapter = new SemiNoConnectionAdapter(waitList);
        mAuthorRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAuthorRecyclerView.setAdapter(semiNoConnectionAdapter);
    }

    private void setupClickListeners() {
        mBackImageView.setOnClickListener(v -> onBackPressed());

        mSearchEditText.setOnClickListener(v -> navigateToSearch(
                "ONLINE_BOOK", "STRUCTURE_ACTIVITY", mStructure.getId()));

        mMoreDescTextView.setOnClickListener(v -> expandDescription());
        mReduceTextView.setOnClickListener(v -> collapseDescription());
        mAdhererButton.setOnClickListener(v -> handleAdhererAction());

        mMoreBookTextView.setOnClickListener(v -> navigateToSearch(
                "ONLINE_BOOK", "STRUCTURE_ACTIVITY", mStructure.getId()));

        mMoreCategorie.setOnClickListener(v -> navigateToSearch(
                "STRUCT_CATEGORY", "STRUCTURE_CATEGORIE", null));

        mMoreAuthorTextView.setOnClickListener(v -> navigateToSearch(
                "AUTHOR_ONLINE", "MAIN_ACTIVITY", null));
    }

    private void navigateToSearch(String searchKey, String onlineBookKey, String structId) {
        Intent intent = new Intent(this, SearchActivity.class);
        intent.putExtra("search_key", searchKey);
        intent.putExtra("online_book_key", onlineBookKey);
        if (structId != null) {
            intent.putExtra("id_struct_key", structId);
        }
        startActivity(intent);
    }

    private void expandDescription() {
        mDescriptionTextView.setText(mStructure.getDescription());
        mMoreDescTextView.setVisibility(View.GONE);
        mReduceTextView.setVisibility(View.VISIBLE);
    }

    private void collapseDescription() {
        mReduceTextView.setVisibility(View.GONE);
        mMoreDescTextView.setVisibility(View.VISIBLE);
        mDescriptionTextView.setText("Bienvenue sur la structure " + mStructure.getName() + "!");
    }

    private void handleAdhererAction() {
        if (EXCLUSIVE_STRUCTURE_ID.equals(mStructure.getId())) {
            showExclusiveStructureDialog();
        } else if ("Se détacher".equals(mAdhererButton.getText().toString())) {
            showStructDeleteDialog(mStructure.getId());
        } else {
            adhereToStructure();
        }
    }

    private void showExclusiveStructureDialog() {
        showSimpleDialog(
                R.drawable.vector_purple_200_desole,
                "Structure Exclusive",
                "Cette structure est exclusivement réservée aux étudiants de la FAST UAM. " +
                        "Veuillez vérifier que vous remplissez les critères d'adhésion puis contacter " +
                        "les numéros suivants :\n+22796627534 / +22794961793."
        );
    }

    private void adhereToStructure() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mAdhererButton.setBackgroundTintList(
                    ColorStateList.valueOf(ContextCompat.getColor(this, R.color.black3)));
        }
        mAdhererButton.setText("Se détacher");
        mStructure.setAdhere(true);

        new DetachStructSyn().execute(
                Server.getUrlApi(this) + "adherer_struct.php",
                mSession.getIdNumber(),
                mStructure.getId()
        );
    }

    private void loadStructureImages() {
        Picasso.get()
                .load(Server.getUrlServer(this) + "ressources/baniere/" + mStructure.getBanner())
                .transform(new RoundedTransformation(200, 10))
                .resize(6200, 2222)
                .placeholder(R.drawable.img_wait_banner)
                .error(R.drawable.img_wait_banner)
                .into(mWelcomeImageView);

        mWelcomeImageView.setVisibility(View.VISIBLE);

        Picasso.get()
                .load(Server.getUrlServer(this) + "ressources/cover/" + mStructure.getCover())
                .placeholder(R.drawable.img_wait_struct)
                .error(R.drawable.img_default_book)
                .transform(new RoundedTransformation(1000, 4))
                .resize(284, 284)
                .into(mProfileImageView);
    }

    private void registerBroadcastReceiver() {
        mNoConnectionReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (ACTION_STRUCTURE.equals(intent.getAction())) {
                    showLoadingState();
                    loadStructureData();
                }
            }
        };

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            registerReceiver(mNoConnectionReceiver,
                    new IntentFilter(ACTION_STRUCTURE),
                    Context.RECEIVER_EXPORTED);
        }
    }

    private void showLoadingState() {
        mNestedScrollView.setVisibility(View.GONE);
        mWaitRecyclerView.setVisibility(View.VISIBLE);

        List<Connection> list = new ArrayList<>();
        list.add(new Connection(getString(R.string.wait), ACTION_STRUCTURE, true));
        NoConnectionAdapter adapter = new NoConnectionAdapter(list);
        mWaitRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mWaitRecyclerView.setAdapter(adapter);
    }

    private void loadStructureData() {
        String baseUrl = Server.getUrlApi(this);
        String idNumber = mSession.getIdNumber();
        String structId = mStructure.getId();

        new StructBookSyn().execute(baseUrl + "StructBook.php", idNumber, structId);
        new CategorySyn().execute(baseUrl + "CategoryStrut.php", idNumber);
        new AuthorSyn().execute(baseUrl + "AuthorTop.php", idNumber);
    }

    // ==================== AsyncTask Classes ====================

    private class StructBookSyn extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            return executePostRequest(params[0],
                    new MultipartBody.Builder()
                            .setType(MultipartBody.FORM)
                            .addFormDataPart("idNumber", params[1])
                            .addFormDataPart("idStruct", params[2])
                            .build());
        }

        @Override
        protected void onPostExecute(String jsonData) {
            if (jsonData != null) {
                processBookData(jsonData);
            } else {
                showNoConnectionError();
            }
        }

        private void processBookData(String jsonData) {
            mWaitRecyclerView.setVisibility(View.GONE);
            mNestedScrollView.setVisibility(View.VISIBLE);
            mSearchEditText.setVisibility(View.VISIBLE);

            if (!RESPONSE_RAS.equals(jsonData)) {
                try {
                    JSONArray jsonArray = new JSONArray(jsonData);
                    mOnlineBookList.clear();

                    // Add "Add Book" option for admins
                    if (mStructure.isAdhere() && "1".equals(mStructure.getAdmin())) {
                        mOnlineBookList.add(new OnlineBook(
                                "add", "addbook.png", "Ajouter un livre",
                                "EduNiger", "oui", "oui", "oui", 9, 9
                        ));
                    }

                    // Add books from server
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject obj = jsonArray.getJSONObject(i);
                        int numberLike = Integer.parseInt(obj.getString("numberLike"));

                        mOnlineBookList.add(new OnlineBook(
                                obj.getString("idBook"),
                                obj.getString("blanket"),
                                obj.getString("bookTitle"),
                                obj.getString("categoryTitle"),
                                obj.getString("isPhysic"),
                                obj.getString("electronic"),
                                obj.getString("isAudio"),
                                numberLike,
                                numberLike
                        ));
                    }

                    HorizontaleAdapter adapter = new HorizontaleAdapter(mOnlineBookList);
                    mBookRecommendedRecyclerView.setLayoutManager(
                            new LinearLayoutManager(StructureActivity.this,
                                    LinearLayoutManager.HORIZONTAL, false));
                    mBookRecommendedRecyclerView.setAdapter(adapter);

                } catch (JSONException e) {
                    Log.e(TAG, "Error parsing book data", e);
                }
            }
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
                processAuthors(jsonData);
            } else {
                showAuthorLoadError();
            }
        }

        private void processAuthors(String jsonData) {
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

                    AuthorHorizontaleAdapter adapter = new AuthorHorizontaleAdapter(mAuthorArrayList);
                    mAuthorRecyclerView.setLayoutManager(
                            new LinearLayoutManager(StructureActivity.this,
                                    LinearLayoutManager.HORIZONTAL, false));
                    mAuthorRecyclerView.setAdapter(adapter);
                    mMoreAuthorRelativeLayout.setVisibility(View.VISIBLE);

                } catch (JSONException e) {
                    Log.e(TAG, "Error parsing author data", e);
                }
            }
        }

        private void showAuthorLoadError() {
            List<Connection> list = new ArrayList<>();
            list.add(new Connection(getString(R.string.no_connection_available),
                    ACTION_STRUCTURE, false));
            SemiNoConnectionAdapter adapter = new SemiNoConnectionAdapter(list);
            mAuthorRecyclerView.setLayoutManager(
                    new LinearLayoutManager(StructureActivity.this,
                            LinearLayoutManager.HORIZONTAL, false));
            mAuthorRecyclerView.setAdapter(adapter);
        }
    }

    private class CategorySyn extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            return executePostRequest(params[0],
                    new MultipartBody.Builder()
                            .setType(MultipartBody.FORM)
                            .addFormDataPart("idNumber", params[1])
                            .build());
        }

        @Override
        protected void onPostExecute(String jsonData) {
            if (jsonData != null) {
                processCategories(jsonData);
            } else {
                showCategoryLoadError();
            }
        }

        private void processCategories(String jsonData) {
            mWaitRecyclerView.setVisibility(View.GONE);
            mCategoryRecyclerView.setVisibility(View.VISIBLE);

            if (!RESPONSE_RAS.equals(jsonData)) {
                try {
                    JSONArray jsonArray = new JSONArray(jsonData);
                    mCategoryList.clear();

                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject obj = jsonArray.getJSONObject(i);
                        mCategoryList.add(new Category(
                                obj.getString("blanket"),
                                obj.getString("title"),
                                mStructure.getName()
                        ));
                    }

                    CategoryAdapter adapter = new CategoryAdapter(mCategoryList);
                    mCategoryRecyclerView.setLayoutManager(new LinearLayoutManager(StructureActivity.this));
                    mCategoryRecyclerView.setAdapter(adapter);

                } catch (JSONException e) {
                    Log.e(TAG, "Error parsing category data", e);
                }
            }
        }

        private void showCategoryLoadError() {
            List<Connection> list = new ArrayList<>();
            list.add(new Connection(getString(R.string.no_connection_available),
                    "CATEGORY_FRAGMENT", false));
            NoConnectionAdapter adapter = new NoConnectionAdapter(list);
            mWaitRecyclerView.setLayoutManager(new LinearLayoutManager(StructureActivity.this));
            mWaitRecyclerView.setAdapter(adapter);
        }
    }

    private class DetachStructSyn extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            return executePostRequest(params[0],
                    new MultipartBody.Builder()
                            .setType(MultipartBody.FORM)
                            .addFormDataPart("id_user", params[1])
                            .addFormDataPart("id_struct", params[2])
                            .build());
        }

        @Override
        protected void onPostExecute(String jsonData) {
            if (jsonData != null && !RESPONSE_RAS.equals(jsonData) && "true".equals(jsonData)) {
                Toast.makeText(StructureActivity.this,
                        "Structure détachée avec succès", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // ==================== Dialogs ====================

    private void showStructDeleteDialog(String structId) {
        StructDeleteDialog dialog = new StructDeleteDialog(this);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;

        TextView errorTextView = dialog.findViewById(R.id.text_view_dialog_structure_delete_err);
        TextView noTextView = dialog.findViewById(R.id.no);
        TextView yesTextView = dialog.findViewById(R.id.yes);
        EditText passwordEditText = dialog.findViewById(R.id.edit_text_dialog_struct_delete_password);

        noTextView.setOnClickListener(v -> dialog.cancel());

        yesTextView.setOnClickListener(v ->
                handleStructureDetach(dialog, passwordEditText, errorTextView, structId));

        dialog.build();
    }

    private void handleStructureDetach(StructDeleteDialog dialog, EditText passwordEditText,
                                       TextView errorTextView, String structId) {
        String password = passwordEditText.getText().toString();

        if (password.isEmpty()) {
            showPasswordError(passwordEditText, errorTextView, "Veuillez entrer votre mot de passe");
            return;
        }

        if (!Objects.equals(PasswordUtil.hashPassword(password), mSession.getPassword())) {
            showPasswordError(passwordEditText, errorTextView, "Mot de passe incorrect");
            return;
        }

        dialog.cancel();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mAdhererButton.setBackgroundTintList(
                    ColorStateList.valueOf(ContextCompat.getColor(this, R.color.purple_200)));
        }
        mAdhererButton.setText("S'adhérer");
        mStructure.setAdhere(false);

        new DetachStructSyn().execute(
                Server.getUrlApi(this) + "DetachStruct.php",
                mSession.getIdNumber(),
                structId
        );
    }

    private void showPasswordError(EditText passwordEditText, TextView errorTextView, String message) {
        passwordEditText.setBackground(getDrawable(R.drawable.forme_white_radius_100dp_border_rouge));
        errorTextView.setVisibility(View.VISIBLE);
        errorTextView.setText(message);
    }

    private void showSimpleDialog(int iconRes, String title, String message) {
        SimpleOkDialog dialog = new SimpleOkDialog(this);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;

        ImageView iconImageView = dialog.findViewById(R.id.image_view_dialog_simple_ok_icon);
        TextView titleTextView = dialog.findViewById(R.id.text_view_dialog_simple_ok_title);
        TextView messageTextView = dialog.findViewById(R.id.text_view_dialog_simple_ok_message);
        TextView okTextView = dialog.findViewById(R.id.text_view_dialog_simple_ok);

        iconImageView.setImageResource(iconRes);
        titleTextView.setText(title);
        messageTextView.setText(message);
        okTextView.setOnClickListener(v -> dialog.cancel());

        dialog.build();
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
                ACTION_STRUCTURE, false));
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
}