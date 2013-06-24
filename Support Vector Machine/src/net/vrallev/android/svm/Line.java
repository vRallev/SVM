package net.vrallev.android.svm;

/**
 * @author Ralf Wondratschek
 */
public class Line {

    private int mStartX;
    private int mStartY;
    private int mEndX;
    private int mEndY;

    public Line(int startX, int startY, int endX, int endY) {
        mStartX = startX;
        mStartY = startY;
        mEndX = endX;
        mEndY = endY;
    }


    public int getStartX() {
        return mStartX;
    }

    public Line setStartX(int startX) {
        mStartX = startX;
        return this;
    }

    public int getStartY() {
        return mStartY;
    }

    public Line setStartY(int startY) {
        mStartY = startY;
        return this;
    }

    public int getEndX() {
        return mEndX;
    }

    public Line setEndX(int endX) {
        mEndX = endX;
        return this;
    }

    public int getEndY() {
        return mEndY;
    }

    public Line setEndY(int endY) {
        this.mEndY = endY;
        return this;
    }
}
