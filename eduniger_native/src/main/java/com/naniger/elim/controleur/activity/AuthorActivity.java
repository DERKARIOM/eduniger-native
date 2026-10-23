package com.naniger.elim.controleur.activity;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
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
import com.naniger.elim.model.author.SimilarAuthors;
import com.naniger.elim.model.cache.ApiCache;
import com.naniger.elim.model.cache.CachedLoader;
import com.naniger.elim.model.data.Author;
import com.naniger.elim.model.data.Server;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Profil d'auteur, organisé comme un profil professionnel :
 * identité (portrait, nom, profession, chiffres clés, partage, contacts) → À propos (biographie)
 * → En chiffres (formats, domaines) → Auteurs similaires → Bibliographie paginée avec recherche,
 * tri et filtres.
 *
 * <p>Données : API Laravel uniquement ({@code /api/authors/{id}}, {@code /api/authors/{id}/similar},
 * {@code /api/book/associations?authorId=…}), via le cache « cache d'abord » de l'application :
 * un profil déjà consulté reste lisible hors ligne. Les anciens scripts PHP
 * ({@code author_book.php}, {@code AuthorSimular.php}) ne sont plus appelés.
 *
 * <p>Extras attendus (inchangés) : {@code intent_author_adapter_id} (obligatoire) et, pour un
 * affichage immédiat avant la réponse du serveur, nom, prénom, portrait et profession.
 */
public class AuthorActivity extends AppCompatActivity implements AuthorProfileAdapter.Listener {

    private static final String TAG = "AuthorActivity";
    private static final long SEARCH_DELAY_MS = 350;
    private static final int PREFETCH_DISTANCE = 5;
    private static final int MAX_SIMILAR = 12;
    private static final int BIO_COLLAPSED_LINES = 6;

    // Vues
    private SwipeRefreshLayout mSwipe;
    private RecyclerView mList;
    private LinearLayoutManager mLayoutManager;
    private View mTopBar, mNotFound;
    private TextView mTopTitle;
    private View mHeader;
    private ImageView mPortrait;
    private TextView mName, mProfession, mMeta;
    private View mCallButton, mEmailButton, mWhatsappButton;
    private View mNotice;
    private TextView mNoticeText;
    private TextView mBio, mBioToggle, mBioEmpty;
    private View mBioSkeleton;
    private View mNumbersSection;
    private TextView mStatPublications, mStatPdf, mStatAudio, mStatPhysical;
    private View mStatPdfBox, mStatAudioBox, mStatPhysicalBox;
    private View mDomainsTitle, mDomainsHint;
    private ChipGroup mDomains;
    private View mSimilarSection;
    private RecyclerView mSimilarList;
    private TextView mSimilarEmpty;
    private View mBibliographySection;
    private TextView mBibliographyTitle;
    private EditText mSearch;
    private View mSearchClear;
    private TextView mSortButton;
    private ChipGroup mFormats;
    private Chip mActiveCategory;

    // Données
    private String mAuthorId;
    private AuthorProfile mProfile;
    private BibliographyQuery mQuery;
    private final PagedList<Publication> mBooks = new PagedList<>(p -> p.id);
    private AuthorProfileAdapter mAdapter;
    private boolean mBioExpanded;
    private float mDensity;

    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private final Runnable mRunSearch = this::reloadBibliography;

