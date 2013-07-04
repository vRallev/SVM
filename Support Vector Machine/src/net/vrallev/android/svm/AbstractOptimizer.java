package net.vrallev.android.svm;

import net.vrallev.android.svm.model.LabeledPoint;
import net.vrallev.android.svm.model.Line;

import java.util.List;

/**
 * @author Ralf Wondratschek
 */
public abstract class AbstractOptimizer implements Optimizer {

    protected Line mLine;
    protected LabeledPoint[] mPoints;
    protected int mIterations;

    protected boolean mCancelled;

    public AbstractOptimizer(Line line, List<LabeledPoint> points, int iterations) {
        mLine = line.clone();
        mPoints = new LabeledPoint[points.size()];
        for (int i = 0; i < mPoints.length; i++) {
            mPoints[i] = points.get(i).clone();
        }

        mIterations = iterations;
        mCancelled = false;
    }

    @Override
    public void cancel() {
        mCancelled = true;
    }
}
