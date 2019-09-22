package com.slaviboy.slideredittext;

public class CornerRadius {

    private float upperLeft;
    private float upperRight;
    private float lowerLeft;
    private float lowerRight;

    public CornerRadius(float upperLeft, float upperRight, float lowerLeft, float lowerRight) {
        this.upperLeft = upperLeft;
        this.upperRight = upperRight;
        this.lowerLeft = lowerLeft;
        this.lowerRight = lowerRight;
    }

    public CornerRadius(CornerRadius cornerRadius) {
        this.upperLeft = cornerRadius.upperLeft;
        this.upperRight = cornerRadius.upperRight;
        this.lowerLeft = cornerRadius.lowerLeft;
        this.lowerRight = cornerRadius.lowerRight;
    }

    public void set(float upperLeft, float upperRight, float lowerLeft, float lowerRight) {
        this.upperLeft = upperLeft;
        this.upperRight = upperRight;
        this.lowerLeft = lowerLeft;
        this.lowerRight = lowerRight;
    }

    public void add(float upperLeft, float upperRight, float lowerLeft, float lowerRight) {
        this.upperLeft += upperLeft;
        this.upperRight += upperRight;
        this.lowerLeft += lowerLeft;
        this.lowerRight += lowerRight;
    }

    public void add(float all) {
        this.add(all, all, all, all);
    }

    public void checkMax(float max) {
        if (max < 2 * upperLeft) upperLeft = max / 2;
        if (max < 2 * upperRight) upperRight = max / 2;
        if (max < 2 * lowerLeft) lowerLeft = max / 2;
        if (max < 2 * lowerRight) lowerRight = max / 2;
    }

    public float getLowerLeft() {
        return lowerLeft;
    }

    public float getLowerRight() {
        return lowerRight;
    }

    public float getUpperLeft() {
        return upperLeft;
    }

    public float getUpperRight() {
        return upperRight;
    }

    public void setLowerLeft(float lowerLeft) {
        this.lowerLeft = lowerLeft;
    }

    public void setLowerRight(float lowerRight) {
        this.lowerRight = lowerRight;
    }

    public void setUpperLeft(float upperLeft) {
        this.upperLeft = upperLeft;
    }

    public void setUpperRight(float upperRight) {
        this.upperRight = upperRight;
    }

    @Override
    public String toString() {
        return "upperLeft: " + upperLeft + ", " +
                "upperRight: " + upperRight + ", " +
                "lowerLeft: " + lowerLeft + ", " +
                "lowerRight: " + lowerRight;
    }
}
