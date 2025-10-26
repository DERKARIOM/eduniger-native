package com.ninotech.fabi.controleur.activity;

import android.app.UiModeManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.pdf.PdfRenderer;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.ninotech.fabi.R;
import com.ninotech.fabi.controleur.custo.StatusBarCusto;
import com.ninotech.fabi.model.data.Themes;

import java.io.File;

/**
 * Visionneuse PDF moderne utilisant l'API Android native PdfRenderer
 * Compatible Android 5.0+ (API 21+)
 */
public class PdfBoxViewerActivity extends AppCompatActivity {

    private ImageView imageViewPdf;
    private TextView textViewPageInfo;
    private TextView textViewZoomLevel;
    private TextView textViewSwipeHint;
    private ImageButton btnPrevious, btnNext, btnZoomIn, btnZoomOut;
    private ProgressBar progressBar;
    private ProgressBar progressBarPage;
    private ScrollView scrollViewVertical;
    private HorizontalScrollView scrollViewHorizontal;

    private PdfRenderer pdfRenderer;
    private ParcelFileDescriptor fileDescriptor;

    private String pdfPath;
    private String pdfTitle;
    private int currentPage = 0;
    private int totalPages = 0;
    private float zoomLevel = 1.0f;

    private int screenWidth;
    private int screenHeight;
    private boolean isA5Format = false;

    private GestureDetector gestureDetector;
    private static final int SWIPE_THRESHOLD = 100;
    private static final int SWIPE_VELOCITY_THRESHOLD = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdfbox_viewer);
        //getSupportActionBar().hide();
        // Mode plein écran pour format A5
        ActionBar ab = getSupportActionBar();
        assert ab != null;
        ab.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        ab.setCustomView(R.layout.custom_action_bar);
        ab.setDisplayHomeAsUpEnabled(true);
        TextView actionBarTitle = ab.getCustomView().findViewById(R.id.action_bar_title);
        UiModeManager uiModeManager = null;
        switch (Themes.getName(getApplicationContext()))
        {
            case "system":
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    uiModeManager = (UiModeManager) getSystemService(Context.UI_MODE_SERVICE);
                }
                int currentMode = uiModeManager.getNightMode();
                if (currentMode == UiModeManager.MODE_NIGHT_YES) {
                    // mode sombre
                    ab.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.black)));
                    actionBarTitle.setTextColor(Color.parseColor("#B4EFEFEF"));
                } else {
                    // mode jours
                    ab.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.white)));
                    ab.setHomeAsUpIndicator(R.drawable.vector_back);
                }
                break;
            case "notNight":
                // mode jours
                ab.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.white)));
                ab.setHomeAsUpIndicator(R.drawable.vector_back);
                break;
            case "night":
                // mode nuit
                ab.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.black)));
                ab.setHomeAsUpIndicator(R.drawable.vector_white_sombre_back);
                actionBarTitle.setTextColor(Color.parseColor("#B4EFEFEF"));
                break;
        }