    // =================================================================== cycle de vie

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_author);
        if (getSupportActionBar() != null) getSupportActionBar().hide();
        mDensity = getResources().getDisplayMetrics().density;

        bindViews();

        Intent intent = getIntent();
        mAuthorId = intent.getStringExtra("intent_author_adapter_id");
        if (!BibliographyQuery.isValidAuthorId(mAuthorId)) {
            // Identifiant absent ou invalide : aucune requête, message clair.
            showNotFound();
            return;
        }
        mAuthorId = mAuthorId.trim();
        mQuery = BibliographyQuery.forAuthor(mAuthorId);

        showIntentPreview(intent);
        setupList();
        setupBibliographyTools();
        setupActions();

        mList.post(this::updateTopBar);
        loadProfile(false);
        loadSimilar(false);
        reloadBibliography();
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
        mSwipe = findViewById(R.id.swipe_refresh_author);
        mList = findViewById(R.id.recycler_view_author_profile);
        mTopBar = findViewById(R.id.linear_layout_author_top_bar);
        mTopTitle = findViewById(R.id.text_view_author_top_title);
        mNotFound = findViewById(R.id.linear_layout_author_not_found);

        mHeader = LayoutInflater.from(this).inflate(R.layout.item_author_profile_header, mList, false);
        mPortrait = mHeader.findViewById(R.id.image_view_author_profile_portrait);
        mName = mHeader.findViewById(R.id.text_view_author_profile_name);
        mProfession = mHeader.findViewById(R.id.text_view_author_profile_profession);
        mMeta = mHeader.findViewById(R.id.text_view_author_profile_meta);
        mCallButton = mHeader.findViewById(R.id.image_button_author_profile_call);
        mEmailButton = mHeader.findViewById(R.id.image_button_author_profile_email);
        mWhatsappButton = mHeader.findViewById(R.id.image_button_author_profile_whatsapp);
        mNotice = mHeader.findViewById(R.id.linear_layout_author_profile_notice);
        mNoticeText = mHeader.findViewById(R.id.text_view_author_profile_notice);
        mBio = mHeader.findViewById(R.id.text_view_author_profile_bio);
        mBioToggle = mHeader.findViewById(R.id.text_view_author_profile_bio_toggle);
        mBioEmpty = mHeader.findViewById(R.id.text_view_author_profile_bio_empty);
        mBioSkeleton = mHeader.findViewById(R.id.linear_layout_author_profile_bio_skeleton);
        mNumbersSection = mHeader.findViewById(R.id.linear_layout_author_profile_numbers);
        mStatPublications = mHeader.findViewById(R.id.text_view_author_stat_publications);
        mStatPdf = mHeader.findViewById(R.id.text_view_author_stat_pdf);
        mStatAudio = mHeader.findViewById(R.id.text_view_author_stat_audio);
        mStatPhysical = mHeader.findViewById(R.id.text_view_author_stat_physical);
        mStatPdfBox = mHeader.findViewById(R.id.linear_layout_author_stat_pdf);
        mStatAudioBox = mHeader.findViewById(R.id.linear_layout_author_stat_audio);
        mStatPhysicalBox = mHeader.findViewById(R.id.linear_layout_author_stat_physical);
        mDomainsTitle = mHeader.findViewById(R.id.text_view_author_profile_domains_title);
        mDomainsHint = mHeader.findViewById(R.id.text_view_author_profile_domains_hint);
        mDomains = mHeader.findViewById(R.id.chip_group_author_profile_domains);
        mSimilarSection = mHeader.findViewById(R.id.linear_layout_author_profile_similar);
        mSimilarList = mHeader.findViewById(R.id.recycler_view_author_profile_similar);
        mSimilarEmpty = mHeader.findViewById(R.id.text_view_author_profile_similar_empty);
        mBibliographySection = mHeader.findViewById(R.id.linear_layout_author_profile_bibliography);
        mBibliographyTitle = mHeader.findViewById(R.id.text_view_author_profile_bibliography_title);
        mSearch = mHeader.findViewById(R.id.edit_text_author_profile_search);
        mSearchClear = mHeader.findViewById(R.id.image_button_author_profile_search_clear);
        mSortButton = mHeader.findViewById(R.id.text_view_author_profile_sort);
        mFormats = mHeader.findViewById(R.id.chip_group_author_profile_formats);
        mActiveCategory = mHeader.findViewById(R.id.chip_author_profile_active_category);

        findViewById(R.id.image_button_author_back).setOnClickListener(v -> finish());
        findViewById(R.id.text_view_author_not_found_back).setOnClickListener(v -> finish());
    }

    /** Affichage immédiat avec les données transmises par l'écran précédent. */
    private void showIntentPreview(Intent intent) {
        String name = ProfileText.fullName(intent.getStringExtra("intent_author_adapter_first_name"),
                intent.getStringExtra("intent_author_adapter_name"));
        setName(name);
        setProfession(intent.getStringExtra("intent_author_adapter_profession"));
        loadPortrait(intent.getStringExtra("intent_author_adapter_profile"));
        mMeta.setVisibility(View.GONE);
        mNumbersSection.setVisibility(View.GONE);
    }

    private void setupList() {
        mLayoutManager = new LinearLayoutManager(this);
        mList.setLayoutManager(mLayoutManager);
        mAdapter = new AuthorProfileAdapter(mHeader, this);
        mList.setAdapter(mAdapter);
        mList.setItemAnimator(null); // pas de clignotement à l'ajout des pages

        mList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView rv, int dx, int dy) {
                updateTopBar();
                // Chargement progressif : page suivante quand la fin approche.
                int lastVisible = mLayoutManager.findLastVisibleItemPosition() - 1; // -1 : en-tête
                if (mBooks.shouldPrefetch(lastVisible, PREFETCH_DISTANCE)) loadNextPage();
            }
        });

        mSwipe.setColorSchemeColors(ContextCompat.getColor(this, R.color.elim_primary));
        mSwipe.setOnRefreshListener(() -> {
            loadProfile(true);
            loadSimilar(true);
            reloadBibliography(true);
        });
    }

    /** Barre du haut : transparente sur le bandeau, puis surface + nom de l'auteur. */
    private void updateTopBar() {
        float progress = 1f;
        if (mLayoutManager.findFirstVisibleItemPosition() == 0) {
            View first = mLayoutManager.findViewByPosition(0);
            float scrolled = first != null ? -first.getTop() : 0f;
            float threshold = 150 * mDensity; // ≈ bas du portrait
            progress = Math.max(0f, Math.min(1f, (scrolled - threshold * 0.6f) / (threshold * 0.4f)));
        }
        int surface = ContextCompat.getColor(this, R.color.elim_surface);
        int alpha = Math.round(progress * 255);
        mTopBar.setBackgroundColor((surface & 0x00FFFFFF) | (alpha << 24));
        mTopBar.setElevation(progress >= 1f ? 3 * mDensity : 0f);
        mTopTitle.setAlpha(progress);
    }

    private void setupActions() {
        View.OnClickListener share = v -> shareProfile();
        findViewById(R.id.image_button_author_share_top).setOnClickListener(share);
        mHeader.findViewById(R.id.text_view_author_profile_share).setOnClickListener(share);
        mHeader.findViewById(R.id.text_view_author_profile_notice_retry).setOnClickListener(v -> {
            mNotice.setVisibility(View.GONE);
            loadProfile(true);
            loadSimilar(true);
            if (mBooks.hasError() || mBooks.isEmptyResult()) reloadBibliography(true);
        });
        mBioToggle.setOnClickListener(v -> {
            mBioExpanded = !mBioExpanded;
            mBio.setMaxLines(mBioExpanded ? Integer.MAX_VALUE : BIO_COLLAPSED_LINES);
            mBioToggle.setText(mBioExpanded ? R.string.author_profile_see_less : R.string.author_profile_see_more);
        });
        mCallButton.setOnClickListener(v -> callAuthor());
        mEmailButton.setOnClickListener(v -> emailAuthor());
        mWhatsappButton.setOnClickListener(v -> whatsappAuthor());
    }

    // =================================================================== profil

    private void loadProfile(boolean force) {
        String url = BibliographyQuery.profileUrl(Server.getUrlHostProd(this), mAuthorId);
        ApiCache.loadObject(this, url, ApiCache.TTL_AUTHOR_PROFILE, force, new CachedLoader.Callback() {
            @Override
            public void onCached(String body, long ageMs) {
                if (isGone()) return;
                applyProfile(body);
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
                // Hors ligne avec un profil déjà enregistré : on le garde et on le signale.
                // Jamais consulté : on le dit, sans prétendre que le profil est disponible.
                showNotice(hadCache ? R.string.author_profile_offline
                        : (mProfile == null ? R.string.author_profile_offline_empty : R.string.author_profile_error));
                if (mProfile == null) showBioState(null, true);
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
        try {
            AuthorProfile p = AuthorJson.profile(body);
            mProfile = p;
            String name = p.fullName();
            if (!name.isEmpty()) setName(name);
            setProfession(p.profession);
            loadPortrait(p.profile);
            bindMeta(p);
            showBioState(p.biography, false);
            bindNumbers(p);
            bindContacts(p);
            bindFormatChips(p);
        } catch (Exception e) {
            Log.e(TAG, "Profil illisible", e);
            showNotice(R.string.author_profile_error);
        }
    }

    private void setName(String name) {
        mName.setText(name);
        mTopTitle.setText(name);
        mPortrait.setContentDescription(getString(R.string.author_profile_portrait, name));
    }

    private void setProfession(String raw) {
        String profession = ProfileText.clean(raw);
        mProfession.setText(profession != null ? profession : "");
        mProfession.setVisibility(profession != null ? View.VISIBLE : View.GONE);
    }

    private void loadPortrait(String rawProfile) {
        String profile = ProfileText.clean(rawProfile);
        if (profile == null) {
            Picasso.get().cancelRequest(mPortrait);
            mPortrait.setImageResource(R.drawable.img_wait_profile);
            return;
        }
        Picasso.get()
                .load(Server.getUrlHostProd(this) + "/api/public/resource/0/profil/" + Uri.encode(profile))
                .placeholder(R.drawable.img_wait_profile)
                .error(R.drawable.img_wait_profile)
                .fit()
                .centerCrop()
                .into(mPortrait);
    }

    /** « 12 publications · PDF · Audio · Sur Elim depuis 2025 » — uniquement des valeurs calculées. */
    private void bindMeta(AuthorProfile p) {
        List<String> parts = new ArrayList<>();
        parts.add(p.publications > 0
                ? getResources().getQuantityString(R.plurals.author_profile_publications, p.publications, p.publications)
                : getString(R.string.author_profile_no_publication));
        String formats = ProfileText.formats(p.electronic, p.audio, p.physical,
                getString(R.string.author_stat_pdf), getString(R.string.author_stat_audio),
                getString(R.string.author_stat_physical));
        if (!formats.isEmpty()) parts.add(formats);
        String since = ProfileText.year(p.firstAddedAt);
        if (since != null) parts.add(getString(R.string.author_profile_since, since));
        mMeta.setText(ProfileText.join(parts, " · "));
        mMeta.setVisibility(View.VISIBLE);
        mBibliographyTitle.setText(p.publications > 0
                ? getString(R.string.author_profile_bibliography) + " · " + p.publications
                : getString(R.string.author_profile_bibliography));
    }

    private void showBioState(String biography, boolean failed) {
        mBioSkeleton.setVisibility(View.GONE);
        if (biography == null) {
            mBio.setVisibility(View.GONE);
            mBioToggle.setVisibility(View.GONE);
            // Échec sans aucune donnée : on n'affirme pas « pas de biographie ».
            mBioEmpty.setVisibility(failed ? View.GONE : View.VISIBLE);
            return;
        }
        mBioEmpty.setVisibility(View.GONE);
        mBio.setVisibility(View.VISIBLE);
        mBio.setText(biography); // texte brut : aucun HTML interprété
        mBio.setMaxLines(mBioExpanded ? Integer.MAX_VALUE : BIO_COLLAPSED_LINES);
        // « Voir plus » seulement si le texte dépasse réellement 6 lignes.
        mBio.post(() -> {
            Layout layout = mBio.getLayout();
            boolean overflow = mBio.getLineCount() > BIO_COLLAPSED_LINES
                    || (layout != null && layout.getLineCount() > 0
                    && layout.getEllipsisCount(layout.getLineCount() - 1) > 0);
            mBioToggle.setVisibility(overflow || mBioExpanded ? View.VISIBLE : View.GONE);
        });
    }

    private void bindNumbers(AuthorProfile p) {
        if (p.publications <= 0) {
            mNumbersSection.setVisibility(View.GONE); // pas de chiffres creux
            return;
        }
        mNumbersSection.setVisibility(View.VISIBLE);
        mStatPublications.setText(String.valueOf(p.publications));
        bindStat(mStatPdfBox, mStatPdf, p.electronic);
        bindStat(mStatAudioBox, mStatAudio, p.audio);
        bindStat(mStatPhysicalBox, mStatPhysical, p.physical);

        mDomains.removeAllViews();
        boolean hasDomains = !p.categories.isEmpty();
        mDomainsTitle.setVisibility(hasDomains ? View.VISIBLE : View.GONE);
        mDomainsHint.setVisibility(hasDomains ? View.VISIBLE : View.GONE);
        for (AuthorProfile.CategoryCount c : p.categories) {
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

    private void bindContacts(AuthorProfile p) {
        mCallButton.setVisibility(p.phone != null ? View.VISIBLE : View.GONE);
        mEmailButton.setVisibility(p.email != null ? View.VISIBLE : View.GONE);
        mWhatsappButton.setVisibility(p.whatsapp != null ? View.VISIBLE : View.GONE);
    }

    private void showNotice(int message) {
        mNoticeText.setText(message);
        mNotice.setVisibility(View.VISIBLE);
    }

    private void showNotFound() {
        mNotFound.setVisibility(View.VISIBLE);
        if (mSwipe != null) mSwipe.setVisibility(View.GONE);
        View share = findViewById(R.id.image_button_author_share_top);
        if (share != null) share.setVisibility(View.GONE);
    }

    // =================================================================== similaires

    private void loadSimilar(boolean force) {
        String url = BibliographyQuery.similarUrl(Server.getUrlHostProd(this), mAuthorId);
        ApiCache.loadList(this, url, ApiCache.TTL_AUTHOR_PROFILE, force, new CachedLoader.Callback() {
            @Override
            public void onCached(String body, long ageMs) {
                if (!isGone()) applySimilar(body);
            }

            @Override
            public void onFresh(String body) {
                if (!isGone()) applySimilar(body);
            }

            @Override
            public void onFailure(boolean hadCache) {
                // Section masquée si rien n'a jamais pu être chargé (pas de faux « aucun auteur »).
                if (!isGone() && !hadCache && mSimilarList.getAdapter() == null) {
                    mSimilarSection.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNotFound() {
                if (!isGone()) mSimilarSection.setVisibility(View.GONE);
            }
        });
    }

    private void applySimilar(String body) {
        List<Author> authors;
        try {
            authors = SimilarAuthors.filter(AuthorJson.similar(body), mAuthorId, Author::getIdNumber, MAX_SIMILAR);
        } catch (Exception e) {
            Log.e(TAG, "Auteurs similaires illisibles", e);
            return;
        }
        mSimilarSection.setVisibility(View.VISIBLE);
        mSimilarEmpty.setVisibility(authors.isEmpty() ? View.VISIBLE : View.GONE);
        mSimilarList.setVisibility(authors.isEmpty() ? View.GONE : View.VISIBLE);
        mSimilarList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        mSimilarList.setAdapter(new AuthorHorizontaleAdapter(authors));
    }

    // =================================================================== bibliographie

    private void setupBibliographyTools() {
        // Recherche (déclenchée 350 ms après la dernière frappe, côté serveur).
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

        // Tri
        updateSortLabel();
        mSortButton.setOnClickListener(this::showSortMenu);

        // Formats (les formats absents de la bibliographie sont masqués une fois le profil connu)
        addFormatChip(R.string.author_filter_all, null, true);
        addFormatChip(R.string.author_stat_pdf, BibliographyQuery.Format.PDF, false);
        addFormatChip(R.string.author_stat_audio, BibliographyQuery.Format.AUDIO, false);
        addFormatChip(R.string.author_stat_physical, BibliographyQuery.Format.PHYSICAL, false);

        // Domaine actif
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
            reloadBibliography();
        });
        mFormats.addView(chip);
    }

    /** Formats sans aucun livre : puce masquée (filtre inutile). Tous les formats si chiffres inconnus. */
    private void bindFormatChips(AuthorProfile p) {
        int shownFormats = 0;
        for (int i = 0; i < mFormats.getChildCount(); i++) {
            View chip = mFormats.getChildAt(i);
            BibliographyQuery.Format f = (BibliographyQuery.Format) chip.getTag();
            int count = f == null ? 1 : f == BibliographyQuery.Format.PDF ? p.electronic
                    : f == BibliographyQuery.Format.AUDIO ? p.audio : p.physical;
            boolean visible = f == null || count > 0 || f == mQuery.format;
            chip.setVisibility(visible ? View.VISIBLE : View.GONE);
            if (f != null && visible) shownFormats++;
        }
        // Un seul format : le filtre n'apporte rien, la rangée est masquée.
        mFormats.setVisibility(shownFormats >= 2 || mQuery.format != null ? View.VISIBLE : View.GONE);
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
                reloadBibliography();
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
        reloadBibliography();
        scrollToBibliography();
    }

    private void clearCategoryFilter() {
        mActiveCategory.setVisibility(View.GONE);
        if (mQuery.categoryId == null) return;
        mQuery = mQuery.withCategory(null);
        reloadBibliography();
    }

    /** Fait défiler jusqu'aux outils de la bibliographie (sous la barre du haut). */
    private void scrollToBibliography() {
        mList.post(() -> {
            int offset = -mBibliographySection.getTop() + mTopBar.getHeight();
            mLayoutManager.scrollToPositionWithOffset(0, offset);
            updateTopBar();
        });
    }

    private void reloadBibliography() {
        reloadBibliography(false);
    }

    /** Nouvelle requête (recherche, tri, filtre, actualisation) : repart de la page 1. */
    private void reloadBibliography(boolean force) {
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
        final BibliographyQuery query = mQuery;
        mBooks.markLoading();
        String url = query.pageUrl(Server.getUrlHostProd(this), page);
        ApiCache.loadList(this, url, ApiCache.TTL_AUTHOR_PROFILE, force, new CachedLoader.Callback() {
            @Override
            public void onCached(String body, long ageMs) {
                applyPage(generation, page, body);
            }

            @Override
            public void onFresh(String body) {
                applyPage(generation, page, body);
                if (page == 1) mSwipe.setRefreshing(false);
            }

            @Override
            public void onFailure(boolean hadCache) {
                if (isGone()) return;
                if (page == 1) mSwipe.setRefreshing(false);
                if (hadCache) return; // la page en cache reste affichée
                if (mBooks.onError(generation)) updateFooter();
            }

            @Override
            public void onNotFound() {
                if (isGone()) return;
                mSwipe.setRefreshing(false);
                showNotFound();
            }
        });
    }

    private void applyPage(int generation, int page, String body) {
        if (isGone()) return;
        List<Publication> items;
        try {
            items = AuthorJson.publications(body, mAuthorId);
        } catch (Exception e) {
            Log.e(TAG, "Bibliographie illisible", e);
            if (mBooks.onError(generation)) updateFooter();
            return;
        }
        // mayHaveMore : calculé sur la réponse brute (avant filtrage des éventuelles
        // incohérences) pour ne pas arrêter la pagination trop tôt.
        int rawCount;
        try {
            rawCount = new org.json.JSONArray(body).length();
        } catch (Exception e) {
            rawCount = items.size();
        }
        int added = mBooks.onPage(generation, page, items, BibliographyQuery.mayHaveMore(rawCount));
        if (added < 0) return; // réponse périmée (autre recherche) : ignorée
        if (page == 1) mAdapter.replaceAll(mBooks.items());
        else mAdapter.append(mBooks.items());
        updateFooter();
        // Page trop courte pour remplir l'écran : on enchaîne sur la suivante.
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
                String name = mName.getText() != null ? mName.getText().toString().trim() : "";
                emptyMessage = name.isEmpty() ? getString(R.string.author_no_books_message)
                        : getString(R.string.author_no_books_message_named, name);
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
        // Fiche du livre existante : lecture, écoute, téléchargement, favoris y sont déjà gérés.
        Intent intent = new Intent(this, BookActivity.class);
        intent.putExtra("intent_adapter_book_id", publication.id);
        startActivity(intent);
    }

    @Override
    public void onRetry() {
        mBooks.clearError();
        if (mBooks.loadedPages() == 0) {
            reloadBibliography(true);
        } else {
            mAdapter.setFooter(AuthorProfileAdapter.Footer.LOADING_MORE, null);
            loadPage(true);
        }
    }

    @Override
    public void onResetFilters() {
        mActiveCategory.setVisibility(View.GONE);
        mHandler.removeCallbacks(mRunSearch);
        mQuery = BibliographyQuery.forAuthor(mAuthorId).withSort(mQuery.sort);
        if (mSearch.getText().length() > 0) mSearch.setText(""); // ne relance pas : requête déjà vidée
        for (int i = 0; i < mFormats.getChildCount(); i++) {
            Chip chip = (Chip) mFormats.getChildAt(i);
            if (chip.getTag() == null) chip.setChecked(true);
        }
        reloadBibliography();
    }

    // =================================================================== partage & contacts

    private void shareProfile() {
        String name = mName.getText() != null ? mName.getText().toString() : "";
        String message = ProfileText.shareMessage(name, mProfile != null ? mProfile.profession : null,
                mProfile != null ? mProfile.publications : 0, getString(R.string.url_play_store));
        Intent send = new Intent(Intent.ACTION_SEND);
        send.setType("text/plain");
        send.putExtra(Intent.EXTRA_SUBJECT, name);
        send.putExtra(Intent.EXTRA_TEXT, message);
        try {
            startActivity(Intent.createChooser(send, getString(R.string.author_profile_share_chooser)));
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, R.string.author_profile_error, Toast.LENGTH_SHORT).show();
        }
    }

    private void callAuthor() {
        if (mProfile == null || mProfile.phone == null) return;
        // ACTION_DIAL : composeur pré-rempli, aucune permission d'appel nécessaire.
        startSafely(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + Uri.encode(mProfile.phone))));
    }

    private void emailAuthor() {
        if (mProfile == null || mProfile.email == null) return;
        Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:" + mProfile.email));
        intent.putExtra(Intent.EXTRA_TEXT, "Bonjour " + mName.getText() + ",");
        startSafely(Intent.createChooser(intent, getString(R.string.author_profile_email)));
    }

    private void whatsappAuthor() {
        if (mProfile == null || mProfile.whatsapp == null) return;
        String url = "https://wa.me/" + mProfile.whatsapp + "?text=" + Uri.encode("Bonjour " + mName.getText() + ",");
        startSafely(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
    }

    private void startSafely(Intent intent) {
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, R.string.author_profile_error, Toast.LENGTH_SHORT).show();
        }
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (imm != null) imm.hideSoftInputFromWindow(mSearch.getWindowToken(), 0);
        mSearch.clearFocus();
    }
}
