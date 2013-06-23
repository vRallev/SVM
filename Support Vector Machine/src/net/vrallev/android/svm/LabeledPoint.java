package net.vrallev.android.svm;

import android.graphics.Color;

import java.io.LineNumberReader;

/**
 * @author Ralf Wondratschek
 */
public class LabeledPoint {

    private int mX;
    private int mY;
    private ColorClass mColorClass;

    public LabeledPoint(int x, int y, ColorClass clazz) {
        mX = x;
        mY = y;
        mColorClass = clazz;
    }

    public void setX(int x) {
        mX = x;
    }

    public int getX() {
        return mX;
    }

    public void setY(int y) {
        mY = y;
    }

    public int getY() {
        return mY;
    }

    public ColorClass getColorClass() {
        return mColorClass;
    }

    public static enum ColorClass {
        RED(1, Color.parseColor("#FF4444"), R.drawable.ab_red_dot),
        BLUE(-1, Color.parseColor("#33B5E5"), R.drawable.ab_blue_dot);

        private int mValue;
        private int mColor;
        private int mDrawable;

        ColorClass(int value, int color, int drawable) {
            mValue = value;
            mColor = color;
            mDrawable = drawable;
        }

        public int getColor() {
            return mColor;
        }

        public int getValue() {
            return mValue;
        }

        public int getDrawable() {
            return mDrawable;
        }

        public static ColorClass getNext(ColorClass old) {
            ColorClass[] values = values();
            int index = -1;
            for (int i = 0; i < values.length; i++) {
                if (values[i].equals(old)) {
                    index = i;
                    break;
                }
            }

            return values[(index + 1) % values.length];
        }
    }
}
