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
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
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
 * Version optimisée pour afficher le PDF en entier
 */
public class PdfBoxViewerActivity extends AppCompatActivity {

    private static final String TAG = "PdfBoxViewer";

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

    private PdfRenderer pdfRenderer;
    private ParcelFileDescriptor fileDescriptor;

    private String pdfPath;
    private String pdfTitle;
    private int currentPage = 0;
    private int totalPages = 0;
    private float zoomLevel = 1.0f;

    private int screenWidth;
    private int screenHeight;
    private int screenDensity;
    private boolean isTablet = false;
    private boolean isLandscape = false;

    private GestureDetector gestureDetector;
    private static final int SWIPE_THRESHOLD = 100;
    private static final int SWIPE_VELOCITY_THRESHOLD = 100;

    // Constantes pour le rendu
    private static final float MAX_ZOOM = 5.0f;
    private static final float MIN_ZOOM = 0.5f;
    private static final float ZOOM_STEP = 0.25f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        Log.d(TAG, "onCreate() démarré");
        setContentView(R.layout.activity_pdfbox_viewer);

        // Détection du type d'appareil et orientation
        detectScreenCharacteristics();

        ActionBar ab = getSupportActionBar();
        assert ab != null;
        ab.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        ab.setCustomView(R.layout.custom_action_bar);
        ab.setDisplayHomeAsUpEnabled(true);

        setupResponsiveActionBar(ab);

        TextView actionBarTitle = ab.getCustomView().findViewById(R.id.action_bar_title);
        setupTheme(ab, actionBarTitle);

        // Récupérer les données de l'Intent
        pdfPath  = getIntent().getStringExtra("PDF_PATH");
        pdfTitle = getIntent().getStringExtra("PDF_TITLE");

        // ── LOG INTENT ──────────────────────────────────────────────────────
        Log.d(TAG, "Intent reçu :");
        Log.d(TAG, "  PDF_PATH  = " + (pdfPath  != null ? pdfPath  : "NULL ⚠️"));
        Log.d(TAG, "  PDF_TITLE = " + (pdfTitle != null ? pdfTitle : "NULL ⚠️"));
        // ────────────────────────────────────────────────────────────────────

        if (actionBarTitle != null) {
            actionBarTitle.setText(pdfTitle);
        }

        initViews();
        setupResponsiveLayouts();
        setupGestureDetector();
        loadPdfDocument();

