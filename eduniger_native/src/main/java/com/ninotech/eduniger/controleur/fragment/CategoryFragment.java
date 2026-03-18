package com.ninotech.eduniger.controleur.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.ninotech.eduniger.R;
import com.ninotech.eduniger.controleur.adapter.CategoryAdapter;
import com.ninotech.eduniger.controleur.adapter.NoConnectionAdapter;
import com.ninotech.eduniger.model.data.Category;
import com.ninotech.eduniger.model.data.Connection;
import com.ninotech.eduniger.model.data.Server;
import com.ninotech.eduniger.model.table.Session;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class CategoryFragment extends Fragment {

    private static final String TAG = "CategoryFragment";
    private static final String ACTION_CATEGORY = "CATEGORY_FRAGMENT";
    private static final String RESPONSE_RAS = "RAS";

    // Views
    private RecyclerView mCategoryRecyclerView;
    private RecyclerView mWaitRecyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;  // ← nouveau

    // Data
    private final List<Category> mCategoryList = new ArrayList<>();
    private Session mSession;

    // Utils
    private OkHttpClient mHttpClient;
    private BroadcastReceiver mNoConnectionReceiver;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHttpClient = new OkHttpClient();
        mSession = new Session(requireContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_category, container, false);

        initializeViews(view);
        setupRecyclerView();
        setupSwipeRefresh();         // ← nouveau
        registerBroadcastReceiver();
        loadCategoryData();

        return view;
    }

    private void initializeViews(View view) {
        mCategoryRecyclerView = view.findViewById(R.id.recycler_view_fragment_category);
        mWaitRecyclerView     = view.findViewById(R.id.recycler_view_fragment_category_wait);
        mSwipeRefreshLayout   = view.findViewById(R.id.swipe_refresh_category);  // ← nouveau
    }

    // ==================== SwipeRefresh ====================

    private void setupSwipeRefresh() {
        mSwipeRefreshLayout.setColorSchemeResources(
                R.color.purple_200,
                android.R.color.holo_blue_light,
                android.R.color.holo_orange_light
        );

        mSwipeRefreshLayout.setOnRefreshListener(() -> {
            mCategoryList.clear();
            loadCategoryData();
        });
    }

    private void stopRefreshing() {
        if (mSwipeRefreshLayout != null && mSwipeRefreshLayout.isRefreshing()) {
            mSwipeRefreshLayout.setRefreshing(false);
        }
    }

    // ==================== Setup ====================

    private void setupRecyclerView() {
        List<Connection> waitList = new ArrayList<>();
        waitList.add(new Connection(getString(R.string.wait), null, true));

        NoConnectionAdapter adapter = new NoConnectionAdapter(waitList);
        mWaitRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        mWaitRecyclerView.setAdapter(adapter);
    }

    private void registerBroadcastReceiver() {
        mNoConnectionReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (ACTION_CATEGORY.equals(intent.getAction())) {
                    handleBroadcastReceived();
                }
            }
        };

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            requireContext().registerReceiver(mNoConnectionReceiver,
                    new IntentFilter(ACTION_CATEGORY),
                    Context.RECEIVER_EXPORTED);
        }
    }

    private void handleBroadcastReceived() {
        try {
            showLoadingState();
            loadCategoryData();
        } catch (Exception e) {
            Log.e(TAG, "Error handling broadcast", e);
        }
    }

    private void showLoadingState() {
        List<Connection> list = new ArrayList<>();
        list.add(new Connection(getString(R.string.wait), ACTION_CATEGORY, true));

        NoConnectionAdapter adapter = new NoConnectionAdapter(list);
        mWaitRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        mWaitRecyclerView.setAdapter(adapter);
    }

    private void loadCategoryData() {
        new CategorySyn().execute(
                Server.getUrlApi(requireContext()) + "Category.php",
                mSession.getIdNumber()
        );
    }

    // ==================== AsyncTask ====================

    private class CategorySyn extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            return executePostRequest(params[0], params[1]);
        }

        @Override
        protected void onPostExecute(String jsonData) {
            if (!isAdded()) return;

            // ← Arrêter le SwipeRefresh dans tous les cas
            stopRefreshing();

            if (jsonData != null) {
                processCategoryData(jsonData);
            } else {
                showNoConnectionError();
            }
        }

        private void processCategoryData(String jsonData) {
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
                                obj.getString("title")
                        ));
                    }

                    updateRecyclerView();

                } catch (JSONException e) {
                    Log.e(TAG, "Error parsing category data", e);
                }
            }
        }

        private void updateRecyclerView() {
            CategoryAdapter adapter = new CategoryAdapter(mCategoryList);
            mCategoryRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
            mCategoryRecyclerView.setAdapter(adapter);
        }
    }

    // ==================== Helper Methods ====================

    private String executePostRequest(String url, String idNumber) {
        try {
            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("idNumber", idNumber)
                    .build();

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
        list.add(new Connection(
                getString(R.string.no_connection_available),
                ACTION_CATEGORY,
                false
        ));

        NoConnectionAdapter adapter = new NoConnectionAdapter(list);
        mWaitRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        mWaitRecyclerView.setAdapter(adapter);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mNoConnectionReceiver != null) {
            try {
                requireContext().unregisterReceiver(mNoConnectionReceiver);
            } catch (Exception e) {
                Log.e(TAG, "Error unregistering receiver", e);
            }
        }
    }
}