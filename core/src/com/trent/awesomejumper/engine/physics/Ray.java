package com.trent.awesomejumper.engine.physics;

import com.badlogic.gdx.math.Vector2;
import com.trent.awesomejumper.engine.entity.Entity;
import com.trent.awesomejumper.utils.Utils;

/**
 * Created by Sinthu on 12.11.2016.
 */

public class Ray {

    private Vector2 start;
    private Vector2 dir;
    private float startX, startY, xDir, yDir;

    public static final float INFINITE = Float.MAX_VALUE;

    private float length = 0f;

    public Ray() {
        this.start = new Vector2(0f,0f);
        this.dir = new Vector2(1f,0f);
        this.startX = start.x;
        this.startY = start.y;
        this.xDir = dir.cpy().nor().x;
        this.yDir = dir.cpy().nor().y;
        this.length = INFINITE;
    }

    public Ray(float startX, float startY, float xDir, float yDir, float length) {
        this(new Vector2(startX,startY), new Vector2(xDir,yDir), length);
    }
    public Ray(Vector2 start, Vector2 direction) {
        this.start = start;
        this.startX = start.x;
        this.startY = start.y;
        this.dir = direction.cpy().nor();
        this.xDir = dir.x;
        this.yDir = dir.y;
        this.length = INFINITE;
    }

    public Ray(Vector2 start, Vector2 direction, float length) {
        this(start,direction);
        this.length = length;
    }


    public Intersection getIntersection(Ray other) {
        // If the two rays are parallel, no intersection can occur.
        if(xDir == other.xDir && yDir == other.yDir)
            return new Intersection(null, false, Float.MAX_VALUE, other);

        // Calculate the coefficient for the other ray.
        float b = (xDir*(other.startY-startY) + yDir*(startX-other.startX)) / (other.xDir*yDir-other.yDir*xDir);
        // Calculate the coefficient for this ray.
        float a = (other.startX + b*other.xDir-startX) / xDir;
        Vector2 result = null;
        boolean intersect = false;
        // Only if b and a are smaller than the lengths of the two rays, the intersection lays on
        // both rays.
        if(b <= other.length && a <= length) {
            result = start.cpy().add(dir.cpy().scl(a));
            intersect = true;
        }

        return new Intersection(result,intersect,a, other);
    }


    public Vector2 getStart() {
        return start;
    }

    public void setStart(Vector2 start) {
        this.start = start;
    }
    public Vector2 getDir() {
        return dir;
    }

    public float getLength() {
        return length;
    }

    @Override
    public String toString() {
        return "START: " + start.toString() + "DIR: " + dir.toString();
    }


    public class Intersection {
        public final Vector2 result;
        public final boolean intersect;
        public final String summary;
        public final float distance;
        public final Ray origin;
        public Intersection(Vector2 result, boolean intersect, float distance, Ray origin) {
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


    }




}
