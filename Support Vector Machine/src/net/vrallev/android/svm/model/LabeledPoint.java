package net.vrallev.android.svm.model;

/**
 * @author Ralf Wondratschek
 */
public class LabeledPoint implements Cloneable {

    private double mX1;
    private double mX2;
    private ColorClass mColorClass;

    public LabeledPoint(double x, double y, ColorClass clazz) {
        mX1 = x;
        mX2 = y;
        mColorClass = clazz;
    }

    public void setX1(double x1) {
        mX1 = x1;
    }

    public double getX1() {
        return mX1;
    }

    public void setX2(double x2) {
        mX2 = x2;
    }

    public double getX2() {
        return mX2;
    }

    public ColorClass getColorClass() {
        return mColorClass;
    }

    public int getY() {
        return mColorClass.getValue();
    }

    @Override
    public LabeledPoint clone() {
        return new LabeledPoint(mX1, mX2, mColorClass);
    }
}
