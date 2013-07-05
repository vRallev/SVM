package net.vrallev.android.svm.model;

import android.animation.*;
import android.view.animation.AccelerateDecelerateInterpolator;
import net.vrallev.android.svm.view.CartesianCoordinateSystem;

/**
 * @author Ralf Wondratschek
 */
public class Line implements Cloneable {

    private NormalVector mNormalVector;
    private double mOffset;

    public Line(double startX, double startY, double endX, double endY) {
        double m = (endY - startY) / (endX - startX);

        mNormalVector = new NormalVector(-1 * m, 1);
        mOffset = startY - m * startX;
    }

    public Line(NormalVector vector, double offset) {
        mNormalVector = vector;
        mOffset = offset;
    }

    public double getIncrease() {
        return mNormalVector.getW1() * -1 / mNormalVector.getW2();
    }

    public double getOffset() {
        return mOffset;
    }

    public NormalVector getNormalVector() {
        return mNormalVector;
    }

    public double getY(double x) {
        return getIncrease() * x + getOffset();
    }

    public double getX(double y) {
        return (y - getOffset()) / getIncrease();
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Line) {
            Line line = (Line) o;
            int offset1 = (int) (line.mOffset * 1000);
            int offset2 = (int) (mOffset * 1000);

            return line.mNormalVector.equals(mNormalVector) && offset1 == offset2;
        }

        return super.equals(o);
    }

    @Override
    public Line clone() {
        return new Line(new NormalVector(mNormalVector.getW1(), mNormalVector.getW2()), mOffset);
    }

    public static class Builder {

        private float mStartX;
        private float mEndX;
        private float mStartY;
        private float mEndY;

        public Builder(float startX, float startY, float endX, float endY) {
            mStartX = startX;
            mEndX = endX;
            mStartY = startY;
            mEndY = endY;
        }

        public Builder updateEndPoint(float x, float y) {
            mEndX = x;
            mEndY = y;
            return this;
        }

        public double getLength() {
            if (mStartX == mEndX && mStartY == mEndY) {
                return 0;
            }

            return Math.sqrt(Math.pow(mStartX - mEndX, 2) + Math.pow(mStartY - mEndY, 2));
        }

        public float getStartX() {
            return mStartX;
        }

        public void setStartX(float startX) {
            mStartX = startX;
        }

        public float getEndX() {
            return mEndX;
        }

        public void setEndX(float endX) {
            mEndX = endX;
        }

        public float getStartY() {
            return mStartY;
        }

        public void setStartY(float startY) {
            mStartY = startY;
        }

        public float getEndY() {
            return mEndY;
        }

        public void setEndY(float endY) {
            mEndY = endY;
        }

        public Line build() {
            return new Line(mStartX, mStartY, mEndX, mEndY);
        }

        public void buildAnimate(final CartesianCoordinateSystem view) {
            if (getEndX() < getStartX()) {
                flipPoints();
            }

            float increase = (mEndY - mStartY) / (mEndX - mStartX);
            float offset = mStartY - increase * mStartX;

            PropertyValuesHolder holderStartX = PropertyValuesHolder.ofFloat("startX", getStartX(), 0);
            PropertyValuesHolder holderEndX = PropertyValuesHolder.ofFloat("endX", getEndX(), 1);
            PropertyValuesHolder holderStartY = PropertyValuesHolder.ofFloat("startY", getStartY(), offset);
            PropertyValuesHolder holderEndY = PropertyValuesHolder.ofFloat("endY", getEndY(), increase * 1 + offset);

            ObjectAnimator objectAnimator = ObjectAnimator.ofPropertyValuesHolder(this, holderStartX, holderEndX, holderStartY, holderEndY);
            objectAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
            objectAnimator.setDuration(1000l);
            objectAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    view.invalidate();
                }
            });
            objectAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    view.setLine(Builder.this.build(), false);
                }
            });
            objectAnimator.start();
        }

        private void flipPoints() {
            float val = getStartX();
            setStartX(getEndX());
            setEndX(val);

            val = getStartY();
            setStartY(getEndY());
            setEndY(val);
        }
    }
}
