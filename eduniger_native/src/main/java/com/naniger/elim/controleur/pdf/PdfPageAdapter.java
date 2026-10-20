package com.naniger.elim.controleur.pdf;

import android.graphics.Bitmap;
import android.graphics.RectF;
import android.util.LruCache;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

/**
 * Pages du document dans la liste. Seules les pages visibles (et leurs voisines, via
 * {@link #prefetch}) sont rendues ; les bitmaps sont gardés dans un cache LRU limité à
 * 1/6 de la mémoire de l'application — jamais tout le document en mémoire.
 */
public class PdfPageAdapter extends RecyclerView.Adapter<PdfPageAdapter.PageHolder> {

    private final PdfDocument mDocument;
    private final LruCache<Integer, Bitmap> mCache;
    private final Map<Integer, Future<?>> mPending = new HashMap<>();
    private final int mGapPx;

    private int mPageWidthPx = 0;
    private float mRenderZoom = 1f;
    private boolean mInverted;

    private Map<Integer, List<RectF>> mHighlights = Collections.emptyMap();
    private int mCurrentHitPage = -1;
    private List<RectF> mCurrentHitRects = Collections.emptyList();

    public PdfPageAdapter(PdfDocument document, int gapPx) {
        mDocument = document;
        mGapPx = gapPx;
        int maxKb = (int) (Runtime.getRuntime().maxMemory() / 1024);
        mCache = new LruCache<Integer, Bitmap>(Math.max(8 * 1024, maxKb / 6)) {
            @Override
            protected int sizeOf(Integer key, Bitmap value) {
                return value.getByteCount() / 1024;
            }
        };
    }

    /** Largeur d'affichage des pages (largeur de la liste). */
    public void setPageWidth(int px) {
        if (px > 0 && px != mPageWidthPx) {
            mPageWidthPx = px;
            mCache.evictAll();
            cancelAll();
            notifyDataSetChanged();
        }
    }

    /** Zoom actuel : au-delà de 1, les pages visibles sont redessinées plus finement (max 2,5×). */
    public void setRenderZoom(float zoom, int firstVisible, int lastVisible) {
        float z = Math.max(1f, Math.min(2.5f, zoom));
        if (Math.abs(z - mRenderZoom) < 0.2f) return;
        mRenderZoom = z;
        // Les bitmaps en cache restent affichés (flous) jusqu'au nouveau rendu.
        for (int i = Math.max(0, firstVisible); i <= lastVisible && i < getItemCount(); i++) {
            notifyItemChanged(i, "rerender");
        }
    }

    public void setInverted(boolean inverted) {
        mInverted = inverted;
        notifyDataSetChanged();
    }

    public void setHighlights(Map<Integer, List<RectF>> perPage, int currentPage, List<RectF> currentRects) {
        mHighlights = perPage != null ? perPage : Collections.emptyMap();
        mCurrentHitPage = currentPage;
        mCurrentHitRects = currentRects != null ? currentRects : Collections.emptyList();
        notifyDataSetChanged();
    }

    /** Prépare les pages voisines de la zone visible (défilement plus fluide). */
    public void prefetch(int first, int last) {
        for (int i = first - 1; i <= last + 1; i++) {
            if (i >= 0 && i < getItemCount() && mCache.get(i) == null && !mPending.containsKey(i)) {
                requestRender(i, null);
            }
        }
    }

    public void release() {
        cancelAll();
        mCache.evictAll();
    }

    @Override
    public int getItemCount() {
        return mDocument.getPageCount();
    }

    @NonNull
    @Override
    public PageHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        PdfPageView v = new PdfPageView(parent.getContext());
        RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.bottomMargin = mGapPx;
        v.setLayoutParams(lp);
        v.setBackgroundColor(0xFFFFFFFF);
        return new PageHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull PageHolder h, int position, @NonNull List<Object> payloads) {
        if (!payloads.isEmpty() && payloads.contains("rerender")) {
            requestRender(position, h);
            return;
        }
        super.onBindViewHolder(h, position, payloads);
    }

    @Override
    public void onBindViewHolder(@NonNull PageHolder h, int position) {
        PdfPageView v = h.page;
        h.boundPage = position;
        v.setAspect(mDocument.aspect(position));
        v.setInverted(mInverted);
        v.setContentDescription("Page " + (position + 1));
        List<RectF> all = mHighlights.get(position);
        v.setHighlights(mDocument.pageWidthPoints(position), all,
                position == mCurrentHitPage ? mCurrentHitRects : null);

        Bitmap cached = mCache.get(position);
        if (cached != null) {
            v.setImageBitmap(cached);
            // Rendu plus net nécessaire (zoom) ? On compare à la taille réellement atteignable,
            // plafonnée par PdfDocument.MAX_BITMAP_BYTES, pour ne jamais boucler.
            if (cached.getWidth() < expectedWidth(position) * 0.9f) requestRender(position, h);
        } else {
            v.setImageDrawable(null);
            requestRender(position, h);
        }
    }

    @Override
    public void onViewRecycled(@NonNull PageHolder h) {
        Future<?> f = mPending.remove(h.boundPage);
        if (f != null) f.cancel(false);   // pas encore commencé : jamais calculé
        h.boundPage = -1;
        h.page.setImageDrawable(null);
    }

    private int expectedWidth(int page) {
        float pw = mDocument.pageWidthPoints(page);
        float ph = pw * mDocument.aspect(page);
        return PageGeometry.bitmapSize(pw, ph, mPageWidthPx, mRenderZoom, PdfDocument.MAX_BITMAP_BYTES)[0];
    }

    private void requestRender(int position, PageHolder holder) {
        if (mPageWidthPx <= 0) return;
        Future<?> old = mPending.remove(position);
        if (old != null) old.cancel(false);
        final float zoom = mRenderZoom;
        Future<?> f = mDocument.render(position, mPageWidthPx, zoom, new PdfDocument.RenderCallback() {
            @Override
            public void onRendered(int page, Bitmap bitmap) {
                mPending.remove(page);
                mCache.put(page, bitmap);
                // Affichage direct si la vue montre toujours cette page (pas de notify :
                // évite le clignotement). Sinon la page sera prise dans le cache au prochain bind.
                if (holder != null && holder.boundPage == page) {
                    holder.page.setAspect(mDocument.aspect(page));
                    holder.page.setImageBitmap(bitmap);
                }
            }

            @Override
            public void onRenderError(int page) {
                mPending.remove(page);
            }
        });
        mPending.put(position, f);
    }

    private void cancelAll() {
        for (Future<?> f : new ArrayList<>(mPending.values())) f.cancel(false);
        mPending.clear();
    }

    static final class PageHolder extends RecyclerView.ViewHolder {
        final PdfPageView page;
        int boundPage = -1;

        PageHolder(PdfPageView v) {
            super(v);
            page = v;
        }
    }
}
