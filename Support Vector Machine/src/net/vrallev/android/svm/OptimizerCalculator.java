package net.vrallev.android.svm;

import de.greenrobot.event.EventBus;
import net.vrallev.android.svm.model.Line;

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

    private Runnable mRunnable = new Runnable() {

        @Override
        public void run() {
            Line result = mOptimizer.optimize();
            EventBus.getDefault().post(new ResultEvent(result));
            mOptimizer = null;
        }
    };

    public static class ResultEvent {

        public Line mLine;

        private ResultEvent(Line line) {
            mLine = line;
        }

        public Line getLine() {
            return mLine;
        }
    }
}
