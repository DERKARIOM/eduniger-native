package com.naniger.elim.controleur.fragment;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.naniger.elim.R;
import com.naniger.elim.controleur.activity.StructureActivity;
import com.naniger.elim.controleur.adapter.StructureCardAdapter;
import com.naniger.elim.model.cache.ApiCache;
import com.naniger.elim.model.cache.CachedLoader;
import com.naniger.elim.model.data.Server;
import com.naniger.elim.model.structure.StructureInfo;
import com.naniger.elim.model.structure.StructureJson;
import com.naniger.elim.model.structure.StructureListQuery;
import com.naniger.elim.model.structure.StructureText;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Onglet Structures : découvrir les écoles, universités, bibliothèques et organisations d'Elim.
 *
 * <p>Données : {@code GET /api/structures} (une seule requête, partagée avec l'accueil via le
 * cache « cache d'abord » : affichage immédiat des données enregistrées, actualisation en
 * arrière-plan). Recherche par nom ou ville, filtres (« Mes structures », villes réellement
 * renseignées) et tri sont appliqués localement : aucune requête réseau par frappe.
 *
 * <p>Le type de structure n'existe pas en base : aucun filtre « type » n'est proposé.
 */
public class StructureFragment extends Fragment implements StructureCardAdapter.Listener {

    private static final String TAG = "StructureFragment";

    private SwipeRefreshLayout mSwipe;
    private RecyclerView mList;
    private View mHeader;
    private EditText mSearch;
    private View mSearchClear;
    private TextView mSortButton;
    private ChipGroup mFilters;
    private TextView mCount;

