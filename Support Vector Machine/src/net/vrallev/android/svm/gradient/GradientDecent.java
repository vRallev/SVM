package net.vrallev.android.svm.gradient;

import net.vrallev.android.svm.model.LabeledPoint;
import net.vrallev.android.svm.model.Line;
import net.vrallev.android.svm.model.NormalVector;

import java.util.List;

/**
 * @author Ralf Wondratschek
 */
public class GradientDecent {

    public static final double C = 10.0;

    private Line mLine;
    private LabeledPoint[] mPoints;

    public GradientDecent(Line line, List<LabeledPoint> points) {
        mLine = line.clone();
        mPoints = new LabeledPoint[points.size()];
        for (int i = 0; i < mPoints.length; i++) {
            mPoints[i] = points.get(i).clone();
        }
    }

    public Line optimize(int iterations) {
        GradientDescentArgument[] arguments = new GradientDescentArgument[iterations + 1];
        arguments[0] = new GradientDescentArgument(mLine.getNormalVector().clone(), mLine.getOffset());

        final double stepSize = 1D / getLipschitzConstant(mPoints) / 4;

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

            double factor = 1 - point.getClassValue() * (argVector.getW1() * point.getX() + argVector.getW2() * point.getY() + argOffset);
            factor = Math.max(0, factor) * point.getClassValue();

            sum.setW1(sum.getW1() + point.getX() * factor);
            sum.setW2(sum.getW2() + point.getY() * factor);
            offsetSum += factor;
        }

        NormalVector resVec = new NormalVector(argVector.getW1() - 2 * C * sum.getW1(), argVector.getW2() - 2 * C * sum.getW2());
        double resOffset = -2 * C * offsetSum;

        return new GradientDescentArgument(resVec, resOffset);
    }

    public static double getLipschitzConstant(LabeledPoint[] points) {
        double sum = 0;
        for (LabeledPoint p : points) {
            double norm = Math.sqrt(Math.pow(p.getX(), 2) + Math.pow(p.getY(), 2));
            sum += Math.pow(p.getClassValue() * norm, 2);
        }

        return 2 * C * sum;
    }
}
