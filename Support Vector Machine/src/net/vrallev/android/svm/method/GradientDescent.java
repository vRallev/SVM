package net.vrallev.android.svm.method;

import net.vrallev.android.svm.model.LabeledPoint;
import net.vrallev.android.svm.model.Line;
import net.vrallev.android.svm.model.NormalVector;

import java.util.List;

/**
 * @author Ralf Wondratschek
 */
public class GradientDescent extends AbstractOptimizer {

    private static final int DEFAULT_ITERATIONS = 10000;
    private static final double STOP_DIFFERENCE = 0.00001;

    public GradientDescent(Line line, List<LabeledPoint> points) {
        this(line, points, DEFAULT_ITERATIONS);
    }

    public GradientDescent(Line line, List<LabeledPoint> points, int iterations) {
        super(line, points, iterations);
    }

    @Override
    public Line innerOptimize() {
        SvmArgument[] arguments = new SvmArgument[mIterations + 1];
        arguments[0] = new SvmArgument(mLine.getNormalVector().clone(), mLine.getOffset());

        final double stepSize = 1D / getLipschitzConstant(mPoints);

        for (int i = 1; i < arguments.length; i++) {
            if (mCancelled) {
                return null;
            }

            SvmArgument derivation = calcDerivation(arguments[i - 1]);

            arguments[i] = arguments[i - 1].next(stepSize, derivation);

            if (stop(arguments[i - 1], arguments[i])) {
                return arguments[i].toLine();
            }
        }

        return arguments[arguments.length - 1].toLine();
    }

    private SvmArgument calcDerivation(SvmArgument arg) {
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

        return new SvmArgument(resVec, resOffset);
    }

    private static double getLipschitzConstant(LabeledPoint[] points) {
        double sum = 0;
        double sum2 = 0;
        for (LabeledPoint p : points) {
            double norm = Math.sqrt(Math.pow(p.getX1(), 2) + Math.pow(p.getX2(), 2));
            sum += Math.pow(norm, 2);
            sum2 += norm;
        }

        sum = 1 + 2 * C * sum;
        sum2 = 1 + 2 * C * sum2;

        return Math.max(Math.max(Math.max(sum, sum2), 2 * C), 1);
    }

    private boolean stop(SvmArgument before, SvmArgument after) {
        return Math.abs(Math.abs(before.getNormalVector().getW1()) - Math.abs(after.getNormalVector().getW1())) < STOP_DIFFERENCE
                && Math.abs(Math.abs(before.getNormalVector().getW2()) - Math.abs(after.getNormalVector().getW2())) < STOP_DIFFERENCE
                && Math.abs(Math.abs(before.getOffset()) - Math.abs(after.getOffset())) < STOP_DIFFERENCE;
    }
}
