package com.naniger.elim.controleur.activity;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Icon;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.Layout;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.naniger.elim.R;
import com.naniger.elim.controleur.adapter.AuthorHorizontaleAdapter;
import com.naniger.elim.controleur.adapter.AuthorProfileAdapter;
import com.naniger.elim.model.author.AuthorJson;
import com.naniger.elim.model.author.AuthorProfile;
import com.naniger.elim.model.author.BibliographyQuery;
import com.naniger.elim.model.author.PagedList;
import com.naniger.elim.model.author.ProfileText;
import com.naniger.elim.model.author.Publication;
import com.naniger.elim.model.cache.ApiCache;
import com.naniger.elim.model.cache.CachedLoader;
import com.naniger.elim.model.data.Server;
import com.naniger.elim.model.net.ApiClient;
import com.naniger.elim.model.structure.StructureInfo;
import com.naniger.elim.model.structure.StructureJson;
import com.naniger.elim.model.structure.StructureText;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Fiche d'une structure, organisée comme une page d'organisation :
 * identité (bannière, logo, nom, localisation, chiffres réels, statut d'adhésion) → actions
 * (Adhérer / Membre, Partager, raccourci, Ajouter un livre pour les administrateurs) → À propos
 * → En chiffres (formats, membres, domaines) → Auteurs publiés → Ressources paginées avec
 * recherche, tri et filtres.
 *
 * <p>Données : {@code GET /api/structures/{id}} et {@code GET /api/book/associations?structureId=…}
 * via le cache « cache d'abord » (une fiche déjà consultée reste lisible hors ligne).
 * Adhésion : {@code POST /api/structures/{id}/join} et {@code /leave} (identité prise du jeton).
 *
 * <p>Extras (inchangés, compatibles avec l'accueil, la recherche et les raccourcis déjà créés) :
 * {@code intent_structure_adapter_id} (obligatoire) et, pour un affichage immédiat, nom, logo,
 * bannière, description, adhésion, nombres de membres et de livres.
 */
public class StructureActivity extends AppCompatActivity implements AuthorProfileAdapter.Listener {

    private static final String TAG = "StructureActivity";
    private static final long SEARCH_DELAY_MS = 350;
    private static final int PREFETCH_DISTANCE = 5;
    private static final int ABOUT_COLLAPSED_LINES = 6;

    public static final String EXTRA_ID = "intent_structure_adapter_id";
    public static final String EXTRA_LOGO = "intent_structure_adapter_logo";
    public static final String EXTRA_NAME = "intent_structure_adapter_name";
    public static final String EXTRA_DESCRIPTION = "intent_structure_adapter_description";
    public static final String EXTRA_IS_ADHERE = "intent_structure_adapter_is_adhere";
    public static final String EXTRA_BANNER = "intent_structure_adapter_banner";
    public static final String EXTRA_AUTHOR = "intent_structure_adapter_author";
    public static final String EXTRA_ADHERER_NUMBER = "intent_structure_adapter_adherer_number";
    public static final String EXTRA_BOOK_NUMBER = "intent_structure_adapter_book_number";
    public static final String EXTRA_ADMIN = "intent_structure_adapter_admin";
    public static final String EXTRA_LOCATION = "intent_structure_adapter_location";

    private static final ExecutorService NETWORK = Executors.newSingleThreadExecutor();

    /** Intent d'ouverture de la fiche depuis la liste des structures. */
    public static Intent intentFor(Context context, StructureInfo s) {
        Intent i = new Intent(context, StructureActivity.class);
        i.putExtra(EXTRA_ID, s.id);
        i.putExtra(EXTRA_NAME, s.name);
        i.putExtra(EXTRA_LOGO, s.logo);
        i.putExtra(EXTRA_BANNER, s.banner);
        i.putExtra(EXTRA_DESCRIPTION, s.description);
        i.putExtra(EXTRA_LOCATION, s.location);
        i.putExtra(EXTRA_IS_ADHERE, s.isMember);
        i.putExtra(EXTRA_ADHERER_NUMBER, String.valueOf(s.members));
        i.putExtra(EXTRA_BOOK_NUMBER, String.valueOf(s.resources));
        i.putExtra(EXTRA_ADMIN, s.isMemberAdmin ? "1" : "0");
        return i;
    }

    // Vues
    private SwipeRefreshLayout mSwipe;
    private RecyclerView mList;
    private LinearLayoutManager mLayoutManager;
    private View mTopBar, mNotFound;
    private TextView mTopTitle;
    private View mHeader;
    private ImageView mBanner, mLogo;
    private TextView mInitials, mName, mLocation, mMeta, mBadge;
    private TextView mJoinButton, mAddBook;
    private View mNotice;
    private TextView mNoticeText;
    private TextView mAbout, mAboutToggle, mAboutEmpty;
    private View mAboutSkeleton, mLocationRow;
    private TextView mLocationValue;
    private View mNumbersSection;
    private TextView mStatResources, mStatPdf, mStatAudio, mStatMembers;
    private View mStatPdfBox, mStatAudioBox, mStatMembersBox;
    private View mDomainsTitle, mDomainsHint;
    private ChipGroup mDomains;
    private View mAuthorsSection;
    private RecyclerView mAuthorsList;
    private View mResourcesSection;
    private TextView mResourcesTitle;
    private EditText mSearch;
    private View mSearchClear;
    private TextView mSortButton;
    private ChipGroup mFormats;
    private Chip mActiveCategory;

    // Données
    private String mId;
    private final StructureInfo mInfo = new StructureInfo();
    private boolean mProfileLoaded;
    private boolean mMembershipBusy;
    private BibliographyQuery mQuery;
    private final PagedList<Publication> mBooks = new PagedList<>(p -> p.id);
    private AuthorProfileAdapter mAdapter;
    private boolean mAboutExpanded;
    private float mDensity;

    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private final Runnable mRunSearch = this::reloadResources;

    // =================================================================== cycle de vie

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_structure);
        if (getSupportActionBar() != null) getSupportActionBar().hide();
        mDensity = getResources().getDisplayMetrics().density;

        bindViews();

        Intent intent = getIntent();
        mId = intent.getStringExtra(EXTRA_ID);
        if (!StructureText.isValidId(mId)) {
            showNotFound(); // identifiant absent ou invalide : aucune requête
            return;
        }
        mId = mId.trim();
        mInfo.id = mId;
        mQuery = BibliographyQuery.forStructure(mId);

        showIntentPreview(intent);
        setupList();
        setupResourceTools();
        setupActions();

        mList.post(this::updateTopBar);
        loadProfile(false);
        reloadResources();
    }

    @Override
    protected void onDestroy() {
        mHandler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    private boolean isGone() {
        return isFinishing() || isDestroyed();
    }

    // =================================================================== vues

    private void bindViews() {
        mSwipe = findViewById(R.id.swipe_refresh_structure);
        mList = findViewById(R.id.recycler_view_structure_profile);
        mTopBar = findViewById(R.id.linear_layout_structure_top_bar);
        mTopTitle = findViewById(R.id.text_view_structure_top_title);
        mNotFound = findViewById(R.id.linear_layout_structure_not_found);

        // Le gestionnaire de mise en page doit exister AVANT de gonfler l'en-tête avec la liste
        // pour parent (RecyclerView.generateLayoutParams l'exige).
        mLayoutManager = new LinearLayoutManager(this);
        mList.setLayoutManager(mLayoutManager);
        mHeader = LayoutInflater.from(this).inflate(R.layout.item_structure_profile_header, mList, false);
        mBanner = mHeader.findViewById(R.id.image_view_structure_profile_banner);
        mLogo = mHeader.findViewById(R.id.image_view_structure_profile_logo);
        mInitials = mHeader.findViewById(R.id.text_view_structure_profile_initials);
        mName = mHeader.findViewById(R.id.text_view_structure_profile_name);
        mLocation = mHeader.findViewById(R.id.text_view_structure_profile_location);
        mMeta = mHeader.findViewById(R.id.text_view_structure_profile_meta);
        mBadge = mHeader.findViewById(R.id.text_view_structure_profile_badge);
        mJoinButton = mHeader.findViewById(R.id.text_view_structure_profile_join);
        mAddBook = mHeader.findViewById(R.id.text_view_structure_profile_add_book);
        mNotice = mHeader.findViewById(R.id.linear_layout_structure_profile_notice);
        mNoticeText = mHeader.findViewById(R.id.text_view_structure_profile_notice);
        mAbout = mHeader.findViewById(R.id.text_view_structure_profile_about);
        mAboutToggle = mHeader.findViewById(R.id.text_view_structure_profile_about_toggle);
        mAboutEmpty = mHeader.findViewById(R.id.text_view_structure_profile_about_empty);
        mAboutSkeleton = mHeader.findViewById(R.id.linear_layout_structure_profile_about_skeleton);
        mLocationRow = mHeader.findViewById(R.id.linear_layout_structure_profile_location_row);
        mLocationValue = mHeader.findViewById(R.id.text_view_structure_profile_location_value);
        mNumbersSection = mHeader.findViewById(R.id.linear_layout_structure_profile_numbers);
        mStatResources = mHeader.findViewById(R.id.text_view_structure_stat_resources);
        mStatPdf = mHeader.findViewById(R.id.text_view_structure_stat_pdf);
        mStatAudio = mHeader.findViewById(R.id.text_view_structure_stat_audio);
        mStatMembers = mHeader.findViewById(R.id.text_view_structure_stat_members);
        mStatPdfBox = mHeader.findViewById(R.id.linear_layout_structure_stat_pdf);
        mStatAudioBox = mHeader.findViewById(R.id.linear_layout_structure_stat_audio);
        mStatMembersBox = mHeader.findViewById(R.id.linear_layout_structure_stat_members);
        mDomainsTitle = mHeader.findViewById(R.id.text_view_structure_profile_domains_title);
        mDomainsHint = mHeader.findViewById(R.id.text_view_structure_profile_domains_hint);
        mDomains = mHeader.findViewById(R.id.chip_group_structure_profile_domains);
        mAuthorsSection = mHeader.findViewById(R.id.linear_layout_structure_profile_authors);
        mAuthorsList = mHeader.findViewById(R.id.recycler_view_structure_profile_authors);
        mResourcesSection = mHeader.findViewById(R.id.linear_layout_structure_profile_resources);
        mResourcesTitle = mHeader.findViewById(R.id.text_view_structure_profile_resources_title);
        mSearch = mHeader.findViewById(R.id.edit_text_structure_profile_search);
        mSearchClear = mHeader.findViewById(R.id.image_button_structure_profile_search_clear);
        mSortButton = mHeader.findViewById(R.id.text_view_structure_profile_sort);
        mFormats = mHeader.findViewById(R.id.chip_group_structure_profile_formats);
        mActiveCategory = mHeader.findViewById(R.id.chip_structure_profile_active_category);

        findViewById(R.id.image_button_structure_back).setOnClickListener(v -> finish());
        findViewById(R.id.text_view_structure_not_found_back).setOnClickListener(v -> finish());
    }

    /** Affichage immédiat avec les données transmises par l'écran précédent (rien d'inventé). */
    private void showIntentPreview(Intent intent) {
        mInfo.name = ProfileText.clean(intent.getStringExtra(EXTRA_NAME));
        mInfo.logo = ProfileText.clean(intent.getStringExtra(EXTRA_LOGO));
        mInfo.banner = ProfileText.clean(intent.getStringExtra(EXTRA_BANNER));
        mInfo.location = ProfileText.clean(intent.getStringExtra(EXTRA_LOCATION));
        mInfo.isMember = intent.getBooleanExtra(EXTRA_IS_ADHERE, false);
        mInfo.isMemberAdmin = "1".equals(intent.getStringExtra(EXTRA_ADMIN));
        mInfo.members = parseCount(intent.getStringExtra(EXTRA_ADHERER_NUMBER));
        mInfo.resources = parseCount(intent.getStringExtra(EXTRA_BOOK_NUMBER));
        String description = ProfileText.biography(intent.getStringExtra(EXTRA_DESCRIPTION));
        mInfo.description = description;

        bindIdentity();
        bindMembership();
        mMeta.setVisibility(View.GONE); // chiffres affichés quand la fiche est confirmée par l'API
        if (description != null) showAbout(description, false);
    }

    private static int parseCount(String s) {
        try {
            return Math.max(0, Integer.parseInt(s == null ? "" : s.trim()));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private void setupList() {
        mAdapter = new AuthorProfileAdapter(mHeader, this);
        mAdapter.configureForStructure(getString(R.string.structure_resources_empty_title),
                getString(R.string.structure_resources_error), getString(R.string.structure_resources_end));
        mList.setAdapter(mAdapter);
        mList.setItemAnimator(null);

        mList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView rv, int dx, int dy) {
                updateTopBar();
                int lastVisible = mLayoutManager.findLastVisibleItemPosition() - 1; // -1 : en-tête
                if (mBooks.shouldPrefetch(lastVisible, PREFETCH_DISTANCE)) loadNextPage();
            }
        });

        mSwipe.setColorSchemeColors(ContextCompat.getColor(this, R.color.elim_primary));
        mSwipe.setOnRefreshListener(() -> {
            loadProfile(true);
            reloadResources(true);
        });
    }

    /** Barre du haut : transparente sur la bannière, puis surface + nom de la structure. */
    private void updateTopBar() {
        float progress = 1f;
        if (mLayoutManager.findFirstVisibleItemPosition() == 0) {
            View first = mLayoutManager.findViewByPosition(0);
            float scrolled = first != null ? -first.getTop() : 0f;
            float threshold = 160 * mDensity; // ≈ bas du logo
            progress = Math.max(0f, Math.min(1f, (scrolled - threshold * 0.6f) / (threshold * 0.4f)));
        }
        int surface = ContextCompat.getColor(this, R.color.elim_surface);
        int alpha = Math.round(progress * 255);
        mTopBar.setBackgroundColor((surface & 0x00FFFFFF) | (alpha << 24));
        mTopBar.setElevation(progress >= 1f ? 3 * mDensity : 0f);
        mTopTitle.setAlpha(progress);
    }

    private void setupActions() {
        View.OnClickListener share = v -> shareStructure();
        findViewById(R.id.image_button_structure_share_top).setOnClickListener(share);
        mHeader.findViewById(R.id.text_view_structure_profile_share).setOnClickListener(share);
        mHeader.findViewById(R.id.image_button_structure_profile_shortcut).setOnClickListener(v -> createHomeShortcut());
        findViewById(R.id.image_button_structure_announcements).setOnClickListener(v ->
                startSafely(new Intent(this, NotificationActivity.class)));
        mHeader.findViewById(R.id.text_view_structure_profile_notice_retry).setOnClickListener(v -> {
            mNotice.setVisibility(View.GONE);
            loadProfile(true);
            if (mBooks.hasError() || mBooks.isEmptyResult()) reloadResources(true);
        });
        mAboutToggle.setOnClickListener(v -> {
            mAboutExpanded = !mAboutExpanded;
            mAbout.setMaxLines(mAboutExpanded ? Integer.MAX_VALUE : ABOUT_COLLAPSED_LINES);
            mAboutToggle.setText(mAboutExpanded ? R.string.author_profile_see_less : R.string.author_profile_see_more);
        });
        mJoinButton.setOnClickListener(v -> onJoinButton());
        // Même écran d'ajout que la carte « Ajouter un livre » de l'ancienne fiche.
        mAddBook.setOnClickListener(v -> startSafely(new Intent(this, AddBookActivity.class)));
    }

    // =================================================================== fiche

    private void loadProfile(boolean force) {
        String url = StructureText.detailUrl(Server.getUrlHostProd(this), mId);
        ApiCache.loadObject(this, url, ApiCache.TTL_STRUCTURE_PROFILE, force, new CachedLoader.Callback() {
            @Override
            public void onCached(String body, long ageMs) {
                if (!isGone()) applyProfile(body);
            }

            @Override
            public void onFresh(String body) {
                if (isGone()) return;
                mNotice.setVisibility(View.GONE);
                applyProfile(body);
                mSwipe.setRefreshing(false);
            }

            @Override
            public void onFailure(boolean hadCache) {
                if (isGone()) return;
                mSwipe.setRefreshing(false);
                // Fiche déjà enregistrée : on la garde et on le signale. Jamais consultée : on le
                // dit, sans prétendre qu'elle est disponible hors ligne.
                showNotice(hadCache ? R.string.structure_offline
                        : (mProfileLoaded ? R.string.structure_error : R.string.structure_offline_empty));
                if (!mProfileLoaded && mInfo.description == null) {
                    mAboutSkeleton.setVisibility(View.GONE); // pas de faux « aucune présentation »
                }
            }

            @Override
            public void onNotFound() {
                if (isGone()) return;
                mSwipe.setRefreshing(false);
                showNotFound();
            }
        });
    }

    private void applyProfile(String body) {
        StructureInfo s;
        try {
            s = StructureJson.detail(body);
        } catch (Exception e) {
            Log.e(TAG, "Fiche illisible", e);
            showNotice(R.string.structure_error);
            return;
        }
        if (s.id != null && !s.id.equals(mId)) {
            Log.w(TAG, "Réponse d'une autre structure ignorée");
            return;
        }
        mProfileLoaded = true;
        copy(s, mInfo);
        bindIdentity();
        bindMeta();
        if (!mMembershipBusy) bindMembership();
        showAbout(mInfo.description, true);
        bindLocationRow();
        bindNumbers(s);
        bindAuthors(s);
        bindFormatChips(s);
        mResourcesTitle.setText(s.resources > 0
                ? getString(R.string.structure_resources) + " · " + s.resources
                : getString(R.string.structure_resources));
    }

    private static void copy(StructureInfo from, StructureInfo to) {
        to.name = from.name != null ? from.name : to.name;
        to.description = from.description;
        to.logo = from.logo;
        to.banner = from.banner;
        to.location = from.location;
        to.createdAt = from.createdAt;
        to.resources = from.resources;
        to.members = from.members;
        to.isMember = from.isMember;
        to.isMemberAdmin = from.isMemberAdmin;
        to.hasStats = from.hasStats;
        to.electronic = from.electronic;
        to.audio = from.audio;
        to.physical = from.physical;
        to.firstAddedAt = from.firstAddedAt;
    }

    private void bindIdentity() {
        String name = mInfo.name != null ? mInfo.name : "";
        mName.setText(name);
        mTopTitle.setText(name);
        mInitials.setText(StructureText.initials(name));

        mLocation.setText(mInfo.location != null ? mInfo.location : "");
        mLocation.setVisibility(mInfo.location != null ? View.VISIBLE : View.GONE);

        String host = Server.getUrlHostProd(this);
        String logoUrl = StructureText.logoUrl(host, mId, mInfo.logo);
        if (logoUrl == null) {
            Picasso.get().cancelRequest(mLogo);
            mLogo.setVisibility(View.GONE); // initiales sur fond bleu doux
        } else {
            mLogo.setContentDescription(getString(R.string.structure_logo_a11y, name));
            Picasso.get().load(logoUrl).fit().centerCrop().into(mLogo, new Callback() {
                @Override
                public void onSuccess() {
                    mLogo.setVisibility(View.VISIBLE);
                }

                @Override
                public void onError(Exception e) {
                    mLogo.setVisibility(View.GONE);
                }
            });
        }

        // Bannière réelle redimensionnée à la vue (avant : resize(6200, 2222), ~55 Mo en mémoire).
        String bannerUrl = StructureText.bannerUrl(host, mId, mInfo.banner);
        if (bannerUrl == null) {
            Picasso.get().cancelRequest(mBanner);
            mBanner.setImageDrawable(null); // couleur Elim du bandeau
        } else {
            Picasso.get().load(bannerUrl).fit().centerCrop().into(mBanner);
        }
    }

    /** « 40 ressources · 12 membres · Sur Elim depuis 2026 » — uniquement des valeurs calculées. */
    private void bindMeta() {
        List<String> parts = new ArrayList<>();
        parts.add(mInfo.resources > 0
                ? getResources().getQuantityString(R.plurals.structure_resources, mInfo.resources, mInfo.resources)
                : getString(R.string.structure_no_resource));
        if (mInfo.members > 0) {
            parts.add(getResources().getQuantityString(R.plurals.structure_members, mInfo.members, mInfo.members));
        }
        String since = ProfileText.year(mInfo.createdAt);
        if (since != null) parts.add(getString(R.string.structure_since, since));
        mMeta.setText(ProfileText.join(parts, " · "));
        mMeta.setVisibility(View.VISIBLE);
    }

    private void showAbout(String description, boolean confirmed) {
        mAboutSkeleton.setVisibility(View.GONE);
        if (description == null) {
            mAbout.setVisibility(View.GONE);
            mAboutToggle.setVisibility(View.GONE);
            // « Aucune présentation » seulement si l'API l'a confirmé.
            mAboutEmpty.setVisibility(confirmed ? View.VISIBLE : View.GONE);
            return;
        }
        mAboutEmpty.setVisibility(View.GONE);
        mAbout.setVisibility(View.VISIBLE);
        mAbout.setText(description); // texte brut : aucun HTML interprété
        mAbout.setMaxLines(mAboutExpanded ? Integer.MAX_VALUE : ABOUT_COLLAPSED_LINES);
        mAbout.post(() -> {
            Layout layout = mAbout.getLayout();
            boolean overflow = mAbout.getLineCount() > ABOUT_COLLAPSED_LINES
                    || (layout != null && layout.getLineCount() > 0
                    && layout.getEllipsisCount(layout.getLineCount() - 1) > 0);
            mAboutToggle.setVisibility(overflow || mAboutExpanded ? View.VISIBLE : View.GONE);
        });
    }

    private void bindLocationRow() {
        mLocationValue.setText(mInfo.location != null ? mInfo.location : "");
        mLocationRow.setVisibility(mInfo.location != null ? View.VISIBLE : View.GONE);
    }

    private void bindNumbers(StructureInfo s) {
        if (s.resources <= 0 && s.members <= 0) {
            mNumbersSection.setVisibility(View.GONE); // pas de chiffres creux
            return;
        }
        mNumbersSection.setVisibility(View.VISIBLE);
        mStatResources.setText(String.valueOf(s.resources));
        bindStat(mStatPdfBox, mStatPdf, s.hasStats ? s.electronic : 0);
        bindStat(mStatAudioBox, mStatAudio, s.hasStats ? s.audio : 0);
        bindStat(mStatMembersBox, mStatMembers, s.members);

        mDomains.removeAllViews();
        boolean hasDomains = !s.categories.isEmpty();
        mDomainsTitle.setVisibility(hasDomains ? View.VISIBLE : View.GONE);
        mDomainsHint.setVisibility(hasDomains ? View.VISIBLE : View.GONE);
        for (AuthorProfile.CategoryCount c : s.categories) {
            Chip chip = (Chip) LayoutInflater.from(this).inflate(R.layout.item_profile_chip, mDomains, false);
            chip.setCheckable(false);
            chip.setText(c.count > 0 ? c.title + " · " + c.count : c.title);
            chip.setOnClickListener(v -> applyCategoryFilter(c.id, c.title));
            mDomains.addView(chip);
        }
    }

    private void bindStat(View box, TextView value, int count) {
        box.setVisibility(count > 0 ? View.VISIBLE : View.INVISIBLE);
        value.setText(String.valueOf(count));
    }

    private void bindAuthors(StructureInfo s) {
        if (s.authors.isEmpty()) {
            mAuthorsSection.setVisibility(View.GONE);
            return;
        }
        mAuthorsSection.setVisibility(View.VISIBLE);
        mAuthorsList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        mAuthorsList.setAdapter(new AuthorHorizontaleAdapter(new ArrayList<>(s.authors)));
    }

    private void showNotice(int message) {
        mNoticeText.setText(message);
        mNotice.setVisibility(View.VISIBLE);
    }

    private void showNotFound() {
        mNotFound.setVisibility(View.VISIBLE);
        if (mSwipe != null) mSwipe.setVisibility(View.GONE);
        View share = findViewById(R.id.image_button_structure_share_top);
        if (share != null) share.setVisibility(View.GONE);
        View notifications = findViewById(R.id.image_button_structure_announcements);
        if (notifications != null) notifications.setVisibility(View.GONE);
    }

    // =================================================================== adhésion

    /** Bouton d'adhésion selon l'état RÉEL renvoyé par l'API (jamais déduit du libellé). */
    private void bindMembership() {
        boolean member = mInfo.isMember;
        String name = mInfo.name != null ? mInfo.name : "";
        if (mMembershipBusy) {
            mJoinButton.setText(R.string.structure_join_in_progress);
            mJoinButton.setEnabled(false);
            mJoinButton.setAlpha(0.7f);
            return;
        }
        mJoinButton.setEnabled(true);
        mJoinButton.setAlpha(1f);
        if (member) {
            mJoinButton.setText(R.string.structure_joined);
            mJoinButton.setBackgroundResource(R.drawable.bg_structure_outline_button);
            mJoinButton.setTextColor(ContextCompat.getColor(this, R.color.elim_primary));
            mJoinButton.setCompoundDrawablesRelative(tinted(R.drawable.ic_structure_check, R.color.elim_primary), null, null, null);
            mJoinButton.setContentDescription(getString(R.string.structure_joined_a11y, name));
        } else {
            mJoinButton.setText(R.string.structure_join);
            mJoinButton.setBackgroundResource(R.drawable.bg_profile_primary_button);
            mJoinButton.setTextColor(ContextCompat.getColor(this, R.color.elim_on_primary));
            mJoinButton.setCompoundDrawablesRelative(tinted(R.drawable.ic_structure_add, R.color.elim_on_primary), null, null, null);
            mJoinButton.setContentDescription(getString(R.string.structure_join_a11y, name));
        }
        mBadge.setVisibility(member ? View.VISIBLE : View.GONE);
        mBadge.setText(mInfo.isMemberAdmin ? R.string.structure_badge_admin : R.string.structure_badge_member);
        mAddBook.setVisibility(member && mInfo.isMemberAdmin ? View.VISIBLE : View.GONE);
    }

    private android.graphics.drawable.Drawable tinted(int drawable, int color) {
        android.graphics.drawable.Drawable d = ContextCompat.getDrawable(this, drawable);
        if (d == null) return null;
        d = d.mutate();
        int size = Math.round(18 * mDensity);
        d.setBounds(0, 0, size, size);
        d.setTint(ContextCompat.getColor(this, color));
        return d;
    }

    private void onJoinButton() {
        if (mMembershipBusy) return;
        if (mInfo.isMember) confirmLeave();
        else changeMembership(true);
    }

    /**
     * Confirmation simple avant de quitter. L'ancien écran demandait le mot de passe et le
     * comparait à une empreinte locale : ce n'était pas un contrôle de sécurité (l'API autorise
     * via le jeton), et les comptes Google, sans mot de passe local, ne pouvaient jamais quitter.
     */
    private void confirmLeave() {
        String name = mInfo.name != null ? mInfo.name : "";
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.structure_leave_dialog_title, name))
                .setMessage(R.string.structure_leave_dialog_message)
                .setNegativeButton(R.string.struct_leave_cancel, null)
                .setPositiveButton(R.string.structure_leave_dialog_confirm, (d, w) -> changeMembership(false))
                .show();
    }

    private void changeMembership(boolean join) {
        mMembershipBusy = true;
        bindMembership();
        final String url = StructureText.membershipUrl(Server.getUrlHostProd(this), mId, join);
        final Context app = getApplicationContext();
        NETWORK.execute(() -> {
            int code;
            String message = null;
            try {
                Request request = new Request.Builder().url(url)
                        .post(RequestBody.create(new byte[0], null))
                        .header("Accept", "application/json").build();
                try (Response r = ApiClient.getInstance(app).newCall(request).execute()) {
                    code = r.code();
                    String body = r.body() != null ? r.body().string() : null;
                    if (body != null) {
                        try {
                            message = ProfileText.clean(new JSONObject(body).optString("message", null));
                        } catch (Exception ignored) {
                            // réponse non JSON : message générique
                        }
                    }
                }
            } catch (Exception e) {
                Log.w(TAG, "Adhésion : échec réseau", e);
                code = -1;
            }
            final int status = code;
            final String serverMessage = message;
            mHandler.post(() -> onMembershipResult(join, status, serverMessage));
        });
    }

    private void onMembershipResult(boolean join, int code, String serverMessage) {
        if (isGone()) return;
        mMembershipBusy = false;
        String name = mInfo.name != null ? mInfo.name : "";
        boolean changed = false;
        if (join && (code == 200 || code == 201)) {
            mInfo.isMember = true;
            mInfo.members++;
            changed = true;
            Toast.makeText(this, getString(R.string.structure_join_success, name), Toast.LENGTH_SHORT).show();
        } else if (join && code == 409) {
            mInfo.isMember = true; // déjà membre côté serveur : l'écran se met à jour
            changed = true;
            if (serverMessage != null) Toast.makeText(this, serverMessage, Toast.LENGTH_SHORT).show();
        } else if (!join && code == 200) {
            mInfo.isMember = false;
            mInfo.isMemberAdmin = false;
            mInfo.members = Math.max(0, mInfo.members - 1);
            changed = true;
            Toast.makeText(this, getString(R.string.structure_leave_success, name), Toast.LENGTH_SHORT).show();
        } else if (!join && code == 404 && serverMessage != null && serverMessage.contains("membre")) {
            mInfo.isMember = false; // n'était déjà plus membre
            mInfo.isMemberAdmin = false;
            changed = true;
        } else if (join && code == 404) {
            showNotFound();
            return;
        } else if (code == 401) {
            Toast.makeText(this, R.string.structure_membership_session, Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, R.string.structure_membership_error, Toast.LENGTH_LONG).show();
        }
        bindMembership();
        if (changed) {
            bindMeta();
            if (mStatMembers != null && mNumbersSection.getVisibility() == View.VISIBLE) {
                bindStat(mStatMembersBox, mStatMembers, mInfo.members);
            }
            // Liste des structures, accueil (recommandations) et fiche : relire l'état réel.
            String host = Server.getUrlHostProd(this);
            ApiCache.invalidate(this, StructureText.listUrl(host));
            ApiCache.invalidate(this, StructureText.detailUrl(host, mId));
            loadProfile(true);
        }
    }

    // =================================================================== ressources

    private void setupResourceTools() {
        mSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) { }
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) { }

            @Override
            public void afterTextChanged(Editable s) {
                mSearchClear.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
                String text = s.toString();
                if (Objects.equals(text.trim(), mQuery.search)) return;
                mQuery = mQuery.withSearch(text);
                mHandler.removeCallbacks(mRunSearch);
                mHandler.postDelayed(mRunSearch, SEARCH_DELAY_MS);
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

        updateSortLabel();
        mSortButton.setOnClickListener(this::showSortMenu);

        addFormatChip(R.string.author_filter_all, null, true);
        addFormatChip(R.string.author_stat_pdf, BibliographyQuery.Format.PDF, false);
        addFormatChip(R.string.author_stat_audio, BibliographyQuery.Format.AUDIO, false);
        addFormatChip(R.string.author_stat_physical, BibliographyQuery.Format.PHYSICAL, false);

        mActiveCategory.setOnCloseIconClickListener(v -> clearCategoryFilter());
        mActiveCategory.setOnClickListener(v -> clearCategoryFilter());
    }

    private void addFormatChip(int label, BibliographyQuery.Format format, boolean checked) {
        Chip chip = (Chip) LayoutInflater.from(this).inflate(R.layout.item_profile_chip, mFormats, false);
        chip.setId(View.generateViewId());
        chip.setText(label);
        chip.setTag(format);
        chip.setChecked(checked);
        chip.setOnCheckedChangeListener((b, isChecked) -> {
            if (!isChecked) return;
            BibliographyQuery.Format f = (BibliographyQuery.Format) b.getTag();
            if (f == mQuery.format) return;
            mQuery = mQuery.withFormat(f);
            reloadResources();
        });
        mFormats.addView(chip);
    }

    /** Formats sans aucune ressource : puce masquée. Un seul format : rangée masquée. */
    private void bindFormatChips(StructureInfo s) {
        if (!s.hasStats) return; // ancienne API : toutes les puces restent proposées
        int shown = 0;
        for (int i = 0; i < mFormats.getChildCount(); i++) {
            View chip = mFormats.getChildAt(i);
            BibliographyQuery.Format f = (BibliographyQuery.Format) chip.getTag();
            int count = f == null ? 1 : f == BibliographyQuery.Format.PDF ? s.electronic
                    : f == BibliographyQuery.Format.AUDIO ? s.audio : s.physical;
            boolean visible = f == null || count > 0 || f == mQuery.format;
            chip.setVisibility(visible ? View.VISIBLE : View.GONE);
            if (f != null && visible) shown++;
        }
        mFormats.setVisibility(shown >= 2 || mQuery.format != null ? View.VISIBLE : View.GONE);
    }

    private void showSortMenu(View anchor) {
        PopupMenu menu = new PopupMenu(this, anchor);
        BibliographyQuery.Sort[] sorts = BibliographyQuery.Sort.values();
        for (int i = 0; i < sorts.length; i++) {
            menu.getMenu().add(0, i, i, sortLabel(sorts[i])).setCheckable(true).setChecked(sorts[i] == mQuery.sort);
        }
        menu.getMenu().setGroupCheckable(0, true, true);
        menu.setOnMenuItemClickListener(item -> {
            BibliographyQuery.Sort s = sorts[item.getItemId()];
            if (s != mQuery.sort) {
                mQuery = mQuery.withSort(s);
                updateSortLabel();
                reloadResources();
            }
            return true;
        });
        menu.show();
    }

    private String sortLabel(BibliographyQuery.Sort s) {
        switch (s) {
            case OLDEST: return getString(R.string.author_sort_oldest);
            case TITLE: return getString(R.string.author_sort_title);
            case TITLE_DESC: return getString(R.string.author_sort_title_desc);
            default: return getString(R.string.author_sort_recent);
        }
    }

    private void updateSortLabel() {
        mSortButton.setText(getString(R.string.author_profile_sort, sortLabel(mQuery.sort)));
    }

    private void applyCategoryFilter(int categoryId, String title) {
        mActiveCategory.setText(title);
        mActiveCategory.setCloseIconContentDescription(getString(R.string.author_filter_category_clear, title));
        mActiveCategory.setVisibility(View.VISIBLE);
        mQuery = mQuery.withCategory(categoryId);
        reloadResources();
        mList.post(() -> {
            int offset = -mResourcesSection.getTop() + mTopBar.getHeight();
            mLayoutManager.scrollToPositionWithOffset(0, offset);
            updateTopBar();
        });
    }

    private void clearCategoryFilter() {
        mActiveCategory.setVisibility(View.GONE);
        if (mQuery.categoryId == null) return;
        mQuery = mQuery.withCategory(null);
        reloadResources();
    }

    private void reloadResources() {
        reloadResources(false);
    }

    /** Nouvelle requête (recherche, tri, filtre, actualisation) : repart de la page 1. */
    private void reloadResources(boolean force) {
        mHandler.removeCallbacks(mRunSearch);
        mBooks.reset();
        mAdapter.replaceAll(mBooks.items());
        mAdapter.setFooter(AuthorProfileAdapter.Footer.LOADING_FIRST, null);
        loadPage(force);
    }

    private void loadNextPage() {
        if (mBooks.nextPageToLoad() > 1) {
            mAdapter.setFooter(AuthorProfileAdapter.Footer.LOADING_MORE, null);
            loadPage(false);
        }
    }

    private void loadPage(boolean force) {
        final int page = mBooks.nextPageToLoad();
        if (page < 1) return;
        final int generation = mBooks.generation();
        mBooks.markLoading();
        String url = mQuery.pageUrl(Server.getUrlHostProd(this), page);
        ApiCache.loadList(this, url, ApiCache.TTL_STRUCTURE_PROFILE, force, new CachedLoader.Callback() {
            @Override
            public void onCached(String body, long ageMs) {
                applyPage(generation, page, body);
            }

            @Override
            public void onFresh(String body) {
                applyPage(generation, page, body);
                if (page == 1 && !isGone()) mSwipe.setRefreshing(false);
            }

            @Override
            public void onFailure(boolean hadCache) {
                if (isGone()) return;
                if (page == 1) mSwipe.setRefreshing(false);
                if (hadCache) return; // la page en cache reste affichée
                if (mBooks.onError(generation)) updateFooter();
            }
        });
    }

    private void applyPage(int generation, int page, String body) {
        if (isGone()) return;
        List<Publication> items;
        int rawCount;
        try {
            items = AuthorJson.structureResources(body, mId);
            rawCount = new org.json.JSONArray(body).length();
        } catch (Exception e) {
            Log.e(TAG, "Ressources illisibles", e);
            if (mBooks.onError(generation)) updateFooter();
            return;
        }
        int added = mBooks.onPage(generation, page, items, BibliographyQuery.mayHaveMore(rawCount));
        if (added < 0) return; // réponse périmée (autre recherche) : ignorée
        if (page == 1) mAdapter.replaceAll(mBooks.items());
        else mAdapter.append(mBooks.items());
        updateFooter();
        mList.post(() -> {
            int lastVisible = mLayoutManager.findLastVisibleItemPosition() - 1;
            if (mBooks.shouldPrefetch(lastVisible, PREFETCH_DISTANCE)) loadNextPage();
        });
    }

    private void updateFooter() {
        AuthorProfileAdapter.Footer footer;
        String emptyMessage = null;
        if (mBooks.hasError()) {
            footer = AuthorProfileAdapter.Footer.ERROR;
        } else if (mBooks.isEmptyResult()) {
            if (mQuery.isFiltered()) {
                footer = AuthorProfileAdapter.Footer.NO_RESULTS;
            } else {
                footer = AuthorProfileAdapter.Footer.EMPTY;
                String name = mInfo.name != null ? mInfo.name : getString(R.string.structures_title);
                emptyMessage = getString(R.string.structure_resources_empty_message, name);
            }
        } else if (mBooks.isLoading()) {
            footer = mBooks.loadedPages() == 0 ? AuthorProfileAdapter.Footer.LOADING_FIRST
                    : AuthorProfileAdapter.Footer.LOADING_MORE;
        } else if (!mBooks.hasMore() && mBooks.loadedPages() > 1) {
            footer = AuthorProfileAdapter.Footer.END;
        } else {
            footer = AuthorProfileAdapter.Footer.NONE;
        }
        mAdapter.setFooter(footer, emptyMessage);
    }

    // =================================================================== AuthorProfileAdapter.Listener

    @Override
    public void onPublicationClick(Publication publication) {
        // Fiche du livre existante : lecture, écoute, téléchargement et favoris y sont gérés.
        Intent intent = new Intent(this, BookActivity.class);
        intent.putExtra("intent_adapter_book_id", publication.id);
        startActivity(intent);
    }

    @Override
    public void onRetry() {
        mBooks.clearError();
        if (mBooks.loadedPages() == 0) {
            reloadResources(true);
        } else {
            mAdapter.setFooter(AuthorProfileAdapter.Footer.LOADING_MORE, null);
            loadPage(true);
        }
    }

    @Override
    public void onResetFilters() {
        mActiveCategory.setVisibility(View.GONE);
        mHandler.removeCallbacks(mRunSearch);
        mQuery = BibliographyQuery.forStructure(mId).withSort(mQuery.sort);
        if (mSearch.getText().length() > 0) mSearch.setText("");
        for (int i = 0; i < mFormats.getChildCount(); i++) {
            Chip chip = (Chip) mFormats.getChildAt(i);
            if (chip.getTag() == null) chip.setChecked(true);
        }
        reloadResources();
    }

    // =================================================================== partage & raccourci

    private void shareStructure() {
        String name = mInfo.name != null ? mInfo.name : "";
        String message = StructureText.shareMessage(name, mInfo.location, mInfo.resources,
                getString(R.string.url_play_store));
        Intent send = new Intent(Intent.ACTION_SEND);
        send.setType("text/plain");
        send.putExtra(Intent.EXTRA_SUBJECT, name);
        send.putExtra(Intent.EXTRA_TEXT, message);
        startSafely(Intent.createChooser(send, getString(R.string.structure_share_chooser)));
    }

    /**
     * Raccourci épinglé vers cette fiche (fonction existante conservée). Le logo de LA structure
     * est utilisé (l'ancien code chargeait toujours celui de la structure n° 1).
     */
    private void createHomeShortcut() {
        ShortcutManager manager = getSystemService(ShortcutManager.class);
        if (manager == null || !manager.isRequestPinShortcutSupported()) {
            Toast.makeText(this, R.string.structure_shortcut_unsupported, Toast.LENGTH_SHORT).show();
            return;
        }
        final String name = mInfo.name != null ? mInfo.name : getString(R.string.structures_title);
        final String logoUrl = StructureText.logoUrl(Server.getUrlHostProd(this), mId, mInfo.logo);
        final Intent target = new Intent(this, StructureActivity.class);
        target.setAction(Intent.ACTION_MAIN);
        target.putExtra(EXTRA_ID, mId);
        target.putExtra(EXTRA_NAME, name);
        target.putExtra(EXTRA_LOGO, mInfo.logo);
        target.putExtra(EXTRA_BANNER, mInfo.banner);
        final Context app = getApplicationContext();
        NETWORK.execute(() -> {
            Bitmap bitmap = null;
            if (logoUrl != null) {
                try {
                    bitmap = Picasso.get().load(logoUrl).resize(192, 192).centerCrop().get();
                } catch (Exception e) {
                    Log.w(TAG, "Logo du raccourci indisponible", e);
                }
            }
            final Icon icon = bitmap != null ? Icon.createWithBitmap(bitmap)
                    : Icon.createWithResource(app, R.mipmap.ic_v2);
            mHandler.post(() -> {
                if (isGone()) return;
                ShortcutInfo info = new ShortcutInfo.Builder(this, "struct_" + mId)
                        .setShortLabel(name.length() > 24 ? name.substring(0, 24) : name)
                        .setLongLabel(name)
                        .setIcon(icon)
                        .setIntent(target)
                        .build();
                try {
                    manager.requestPinShortcut(info, null);
                    Toast.makeText(this, R.string.structure_shortcut_done, Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Toast.makeText(this, R.string.structure_shortcut_unsupported, Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void startSafely(Intent intent) {
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, R.string.structure_error, Toast.LENGTH_SHORT).show();
        }
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (imm != null) imm.hideSoftInputFromWindow(mSearch.getWindowToken(), 0);
        mSearch.clearFocus();
    }
}
