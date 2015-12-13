package com.trent.awesomejumper.engine.modelcomponents;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.trent.awesomejumper.engine.entity.Entity;
import com.trent.awesomejumper.engine.physics.CollisionBox;

import java.util.HashMap;

/**
 * Created by Sinthu on 09.12.2015.
 * Body component implementation. Holds information about position, acceleration, velocity and
 * dimensions of entities. Also holds the hitbox skeleton used to calculate damage.
 */
public class Body extends ModelComponent{

    // MEMBERS & INSTANCES
    // ---------------------------------------------------------------------------------------------


    // Movement
    private Vector2 position;
    private Vector2 velocity;
    private Vector2 acceleration;

    //private Rectangle bounds;
    private CollisionBox bounds;

    // Hitboxes
    // TODO: Either address this array with an enum to get Head, Arm, Leg etc... or
    // TODO: use a HashMap where the key represents the kind of hitbox.
    Array<CollisionBox> hitboxSkeleton = new Array<>();

    HashMap<String, CollisionBox> skeleton = new HashMap<>();


    public Body(Entity entity, float width, float height) {
        /**
         * Initialises all members with default values.
         * The constructor of the entity or the specific subclass then applies a more useful
         * start configuration to all values.
         */
        this.entity = entity;
        entity.hasBody = true;
        this.position = new Vector2(0f,0f);
        this.velocity = new Vector2(0f,0f);
        this.acceleration = new Vector2(0f,0f);
        this.bounds = new CollisionBox(position, width, height);
        hitboxSkeleton.clear();
    }


    public void update(float delta) {
        position.add(velocity.cpy().scl(delta));
        // update outer general bounds
        bounds.update(position);
        // update skeleton
        for(CollisionBox b : hitboxSkeleton) {
            b.update(position);
        }

    }


    // GETTER AND SETTER
    // ---------------------------------------------------------------------------------------------

    // Entity
    public Entity getEntity() {
        return entity;
    }

    // Position
    public Vector2 getPosition() {
        return position;
    }
    public void setPosition(Vector2 position) {
        this.position = position;
    }

    public void setPositionX(float x) {
        position.x = x;
    }
    public void setPositionY(float y) {
        position.y = y;
    }

    // Velocity
    public Vector2 getVelocity() {
        return velocity;
    }

    public void setVelocity(Vector2 velocity) {
        this.velocity = velocity;
    }
    public void setVelocity(float x, float y) {
        this.velocity = new Vector2(x,y);
    }
    public void setVelocityX(float x) {
        velocity.x = x;
    }
    public void setVelocityY(float y) {
        velocity.y = y;
    }

    // Acceleration
    public Vector2 getAcceleration() {
        return acceleration;
    }
    public float getAccelerationX() {
        return acceleration.x;
    }
    public float getAccelerationY() {
        return acceleration.y;
    }
    public void setAcceleration(Vector2 acceleration) {
        this.acceleration = acceleration;
    }
    public void setAcceleration(float x, float y) {
        acceleration.x = x;
        acceleration.y = y;
    }
    public void setAccelerationX(float x) {
        acceleration.x = x;
    }
    public void setAccelerationY(float y) {
        acceleration.y = y;
    }

    // Dimensions
    public float getWidth() {
        return bounds.getWidth();
    }
    public float getHeight(){
        return bounds.getHeight();
    }
    public CollisionBox getBounds() {
        return bounds;
    }
    public void setWidth(float width) {
        bounds.setWidth(width);
    }
    public void setHeight(float height) {
        bounds.setHeight(height);
    }
    public void setBounds(CollisionBox bounds) {
        this.bounds = bounds;
    }
    public void setBounds(float x, float y) {
        bounds.setPosition(x,y);
    }

    // Hitboxes
    public Array<CollisionBox> getHitboxSkeleton() {
        return hitboxSkeleton;
    }

    /**
     * Adds a collisionBox to the skeleton of the entity.
     * @param box
     */
    public void add(CollisionBox box) {
        hitboxSkeleton.add(box);
    }



}


