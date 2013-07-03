package net.vrallev.android.svm.gradient;

import net.vrallev.android.svm.Optimizer;
import net.vrallev.android.svm.model.LabeledPoint;
import net.vrallev.android.svm.model.Line;
import net.vrallev.android.svm.model.NormalVector;

import java.util.List;

/**
 * @author Ralf Wondratschek
 */
public class GradientDescent implements Optimizer {

    private Line mLine;
    private LabeledPoint[] mPoints;

    public GradientDescent(Line line, List<LabeledPoint> points) {
        mLine = line.clone();
        mPoints = new LabeledPoint[points.size()];
        for (int i = 0; i < mPoints.length; i++) {
            mPoints[i] = points.get(i).clone();
        }
    }

    @Override
    public Line optimize(int iterations) {
        GradientDescentArgument[] arguments = new GradientDescentArgument[iterations + 1];
        arguments[0] = new GradientDescentArgument(mLine.getNormalVector().clone(), mLine.getOffset());

        final double stepSize = 1D / getLipschitzConstant(mPoints);

        for (int i = 1; i < arguments.length; i++) {
            GradientDescentArgument derivation = calcDerivation(arguments[i - 1]);

            arguments[i] = arguments[i - 1].next(stepSize, derivation);
        }

        return arguments[arguments.length - 1].toLine();
    }

    private GradientDescentArgument calcDerivation(GradientDescentArgument arg) {
        double argOffset = arg.getOffset();
        NormalVector argVector = arg.getNormalVector();

        NormalVector sum = new NormalVector(0, 0);
        double offsetSum = 0;
        for (LabeledPoint point : mPoints) {

            double factor = 1 - point.getY() * (argVector.getW1() * point.getX1() + argVector.getW2() * point.getX2() + argOffset);
            factor = Math.max(0, factor) * point.getY();

            sum.setW1(sum.getW1() + point.getX1() * factor);
            sum.setW2(sum.getW2() + point.getX2() * factor);
            offsetSum += factor;
        }

        NormalVector resVec = new NormalVector(argVector.getW1() - 2 * C * sum.getW1(), argVector.getW2() - 2 * C * sum.getW2());
        double resOffset = -2 * C * offsetSum;

        return new GradientDescentArgument(resVec, resOffset);
    }

    public static double getLipschitzConstant(LabeledPoint[] points) {
        double sum = 0;
        double sum2 = 0;
        for (LabeledPoint p : points) {
            double norm = Math.sqrt(Math.pow(p.getX1(), 2) + Math.pow(p.getX2(), 2));
            sum += Math.pow(norm, 2);
            sum2 += norm;
        }

        sum = 1 + 2 * C * sum;
        sum2 = 1 * 2 * C * sum2;

        return Math.max(Math.max(Math.max(sum, sum2), 2 * C), 1);
    }
}
