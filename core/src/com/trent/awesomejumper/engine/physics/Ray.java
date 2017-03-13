package com.trent.awesomejumper.engine.physics;

import com.badlogic.gdx.math.Vector2;
import com.trent.awesomejumper.utils.Utils;

/**
 * Ray class used to resolve entity / projectile collisions and world / projectile collisions.
 * Created by Sinthu on 12.11.2016.
 */

public class Ray {

    // ---------------------------------------------------------------------------------------------
    // MEMBERS & INSTANCES
    // ---------------------------------------------------------------------------------------------

    private Vector2 origin;                                     // start of the ray
    private Vector2 dir;                                        // direction the ray travels
    private float originX, originY, xDir, yDir;                 // atomic components
    public static final float INFINITE = Float.MAX_VALUE;       // static constant for infinite length
    private float length = 0f;                                  // default length is 0

    // ---------------------------------------------------------------------------------------------
    // CONSTRUCTOR
    // ---------------------------------------------------------------------------------------------
    public Ray() {
        this.origin = new Vector2(0f,0f);
        this.dir = new Vector2(1f,0f);
        this.originX = origin.x;
        this.originY = origin.y;
        this.xDir = dir.cpy().nor().x;
        this.yDir = dir.cpy().nor().y;
        this.length = INFINITE;
    }

    public Ray(float startX, float startY, float xDir, float yDir, float length) {
        this(new Vector2(startX,startY), new Vector2(xDir,yDir), length);
    }
    public Ray(Vector2 start, Vector2 direction) {
        this.origin = start;
        this.originX = start.x;
        this.originY = start.y;
        this.dir = direction.cpy().nor();
        this.xDir = dir.x;
        this.yDir = dir.y;
        this.length = INFINITE;
    }

    public Ray(Vector2 start, Vector2 direction, float length) {
        this(start,direction);
        this.length = length;
    }

    // ---------------------------------------------------------------------------------------------
    // METHODS & FUNCTIONS
    // ---------------------------------------------------------------------------------------------
    /**
     * Calculates an intersection between this ray and another ray.
     * If an intersection occurs, the returned intersection object will contain the point of
     * intersection and the distance to the rays origin.
     * @param other other ray
     * @return intersection object
     */
    public Intersection getIntersection(Ray other) {
        // If the two rays are parallel, no intersection can occur.
        if(xDir == other.xDir && yDir == other.yDir)
            return new Intersection(null, false, Float.MAX_VALUE, other);

        // Calculate the coefficient for the other ray.
        float b = (xDir*(other.originY - originY) + yDir*(originX -other.originX)) / (other.xDir*yDir-other.yDir*xDir);
        // Calculate the coefficient for this ray.
        float a = (other.originX + b*other.xDir- originX) / xDir;
        Vector2 result = null;
        boolean intersect = false;
        // Only if b and a are smaller than the lengths of the two rays, the intersection lays on
        // both rays.
        if(Math.abs(b) <= other.length && Math.abs(a) <= length) {
            result = origin.cpy().add(dir.cpy().scl(a));
            intersect = true;
        }

        return new Intersection(result,intersect,a, other);
    }


    // ---------------------------------------------------------------------------------------------
    // GETTER & SETTER
    // ---------------------------------------------------------------------------------------------
    public Vector2 getOrigin() {
        return origin;
    }

    public void setOrigin(Vector2 origin) {
        this.origin = origin;
    }
    public Vector2 getDir() {
        return dir;
    }

    public float getLength() {
        return length;
    }

    @Override
    public String toString() {
        return "START: " + origin.toString() + "DIR: " + dir.toString() + "LENGTH: " + length;
    }


    /**
     * Inner class that represents an intersection between two rays.
     */
    public class Intersection implements Comparable<Intersection> {
        public final Vector2 result;                            // intersection point
        public final boolean intersect;                         // is there an intersection?
        public final String summary;                            // contains all information
        public final float distance;                            // distance in which intersection occurs
        public final Ray origin;                                // original ray


        // -----------------------------------------------------------------------------------------
        // CONSTRUCTOR
        // -----------------------------------------------------------------------------------------
        Intersection(Vector2 result, boolean intersect, float distance, Ray origin) {
            this.result = result;
            this.intersect = intersect;
            this.distance = distance;
            this.origin = origin;
            if(intersect && result!=null)
                this.summary = "INTERSECTION AT: " + Utils.printVec(result) + " DST: " + distance;
            else
                this.summary = "NO INTERSECTION FOUND.";
        }


        @Override
        public String toString() {
            return this.summary;
        }


        @Override
        public int compareTo(Intersection o) {
            if(Math.abs(this.distance)< Math.abs(o.distance))
                return -1;
            else if(Math.abs(this.distance) > Math.abs(o.distance))
                return 1;
            return 0;
        }
    }




}
