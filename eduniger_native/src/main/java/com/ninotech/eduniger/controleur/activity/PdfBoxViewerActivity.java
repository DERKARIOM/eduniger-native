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
    /** ScrollView qui contient toutes les pages en mode vertical */
    private ScrollView scrollViewAllPages;
    /** LinearLayout parent qui empile les ImageView des pages */
    private LinearLayout containerAllPages;
    /** Bouton pour basculer entre mode page et mode vertical */
    private ImageButton btnToggleMode;
    /** Label du mode affiché à côté du bouton toggle */
    private TextView textViewMode;
    /** Champ de saisie du numéro de page */
    private EditText editTextPageNumber;
    /** Label "/ N" affiché à droite du champ */
    private TextView textViewTotalPages;

    // ── État du mode de lecture ──────────────────────────────────────────────
    /**
     * true  → mode vertical : toutes les pages empilées dans scrollViewAllPages
     * false → mode page     : une seule page à la fois dans imageViewPdf
     */
    private boolean isVerticalMode = false;

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
    private static final float MAX_ZOOM   = 5.0f;
    private static final float MIN_ZOOM   = 0.5f;
    private static final float ZOOM_STEP  = 0.25f;

    // =========================================================================
    // onCreate
    // =========================================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        Log.d(TAG, "onCreate() démarré");
        setContentView(R.layout.activity_pdfbox_viewer);

        detectScreenCharacteristics();

        ActionBar ab = getSupportActionBar();
        assert ab != null;
        ab.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        ab.setCustomView(R.layout.custom_action_bar);
        ab.setDisplayHomeAsUpEnabled(true);
        setupResponsiveActionBar(ab);

        TextView actionBarTitle = ab.getCustomView().findViewById(R.id.action_bar_title);
        setupTheme(ab, actionBarTitle);

        pdfPath  = getIntent().getStringExtra("PDF_PATH");
        pdfTitle = getIntent().getStringExtra("PDF_TITLE");

        Log.d(TAG, "Intent reçu :");
        Log.d(TAG, "  PDF_PATH  = " + (pdfPath  != null ? pdfPath  : "NULL ⚠️"));
        Log.d(TAG, "  PDF_TITLE = " + (pdfTitle != null ? pdfTitle : "NULL ⚠️"));

        if (actionBarTitle != null) actionBarTitle.setText(pdfTitle);

        initViews();
        setupResponsiveLayouts();
        setupGestureDetector();
        loadPdfDocument();

        Log.d(TAG, "onCreate() terminé");
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

        // Vues existantes
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

        // Nouvelles vues
        scrollViewAllPages  = findViewById(R.id.scrollViewAllPages);
        containerAllPages   = findViewById(R.id.containerAllPages);
        btnToggleMode       = findViewById(R.id.btnToggleMode);
        textViewMode        = findViewById(R.id.textViewMode);
        editTextPageNumber  = findViewById(R.id.editTextPageNumber);
        textViewTotalPages  = findViewById(R.id.textViewTotalPages);

        setupImageView();

        if (btnPrevious != null) btnPrevious.setOnClickListener(v -> previousPage());
        if (btnNext     != null) btnNext.setOnClickListener(v -> nextPage());
        if (btnZoomIn   != null) btnZoomIn.setOnClickListener(v -> zoomIn());
        if (btnZoomOut  != null) btnZoomOut.setOnClickListener(v -> zoomOut());

        // ── Toggle mode vertical / page ──────────────────────────────────────
        if (btnToggleMode != null) {
            btnToggleMode.setOnClickListener(v -> toggleReadingMode());
        }

        // ── Saisie directe du numéro de page ────────────────────────────────
        if (editTextPageNumber != null) {
            // Validation via touche "Entrée" du clavier
            editTextPageNumber.setOnEditorActionListener((v, actionId, event) -> {
                if (actionId == EditorInfo.IME_ACTION_GO
                        || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER
                        && event.getAction() == KeyEvent.ACTION_DOWN)) {
                    navigateToEnteredPage();
                    return true;
                }
                return false;
            });

            // Validation via touche "Retour" physique
            editTextPageNumber.setOnKeyListener((v, keyCode, event) -> {
                if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
                    navigateToEnteredPage();
                    return true;
                }
                return false;
            });
        }

        setupScrollViews();
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

    /**
     * Bascule entre le mode "une page à la fois" et le mode "défilement vertical".
     * La logique de rendu n'est pas modifiée : on réutilise renderPage() et
     * RenderPageTask pour le mode page ; on ajoute RenderAllPagesTask pour le mode vertical.
     */
    private void toggleReadingMode() {
        isVerticalMode = !isVerticalMode;
        Log.d(TAG, "toggleReadingMode() → isVerticalMode=" + isVerticalMode);

        if (isVerticalMode) {
            // ── Mode vertical ────────────────────────────────────────────────
            if (textViewMode    != null) textViewMode.setText("Mode vertical");
            if (scrollViewVertical  != null) scrollViewVertical.setVisibility(View.GONE);
            if (scrollViewAllPages  != null) scrollViewAllPages.setVisibility(View.VISIBLE);
            // Les boutons page précédente/suivante restent actifs pour scroller vers une page
            // Les boutons zoom sont désactivés en mode vertical (zoom global non géré)
            if (btnZoomIn  != null) { btnZoomIn.setEnabled(false);  btnZoomIn.setAlpha(0.4f); }
            if (btnZoomOut != null) { btnZoomOut.setEnabled(false); btnZoomOut.setAlpha(0.4f); }
            if (textViewSwipeHint != null) textViewSwipeHint.setVisibility(View.GONE);

            new RenderAllPagesTask().execute();

        } else {
            // ── Mode page ────────────────────────────────────────────────────
            if (textViewMode    != null) textViewMode.setText("Mode page");
            if (scrollViewVertical  != null) scrollViewVertical.setVisibility(View.VISIBLE);
            if (scrollViewAllPages  != null) scrollViewAllPages.setVisibility(View.GONE);
            if (btnZoomIn  != null) { btnZoomIn.setEnabled(true);  btnZoomIn.setAlpha(1.0f); }
            if (btnZoomOut != null) { btnZoomOut.setEnabled(true); btnZoomOut.setAlpha(1.0f); }
            if (imageViewPdf != null) imageViewPdf.setVisibility(View.VISIBLE);

            // Revenir à la page courante
            renderPage(currentPage);
        }
    }

    // =========================================================================
    // Saisie du numéro de page
    // =========================================================================

    /** Lit le champ editTextPageNumber et navigue vers la page saisie. */
    private void navigateToEnteredPage() {
        if (editTextPageNumber == null || totalPages == 0) return;

        String raw = editTextPageNumber.getText().toString().trim();
        if (raw.isEmpty()) return;

        try {
            int page = Integer.parseInt(raw); // numéro affiché (base 1)
            if (page < 1 || page > totalPages) {
                Toast.makeText(this,
                        "Page invalide. Entrez un numéro entre 1 et " + totalPages,
                        Toast.LENGTH_SHORT).show();
                // Remettre la valeur courante
                editTextPageNumber.setText(String.valueOf(currentPage + 1));
                return;
            }

            // Masquer le clavier
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) imm.hideSoftInputFromWindow(editTextPageNumber.getWindowToken(), 0);
            editTextPageNumber.clearFocus();

            if (isVerticalMode) {
                // En mode vertical : scroller jusqu'à la vue de la page concernée
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

    /**
     * Fait défiler le mode vertical jusqu'à la vue correspondant à la page demandée.
     * Les vues sont indexées dans containerAllPages dans l'ordre naturel des pages.
     */
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
            Log.d(TAG, "scrollToPageInVerticalMode() → page=" + (pageIndex + 1) + " scrollY=" + scrollY);
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
                // Le swipe n'est utile qu'en mode page
                if (isVerticalMode) return false;
                try {
                    float diffX = e2.getX() - e1.getX();
                    float diffY = e2.getY() - e1.getY();
                    float density        = getResources().getDisplayMetrics().density;
                    int adaptiveSwipe    = (int) (SWIPE_THRESHOLD          * density);
                    int adaptiveVelocity = (int) (SWIPE_VELOCITY_THRESHOLD * density);
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
            // Reconstruire toutes les pages avec les nouvelles dimensions
            if (containerAllPages != null) containerAllPages.removeAllViews();
            new RenderAllPagesTask().execute();
        } else {
            renderPage(currentPage);
        }
    }

    private void renderPage(int pageNumber) {
        if (pdfRenderer == null) {
            Log.e(TAG, "renderPage() → pdfRenderer NULL ⚠️"); return;
        }
        if (pageNumber < 0 || pageNumber >= totalPages) {
            Log.e(TAG, "renderPage() → pageNumber invalide : " + pageNumber + " ⚠️"); return;
        }
        Log.d(TAG, "renderPage() → page " + (pageNumber + 1) + "/" + totalPages + " | zoom=" + zoomLevel);
        currentPage = pageNumber;
        new RenderPageTask().execute(pageNumber);
        updatePageInfo();
        updateNavigationButtons();
    }

    private void updatePageInfo() {
        if (textViewPageInfo != null)
            textViewPageInfo.setText(String.format("Page %d / %d", currentPage + 1, totalPages));

        // Mettre à jour le champ de saisie seulement s'il n'a pas le focus
        // (éviter d'écraser ce que l'utilisateur est en train de taper)
        if (editTextPageNumber != null && !editTextPageNumber.hasFocus())
            editTextPageNumber.setText(String.valueOf(currentPage + 1));

        if (totalPages > 0 && textViewTotalPages != null)
            textViewTotalPages.setText("/ " + totalPages);

        if (progressBarPage != null && totalPages > 0)
            progressBarPage.setProgress((int) (((float)(currentPage + 1) / totalPages) * 100));

        updateZoomLevel();
    }

    private void updateZoomLevel() {
        if (textViewZoomLevel != null)
            textViewZoomLevel.setText(String.format("Zoom: %d%%", (int)(zoomLevel * 100)));
    }

    private void updateNavigationButtons() {
        if (btnPrevious != null && btnNext != null) {
            btnPrevious.setEnabled(currentPage > 0);
            btnNext.setEnabled(currentPage < totalPages - 1);
            btnPrevious.setAlpha(currentPage > 0              ? 1.0f : 0.5f);
            btnNext.setAlpha(currentPage < totalPages - 1 ? 1.0f : 0.5f);
        }
    }

    // =========================================================================
    // Navigation
    // =========================================================================

    private void previousPage() {
        if (currentPage > 0) {
            Log.d(TAG, "previousPage() → " + currentPage + " → " + (currentPage - 1));
            if (isVerticalMode) scrollToPageInVerticalMode(currentPage - 1);
            else { renderPage(currentPage - 1); resetScrollPosition(); }
        } else {
            Toast.makeText(this, "Première page", Toast.LENGTH_SHORT).show();
        }
    }

    private void nextPage() {
        if (currentPage < totalPages - 1) {
            Log.d(TAG, "nextPage() → " + currentPage + " → " + (currentPage + 1));
            if (isVerticalMode) scrollToPageInVerticalMode(currentPage + 1);
            else { renderPage(currentPage + 1); resetScrollPosition(); }
        } else {
            Toast.makeText(this, "Dernière page", Toast.LENGTH_SHORT).show();
        }
    }

    private void zoomIn() {
        if (zoomLevel < MAX_ZOOM) {
            zoomLevel += ZOOM_STEP;
            Log.d(TAG, "zoomIn() → zoomLevel=" + zoomLevel);
            renderPage(currentPage);
            updateZoomLevel();
        } else {
            Toast.makeText(this, "Zoom maximum atteint", Toast.LENGTH_SHORT).show();
        }
    }

    private void zoomOut() {
        if (zoomLevel > MIN_ZOOM) {
            zoomLevel -= ZOOM_STEP;
            Log.d(TAG, "zoomOut() → zoomLevel=" + zoomLevel);
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
            float scale = Math.min((float)maxBitmapSize / finalWidth,
                    (float)maxBitmapSize / finalHeight);
            finalWidth  = (int)(finalWidth  * scale);
            finalHeight = (int)(finalHeight * scale);
            Log.w(TAG, "Bitmap limité à " + finalWidth + "x" + finalHeight);
        }
        Log.d(TAG, "Dimensions : " + pageWidth + "x" + pageHeight + " → " + finalWidth + "x" + finalHeight);
        return new int[]{finalWidth, finalHeight};
    }

    /**
     * Dimensions pour le mode vertical : on utilise toujours la largeur d'écran
     * avec un zoom fixe à 1.0 (le zoom est géré page par page en mode page).
     */
    private int[] calculateVerticalDimensions(PdfRenderer.Page page) {
        int pageWidth  = page.getWidth();
        int pageHeight = page.getHeight();
        int availableWidth = screenWidth - dp(24); // marges gauche + droite
        float scale = (float) availableWidth / pageWidth;
        int finalWidth  = availableWidth;
        int finalHeight = (int)(pageHeight * scale);
        int maxBitmapSize = getMaxBitmapSize();
        if (finalWidth > maxBitmapSize || finalHeight > maxBitmapSize) {
            float s = Math.min((float)maxBitmapSize / finalWidth,
                    (float)maxBitmapSize / finalHeight);
            finalWidth  = (int)(finalWidth  * s);
            finalHeight = (int)(finalHeight * s);
        }
        return new int[]{finalWidth, finalHeight};
    }

    private int getMaxBitmapSize() {
        return Math.min(screenWidth * 4, screenHeight * 4);
    }

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
        Log.d(TAG, "onDestroy() → fermeture du PdfRenderer");
        closeRenderer();
    }

    private void closeRenderer() {
        try {
            if (pdfRenderer != null) { pdfRenderer.close(); pdfRenderer = null; }
            if (fileDescriptor != null) { fileDescriptor.close(); fileDescriptor = null; }
            Log.d(TAG, "closeRenderer() → ressources libérées ✓");
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
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        currentPage    = savedInstanceState.getInt("currentPage", 0);
        zoomLevel      = savedInstanceState.getFloat("zoomLevel", 1.0f);
        isVerticalMode = savedInstanceState.getBoolean("verticalMode", false);
    }

    // =========================================================================
    // AsyncTask : chargement du PDF
    // =========================================================================

    private class LoadPdfTask extends AsyncTask<String, Void, Boolean> {

        private String errorReason = "";

        @Override
        protected void onPreExecute() {
            Log.d(TAG, "LoadPdfTask → onPreExecute()");
            if (progressBar    != null) progressBar.setVisibility(View.VISIBLE);
            if (textViewLoading != null) textViewLoading.setVisibility(View.VISIBLE);
            if (imageViewPdf   != null) imageViewPdf.setVisibility(View.GONE);
        }

        @Override
        protected Boolean doInBackground(String... params) {
            Log.d(TAG, "LoadPdfTask → doInBackground()");
            if (params == null || params.length == 0 || params[0] == null) {
                errorReason = "PDF_PATH est NULL ⚠️"; Log.e(TAG, errorReason); return false;
            }
            String path = params[0];
            File file = new File(path);
            Log.d(TAG, "  Chemin : " + path + " | existe=" + file.exists()
                    + " | lisible=" + file.canRead() + " | taille=" + file.length());
            if (!file.exists())   { errorReason = "Fichier introuvable : " + path; Log.e(TAG, errorReason); return false; }
            if (!file.canRead())  { errorReason = "Fichier non lisible : " + path; Log.e(TAG, errorReason); return false; }
            if (file.length()==0) { errorReason = "Fichier vide (0 octet)";        Log.e(TAG, errorReason); return false; }
            try {
                fileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
                pdfRenderer    = new PdfRenderer(fileDescriptor);
                totalPages     = pdfRenderer.getPageCount();
                Log.d(TAG, "  PdfRenderer créé ✓ | totalPages=" + totalPages);
                if (totalPages <= 0) { errorReason = "PDF sans page ⚠️"; Log.e(TAG, errorReason); return false; }
                return true;
            } catch (IOException e)        { errorReason = "IOException : " + e.getMessage();                 Log.e(TAG, errorReason, e); return false; }
            catch (SecurityException e)  { errorReason = "SecurityException : " + e.getMessage();           Log.e(TAG, errorReason, e); return false; }
            catch (OutOfMemoryError e)   { errorReason = "OutOfMemoryError";                                Log.e(TAG, errorReason, e); return false; }
            catch (Exception e)          { errorReason = e.getClass().getSimpleName()+": "+e.getMessage();  Log.e(TAG, errorReason, e); return false; }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            Log.d(TAG, "LoadPdfTask → onPostExecute() | success=" + success);
            if (progressBar    != null) progressBar.setVisibility(View.GONE);
            if (textViewLoading != null) textViewLoading.setVisibility(View.GONE);

            if (success && totalPages > 0) {
                Log.d(TAG, "PDF chargé ✓ | " + totalPages + " pages");

                // Initialiser le label total pages
                if (textViewTotalPages  != null) textViewTotalPages.setText("/ " + totalPages);
                if (editTextPageNumber  != null) editTextPageNumber.setText("1");

                if (imageViewPdf != null) imageViewPdf.setVisibility(View.VISIBLE);

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
                Log.d(TAG, "RenderPageTask → rendu page " + (pageNumRendered + 1));
                page = pdfRenderer.openPage(pageNumRendered);
                int[] dim    = calculateOptimalDimensions(page);
                Bitmap bitmap = Bitmap.createBitmap(dim[0], dim[1], Bitmap.Config.ARGB_8888);
                android.graphics.Canvas canvas = new android.graphics.Canvas(bitmap);
                canvas.drawColor(Color.WHITE);
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
                Log.d(TAG, "  Rendu OK : " + bitmap.getWidth() + "x" + bitmap.getHeight());
                return bitmap;
            } catch (OutOfMemoryError e) { Log.e(TAG, "OOM page " + (pageNumRendered + 1), e); return null; }
            catch (Exception e)         { Log.e(TAG, "Exception page " + (pageNumRendered + 1) + " : " + e.getMessage(), e); return null; }
            finally { if (page != null) { page.close(); Log.d(TAG, "  Page fermée ✓"); } }
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
                Log.d(TAG, "RenderPageTask → image affichée ✓");
            } else {
                Log.e(TAG, "RenderPageTask → bitmap NULL ⚠️");
                Toast.makeText(PdfBoxViewerActivity.this, "Erreur lors du rendu de la page", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // =========================================================================
    // AsyncTask : rendu de toutes les pages (mode vertical)
    // =========================================================================

    /**
     * Rend chaque page dans un ImageView distinct et les ajoute au containerAllPages.
     * La logique de base (PdfRenderer.openPage / render / close) est identique
     * à RenderPageTask — seul le calcul des dimensions et l'insertion dans le
     * LinearLayout changent.
     */
    private class RenderAllPagesTask extends AsyncTask<Void, Integer, Void> {

        @Override
        protected void onPreExecute() {
            Log.d(TAG, "RenderAllPagesTask → onPreExecute()");
            if (progressBar      != null) progressBar.setVisibility(View.VISIBLE);
            if (containerAllPages != null) containerAllPages.removeAllViews();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            Log.d(TAG, "RenderAllPagesTask → doInBackground() | " + totalPages + " pages");
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
                    Log.d(TAG, "  Page " + (i + 1) + " rendue ✓");
                } catch (OutOfMemoryError e) {
                    Log.e(TAG, "  OOM page " + (i + 1) + " ⚠️", e);
                    final int idx = i;
                    runOnUiThread(() -> addErrorPlaceholder(idx));
                } catch (Exception e) {
                    Log.e(TAG, "  Erreur page " + (i + 1) + " : " + e.getMessage(), e);
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
            int done = values[0];
            Log.d(TAG, "RenderAllPagesTask → progression : " + done + "/" + totalPages);
            if (progressBarPage != null && totalPages > 0)
                progressBarPage.setProgress((int)((float) done / totalPages * 100));
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            Log.d(TAG, "RenderAllPagesTask → terminé ✓");
            if (progressBar != null) progressBar.setVisibility(View.GONE);
        }

        /** Insère le bitmap rendu dans un ImageView et l'ajoute au container. */
        private void addPageToContainer(int pageIndex, Bitmap bitmap) {
            if (containerAllPages == null) return;

            ImageView iv = new ImageView(PdfBoxViewerActivity.this);
            iv.setImageBitmap(bitmap);
            iv.setAdjustViewBounds(true);
            iv.setScaleType(ImageView.ScaleType.FIT_CENTER);

            // Espacement entre les pages
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.bottomMargin = dp(8);
            iv.setLayoutParams(lp);

            // Tag pour identifier la vue lors du scroll vers une page
            iv.setTag(pageIndex);

            containerAllPages.addView(iv);
        }

        /** Ajoute un placeholder texte si le rendu d'une page échoue. */
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