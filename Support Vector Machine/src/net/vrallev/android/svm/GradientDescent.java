package net.vrallev.android.svm;

import java.util.List;

/**
 * @author Ralf Wondratschek
 */
public class GradientDescent {

    private static final float STEP_SIZE = 1.00f;
    private static final float C = 10.0f;

    private Line mLine;
    private LabeledPoint[] mPoints;

    private Argument[] mArguments;

    public GradientDescent(Line line, List<LabeledPoint> points) {
        mLine = line.clone();
        mPoints = new LabeledPoint[points.size()];
        for (int i = 0; i < mPoints.length; i++) {
            mPoints[i] = points.get(i).clone();
        }
    }

    public GradientDescent(Line line, List<LabeledPoint> points, int viewHeight) {
        mLine = line.clone();
        mLine.setEndY(viewHeight - mLine.getEndY());
        mLine.setStartY(viewHeight - mLine.getStartY());

        mPoints = new LabeledPoint[points.size()];
        for (int i = 0; i < mPoints.length; i++) {
            mPoints[i] = points.get(i).clone();
            mPoints[i].setY(viewHeight - mPoints[i].getY());
        }
    }

    public Line calc(int iterations) {
        mArguments = new Argument[iterations + 1];
        mArguments[0] = new Argument(mLine.getNormalVector()[0], mLine.getNormalVector()[1], mLine.getOffset());

        for (int i = 1; i < mArguments.length; i++) {
            mArguments[i] = mArguments[i - 1].minus(derivate(mArguments[i - 1]).multiply(STEP_SIZE));
//            float factor = mArguments[i].w1;
//            mArguments[i].w1 /= factor;
//            mArguments[i].w2 /= factor;
//            mArguments[i].b /= factor;
//            float norm = (float) Math.sqrt(Math.pow(mArguments[i].w1, 2) + Math.pow(mArguments[i].w2, 2));
//            mArguments[i].w1 /= norm;
//            mArguments[i].w2 /= norm;
//            mArguments[i].b /= norm;
        }

//        float factor = mArguments[mArguments.length - 1].w1;
//        mArguments[mArguments.length - 1].w1 /= factor;
//        mArguments[mArguments.length - 1].w2 /= factor;
//        mArguments[mArguments.length - 1].b /= factor;

        return new Line(mLine.getStartX(), mLine.getEndX(), mArguments[mArguments.length - 1]);
    }

    private Argument derivate(Argument arg) {
        return new Argument(derivateW1(arg), derivateW2(arg), derivateB(arg));
    }

    private float derivateW1(Argument arg) {
        float sum = 0;

        for (int i = 0; i < mPoints.length; i++) {
            if (!useNullFunction(mPoints[i], arg)) {
                sum += 2 * (1 - mPoints[i].getClassValue() * (arg.w1 * mPoints[i].getX() + arg.w2 * mPoints[i].getY() + arg.b)) * (-1 * mPoints[i].getX() * mPoints[i].getClassValue());
            }
         }

        return arg.w1 + C * sum;
    }

    private float derivateW2(Argument arg) {
        float sum = 0;

        for (int i = 0; i < mPoints.length; i++) {
            if (!useNullFunction(mPoints[i], arg)) {
                sum += 2 * (1 - mPoints[i].getClassValue() * (arg.w1 * mPoints[i].getX() + arg.w2 * mPoints[i].getY() + arg.b)) * (-1 * mPoints[i].getY() * mPoints[i].getClassValue());
            }
         }

        return arg.w2 + C * sum;
    }

    private float derivateB(Argument arg) {
        float sum = 0;

        for (int i = 0; i < mPoints.length; i++) {
            if (!useNullFunction(mPoints[i], arg)) {
                sum += 2 * (1 - mPoints[i].getClassValue() * (arg.w1 * mPoints[i].getX() + arg.w2 * mPoints[i].getY() + arg.b)) * (-1 * mPoints[i].getClassValue());
            }
         }

        return C * sum;
    }

    private boolean useNullFunction(LabeledPoint point, Argument arg) {
        float res = 1 - point.getClassValue() * (arg.w1 * point.getX() + arg.w2 * point.getY() + arg.b);
        return res <= 0;
    }

    public static class Argument {

        public float w1;
        public float w2;
        public float b;

        public Argument(float w1, float w2, float b) {
            this.w1 = w1;
            this.w2 = w2;
            this.b = b;
        }

        public Argument minus(Argument arg) {
            return new Argument(this.w1 - arg.w1, this.w2 - arg.w2, this.b - arg.b);
        }

        public Argument multiply(float arg) {
            return new Argument(this.w1 * arg, this.w2 * arg, this.b * arg);
        }
    }
}
