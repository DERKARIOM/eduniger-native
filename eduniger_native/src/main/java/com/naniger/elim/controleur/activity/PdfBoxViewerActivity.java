package com.naniger.elim.controleur.activity;

import android.content.Context;
import android.graphics.RectF;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.naniger.elim.R;
import com.naniger.elim.controleur.pdf.PageGeometry;
import com.naniger.elim.controleur.pdf.PdfDocument;
import com.naniger.elim.controleur.pdf.PdfPageAdapter;
import com.naniger.elim.controleur.pdf.ReadingPositionStore;
import com.naniger.elim.controleur.pdf.ZoomRecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lecteur PDF d'Elim (moteur natif Android PdfRenderer, sans dépendance payante).
 *
 * <p>Extras attendus (inchangés) : {@code PDF_PATH} (fichier local) et {@code PDF_TITLE}.
 *
 * <ul>
 *   <li>Lecture continue verticale ; seules les pages visibles et voisines sont rendues.</li>
 *   <li>Zoom par pincement et double-tap (1× à 4×), rendu plus net après le geste.</li>
 *   <li>Barre haute (retour, titre, recherche, aller à la page, menu) et barre basse
 *       (Page x / n + curseur). Tap sur la page : lecture plein écran.</li>
 *   <li>Reprise automatique à la dernière page lue.</li>
 *   <li>Recherche avec surlignage et navigation entre résultats : Android 15+.</li>
 *   <li>Couleurs inversées pour lire dans le noir (préférence mémorisée).</li>
 *   <li>États de chargement et d'erreur explicites (fichier absent, vide, protégé, endommagé).</li>
 * </ul>
 */
public class PdfBoxViewerActivity extends AppCompatActivity {

    private static final String STATE_PAGE = "page";

    private ZoomRecyclerView mPages;
    private LinearLayoutManager mLayoutManager;
    private View mTopBar, mSearchBar, mBottomBar, mLoading, mError;
    private TextView mTitle, mPageLabel, mPageChip, mErrorMessage, mSearchCount;
    private EditText mSearchInput;
    private ImageButton mSearchButton;
    private SeekBar mSeek;

    private PdfDocument mDocument;
    private PdfPageAdapter mAdapter;
    private ReadingPositionStore mStore;

    private String mPath;
    private int mPageCount;
    private int mCurrentPage;
    private int mRestorePage = -1;
    private boolean mChromeVisible = true;
    private boolean mSeekDragging;

