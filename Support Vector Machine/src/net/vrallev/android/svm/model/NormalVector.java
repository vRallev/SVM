package net.vrallev.android.svm.model;

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
    public boolean equals(Object o) {
        if (o instanceof NormalVector) {
            NormalVector vector = (NormalVector) o;
            double res = (vector.mW2 / vector.mW1) / (mW2 / mW1);
            return res < 1.0001 && res > 0.999;
        }

        return super.equals(o);
    }

    @Override
    public NormalVector clone() {
        return new NormalVector(mW1, mW2);
    }
}
