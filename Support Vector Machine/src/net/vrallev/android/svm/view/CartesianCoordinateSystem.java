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
import com.google.gson.reflect.TypeToken;
import de.greenrobot.event.EventBus;
import net.vrallev.android.base.util.L;
import net.vrallev.android.svm.MenuState;
import net.vrallev.android.svm.gradient.DirtyLineEvent;
import net.vrallev.android.svm.model.LabeledPoint;
import net.vrallev.android.svm.model.Line;

import java.util.ArrayList;
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

    private List<LabeledPoint> mPoints;

    private Line mLine;
    private Line.Builder mLineBuilder;

    private MenuState mMenuState;

    private ObjectAnimator mObjectAnimator;

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

        mPoints = new ArrayList<LabeledPoint>();

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

        canvas.drawLine(0, 0 , textLineWidth, 0, mPaint);
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

        for (LabeledPoint p : mPoints) {
            mPaint.setColor(p.getColorClass().getColor());
            canvas.drawCircle((float) p.getX1() * mWidth, (float) (1 - p.getX2()) * mHeight, CIRCLE_RADIUS, mPaint);
        }

        Line lineText = mLine;
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

        if (mLine != null) {
            mPaint.setColor(Color.WHITE);
            canvas.drawLine(0, (float) (1 - mLine.getY(0)) * mHeight, mWidth, (float) (1 - mLine.getY(1)) * mHeight, mPaint);

        } else if (mLineBuilder != null) {
            mPaint.setColor(Color.WHITE);
            canvas.drawLine(mLineBuilder.getStartX() * mWidth, (1 - mLineBuilder.getStartY()) * mHeight, mLineBuilder.getEndX() * mWidth, (1 - mLineBuilder.getEndY()) * mHeight, mPaint);
        }
    }

    private LabeledPoint mPendingPoint;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (MenuState.STATE_LINE.equals(mMenuState)) {
                    if (mObjectAnimator != null) {
                        mObjectAnimator.cancel();
                    }
                    mLine = null;
                    mLineBuilder = new Line.Builder(event.getX() / mWidth, 1 - event.getY() / mHeight, event.getX() / mWidth, 1 - event.getY() / mHeight);
                    EventBus.getDefault().postSticky(new DirtyLineEvent());

                } else {
                    LabeledPoint onClickPoint = getPointOnClick(event.getX(), event.getY());
                    if (onClickPoint != null) {
                        mPoints.remove(onClickPoint);
                    } else {
                        mPendingPoint = new LabeledPoint(event.getX() / mWidth, 1 - event.getY() / mHeight, mMenuState.getColorClass());
                        mPoints.add(mPendingPoint);
                    }
                    EventBus.getDefault().postSticky(new DirtyLineEvent());
                }
                invalidate();
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
                        mLine = null;
                        mLineBuilder = null;

                    } else {
                        mLineBuilder.buildAnimate(this);
                    }
                    invalidate();
                    EventBus.getDefault().postSticky(new DirtyLineEvent());
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

        Gson gson = new Gson();
        if (!mPoints.isEmpty()) {
            bundle.putString("points", gson.toJson(mPoints));
        }
        if (mLine != null) {
            bundle.putString("line", gson.toJson(mLine));
        }

        return bundle;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {

        if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;
            Gson gson = new Gson();

            String points = bundle.getString("points", null);
            if (points != null) {
                mPoints = gson.fromJson(points, new TypeToken<ArrayList<LabeledPoint>>() {
                }.getType());
            }
            String line = bundle.getString("line", null);
            if (line != null) {
                mLine = gson.fromJson(line, Line.class);
            }

            super.onRestoreInstanceState(bundle.getParcelable("instanceState"));
            return;
        }

        super.onRestoreInstanceState(state);
    }

    public void setMenuState(MenuState menuState) {
        mMenuState = menuState;
    }

    public void setLine(Line line) {
        if (mLine != null) {
            animateLineToPosition(mLine, line);
            mLine = null;

        } else {
            mLine = line;
            mLineBuilder = null;
            invalidate();
        }
        EventBus.getDefault().postSticky(new DirtyLineEvent());
    }

    public Line getLine() {
        if (mLine != null) {
            return mLine;
        } else if (mLineBuilder != null) {
            return mLineBuilder.build();
        }
        return null;
    }

    public List<LabeledPoint> getPoints() {
        return mPoints;
    }

    public void addPoint(LabeledPoint point) {
        mPoints.add(point);
        invalidate();
        EventBus.getDefault().postSticky(new DirtyLineEvent());
    }

    public void clearPoints() {
        mPoints.clear();
        invalidate();
        EventBus.getDefault().postSticky(new DirtyLineEvent());
    }

    public State getState() {
        Line l = mLine;
        if (l == null && mLineBuilder != null) {
            l = mLineBuilder.build();
        }
        return new State(mPoints, l);
    }

    private LabeledPoint getPointOnClick(float x, float y) {
        for (LabeledPoint p : mPoints) {
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
                    setLine(mLineBuilder.build());
                }
                mObjectAnimator = null;
            }
        });
        mObjectAnimator.start();
    }

    public static class State {

        private List<LabeledPoint> mPoints;
        private Line mLine;

        public State(List<LabeledPoint> points, Line line) {
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

        public void setLine(Line line) {
            mLine = line;
        }

        public List<LabeledPoint> getPoints() {
            return mPoints;
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof State) {
                State state = (State) o;
                for (LabeledPoint p : mPoints) {
                    if (!state.mPoints.contains(p)) {
                        return false;
                    }
                }
                for (LabeledPoint p : state.mPoints) {
                    if (!mPoints.contains(p)) {
                        return false;
                    }
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
    }
}