//        getWindow().setFlags(
//                WindowManager.LayoutParams.FLAG_FULLSCREEN,
//                WindowManager.LayoutParams.FLAG_FULLSCREEN
//        );

        // Récupérer les dimensions de l'écran
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        screenWidth = displayMetrics.widthPixels;
        screenHeight = displayMetrics.heightPixels;

        // Récupérer les données
        pdfPath = getIntent().getStringExtra("PDF_PATH");
        pdfTitle = getIntent().getStringExtra("PDF_TITLE");
        actionBarTitle.setText(pdfTitle);

        // Initialiser les vues
        initViews();

        // Initialiser le détecteur de gestes
        setupGestureDetector();

        // Charger le PDF
        loadPdf();
    }

    private void initViews() {
        imageViewPdf = findViewById(R.id.imageViewPdf);
        textViewPageInfo = findViewById(R.id.textViewPageInfo);
        textViewZoomLevel = findViewById(R.id.textViewZoomLevel);
        textViewSwipeHint = findViewById(R.id.textViewSwipeHint);
        btnPrevious = findViewById(R.id.btnPrevious);
        btnNext = findViewById(R.id.btnNext);
        btnZoomIn = findViewById(R.id.btnZoomIn);
        btnZoomOut = findViewById(R.id.btnZoomOut);
        progressBar = findViewById(R.id.progressBar);
        progressBarPage = findViewById(R.id.progressBarPage);

        // Configuration de l'ImageView pour ajustement optimal
        imageViewPdf.setScaleType(ImageView.ScaleType.FIT_CENTER);

        btnPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                previousPage();
            }
        });

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nextPage();
            }
        });

        btnZoomIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                zoomIn();
            }
        });

        btnZoomOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                zoomOut();
            }
        });
    }

    private void setupGestureDetector() {
        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                try {
                    float diffX = e2.getX() - e1.getX();
                    float diffY = e2.getY() - e1.getY();

                    // Vérifier que le mouvement horizontal est dominant
                    if (Math.abs(diffX) > Math.abs(diffY)) {
                        if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
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
        });
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        gestureDetector.onTouchEvent(ev);
        return super.dispatchTouchEvent(ev);
    }

    private void onSwipeRight() {
        previousPage();
    }

    private void onSwipeLeft() {
        nextPage();
    }

    private void loadPdf() {
        new LoadPdfTask().execute(pdfPath);
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
        textViewPageInfo.setText(String.format("Page %d / %d", currentPage + 1, totalPages));

        // Mettre à jour la barre de progression
        if (totalPages > 0) {
            int progress = (int) (((float) (currentPage + 1) / totalPages) * 100);
            progressBarPage.setProgress(progress);
        }

        // Mettre à jour le niveau de zoom
        updateZoomLevel();
    }

    private void updateZoomLevel() {
        int zoomPercent = (int) (zoomLevel * 100);
        textViewZoomLevel.setText(String.format("Zoom: %d%%", zoomPercent));
    }

    private void updateNavigationButtons() {
        btnPrevious.setEnabled(currentPage > 0);
        btnNext.setEnabled(currentPage < totalPages - 1);
    }

    private void previousPage() {
        if (currentPage > 0) {
            renderPage(currentPage - 1);
            //Toast.makeText(this, "Page précédente", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Première page", Toast.LENGTH_SHORT).show();
        }
    }

    private void nextPage() {
        if (currentPage < totalPages - 1) {
            renderPage(currentPage + 1);
            //Toast.makeText(this, "Page suivante", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Dernière page", Toast.LENGTH_SHORT).show();
        }
    }

    private void zoomIn() {
        if (zoomLevel < 3.0f) {
            zoomLevel += 0.25f;
            renderPage(currentPage);
            updateZoomLevel();
        } else {
            Toast.makeText(this, "Zoom maximum atteint", Toast.LENGTH_SHORT).show();
        }
    }

    private void zoomOut() {
        if (zoomLevel > 0.5f) {
            zoomLevel -= 0.25f;
            renderPage(currentPage);
            updateZoomLevel();
        } else {
            Toast.makeText(this, "Zoom minimum atteint", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Détecter si le PDF est au format A5 (420 x 595 points)
     */
    private boolean detectA5Format(PdfRenderer.Page page) {
        int width = page.getWidth();
        int height = page.getHeight();

        // Format A5 en points: 420 x 595 (portrait) ou 595 x 420 (paysage)
        // Tolérance de ±10 points
        boolean isA5Portrait = Math.abs(width - 420) < 10 && Math.abs(height - 595) < 10;
        boolean isA5Landscape = Math.abs(width - 595) < 10 && Math.abs(height - 420) < 10;

        return isA5Portrait || isA5Landscape;
    }

    /**
     * Calculer les dimensions optimales pour remplir l'écran
     */
    private int[] calculateOptimalDimensions(PdfRenderer.Page page) {
        int pageWidth = page.getWidth();
        int pageHeight = page.getHeight();

        // Soustraire la hauteur des contrôles
        int availableHeight = screenHeight - 200;

        // Calculer le ratio
        float widthRatio = (float) screenWidth / pageWidth;
        float heightRatio = (float) availableHeight / pageHeight;
        float optimalRatio = Math.min(widthRatio, heightRatio);

        // Appliquer le zoom
        optimalRatio *= zoomLevel;

        int width = (int) (pageWidth * optimalRatio);
        int height = (int) (pageHeight * optimalRatio);

        return new int[]{width, height};
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
            progressBar.setVisibility(View.VISIBLE);
            imageViewPdf.setVisibility(View.GONE);
        }

        @Override
        protected Boolean doInBackground(String... params) {
            try {
                File file = new File(params[0]);
                if (!file.exists()) {
                    return false;
                }

                // Ouvrir le fichier PDF avec PdfRenderer natif Android
                fileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
                pdfRenderer = new PdfRenderer(fileDescriptor);
                totalPages = pdfRenderer.getPageCount();

                // Détecter le format A5
                if (totalPages > 0) {
                    PdfRenderer.Page firstPage = pdfRenderer.openPage(0);
                    isA5Format = detectA5Format(firstPage);
                    firstPage.close();
                }

                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            progressBar.setVisibility(View.GONE);

            if (success && totalPages > 0) {
                imageViewPdf.setVisibility(View.VISIBLE);

                if (isA5Format) {
                    // Afficher le hint de swipe pendant 3 secondes
                    textViewSwipeHint.setVisibility(View.VISIBLE);
                    textViewSwipeHint.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            textViewSwipeHint.setVisibility(View.GONE);
                        }
                    }, 3000);

//                    Toast.makeText(PdfBoxViewerActivity.this,
//                            "Format A5 détecté - Optimisation plein écran",
//                            Toast.LENGTH_SHORT).show();
                }

                renderPage(0);
            } else {
                Toast.makeText(PdfBoxViewerActivity.this,
                        "Erreur lors du chargement du PDF", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    // AsyncTask pour rendre une page
    private class RenderPageTask extends AsyncTask<Integer, Void, Bitmap> {

        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Bitmap doInBackground(Integer... params) {
            PdfRenderer.Page page = null;
            try {
                int pageNum = params[0];
                page = pdfRenderer.openPage(pageNum);

                // Calculer les dimensions optimales
                int[] dimensions;
                if (isA5Format && zoomLevel == 1.0f) {
                    dimensions = calculateOptimalDimensions(page);
                } else {
                    int width = (int) (page.getWidth() * zoomLevel * 2);
                    int height = (int) (page.getHeight() * zoomLevel * 2);
                    dimensions = new int[]{width, height};
                }

                // Créer le bitmap avec fond blanc
                Bitmap bitmap = Bitmap.createBitmap(
                        dimensions[0],
                        dimensions[1],
                        Bitmap.Config.ARGB_8888
                );

                // Remplir de blanc
                android.graphics.Canvas canvas = new android.graphics.Canvas(bitmap);
                canvas.drawColor(android.graphics.Color.WHITE);
                canvas.drawBitmap(bitmap, 0, 0, null);

                // Rendre la page PDF sur le bitmap
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
            progressBar.setVisibility(View.GONE);

            if (bitmap != null) {
                imageViewPdf.setImageBitmap(bitmap);
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
                onBackPressed(); // Appel de la méthode onBackPressed() pour simuler le comportement du bouton retour
                return true;

        }
        return super.onOptionsItemSelected(item);
    }
}