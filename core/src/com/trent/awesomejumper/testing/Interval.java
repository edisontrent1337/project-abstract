package com.trent.awesomejumper.testing;

import com.badlogic.gdx.math.Vector2;

/**
 * Created by Sinthu on 28.10.2015.
 */
public class Interval {

    public float min, max;

    public Interval(float min, float max) {
        this.min = min;
        this.max = max;

    }


    public String toString() {
        return "min:  " + min + " | " + "max: " + max;
    }

    /**
     * Returns, whether two intervals are the exact same.
     * @param i Interval to investigate
     * @return  bool
     */
    public boolean isSameAs(Interval i) {
        return (i.max == this.max) && (i.min == this.min);

    }


}
