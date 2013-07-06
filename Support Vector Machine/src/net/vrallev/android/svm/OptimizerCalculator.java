package net.vrallev.android.svm;

import de.greenrobot.event.EventBus;
import net.vrallev.android.svm.model.Line;
import net.vrallev.android.svm.model.NormalVector;

/**
 * @author Ralf Wondratschek
 */
public final class OptimizerCalculator {

    private static OptimizerCalculator sOptimizerCalculator = new OptimizerCalculator();

    public static OptimizerCalculator getInstance() {
        return sOptimizerCalculator;
    }

    private Optimizer mOptimizer;

    private OptimizerCalculator() {

    }

    public void calculate(Optimizer optimizer) {
        if (mOptimizer != null) {
            mOptimizer.cancel();
        }

        mOptimizer = optimizer;
        new Thread(mRunnable).start();
    }

    public boolean isCalculating() {
        return mOptimizer != null;
    }

    private Runnable mRunnable = new Runnable() {

        @Override
        public void run() {
            Line result = mOptimizer.optimize();

            /*
             + Sometimes a method diverges, so we need to catch this case
             */
            final boolean diverged = Double.isNaN(result.getOffset()) || Double.isNaN(result.getNormalVector().getW1()) || Double.isNaN(result.getNormalVector().getW2());
            if (diverged) {
                result = new Line(new NormalVector(-1, 1), 0);
            }

            EventBus.getDefault().postSticky(new ResultEvent(result, diverged));
            mOptimizer = null;
        }
    };

    public static class ResultEvent {

        private final Line mLine;
        private final boolean mDiverged;

        private ResultEvent(Line line, boolean diverged) {
            mLine = line;
            mDiverged = diverged;
        }

        public Line getLine() {
            return mLine;
        }

        public boolean isDiverged() {
            return mDiverged;
        }
    }
}
