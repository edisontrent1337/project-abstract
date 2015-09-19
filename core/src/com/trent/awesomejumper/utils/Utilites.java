package com.trent.awesomejumper.utils;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.trent.awesomejumper.testing.CollisionBox;

/**
 * Created by Sinthu on 12.06.2015.
 */
public class Utilites {


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
    /*
    * Returns the dot product of two
    * vectors v1 and v2.
    */

    public static float dPro(Vector2 v1, Vector2 v2) {
        return v1.x * v2.x + v1.y * v2.y;
    }
    // PROJECTION OF A SHAPE ONTO AN AXIS
    /*
     * Returns the projection values of a shape (CollisionBox) onto an axis
     * @param box represents the shape which will be projected
     * @param axis
     *
     */
    public static Vector2 getProjection(CollisionBox box, Vector2 axis) {
        float min = dPro(box.getVertices().get(0), axis);
        float max = min;

        for (int i = 1; i < box.getVertices().size; i++) {
            float projection = dPro(box.getVertices().get(i), axis);
            if (projection < min) {
                min = projection;
            }
            if (projection > max) {
                max = projection;
            }

        }

        return new Vector2(min, max);
    }

    // CHECK WHETHER TWO PROJECTIONS OVERLAP
    // ---------------------------------------------------------------------------------------------
    /*
     * @param projection1 projection of the first shape onto an axis, x = min, y = max
     * @param projection2 projection of the second shape onto an axis, x = min, y = max
     */
    public static boolean overlaps(Vector2 projection1, Vector2 projection2) {
        if (projection1.x < projection2.x) {
            return projection1.y >= projection2.x;
        } else {
            return projection2.y >= projection1.x;
        }


    }


    // OVERLAP BETWEEN TWO PROJECTIONS
    // ---------------------------------------------------------------------------------------------
    /*
     * @param projection1 projection of the first shape onto an axis, x = min, y = max
     * @param projection2 projection of the second shape onto an axis, x = min, y = max
     */
    public static float getOverlap(Vector2 projection1, Vector2 projection2) {
        if (projection1.x >= projection2.y) {
            return projection2.y - projection1.x;
        }
        else {
            return projection1.x - projection2.y;
        }

    }


    // DIFFERENTIAL VECTOR BETWEEN a AND b
    public static Vector2 subVec(Vector2 a, Vector2 b) {
        return new Vector2(b.x - a.x, b.y - a.y);
    }

    // NORMAL VECTOR
    /*
     * Returns a normalized vector n perpendicular
     * to v.
     */
    public static Vector2 getNormal(Vector2 v) {
        return new Vector2(-v.y, v.x).nor();
    }


}
