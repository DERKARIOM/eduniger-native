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
import com.ninotech.eduniger.controleur.adapter.NoConnectionAdapter;
import com.ninotech.eduniger.controleur.adapter.OnlineBookAdapter;
import com.ninotech.eduniger.model.data.Connection;
import com.ninotech.eduniger.model.data.OnlineBook;
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

public class BooksFragment extends Fragment {

    private static final String TAG = "BooksFragment";
    private static final String ACTION_RANKING = "RANKING_FRAGMENT";
    private static final String RESPONSE_RAS = "RAS";

    // Views
    private RecyclerView mBookRecyclerView;
    private RecyclerView mWaitRecyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;  // ← nouveau

    // Data
    private final List<OnlineBook> mOnlineBookList = new ArrayList<>();
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
        View view = inflater.inflate(R.layout.fragment_books, container, false);

        initializeViews(view);
        setupRecyclerView();
        setupSwipeRefresh();         // ← nouveau
        registerBroadcastReceiver();
        loadRankingData();

        return view;
    }

    private void initializeViews(View view) {
        mBookRecyclerView  = view.findViewById(R.id.recycler_view_ranking);
        mWaitRecyclerView  = view.findViewById(R.id.recycler_view_fragment_books_wait);
        mSwipeRefreshLayout = view.findViewById(R.id.swipe_refresh_books);  // ← nouveau
    }

    // ==================== SwipeRefresh ====================

    private void setupSwipeRefresh() {
        mSwipeRefreshLayout.setColorSchemeResources(
                R.color.purple_200,
                android.R.color.holo_blue_light,
                android.R.color.holo_orange_light
        );

        mSwipeRefreshLayout.setOnRefreshListener(() -> {
            mOnlineBookList.clear();
            loadRankingData();
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
                if (ACTION_RANKING.equals(intent.getAction())) {
                    handleBroadcastReceived();
                }
            }
        };

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            requireContext().registerReceiver(mNoConnectionReceiver,
                    new IntentFilter(ACTION_RANKING),
                    Context.RECEIVER_EXPORTED);
        }
    }

    private void handleBroadcastReceived() {
        try {
            showLoadingState();
            loadRankingData();
        } catch (Exception e) {
            Log.e(TAG, "Error handling broadcast", e);
        }
    }

    private void showLoadingState() {
        List<Connection> list = new ArrayList<>();
        list.add(new Connection(getString(R.string.wait), ACTION_RANKING, true));

        NoConnectionAdapter adapter = new NoConnectionAdapter(list);
        mWaitRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        mWaitRecyclerView.setAdapter(adapter);
    }

    private void loadRankingData() {
        new RankingSyn().execute(
                Server.getUrlApi(requireContext()) + "books.php",
                mSession.getIdNumber()
        );
    }

    // ==================== AsyncTask ====================

    private class RankingSyn extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String url = params[0] + "?id_number=" + params[1];
            return executeGetRequest(url);
        }

        private String executeGetRequest(String url) {
            try {
                Request request = new Request.Builder()
                        .url(url)
                        .get()
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

        @Override
        protected void onPostExecute(String jsonData) {
            if (!isAdded()) return;

            // ← Arrêter le SwipeRefresh dans tous les cas
            stopRefreshing();

            if (jsonData != null) {
                processRankingData(jsonData);
            } else {
                showNoConnectionError();
            }
        }

        private void processRankingData(String jsonData) {
            mWaitRecyclerView.setVisibility(View.GONE);
            mBookRecyclerView.setVisibility(View.VISIBLE);

            if (!RESPONSE_RAS.equals(jsonData)) {
                try {
                    JSONArray jsonArray = new JSONArray(jsonData);
                    mOnlineBookList.clear();

                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject obj = jsonArray.getJSONObject(i);

                        String category = obj.getString("nameStruct") + " : " +
                                obj.getString("categoryTitle");

                        mOnlineBookList.add(new OnlineBook(
                                obj.getString("idBook"),
                                obj.getString("blanket"),
                                obj.getString("bookTitle"),
                                category,
                                obj.getString("isPhysic"),
                                obj.getString("electronic"),
                                obj.getString("isAudio"),
                                Integer.parseInt(obj.getString("numberLike")),
                                Integer.parseInt(obj.getString("numberView"))
                        ));
                    }

                    updateRecyclerView();

                } catch (JSONException e) {
                    Log.e(TAG, "Error parsing ranking data", e);
                }
            }
        }

        private void updateRecyclerView() {
            OnlineBookAdapter adapter = new OnlineBookAdapter(mOnlineBookList);
            mBookRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
            mBookRecyclerView.setAdapter(adapter);
        }
    }

    // ==================== Helper Methods ====================

    private String executePostRequest(String url, String idNumber) {
        try {
            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("id_number", idNumber)
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
                ACTION_RANKING,
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