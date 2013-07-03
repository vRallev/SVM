package net.vrallev.android.svm.gradient;

import net.vrallev.android.svm.Optimizer;
import net.vrallev.android.svm.model.LabeledPoint;
import net.vrallev.android.svm.model.Line;
import net.vrallev.android.svm.model.NormalVector;

import java.util.List;

/**
 * @author Ralf Wondratschek
 */
public class SubGradientDescent implements Optimizer {

    private static final double DEFAULT_STEP_PARAMETER = 100D;
    private static final double STOP_DIFFERENCE = 0.001;

    private Line mLine;
    private LabeledPoint[] mPoints;
    private double mStepParameter;

    public SubGradientDescent(Line line, List<LabeledPoint> points) {
        this(line, points, DEFAULT_STEP_PARAMETER);
    }

    public SubGradientDescent(Line line, List<LabeledPoint> points, double stepParameter) {
        mLine = line.clone();
        mPoints = new LabeledPoint[points.size()];
        for (int i = 0; i < mPoints.length; i++) {
            mPoints[i] = points.get(i).clone();
        }

        mStepParameter = stepParameter;
    }

    @Override
    public Line optimize(int iterations) {
        GradientDescentArgument[] arguments = new GradientDescentArgument[iterations + 1];
        arguments[0] = new GradientDescentArgument(mLine.getNormalVector().clone(), mLine.getOffset());

        for (int i = 1; i < arguments.length; i++) {
            GradientDescentArgument subGradient = getSubGradient(arguments[i - 1], 1D / 2D);

            double stepSize = getStepSize(i, mStepParameter);
            arguments[i] = arguments[i - 1].minus(subGradient.multipy(1D / subGradient.norm()).multipy(stepSize));

            if (stop(arguments[i - 1], arguments[i])) {
                return arguments[i].toLine();
            }
        }

        return arguments[arguments.length - 1].toLine();
    }

    private GradientDescentArgument getSubGradient(GradientDescentArgument arg, double t) {
        double argOffset = arg.getOffset();
        NormalVector argVector = arg.getNormalVector();

        if (t < 0 || t > 1) {
            throw new IllegalArgumentException();
        }

        NormalVector sum = new NormalVector(0, 0);
        double offsetSum = 0;
        for (LabeledPoint point : mPoints) {

            double factor = 1 - point.getY() * (argVector.getW1() * point.getX1() + argVector.getW2() * point.getX2() + argOffset);

            if (factor > 0) {
                sum.setW1(sum.getW1() + -1 * point.getY() * point.getX1());
                sum.setW2(sum.getW2() + -1 * point.getY() * point.getX2());
                offsetSum += -1 * point.getY();

            } else if (factor == 0) {
                sum.setW1(sum.getW1() + (1 - t) * -1 * point.getY() * point.getX1());
                sum.setW2(sum.getW2() + (1 - t) * -1 * point.getY() * point.getX2());
                offsetSum += (1 - t) * -1 * point.getY();

            }
        }

        NormalVector resVec = new NormalVector(argVector.getW1() + C * sum.getW1(), argVector.getW2() + C * sum.getW2());
        double resOffset = C * offsetSum;

        return new GradientDescentArgument(resVec, resOffset);
    }

    private double getStepSize(int iteration, double stepParameter) {
        return stepParameter / iteration;
    }

    private boolean stop(GradientDescentArgument before, GradientDescentArgument after) {
        return Math.abs(Math.abs(before.getNormalVector().getW1()) - Math.abs(after.getNormalVector().getW1())) < STOP_DIFFERENCE
                && Math.abs(Math.abs(before.getNormalVector().getW2()) - Math.abs(after.getNormalVector().getW2())) < STOP_DIFFERENCE
                && Math.abs(Math.abs(before.getOffset()) - Math.abs(after.getOffset())) < STOP_DIFFERENCE;
    }
}
