package com.trent.awesomejumper.utils;

/**
 * Created by Sinthu on 28.10.2015.
 */
public class Interval {

    public float min, max;

    public Interval(float min, float max) {
        this.min = min;
        this.max = max;

    }

    @Override
    public String toString() {
        return "min:  " + min + " | " + "max: " + max;
    }




}
