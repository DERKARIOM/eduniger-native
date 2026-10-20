package com.naniger.elim.controleur.pdf;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.RectF;
import android.graphics.pdf.PdfRenderer;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Accès au document PDF via le moteur natif d'Android ({@link PdfRenderer}, gratuit, sans
 * dépendance). {@link PdfRenderer} n'est PAS thread-safe : toutes les opérations passent par
 * un unique fil d'exécution, ce qui supprime les plantages « current page not closed » de
 * l'ancien lecteur (deux tâches de rendu concurrentes).
 *
 * <ul>
 *   <li>Rendu page par page, à la demande, annulable (une page qui sort de l'écran avant
 *       d'être dessinée n'est jamais calculée).</li>
 *   <li>Taille des bitmaps plafonnée ({@link #MAX_BITMAP_BYTES}) : un zoom fort ne peut pas
 *       provoquer de manque de mémoire.</li>
 *   <li>Recherche de texte : Android 15+ uniquement ({@code Page.searchText}, API 35). Sur
 *       les versions précédentes, PdfRenderer ne donne pas accès au texte.</li>
 *   <li>Fermeture sûre : le moteur est fermé sur son propre fil, après la tâche en cours.</li>
 * </ul>
 */
public class PdfDocument {

    private static final String TAG = "PdfDocument";
    /** Taille maximale d'une page rendue (≈ 2 400 × 3 300 px). */
    public static final long MAX_BITMAP_BYTES = 32L * 1024 * 1024;

    public interface OpenCallback {
        void onOpened(int pageCount);

        /** @param reason message destiné à l'utilisateur */
        void onError(String reason);
    }

    public interface RenderCallback {
        void onRendered(int page, Bitmap bitmap);

        void onRenderError(int page);
    }

    /** Un résultat de recherche : page + rectangles en points PDF. */
    public static final class SearchHit {
        public final int page;
        public final List<RectF> rects;

        SearchHit(int page, List<RectF> rects) {
            this.page = page;
            this.rects = rects;
        }
    }

    public interface SearchCallback {
        void onProgress(int pagesSearched, int pageCount);

        void onDone(List<SearchHit> hits);
    }

    private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();
    private final Handler mMain = new Handler(Looper.getMainLooper());
    private final AtomicInteger mSearchGeneration = new AtomicInteger();

    private PdfRenderer mRenderer;
    private ParcelFileDescriptor mFd;
    private volatile boolean mClosed;
    private volatile int mPageCount;
    /** Taille de chaque page en points (connue après rendu ; défaut = page 1). */
    private volatile float[] mWidths = new float[0];
    private volatile float[] mHeights = new float[0];

    public void open(String path, OpenCallback cb) {
        mExecutor.execute(() -> {
            String error = null;
            try {
                if (path == null || path.isEmpty()) {
                    error = "Aucun document à ouvrir.";
                } else {
                    File f = new File(path);
                    if (!f.exists()) error = "Le fichier est introuvable. Il a peut-être été supprimé : téléchargez à nouveau le livre.";
                    else if (!f.canRead()) error = "Le fichier n'est pas accessible.";
                    else if (f.length() == 0) error = "Le fichier est vide. Téléchargez à nouveau le livre.";
                    else {
                        mFd = ParcelFileDescriptor.open(f, ParcelFileDescriptor.MODE_READ_ONLY);
                        mRenderer = new PdfRenderer(mFd);
                        int n = mRenderer.getPageCount();
                        if (n <= 0) {
                            error = "Ce document ne contient aucune page.";
                        } else {
                            float[] w = new float[n];
                            float[] h = new float[n];
                            PdfRenderer.Page first = mRenderer.openPage(0);
                            float w0 = first.getWidth(), h0 = first.getHeight();
                            first.close();
                            for (int i = 0; i < n; i++) { w[i] = w0; h[i] = h0; }
                            mWidths = w;
                            mHeights = h;
                            mPageCount = n;
                        }
                    }
                }
            } catch (SecurityException e) {
                error = "Ce document est protégé par un mot de passe et ne peut pas être ouvert.";
            } catch (Exception e) {
                Log.e(TAG, "Ouverture impossible", e);
                error = "Le fichier est endommagé ou n'est pas un PDF valide. Téléchargez à nouveau le livre.";
            } catch (OutOfMemoryError e) {
                error = "Mémoire insuffisante pour ouvrir ce document.";
            }
            final String err = error;
            final int count = mPageCount;
            mMain.post(() -> {
                if (mClosed) return;
                if (err != null) cb.onError(err);
                else cb.onOpened(count);
            });
        });
    }

    public int getPageCount() {
        return mPageCount;
    }

    /** Rapport hauteur / largeur de la page (connu exactement une fois la page rendue). */
    public float aspect(int page) {
        float[] w = mWidths, h = mHeights;
        if (page < 0 || page >= w.length || w[page] <= 0) return 1.414f;
        return h[page] / w[page];
    }

    public float pageWidthPoints(int page) {
        float[] w = mWidths;
        return (page >= 0 && page < w.length) ? w[page] : 0f;
    }

    /**
     * Demande le rendu d'une page.
     *
     * @return tâche annulable (annuler une tâche pas encore commencée évite tout calcul).
     */
    public Future<?> render(int page, int widthPx, float zoom, RenderCallback cb) {
        return mExecutor.submit(() -> {
            if (mClosed || mRenderer == null || page < 0 || page >= mPageCount) return;
            if (Thread.currentThread().isInterrupted()) return;
            PdfRenderer.Page p = null;
            try {
                p = mRenderer.openPage(page);
                float pw = p.getWidth(), ph = p.getHeight();
                mWidths[page] = pw;
                mHeights[page] = ph;
                int[] size = PageGeometry.bitmapSize(pw, ph, widthPx, zoom, MAX_BITMAP_BYTES);
                Bitmap b = Bitmap.createBitmap(size[0], size[1], Bitmap.Config.ARGB_8888);
                b.eraseColor(Color.WHITE);
                p.render(b, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
                mMain.post(() -> {
                    if (!mClosed) cb.onRendered(page, b);
                });
            } catch (OutOfMemoryError | Exception e) {
                Log.e(TAG, "Rendu impossible page " + (page + 1), e);
                mMain.post(() -> {
                    if (!mClosed) cb.onRenderError(page);
                });
            } finally {
                if (p != null) {
                    try { p.close(); } catch (Exception ignored) { }
                }
            }
        });
    }

    /** La recherche de texte n'existe dans PdfRenderer qu'à partir d'Android 15 (API 35). */
    public static boolean isSearchSupported() {
        return Build.VERSION.SDK_INT >= 35;
    }

    /** Lance une recherche ; une nouvelle recherche annule la précédente. */
    public void search(String query, SearchCallback cb) {
        final int generation = mSearchGeneration.incrementAndGet();
        if (!isSearchSupported() || query == null || query.trim().isEmpty()) {
            mMain.post(() -> cb.onDone(new ArrayList<>()));
            return;
        }
        mExecutor.execute(() -> {
            List<SearchHit> hits = new ArrayList<>();
            int n = mPageCount;
            for (int i = 0; i < n; i++) {
                if (mClosed || generation != mSearchGeneration.get()) return;
                try {
                    if (Build.VERSION.SDK_INT >= 35) hits.addAll(searchPage(i, query.trim()));
                } catch (Exception e) {
                    Log.w(TAG, "Recherche impossible page " + (i + 1) + " : " + e.getMessage());
                }
                if (i % 10 == 9 || i == n - 1) {
                    final int done = i + 1;
                    mMain.post(() -> {
                        if (!mClosed && generation == mSearchGeneration.get()) cb.onProgress(done, n);
                    });
                }
            }
            mMain.post(() -> {
                if (!mClosed && generation == mSearchGeneration.get()) cb.onDone(hits);
            });
        });
    }

    public void cancelSearch() {
        mSearchGeneration.incrementAndGet();
    }

    @RequiresApi(35)
    private List<SearchHit> searchPage(int page, String query) {
        List<SearchHit> out = new ArrayList<>();
        PdfRenderer.Page p = mRenderer.openPage(page);
        try {
            List<android.graphics.pdf.models.PageMatchBounds> matches = p.searchText(query);
            if (matches != null) {
                for (android.graphics.pdf.models.PageMatchBounds m : matches) {
                    List<RectF> rects = m.getBounds();
                    if (rects != null && !rects.isEmpty()) out.add(new SearchHit(page, new ArrayList<>(rects)));
                }
            }
        } finally {
            p.close();
        }
        return out;
    }

    /** Ferme le document. Sûr à appeler plusieurs fois ; les rendus en attente sont abandonnés. */
    public void close() {
        if (mClosed) return;
        mClosed = true;
        mSearchGeneration.incrementAndGet();
        mExecutor.execute(() -> {
            try { if (mRenderer != null) mRenderer.close(); } catch (Exception ignored) { }
            try { if (mFd != null) mFd.close(); } catch (Exception ignored) { }
            mRenderer = null;
            mFd = null;
        });
        mExecutor.shutdown();
    }
}
