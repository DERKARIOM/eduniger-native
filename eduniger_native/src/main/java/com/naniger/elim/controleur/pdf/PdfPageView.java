package com.naniger.elim.controleur.pdf;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;

import java.util.ArrayList;
import java.util.List;

/**
 * Une page du document :
 * <ul>
 *   <li>hauteur réservée dès l'affichage selon le ratio de la page (aucun « saut » quand le
 *       rendu arrive) ;</li>
 *   <li>surlignage des résultats de recherche (le résultat courant est plus marqué) ;</li>
 *   <li>mode « couleurs inversées » pour lire dans le noir.</li>
 * </ul>
 */
public class PdfPageView extends AppCompatImageView {

    private static final ColorMatrixColorFilter INVERT = new ColorMatrixColorFilter(new ColorMatrix(new float[]{
            -1, 0, 0, 0, 255,
            0, -1, 0, 0, 255,
            0, 0, -1, 0, 255,
            0, 0, 0, 1, 0}));

    private float mAspect = 1.414f;
    private float mPageWidthPoints = 0f;
    private final List<RectF> mHighlights = new ArrayList<>();
    private final List<RectF> mCurrent = new ArrayList<>();
    private final Paint mHighlightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint mCurrentPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF mTmp = new RectF();

    public PdfPageView(Context context) {
        this(context, null);
    }

    public PdfPageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setScaleType(ScaleType.FIT_XY);
        mHighlightPaint.setColor(0x55FFC107);   // jaune translucide
        mCurrentPaint.setColor(0x88FF9800);     // orange plus marqué
    }

    public void setAspect(float aspect) {
        if (aspect > 0 && Math.abs(aspect - mAspect) > 0.001f) {
            mAspect = aspect;
            requestLayout();
        }
    }

    public void setInverted(boolean inverted) {
        setColorFilter(inverted ? INVERT : null);
    }

    /** Rectangles en points PDF ; {@code pageWidthPoints} sert à les mettre à l'échelle. */
    public void setHighlights(float pageWidthPoints, List<RectF> all, List<RectF> current) {
        mPageWidthPoints = pageWidthPoints;
        mHighlights.clear();
        mCurrent.clear();
        if (all != null) mHighlights.addAll(all);
        if (current != null) mCurrent.addAll(current);
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int w = MeasureSpec.getSize(widthMeasureSpec);
        setMeasuredDimension(w, Math.round(w * mAspect));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mPageWidthPoints <= 0 || (mHighlights.isEmpty() && mCurrent.isEmpty())) return;
        float k = getWidth() / mPageWidthPoints;
        for (RectF r : mHighlights) drawRect(canvas, r, k, mHighlightPaint);
        for (RectF r : mCurrent) drawRect(canvas, r, k, mCurrentPaint);
    }

    private void drawRect(Canvas canvas, RectF r, float k, Paint paint) {
        mTmp.set(r.left * k, r.top * k, r.right * k, r.bottom * k);
        canvas.drawRect(mTmp, paint);
    }
}
