package net.vrallev.android.svm.gradient;

import net.vrallev.android.svm.LabeledPoint;
import net.vrallev.android.svm.Line;

import java.util.List;

/**
 * @author Ralf Wondratschek
 */
public class Gradient {

    public static final double C = 10.0;

    private Line mLine;
    private LabeledPoint[] mPoints;

    public Gradient(Line line, List<LabeledPoint> points) {
        mLine = line.clone();
        mPoints = new LabeledPoint[points.size()];
        for (int i = 0; i < mPoints.length; i++) {
            mPoints[i] = points.get(i).clone();
        }

//        normalize();
    }

    private void normalize() {
        double maxX = 0;
        double maxY = 0;

        for (LabeledPoint p : mPoints) {
            maxX = Math.max(maxX, p.getX());
            maxY = Math.max(maxY, p.getY());
        }

        for (LabeledPoint p : mPoints) {
            p.setX(p.getX() / maxX);
            p.setY(p.getY() / maxY);
        }

        double y = mLine.getIncrease() + mLine.getOffset();
        mLine = new Line(0, mLine.getOffset() / maxY, 1 / maxX, y / maxY);
    }



    public Line run(int iterations) {
        Argument[] arguments = new Argument[iterations + 1];
        arguments[0] = new Argument(mLine.getNormalVectorNew(), mLine.getOffset());

        double stepSize = 0.00001;

        for (int i = 1; i < arguments.length; i++) {
            Argument derivate = derivate(arguments[i - 1]).multipy(stepSize);

//            derivate = stop(derivate, stepSize);
//            if (derivate == null) {
//                return arguments[i - 1].toLine();
//            }

            arguments[i] = arguments[i - 1].minus(derivate);
//            arguments[i] = arguments[i - 1].next(stepSize, derivate);
        }

        return arguments[arguments.length - 1].toLine();
    }




    private Argument derivate(Argument arg) {
        double argOffset = arg.getOffset();
        NormalVector argVector = arg.getNormalVector();

        NormalVector sum = new NormalVector(0, 0);
        double offsetSum = 0;
        for (int i = 0; i < mPoints.length; i++) {

            double factor = 1 - mPoints[i].getClassValue() * (argVector.getW1() * mPoints[i].getX() + argVector.getW2() * mPoints[i].getY() + argOffset);
            factor = Math.max(0, factor) * mPoints[i].getClassValue();

            sum.setW1(sum.getW1() + mPoints[i].getX() * factor);
            sum.setW2(sum.getW2() + mPoints[i].getY() * factor);
            offsetSum += factor;
        }

        NormalVector resVec = new NormalVector(argVector.getW1() - 2 * C * sum.getW1(), argVector.getW2() - 2 * C * sum.getW2());
        double resOffset = -2 * C * offsetSum;

        return new Argument(resVec, resOffset);
    }

    private Argument stop(Argument arg, double stepSize) {
        if (arg.getOffset() < stepSize * 10) {
            arg.setOffset(0);
        }
        if (arg.getNormalVector().getW1() < stepSize * 10) {
            arg.getNormalVector().setW1(0);
        }
        if (arg.getNormalVector().getW2() < stepSize * 10) {
            arg.getNormalVector().setW2(0);
        }

        if (arg.getOffset() == 0 && arg.getNormalVector().getW1() == 0 && arg.getNormalVector().getW2() == 0) {
            return null;
        } else {
            return arg;
        }

//        return arg.getOffset() < 0.01 && arg.getNormalVector().getW1() < 0.01 && arg.getNormalVector().getW2() < 0.01;
    }

//    private double getStepSize() {
//
//    }
}
