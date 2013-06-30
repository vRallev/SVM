package net.vrallev.android.svm.gradient;

/**
 * @author Ralf Wondratschek
 */
public class NormalVector implements Cloneable {

    private double mW1;
    private double mW2;

    public NormalVector(double w1, double w2) {
        mW1 = w1;
        mW2 = w2;
    }

    public double getW2() {
        return mW2;
    }

    public void setW2(double w2) {
        mW2 = w2;
    }

    public double getW1() {
        return mW1;
    }

    public void setW1(double w1) {
        mW1 = w1;
    }

    @Override
    public NormalVector clone() {
        return new NormalVector(mW1, mW2);
    }
}
