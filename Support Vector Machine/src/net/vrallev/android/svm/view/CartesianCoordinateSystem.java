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
import net.vrallev.android.svm.LabeledPoint;
import net.vrallev.android.svm.Line;

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
    private LabeledPoint.ColorClass mColorClass;

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

        int drawText = 0;
        for (float i = mHeight - 20; i >= 0; i -= 20) {
            drawText++;

            canvas.drawLine(0, i, 10, i, mPaint);

            if (drawText % 5 == 0) {
                mPaint.setStrokeWidth(1);
                canvas.drawText(String.valueOf(drawText * 20), 12, i + 4, mPaint);
                mPaint.setStrokeWidth(strokeWidth);
            }
        }

        drawText = 0;
        for (float i = 20; i < mWidth; i += 20) {
            drawText++;

            canvas.drawLine(i, mHeight, i, mHeight - 10, mPaint);

            if (drawText % 5 == 0) {
                mPaint.setStrokeWidth(1);
                canvas.drawText(String.valueOf(drawText * 20), i - 10, mHeight - 12, mPaint);
                mPaint.setStrokeWidth(strokeWidth);
            }
        }

        for (LabeledPoint p : mPoints) {
            mPaint.setColor(p.getColorClass().getColor());
            canvas.drawCircle(p.getX(), p.getY(), CIRCLE_RADIUS, mPaint);
        }

        if (mLine != null) {
            mPaint.setColor(LabeledPoint.ColorClass.LINE.getColor());
            canvas.drawLine(mLine.getStartX(), mLine.getStartY(), mLine.getEndX(), mLine.getEndY(), mPaint);
        }

        mPaint.setStrokeWidth(strokeWidth);
        mPaint.setColor(Color.WHITE);
    }

    private LabeledPoint mPendingPoint;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (LabeledPoint.ColorClass.LINE.equals(mColorClass)) {
                    mLine = new Line(event.getX(), event.getY(), event.getX(), event.getY());

                } else {
                    LabeledPoint onClickPoint = getPointOnClick(event.getX(), event.getY());
                    if (onClickPoint != null) {
                        mPoints.remove(onClickPoint);
                    } else {
                        mPendingPoint = new LabeledPoint(event.getX(), event.getY(), mColorClass);
                        mPoints.add(mPendingPoint);
                    }
                }
                invalidate();
                return true;

            case MotionEvent.ACTION_MOVE:
                if (LabeledPoint.ColorClass.LINE.equals(mColorClass) && mLine != null) {
                    mLine.setEndX(event.getX()).setEndY(event.getY());

                } else if (mPendingPoint != null) {
                    mPendingPoint.setX(event.getX());
                    mPendingPoint.setY(event.getY());
                }
                invalidate();
                return true;

            case MotionEvent.ACTION_UP:
                if (LabeledPoint.ColorClass.LINE.equals(mColorClass) && mLine != null) {

                    if (Math.sqrt(Math.pow(mLine.getStartX() - mLine.getEndX(), 2) + Math.pow(mLine.getStartY() - mLine.getEndY(), 2)) < 20) {
                        mLine = null;
                    } else {
                        mLine.stretchTo(0, mWidth, this);

//                        GradientDescent gradientDescent = new GradientDescent(mLine, mPoints, mHeight);
//                        mLine = gradientDescent.calc(5);
                        invalidate();
                        L.d("REEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEE");
                    }
                }
                mPendingPoint = null;
                return true;
        }

        return super.onTouchEvent(event);
    }

    public void setColorClass(LabeledPoint.ColorClass clazz) {
        mColorClass = clazz;
    }

    private LabeledPoint getPointOnClick(float x, float y) {
        for (LabeledPoint p : mPoints) {
            if (Math.pow(x - p.getX(), 2) + Math.pow(y - p.getY(), 2) <= Math.pow(CIRCLE_RADIUS * 2, 2)) {
                return p;
            }
        }

        return null;
    }
}
