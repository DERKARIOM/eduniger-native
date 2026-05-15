package com.ninotech.eduniger.controleur.activity;

import android.app.UiModeManager;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.pdf.PdfRenderer;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelFileDescriptor;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.ninotech.eduniger.R;
import com.ninotech.eduniger.model.data.Themes;

import java.io.File;
import java.io.IOException;

/**
 * Visionneuse PDF moderne utilisant l'API Android native PdfRenderer
 * Compatible Android 5.0+ (API 21+)
 * Version avec mode vertical continu + saisie directe du numéro de page
 * + mode plein écran immersif
 */
public class PdfBoxViewerActivity extends AppCompatActivity {

    private static final String TAG = "PdfBoxViewer";

    // ── Vues existantes ──────────────────────────────────────────────────────
    private ImageView imageViewPdf;
    private TextView textViewPageInfo;
    private TextView textViewZoomLevel;
    private TextView textViewSwipeHint;
    private TextView textViewLoading;
    private ImageButton btnPrevious, btnNext, btnZoomIn, btnZoomOut;
    private ProgressBar progressBar;
    private ProgressBar progressBarPage;
    private ScrollView scrollViewVertical;
    private HorizontalScrollView scrollViewHorizontal;
    private LinearLayout controlLayout;
    private FrameLayout pdfContainer;

    // ── Nouvelles vues ───────────────────────────────────────────────────────
    private ScrollView scrollViewAllPages;
    private LinearLayout containerAllPages;
    private ImageButton btnToggleMode;
    private TextView textViewMode;
    private EditText editTextPageNumber;
    private TextView textViewTotalPages;
    private LinearLayout toolbarSecondary;
    private LinearLayout loadingLayout;

    // ── Boutons plein écran ──────────────────────────────────────────────────
    private ImageButton btnFullscreen;
    private ImageButton btnFullscreenCenter;

    // ── État ─────────────────────────────────────────────────────────────────
    private boolean isVerticalMode  = false;
    private boolean isFullscreen    = false;
    private boolean uiVisible       = true;

    // ── Auto-masquage UI en plein écran ──────────────────────────────────────
    private final Handler autoHideHandler = new Handler(Looper.getMainLooper());
    private static final int AUTO_HIDE_DELAY_MS = 3000;
    private final Runnable autoHideRunnable = this::hideSystemAndAppUI;

    // ── PDF ──────────────────────────────────────────────────────────────────
    private PdfRenderer pdfRenderer;
    private ParcelFileDescriptor fileDescriptor;

    private String pdfPath;
    private String pdfTitle;
    private int currentPage = 0;
    private int totalPages  = 0;
    private float zoomLevel = 1.0f;

    // ── Écran ────────────────────────────────────────────────────────────────
    private int screenWidth;
    private int screenHeight;
    private int screenDensity;
    private boolean isTablet    = false;
    private boolean isLandscape = false;

    // ── Gestes ──────────────────────────────────────────────────────────────
    private GestureDetector gestureDetector;
    private static final int SWIPE_THRESHOLD          = 100;
    private static final int SWIPE_VELOCITY_THRESHOLD = 100;

    // ── Zoom ─────────────────────────────────────────────────────────────────
    private static final float MAX_ZOOM  = 5.0f;
    private static final float MIN_ZOOM  = 0.5f;
    private static final float ZOOM_STEP = 0.25f;

    // =========================================================================
    // onCreate
    // =========================================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate() démarré");

