package net.vrallev.android.svm.gradient;

import net.vrallev.android.svm.Line;

/**
 * @author Ralf Wondratschek
 */
public class Argument implements Cloneable {

    private NormalVector mNormalVector;
    private double mOffset;

    public Argument(NormalVector normalVector, double offset) {
        mNormalVector = normalVector;
        mOffset = offset;
    }

    public double getOffset() {
        return mOffset;
    }

    public void setOffset(double offset) {
        mOffset = offset;
    }

    public NormalVector getNormalVector() {
        return mNormalVector;
    }

    public void setNormalVector(NormalVector normalVector) {
        mNormalVector = normalVector;
    }

    public Argument multipy(double factor) {
        Argument res = this.clone();
        res.getNormalVector().setW1(res.getNormalVector().getW1() * factor);
        res.getNormalVector().setW2(res.getNormalVector().getW2() * factor);
        res.setOffset(res.getOffset() * factor);
        return res;
    }

    public Argument minus(Argument arg) {
        Argument res = this.clone();
        res.getNormalVector().setW1(res.getNormalVector().getW1() - arg.getNormalVector().getW1());
        res.getNormalVector().setW2(res.getNormalVector().getW2() - arg.getNormalVector().getW2());
        res.setOffset(res.getOffset() - arg.getOffset());
        return res;
    }

    public Argument next(double stepSize, Argument derivate) {
        return minus(derivate.multipy(stepSize));
    }

    public Line toLine() {
        double m = mNormalVector.getW1() * -1 / mNormalVector.getW2();
        return new Line(0, mOffset, 1, m + mOffset);
    }

    @Override
    public Argument clone() {
        return new Argument(mNormalVector.clone(), mOffset);
    }
}
