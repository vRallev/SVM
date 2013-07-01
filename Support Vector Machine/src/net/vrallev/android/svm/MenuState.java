package net.vrallev.android.svm;

import net.vrallev.android.svm.model.ColorClass;

/**
 * @author Ralf Wondratschek
 */
public enum MenuState {

    STATE_RED(ColorClass.RED, R.drawable.ab_red_dot),
    STATE_BLUE(ColorClass.BLUE, R.drawable.ab_blue_dot),
    STATE_LINE(null, R.drawable.ab_line);

    private ColorClass mColorClass;
    private int mDrawable;

    MenuState(ColorClass clazz, int drawable) {
        mColorClass = clazz;
        mDrawable = drawable;
    }

    public ColorClass getColorClass() {
        return mColorClass;
    }

    public int getDrawable() {
        return mDrawable;
    }

    public static MenuState getNext(MenuState old) {
        MenuState[] values = values();
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