        // Fond noir dès le départ pour éviter le flash blanc
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.BLACK));

        setContentView(R.layout.activity_pdfbox_viewer);

        detectScreenCharacteristics();

        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            ab.setCustomView(R.layout.custom_action_bar);
            ab.setDisplayHomeAsUpEnabled(true);
            setupResponsiveActionBar(ab);
            TextView actionBarTitle = ab.getCustomView().findViewById(R.id.action_bar_title);
            setupTheme(ab, actionBarTitle);
            if (actionBarTitle != null) actionBarTitle.setText(pdfTitle);
        }

        pdfPath  = getIntent().getStringExtra("PDF_PATH");
        pdfTitle = getIntent().getStringExtra("PDF_TITLE");

        initViews();
        setupResponsiveLayouts();
        setupGestureDetector();
        setupFullscreenToggle();
        loadPdfDocument();

        Log.d(TAG, "onCreate() terminé");
    }

    // =========================================================================
    // Plein écran
    // =========================================================================

    /**
     * Configure les boutons plein écran (toolbar + centre).
     */
    private void setupFullscreenToggle() {
        View.OnClickListener fsListener = v -> toggleFullscreen();
        if (btnFullscreen       != null) btnFullscreen.setOnClickListener(fsListener);
        if (btnFullscreenCenter != null) btnFullscreenCenter.setOnClickListener(fsListener);
    }

    /**
     * Bascule entre mode normal et mode plein écran immersif.
     * En plein écran : ActionBar + barres système masquées, UI overlay auto-masquée.
     */
    private void toggleFullscreen() {
        isFullscreen = !isFullscreen;
        Log.d(TAG, "toggleFullscreen() → isFullscreen=" + isFullscreen);

        if (isFullscreen) {
            enterFullscreen();
        } else {
            exitFullscreen();
        }
    }

    private void enterFullscreen() {
        // Masquer l'ActionBar
        ActionBar ab = getSupportActionBar();
        if (ab != null) ab.hide();

        // Mode immersif sticky
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        );

        // Garder l'écran allumé en plein écran
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Lancer l'auto-masquage de l'UI overlay
        scheduleAutoHide();

        // Animation de sortie de la toolbar et du controlLayout
        animateUIOut();

        uiVisible = true; // sera mis à false par hideSystemAndAppUI()
    }

    private void exitFullscreen() {
        // Rétablir l'ActionBar
        ActionBar ab = getSupportActionBar();
        if (ab != null) ab.show();

        // Restaurer les barres système
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        );

        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Annuler l'auto-masquage
        autoHideHandler.removeCallbacks(autoHideRunnable);

        // Montrer l'UI
        showAppUI();
    }

    /**
     * Masque les barres système ET l'UI overlay (toolbar + contrôles).
     * Appelé automatiquement après AUTO_HIDE_DELAY_MS en plein écran.
     */
    private void hideSystemAndAppUI() {
        if (!isFullscreen) return;
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        );
        animateUIOut();
        uiVisible = false;
    }

    /**
     * Montre l'UI overlay, puis planifie un nouveau masquage.
     * Appelé lors d'un tap sur l'écran en plein écran.
     */
    private void showAppUI() {
        animateUIIn();
        uiVisible = true;
        if (isFullscreen) scheduleAutoHide();
    }

    private void scheduleAutoHide() {
        autoHideHandler.removeCallbacks(autoHideRunnable);
        autoHideHandler.postDelayed(autoHideRunnable, AUTO_HIDE_DELAY_MS);
    }

    // ── Animations UI overlay ─────────────────────────────────────────────────

    private void animateUIIn() {
        if (toolbarSecondary != null) {
            toolbarSecondary.setVisibility(View.VISIBLE);
            toolbarSecondary.animate().alpha(1f).translationY(0).setDuration(250).start();
        }
        if (progressBarPage != null) {
            progressBarPage.setVisibility(View.VISIBLE);
            progressBarPage.animate().alpha(1f).setDuration(250).start();
        }
        if (controlLayout != null) {
            controlLayout.setVisibility(View.VISIBLE);
            controlLayout.animate().alpha(1f).translationY(0).setDuration(250).start();
        }
    }

    private void animateUIOut() {
        float toolbarH = toolbarSecondary != null ? toolbarSecondary.getHeight() : 0;
        float controlH = controlLayout != null ? controlLayout.getHeight() : 0;

        if (toolbarSecondary != null) {
            toolbarSecondary.animate().alpha(0f).translationY(-toolbarH)
                    .setDuration(300)
                    .withEndAction(() -> {
                        if (!uiVisible) toolbarSecondary.setVisibility(View.INVISIBLE);
                    }).start();
        }
        if (progressBarPage != null) {
            progressBarPage.animate().alpha(0f).setDuration(300)
                    .withEndAction(() -> {
                        if (!uiVisible) progressBarPage.setVisibility(View.INVISIBLE);
                    }).start();
        }
        if (controlLayout != null) {
            controlLayout.animate().alpha(0f).translationY(controlH)
                    .setDuration(300)
                    .withEndAction(() -> {
                        if (!uiVisible) controlLayout.setVisibility(View.INVISIBLE);
                    }).start();
        }
    }

    // =========================================================================
    // Chargement du PDF
    // =========================================================================

    private void loadPdfDocument() {
        Log.d(TAG, "loadPdfDocument() → lancement de LoadPdfTask");
        new LoadPdfTask().execute(pdfPath);
    }

    // =========================================================================
    // Détection écran
    // =========================================================================

    private void detectScreenCharacteristics() {
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        screenWidth   = dm.widthPixels;
        screenHeight  = dm.heightPixels;
        screenDensity = dm.densityDpi;
        isTablet   = (getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK)
                >= Configuration.SCREENLAYOUT_SIZE_LARGE;
        isLandscape = getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
        Log.d(TAG, "Écran : " + screenWidth + "x" + screenHeight
                + " | densité=" + screenDensity + " | tablette=" + isTablet + " | paysage=" + isLandscape);
    }

    // =========================================================================
    // ActionBar
    // =========================================================================

    private void setupResponsiveActionBar(ActionBar ab) {
        View cv = ab.getCustomView();
        if (cv != null) {
            ViewGroup.LayoutParams p = cv.getLayoutParams();
            p.height = isTablet ? dp(80) : screenDensity >= DisplayMetrics.DENSITY_XXHIGH ? dp(70) : dp(56);
            cv.setLayoutParams(p);
        }
    }

    // =========================================================================
    // Layouts
    // =========================================================================

    private void setupResponsiveLayouts() {
        setupResponsiveControls();
        setupResponsiveTextSizes();
        if (isTablet) setupTabletLayout();
    }

    private void setupResponsiveControls() {
        int sz = isTablet ? dp(64) : screenDensity >= DisplayMetrics.DENSITY_XXHIGH ? dp(56) : dp(50);
        if (btnPrevious != null) {
            setButtonSize(btnPrevious, sz);
            setButtonSize(btnNext,     sz);
            setButtonSize(btnZoomIn,   sz);
            setButtonSize(btnZoomOut,  sz);
        }
    }

    private void setButtonSize(ImageButton btn, int size) {
        ViewGroup.LayoutParams p = btn.getLayoutParams();
        p.width  = size;
        p.height = size;
        btn.setLayoutParams(p);
    }

    private void setupTabletLayout() {
        if (isLandscape && controlLayout != null) {
            ViewGroup.LayoutParams p = controlLayout.getLayoutParams();
            p.height = dp(100);
            controlLayout.setLayoutParams(p);
        }
        if (textViewSwipeHint != null) textViewSwipeHint.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
    }

    private void setupResponsiveTextSizes() {
        float pageInfoSize, zoomSize, hintSize, loadingSize;
        if (isTablet) {
            pageInfoSize = 18f; zoomSize = 14f; hintSize = 20f; loadingSize = 16f;
        } else if (screenDensity >= DisplayMetrics.DENSITY_XXHIGH) {
            pageInfoSize = 16f; zoomSize = 12f; hintSize = 18f; loadingSize = 14f;
        } else {
            pageInfoSize = 14f; zoomSize = 10f; hintSize = 16f; loadingSize = 12f;
        }
        if (textViewPageInfo  != null) textViewPageInfo.setTextSize(TypedValue.COMPLEX_UNIT_SP, pageInfoSize);
        if (textViewZoomLevel != null) textViewZoomLevel.setTextSize(TypedValue.COMPLEX_UNIT_SP, zoomSize);
        if (textViewSwipeHint != null) textViewSwipeHint.setTextSize(TypedValue.COMPLEX_UNIT_SP, hintSize);
        if (textViewLoading   != null) textViewLoading.setTextSize(TypedValue.COMPLEX_UNIT_SP, loadingSize);
    }

    // =========================================================================
    // Vues
    // =========================================================================

    private void initViews() {
        Log.d(TAG, "initViews() démarré");

        imageViewPdf         = findViewById(R.id.imageViewPdf);
        textViewPageInfo     = findViewById(R.id.textViewPageInfo);
        textViewZoomLevel    = findViewById(R.id.textViewZoomLevel);
        textViewSwipeHint    = findViewById(R.id.textViewSwipeHint);
        textViewLoading      = findViewById(R.id.textViewLoading);
        btnPrevious          = findViewById(R.id.btnPrevious);
        btnNext              = findViewById(R.id.btnNext);
        btnZoomIn            = findViewById(R.id.btnZoomIn);
        btnZoomOut           = findViewById(R.id.btnZoomOut);
        progressBar          = findViewById(R.id.progressBar);
        progressBarPage      = findViewById(R.id.progressBarPage);
        scrollViewVertical   = findViewById(R.id.scrollViewVertical);
        scrollViewHorizontal = findViewById(R.id.scrollViewHorizontal);
        controlLayout        = findViewById(R.id.controlLayout);
        pdfContainer         = findViewById(R.id.pdfContainer);

        scrollViewAllPages  = findViewById(R.id.scrollViewAllPages);
        containerAllPages   = findViewById(R.id.containerAllPages);
        btnToggleMode       = findViewById(R.id.btnToggleMode);
        textViewMode        = findViewById(R.id.textViewMode);
        editTextPageNumber  = findViewById(R.id.editTextPageNumber);
        textViewTotalPages  = findViewById(R.id.textViewTotalPages);
        toolbarSecondary    = findViewById(R.id.toolbarSecondary);
        loadingLayout       = findViewById(R.id.loadingLayout);

        btnFullscreen       = findViewById(R.id.btnFullscreen);
        btnFullscreenCenter = findViewById(R.id.btnFullscreenCenter);

        setupImageView();

        if (btnPrevious != null) btnPrevious.setOnClickListener(v -> previousPage());
        if (btnNext     != null) btnNext.setOnClickListener(v -> nextPage());
        if (btnZoomIn   != null) btnZoomIn.setOnClickListener(v -> zoomIn());
        if (btnZoomOut  != null) btnZoomOut.setOnClickListener(v -> zoomOut());

        if (btnToggleMode != null) {
            btnToggleMode.setOnClickListener(v -> toggleReadingMode());
        }

        if (editTextPageNumber != null) {
            editTextPageNumber.setOnEditorActionListener((v, actionId, event) -> {
                if (actionId == EditorInfo.IME_ACTION_GO
                        || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER
                        && event.getAction() == KeyEvent.ACTION_DOWN)) {
                    navigateToEnteredPage();
                    return true;
                }
                return false;
            });
            editTextPageNumber.setOnKeyListener((v, keyCode, event) -> {
                if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
                    navigateToEnteredPage();
                    return true;
                }
                return false;
            });
        }

        setupScrollViews();

        // Tap sur le PDF : toggle UI en plein écran
        if (pdfContainer != null) {
            pdfContainer.setOnClickListener(v -> {
                if (isFullscreen) {
                    if (uiVisible) {
                        autoHideHandler.removeCallbacks(autoHideRunnable);
                        hideSystemAndAppUI();
                    } else {
                        showAppUI();
                    }
                }
            });
        }

        Log.d(TAG, "initViews() terminé");
    }

    private void setupImageView() {
        if (imageViewPdf != null) {
            imageViewPdf.setScaleType(ImageView.ScaleType.FIT_CENTER);
            imageViewPdf.setAdjustViewBounds(true);
        }
    }

    private void setupScrollViews() {
        if (scrollViewVertical   != null) { scrollViewVertical.setVerticalScrollBarEnabled(true);     scrollViewVertical.setScrollbarFadingEnabled(true); }
        if (scrollViewHorizontal != null) { scrollViewHorizontal.setHorizontalScrollBarEnabled(true); scrollViewHorizontal.setScrollbarFadingEnabled(true); }
        if (scrollViewAllPages   != null) { scrollViewAllPages.setVerticalScrollBarEnabled(true);     scrollViewAllPages.setScrollbarFadingEnabled(true); }
    }

    // =========================================================================
    // Mode de lecture
    // =========================================================================

    private void toggleReadingMode() {
        isVerticalMode = !isVerticalMode;
        Log.d(TAG, "toggleReadingMode() → isVerticalMode=" + isVerticalMode);

        if (isVerticalMode) {
            if (textViewMode       != null) textViewMode.setText("Vertical");
            if (scrollViewVertical != null) scrollViewVertical.setVisibility(View.GONE);
            if (scrollViewAllPages != null) scrollViewAllPages.setVisibility(View.VISIBLE);
            if (btnZoomIn  != null) { btnZoomIn.setEnabled(false);  btnZoomIn.setAlpha(0.3f); }
            if (btnZoomOut != null) { btnZoomOut.setEnabled(false); btnZoomOut.setAlpha(0.3f); }
            if (textViewSwipeHint != null) textViewSwipeHint.setVisibility(View.GONE);
            new RenderAllPagesTask().execute();
        } else {
            if (textViewMode       != null) textViewMode.setText("Mode page");
            if (scrollViewVertical != null) scrollViewVertical.setVisibility(View.VISIBLE);
            if (scrollViewAllPages != null) scrollViewAllPages.setVisibility(View.GONE);
            if (btnZoomIn  != null) { btnZoomIn.setEnabled(true);  btnZoomIn.setAlpha(1.0f); }
            if (btnZoomOut != null) { btnZoomOut.setEnabled(true); btnZoomOut.setAlpha(1.0f); }
            if (imageViewPdf != null) imageViewPdf.setVisibility(View.VISIBLE);
            renderPage(currentPage);
        }
    }

    // =========================================================================
    // Saisie du numéro de page
    // =========================================================================

    private void navigateToEnteredPage() {
        if (editTextPageNumber == null || totalPages == 0) return;
        String raw = editTextPageNumber.getText().toString().trim();
        if (raw.isEmpty()) return;
        try {
            int page = Integer.parseInt(raw);
            if (page < 1 || page > totalPages) {
                Toast.makeText(this, "Page invalide. Entrez un numéro entre 1 et " + totalPages, Toast.LENGTH_SHORT).show();
                editTextPageNumber.setText(String.valueOf(currentPage + 1));
                return;
            }
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) imm.hideSoftInputFromWindow(editTextPageNumber.getWindowToken(), 0);
            editTextPageNumber.clearFocus();

            if (isVerticalMode) {
                scrollToPageInVerticalMode(page - 1);
            } else {
                renderPage(page - 1);
                resetScrollPosition();
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Numéro de page invalide", Toast.LENGTH_SHORT).show();
            editTextPageNumber.setText(String.valueOf(currentPage + 1));
        }
    }

    private void scrollToPageInVerticalMode(int pageIndex) {
        if (containerAllPages == null || scrollViewAllPages == null) return;
        if (pageIndex < 0 || pageIndex >= containerAllPages.getChildCount()) return;
        View target = containerAllPages.getChildAt(pageIndex);
        if (target == null) return;
        currentPage = pageIndex;
        updatePageInfo();
        scrollViewAllPages.post(() -> {
            int[] loc = new int[2];
            target.getLocationInWindow(loc);
            int[] parentLoc = new int[2];
            scrollViewAllPages.getLocationInWindow(parentLoc);
            int scrollY = scrollViewAllPages.getScrollY() + loc[1] - parentLoc[1];
            scrollViewAllPages.smoothScrollTo(0, scrollY);
        });
    }

    // =========================================================================
    // Gestes
    // =========================================================================

    private void setupGestureDetector() {
        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2,
                                   float velocityX, float velocityY) {
                if (isVerticalMode) return false;
                try {
                    float diffX = e2.getX() - e1.getX();
                    float diffY = e2.getY() - e1.getY();
                    float density        = getResources().getDisplayMetrics().density;
                    int adaptiveSwipe    = (int)(SWIPE_THRESHOLD          * density);
                    int adaptiveVelocity = (int)(SWIPE_VELOCITY_THRESHOLD * density);
                    if (Math.abs(diffX) > Math.abs(diffY)
                            && Math.abs(diffX) > adaptiveSwipe
                            && Math.abs(velocityX) > adaptiveVelocity) {
                        if (diffX > 0) onSwipeRight(); else onSwipeLeft();
                        return true;
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Erreur geste fling : " + e.getMessage(), e);
                }
                return false;
            }

            @Override public boolean onDown(MotionEvent e) { return true; }

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                if (isVerticalMode) return false;
                if (zoomLevel > 1.0f) { zoomLevel = 1.0f; resetScrollPosition(); }
                else                  { zoomLevel = 2.0f; }
                renderPage(currentPage);
                updateZoomLevel();
                return true;
            }
        });

        if (pdfContainer != null) {
            pdfContainer.setOnTouchListener((v, event) -> {
                gestureDetector.onTouchEvent(event);
                // Propager le click aussi pour le toggle UI
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    v.performClick();
                }
                return false;
            });
        }
    }

    private void resetScrollPosition() {
        if (scrollViewVertical   != null) scrollViewVertical.scrollTo(0, 0);
        if (scrollViewHorizontal != null) scrollViewHorizontal.scrollTo(0, 0);
    }

    // =========================================================================
    // Rendu
    // =========================================================================

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.d(TAG, "onConfigurationChanged() → re-rendu page " + currentPage);
        detectScreenCharacteristics();
        if (pdfRenderer == null) return;
        if (isVerticalMode) {
            if (containerAllPages != null) containerAllPages.removeAllViews();
            new RenderAllPagesTask().execute();
        } else {
            renderPage(currentPage);
        }
    }

    private void renderPage(int pageNumber) {
        if (pdfRenderer == null) { Log.e(TAG, "renderPage() → pdfRenderer NULL"); return; }
        if (pageNumber < 0 || pageNumber >= totalPages) { Log.e(TAG, "renderPage() → pageNumber invalide : " + pageNumber); return; }
        currentPage = pageNumber;
        new RenderPageTask().execute(pageNumber);
        updatePageInfo();
        updateNavigationButtons();
    }

    private void updatePageInfo() {
        if (textViewPageInfo != null)
            textViewPageInfo.setText(String.format("Page %d / %d", currentPage + 1, totalPages));
        if (editTextPageNumber != null && !editTextPageNumber.hasFocus())
            editTextPageNumber.setText(String.valueOf(currentPage + 1));
        if (totalPages > 0 && textViewTotalPages != null)
            textViewTotalPages.setText("/ " + totalPages);
        if (progressBarPage != null && totalPages > 0)
            progressBarPage.setProgress((int)(((float)(currentPage + 1) / totalPages) * 100));
        updateZoomLevel();
    }

    private void updateZoomLevel() {
        if (textViewZoomLevel != null)
            textViewZoomLevel.setText(String.format("%d%%", (int)(zoomLevel * 100)));
    }

    private void updateNavigationButtons() {
        if (btnPrevious != null && btnNext != null) {
            btnPrevious.setEnabled(currentPage > 0);
            btnNext.setEnabled(currentPage < totalPages - 1);
            btnPrevious.setAlpha(currentPage > 0             ? 1.0f : 0.4f);
            btnNext.setAlpha(currentPage < totalPages - 1 ? 1.0f : 0.4f);
        }
    }

    // =========================================================================
    // Navigation
    // =========================================================================

    private void previousPage() {
        if (currentPage > 0) {
            if (isVerticalMode) scrollToPageInVerticalMode(currentPage - 1);
            else { renderPage(currentPage - 1); resetScrollPosition(); }
        } else {
            Toast.makeText(this, "Première page", Toast.LENGTH_SHORT).show();
        }
    }

    private void nextPage() {
        if (currentPage < totalPages - 1) {
            if (isVerticalMode) scrollToPageInVerticalMode(currentPage + 1);
            else { renderPage(currentPage + 1); resetScrollPosition(); }
        } else {
            Toast.makeText(this, "Dernière page", Toast.LENGTH_SHORT).show();
        }
    }

    private void zoomIn() {
        if (zoomLevel < MAX_ZOOM) {
            zoomLevel += ZOOM_STEP;
            renderPage(currentPage);
            updateZoomLevel();
        } else {
            Toast.makeText(this, "Zoom maximum atteint", Toast.LENGTH_SHORT).show();
        }
    }

    private void zoomOut() {
        if (zoomLevel > MIN_ZOOM) {
            zoomLevel -= ZOOM_STEP;
            renderPage(currentPage);
            updateZoomLevel();
            if (zoomLevel == 1.0f) resetScrollPosition();
        } else {
            Toast.makeText(this, "Zoom minimum atteint", Toast.LENGTH_SHORT).show();
        }
    }

    private void onSwipeRight() { previousPage(); }
    private void onSwipeLeft()  { nextPage(); }

    // =========================================================================
    // Calcul dimensions
    // =========================================================================

    private int[] calculateOptimalDimensions(PdfRenderer.Page page) {
        int pageWidth  = page.getWidth();
        int pageHeight = page.getHeight();
        int availableWidth  = screenWidth;
        int availableHeight = screenHeight - getControlHeight();
        float widthRatio  = (float) availableWidth  / pageWidth;
        float heightRatio = (float) availableHeight / pageHeight;
        float scaleFactor = Math.min(widthRatio, heightRatio) * zoomLevel;
        int finalWidth  = (int)(pageWidth  * scaleFactor);
        int finalHeight = (int)(pageHeight * scaleFactor);
        int maxBitmapSize = getMaxBitmapSize();
        if (finalWidth > maxBitmapSize || finalHeight > maxBitmapSize) {
            float scale = Math.min((float)maxBitmapSize / finalWidth, (float)maxBitmapSize / finalHeight);
            finalWidth  = (int)(finalWidth  * scale);
            finalHeight = (int)(finalHeight * scale);
        }
        return new int[]{finalWidth, finalHeight};
    }

    private int[] calculateVerticalDimensions(PdfRenderer.Page page) {
        int pageWidth  = page.getWidth();
        int pageHeight = page.getHeight();
        int availableWidth = screenWidth - dp(24);
        float scale = (float) availableWidth / pageWidth;
        int finalWidth  = availableWidth;
        int finalHeight = (int)(pageHeight * scale);
        int maxBitmapSize = getMaxBitmapSize();
        if (finalWidth > maxBitmapSize || finalHeight > maxBitmapSize) {
            float s = Math.min((float)maxBitmapSize / finalWidth, (float)maxBitmapSize / finalHeight);
            finalWidth  = (int)(finalWidth  * s);
            finalHeight = (int)(finalHeight * s);
        }
        return new int[]{finalWidth, finalHeight};
    }

    private int getMaxBitmapSize() { return Math.min(screenWidth * 4, screenHeight * 4); }

    private int getControlHeight() {
        if (isTablet) return isLandscape ? 120 : 180;
        else          return isLandscape ? 100 : 160;
    }

    // =========================================================================
    // Cycle de vie
    // =========================================================================

    @Override
    protected void onDestroy() {
        super.onDestroy();
        autoHideHandler.removeCallbacksAndMessages(null);
        closeRenderer();
    }

    @Override
    protected void onPause() {
        super.onPause();
        autoHideHandler.removeCallbacks(autoHideRunnable);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isFullscreen) scheduleAutoHide();
    }

    private void closeRenderer() {
        try {
            if (pdfRenderer  != null) { pdfRenderer.close();  pdfRenderer  = null; }
            if (fileDescriptor != null) { fileDescriptor.close(); fileDescriptor = null; }
        } catch (Exception e) {
            Log.e(TAG, "closeRenderer() → erreur : " + e.getMessage(), e);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("currentPage",    currentPage);
        outState.putFloat("zoomLevel",    zoomLevel);
        outState.putBoolean("verticalMode", isVerticalMode);
        outState.putBoolean("fullscreen",   isFullscreen);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        currentPage    = savedInstanceState.getInt("currentPage", 0);
        zoomLevel      = savedInstanceState.getFloat("zoomLevel", 1.0f);
        isVerticalMode = savedInstanceState.getBoolean("verticalMode", false);
        isFullscreen   = savedInstanceState.getBoolean("fullscreen",   false);
        if (isFullscreen) enterFullscreen();
    }

    // =========================================================================
    // AsyncTask : chargement du PDF
    // =========================================================================

    private class LoadPdfTask extends AsyncTask<String, Void, Boolean> {

        private String errorReason = "";

        @Override
        protected void onPreExecute() {
            if (loadingLayout  != null) loadingLayout.setVisibility(View.VISIBLE);
            if (imageViewPdf   != null) imageViewPdf.setVisibility(View.GONE);
        }

        @Override
        protected Boolean doInBackground(String... params) {
            if (params == null || params.length == 0 || params[0] == null) {
                errorReason = "PDF_PATH est NULL"; return false;
            }
            String path = params[0];
            File file = new File(path);
            if (!file.exists())   { errorReason = "Fichier introuvable : " + path;  return false; }
            if (!file.canRead())  { errorReason = "Fichier non lisible : " + path;  return false; }
            if (file.length()==0) { errorReason = "Fichier vide (0 octet)";          return false; }
            try {
                fileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
                pdfRenderer    = new PdfRenderer(fileDescriptor);
                totalPages     = pdfRenderer.getPageCount();
                if (totalPages <= 0) { errorReason = "PDF sans page"; return false; }
                return true;
            } catch (IOException e)       { errorReason = "IOException : "       + e.getMessage(); return false; }
            catch (SecurityException e)   { errorReason = "SecurityException : " + e.getMessage(); return false; }
            catch (OutOfMemoryError e)    { errorReason = "OutOfMemoryError";                       return false; }
            catch (Exception e)           { errorReason = e.getClass().getSimpleName() + ": " + e.getMessage(); return false; }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (loadingLayout != null) loadingLayout.setVisibility(View.GONE);

            if (success && totalPages > 0) {
                if (textViewTotalPages != null) textViewTotalPages.setText("/ " + totalPages);
                if (editTextPageNumber != null) editTextPageNumber.setText("1");
                if (imageViewPdf       != null) imageViewPdf.setVisibility(View.VISIBLE);

                if (textViewSwipeHint != null) {
                    textViewSwipeHint.setVisibility(View.VISIBLE);
                    textViewSwipeHint.postDelayed(() -> {
                        if (textViewSwipeHint != null) textViewSwipeHint.setVisibility(View.GONE);
                    }, 3000);
                }
                renderPage(0);
            } else {
                Log.e(TAG, "Échec chargement PDF → " + errorReason);
                Toast.makeText(PdfBoxViewerActivity.this, "Erreur lors du chargement du PDF", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    // =========================================================================
    // AsyncTask : rendu d'une seule page (mode page)
    // =========================================================================

    private class RenderPageTask extends AsyncTask<Integer, Void, Bitmap> {

        private int pageNumRendered = -1;

        @Override
        protected void onPreExecute() {
            if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Bitmap doInBackground(Integer... params) {
            PdfRenderer.Page page = null;
            try {
                pageNumRendered = params[0];
                page = pdfRenderer.openPage(pageNumRendered);
                int[] dim    = calculateOptimalDimensions(page);
                Bitmap bitmap = Bitmap.createBitmap(dim[0], dim[1], Bitmap.Config.ARGB_8888);
                android.graphics.Canvas canvas = new android.graphics.Canvas(bitmap);
                canvas.drawColor(Color.WHITE);
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
                return bitmap;
            } catch (OutOfMemoryError e) { Log.e(TAG, "OOM page " + (pageNumRendered + 1), e); return null; }
            catch (Exception e)         { Log.e(TAG, "Exception page " + (pageNumRendered + 1), e); return null; }
            finally { if (page != null) page.close(); }
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (progressBar != null) progressBar.setVisibility(View.GONE);
            if (bitmap != null && imageViewPdf != null) {
                imageViewPdf.setImageBitmap(bitmap);
                imageViewPdf.setAlpha(0f);
                imageViewPdf.animate().alpha(1f).setDuration(200).start();
                resetScrollPosition();
                ViewGroup.LayoutParams p = imageViewPdf.getLayoutParams();
                p.width  = bitmap.getWidth();
                p.height = bitmap.getHeight();
                imageViewPdf.setLayoutParams(p);
            } else {
                Toast.makeText(PdfBoxViewerActivity.this, "Erreur lors du rendu de la page", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // =========================================================================
    // AsyncTask : rendu de toutes les pages (mode vertical)
    // =========================================================================

    private class RenderAllPagesTask extends AsyncTask<Void, Integer, Void> {

        @Override
        protected void onPreExecute() {
            if (progressBar      != null) progressBar.setVisibility(View.VISIBLE);
            if (containerAllPages != null) containerAllPages.removeAllViews();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            for (int i = 0; i < totalPages; i++) {
                if (isCancelled()) break;
                PdfRenderer.Page page = null;
                try {
                    page = pdfRenderer.openPage(i);
                    int[] dim    = calculateVerticalDimensions(page);
                    Bitmap bitmap = Bitmap.createBitmap(dim[0], dim[1], Bitmap.Config.ARGB_8888);
                    android.graphics.Canvas canvas = new android.graphics.Canvas(bitmap);
                    canvas.drawColor(Color.WHITE);
                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
                    final int pageIndex = i;
                    final Bitmap bmp    = bitmap;
                    runOnUiThread(() -> addPageToContainer(pageIndex, bmp));
                } catch (OutOfMemoryError e) {
                    final int idx = i;
                    runOnUiThread(() -> addErrorPlaceholder(idx));
                } catch (Exception e) {
                    final int idx = i;
                    runOnUiThread(() -> addErrorPlaceholder(idx));
                } finally {
                    if (page != null) page.close();
                }
                publishProgress(i + 1);
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            if (progressBarPage != null && totalPages > 0)
                progressBarPage.setProgress((int)((float) values[0] / totalPages * 100));
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (progressBar != null) progressBar.setVisibility(View.GONE);
        }

        private void addPageToContainer(int pageIndex, Bitmap bitmap) {
            if (containerAllPages == null) return;
            ImageView iv = new ImageView(PdfBoxViewerActivity.this);
            iv.setImageBitmap(bitmap);
            iv.setAdjustViewBounds(true);
            iv.setScaleType(ImageView.ScaleType.FIT_CENTER);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.bottomMargin = dp(8);
            iv.setLayoutParams(lp);
            iv.setTag(pageIndex);
            containerAllPages.addView(iv);
        }

        private void addErrorPlaceholder(int pageIndex) {
            if (containerAllPages == null) return;
            TextView tv = new TextView(PdfBoxViewerActivity.this);
            tv.setText("Page " + (pageIndex + 1) + " : erreur de rendu");
            tv.setTextColor(Color.GRAY);
            tv.setPadding(dp(16), dp(32), dp(16), dp(32));
            tv.setTag(pageIndex);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.bottomMargin = dp(8);
            tv.setLayoutParams(lp);
            containerAllPages.addView(tv);
        }
    }

    // =========================================================================
    // Options menu
    // =========================================================================

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (isFullscreen) { exitFullscreen(); return true; }
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // =========================================================================
    // Thème
    // =========================================================================

    private void setupTheme(ActionBar ab, TextView actionBarTitle) {
        UiModeManager uiModeManager = null;
        switch (Themes.getName(getApplicationContext())) {
            case "system":
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                    uiModeManager = (UiModeManager) getSystemService(Context.UI_MODE_SERVICE);
                if (uiModeManager != null) {
                    int mode = uiModeManager.getNightMode();
                    if (mode == UiModeManager.MODE_NIGHT_YES) {
                        ab.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.black)));
                        if (actionBarTitle != null) actionBarTitle.setTextColor(Color.parseColor("#B4EFEFEF"));
                    } else {
                        ab.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.white)));
                        ab.setHomeAsUpIndicator(R.drawable.vector_back);
                    }
                }
                break;
            case "notNight":
                ab.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.white)));
                ab.setHomeAsUpIndicator(R.drawable.vector_back);
                break;
            case "night":
                ab.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.black)));
                ab.setHomeAsUpIndicator(R.drawable.vector_white_sombre_back);
                if (actionBarTitle != null) actionBarTitle.setTextColor(Color.parseColor("#B4EFEFEF"));
                break;
        }
    }

    // =========================================================================
    // Utilitaires
    // =========================================================================

    private int dp(int dpValue) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, dpValue,
                getResources().getDisplayMetrics());
    }
}