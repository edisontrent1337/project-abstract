package com.trent.awesomejumper.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.trent.awesomejumper.testing.CollisionBox;
import com.trent.awesomejumper.testing.Interval;

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

    // NICE BRACKETS

    public static String brackify(int a, int b) {
        return "[" + a + "  |   " + b + "]";
    }

    // FORMAT VECTORS FOR CONSOLE OUTPUT

    public static String formVec(Vector2 v) {
        return "[" + String.format("%.3f", v.x) + "|" + String.format("%.3f", v.y) + "]";
    }

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
    public static float dPro(Vector2 v1, Vector2 v2) {
        return v1.x * v2.x + v1.y * v2.y;
    }

    // PROJECTION OF A SHAPE ONTO AN AXIS
    /**
     * Returns the projection values of a shape (CollisionBox) onto an axis
     * @param box represents the shape which will be projected.
     * @param axis represents the axis on which the shape will be projected.
     *
     */
    public static Interval getProjection(CollisionBox box, Vector2 axis) {
        float min = dPro(box.getVertices().get(0), axis);
        float max = min;

        for (int i = 1; i < box.getVertices().size; i++) {
            float projection = dPro(box.getVertices().get(i), axis);
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

        return proj1.min <= proj2.max && proj2.min <= proj1.max;

    }


    // OVERLAP BETWEEN TWO PROJECTIONS
    // ---------------------------------------------------------------------------------------------
    /**
     * @param proj1 projection of the first shape onto an axis, x = min, y = max
     * @param proj2 projection of the second shape onto an axis, x = min, y = max
     */
    public static float getOverlap(Interval proj1, Interval proj2) {

        /**
         * The first projections maximum is bigger than the second projections minimum
         */
      /*  if(Math.abs(proj1.max) >= Math.abs(proj2.min)) {
            Gdx.app.log("CALCULATED OVERLAP", Float.toString(proj1.max - proj2.min));
            return proj1.max - proj2.min;
        }
        /**
         * The second projections maximum is bigger than the first projections minimum

        else
            Gdx.app.log("CALCULATED OVERLAP", Float.toString(proj2.max - proj1.min));
            return proj2.max - proj1.min;*/
        if(proj1.isSameAs(proj2))
            return 0;
        Gdx.app.log("CALCULATED OVERLAP", Float.toString(Math.min(proj1.max, proj2.max) - Math.max(proj1.min, proj2.min)));
        return Math.min(proj1.max, proj2.max) - Math.max(proj1.min, proj2.min);

    }


    // DIFFERENTIAL VECTOR
    // ---------------------------------------------------------------------------------------------
    public static Vector2 subVec(Vector2 start, Vector2 end) {
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


}
