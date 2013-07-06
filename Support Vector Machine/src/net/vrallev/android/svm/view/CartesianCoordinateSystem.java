package net.vrallev.android.svm.view;

import android.animation.*;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import com.google.gson.Gson;
import de.greenrobot.event.EventBus;
import net.vrallev.android.base.util.L;
import net.vrallev.android.svm.MenuState;
import net.vrallev.android.svm.OptimizerCalculator;
import net.vrallev.android.svm.model.DirtyLineEvent;
import net.vrallev.android.svm.model.LabeledPoint;
import net.vrallev.android.svm.model.Line;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The view used for drawing the coordinate system.
 *
 * @author Ralf Wondratschek
 */
public class CartesianCoordinateSystem extends View {

    @SuppressWarnings("unused")
    private static final L L = new L(CartesianCoordinateSystem.class);

    // TODO: fix hardcoded values
    private static final int STROKE_WIDTH = 4;
    private static final int CIRCLE_RADIUS = 20;

    private Paint mPaint;
    private Rect mTextBounds;

    private int mHeight;
    private int mWidth;

    private State mState;
    private Line.Builder mLineBuilder;

    private MenuState mMenuState;

    private ObjectAnimator mObjectAnimator;

    private boolean mIgnoreTouch;

    @SuppressWarnings("UnusedDeclaration")
    public CartesianCoordinateSystem(Context context) {
        super(context);
        construtor();
    }

    @SuppressWarnings("UnusedDeclaration")
    public CartesianCoordinateSystem(Context context, AttributeSet attrs) {
        super(context, attrs);
        construtor();
    }

    @SuppressWarnings("UnusedDeclaration")
    public CartesianCoordinateSystem(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        construtor();
    }

    private void construtor() {
        mPaint = new Paint();
        mPaint.setStyle(Style.FILL_AND_STROKE);
        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.WHITE);

        mPaint.setStrokeWidth(STROKE_WIDTH);

        mState = new State();

        mTextBounds = new Rect();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        mWidth = w;
        mHeight = h;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawLine(0, 0, 0, mHeight, mPaint);
        canvas.drawLine(0, mHeight, mWidth, mHeight, mPaint);

        float strokeWidth = mPaint.getStrokeWidth();

        int textLineWidth = 10;
        int textPadding = textLineWidth + 6;

        canvas.drawLine(0, 0, textLineWidth, 0, mPaint);
        canvas.drawLine(0, mHeight / 4 * 3, textLineWidth, mHeight / 4 * 3, mPaint);
        canvas.drawLine(0, mHeight / 2, textLineWidth, mHeight / 2, mPaint);
        canvas.drawLine(0, mHeight / 4, textLineWidth, mHeight / 4, mPaint);


        canvas.drawLine(mWidth, mHeight, mWidth, mHeight - textLineWidth, mPaint);
        canvas.drawLine(mWidth / 4, mHeight, mWidth / 4, mHeight - textLineWidth, mPaint);
        canvas.drawLine(mWidth / 2, mHeight, mWidth / 2, mHeight - textLineWidth, mPaint);
        canvas.drawLine(mWidth / 4 * 3, mHeight, mWidth / 4 * 3, mHeight - textLineWidth, mPaint);

        mPaint.setTextSize(24);
        mPaint.setStrokeWidth(1);

        String text = "0.25";
        mPaint.getTextBounds(text, 0, text.length(), mTextBounds);
        canvas.drawText(text, textPadding, mHeight / 4 * 3 + mTextBounds.height() / 2, mPaint);
        canvas.drawText(text, mWidth / 4 - mTextBounds.width() / 2, mHeight - textPadding, mPaint);

        text = "0.5";
        mPaint.getTextBounds(text, 0, text.length(), mTextBounds);
        canvas.drawText(text, textPadding, mHeight / 2 + mTextBounds.height() / 2, mPaint);
        canvas.drawText(text, mWidth / 2 - mTextBounds.width() / 2, mHeight - textPadding, mPaint);

        text = "0.75";
        mPaint.getTextBounds(text, 0, text.length(), mTextBounds);
        canvas.drawText(text, textPadding, mHeight / 4 + mTextBounds.height() / 2, mPaint);
        canvas.drawText(text, mWidth / 4 * 3 - mTextBounds.width() / 2, mHeight - textPadding, mPaint);

