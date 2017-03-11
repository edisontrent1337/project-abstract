package com.trent.awesomejumper.engine.physics;

import com.badlogic.gdx.math.Vector2;
import com.trent.awesomejumper.engine.entity.Entity;
import com.trent.awesomejumper.utils.Utils;

/**
 * Created by Sinthu on 12.11.2016.
 */

public class Ray extends Entity {

    private Vector2 start;
    private Vector2 dir, normalizedDir;
    private float startX, startY, xDir, yDir;

    public static final float INFINITE = Float.MAX_VALUE;

    private float length = 0f;

    public Ray() {
        this.start = new Vector2(0f,0f);
        this.dir = new Vector2(1f,0f);
        this.normalizedDir = new Vector2(1,0);
        this.startX = start.x;
        this.startY = start.y;
        this.xDir = normalizedDir.x;
        this.yDir = normalizedDir.y;
        this.length = INFINITE;
    }

    public Ray(float startX, float startY, float xDir, float yDir, float length) {
        this(new Vector2(startX,startY), new Vector2(xDir,yDir), length);
    }
    public Ray(Vector2 start, Vector2 direction) {
        this.start = start;
        this.startX = start.x;
        this.startY = start.y;
        this.dir = direction;
        this.normalizedDir = direction.cpy().nor();
        this.xDir = normalizedDir.x;
        this.yDir = normalizedDir.y;
        this.length = INFINITE;
    }

    public Ray(Vector2 start, Vector2 direction, float length) {
        this(start,direction);
        this.length = length;
    }


    public Intersection getIntersection(Ray other) {
        if(xDir == other.xDir && yDir == other.yDir)
            return new Intersection(null, false);

        float b = (xDir*(other.startY-startY) + yDir*(startX-other.startX)) / (other.xDir*yDir-other.yDir*xDir);
        float a = (other.startX + b*other.xDir-startX) / xDir;
        Vector2 result = null;
        boolean intersect = false;
        if(b <= other.length && a <= length) {
            result = other.start.cpy().add(other.normalizedDir.cpy().scl(b));
            intersect = true;
        }


        return new Intersection(result,intersect);
    }


    public Vector2 getStart() {
        return start;
    }

    public Vector2 getDir() {
        return dir;
    }

    public class Intersection {
        final Vector2 result;
        final boolean intersect;
        final String summary;
        public Intersection(Vector2 result, boolean intersect) {
            this.result = result;
            this.intersect = intersect;
            if(intersect)
                this.summary = "INTERSECTION AT: " + Utils.printVec(result);
            else
                this.summary = "NO INTERSECTION FOUND.";
        }

        @Override
        public String toString() {
            return this.summary;
        }

    }




}