    private final List<PdfDocument.SearchHit> mHits = new ArrayList<>();
    private int mHitIndex = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_pdfbox_viewer);
        // Lecture : l'écran ne doit pas s'éteindre en cours de page.
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mPath = getIntent().getStringExtra("PDF_PATH");
        String title = getIntent().getStringExtra("PDF_TITLE");
        mStore = new ReadingPositionStore(this);
        mRestorePage = savedInstanceState != null
                ? savedInstanceState.getInt(STATE_PAGE, -1)
                : mStore.lastPage(mPath);

        bindViews();
        mTitle.setText(title != null && !title.isEmpty() ? title : getString(R.string.app_name));
        applyInsets();
        setupActions();
        setupBackHandling();
        openDocument();
    }

    // ===================================================================== vues

    private void bindViews() {
        mPages = findViewById(R.id.reader_pages);
        mTopBar = findViewById(R.id.reader_top_bar);
        mSearchBar = findViewById(R.id.reader_search_bar);
        mBottomBar = findViewById(R.id.reader_bottom_bar);
        mLoading = findViewById(R.id.reader_loading);
        mError = findViewById(R.id.reader_error);
        mTitle = findViewById(R.id.reader_title);
        mPageLabel = findViewById(R.id.reader_page_label);
        mPageChip = findViewById(R.id.reader_page_chip);
        mErrorMessage = findViewById(R.id.reader_error_message);
        mSearchCount = findViewById(R.id.reader_search_count);
        mSearchInput = findViewById(R.id.reader_search_input);
        mSearchButton = findViewById(R.id.reader_search);
        mSeek = findViewById(R.id.reader_seek);

        mLayoutManager = new LinearLayoutManager(this);
        mLayoutManager.setInitialPrefetchItemCount(2);
        mPages.setLayoutManager(mLayoutManager);
        mPages.setItemAnimator(null);          // pas de fondu parasite à chaque rendu
        mPages.setItemViewCacheSize(2);
        mPages.setHasFixedSize(true);
    }

    /** Barres sous la barre d'état / au-dessus de la barre de navigation (Android 15+ plein écran). */
    private void applyInsets() {
        View root = findViewById(R.id.reader_root);
        final int topBase = mTopBar.getPaddingTop();
        final int bottomBase = mBottomBar.getPaddingBottom();
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            // « IgnoringVisibility » : en plein écran (barres système masquées) les marges ne
            // changent pas, donc la page lue ne saute pas.
            Insets bars = insets.getInsetsIgnoringVisibility(WindowInsetsCompat.Type.systemBars()
                    | WindowInsetsCompat.Type.displayCutout());
            mTopBar.setPadding(mTopBar.getPaddingLeft(), topBase + bars.top, mTopBar.getPaddingRight(), mTopBar.getPaddingBottom());
            mSearchBar.setPadding(mSearchBar.getPaddingLeft(), topBase + bars.top, mSearchBar.getPaddingRight(), mSearchBar.getPaddingBottom());
            mBottomBar.setPadding(mBottomBar.getPaddingLeft(), mBottomBar.getPaddingTop(), mBottomBar.getPaddingRight(), bottomBase + bars.bottom);
            // Espace pour que la première et la dernière page ne restent pas sous les barres.
            mPages.post(() -> mPages.setPadding(mPages.getPaddingLeft(), mTopBar.getHeight() + dp(8),
                    mPages.getPaddingRight(), mBottomBar.getHeight() + dp(8)));
            return insets;
        });
    }

    private void setupActions() {
        findViewById(R.id.reader_back).setOnClickListener(v -> finish());
        findViewById(R.id.reader_error_close).setOnClickListener(v -> finish());
        findViewById(R.id.reader_goto).setOnClickListener(v -> showGoToPageDialog());
        findViewById(R.id.reader_more).setOnClickListener(this::showMoreMenu);

        if (PdfDocument.isSearchSupported()) {
            mSearchButton.setVisibility(View.VISIBLE);
            mSearchButton.setOnClickListener(v -> openSearch());
        }
        findViewById(R.id.reader_search_close).setOnClickListener(v -> closeSearch());
        findViewById(R.id.reader_search_prev).setOnClickListener(v -> moveHit(-1));
        findViewById(R.id.reader_search_next).setOnClickListener(v -> moveHit(1));
        mSearchInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH
                    || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                runSearch(mSearchInput.getText().toString());
                return true;
            }
            return false;
        });

        mPages.setListener(new ZoomRecyclerView.Listener() {
            @Override
            public void onSingleTap() {
                setChromeVisible(!mChromeVisible);
            }

            @Override
            public void onZoomSettled(float scale) {
                if (mAdapter != null) {
                    mAdapter.setRenderZoom(scale, mLayoutManager.findFirstVisibleItemPosition(),
                            mLayoutManager.findLastVisibleItemPosition());
                }
            }
        });

        mPages.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@androidx.annotation.NonNull RecyclerView rv, int dx, int dy) {
                updateCurrentPage();
            }

            @Override
            public void onScrollStateChanged(@androidx.annotation.NonNull RecyclerView rv, int state) {
                if (state == RecyclerView.SCROLL_STATE_IDLE && mAdapter != null) {
                    mAdapter.prefetch(mLayoutManager.findFirstVisibleItemPosition(),
                            mLayoutManager.findLastVisibleItemPosition());
                }
            }
        });

        mSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) showPageLabel(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                mSeekDragging = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mSeekDragging = false;
                goToPage(seekBar.getProgress());
            }
        });
    }

    private void setupBackHandling() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (mSearchBar.getVisibility() == View.VISIBLE) {
                    closeSearch();
                } else if (mPages.getScale() > 1.01f) {
                    mPages.resetZoom();
                    onZoomReset();
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });
    }

    // =============================================================== document

    private void openDocument() {
        mDocument = new PdfDocument();
        mDocument.open(mPath, new PdfDocument.OpenCallback() {
            @Override
            public void onOpened(int pageCount) {
                mPageCount = pageCount;
                mAdapter = new PdfPageAdapter(mDocument, dp(8));
                mAdapter.setInverted(mStore.invertColors());
                mPages.setAdapter(mAdapter);
                mPages.post(() -> mAdapter.setPageWidth(
                        mPages.getWidth() - mPages.getPaddingLeft() - mPages.getPaddingRight()));
                mSeek.setMax(Math.max(0, pageCount - 1));
                mLoading.setVisibility(View.GONE);

                int start = (mRestorePage > 0 && mRestorePage < pageCount) ? mRestorePage : 0;
                mCurrentPage = start;
                showPageLabel(start);
                if (start > 0) {
                    mPages.post(() -> mLayoutManager.scrollToPositionWithOffset(start, 0));
                    Toast.makeText(PdfBoxViewerActivity.this,
                            getString(R.string.reader_resume, start + 1), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(String reason) {
                mLoading.setVisibility(View.GONE);
                mTopBar.setVisibility(View.GONE);
                mBottomBar.setVisibility(View.GONE);
                mErrorMessage.setText(reason);
                mError.setVisibility(View.VISIBLE);
            }
        });
    }

    private void updateCurrentPage() {
        if (mPageCount == 0) return;
        int first = mLayoutManager.findFirstVisibleItemPosition();
        if (first == RecyclerView.NO_POSITION) return;
        View v = mLayoutManager.findViewByPosition(first);
        int bottom = v != null ? v.getBottom() : 0;
        int page = PageGeometry.currentPage(first, bottom, mPages.getHeight(), mPageCount);
        if (page != mCurrentPage) {
            mCurrentPage = page;
            if (!mSeekDragging) {
                mSeek.setProgress(page);
                showPageLabel(page);
            }
        }
    }

    private void showPageLabel(int page) {
        mPageLabel.setText(getString(R.string.reader_page_of, page + 1, mPageCount));
        mPageChip.setText(getString(R.string.reader_page_chip, page + 1, mPageCount));
        if (!mChromeVisible) flashChip();
    }

    private void goToPage(int page) {
        if (page < 0 || page >= mPageCount) return;
        mCurrentPage = page;
        mLayoutManager.scrollToPositionWithOffset(page, 0);
        mSeek.setProgress(page);
        showPageLabel(page);
    }

    private void onZoomReset() {
        if (mAdapter != null) {
            mAdapter.setRenderZoom(1f, mLayoutManager.findFirstVisibleItemPosition(),
                    mLayoutManager.findLastVisibleItemPosition());
        }
    }

    // ============================================================ plein écran

    private void setChromeVisible(boolean visible) {
        mChromeVisible = visible;
        View top = mSearchBar.getVisibility() == View.VISIBLE ? mSearchBar : mTopBar;
        top.animate().translationY(visible ? 0 : -top.getHeight()).alpha(visible ? 1f : 0f).setDuration(200).start();
        mBottomBar.animate().translationY(visible ? 0 : mBottomBar.getHeight()).alpha(visible ? 1f : 0f).setDuration(200).start();
        WindowInsetsControllerCompat c = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        c.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
        if (visible) c.show(WindowInsetsCompat.Type.systemBars());
        else {
            c.hide(WindowInsetsCompat.Type.systemBars());
            flashChip();
        }
    }

    private void flashChip() {
        mPageChip.animate().cancel();
        mPageChip.setAlpha(1f);
        mPageChip.animate().alpha(0f).setStartDelay(1500).setDuration(400).start();
    }

    // =============================================================== actions

    private void showGoToPageDialog() {
        if (mPageCount == 0) return;
        EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setHint(getString(R.string.reader_goto_hint, mPageCount));
        input.setGravity(Gravity.CENTER);
        input.setText(String.valueOf(mCurrentPage + 1));
        input.selectAll();
        int pad = dp(24);
        android.widget.FrameLayout box = new android.widget.FrameLayout(this);
        box.setPadding(pad, dp(8), pad, 0);
        box.addView(input);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.reader_goto_title)
                .setView(box)
                .setPositiveButton(R.string.reader_ok, null)
                .setNegativeButton(R.string.reader_cancel, null)
                .create();
        dialog.setOnShowListener(d -> {
            input.requestFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) imm.showSoftInput(input, InputMethodManager.SHOW_IMPLICIT);
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                int page = PageGeometry.parsePageInput(input.getText().toString(), mPageCount);
                if (page < 0) {
                    input.setError(getString(R.string.reader_goto_invalid, mPageCount));
                    return;
                }
                goToPage(page);
                dialog.dismiss();
            });
        });
        dialog.show();
    }

    private void showMoreMenu(View anchor) {
        PopupMenu menu = new PopupMenu(this, anchor);
        menu.getMenu().add(0, 1, 0, R.string.reader_invert)
                .setCheckable(true).setChecked(mStore.invertColors());
        menu.getMenu().add(0, 2, 1, R.string.reader_fit_width);
        menu.getMenu().add(0, 3, 2, R.string.reader_first_page);
        menu.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case 1:
                    boolean invert = !mStore.invertColors();
                    mStore.setInvertColors(invert);
                    if (mAdapter != null) mAdapter.setInverted(invert);
                    return true;
                case 2:
                    mPages.resetZoom();
                    onZoomReset();
                    return true;
                case 3:
                    goToPage(0);
                    return true;
                default:
                    return false;
            }
        });
        menu.show();
    }

    // ============================================================= recherche

    private void openSearch() {
        mTopBar.setVisibility(View.GONE);
        mSearchBar.setVisibility(View.VISIBLE);
        mSearchCount.setText("");
        mSearchInput.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) imm.showSoftInput(mSearchInput, InputMethodManager.SHOW_IMPLICIT);
    }

    private void closeSearch() {
        mDocument.cancelSearch();
        hideKeyboard();
        mSearchBar.setVisibility(View.GONE);
        mTopBar.setVisibility(View.VISIBLE);
        mHits.clear();
        mHitIndex = -1;
        if (mAdapter != null) mAdapter.setHighlights(null, -1, null);
    }

    private void runSearch(String query) {
        hideKeyboard();
        if (query.trim().isEmpty()) return;
        mSearchCount.setText(getString(R.string.reader_searching, 0));
        mDocument.search(query, new PdfDocument.SearchCallback() {
            @Override
            public void onProgress(int done, int total) {
                mSearchCount.setText(getString(R.string.reader_searching, done * 100 / Math.max(1, total)));
            }

            @Override
            public void onDone(List<PdfDocument.SearchHit> hits) {
                mHits.clear();
                mHits.addAll(hits);
                if (mHits.isEmpty()) {
                    mHitIndex = -1;
                    mSearchCount.setText(R.string.reader_search_none);
                    if (mAdapter != null) mAdapter.setHighlights(null, -1, null);
                    return;
                }
                // Premier résultat à partir de la page courante.
                mHitIndex = 0;
                for (int i = 0; i < mHits.size(); i++) {
                    if (mHits.get(i).page >= mCurrentPage) { mHitIndex = i; break; }
                }
                showHit();
            }
        });
    }

    private void moveHit(int delta) {
        if (mHits.isEmpty()) return;
        mHitIndex = (mHitIndex + delta + mHits.size()) % mHits.size();
        showHit();
    }

    private void showHit() {
        PdfDocument.SearchHit hit = mHits.get(mHitIndex);
        Map<Integer, List<RectF>> perPage = new HashMap<>();
        for (PdfDocument.SearchHit h : mHits) {
            List<RectF> l = perPage.get(h.page);
            if (l == null) { l = new ArrayList<>(); perPage.put(h.page, l); }
            l.addAll(h.rects);
        }
        mAdapter.setHighlights(perPage, hit.page, hit.rects);
        mSearchCount.setText((mHitIndex + 1) + " / " + mHits.size());
        // Amène le résultat à environ un quart de l'écran.
        float pw = mDocument.pageWidthPoints(hit.page);
        int viewW = mPages.getWidth() - mPages.getPaddingLeft() - mPages.getPaddingRight();
        int offsetInPage = pw > 0 && !hit.rects.isEmpty() ? Math.round(hit.rects.get(0).top * viewW / pw) : 0;
        mLayoutManager.scrollToPositionWithOffset(hit.page, -offsetInPage + mPages.getHeight() / 4);
        mCurrentPage = hit.page;
        mSeek.setProgress(hit.page);
        showPageLabel(hit.page);
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) imm.hideSoftInputFromWindow(mSearchInput.getWindowToken(), 0);
    }

    // ============================================================ cycle de vie

    @Override
    protected void onPause() {
        super.onPause();
        if (mPageCount > 0) mStore.savePage(mPath, mCurrentPage);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_PAGE, mCurrentPage);
    }

    @Override
    protected void onDestroy() {
        if (mAdapter != null) mAdapter.release();
        if (mDocument != null) mDocument.close();
        super.onDestroy();
    }

    private int dp(int v) {
        return Math.round(v * getResources().getDisplayMetrics().density);
    }
}
