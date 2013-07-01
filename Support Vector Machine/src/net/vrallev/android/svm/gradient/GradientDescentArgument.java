package net.vrallev.android.svm.gradient;

import net.vrallev.android.svm.model.Line;
import net.vrallev.android.svm.model.NormalVector;

/**
 * @author Ralf Wondratschek
 */
public class GradientDescentArgument implements Cloneable {

    private NormalVector mNormalVector;
    private double mOffset;

    public GradientDescentArgument(NormalVector normalVector, double offset) {
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

    public GradientDescentArgument multipy(double factor) {
        GradientDescentArgument res = this.clone();
        res.getNormalVector().setW1(res.getNormalVector().getW1() * factor);
        res.getNormalVector().setW2(res.getNormalVector().getW2() * factor);
        res.setOffset(res.getOffset() * factor);
        return res;
    }

    public GradientDescentArgument minus(GradientDescentArgument arg) {
        GradientDescentArgument res = this.clone();
        res.getNormalVector().setW1(res.getNormalVector().getW1() - arg.getNormalVector().getW1());
        res.getNormalVector().setW2(res.getNormalVector().getW2() - arg.getNormalVector().getW2());
        res.setOffset(res.getOffset() - arg.getOffset());
        return res;
    }

    public GradientDescentArgument next(double stepSize, GradientDescentArgument derivation) {
        return minus(derivation.multipy(stepSize));
    }

    public Line toLine() {
        return new Line(mNormalVector.clone(), mOffset);
    }

    @Override
    public GradientDescentArgument clone() {
        return new GradientDescentArgument(mNormalVector.clone(), mOffset);
    }
}
