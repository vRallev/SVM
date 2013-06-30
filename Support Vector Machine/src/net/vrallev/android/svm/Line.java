package net.vrallev.android.svm;

import android.view.View;
import net.vrallev.android.svm.gradient.NormalVector;

/**
 * @author Ralf Wondratschek
 */
public class Line implements Cloneable {

    private double mStartX;
    private double mStartY;
    private double mEndX;
    private double mEndY;

    public Line(double startX, double startY, double endX, double endY) {
        mStartX = startX;
        mStartY = startY;
        mEndX = endX;
        mEndY = endY;
    }

    public Line(double startX, double endX, GradientDescent.Argument argument) {
        mStartX = startX;
        mEndX = endX;

        float offset = argument.b;
        float m = argument.w1 / argument.w2;

        mStartY = m * mStartX + offset;
        mEndY = m * mEndX + offset;
    }

    public double getStartX() {
        return mStartX;
    }

    public Line setStartX(double startX) {
        mStartX = startX;
        return this;
    }

    public double getStartY() {
        return mStartY;
    }

    public Line setStartY(double startY) {
        mStartY = startY;
        return this;
    }

    public double getEndX() {
        return mEndX;
    }

    public Line setEndX(double endX) {
        mEndX = endX;
        return this;
    }

    public double getEndY() {
        return mEndY;
    }

    public Line setEndY(double endY) {
        this.mEndY = endY;
        return this;
    }

    public double getIncrease() {
        return (mEndY - mStartY) / (mEndX - mStartX);
    }

    public double getOffset() {
        return mStartY - (getIncrease() * mStartX);
    }

    public double[] getNormalVector() {
        return new double[]{-1 * getIncrease(), 1};
    }

    public NormalVector getNormalVectorNew() {
        return new NormalVector(-1 * getIncrease(), 1);
    }

    public void stretchTo(float startX, float endX, final View animateView) {
        if (getEndX() < getStartX()) {
            flipPoints();
        }

        float increase = (float) getIncrease();
        float offset = (float) getOffset();

        if (animateView == null) {
            setStartX(startX);
            setStartY(increase * startX + offset);

            setEndX(endX);
            setEndY(increase * endX + offset);

        } else {
//            PropertyValuesHolder holderStartX = PropertyValuesHolder.ofFloat("startX", getStartX(), startX);
//            PropertyValuesHolder holderEndX = PropertyValuesHolder.ofFloat("endX", getEndX(), endX);
//            PropertyValuesHolder holderStartY = PropertyValuesHolder.ofFloat("startY", getStartY(), increase * startX + offset);
//            PropertyValuesHolder holderEndY = PropertyValuesHolder.ofFloat("endY", getEndY(), increase * endX + offset);
//
//            ObjectAnimator objectAnimator = ObjectAnimator.ofPropertyValuesHolder(this, holderStartX, holderEndX, holderStartY, holderEndY);
//            objectAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
//            objectAnimator.setDuration(1000l);
//            objectAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
//                @Override
//                public void onAnimationUpdate(ValueAnimator animation) {
//                    animateView.invalidate();
//                }
//            });
//            objectAnimator.start();
        }
    }

    private void flipPoints() {
        double val = getStartX();
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