        text = "1.0";
        mPaint.getTextBounds(text, 0, text.length(), mTextBounds);
        canvas.drawText(text, textPadding, mTextBounds.height(), mPaint);
        canvas.drawText(text, mWidth - mTextBounds.width() - 3, mHeight - textPadding, mPaint);

        for (LabeledPoint p : mState.mPoints) {
            mPaint.setColor(p.getColorClass().getColor());
            canvas.drawCircle((float) p.getX1() * mWidth, (float) (1 - p.getX2()) * mHeight, CIRCLE_RADIUS, mPaint);
        }

        Line lineText = mState.mLine;
        if (lineText == null && mLineBuilder != null) {
            lineText = mLineBuilder.build();
        }

        if (lineText != null) {
            mPaint.setColor(Color.WHITE);
            text = "y = " + Math.round(lineText.getIncrease() * 100) / 100D + " * x + " + Math.round(lineText.getOffset() * 100) / 100D;
            mPaint.getTextBounds(text, 0, text.length(), mTextBounds);
            canvas.drawText(text, mWidth - textPadding - mTextBounds.width(), textPadding + mTextBounds.height(), mPaint);
        }

        mPaint.setStrokeWidth(strokeWidth);
        mPaint.setColor(Color.WHITE);

        if (mLineBuilder != null) {
            mPaint.setColor(Color.WHITE);
            canvas.drawLine(mLineBuilder.getStartX() * mWidth, (1 - mLineBuilder.getStartY()) * mHeight, mLineBuilder.getEndX() * mWidth, (1 - mLineBuilder.getEndY()) * mHeight, mPaint);

        } else if (mState.mLine != null) {
            mPaint.setColor(Color.WHITE);
            canvas.drawLine(0, (float) (1 - mState.mLine.getY(0)) * mHeight, mWidth, (float) (1 - mState.mLine.getY(1)) * mHeight, mPaint);
        }
    }

    private LabeledPoint mPendingPoint;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            mIgnoreTouch = OptimizerCalculator.getInstance().isCalculating();
        }

        if (mIgnoreTouch) {
            return true;
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (MenuState.STATE_LINE.equals(mMenuState)) {
                    if (mObjectAnimator != null) {
                        mObjectAnimator.cancel();
                    }
                    mState.mLine = null;
                    mLineBuilder = new Line.Builder(event.getX() / mWidth, 1 - event.getY() / mHeight, event.getX() / mWidth, 1 - event.getY() / mHeight);
                    invalidate();

                } else {
                    LabeledPoint onClickPoint = getPointOnClick(event.getX(), event.getY());
                    if (onClickPoint != null) {
                        mState.removePoint(onClickPoint);
                    } else {
                        mPendingPoint = LabeledPoint.getInstance(event.getX() / mWidth, 1 - event.getY() / mHeight, mMenuState.getColorClass());
                        mState.addPoint(mPendingPoint);
                    }
                }
                return true;

            case MotionEvent.ACTION_MOVE:
                if (MenuState.STATE_LINE.equals(mMenuState) && mLineBuilder != null) {
                    mLineBuilder.updateEndPoint(event.getX() / mWidth, 1 - event.getY() / mHeight);

                } else if (mPendingPoint != null) {
                    mPendingPoint.setX1(event.getX() / mWidth);
                    mPendingPoint.setX2(1 - event.getY() / mHeight);
                }
                invalidate();
                return true;

            case MotionEvent.ACTION_UP:
                if (MenuState.STATE_LINE.equals(mMenuState) && mLineBuilder != null) {

                    if (mLineBuilder.getLength() < 1 / 20D) {
                        mState.setLine(null);
                        mLineBuilder = null;

                    } else {
                        mLineBuilder.buildAnimate(this);
                    }
                    invalidate();
                }

                mPendingPoint = null;
                return true;
        }

        return super.onTouchEvent(event);
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putParcelable("instanceState", super.onSaveInstanceState());
        bundle.putString("state", new Gson().toJson(mState));

        return bundle;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {

        if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;
            String json = bundle.getString("state", null);
            if (json != null) {
                State state1 = new Gson().fromJson(json, State.class);
                mState = new State(state1.getPoints(), state1.getLine());
                state1.releasePoints();
            }

            super.onRestoreInstanceState(bundle.getParcelable("instanceState"));
            return;
        }

        super.onRestoreInstanceState(state);
    }

    public void setMenuState(MenuState menuState) {
        mMenuState = menuState;
    }

    public void setLine(Line line, boolean animate) {
        if (animate && mState.mLine != null) {
            animateLineToPosition(mState.mLine, line);
            mState.mLine = line;

        } else {
            mState.setLine(line);
            mLineBuilder = null;
        }
    }

    public Line getLine() {
        if (mState.mLine != null) {
            return mState.mLine;
        } else if (mLineBuilder != null) {
            return mLineBuilder.build();
        }
        return null;
    }

    public List<LabeledPoint> getPoints() {
        return mState.mPoints;
    }

    public void addPoint(LabeledPoint point) {
        mState.addPoint(point);
    }

    public void clearPoints() {
        mState.clearPoints();
    }

    public void removePoint(LabeledPoint point) {
        mState.removePoint(point);
    }

    public State getState() {
        return new State(mState.mPoints, mState.mLine);
    }

    private LabeledPoint getPointOnClick(float x, float y) {
        for (LabeledPoint p : mState.mPoints) {
            if (Math.pow(x - p.getX1() * mWidth, 2) + Math.pow(y - (1 - p.getX2()) * mHeight, 2) <= Math.pow(CIRCLE_RADIUS * 2, 2)) {
                return p;
            }
        }

        return null;
    }

    private void animateLineToPosition(Line start, Line end) {
        mLineBuilder = new Line.Builder(0, (float) start.getY(0), 1, (float) start.getY(1));

        PropertyValuesHolder holderStartY = PropertyValuesHolder.ofFloat("startY", mLineBuilder.getStartY(), (float) end.getY(0));
        PropertyValuesHolder holderEndY = PropertyValuesHolder.ofFloat("endY", mLineBuilder.getEndY(), (float) end.getY(1));

        L.d("Values ", mLineBuilder.getStartY(), " ", end.getY(0), " ", mLineBuilder.getEndY(), " ", end.getY(1));

        mObjectAnimator = ObjectAnimator.ofPropertyValuesHolder(mLineBuilder, /*holderStartX, holderEndX,*/ holderStartY, holderEndY);
        mObjectAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        mObjectAnimator.setDuration(1000l);
        mObjectAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                invalidate();
            }
        });
        mObjectAnimator.addListener(new AnimatorListenerAdapter() {
            private boolean mCancelled = false;

            @Override
            public void onAnimationCancel(Animator animation) {
                mCancelled = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (!mCancelled && mLineBuilder != null) {
                    setLine(mLineBuilder.build(), false);
                }
                mObjectAnimator = null;
            }
        });
        mObjectAnimator.start();
    }

    public class State {

        private List<LabeledPoint> mPoints;
        private Line mLine;

        private State() {
            this(Collections.<LabeledPoint>emptyList(), null);
        }

        private State(List<LabeledPoint> points, Line line) {
            mPoints = new ArrayList<LabeledPoint>(points.size());
            for (LabeledPoint p : points) {
                mPoints.add(p.clone());
            }

            if (line != null) {
                mLine = line.clone();
            }
        }

        public Line getLine() {
            return mLine;
        }

        private void setLine(Line line) {
            mLine = line;
            invalidate();
            EventBus.getDefault().postSticky(DirtyLineEvent.INSTANCE);
        }

        public List<LabeledPoint> getPoints() {
            return mPoints;
        }

        private void addPoint(LabeledPoint point) {
            mPoints.add(point);
            invalidate();
            EventBus.getDefault().postSticky(DirtyLineEvent.INSTANCE);
        }

        private void removePoint(LabeledPoint point) {
            mPoints.remove(point);
            point.release();
            invalidate();
            EventBus.getDefault().postSticky(DirtyLineEvent.INSTANCE);
        }

        private void clearPoints() {
            releasePoints();
            mPoints.clear();
            invalidate();
            EventBus.getDefault().postSticky(DirtyLineEvent.INSTANCE);
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof State) {
                State state = (State) o;
                if (!LabeledPoint.listEqual(state.mPoints, mPoints)) {
                    return false;
                }
                if (mLine == null && state.mLine == null) {
                    return true;
                }
                if (mLine != null && state.mLine != null) {
                    return state.mLine.equals(mLine);
                }

                return false;
            }

            return super.equals(o);
        }

        public void releasePoints() {
            for (LabeledPoint p : mPoints) {
                p.release();
            }
        }
    }
}
