package net.vrallev.android.svm.method;

import android.os.Bundle;
import net.vrallev.android.svm.model.LabeledPoint;
import net.vrallev.android.svm.model.Line;
import net.vrallev.android.svm.model.NormalVector;

import java.util.List;

/**
 * @author Peter Grube
 * @author Ralf Wondratschek
 */
public class NewtonMethod extends AbstractOptimizer {

    private static final String FIRST_DERIVATE_W1 = "firstDerivateW1";
    private static final String FIRST_DERIVATE_W2 = "firstDerivateW2";
    private static final String FIRST_DERIVATE_B = "firstDerivateB";
    private static final String SECOND_DERIVATE_W1 = "secondDerivateW1";
    private static final String SECOND_DERIVATE_W2 = "secondDerivateW2";
    private static final String SECOND_DERIVATE_B = "secondDerivateB";
    private static final String SECOND_DERIVATE_W1W2 = "secondDerivateW1W2";
    private static final String SECOND_DERIVATE_W1B = "secondDerivateW1B";
    private static final String SECOND_DERIVATE_W2B = "secondDerivateW2B";

    private static final int DEFAULT_ITERATIONS = 100;
    private static final double STOP_DIFFERENCE = 0.00000001;

    public NewtonMethod(Line line, List<LabeledPoint> points) {
        this(line, points, DEFAULT_ITERATIONS);
    }

    public NewtonMethod(Line line, List<LabeledPoint> points, int iterations) {
        super(line, points, iterations);
    }

    @Override
    protected Line innerOptimize() {
        SvmArgument argument = new SvmArgument(mLine.getNormalVector(), mLine.getOffset());

        Bundle derivation = new Bundle();

        for (int i = 0; i < mIterations; i++) {

            if (mCancelled) {
                return null;
            }

            derivation.putDouble(FIRST_DERIVATE_W1, firstDerivateW1(argument.getOffset(), C, mPoints, argument.getNormalVector()));
            derivation.putDouble(FIRST_DERIVATE_W2, firstDerivateW2(argument.getOffset(), C, mPoints, argument.getNormalVector()));
            derivation.putDouble(FIRST_DERIVATE_B, firstDerivateB(argument.getOffset(), C, mPoints, argument.getNormalVector()));
            derivation.putDouble(SECOND_DERIVATE_W1, secondDerivateW1(argument.getOffset(), C, mPoints, argument.getNormalVector()));
            derivation.putDouble(SECOND_DERIVATE_W2, secondDerivateW2(argument.getOffset(), C, mPoints, argument.getNormalVector()));
            derivation.putDouble(SECOND_DERIVATE_B, secondDerivateB(argument.getOffset(), C, mPoints, argument.getNormalVector()));
            derivation.putDouble(SECOND_DERIVATE_W1W2, secondDerivateW2W1(argument.getOffset(), C, mPoints, argument.getNormalVector()));
            derivation.putDouble(SECOND_DERIVATE_W1B, secondDerivateW1B(argument.getOffset(), C, mPoints, argument.getNormalVector()));
            derivation.putDouble(SECOND_DERIVATE_W2B, secondDerivateW2B(argument.getOffset(), C, mPoints, argument.getNormalVector()));

            SvmArgument newArg = newtonMethod(argument, derivation);
            if (stop(argument, newArg)) {
                return newArg.toLine();
            }

            argument = newArg;
        }
        return argument.toLine();
    }

