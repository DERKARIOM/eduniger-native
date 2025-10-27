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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ninotech.fabi.R;
import com.ninotech.fabi.controleur.activity.LoginActivity;
import com.ninotech.fabi.controleur.activity.SearchActivity;
import com.ninotech.fabi.controleur.adapter.AuthorHorizontaleAdapter;
import com.ninotech.fabi.controleur.adapter.HorizontaleAdapter;
import com.ninotech.fabi.controleur.adapter.NoConnectionAdapter;
import com.ninotech.fabi.controleur.adapter.SemiNoConnectionAdapter;
import com.ninotech.fabi.controleur.adapter.StructureAdapter;
import com.ninotech.fabi.controleur.animation.RoundedTransformation;
import com.ninotech.fabi.controleur.dialog.UpdateDialog;
import com.ninotech.fabi.model.data.Account;
import com.ninotech.fabi.model.data.Author;
import com.ninotech.fabi.model.data.Connection;
import com.ninotech.fabi.model.data.OnlineBook;
import com.ninotech.fabi.model.data.Server;
import com.ninotech.fabi.model.data.Structure;
import com.ninotech.fabi.model.table.Session;
import com.ninotech.fabi.model.table.UserTable;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment";
    private static final String ACTION_HOME_FRAGMENT = "HOME_FRAGMENT";
    private static final String RESPONSE_RAS = "RAS";
    private static final String RESPONSE_EXPIRED_VERSION = "expiresVersion";

    // Views
    private RecyclerView mBookRecommendedRecyclerView;
    private RecyclerView mServerdRecyclerView;
    private RecyclerView mStructureRecyclerView;
    private RecyclerView mAuthorRecyclerView;
    private RecyclerView mWaitRecyclerView;
    private ImageView mWelcomeImageView;
    private TextView mBookMoreTextView;
    private TextView mStructMoreTextView;
    private TextView mAuthorMoreTextView;
    private NestedScrollView mNestedScrollView;
    private RelativeLayout mMoreStructRelativeLayout;
    private RelativeLayout mMoreAuthorRelativeLayout;

    // Data
    private final List<OnlineBook> mOnlineBookList = new ArrayList<>();
    private final List<Structure> mStructures = new ArrayList<>();
    private final List<Structure> mServers = new ArrayList<>();
    private final List<Author> mAuthorArrayList = new ArrayList<>();
    private final Set<String> mStructureIds = new HashSet<>();

    // Adapters
    private StructureAdapter mStructAdapter;
    private StructureAdapter mServerAdapter;
    private NoConnectionAdapter mNoConnectionAdapter;
    private SemiNoConnectionAdapter mSemiNoConnectionAdapter;

    // Utils
    private Account mAccount;
    private UserTable mUserTable;
    private Session mSession;
    private String mPubData;
    private BroadcastReceiver mReceiver;
    private OkHttpClient mHttpClient;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHttpClient = new OkHttpClient();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        initializeComponents();
        initializeViews(view);
        setupRecyclerViews();
        setupClickListeners();
        registerBroadcastReceiver();
        loadInitialData();

        return view;
    }

    private void initializeComponents() {
        Context context = requireContext();
        mSession = new Session(context);
        mUserTable = new UserTable(context);
        mAccount = new Account();
        mStructAdapter = new StructureAdapter(mStructures);
        mServerAdapter = new StructureAdapter(mServers);
    }

    private void initializeViews(View view) {
        mBookRecommendedRecyclerView = view.findViewById(R.id.recycler_view_ranking);
        mWelcomeImageView = view.findViewById(R.id.image_view_fragment_recommended_welcome);
        mBookMoreTextView = view.findViewById(R.id.text_view_recommended_more);
        mStructMoreTextView = view.findViewById(R.id.text_view_recommended_more_structure);
        mAuthorMoreTextView = view.findViewById(R.id.text_view_recommended_more_author);
        mStructureRecyclerView = view.findViewById(R.id.recycler_view_fragment_recommended_structure);
        mAuthorRecyclerView = view.findViewById(R.id.recycler_view_author);
        mWaitRecyclerView = view.findViewById(R.id.recycler_view_fragment_recommended_wait);
        mServerdRecyclerView = view.findViewById(R.id.recycler_view_fragment_home_server);
        mNestedScrollView = view.findViewById(R.id.nested_scroll_view_fragment_home);
        mMoreStructRelativeLayout = view.findViewById(R.id.relative_layout_fragment_home_more_structure);
        mMoreAuthorRelativeLayout = view.findViewById(R.id.relative_layout_fragment_home_more_author);

        mMoreStructRelativeLayout.setVisibility(View.GONE);
        mStructureRecyclerView.setVisibility(View.GONE);
        mAuthorRecyclerView.setVisibility(View.GONE);
        mMoreAuthorRelativeLayout.setVisibility(View.GONE);
    }

    private void setupRecyclerViews() {
        Context context = requireContext();

        // Wait RecyclerView
        List<Connection> waitList = new ArrayList<>();
        waitList.add(new Connection(getString(R.string.wait), ACTION_HOME_FRAGMENT, true));

        mNoConnectionAdapter = new NoConnectionAdapter(waitList);
        mWaitRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        mWaitRecyclerView.setAdapter(mNoConnectionAdapter);

        // Structure RecyclerView
        mSemiNoConnectionAdapter = new SemiNoConnectionAdapter(waitList);
        mStructureRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        mStructureRecyclerView.setAdapter(mSemiNoConnectionAdapter);

        // Author RecyclerView
        mAuthorRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        mAuthorRecyclerView.setAdapter(mNoConnectionAdapter);
    }

    private void setupClickListeners() {
        mBookMoreTextView.setOnClickListener(v -> navigateToSearch("ONLINE_BOOK"));
        mStructMoreTextView.setOnClickListener(v -> navigateToSearch("STRUCTURE"));
        mAuthorMoreTextView.setOnClickListener(v -> navigateToSearch("AUTHOR_ONLINE"));
    }

    private void navigateToSearch(String searchKey) {
        Intent intent = new Intent(getContext(), SearchActivity.class);
        intent.putExtra("search_key", searchKey);
        intent.putExtra("online_book_key", "MAIN_ACTIVITY");
        startActivity(intent);
    }

    private void registerBroadcastReceiver() {
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (ACTION_HOME_FRAGMENT.equals(intent.getAction())) {
                    handleBroadcastReceived();
                }
            }
        };

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            requireContext().registerReceiver(mReceiver,
                    new IntentFilter(ACTION_HOME_FRAGMENT),
                    Context.RECEIVER_EXPORTED);
        }
    }

    private void handleBroadcastReceived() {
        try {
            showLoadingState();
            loadAllData();
        } catch (Exception e) {
            Log.e(TAG, "Error handling broadcast", e);
        }
    }

    private void showLoadingState() {
        mNestedScrollView.setVisibility(View.GONE);
        mWaitRecyclerView.setVisibility(View.VISIBLE);

        List<Connection> list = new ArrayList<>();
        list.add(new Connection(getString(R.string.wait), ACTION_HOME_FRAGMENT, true));
        NoConnectionAdapter adapter = new NoConnectionAdapter(list);
        mWaitRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mWaitRecyclerView.setAdapter(adapter);
    }

    private void loadInitialData() {
        if (mAccount.isSession(requireContext())) {
            loadAllData();
        }
    }

    private void loadAllData() {
        Context context = requireContext();
        String baseUrl = Server.getIpServerAndroid(context);
        String idNumber = mSession.getIdNumber();
        String version = getString(R.string.app_version);

        new PubSyn().execute(baseUrl + "Pub.php", idNumber, version);
        new RecommendedSyn().execute(baseUrl + "Recommended.php", idNumber, version);
        new StructureSyn().execute(baseUrl + "Structure.php", idNumber);
        new StructureSyn2().execute(baseUrl + "StructureTop.php", idNumber);
        new AuthorSyn().execute(baseUrl + "AuthorTop.php", idNumber);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mReceiver != null) {
            try {
                requireContext().unregisterReceiver(mReceiver);
            } catch (Exception e) {
                Log.e(TAG, "Error unregistering receiver", e);
            }
        }
    }

    // ==================== AsyncTask Classes ====================

    private class PubSyn extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            return executePostRequest(params[0],
                    new MultipartBody.Builder()
                            .setType(MultipartBody.FORM)
                            .addFormDataPart("idNumber", params[1])
                            .addFormDataPart("version", params[2])
                            .build());
        }

        @Override
        protected void onPostExecute(String jsonData) {
            mPubData = jsonData;
        }
    }

    private class RecommendedSyn extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            return executePostRequest(params[0],
                    new MultipartBody.Builder()
                            .setType(MultipartBody.FORM)
                            .addFormDataPart("idNumber", params[1])
                            .addFormDataPart("version", params[2])
                            .build());
        }

        @Override
        protected void onPostExecute(String jsonData) {
            if (!isAdded()) return;

            if (jsonData != null) {
                loadPublicityImage();
                showContentState();

                if (RESPONSE_EXPIRED_VERSION.equals(jsonData)) {
                    showUpdateDialog();
                } else if (!RESPONSE_RAS.equals(jsonData)) {
                    processRecommendedBooks(jsonData);
                    setupServerRecyclerView();
                    mServerdRecyclerView.setVisibility(View.VISIBLE);
                    mStructureRecyclerView.setVisibility(View.VISIBLE);
                }
            } else {
                showNoConnectionState(mWaitRecyclerView);
            }
        }

        private void loadPublicityImage() {
            if (mPubData != null && isAdded()) {
                Picasso.get()
                        .load(Server.getIpServer(requireContext()) + "ressources/pub/" + mPubData)
                        .transform(new RoundedTransformation(200, 10))
                        .resize(6200, 3333)
                        .placeholder(R.drawable.img_wait_pub)
                        .into(mWelcomeImageView);
            }
        }

        private void showContentState() {
            mWaitRecyclerView.setVisibility(View.GONE);
            mNestedScrollView.setVisibility(View.VISIBLE);
        }

        private void processRecommendedBooks(String jsonData) {
            try {
                JSONArray jsonArray = new JSONArray(jsonData);
                mOnlineBookList.clear();

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
                        new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
                mBookRecommendedRecyclerView.setAdapter(adapter);
            } catch (JSONException e) {
                Log.e(TAG, "Error parsing recommended books", e);
            }
        }

        private void setupServerRecyclerView() {
            String isAuthor = mUserTable.getIsAuthor(mSession.getIdNumber());
            mServers.clear();
            mServers.add(createStructure("AddBook", "eduniger.png",
                    "Ajouter un contenu", "@ninotech", "Créez librement", "0"));
            mServerdRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            mServerdRecyclerView.setAdapter(mServerAdapter);
        }

        private Structure createStructure(String id, String logo, String name,
                                          String author, String bookNumber, String isAdmin) {
            return new Structure(id, logo, name, "Description", false, "-1",
                    author, "cati", bookNumber, isAdmin);
        }
    }

    private class StructureSyn extends AsyncTask<String, Void, String> {
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
            if (!isAdded()) return;

            if (jsonData != null && !RESPONSE_RAS.equals(jsonData)) {
                processStructures(jsonData);
                updateStructureView();
            } else if (jsonData == null) {
                showNoConnectionState(mStructureRecyclerView);
            }
        }

        private void processStructures(String jsonData) {
            try {
                JSONArray jsonArray = new JSONArray(jsonData);

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject obj = jsonArray.getJSONObject(i);
                    String id = obj.getString("id");

                    if (!mStructureIds.contains(id)) {
                        mStructureIds.add(id);
                        mStructures.add(new Structure(
                                id,
                                obj.getString("logo"),
                                obj.getString("nameStruct"),
                                obj.getString("description"),
                                true,
                                obj.getString("banner"),
                                obj.getString("author"),
                                obj.getString("adhererNumber"),
                                obj.getString("bookNumber"),
                                obj.getString("isAdmin")
                        ));
                    }
                }
            } catch (JSONException e) {
                Log.e(TAG, "Error parsing structures", e);
            }
        }

        private void updateStructureView() {
            mStructureRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            mStructureRecyclerView.setAdapter(mStructAdapter);
            mMoreStructRelativeLayout.setVisibility(View.VISIBLE);
            mAuthorRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    private class StructureSyn2 extends AsyncTask<String, Void, String> {
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
            if (!isAdded()) return;

            if (jsonData != null) {
                processTopStructures(jsonData);
                mStructureRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                mStructureRecyclerView.setAdapter(mStructAdapter);
            } else {
                showNoConnectionState(mStructureRecyclerView);
            }
        }

        private void processTopStructures(String jsonData) {
            try {
                JSONArray jsonArray = new JSONArray(jsonData);

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject obj = jsonArray.getJSONObject(i);
                    String id = obj.getString("id");

                    if (!mStructureIds.contains(id)) {
                        mStructureIds.add(id);
                        mStructures.add(new Structure(
                                id,
                                obj.getString("logo"),
                                obj.getString("nameStruct"),
                                obj.getString("description"),
                                false,
                                obj.getString("banner"),
                                obj.getString("author"),
                                obj.getString("adhererNumber"),
                                obj.getString("bookNumber"),
                                "0"
                        ));
                    }
                }
            } catch (JSONException e) {
                Log.e(TAG, "Error parsing top structures", e);
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
            if (!isAdded()) return;

            if (jsonData != null) {
                processAuthors(jsonData);
                updateAuthorView();
            } else {
                showNoConnectionState(mAuthorRecyclerView);
            }
        }

        private void processAuthors(String jsonData) {
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
                Log.e(TAG, "Error parsing authors", e);
            }
        }

        private void updateAuthorView() {
            AuthorHorizontaleAdapter adapter = new AuthorHorizontaleAdapter(mAuthorArrayList);
            mAuthorRecyclerView.setLayoutManager(
                    new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
            mAuthorRecyclerView.setAdapter(adapter);
            mMoreAuthorRelativeLayout.setVisibility(View.VISIBLE);
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

    private void showNoConnectionState(RecyclerView recyclerView) {
        List<Connection> list = new ArrayList<>();
        list.add(new Connection(getString(R.string.no_connection_available),
                ACTION_HOME_FRAGMENT, false));

        NoConnectionAdapter adapter = new NoConnectionAdapter(list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
    }

    private void showUpdateDialog() {
        UpdateDialog dialog = new UpdateDialog(requireActivity());
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;

        TextView annuler = dialog.findViewById(R.id.annuler);
        TextView installer = dialog.findViewById(R.id.installer);

        annuler.setOnClickListener(v -> handleLogout(dialog));
        installer.setOnClickListener(v -> openWebsite("https://play.google.com/store/apps/details?id=com.ninotech.fabi&pcampaignid=web_share"));
        dialog.setOnCancelListener(d -> handleLogout(dialog));

        dialog.build();
    }

    private void handleLogout(UpdateDialog dialog) {
        if (mAccount.logout(requireContext())) {
            Intent intent = new Intent(getContext(), LoginActivity.class);
            startActivity(intent);
            requireActivity().finish();
            dialog.cancel();
        }
    }

    private void openWebsite(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(intent);
    }
}