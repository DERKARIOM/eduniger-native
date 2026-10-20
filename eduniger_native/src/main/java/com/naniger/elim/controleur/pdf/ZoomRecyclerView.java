package com.naniger.elim.controleur.pdf;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.VelocityTracker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Liste des pages avec zoom :
 * <ul>
 *   <li>pincer pour zoomer (1× à 4×), centré sur les doigts ;</li>
 *   <li>double-tap : 2,5× à l'endroit touché, ou retour à 1× ;</li>
 *   <li>zoomé : glisser déplace dans les deux sens, avec inertie verticale ;</li>
 *   <li>tap simple signalé à l'écran (afficher/masquer les barres).</li>
 * </ul>
 * Le zoom est appliqué au dessin (canvas) ; à la fin d'un geste, {@link Listener#onZoomSettled}
 * permet de redessiner les pages visibles en plus haute définition.
 */
public class ZoomRecyclerView extends RecyclerView {

    public interface Listener {
        void onSingleTap();

        void onZoomSettled(float scale);
    }

    private static final float MIN_SCALE = 1f;
    private static final float MAX_SCALE = 4f;
    private static final float DOUBLE_TAP_SCALE = 2.5f;

    private float mScale = 1f;
    private float mTx = 0f;
    private float mTy = 0f;
    private float mLastX, mLastY;
    private boolean mScaling;
    private int mActivePointer = -1;
    private VelocityTracker mVelocity;
    private ValueAnimator mAnimator;
    private Listener mListener;

    private final ScaleGestureDetector mScaleDetector;
    private final GestureDetector mGestureDetector;

    public ZoomRecyclerView(@NonNull Context context) {
        this(context, null);
    }

    public ZoomRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mScaleDetector = new ScaleGestureDetector(context, new ScaleGestureDetector.SimpleOnScaleGestureListener() {
            @Override
            public boolean onScaleBegin(@NonNull ScaleGestureDetector d) {
                mScaling = true;
                return true;
            }

            @Override
            public boolean onScale(@NonNull ScaleGestureDetector d) {
                zoomTo(mScale * d.getScaleFactor(), d.getFocusX(), d.getFocusY());
                return true;
            }

            @Override
            public void onScaleEnd(@NonNull ScaleGestureDetector d) {
                mScaling = false;
                if (mListener != null) mListener.onZoomSettled(mScale);
            }
        });
        mGestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapConfirmed(@NonNull MotionEvent e) {
                if (mListener != null) mListener.onSingleTap();
                return true;
            }

            @Override
            public boolean onDoubleTap(@NonNull MotionEvent e) {
                animateZoom(mScale > 1.05f ? 1f : DOUBLE_TAP_SCALE, e.getX(), e.getY());
                return true;
            }
        });
    }

    public void setListener(Listener listener) {
        mListener = listener;
    }

    public float getScale() {
        return mScale;
    }

    public void resetZoom() {
        if (mAnimator != null) mAnimator.cancel();
        mScale = 1f;
        mTx = 0f;
        mTy = 0f;
        invalidate();
    }

    private void zoomTo(float target, float fx, float fy) {
        float s = Math.max(MIN_SCALE, Math.min(MAX_SCALE, target));
        float k = s / mScale;
        mTx = fx - (fx - mTx) * k;
        mTy = fy - (fy - mTy) * k;
        mScale = s;
        clamp();
        invalidate();
    }

    private void animateZoom(float target, float fx, float fy) {
        if (mAnimator != null) mAnimator.cancel();
        mAnimator = ValueAnimator.ofFloat(mScale, target);
        mAnimator.setDuration(220);
        mAnimator.addUpdateListener(a -> zoomTo((float) a.getAnimatedValue(), fx, fy));
        mAnimator.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                if (mListener != null) mListener.onZoomSettled(mScale);
            }
        });
        mAnimator.start();
    }

    private void clamp() {
        mTx = PageGeometry.clampTranslation(mTx, getWidth(), mScale);
        mTy = PageGeometry.clampTranslation(mTy, getHeight(), mScale);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        canvas.save();
        canvas.translate(mTx, mTy);
        canvas.scale(mScale, mScale);
        super.dispatchDraw(canvas);
        canvas.restore();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent e) {
        // Zoomé ou deux doigts : c'est nous qui gérons le geste.
        if (mScale > 1f || e.getPointerCount() > 1) return true;
        return super.onInterceptTouchEvent(e);
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        mScaleDetector.onTouchEvent(e);
        mGestureDetector.onTouchEvent(e);

        if (mScale <= 1f && !mScaling && e.getPointerCount() == 1) {
            return super.onTouchEvent(e);   // défilement normal de la liste
        }

        if (mVelocity == null) mVelocity = VelocityTracker.obtain();
        mVelocity.addMovement(e);

        switch (e.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                stopScroll();
                mActivePointer = e.getPointerId(0);
                mLastX = e.getX();
                mLastY = e.getY();
                break;
            case MotionEvent.ACTION_POINTER_UP: {
                int idx = e.getActionIndex();
                if (e.getPointerId(idx) == mActivePointer) {
                    int other = idx == 0 ? 1 : 0;
                    mActivePointer = e.getPointerId(other);
                    mLastX = e.getX(other);
                    mLastY = e.getY(other);
                }
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                int idx = e.findPointerIndex(mActivePointer);
                if (idx < 0) idx = 0;
                float x = e.getX(idx), y = e.getY(idx);
                if (!mScaling) {
                    float dx = x - mLastX, dy = y - mLastY;
                    mTx += dx;
                    float oldTy = mTy;
                    mTy += dy;
                    clamp();
                    // Le reste du déplacement vertical fait défiler la liste.
                    float leftover = dy - (mTy - oldTy);
                    if (Math.abs(leftover) > 0.5f) scrollBy(0, Math.round(-leftover / mScale));
                    invalidate();
                }
                mLastX = x;
                mLastY = y;
                break;
            }
            case MotionEvent.ACTION_UP:
                mVelocity.computeCurrentVelocity(1000);
                float vy = mVelocity.getYVelocity();
                if (!mScaling && Math.abs(vy) > 300) fling(0, Math.round(-vy / mScale));
                // fall through
            case MotionEvent.ACTION_CANCEL:
                mActivePointer = -1;
                if (mVelocity != null) {
                    mVelocity.recycle();
                    mVelocity = null;
                }
                break;
            default:
                break;
        }
        return true;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        clamp();
    }
}
