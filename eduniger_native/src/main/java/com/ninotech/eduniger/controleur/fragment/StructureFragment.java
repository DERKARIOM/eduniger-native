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
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.ninotech.eduniger.R;
import com.ninotech.eduniger.controleur.adapter.NoConnectionAdapter;
import com.ninotech.eduniger.controleur.adapter.StructureAdapter;
import com.ninotech.eduniger.controleur.adapter.StructureStoryAdapter;
import com.ninotech.eduniger.model.data.Connection;
import com.ninotech.eduniger.model.data.Server;
import com.ninotech.eduniger.model.data.Structure;
import com.ninotech.eduniger.model.data.StructureStory;
import com.ninotech.eduniger.model.table.Session;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class StructureFragment extends Fragment {

    private RecyclerView mStructureRecyclerView;
    private RecyclerView mStoriesRecyclerView;
    private View mSkeletonLoadingContainer;
    private View mNoConnectionContainer;
    private ArrayList<Structure> mStructures;
    private StructureAdapter StructAdapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private Session mSession;

    // Stories
    private final List<StructureStory> mStories = new ArrayList<>();
    private StructureStoryAdapter mStoryAdapter;

    // Animators
    private ValueAnimator mShimmerAnimator;
    private ValueAnimator mArrowAnimator;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_structure, container, false);

        mStructureRecyclerView    = view.findViewById(R.id.recycler_view_fragment_structure);
        mStoriesRecyclerView      = view.findViewById(R.id.recycler_view_stories);
        mSkeletonLoadingContainer = view.findViewById(R.id.skeleton_loading_container);
        mNoConnectionContainer    = view.findViewById(R.id.no_connection_container);
        mSwipeRefreshLayout       = view.findViewById(R.id.swipe_refresh_structure);
        mSession                  = new Session(getContext());
        mStructures               = new ArrayList<>();
        StructAdapter             = new StructureAdapter(mStructures);

        startSkeletonShimmer(mSkeletonLoadingContainer);
        startArrowAnimation();

        setupStories();
        setupBroadcastReceiver();
        loadStructures();
        setupSwipeRefresh();

        return view;
    }

    // ==================== Stories ====================

    private void setupStories() {
        mStories.add(new StructureStory("1", "eduniger.png",  "EduNiger",   true));
        mStories.add(new StructureStory("2", "ninotech.png",  "NinoTech",   true));
        mStories.add(new StructureStory("3", "sosbac.png",    "SOS BAC",    false));
        mStories.add(new StructureStory("4", "uaz.png",       "UAZ",        true));
        mStories.add(new StructureStory("5", "inp.png",       "INP-HB",     false));
        mStories.add(new StructureStory("6", "iftic.png",     "IFTIC",      true));
        mStories.add(new StructureStory("7", "ungestion.png", "UN Gestion", false));

        mStoryAdapter = new StructureStoryAdapter(mStories, story ->
                Toast.makeText(getContext(), "Story : " + story.getName(), Toast.LENGTH_SHORT).show());

        mStoriesRecyclerView.setLayoutManager(
                new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        mStoriesRecyclerView.setAdapter(mStoryAdapter);
    }

    // ==================== États ====================

    private void showLoadingState() {
        mStructureRecyclerView.setVisibility(View.GONE);
        mNoConnectionContainer.setVisibility(View.GONE);
        mSkeletonLoadingContainer.setVisibility(View.VISIBLE);
        startSkeletonShimmer(mSkeletonLoadingContainer);
    }

    private void showContentState() {
        mSkeletonLoadingContainer.setVisibility(View.GONE);
        stopSkeletonShimmer(mSkeletonLoadingContainer);
        mNoConnectionContainer.setVisibility(View.GONE);
        mStructureRecyclerView.setVisibility(View.VISIBLE);
    }

    private void showNoConnectionError() {
        if (!isAdded()) return;
        stopSkeletonShimmer(mSkeletonLoadingContainer);
        mSkeletonLoadingContainer.setVisibility(View.GONE);
        mStructureRecyclerView.setVisibility(View.GONE);
        mNoConnectionContainer.setVisibility(View.VISIBLE);
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
            showLoadingState();
            mStructures.clear();
            loadStructures();
        });
    }

    private void stopRefreshing() {
        if (mSwipeRefreshLayout != null && mSwipeRefreshLayout.isRefreshing()) {
            mSwipeRefreshLayout.setRefreshing(false);
        }
    }

    // ==================== BroadcastReceiver ====================

    private void setupBroadcastReceiver() {
        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if ("CATEGORY_FRAGMENT".equals(intent.getAction())) {
                    try {
                        showLoadingState();
                        mStructures.clear();
                        loadStructures();
                    } catch (Exception e) {
                        Log.e("StructureFragment", e.getMessage());
                    }
                }
            }
        };

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getContext().registerReceiver(receiver,
                    new IntentFilter("CATEGORY_FRAGMENT"),
                    Context.RECEIVER_EXPORTED);
        }
    }

    // ==================== Chargement ====================

    private void loadStructures() {
        new StructureSyn().execute(
                Server.getUrlApi(getContext()) + "structure.php",
                mSession.getIdNumber());
        new StructureSyn2().execute(
                Server.getUrlApi(getContext()) + "StructureMore.php",
                mSession.getIdNumber());
    }

    // ==================== AsyncTask ====================

    private class StructureSyn extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            try {
                OkHttpClient client = new OkHttpClient();
                String url = params[0] + "?id_user=" + params[1];
                Request request = new Request.Builder().url(url).get().build();
                try {
                    Response response = client.newCall(request).execute();
                    assert response.body() != null;
                    return response.body().string();
                } catch (IOException e) {
                    if (isAdded()) Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) { return null; }
            return null;
        }

        @Override
        protected void onPostExecute(String jsonData) {
            if (!isAdded()) return;
            stopRefreshing();

            if (jsonData != null) {
                showContentState();
                if (!jsonData.equals("RAS")) {
                    try {
                        JSONArray jsonArray = new JSONArray(jsonData);
                        for (int i = 0; i < jsonArray.length(); i++) {
                            mStructures.add(new Structure(
                                    jsonArray.getJSONObject(i).getString("id"),
                                    jsonArray.getJSONObject(i).getString("logo"),
                                    jsonArray.getJSONObject(i).getString("nameStruct"),
                                    jsonArray.getJSONObject(i).getString("description"), true,
                                    jsonArray.getJSONObject(i).getString("banner"),
                                    jsonArray.getJSONObject(i).getString("author"),
                                    jsonArray.getJSONObject(i).getString("adhererNumber"),
                                    jsonArray.getJSONObject(i).getString("bookNumber"),
                                    jsonArray.getJSONObject(i).getString("isAdmin")));
                        }
                        mStructureRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                        mStructureRecyclerView.setAdapter(StructAdapter);
                    } catch (JSONException e) { throw new RuntimeException(e); }
                }
            } else {
                showNoConnectionError();
            }
        }
    }

    private class StructureSyn2 extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            try {
                OkHttpClient client = new OkHttpClient();
                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("idUser", params[1])
                        .build();
                Request request = new Request.Builder().url(params[0]).post(requestBody).build();
                try {
                    Response response = client.newCall(request).execute();
                    assert response.body() != null;
                    return response.body().string();
                } catch (IOException e) {
                    if (isAdded()) Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) { return null; }
            return null;
        }

        @Override
        protected void onPostExecute(String jsonData) {
            if (!isAdded()) return;
            stopRefreshing();

            if (jsonData != null && !jsonData.equals("RAS")) {
                try {
                    JSONArray jsonArray = new JSONArray(jsonData);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        if (!isExistsS(mStructures, jsonArray.getJSONObject(i).getString("id")))
                            mStructures.add(new Structure(
                                    jsonArray.getJSONObject(i).getString("id"),
                                    jsonArray.getJSONObject(i).getString("logo"),
                                    jsonArray.getJSONObject(i).getString("nameStruct"),
                                    jsonArray.getJSONObject(i).getString("description"), false,
                                    jsonArray.getJSONObject(i).getString("banner"),
                                    jsonArray.getJSONObject(i).getString("author"),
                                    jsonArray.getJSONObject(i).getString("adhererNumber"),
                                    jsonArray.getJSONObject(i).getString("bookNumber"), "0"));
                    }
                    mStructureRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                    mStructureRecyclerView.setAdapter(StructAdapter);
                } catch (JSONException e) { throw new RuntimeException(e); }
            } else if (jsonData == null) {
                showNoConnectionError();
            }
        }
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
        if (mShimmerAnimator != null) { mShimmerAnimator.cancel(); mShimmerAnimator = null; }
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
            float f  = (float) anim.getAnimatedValue();
            float dp = requireContext().getResources().getDisplayMetrics().density;
            float t1 = bounce(f), t2 = bounce((f + 0.33f) % 1f), t3 = bounce((f + 0.66f) % 1f);
            float max = 10f;
            arrow1.setTranslationY(t1 * max * dp); arrow2.setTranslationY(t2 * max * dp); arrow3.setTranslationY(t3 * max * dp);
            arrow1.setAlpha(0.25f + t1 * 0.3f);   arrow2.setAlpha(0.55f + t2 * 0.25f);   arrow3.setAlpha(0.85f + t3 * 0.15f);
        });
        mArrowAnimator.start();
    }

    private float bounce(float t) { return (float) Math.sin(t * Math.PI); }

    // ==================== Utilitaire ====================

    public boolean isExistsS(ArrayList<Structure> structures, String id) {
        for (int i = 0; i < structures.size(); i++) {
            if (structures.get(i).getId().equals(id)) return true;
        }
        return false;
    }

    // ==================== Cycle de vie ====================

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mShimmerAnimator != null) { mShimmerAnimator.cancel(); mShimmerAnimator = null; }
        if (mArrowAnimator != null)   { mArrowAnimator.cancel();   mArrowAnimator = null;   }
    }
}