package com.slaviboy.slideredittext;

public class Range {

    private double lower;
    private double upper;
    private double current;

    public Range(double lower, double upper) {
        this.lower = lower;
        this.upper = upper;
    }

    public void setLower(double lower) {
        this.lower = lower;
    }

    public void setUpper(double upper) {
        this.upper = upper;
    }

    public void setCurrent(double maxPosition, double currentPosition) {
        current = lower - (lower - upper) * (currentPosition / maxPosition);
    }

    public void setCurrent(double current) {

        double min = Math.min(lower, upper);
        double max = Math.max(lower, upper);

        // set current with check
        if (current < min) {
            current = min;
        } else if (current > max) {
            current = max;
        }
        this.current = current;
    }

    public double getLower() {
        return lower;
    }

    public double getUpper() {
        return upper;
    }

    public double getCurrent() {
        return current;
    }

    @Override
    public String toString() {
        return "lower: " + lower + ", " + "upper: " + upper;
    }
}
