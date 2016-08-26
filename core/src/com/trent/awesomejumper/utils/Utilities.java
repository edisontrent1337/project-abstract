package com.trent.awesomejumper.utils;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.trent.awesomejumper.engine.physics.CollisionBox;

/**
 * Created by Sinthu on 12.06.2015.
 */
public class Utilities {


    public static final int WIDTH = 800;
    public static final int HEIGHT = 480;


    // RESET OPACITY OF AN IMAGE
    // ---------------------------------------------------------------------------------------------
    public static void resetOpacity(Image image) {
        Color c = image.getColor();
        c.a = 0f;
        image.setColor(c);
    }


    // CONSOLE FORMATTING UTILS
    // ---------------------------------------------------------------------------------------------

    // FORMAT VECTORS FOR CONSOLE OUTPUT
    /**
     * Formats an input vector v for console output. Rounded to 3 decimal places.
     * @param v - input vector
     */
    public static String formVec(Vector2 v) {
        return "(" + String.format("%.3f", v.x) + "|" + String.format("%.3f", v.y) + ")";
    }


    /**
     * Formats an input vector (x,y) for console output. Rounded to 3 decimal places.
     * @param x input x component
     * @param y inpit y component
     */
    public static String formVec(float x, float y) {
        return formVec(new Vector2(x, y));
    }


    // VECTOR OPERATIONS
    // ---------------------------------------------------------------------------------------------

    // DOT PRODUCT
    /**
     * Returns the dot product of two vectors.
     * @param v1 Vector 1
     * @param v2 Vector 2
     *
    */
    public static float dot(Vector2 v1, Vector2 v2) {
        return v1.x * v2.x + v1.y * v2.y;
    }

    /**
     * Returns the dot product of two vectors.
     * @param x1 x component of vector 1
     * @param x2 x component of vector 2
     * @param y1 y component of vector 1
     * @param y2 y component of vector 2
     *
     */
    public static float dot(float x1, float y1, float x2, float y2) {
        return x1 * x2 + y1 * y2;
    }

    // PROJECTION OF A SHAPE ONTO AN AXIS
    // ---------------------------------------------------------------------------------------------
    /**
     * Returns the projection min and max values of a shape (CollisionBox) onto an axis
     * @param box represents the shape which will be projected.
     * @param axis represents the axis on which the shape will be projected.
     */
    public static Interval getProjection(CollisionBox box, Vector2 axis) {
        float min = dot(box.getVertices().get(0), axis);
        float max = min;

        for (int i = 1; i < box.getVertices().size; i++) {
            float projection = dot(box.getVertices().get(i), axis);
            /**
             *  If the calculated projection is bigger or smaller than the min/max,
             *  the new value will be applied.
             */
            if (projection < min) {
                min = projection;
            }
            if (projection > max) {
                max = projection;
            }

        }

        return new Interval(min, max);
    }

    // CHECK WHETHER TWO PROJECTIONS OVERLAP
    // ---------------------------------------------------------------------------------------------
    /**
     * Returns whether two Intervals overlap or not.
     * @param proj1 projection of the first shape onto an axis, x = min, y = max
     * @param proj2 projection of the second shape onto an axis, x = min, y = max
     */
    public static boolean overlaps(Interval proj1, Interval proj2) {
        return proj1.min < proj2.max && proj2.min < proj1.max;
    }


    // OVERLAP BETWEEN TWO PROJECTIONS
    // ---------------------------------------------------------------------------------------------
    /**
     * Returns the overlap of two Intervals
     * @param proj1 projection of the first shape onto an axis, x = min, y = max
     * @param proj2 projection of the second shape onto an axis, x = min, y = max
     */
    public static float getOverlap(Interval proj1, Interval proj2) {

        return Math.min(proj1.max, proj2.max) - Math.max(proj1.min, proj2.min);

    }


    // DIFFERENTIAL VECTOR
    // ---------------------------------------------------------------------------------------------
    public static Vector2 sub(Vector2 start, Vector2 end) {
        return new Vector2(end.x - start.x, end.y - start.y);
    }

    // NORMAL VECTOR
    /**
     * Returns a normalized vector n perpendicular
     * to v.
     */
    public static Vector2 getNormal(Vector2 v) {
        return new Vector2(-v.y, v.x).nor();
    }


    public static float angle(Vector2 delta) {
        float angle = (float)Math.atan2(delta.y, delta.x)*180f/PhysicalConstants.PI;
        if(angle < 0) {
            angle = 360f - (-angle);
        }
        return angle;

    }


    // CHECK WHETHER AN OBJECT IS NULL
    // ---------------------------------------------------------------------------------------------

    public static void checkNotNull(String message, Object object) {
        if(object == null)
            throw new NullPointerException(message);
    }


    // PYTHAGORAS FOR TWO VALUES
    // ---------------------------------------------------------------------------------------------

    public static float pythagoras(float a, float b) {
        return (float) Math.sqrt(a*a + b*b);
    }


    // EASING FUNCTIONS
    // ---------------------------------------------------------------------------------------------


    // COLOR CREATION
    // ---------------------------------------------------------------------------------------------
    public static Color color(int r, int g, int b) {
        return color(r,g,b,256);
    }

    public static Color color(int r, int g ,int b, float a) {
        return new Color(r/256f, g/256f, b/256f, a);
    }

}
