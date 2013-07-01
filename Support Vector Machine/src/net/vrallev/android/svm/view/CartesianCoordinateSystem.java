package net.vrallev.android.svm.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import net.vrallev.android.base.util.L;
import net.vrallev.android.svm.MenuState;
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

    private int mHeight;
    private int mWidth;

    private List<LabeledPoint> mPoints;

    private Line mLine;
    private Line.Builder mLineBuilder;

    private MenuState mMenuState;

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

        int max = Math.max(mWidth, mHeight);

        canvas.drawLine(0, mHeight - max, 10, mHeight - max, mPaint);
        canvas.drawLine(0, mHeight - max / 4, 10, mHeight - max / 4, mPaint);
        canvas.drawLine(0, mHeight - max / 2, 10, mHeight - max / 2, mPaint);
        canvas.drawLine(0, mHeight - max / 4 * 3, 10, mHeight - max / 4 * 3, mPaint);

        canvas.drawLine(max, mHeight, max, mHeight - 10, mPaint);
        canvas.drawLine(max / 4, mHeight, max / 4, mHeight - 10, mPaint);
        canvas.drawLine(max / 2, mHeight, max / 2, mHeight - 10, mPaint);
        canvas.drawLine(max / 4 * 3, mHeight, max / 4 * 3, mHeight - 10, mPaint);

        mPaint.setTextSize(24);

        mPaint.setStrokeWidth(1);
        canvas.drawText("0.25", 16, mHeight - max / 4 + 8, mPaint);
        canvas.drawText("0.5", 16, mHeight - max / 2 + 8, mPaint);
        canvas.drawText("0.75", 16, mHeight - max / 4 * 3 + 8, mPaint);
        canvas.drawText("1.0", 16, mHeight - max + 24, mPaint);

        canvas.drawText("0.25", max / 4 - 14, mHeight - 16, mPaint);
        canvas.drawText("0.5", max / 2 - 14, mHeight - 16, mPaint);
        canvas.drawText("0.75", max / 4 * 3 - 14, mHeight - 16, mPaint);
        canvas.drawText("1.0", max - 14, mHeight - 16, mPaint);

        for (LabeledPoint p : mPoints) {
            mPaint.setColor(p.getColorClass().getColor());
            canvas.drawCircle((float) p.getX() * max, (float) (1 - p.getY()) * max, CIRCLE_RADIUS, mPaint);
        }

        mPaint.setStrokeWidth(strokeWidth);
        mPaint.setColor(Color.WHITE);

        if (mLine != null) {
            mPaint.setColor(Color.WHITE);
            canvas.drawLine(0, (float) (1 - mLine.getY(0)) * max, max, (float) (1 - mLine.getY(1)) * max, mPaint);

        } else if (mLineBuilder != null) {
            mPaint.setColor(Color.WHITE);
            canvas.drawLine((float) mLineBuilder.getStartX() * max, (float) (1 - mLineBuilder.getStartY()) * max, (float) mLineBuilder.getEndX() * max, (float) (1 - mLineBuilder.getEndY()) * max, mPaint);
        }
    }

    private LabeledPoint mPendingPoint;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float max = Math.max(mWidth, mHeight);

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (MenuState.STATE_LINE.equals(mMenuState)) {
                    mLine = null;
                    mLineBuilder = new Line.Builder(event.getX() / max, 1 - event.getY() / max, event.getX() / max, 1 - event.getY() / max);

                } else {
                    LabeledPoint onClickPoint = getPointOnClick(event.getX(), event.getY());
                    if (onClickPoint != null) {
                        mPoints.remove(onClickPoint);
                    } else {
                        mPendingPoint = new LabeledPoint(event.getX() / max, 1 - event.getY() / max, mMenuState.getColorClass());
                        mPoints.add(mPendingPoint);
                    }
                }
                invalidate();
                return true;

            case MotionEvent.ACTION_MOVE:
                if (MenuState.STATE_LINE.equals(mMenuState) && mLineBuilder != null) {
                    mLineBuilder.updateEndPoint(event.getX() / max, 1 - event.getY() / max);

                } else if (mPendingPoint != null) {
                    mPendingPoint.setX(event.getX() / max);
                    mPendingPoint.setY(1 - event.getY() / max);
                }
                invalidate();
                return true;

            case MotionEvent.ACTION_UP:
                if (MenuState.STATE_LINE.equals(mMenuState) && mLineBuilder != null) {

                    if (mLineBuilder.getLength() < 20 / max) {
                        mLine = null;
                        mLineBuilder = null;

                    } else {
                        mLineBuilder.buildAnimate(this);
                        invalidate();
                    }
                }
                mPendingPoint = null;
                return true;
        }

        return super.onTouchEvent(event);
    }

    public void setMenuState(MenuState menuState) {
        mMenuState = menuState;
    }

    public void setLine(Line line) {
        mLine = line;
        mLineBuilder = null;
        invalidate();
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
    }

    public void clearPoints() {
        mPoints.clear();
        invalidate();
    }

    private LabeledPoint getPointOnClick(float x, float y) {
        double max = Math.max(mWidth, mHeight);

        for (LabeledPoint p : mPoints) {
            if (Math.pow(x - p.getX() * max, 2) + Math.pow(y - (1 - p.getY()) * max, 2) <= Math.pow(CIRCLE_RADIUS * 2, 2)) {
                return p;
            }
        }

        return null;
    }
}