        Log.d(TAG, "onCreate() terminé");
    }

    // ==================== Chargement du PDF ====================

    private void loadPdfDocument() {
        Log.d(TAG, "loadPdfDocument() → lancement de LoadPdfTask");
        new LoadPdfTask().execute(pdfPath);
    }

    // ==================== Détection écran ====================

    private void detectScreenCharacteristics() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        screenWidth   = displayMetrics.widthPixels;
        screenHeight  = displayMetrics.heightPixels;
        screenDensity = displayMetrics.densityDpi;

        isTablet = (getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK)
                >= Configuration.SCREENLAYOUT_SIZE_LARGE;

        isLandscape = getResources().getConfiguration().orientation
                == Configuration.ORIENTATION_LANDSCAPE;

        Log.d(TAG, "Écran : " + screenWidth + "x" + screenHeight
                + " | densité=" + screenDensity
                + " | tablette=" + isTablet
                + " | paysage=" + isLandscape);
    }

    // ==================== ActionBar ====================

    private void setupResponsiveActionBar(ActionBar ab) {
        View customView = ab.getCustomView();
        if (customView != null) {
            ViewGroup.LayoutParams params = customView.getLayoutParams();
            if (isTablet) {
                params.height = dp(80);
            } else if (screenDensity >= DisplayMetrics.DENSITY_XXHIGH) {
                params.height = dp(70);
            } else {
                params.height = dp(56);
            }
            customView.setLayoutParams(params);
        }
    }

    // ==================== Layouts ====================

    private void setupResponsiveLayouts() {
        setupResponsiveControls();
        setupResponsiveTextSizes();
        if (isTablet) {
            setupTabletLayout();
        }
    }

    private void setupResponsiveControls() {
        int buttonSize = isTablet ? dp(64)
                : screenDensity >= DisplayMetrics.DENSITY_XXHIGH ? dp(56) : dp(50);

        if (btnPrevious != null) {
            setButtonSize(btnPrevious, buttonSize);
            setButtonSize(btnNext,     buttonSize);
            setButtonSize(btnZoomIn,   buttonSize);
            setButtonSize(btnZoomOut,  buttonSize);
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
            ViewGroup.LayoutParams params = controlLayout.getLayoutParams();
            params.height = dp(100);
            controlLayout.setLayoutParams(params);
        }
        if (textViewSwipeHint != null) {
            textViewSwipeHint.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        }
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

    // ==================== Vues ====================

    private void initViews() {
        Log.d(TAG, "initViews() démarré");

        imageViewPdf          = findViewById(R.id.imageViewPdf);
        textViewPageInfo      = findViewById(R.id.textViewPageInfo);
        textViewZoomLevel     = findViewById(R.id.textViewZoomLevel);
        textViewSwipeHint     = findViewById(R.id.textViewSwipeHint);
        textViewLoading       = findViewById(R.id.textViewLoading);
        btnPrevious           = findViewById(R.id.btnPrevious);
        btnNext               = findViewById(R.id.btnNext);
        btnZoomIn             = findViewById(R.id.btnZoomIn);
        btnZoomOut            = findViewById(R.id.btnZoomOut);
        progressBar           = findViewById(R.id.progressBar);
        progressBarPage       = findViewById(R.id.progressBarPage);
        scrollViewVertical    = findViewById(R.id.scrollViewVertical);
        scrollViewHorizontal  = findViewById(R.id.scrollViewHorizontal);
        controlLayout         = findViewById(R.id.controlLayout);
        pdfContainer          = findViewById(R.id.pdfContainer);

        // ── LOG VUES NULLES ─────────────────────────────────────────────────
        if (imageViewPdf         == null) Log.w(TAG, "  imageViewPdf est NULL ⚠️ (vérifier l'id R.id.imageViewPdf)");
        if (btnPrevious          == null) Log.w(TAG, "  btnPrevious est NULL ⚠️");
        if (btnNext              == null) Log.w(TAG, "  btnNext est NULL ⚠️");
        if (btnZoomIn            == null) Log.w(TAG, "  btnZoomIn est NULL ⚠️");
        if (btnZoomOut           == null) Log.w(TAG, "  btnZoomOut est NULL ⚠️");
        if (scrollViewVertical   == null) Log.w(TAG, "  scrollViewVertical est NULL ⚠️");
        if (scrollViewHorizontal == null) Log.w(TAG, "  scrollViewHorizontal est NULL ⚠️");
        if (pdfContainer         == null) Log.w(TAG, "  pdfContainer est NULL ⚠️");
        // ────────────────────────────────────────────────────────────────────

        setupImageView();

        if (btnPrevious != null) btnPrevious.setOnClickListener(v -> previousPage());
        if (btnNext     != null) btnNext.setOnClickListener(v -> nextPage());
        if (btnZoomIn   != null) btnZoomIn.setOnClickListener(v -> zoomIn());
        if (btnZoomOut  != null) btnZoomOut.setOnClickListener(v -> zoomOut());

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
    }

    // ==================== Gestes ====================

    private void setupGestureDetector() {
        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2,
                                   float velocityX, float velocityY) {
                try {
                    float diffX = e2.getX() - e1.getX();
                    float diffY = e2.getY() - e1.getY();

                    float density = getResources().getDisplayMetrics().density;
                    int adaptiveSwipe    = (int) (SWIPE_THRESHOLD * density);
                    int adaptiveVelocity = (int) (SWIPE_VELOCITY_THRESHOLD * density);

                    if (Math.abs(diffX) > Math.abs(diffY)
                            && Math.abs(diffX) > adaptiveSwipe
                            && Math.abs(velocityX) > adaptiveVelocity) {
                        if (diffX > 0) onSwipeRight();
                        else           onSwipeLeft();
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

    // ==================== Rendu ====================

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.d(TAG, "onConfigurationChanged() → re-rendu de la page " + currentPage);
        detectScreenCharacteristics();
        if (pdfRenderer != null) renderPage(currentPage);
    }

    private void renderPage(int pageNumber) {
        if (pdfRenderer == null) {
            Log.e(TAG, "renderPage() → pdfRenderer est NULL, rendu impossible ⚠️");
            return;
        }
        if (pageNumber < 0 || pageNumber >= totalPages) {
            Log.e(TAG, "renderPage() → pageNumber invalide : " + pageNumber
                    + " (totalPages=" + totalPages + ") ⚠️");
            return;
        }
        Log.d(TAG, "renderPage() → page " + (pageNumber + 1) + "/" + totalPages
                + " | zoom=" + zoomLevel);
        currentPage = pageNumber;
        new RenderPageTask().execute(pageNumber);
        updatePageInfo();
        updateNavigationButtons();
    }

    private void updatePageInfo() {
        if (textViewPageInfo != null) {
            textViewPageInfo.setText(
                    String.format("Page %d / %d", currentPage + 1, totalPages));
        }
        if (progressBarPage != null && totalPages > 0) {
            int progress = (int) (((float) (currentPage + 1) / totalPages) * 100);
            progressBarPage.setProgress(progress);
        }
        updateZoomLevel();
    }

    private void updateZoomLevel() {
        if (textViewZoomLevel != null) {
            textViewZoomLevel.setText(
                    String.format("Zoom: %d%%", (int) (zoomLevel * 100)));
        }
    }

    private void updateNavigationButtons() {
        if (btnPrevious != null && btnNext != null) {
            btnPrevious.setEnabled(currentPage > 0);
            btnNext.setEnabled(currentPage < totalPages - 1);
            btnPrevious.setAlpha(currentPage > 0 ? 1.0f : 0.5f);
            btnNext.setAlpha(currentPage < totalPages - 1 ? 1.0f : 0.5f);
        }
    }

    // ==================== Navigation ====================

    private void previousPage() {
        if (currentPage > 0) {
            Log.d(TAG, "previousPage() → " + currentPage + " → " + (currentPage - 1));
            renderPage(currentPage - 1);
            resetScrollPosition();
        } else {
            Toast.makeText(this, "Première page", Toast.LENGTH_SHORT).show();
        }
    }

    private void nextPage() {
        if (currentPage < totalPages - 1) {
            Log.d(TAG, "nextPage() → " + currentPage + " → " + (currentPage + 1));
            renderPage(currentPage + 1);
            resetScrollPosition();
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

    // ==================== Calcul dimensions ====================

    private int[] calculateOptimalDimensions(PdfRenderer.Page page) {
        int pageWidth  = page.getWidth();
        int pageHeight = page.getHeight();

        int availableWidth  = screenWidth;
        int availableHeight = screenHeight - getControlHeight();

        float widthRatio  = (float) availableWidth  / pageWidth;
        float heightRatio = (float) availableHeight / pageHeight;
        float scaleFactor = Math.min(widthRatio, heightRatio) * zoomLevel;

        int finalWidth  = (int) (pageWidth  * scaleFactor);
        int finalHeight = (int) (pageHeight * scaleFactor);

        int maxBitmapSize = getMaxBitmapSize();
        if (finalWidth > maxBitmapSize || finalHeight > maxBitmapSize) {
            float scale = Math.min(
                    (float) maxBitmapSize / finalWidth,
                    (float) maxBitmapSize / finalHeight);
            finalWidth  = (int) (finalWidth  * scale);
            finalHeight = (int) (finalHeight * scale);
            Log.w(TAG, "Bitmap limité à " + finalWidth + "x" + finalHeight
                    + " pour éviter OutOfMemory");
        }

        Log.d(TAG, "Dimensions calculées : pageSource=" + pageWidth + "x" + pageHeight
                + " → rendu=" + finalWidth + "x" + finalHeight
                + " | scaleFactor=" + scaleFactor);

        return new int[]{finalWidth, finalHeight};
    }

    private int getMaxBitmapSize() {
        return Math.min(screenWidth * 4, screenHeight * 4);
    }

    private int getControlHeight() {
        if (isTablet) return isLandscape ? 120 : 180;
        else          return isLandscape ? 100 : 160;
    }

    // ==================== Cycle de vie ====================

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
        outState.putInt("currentPage", currentPage);
        outState.putFloat("zoomLevel", zoomLevel);
        Log.d(TAG, "onSaveInstanceState() → page=" + currentPage + " zoom=" + zoomLevel);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        currentPage = savedInstanceState.getInt("currentPage", 0);
        zoomLevel   = savedInstanceState.getFloat("zoomLevel", 1.0f);
        Log.d(TAG, "onRestoreInstanceState() → page=" + currentPage + " zoom=" + zoomLevel);
    }

    // ==================== AsyncTask : chargement ====================

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
            Log.d(TAG, "LoadPdfTask → doInBackground() démarré");

            // ── Vérification du chemin ──────────────────────────────────────
            if (params == null || params.length == 0 || params[0] == null) {
                errorReason = "PDF_PATH est NULL : aucun chemin transmis via l'Intent ⚠️";
                Log.e(TAG, errorReason);
                return false;
            }

            String path = params[0];
            Log.d(TAG, "  Chemin reçu : " + path);

            // ── Vérification du fichier ─────────────────────────────────────
            File file = new File(path);
            Log.d(TAG, "  Fichier absolu : " + file.getAbsolutePath());
            Log.d(TAG, "  Fichier existe : " + file.exists());
            Log.d(TAG, "  Fichier lisible : " + file.canRead());
            Log.d(TAG, "  Taille fichier : " + file.length() + " octets");

            if (!file.exists()) {
                errorReason = "Fichier introuvable sur le disque : " + path + " ⚠️";
                Log.e(TAG, errorReason);
                return false;
            }

            if (!file.canRead()) {
                errorReason = "Fichier non lisible (permissions ?) : " + path + " ⚠️";
                Log.e(TAG, errorReason);
                return false;
            }

            if (file.length() == 0) {
                errorReason = "Fichier vide (0 octet) : téléchargement incomplet ? ⚠️";
                Log.e(TAG, errorReason);
                return false;
            }

            // ── Ouverture PdfRenderer ───────────────────────────────────────
            try {
                Log.d(TAG, "  Ouverture du ParcelFileDescriptor...");
                fileDescriptor = ParcelFileDescriptor.open(
                        file, ParcelFileDescriptor.MODE_READ_ONLY);
                Log.d(TAG, "  ParcelFileDescriptor ouvert ✓");

                Log.d(TAG, "  Création du PdfRenderer...");
                pdfRenderer = new PdfRenderer(fileDescriptor);
                totalPages  = pdfRenderer.getPageCount();
                Log.d(TAG, "  PdfRenderer créé ✓ | totalPages=" + totalPages);

                if (totalPages <= 0) {
                    errorReason = "Le PDF ne contient aucune page ⚠️";
                    Log.e(TAG, errorReason);
                    return false;
                }

                return true;

            } catch (IOException e) {
                errorReason = "IOException : " + e.getMessage()
                        + " — fichier corrompu ou format non supporté ⚠️";
                Log.e(TAG, errorReason, e);
                return false;
            } catch (SecurityException e) {
                errorReason = "SecurityException : permission refusée pour " + path + " ⚠️";
                Log.e(TAG, errorReason, e);
                return false;
            } catch (OutOfMemoryError e) {
                errorReason = "OutOfMemoryError : PDF trop lourd pour la mémoire disponible ⚠️";
                Log.e(TAG, errorReason, e);
                return false;
            } catch (Exception e) {
                errorReason = "Exception inattendue : " + e.getClass().getSimpleName()
                        + " — " + e.getMessage() + " ⚠️";
                Log.e(TAG, errorReason, e);
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            Log.d(TAG, "LoadPdfTask → onPostExecute() | success=" + success);

            if (progressBar    != null) progressBar.setVisibility(View.GONE);
            if (textViewLoading != null) textViewLoading.setVisibility(View.GONE);

            if (success && totalPages > 0) {
                Log.d(TAG, "PDF chargé avec succès ✓ | " + totalPages + " pages");

                if (imageViewPdf != null) imageViewPdf.setVisibility(View.VISIBLE);

                if (textViewSwipeHint != null) {
                    textViewSwipeHint.setVisibility(View.VISIBLE);
                    textViewSwipeHint.postDelayed(() -> {
                        if (textViewSwipeHint != null)
                            textViewSwipeHint.setVisibility(View.GONE);
                    }, 3000);
                }

                renderPage(0);

            } else {
                Log.e(TAG, "Échec du chargement PDF → raison : " + errorReason);
                Toast.makeText(PdfBoxViewerActivity.this,
                        "Erreur lors du chargement du PDF", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    // ==================== AsyncTask : rendu page ====================

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

                int[] dimensions = calculateOptimalDimensions(page);
                int bmpWidth  = dimensions[0];
                int bmpHeight = dimensions[1];

                Log.d(TAG, "  Création bitmap " + bmpWidth + "x" + bmpHeight);
                Bitmap bitmap = Bitmap.createBitmap(
                        bmpWidth, bmpHeight, Bitmap.Config.ARGB_8888);

                android.graphics.Canvas canvas = new android.graphics.Canvas(bitmap);
                canvas.drawColor(Color.WHITE);

                page.render(bitmap, null, null,
                        PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);

                Log.d(TAG, "  Rendu terminé ✓ | bitmap=" + bitmap.getWidth()
                        + "x" + bitmap.getHeight());
                return bitmap;

            } catch (OutOfMemoryError e) {
                Log.e(TAG, "RenderPageTask → OutOfMemoryError page "
                        + (pageNumRendered + 1) + " ⚠️", e);
                return null;
            } catch (Exception e) {
                Log.e(TAG, "RenderPageTask → Exception page "
                        + (pageNumRendered + 1) + " : " + e.getMessage() + " ⚠️", e);
                return null;
            } finally {
                if (page != null) {
                    page.close();
                    Log.d(TAG, "  PdfRenderer.Page fermée ✓");
                }
            }
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
                Log.e(TAG, "RenderPageTask → bitmap NULL, page non affichée ⚠️");
                Toast.makeText(PdfBoxViewerActivity.this,
                        "Erreur lors du rendu de la page", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // ==================== Options menu ====================

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // ==================== Thème ====================

    private void setupTheme(ActionBar ab, TextView actionBarTitle) {
        UiModeManager uiModeManager = null;
        switch (Themes.getName(getApplicationContext())) {
            case "system":
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    uiModeManager = (UiModeManager) getSystemService(Context.UI_MODE_SERVICE);
                }
                if (uiModeManager != null) {
                    int mode = uiModeManager.getNightMode();
                    if (mode == UiModeManager.MODE_NIGHT_YES) {
                        ab.setBackgroundDrawable(new ColorDrawable(
                                getResources().getColor(R.color.black)));
                        if (actionBarTitle != null)
                            actionBarTitle.setTextColor(Color.parseColor("#B4EFEFEF"));
                    } else {
                        ab.setBackgroundDrawable(new ColorDrawable(
                                getResources().getColor(R.color.white)));
                        ab.setHomeAsUpIndicator(R.drawable.vector_back);
                    }
                }
                break;
            case "notNight":
                ab.setBackgroundDrawable(new ColorDrawable(
                        getResources().getColor(R.color.white)));
                ab.setHomeAsUpIndicator(R.drawable.vector_back);
                break;
            case "night":
                ab.setBackgroundDrawable(new ColorDrawable(
                        getResources().getColor(R.color.black)));
                ab.setHomeAsUpIndicator(R.drawable.vector_white_sombre_back);
                if (actionBarTitle != null)
                    actionBarTitle.setTextColor(Color.parseColor("#B4EFEFEF"));
                break;
        }
    }

    // ==================== Utilitaires ====================

    /** Convertit des dp en pixels */
    private int dp(int dpValue) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, dpValue,
                getResources().getDisplayMetrics());
    }
}