package net.vrallev.android.svm;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

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
            long duration = 1000l;
            AccelerateDecelerateInterpolator interpolator = new AccelerateDecelerateInterpolator();

            ObjectAnimator animatorStartX = ObjectAnimator.ofFloat(this, "startX", getStartX(), startX);
            animatorStartX.setDuration(duration);
            animatorStartX.setInterpolator(interpolator);

            ObjectAnimator animatorEndX = ObjectAnimator.ofFloat(this, "endX", getEndX(), endX);
            animatorEndX.setDuration(duration);
            animatorEndX.setInterpolator(interpolator);

            ObjectAnimator animatorStartY = ObjectAnimator.ofFloat(this, "startY", getStartY(), increase * startX + offset);
            animatorStartY.setDuration(duration);
            animatorStartY.setInterpolator(interpolator);

            ObjectAnimator animatorEndY = ObjectAnimator.ofFloat(this, "endY", getEndY(), increase * endX + offset);
            animatorEndY.setDuration(duration);
            animatorEndY.setInterpolator(interpolator);

            animatorStartX.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    animateView.invalidate();
                }
            });

            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.playTogether(animatorStartX, animatorEndX, animatorStartY, animatorEndY);
            animatorSet.start();
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
    protected Line clone() {
        return new Line(mStartX, mStartY, mEndX, mEndY);
    }
}
