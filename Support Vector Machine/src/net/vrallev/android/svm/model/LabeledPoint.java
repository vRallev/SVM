package net.vrallev.android.svm.model;

/**
 * @author Ralf Wondratschek
 */
public class LabeledPoint implements Cloneable {

    private double mX;
    private double mY;
    private ColorClass mColorClass;

    public LabeledPoint(double x, double y, ColorClass clazz) {
        mX = x;
        mY = y;
        mColorClass = clazz;
    }

    public void setX(double x) {
        mX = x;
    }

    public double getX() {
        return mX;
    }

    public void setY(double y) {
        mY = y;
    }

    public double getY() {
        return mY;
    }

    public ColorClass getColorClass() {
        return mColorClass;
    }

    public int getClassValue() {
        return mColorClass.getValue();
    }

    @Override
    public LabeledPoint clone() {
        return new LabeledPoint(mX, mY, mColorClass);
    }
}
