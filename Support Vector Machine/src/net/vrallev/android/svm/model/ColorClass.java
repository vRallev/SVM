package net.vrallev.android.svm.model;

import android.graphics.Color;

/**
 * @author Ralf Wondratschek
 */
public enum ColorClass {

    RED(1, Color.parseColor("#FF4444")),
    BLUE(-1, Color.parseColor("#33B5E5"));

    private int mValue;
    private int mColor;

    ColorClass(int value, int color) {
        mValue = value;
        mColor = color;
    }

    public int getColor() {
        return mColor;
    }

    public int getValue() {
        return mValue;
    }
}