    private static double[][] invertHesse(
            double dw1up2,
            double dw2up2,
            double dbup2,
            double dw2db,
            double dw1db,
            double dw1dw2) {

        double h11 = dw2up2 * dbup2 - dw2db * dw2db;
        double h12 = dw1db * dw2db - dw1dw2 * dbup2;
        double h13 = dw1dw2 * dw2db - dw1db * dw2up2;
        double h21 = h12;
        double h22 = dw1up2 * dbup2 - dw1db * dw1db;
        double h23 = dw1db * dw1dw2 - dw1up2 * dw2db;
        double h31 = h13;
        double h32 = h23;
        double h33 = dw1up2 * dw2up2 - dw1dw2 * dw1dw2;

        double[][] invHesse = {{h11, h12, h13},
                {h21, h22, h23},
                {h31, h32, h33}};

        double det = dw1up2 * dw2up2 * dbup2 + dw1dw2 * dw2db * dw1db + dw1db * dw1dw2 * dw2db - dw1db * dw2up2 * dw1db - dw2db * dw2db * dw1up2 - dbup2 * dw1dw2 * dw1dw2;
        double detHesse = 1 / det;

        double[][] invertHesse = new double[3][3];

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                invertHesse[i][j] = detHesse * invHesse[i][j];
            }
        }

        return invertHesse;
    }

    private static double[] secondDerTimesfirstDer(double[][] matrix, double[] vector) {

        double[] res = new double[3];

        for (int i = 0; i < 3; i++) {
            res[i] = matrix[i][0] * vector[0] + matrix[i][1] * vector[1] + matrix[i][2] * vector[2];
        }

        return res;
    }

    private static SvmArgument newtonMethod(SvmArgument normVecOffset, Bundle derivates) {
        SvmArgument vecOffs = normVecOffset.clone();

        double[] firstDerivates = {derivates.getDouble(FIRST_DERIVATE_W1), derivates.getDouble(FIRST_DERIVATE_W2), derivates.getDouble(FIRST_DERIVATE_B)};
        double[] functionProduct = secondDerTimesfirstDer(
                invertHesse(
                        derivates.getDouble(SECOND_DERIVATE_W1),
                        derivates.getDouble(SECOND_DERIVATE_W2),
                        derivates.getDouble(SECOND_DERIVATE_B),
                        derivates.getDouble(SECOND_DERIVATE_W2B),
                        derivates.getDouble(SECOND_DERIVATE_W1B),
                        derivates.getDouble(SECOND_DERIVATE_W1W2)),
                firstDerivates);

        vecOffs.setNormalVector(new NormalVector(normVecOffset.getNormalVector().getW1() - functionProduct[0], normVecOffset.getNormalVector().getW2() - functionProduct[1]));
        vecOffs.setOffset(normVecOffset.getOffset() - functionProduct[2]);

        return vecOffs;
    }


    private static double calculateExponent(double y, double w1, double w2, double x1, double x2, double b) {
        return Math.exp((-1) * y * (w1 * x1 + w2 * x2 + b));
    }

    private static double firstDerivateW1(double b, double c, LabeledPoint[] points, NormalVector vectorW) {
        double sum = 0.0;
        double zaehler = 0.0;
        double nenner = 0.0;
        double exp = 0.0;

        for (int i = 0; i < points.length; i++) {
            exp = calculateExponent(points[i].getY(), vectorW.getW1(), vectorW.getW2(), points[i].getX1(), points[i].getX2(), b);
            zaehler = exp * ((-1) * points[i].getY() * points[i].getX1());
            nenner = 1 + exp;
            sum += zaehler / nenner;
        }

        return vectorW.getW1() + c * sum;
    }

    private static double firstDerivateW2(double b, double c, LabeledPoint[] points, NormalVector vectorW) {
        double sum = 0.0;
        double zaehler = 0.0;
        double nenner = 0.0;
        double exp = 0.0;

        for (int i = 0; i < points.length; i++) {
            exp = calculateExponent(points[i].getY(), vectorW.getW1(), vectorW.getW2(), points[i].getX1(), points[i].getX2(), b);
            zaehler = exp * ((-1) * points[i].getY() * points[i].getX2());
            nenner = 1 + exp;
            sum += zaehler / nenner;
        }

        return vectorW.getW2() + c * sum;
    }

    private static double firstDerivateB(double b, double c, LabeledPoint[] points, NormalVector vectorW) {
        double sum = 0.0;
        double zaehler = 0.0;
        double nenner = 0.0;
        double exp = 0.0;

        for (int i = 0; i < points.length; i++) {
            exp = calculateExponent(points[i].getY(), vectorW.getW1(), vectorW.getW2(), points[i].getX1(), points[i].getX2(), b);
            zaehler = exp * ((-1) * points[i].getY());
            nenner = 1 + exp;
            sum += zaehler / nenner;
        }

        return c * sum;
    }

    private static double secondDerivateW1(double b, double c, LabeledPoint[] points, NormalVector vectorW) {
        double sum = 0.0;
        double zaehler = 0.0;
        double nenner = 0.0;
        double exp = 0.0;

        for (int i = 0; i < points.length; i++) {
            exp = calculateExponent(points[i].getY(), vectorW.getW1(), vectorW.getW2(), points[i].getX1(), points[i].getX2(), b);
            zaehler = Math.pow(points[i].getY(), 2) * Math.pow(points[i].getX1(), 2) * exp;
            nenner = Math.pow(1 + exp, 2);
            sum += zaehler / nenner;
        }

        return 1.0 + c * sum;
    }

    private static double secondDerivateW2(double b, double c, LabeledPoint[] points, NormalVector vectorW) {
        double sum = 0.0;
        double zaehler = 0.0;
        double nenner = 0.0;
        double exp = 0.0;

        for (int i = 0; i < points.length; i++) {
            exp = calculateExponent(points[i].getY(), vectorW.getW1(), vectorW.getW2(), points[i].getX1(), points[i].getX2(), b);
            zaehler = Math.pow(points[i].getY(), 2) * Math.pow(points[i].getX2(), 2) * exp;
            nenner = Math.pow(1 + exp, 2);
            sum += zaehler / nenner;
        }

        return 1.0 + c * sum;
    }

    private static double secondDerivateW2W1(double b, double c, LabeledPoint[] points, NormalVector vectorW) {
        double sum = 0.0;
        double zaehler = 0.0;
        double nenner = 0.0;
        double exp = 0.0;

        for (int i = 0; i < points.length; i++) {
            exp = calculateExponent(points[i].getY(), vectorW.getW1(), vectorW.getW2(), points[i].getX1(), points[i].getX2(), b);
            zaehler = Math.pow(points[i].getY(), 2) * points[i].getX1() * points[i].getX2() * exp;
            nenner = Math.pow(1 + exp, 2);
            sum += zaehler / nenner;
        }

        return c * sum;
    }

    private static double secondDerivateW2B(double b, double c, LabeledPoint[] points, NormalVector vectorW) {
        double sum = 0.0;
        double zaehler = 0.0;
        double nenner = 0.0;
        double exp = 0.0;

        for (int i = 0; i < points.length; i++) {
            exp = calculateExponent(points[i].getY(), vectorW.getW1(), vectorW.getW2(), points[i].getX1(), points[i].getX2(), b);
            zaehler = Math.pow(points[i].getY(), 2) * points[i].getX2() * exp;
            nenner = Math.pow(1 + exp, 2);
            sum += zaehler / nenner;
        }

        return c * sum;
    }

    private static double secondDerivateW1B(double b, double c, LabeledPoint[] points, NormalVector vectorW) {
        double sum = 0.0;
        double zaehler = 0.0;
        double nenner = 0.0;
        double exp = 0.0;

        for (int i = 0; i < points.length; i++) {
            exp = calculateExponent(points[i].getY(), vectorW.getW1(), vectorW.getW2(), points[i].getX1(), points[i].getX2(), b);
            zaehler = Math.pow(points[i].getY(), 2) * points[i].getX1() * exp;
            nenner = Math.pow(1 + exp, 2);
            sum += zaehler / nenner;
        }

        return c * sum;
    }

    private static double secondDerivateB(double b, double c, LabeledPoint[] points, NormalVector vectorW) {
        double sum = 0.0;
        double zaehler = 0.0;
        double nenner = 0.0;
        double exp = 0.0;

        for (int i = 0; i < points.length; i++) {
            exp = calculateExponent(points[i].getY(), vectorW.getW1(), vectorW.getW2(), points[i].getX1(), points[i].getX2(), b);
            zaehler = Math.pow(points[i].getY(), 2) * exp;
            nenner = Math.pow(1 + exp, 2);
            sum += zaehler / nenner;
        }

        return c * sum;
    }

    private boolean stop(SvmArgument before, SvmArgument after) {
        return Math.abs(Math.abs(before.getNormalVector().getW1()) - Math.abs(after.getNormalVector().getW1())) < STOP_DIFFERENCE
                && Math.abs(Math.abs(before.getNormalVector().getW2()) - Math.abs(after.getNormalVector().getW2())) < STOP_DIFFERENCE
                && Math.abs(Math.abs(before.getOffset()) - Math.abs(after.getOffset())) < STOP_DIFFERENCE;
    }
}
