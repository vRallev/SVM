package net.vrallev.android.svm;

import net.vrallev.android.svm.model.Line;

/**
 * @author Ralf Wondratschek
 */
public interface Optimizer {

    public static final double C = 100.0;

    public Line optimize();

    public void cancel();

}
