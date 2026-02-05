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
    private boolean isA5Format = false;
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
        setContentView(R.layout.activity_pdfbox_viewer);

        // Détection du type d'appareil et orientation
        detectScreenCharacteristics();

        ActionBar ab = getSupportActionBar();
        assert ab != null;
        ab.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        ab.setCustomView(R.layout.custom_action_bar);
        ab.setDisplayHomeAsUpEnabled(true);

        // Configuration responsive de l'ActionBar
        setupResponsiveActionBar(ab);

        TextView actionBarTitle = ab.getCustomView().findViewById(R.id.action_bar_title);
        setupTheme(ab, actionBarTitle);

        // Récupérer les données
        pdfPath = getIntent().getStringExtra("PDF_PATH");
        pdfTitle = getIntent().getStringExtra("PDF_TITLE");
        if (actionBarTitle != null) {
            actionBarTitle.setText(pdfTitle);
        }

        // Initialiser les vues
        initViews();

        // Configuration responsive des layouts
        setupResponsiveLayouts();

        // Initialiser le détecteur de gestes
        setupGestureDetector();

        // Charger le PDF
        loadPdfDocument();
    }

    /**
     * Méthode pour charger le document PDF
     */
    private void loadPdfDocument() {
        new LoadPdfTask().execute(pdfPath);
    }

    private void detectScreenCharacteristics() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        screenWidth = displayMetrics.widthPixels;
        screenHeight = displayMetrics.heightPixels;
        screenDensity = displayMetrics.densityDpi;

        // Détection tablette
        isTablet = (getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK)
                >= Configuration.SCREENLAYOUT_SIZE_LARGE;

        // Détection orientation
        isLandscape = getResources().getConfiguration().orientation
                == Configuration.ORIENTATION_LANDSCAPE;
    }

    private void setupResponsiveActionBar(ActionBar ab) {
        // Ajuster la hauteur de l'ActionBar selon la densité d'écran
        View customView = ab.getCustomView();
        if (customView != null) {
            ViewGroup.LayoutParams params = customView.getLayoutParams();

            if (isTablet) {
                params.height = (int) TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, 80, getResources().getDisplayMetrics());
            } else if (screenDensity >= DisplayMetrics.DENSITY_XXHIGH) {
                params.height = (int) TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, 70, getResources().getDisplayMetrics());
            } else {
                params.height = (int) TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, 56, getResources().getDisplayMetrics());
            }

            customView.setLayoutParams(params);
        }
    }

    private void setupResponsiveLayouts() {
        // Configuration responsive des contrôles
        setupResponsiveControls();

        // Configuration responsive des textes
        setupResponsiveTextSizes();

        // Configuration spécifique pour tablettes
        if (isTablet) {
            setupTabletLayout();
        }
    }

    private void setupResponsiveControls() {
        // Taille des boutons selon la résolution
        int buttonSize;
        if (isTablet) {
            buttonSize = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, 64, getResources().getDisplayMetrics());
        } else if (screenDensity >= DisplayMetrics.DENSITY_XXHIGH) {
            buttonSize = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, 56, getResources().getDisplayMetrics());
        } else {
            buttonSize = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, 50, getResources().getDisplayMetrics());
        }

        // Appliquer la taille aux boutons
        if (btnPrevious != null) {
            ViewGroup.LayoutParams previousParams = btnPrevious.getLayoutParams();
            ViewGroup.LayoutParams nextParams = btnNext.getLayoutParams();
            ViewGroup.LayoutParams zoomInParams = btnZoomIn.getLayoutParams();
            ViewGroup.LayoutParams zoomOutParams = btnZoomOut.getLayoutParams();

            previousParams.width = buttonSize;
            previousParams.height = buttonSize;
            nextParams.width = buttonSize;
            nextParams.height = buttonSize;
            zoomInParams.width = buttonSize;
            zoomInParams.height = buttonSize;
            zoomOutParams.width = buttonSize;
            zoomOutParams.height = buttonSize;

            btnPrevious.setLayoutParams(previousParams);
            btnNext.setLayoutParams(nextParams);
            btnZoomIn.setLayoutParams(zoomInParams);
            btnZoomOut.setLayoutParams(zoomOutParams);
        }
    }

    private void setupTabletLayout() {
        // Pour les tablettes, ajuster les layouts pour mieux utiliser l'espace
        if (isLandscape && controlLayout != null) {
            // En mode paysage tablette, on peut réduire la hauteur des contrôles
            ViewGroup.LayoutParams params = controlLayout.getLayoutParams();
            params.height = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, 100, getResources().getDisplayMetrics());
            controlLayout.setLayoutParams(params);
        }

        // Agrandir le texte du hint de swipe pour tablettes
        if (textViewSwipeHint != null) {
            textViewSwipeHint.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        }
    }

    private void setupResponsiveTextSizes() {
        float pageInfoTextSize, zoomTextSize, hintTextSize, loadingTextSize;

        if (isTablet) {
            pageInfoTextSize = 18f;
            zoomTextSize = 14f;
            hintTextSize = 20f;
            loadingTextSize = 16f;
        } else if (screenDensity >= DisplayMetrics.DENSITY_XXHIGH) {
            pageInfoTextSize = 16f;
            zoomTextSize = 12f;
            hintTextSize = 18f;
            loadingTextSize = 14f;
        } else {
            pageInfoTextSize = 14f;
            zoomTextSize = 10f;
            hintTextSize = 16f;
            loadingTextSize = 12f;
        }

        if (textViewPageInfo != null) {
            textViewPageInfo.setTextSize(TypedValue.COMPLEX_UNIT_SP, pageInfoTextSize);
        }
        if (textViewZoomLevel != null) {
            textViewZoomLevel.setTextSize(TypedValue.COMPLEX_UNIT_SP, zoomTextSize);
        }
        if (textViewSwipeHint != null) {
            textViewSwipeHint.setTextSize(TypedValue.COMPLEX_UNIT_SP, hintTextSize);
        }
        if (textViewLoading != null) {
            textViewLoading.setTextSize(TypedValue.COMPLEX_UNIT_SP, loadingTextSize);
        }
    }

    private void initViews() {
        imageViewPdf = findViewById(R.id.imageViewPdf);
        textViewPageInfo = findViewById(R.id.textViewPageInfo);
        textViewZoomLevel = findViewById(R.id.textViewZoomLevel);
        textViewSwipeHint = findViewById(R.id.textViewSwipeHint);
        textViewLoading = findViewById(R.id.textViewLoading);
        btnPrevious = findViewById(R.id.btnPrevious);
        btnNext = findViewById(R.id.btnNext);
        btnZoomIn = findViewById(R.id.btnZoomIn);
        btnZoomOut = findViewById(R.id.btnZoomOut);
        progressBar = findViewById(R.id.progressBar);
        progressBarPage = findViewById(R.id.progressBarPage);
        scrollViewVertical = findViewById(R.id.scrollViewVertical);
        scrollViewHorizontal = findViewById(R.id.scrollViewHorizontal);
        controlLayout = findViewById(R.id.controlLayout);
        pdfContainer = findViewById(R.id.pdfContainer);

        // Configuration de l'ImageView pour un meilleur affichage
        setupImageView();

        if (btnPrevious != null) {
            btnPrevious.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    previousPage();
                }
            });
        }

        if (btnNext != null) {
            btnNext.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    nextPage();
                }
            });
        }

        if (btnZoomIn != null) {
            btnZoomIn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    zoomIn();
                }
            });
        }

        if (btnZoomOut != null) {
            btnZoomOut.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    zoomOut();
                }
            });
        }

        // Configuration des ScrollViews
        setupScrollViews();
    }

    private void setupImageView() {
        if (imageViewPdf != null) {
            // Configuration pour un affichage optimal
            imageViewPdf.setScaleType(ImageView.ScaleType.FIT_CENTER);
            imageViewPdf.setAdjustViewBounds(true);
        }
    }

    private void setupScrollViews() {
        // Configuration des ScrollViews pour permettre le défilement
        if (scrollViewVertical != null) {
            scrollViewVertical.setVerticalScrollBarEnabled(true);
            scrollViewVertical.setScrollbarFadingEnabled(true);
        }
        if (scrollViewHorizontal != null) {
            scrollViewHorizontal.setHorizontalScrollBarEnabled(true);
            scrollViewHorizontal.setScrollbarFadingEnabled(true);
        }
    }

    private void setupGestureDetector() {
        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                try {
                    float diffX = e2.getX() - e1.getX();
                    float diffY = e2.getY() - e1.getY();

                    // Ajuster les seuils selon la densité d'écran
                    float density = getResources().getDisplayMetrics().density;
                    int adaptiveSwipeThreshold = (int) (SWIPE_THRESHOLD * density);
                    int adaptiveVelocityThreshold = (int) (SWIPE_VELOCITY_THRESHOLD * density);

                    // Vérifier que le mouvement horizontal est dominant
                    if (Math.abs(diffX) > Math.abs(diffY)) {
                        if (Math.abs(diffX) > adaptiveSwipeThreshold &&
                                Math.abs(velocityX) > adaptiveVelocityThreshold) {
                            if (diffX > 0) {
                                // Swipe vers la droite = page précédente
                                onSwipeRight();
                            } else {
                                // Swipe vers la gauche = page suivante
                                onSwipeLeft();
                            }
                            return true;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return false;
            }

            @Override
            public boolean onDown(MotionEvent e) {
                return true;
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                // Zoom/dézoom au double tap
                if (zoomLevel > 1.0f) {
                    zoomLevel = 1.0f;
                    resetScrollPosition();
                } else {
                    zoomLevel = 2.0f;
                }
                renderPage(currentPage);
                updateZoomLevel();
                return true;
            }
        });

        // Ajouter le listener de touch sur le container PDF
        if (pdfContainer != null) {
            pdfContainer.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    gestureDetector.onTouchEvent(event);
                    // Permettre le défilement normal
                    return false;
                }
            });
        }
    }

    private void resetScrollPosition() {
        // Réinitialiser la position de défilement
        if (scrollViewVertical != null) {
            scrollViewVertical.scrollTo(0, 0);
        }
        if (scrollViewHorizontal != null) {
            scrollViewHorizontal.scrollTo(0, 0);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // Recréer les dimensions d'écran
        detectScreenCharacteristics();

        // Re-rendre la page courante avec la nouvelle configuration
        if (pdfRenderer != null) {
            renderPage(currentPage);
        }
    }

    private void renderPage(int pageNumber) {
        if (pdfRenderer == null || pageNumber < 0 || pageNumber >= totalPages) {
            return;
        }

        currentPage = pageNumber;
        new RenderPageTask().execute(pageNumber);
        updatePageInfo();
        updateNavigationButtons();
    }

    private void updatePageInfo() {
        if (textViewPageInfo != null) {
            textViewPageInfo.setText(String.format("Page %d / %d", currentPage + 1, totalPages));
        }

        // Mettre à jour la barre de progression
        if (progressBarPage != null && totalPages > 0) {
            int progress = (int) (((float) (currentPage + 1) / totalPages) * 100);
            progressBarPage.setProgress(progress);
        }

        // Mettre à jour le niveau de zoom
        updateZoomLevel();
    }

    private void updateZoomLevel() {
        if (textViewZoomLevel != null) {
            int zoomPercent = (int) (zoomLevel * 100);
            textViewZoomLevel.setText(String.format("Zoom: %d%%", zoomPercent));
        }
    }

    private void updateNavigationButtons() {
        if (btnPrevious != null && btnNext != null) {
            btnPrevious.setEnabled(currentPage > 0);
            btnNext.setEnabled(currentPage < totalPages - 1);

            // Feedback visuel pour les boutons désactivés
            btnPrevious.setAlpha(currentPage > 0 ? 1.0f : 0.5f);
            btnNext.setAlpha(currentPage < totalPages - 1 ? 1.0f : 0.5f);
        }
    }

    private void previousPage() {
        if (currentPage > 0) {
            renderPage(currentPage - 1);
            resetScrollPosition();
        } else {
            Toast.makeText(this, "Première page", Toast.LENGTH_SHORT).show();
        }
    }

    private void nextPage() {
        if (currentPage < totalPages - 1) {
            renderPage(currentPage + 1);
            resetScrollPosition();
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
            if (zoomLevel == 1.0f) {
                resetScrollPosition();
            }
        } else {
            Toast.makeText(this, "Zoom minimum atteint", Toast.LENGTH_SHORT).show();
        }
    }

    private void onSwipeRight() {
        previousPage();
    }

    private void onSwipeLeft() {
        nextPage();
    }

    /**
     * Calculer les dimensions optimales pour afficher le PDF en entier
     */
    private int[] calculateOptimalDimensions(PdfRenderer.Page page) {
        int pageWidth = page.getWidth();
        int pageHeight = page.getHeight();

        // Calculer la zone d'affichage disponible (écran moins les contrôles)
        int availableWidth = screenWidth;
        int availableHeight = screenHeight - getControlHeight();

        // Calculer les ratios pour s'adapter à l'écran
        float widthRatio = (float) availableWidth / pageWidth;
        float heightRatio = (float) availableHeight / pageHeight;

        // Utiliser le ratio le plus petit pour afficher la page en entier
        float scaleFactor = Math.min(widthRatio, heightRatio);

        // Appliquer le niveau de zoom
        scaleFactor *= zoomLevel;

        // Calculer les dimensions finales
        int finalWidth = (int) (pageWidth * scaleFactor);
        int finalHeight = (int) (pageHeight * scaleFactor);

        // Pour les très grands zooms, limiter la taille pour éviter les OutOfMemory
        int maxBitmapSize = getMaxBitmapSize();
        if (finalWidth > maxBitmapSize || finalHeight > maxBitmapSize) {
            float scale = Math.min((float) maxBitmapSize / finalWidth, (float) maxBitmapSize / finalHeight);
            finalWidth = (int) (finalWidth * scale);
            finalHeight = (int) (finalHeight * scale);
        }

        return new int[]{finalWidth, finalHeight};
    }

    /**
     * Obtenir la taille maximale du bitmap pour éviter les OutOfMemory
     */
    private int getMaxBitmapSize() {
        // Limiter à 4K pour éviter les problèmes de mémoire
        return Math.min(screenWidth * 4, screenHeight * 4);
    }

    private int getControlHeight() {
        // Hauteur estimée des contrôles en bas de l'écran
        if (isTablet) {
            return isLandscape ? 120 : 180;
        } else {
            return isLandscape ? 100 : 160;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        closeRenderer();
    }

    private void closeRenderer() {
        try {
            if (pdfRenderer != null) {
                pdfRenderer.close();
                pdfRenderer = null;
            }
            if (fileDescriptor != null) {
                fileDescriptor.close();
                fileDescriptor = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("currentPage", currentPage);
        outState.putFloat("zoomLevel", zoomLevel);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        currentPage = savedInstanceState.getInt("currentPage", 0);
        zoomLevel = savedInstanceState.getFloat("zoomLevel", 1.0f);
    }

    // AsyncTask pour charger le PDF
    private class LoadPdfTask extends AsyncTask<String, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            if (progressBar != null) {
                progressBar.setVisibility(View.VISIBLE);
            }
            if (textViewLoading != null) {
                textViewLoading.setVisibility(View.VISIBLE);
            }
            if (imageViewPdf != null) {
                imageViewPdf.setVisibility(View.GONE);
            }
        }

        @Override
        protected Boolean doInBackground(String... params) {
            try {
                if (params[0] == null) {
                    return false;
                }

                File file = new File(params[0]);
                if (!file.exists()) {
                    return false;
                }

                fileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
                pdfRenderer = new PdfRenderer(fileDescriptor);
                totalPages = pdfRenderer.getPageCount();

                return true;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (progressBar != null) {
                progressBar.setVisibility(View.GONE);
            }
            if (textViewLoading != null) {
                textViewLoading.setVisibility(View.GONE);
            }

            if (success && totalPages > 0) {
                if (imageViewPdf != null) {
                    imageViewPdf.setVisibility(View.VISIBLE);
                }

                // Afficher le hint de swipe pour tous les PDF
                if (textViewSwipeHint != null) {
                    textViewSwipeHint.setVisibility(View.VISIBLE);
                    textViewSwipeHint.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (textViewSwipeHint != null) {
                                textViewSwipeHint.setVisibility(View.GONE);
                            }
                        }
                    }, 3000);
                }

                renderPage(0);
            } else {
                Toast.makeText(PdfBoxViewerActivity.this,
                        "Erreur lors du chargement du PDF", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    // AsyncTask pour rendre une page
    private class RenderPageTask extends AsyncTask<Integer, Void, Bitmap> {

        @Override
        protected void onPreExecute() {
            if (progressBar != null) {
                progressBar.setVisibility(View.VISIBLE);
            }
        }

        @Override
        protected Bitmap doInBackground(Integer... params) {
            PdfRenderer.Page page = null;
            try {
                int pageNum = params[0];
                page = pdfRenderer.openPage(pageNum);

                // Calculer les dimensions optimales
                int[] dimensions = calculateOptimalDimensions(page);

                // Créer le bitmap avec la qualité optimale
                Bitmap bitmap = Bitmap.createBitmap(
                        dimensions[0],
                        dimensions[1],
                        Bitmap.Config.ARGB_8888
                );

                // Remplir avec fond blanc
                android.graphics.Canvas canvas = new android.graphics.Canvas(bitmap);
                canvas.drawColor(Color.WHITE);

                // Rendre la page PDF avec la meilleure qualité
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);

                return bitmap;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            } finally {
                if (page != null) {
                    page.close();
                }
            }
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (progressBar != null) {
                progressBar.setVisibility(View.GONE);
            }

            if (bitmap != null && imageViewPdf != null) {
                imageViewPdf.setImageBitmap(bitmap);

                // Animation fluide
                imageViewPdf.setAlpha(0f);
                imageViewPdf.animate().alpha(1f).setDuration(200).start();

                // Réinitialiser le défilement
                resetScrollPosition();

                // Ajuster la taille de l'image view
                ViewGroup.LayoutParams params = imageViewPdf.getLayoutParams();
                params.width = bitmap.getWidth();
                params.height = bitmap.getHeight();
                imageViewPdf.setLayoutParams(params);

            } else {
                Toast.makeText(PdfBoxViewerActivity.this,
                        "Erreur lors du rendu de la page", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Méthode pour la configuration du thème
    private void setupTheme(ActionBar ab, TextView actionBarTitle) {
        UiModeManager uiModeManager = null;
        switch (Themes.getName(getApplicationContext())) {
            case "system":
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    uiModeManager = (UiModeManager) getSystemService(Context.UI_MODE_SERVICE);
                }
                if (uiModeManager != null) {
                    int currentMode = uiModeManager.getNightMode();
                    if (currentMode == UiModeManager.MODE_NIGHT_YES) {
                        ab.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.black)));
                        if (actionBarTitle != null) {
                            actionBarTitle.setTextColor(Color.parseColor("#B4EFEFEF"));
                        }
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
                if (actionBarTitle != null) {
                    actionBarTitle.setTextColor(Color.parseColor("#B4EFEFEF"));
                }
                break;
        }
    }
}