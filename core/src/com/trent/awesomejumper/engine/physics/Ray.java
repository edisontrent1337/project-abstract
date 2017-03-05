package com.trent.awesomejumper.engine.physics;

import com.badlogic.gdx.math.Vector2;
import com.trent.awesomejumper.engine.entity.Entity;

/**
 * Created by Sinthu on 12.11.2016.
 */

public class Ray extends Entity {

    private Vector2 start;
    private Vector2 direction;

    private float length;

    public Ray() {
        this.start = new Vector2(0f,0f);
        this.direction = new Vector2(1f,0f);
    }

    public Ray(Vector2 start, Vector2 direction) {
        this.start = start;
        this.direction = direction;
    }


    public Vector2 getIntersection(Ray other) {
        return null;
    }


    public Vector2 getStart() {
        return start;
    }

    public Vector2 getDirection() {
        return direction;
    }

    public void update() {

    }


}
