package net.vrallev.android.svm.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;
import net.vrallev.android.base.util.L;
import net.vrallev.android.svm.LabeledPoint;
import net.vrallev.android.svm.Line;
import net.vrallev.android.svm.gradient.Gradient;

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
            mPaint.setColor(LabeledPoint.ColorClass.LINE.getColor());
            canvas.drawLine((float) mLine.getStartX() * max, (float) (1 - mLine.getStartY()) * max, (float) mLine.getEndX() * max, (float) (1 - mLine.getEndY()) * max, mPaint);
        }
    }

    private LabeledPoint mPendingPoint;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        double max = Math.max(mWidth, mHeight);

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (LabeledPoint.ColorClass.LINE.equals(mColorClass)) {
                    mLine = new Line(event.getX() / max, 1 - event.getY() / max, event.getX() / max, 1 - event.getY() / max);

                } else {
                    LabeledPoint onClickPoint = getPointOnClick(event.getX(), event.getY());
                    if (onClickPoint != null) {
                        mPoints.remove(onClickPoint);
                    } else {
                        mPendingPoint = new LabeledPoint(event.getX() / max, 1 - event.getY() / max, mColorClass);
                        mPoints.add(mPendingPoint);
                    }
                }
                invalidate();
                return true;

            case MotionEvent.ACTION_MOVE:
                if (LabeledPoint.ColorClass.LINE.equals(mColorClass) && mLine != null) {
                    mLine.setEndX(event.getX() / max).setEndY(1 - event.getY() / max);

                } else if (mPendingPoint != null) {
                    mPendingPoint.setX(event.getX() / max);
                    mPendingPoint.setY(1 - event.getY() / max);
                }
                invalidate();
                return true;

            case MotionEvent.ACTION_UP:
                if (LabeledPoint.ColorClass.LINE.equals(mColorClass) && mLine != null) {

                    if (Math.sqrt(Math.pow(mLine.getStartX() - mLine.getEndX(), 2) + Math.pow(mLine.getStartY() - mLine.getEndY(), 2)) < 20 / max) {
                        mLine = null;
                    } else {
                        mLine.stretchTo(0, 1, null);
                        invalidate();

//                        GradientDescent gradientDescent = new GradientDescent(mLine, mPoints, mHeight);
//                        mLine = gradientDescent.calc(5);
//                        invalidate();
//                        L.d("REEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEE");
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
        double max = Math.max(mWidth, mHeight);

        for (LabeledPoint p : mPoints) {
            if (Math.pow(x - p.getX() * max, 2) + Math.pow(y - (1 - p.getY()) * max, 2) <= Math.pow(CIRCLE_RADIUS * 2, 2)) {
                return p;
            }
        }

        return null;
    }

    public void test() {
        Gradient gradient = new Gradient(mLine, mPoints);
        Line line = gradient.run(10000);
        mLine = line;

        Toast.makeText(getContext(), "y = " + Math.round(line.getIncrease() * 100) / 100D + " * x + " + Math.round(line.getOffset() * 100) / 100D, Toast.LENGTH_SHORT).show();

        invalidate();
    }
}
