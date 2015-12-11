package com.trent.awesomejumper.engine.modelcomponents;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.trent.awesomejumper.engine.entity.Entity;
import com.trent.awesomejumper.engine.physics.CollisionBox;

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

    // Dimensions
    private static final float DEFAULT_SIZE = 1f;
    private Rectangle bounds;

    // Hitboxes
    Array<CollisionBox> hitboxSkeleton = new Array<>();


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
        this.bounds = new Rectangle(position.x, position.y, width, height);
        hitboxSkeleton.clear();
    }



    public void update(float delta) {
        position.add(velocity.cpy().scl(delta));
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
    public float getPositionX() {
        return position.x;
    }
    public float getPositionY() {
        return position.y;
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
    public Rectangle getBounds() {
        return bounds;
    }
    public void setWidth(float width) {
        bounds.width = width;
    }
    public void setHeight(float height) {
        bounds.height = height;
    }
    public void setBounds(Rectangle bounds) {
        this.bounds = bounds;
    }
    public void setBounds(float x, float y) {
        bounds.x = x;
        bounds.y = y;
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


