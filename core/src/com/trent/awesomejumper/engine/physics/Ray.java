package com.trent.awesomejumper.engine.physics;

import com.badlogic.gdx.math.Vector2;
import com.trent.awesomejumper.controller.entitymanagement.EntityManager;
import com.trent.awesomejumper.engine.entity.Entity;
import com.trent.awesomejumper.models.projectile.Projectile;
import com.trent.awesomejumper.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;


/**
 * Ray class used to resolve entity / projectile collisions and world / projectile collisions.
 * Created by Sinthu on 12.11.2016.
 */

public class Ray {

    // ---------------------------------------------------------------------------------------------
    // MEMBERS & INSTANCES
    // ---------------------------------------------------------------------------------------------

    protected Vector2 origin;                                   // start of the ray
    protected Vector2 dir;                                      // direction the ray travels
    protected float originX, originY, xDir, yDir;               // atomic components
    public static final float INFINITE = Float.MAX_VALUE;       // static constant for infinite length
    protected float length = 0f;                                // default length is 0

    protected HashMap<Integer, Vector2> penetratedEntities = new HashMap<>();
    protected ArrayList<Vector2> hitHashCells = new ArrayList<>();
    protected ArrayList<Vector2> penetrations = new ArrayList<>();
    protected boolean active = true;

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

    public Ray(float originX, float originY, float xDir, float yDir, float length) {
        this(new Vector2(originX,originY), new Vector2(xDir,yDir), length);

    }
    public Ray(Vector2 origin, Vector2 direction) {
        this.origin = origin;
        this.originX = origin.x;
        this.originY = origin.y;
        this.dir = direction.cpy().nor();
        this.xDir = dir.x;
        this.yDir = dir.y;
        this.length = INFINITE;
    }

    public Ray(Vector2 origin, Vector2 direction, float length) {
        this(origin,direction);
        if(length < 0)
            throw new IllegalArgumentException("THE LENGTH OF A RAY MUST BE GREATER THAN 0");
        else {
            this.length = length;
        }
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
        float otherCoefficient = (xDir*(other.originY - originY) + yDir*(originX -other.originX)) / (other.xDir*yDir-other.yDir*xDir);
        // Calculate the coefficient for this ray.
        float originCoefficient = (other.originX + otherCoefficient*other.xDir- originX) / xDir;
        Vector2 result = null;

        /*Utils.log("THIS RAY", this.toString());
        Utils.log("GENERATED COEFFICIENT FOR THIS",originCoefficient);
        Utils.log("ORIGIN LENGTH",this.length);
        Utils.log("OTHER RAY", other.toString());
        Utils.log("GENERATED COEFFICIENT FOR OTHER",otherCoefficient);
        Utils.log("OTHERS LENGTH",other.length);*/


        // Only if b and a are smaller than the lengths of the two rays and both positive, the intersection lays on
        // both rays on the desired direction.
        if(otherCoefficient <= other.length && originCoefficient <= length && otherCoefficient > 0 && originCoefficient >= 0) {
            result = origin.cpy().add(dir.cpy().scl(originCoefficient));
            // result is rounded to avoid errors
            result.x = Math.round(result.x*10000.0f)/10000f;
            result.y = Math.round(result.y*10000.0f)/10000f;
            return new Intersection(result,true,originCoefficient,other);
        }

        return new Intersection(result,false, originCoefficient, other);
    }

    public void addHitHashCell(Vector2 index) {
        hitHashCells.add(index);
    }

    public void addEntityPenetrationPoint(Vector2 point) {
        penetrations.add(point);
    }

    public void addPenetratedEntity(int id, Vector2 point) {
        penetratedEntities.put(id,point);
    }

    public void register() {
        EntityManager.getInstance().registerRay(this);
    }

    public void destroy() {
        active = false;
    }

    // ---------------------------------------------------------------------------------------------
    // GETTER & SETTER
    // ---------------------------------------------------------------------------------------------
    public boolean isActive() {
        return active;
    }
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

    public ArrayList<Vector2> getHitHashCells() {
        return hitHashCells;
    }
    public ArrayList<Vector2> getPenetrations() {
        return penetrations;
    }

    public HashMap<Integer,Vector2> getPenetratedEntities() {
        return penetratedEntities;
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
                this.summary = "INTERSECTION AT: " + Utils.printVec(result) + " DST: " + distance + "RESPONS. RAY: " + origin.toString();
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
