package com.trent.awesomejumper.engine.modelcomponents;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.trent.awesomejumper.models.Entity;
import com.trent.awesomejumper.testing.CollisionBox;

import java.util.ArrayList;

/**
 * Created by Sinthu on 09.12.2015.
 */
public class Body {
    // MEMBERS & INSTANCES
    // ---------------------------------------------------------------------------------------------

    // Represented entity
    private Entity entity;

    // Movement
    private Vector2 position;
    private Vector2 velocity;
    private Vector2 acceleration;

    // Dimensions
    private static final float DEFAULT_SIZE = 1f;
    private Rectangle bounds;

    // Hitboxes
    Array<CollisionBox> bodyHitboxes = new Array<CollisionBox>();


    public Body(Entity entity) {
        /**
         * Initialises all members with default values.
         * The constructor of the entity or the specific subclass then applies a more useful
         * start configuration to all values.
         */
        this.entity = entity;
        this.position = new Vector2(0f,0f);
        this.velocity = new Vector2(0f,0f);
        this.acceleration = new Vector2(0f,0f);
        this.bounds = new Rectangle(position.x, position.y, DEFAULT_SIZE, DEFAULT_SIZE);
        bodyHitboxes.clear();
    }



    public void update(float delta) {
        position.add(velocity.cpy().scl(delta));
        for(CollisionBox b : bodyHitboxes) {
            b.update(velocity.cpy().scl(delta));
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
    public void setPosition(float x, float y) {
        position.x = x;
        position.y = y;
    }
    public void setPositionX(float x) {
        position.x = x;
    }
    public void setPositionY(float y) {
        position.y = y;
    }

    // Velocity
    public Vector2 getVelocity() {
        return  velocity;
    }
    public float getVelocityX() {
        return velocity.x;
    }
    public float getVelocityY() {
        return velocity.y;
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
    public Array<CollisionBox> getBodyHitboxes() {
        return bodyHitboxes;
    }





}


