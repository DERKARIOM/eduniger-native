package com.ninotech.eduniger.controleur.fragment;

import android.animation.ValueAnimator;
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
import com.ninotech.eduniger.model.data.Category;
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
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private View mSkeletonLoadingContainer;
    private View mNoConnectionContainer;

    // Data
    private final List<Category> mCategoryList = new ArrayList<>();
    private Session mSession;

    // Utils
    private OkHttpClient mHttpClient;
    private BroadcastReceiver mNoConnectionReceiver;
    private ValueAnimator mShimmerAnimator;
    private ValueAnimator mArrowAnimator;

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
        setupSwipeRefresh();
        registerBroadcastReceiver();
        loadCategoryData();

        return view;
    }

    private void initializeViews(View view) {
        mCategoryRecyclerView     = view.findViewById(R.id.recycler_view_fragment_category);
        mSwipeRefreshLayout       = view.findViewById(R.id.swipe_refresh_category);
        mSkeletonLoadingContainer = view.findViewById(R.id.skeleton_loading_container);
        mNoConnectionContainer    = view.findViewById(R.id.no_connection_container);

        startSkeletonShimmer(mSkeletonLoadingContainer);
        startArrowAnimation();
    }

    // ==================== SwipeRefresh ====================

    private void setupSwipeRefresh() {
        mSwipeRefreshLayout.setColorSchemeResources(
                R.color.purple_200,
                android.R.color.holo_blue_light,
                android.R.color.holo_orange_light
        );

        mSwipeRefreshLayout.setOnRefreshListener(() -> {
            mNoConnectionContainer.setVisibility(View.GONE);
            mCategoryList.clear();
            loadCategoryData();
        });
    }

    private void stopRefreshing() {
        if (mSwipeRefreshLayout != null && mSwipeRefreshLayout.isRefreshing()) {
            mSwipeRefreshLayout.setRefreshing(false);
        }
    }

    // ==================== États ====================

    private void showLoadingState() {
        mNoConnectionContainer.setVisibility(View.GONE);
        mCategoryRecyclerView.setVisibility(View.GONE);
        mSkeletonLoadingContainer.setVisibility(View.VISIBLE);
        startSkeletonShimmer(mSkeletonLoadingContainer);
    }

    private void showContentState() {
        mSkeletonLoadingContainer.setVisibility(View.GONE);
        stopSkeletonShimmer(mSkeletonLoadingContainer);
        mNoConnectionContainer.setVisibility(View.GONE);
        mCategoryRecyclerView.setVisibility(View.VISIBLE);
    }

    private void showNoConnectionError() {
        if (!isAdded()) return;
        stopSkeletonShimmer(mSkeletonLoadingContainer);
        mSkeletonLoadingContainer.setVisibility(View.GONE);
        mCategoryRecyclerView.setVisibility(View.GONE);
        mNoConnectionContainer.setVisibility(View.VISIBLE);
    }

    // ==================== BroadcastReceiver ====================

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

    // ==================== Chargement ====================

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
            stopRefreshing();

            if (jsonData != null) {
                processCategoryData(jsonData);
            } else {
                showNoConnectionError();
            }
        }

        private void processCategoryData(String jsonData) {
            showContentState();

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

                    CategoryAdapter adapter = new CategoryAdapter(mCategoryList);
                    mCategoryRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
                    mCategoryRecyclerView.setAdapter(adapter);

                } catch (JSONException e) {
                    Log.e(TAG, "Error parsing category data", e);
                }
            }
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
                if (response.body() != null) return response.body().string();
            }
        } catch (IOException e) {
            Log.e(TAG, "Network error: " + e.getMessage(), e);
        } catch (Exception e) {
            Log.e(TAG, "Unexpected error: " + e.getMessage(), e);
        }
        return null;
    }

    // ==================== Shimmer ====================

    private void startSkeletonShimmer(View container) {
        if (!(container instanceof ViewGroup)) return;

        List<View> skeletonViews = new ArrayList<>();
        collectSkeletonViews((ViewGroup) container, skeletonViews);

        mShimmerAnimator = ValueAnimator.ofFloat(0f, 1f);
        mShimmerAnimator.setDuration(1200);
        mShimmerAnimator.setRepeatCount(ValueAnimator.INFINITE);
        mShimmerAnimator.setRepeatMode(ValueAnimator.RESTART);
        mShimmerAnimator.addUpdateListener(anim -> {
            float fraction = (float) anim.getAnimatedValue();
            float alpha = 0.4f + 0.6f * (float)(0.5 + 0.5 * Math.sin(fraction * 2 * Math.PI));
            for (View v : skeletonViews) v.setAlpha(alpha);
        });
        mShimmerAnimator.start();
    }

    private void stopSkeletonShimmer(View container) {
        if (mShimmerAnimator != null) {
            mShimmerAnimator.cancel();
            mShimmerAnimator = null;
        }
        if (container instanceof ViewGroup) {
            List<View> views = new ArrayList<>();
            collectSkeletonViews((ViewGroup) container, views);
            for (View v : views) v.setAlpha(1f);
        }
    }

    private void collectSkeletonViews(ViewGroup parent, List<View> out) {
        for (int i = 0; i < parent.getChildCount(); i++) {
            View child = parent.getChildAt(i);
            if (child instanceof ViewGroup) collectSkeletonViews((ViewGroup) child, out);
            else out.add(child);
        }
    }

    // ==================== Arrow Animation ====================

    private void startArrowAnimation() {
        if (mNoConnectionContainer == null) return;

        View arrow1 = mNoConnectionContainer.findViewById(R.id.arrow_1);
        View arrow2 = mNoConnectionContainer.findViewById(R.id.arrow_2);
        View arrow3 = mNoConnectionContainer.findViewById(R.id.arrow_3);

        if (arrow1 == null || arrow2 == null || arrow3 == null) return;

        mArrowAnimator = ValueAnimator.ofFloat(0f, 1f);
        mArrowAnimator.setDuration(1000);
        mArrowAnimator.setRepeatCount(ValueAnimator.INFINITE);
        mArrowAnimator.setRepeatMode(ValueAnimator.RESTART);
        mArrowAnimator.setInterpolator(new android.view.animation.AccelerateDecelerateInterpolator());

        mArrowAnimator.addUpdateListener(anim -> {
            float f = (float) anim.getAnimatedValue();
            float dp = requireContext().getResources().getDisplayMetrics().density;

            float t1 = bounce(f);
            float t2 = bounce((f + 0.33f) % 1f);
            float t3 = bounce((f + 0.66f) % 1f);

            float max = 10f;
            arrow1.setTranslationY(t1 * max * dp);
            arrow2.setTranslationY(t2 * max * dp);
            arrow3.setTranslationY(t3 * max * dp);

            arrow1.setAlpha(0.25f + t1 * 0.3f);
            arrow2.setAlpha(0.55f + t2 * 0.25f);
            arrow3.setAlpha(0.85f + t3 * 0.15f);
        });

        mArrowAnimator.start();
    }

    private float bounce(float t) {
        return (float) Math.sin(t * Math.PI);
    }

    // ==================== Cycle de vie ====================

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mShimmerAnimator != null) {
            mShimmerAnimator.cancel();
            mShimmerAnimator = null;
        }
        if (mArrowAnimator != null) {
            mArrowAnimator.cancel();
            mArrowAnimator = null;
        }
        if (mNoConnectionReceiver != null) {
            try {
                requireContext().unregisterReceiver(mNoConnectionReceiver);
            } catch (Exception e) {
                Log.e(TAG, "Error unregistering receiver", e);
            }
        }
    }
}