    private StructureCardAdapter mAdapter;
    private final List<StructureInfo> mAll = new ArrayList<>();
    private StructureListQuery mQuery = StructureListQuery.initial();
    private boolean mLoaded;     // au moins une réponse (cache ou réseau) affichée
    private boolean mFailed;     // dernier chargement sans aucune donnée
    private boolean mFirstResume = true;
    /** Filtres affichés (évite de recréer les puces quand les données n'ont pas changé). */
    private String mFilterSignature = "";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_structure, container, false);
        mSwipe = view.findViewById(R.id.swipe_refresh_structure);
        mList = view.findViewById(R.id.recycler_view_fragment_structure);

        // Gestionnaire de mise en page AVANT de gonfler l'en-tête avec la liste pour parent.
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
        mList.setLayoutManager(layoutManager);
        mHeader = inflater.inflate(R.layout.item_structures_header, mList, false);
        mSearch = mHeader.findViewById(R.id.edit_text_structures_search);
        mSearchClear = mHeader.findViewById(R.id.image_button_structures_search_clear);
        mSortButton = mHeader.findViewById(R.id.text_view_structures_sort);
        mFilters = mHeader.findViewById(R.id.chip_group_structures_filters);
        mCount = mHeader.findViewById(R.id.text_view_structures_count);

        mAdapter = new StructureCardAdapter(mHeader, this);
        mList.setAdapter(mAdapter);
        mList.setItemAnimator(null);
        mList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView rv, int state) {
                if (state == RecyclerView.SCROLL_STATE_DRAGGING) hideKeyboard();
            }
        });

        mSwipe.setColorSchemeColors(ContextCompat.getColor(requireContext(), R.color.elim_primary));
        mSwipe.setOnRefreshListener(() -> load(true));

        setupSearch();
        updateSortLabel();
        mSortButton.setOnClickListener(this::showSortMenu);
        rebuildFilters();

        render();
        load(false);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mFirstResume) {
            mFirstResume = false;
        } else if (!isHidden()) {
            // Retour d'une fiche : si l'adhésion a changé, la fiche a invalidé le cache de la
            // liste et la réponse fraîche est demandée ; sinon le cache valide est réaffiché.
            load(false);
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        // Onglets gérés par hide()/show() : pas d'onResume en revenant sur l'onglet.
        if (!hidden && isAdded() && mAdapter != null) load(false);
        if (hidden) hideKeyboard();
    }

    // =================================================================== chargement

    private void load(boolean force) {
        Context context = getContext();
        if (context == null) return;
        if (!mLoaded) {
            mFailed = false;
            render();
        }
        ApiCache.loadList(context, StructureText.listUrl(Server.getUrlHostProd(context)), ApiCache.TTL_STRUCTURES,
                force, new CachedLoader.Callback() {
                    @Override
                    public void onCached(String body, long ageMs) {
                        apply(body);
                    }

                    @Override
                    public void onFresh(String body) {
                        apply(body);
                        if (mSwipe != null) mSwipe.setRefreshing(false);
                    }

                    @Override
                    public void onFailure(boolean hadCache) {
                        if (!isAdded()) return;
                        mSwipe.setRefreshing(false);
                        if (mLoaded) {
                            // Données valides déjà affichées : on les garde et on le signale.
                            if (force) Toast.makeText(requireContext(), R.string.offline_cached_notice, Toast.LENGTH_SHORT).show();
                            return;
                        }
                        mFailed = true;
                        render();
                    }
                });
    }

    private void apply(String body) {
        if (!isAdded()) return;
        List<StructureInfo> parsed;
        try {
            parsed = StructureJson.list(body);
        } catch (Exception e) {
            Log.e(TAG, "Réponse /api/structures illisible", e);
            if (!mLoaded) {
                mFailed = true;
                render();
            }
            return;
        }
        mAll.clear();
        mAll.addAll(parsed);
        mLoaded = true;
        mFailed = false;
        rebuildFilters();
        render();
    }

    /** Applique recherche, filtre et tri, puis met à jour la liste et l'état. */
    private void render() {
        if (mAdapter == null) return;
        if (!mLoaded) {
            mCount.setVisibility(View.INVISIBLE);
            mAdapter.submit(new ArrayList<>(), mFailed ? StructureCardAdapter.State.ERROR : StructureCardAdapter.State.LOADING);
            return;
        }
        List<StructureInfo> visible = mQuery.apply(mAll);
        StructureCardAdapter.State state = StructureCardAdapter.State.NONE;
        if (visible.isEmpty()) {
            state = mAll.isEmpty() ? StructureCardAdapter.State.EMPTY : StructureCardAdapter.State.NO_RESULTS;
        }
        mAdapter.submit(visible, state);
        mCount.setText(getResources().getQuantityString(R.plurals.structures_count, visible.size(), visible.size()));
        mCount.setVisibility(visible.isEmpty() ? View.INVISIBLE : View.VISIBLE);
    }

    // =================================================================== recherche, tri, filtres

    private void setupSearch() {
        mSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) { }
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) { }

            @Override
            public void afterTextChanged(Editable s) {
                mSearchClear.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
                if (Objects.equals(s.toString().trim(), mQuery.search)) return;
                mQuery = mQuery.withSearch(s.toString());
                render(); // filtrage local, instantané
            }
        });
        mSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                hideKeyboard();
                return true;
            }
            return false;
        });
        mSearchClear.setOnClickListener(v -> mSearch.setText(""));
    }

    private void showSortMenu(View anchor) {
        PopupMenu menu = new PopupMenu(requireContext(), anchor);
        StructureListQuery.Sort[] sorts = StructureListQuery.Sort.values();
        for (int i = 0; i < sorts.length; i++) {
            menu.getMenu().add(0, i, i, sortLabel(sorts[i])).setCheckable(true).setChecked(sorts[i] == mQuery.sort);
        }
        menu.getMenu().setGroupCheckable(0, true, true);
        menu.setOnMenuItemClickListener(item -> {
            StructureListQuery.Sort s = sorts[item.getItemId()];
            if (s != mQuery.sort) {
                mQuery = mQuery.withSort(s);
                updateSortLabel();
                render();
                mList.scrollToPosition(0);
            }
            return true;
        });
        menu.show();
    }

    private String sortLabel(StructureListQuery.Sort s) {
        switch (s) {
            case RESOURCES: return getString(R.string.structures_sort_resources);
            case MEMBERS: return getString(R.string.structures_sort_members);
            case RECENT: return getString(R.string.structures_sort_recent);
            default: return getString(R.string.structures_sort_name);
        }
    }

    private void updateSortLabel() {
        String label = sortLabel(mQuery.sort);
        mSortButton.setText(getString(R.string.structures_sort, label));
        mSortButton.setContentDescription(getString(R.string.structures_sort, label));
    }

    /**
     * Puces de filtre construites à partir des données : « Toutes », « Mes structures » (si
     * l'utilisateur est membre d'au moins une structure) et les villes réellement renseignées
     * (au moins deux villes différentes, sinon le filtre n'apporterait rien).
     */
    private void rebuildFilters() {
        int mine = StructureListQuery.memberCount(mAll);
        List<String> locations = StructureListQuery.locations(mAll);
        String signature = mine + "|" + locations;
        if (signature.equals(mFilterSignature) && mFilters.getChildCount() > 0) return;
        mFilterSignature = signature;

        // Filtre devenu impossible (ex. plus aucune adhésion) : retour à « Toutes ».
        if (mQuery.onlyMine && mine == 0) mQuery = mQuery.withOnlyMine(false);
        if (mQuery.location != null && !containsIgnoreCase(locations, mQuery.location)) mQuery = mQuery.withLocation(null);

        mFilters.setOnCheckedChangeListener(null);
        mFilters.removeAllViews();
        addFilterChip(getString(R.string.structures_filter_all), null, false,
                !mQuery.onlyMine && mQuery.location == null);
        if (mine > 0) addFilterChip(getString(R.string.structures_filter_mine, mine), null, true, mQuery.onlyMine);
        for (String l : locations) {
            addFilterChip(l, l, false, mQuery.location != null && StructureText.fold(l).equals(StructureText.fold(mQuery.location)));
        }
        boolean useful = mine > 0 || !locations.isEmpty();
        mFilters.setVisibility(useful ? View.VISIBLE : View.GONE);
    }

    private void addFilterChip(String label, String location, boolean mine, boolean checked) {
        Chip chip = (Chip) LayoutInflater.from(requireContext()).inflate(R.layout.item_profile_chip, mFilters, false);
        chip.setId(View.generateViewId());
        chip.setText(label);
        chip.setChecked(checked);
        chip.setOnCheckedChangeListener((b, isChecked) -> {
            if (!isChecked) return;
            mQuery = mQuery.withOnlyMine(mine).withLocation(location);
            render();
        });
        mFilters.addView(chip);
    }

    private static boolean containsIgnoreCase(List<String> values, String v) {
        for (String s : values) if (StructureText.fold(s).equals(StructureText.fold(v))) return true;
        return false;
    }

    private void resetFilters() {
        mQuery = StructureListQuery.initial().withSort(mQuery.sort);
        if (mSearch.getText().length() > 0) mSearch.setText("");
        mFilterSignature = "";
        rebuildFilters();
        render();
    }

    private void hideKeyboard() {
        if (mSearch == null || getContext() == null) return;
        InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) imm.hideSoftInputFromWindow(mSearch.getWindowToken(), 0);
        mSearch.clearFocus();
    }

    // =================================================================== StructureCardAdapter.Listener

    @Override
    public void onStructureClick(StructureInfo structure) {
        hideKeyboard();
        startActivity(StructureActivity.intentFor(requireContext(), structure));
    }

    @Override
    public void onStateAction(StructureCardAdapter.State state) {
        if (state == StructureCardAdapter.State.NO_RESULTS) {
            resetFilters();
        } else if (state == StructureCardAdapter.State.ERROR) {
            mFailed = false;
            render();
            load(true);
        }
    }
}
