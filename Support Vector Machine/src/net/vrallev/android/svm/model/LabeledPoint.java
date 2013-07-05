package net.vrallev.android.svm.model;

import net.vrallev.android.base.util.L;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Ralf Wondratschek
 */
public class LabeledPoint implements Cloneable {

    private static List<LabeledPoint> sPool = new ArrayList<LabeledPoint>();

    public static LabeledPoint getInstance(double x, double y, ColorClass clazz) {
        if (sPool.isEmpty()) {
            return new LabeledPoint(x, y, clazz);
        } else {
            LabeledPoint p = sPool.remove(0);
            p.mX1 = x;
            p.mX2 = y;
            p.mColorClass = clazz;
            return p;
        }
    }

    private double mX1;
    private double mX2;
    private ColorClass mColorClass;

    private static int count = 0;

    private LabeledPoint(double x, double y, ColorClass clazz) {
        mX1 = x;
        mX2 = y;
        mColorClass = clazz;

        count++;
        L.debug("Points Count " + count);
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

    public void release() {
        sPool.add(this);
    }

    public static boolean listEqual(List<LabeledPoint> list1, List<LabeledPoint> list2) {
        if (list1.size() != list2.size()) {
            return false;
        }
        for (LabeledPoint p : list1) {
            if (!list2.contains(p)) {
                return false;
            }
        }
        for (LabeledPoint p : list2) {
            if (!list1.contains(p)) {
                return false;
            }
        }
        return true;
    }

    public static boolean hasColorClass(List<LabeledPoint> points, ColorClass clazz) {
        for(LabeledPoint p : points) {
            if (p.getColorClass().equals(clazz)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof LabeledPoint) {
            LabeledPoint point = (LabeledPoint) o;
            return point.mColorClass.equals(mColorClass) && point.mX1 == mX1 && point.mX2 == mX2;
        }

        return super.equals(o);
    }

    @Override
    public LabeledPoint clone() {
        return getInstance(mX1, mX2, mColorClass);
    }
}
