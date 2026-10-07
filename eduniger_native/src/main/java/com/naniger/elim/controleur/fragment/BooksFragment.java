package com.naniger.elim.controleur.fragment;

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
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.naniger.elim.R;
import com.naniger.elim.controleur.adapter.OnlineBookAdapter;
import com.naniger.elim.model.data.OnlineBook;
import com.naniger.elim.model.data.Server;
import com.naniger.elim.model.table.Session;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.naniger.elim.model.net.ApiClient;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class BooksFragment extends Fragment {

    private static final String TAG = "BooksFragment";
    private static final String ACTION_RANKING = "RANKING_FRAGMENT";
    private static final String RESPONSE_RAS = "RAS";

    // Views
    private RecyclerView mBookRecyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private View mSkeletonLoadingContainer;
    private View mNoConnectionContainer;

    // Data
    private final List<OnlineBook> mOnlineBookList = new ArrayList<>();
    private Session mSession;

    // Utils
    private OkHttpClient mHttpClient;
    private BroadcastReceiver mNoConnectionReceiver;
    private ValueAnimator mShimmerAnimator;
    private ValueAnimator mArrowAnimator;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHttpClient = ApiClient.getInstance(requireContext());
        mSession = new Session(requireContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_books, container, false);

        initializeViews(view);
        setupSwipeRefresh();
        registerBroadcastReceiver();
        loadRankingData();

        return view;
    }

    private void initializeViews(View view) {
        mBookRecyclerView       = view.findViewById(R.id.recycler_view_ranking);
        mSwipeRefreshLayout     = view.findViewById(R.id.swipe_refresh_books);
        mSkeletonLoadingContainer = view.findViewById(R.id.skeleton_loading_container);
        mNoConnectionContainer  = view.findViewById(R.id.no_connection_container);

        // Bouton Réessayer
        Button retryButton = mNoConnectionContainer.findViewById(R.id.button_adapter_no_connection_re_load);
        retryButton.setOnClickListener(v -> {
            mOnlineBookList.clear();
            showLoadingState();
            loadRankingData();
        });

        // Lancer le shimmer dès le départ
        startSkeletonShimmer(mSkeletonLoadingContainer);
        startArrowAnimation();
    }

    private void startArrowAnimation() {
        if (mNoConnectionContainer == null) return;

        View arrow1 = mNoConnectionContainer.findViewById(R.id.arrow_1);
        View arrow2 = mNoConnectionContainer.findViewById(R.id.arrow_2);
        View arrow3 = mNoConnectionContainer.findViewById(R.id.arrow_3);

        if (arrow1 == null || arrow2 == null || arrow3 == null) return;

        // Animation de translation Y en boucle (effet cascade vers le bas)
        mArrowAnimator = ValueAnimator.ofFloat(0f, 1f);
        mArrowAnimator.setDuration(1000);
        mArrowAnimator.setRepeatCount(ValueAnimator.INFINITE);
        mArrowAnimator.setRepeatMode(ValueAnimator.RESTART);
        mArrowAnimator.setInterpolator(new android.view.animation.AccelerateDecelerateInterpolator());

        mArrowAnimator.addUpdateListener(anim -> {
            float f = (float) anim.getAnimatedValue();

            // Chaque flèche décalée dans le temps (cascade)
            float t1 = bounce(f);                           // flèche 1 : en avance
            float t2 = bounce((f + 0.33f) % 1f);           // flèche 2 : +33%
            float t3 = bounce((f + 0.66f) % 1f);           // flèche 3 : +66%

            float maxTranslation = 10f; // dp en pixels
            float dp = requireContext().getResources().getDisplayMetrics().density;

            arrow1.setTranslationY(t1 * maxTranslation * dp);
            arrow2.setTranslationY(t2 * maxTranslation * dp);
            arrow3.setTranslationY(t3 * maxTranslation * dp);

            // Opacité qui suit le mouvement
            arrow1.setAlpha(0.25f + t1 * 0.3f);
            arrow2.setAlpha(0.55f + t2 * 0.25f);
            arrow3.setAlpha(0.85f + t3 * 0.15f);
        });

        mArrowAnimator.start();
    }

    private float bounce(float t) {
        return (float) Math.sin(t * Math.PI);
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
            mOnlineBookList.clear();
            loadRankingData();
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
        mBookRecyclerView.setVisibility(View.GONE);
        mSkeletonLoadingContainer.setVisibility(View.VISIBLE);
        startSkeletonShimmer(mSkeletonLoadingContainer);
    }

    private void showContentState() {
        mSkeletonLoadingContainer.setVisibility(View.GONE);
        stopSkeletonShimmer(mSkeletonLoadingContainer);
        mNoConnectionContainer.setVisibility(View.GONE);
        mBookRecyclerView.setVisibility(View.VISIBLE);
    }

    private void showNoConnectionError() {
        if (!isAdded()) return;
        stopSkeletonShimmer(mSkeletonLoadingContainer);
        mSkeletonLoadingContainer.setVisibility(View.GONE);
        mBookRecyclerView.setVisibility(View.GONE);
        mNoConnectionContainer.setVisibility(View.VISIBLE);
    }

    // ==================== BroadcastReceiver ====================

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
                    Context.RECEIVER_NOT_EXPORTED);
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

    // ==================== Chargement ====================

    private void loadRankingData() {
        // Ancien : books.php (backend PHP legacy, JWT maison). Bascule vers l'API Laravel
        // reelle (Sanctum, deja authentifiee via ApiClient/AuthInterceptor) qui expose les
        // livres avec leurs associations (categories, auteur, structures, fichiers audio).
        // Limite connue : 'numberLike'/'numberView' n'existent pas encore cote Laravel (le
        // systeme de favoris/vues n'a pas ete migre - cf. audit mobile) -> mis a 0 pour
        // l'instant plutot que de laisser planter le parsing.
        new RankingSyn().execute(Server.getUrlHostProd(requireContext()) + "/api/book/associations");
    }

    // ==================== AsyncTask ====================

    private class RankingSyn extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            return executeGetRequest(params[0]);
        }

        private String executeGetRequest(String url) {
            try {
                Request request = new Request.Builder().url(url).get().build();
                try (Response response = mHttpClient.newCall(request).execute()) {
                    String body = response.body() != null ? response.body().string() : null;
                    if (!response.isSuccessful()) {
                        Log.e(TAG, "GET /api/book/associations a echoue (" + response.code() + "): " + body);
                        return null;
                    }
                    return body;
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
            stopRefreshing();

            if (jsonData != null) {
                processRankingData(jsonData);
            } else {
                showNoConnectionError();
            }
        }

        private void processRankingData(String jsonData) {
            try {
                JSONArray jsonArray = new JSONArray(jsonData);
                showContentState();
                mOnlineBookList.clear();

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject obj = jsonArray.getJSONObject(i);

                    // 'categories' et 'structures' sont des relations many-to-many cote
                    // Laravel (un livre peut appartenir a plusieurs) : on prend la premiere
                    // de chaque pour reconstituer l'etiquette 'Structure : Categorie'
                    // affichee par l'ancien systeme, faute d'un champ plat equivalent.
                    JSONArray categories = obj.optJSONArray("categories");
                    String categoryTitle = (categories != null && categories.length() > 0)
                            ? categories.getJSONObject(0).optString("title", "") : "";
                    JSONArray structures = obj.optJSONArray("structures");
                    String nameStruct = (structures != null && structures.length() > 0)
                            ? structures.getJSONObject(0).optString("nameStruct", "") : "";
                    String idStruct = (structures != null && structures.length() > 0)
                            ? structures.getJSONObject(0).optString("id", "") : "";
                    String category = nameStruct.isEmpty() && categoryTitle.isEmpty() ? ""
                            : nameStruct + " : " + categoryTitle;

                    mOnlineBookList.add(new OnlineBook(
                            obj.optString("idBook", ""),
                            obj.optString("blanket", ""),
                            obj.optString("title", ""),
                            category,
                            obj.optBoolean("isPhysic", false) ? "1" : "0",
                            obj.isNull("electronic") ? "null" : obj.optString("electronic", "null"),
                            obj.optBoolean("isAudio", false) ? "1" : "0",
                            idStruct,
                            0,  // numberLike : favoris/likes pas encore migres cote Laravel
                            0   // numberView : vues pas encore migrees cote Laravel
                    ));
                }

                OnlineBookAdapter adapter = new OnlineBookAdapter(mOnlineBookList);
                mBookRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
                mBookRecyclerView.setAdapter(adapter);

            } catch (JSONException e) {
                Log.e(TAG, "Reponse inattendue (non-JSONArray) pour /api/book/associations: " + jsonData, e);
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

    // ==================== Cycle de vie ====================

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mShimmerAnimator != null) {
            mShimmerAnimator.cancel();
            mShimmerAnimator = null;
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