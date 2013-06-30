package net.vrallev.android.svm;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import net.vrallev.android.svm.gradient.NormalVector;

/**
 * @author Ralf Wondratschek
 */
public class Line implements Cloneable {

    private float mStartX;
    private float mStartY;
    private float mEndX;
    private float mEndY;

    public Line(float startX, float startY, float endX, float endY) {
        mStartX = startX;
        mStartY = startY;
        mEndX = endX;
        mEndY = endY;
    }

    public Line(float startX, float endX, GradientDescent.Argument argument) {
        mStartX = startX;
        mEndX = endX;

        float offset = argument.b;
        float m = argument.w1 / argument.w2;

        mStartY = m * mStartX + offset;
        mEndY = m * mEndX + offset;
    }

    public float getStartX() {
        return mStartX;
    }

    public Line setStartX(float startX) {
        mStartX = startX;
        return this;
    }

    public float getStartY() {
        return mStartY;
    }

    public Line setStartY(float startY) {
        mStartY = startY;
        return this;
    }

    public float getEndX() {
        return mEndX;
    }

    public Line setEndX(float endX) {
        mEndX = endX;
        return this;
    }

    public float getEndY() {
        return mEndY;
    }

    public Line setEndY(float endY) {
        this.mEndY = endY;
        return this;
    }

    public float getIncrease() {
        return (mEndY - mStartY) / (mEndX - mStartX);
    }

    public float getOffset() {
        return mStartY - (getIncrease() * mStartX);
    }

    public float[] getNormalVector() {
        return new float[]{-1 * getIncrease(), 1};
    }

    public NormalVector getNormalVectorNew() {
        return new NormalVector(-1 * getIncrease(), 1);
    }

    public void stretchTo(float startX, float endX, final View animateView) {
        if (getEndX() < getStartX()) {
            flipPoints();
        }

        float increase = getIncrease();
        float offset = getOffset();

        if (animateView == null) {
            setStartX(startX);
            setStartY(increase * startX + offset);

            setEndX(endX);
            setEndY(increase * endX + offset);

        } else {
            PropertyValuesHolder holderStartX = PropertyValuesHolder.ofFloat("startX", getStartX(), startX);
            PropertyValuesHolder holderEndX = PropertyValuesHolder.ofFloat("endX", getEndX(), endX);
            PropertyValuesHolder holderStartY = PropertyValuesHolder.ofFloat("startY", getStartY(), increase * startX + offset);
            PropertyValuesHolder holderEndY = PropertyValuesHolder.ofFloat("endY", getEndY(), increase * endX + offset);

            ObjectAnimator objectAnimator = ObjectAnimator.ofPropertyValuesHolder(this, holderStartX, holderEndX, holderStartY, holderEndY);
            objectAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
            objectAnimator.setDuration(1000l);
            objectAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    animateView.invalidate();
                }
            });
            objectAnimator.start();
        }
    }

    private void flipPoints() {
        float val = getStartX();
        setStartX(getEndX());
        setEndX(val);

        val = getStartY();
        setStartY(getEndY());
        setEndY(val);
    }

    @Override
    public Line clone() {
        return new Line(mStartX, mStartY, mEndX, mEndY);
    }
}
