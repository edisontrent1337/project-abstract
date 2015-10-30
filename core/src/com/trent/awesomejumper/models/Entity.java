package com.trent.awesomejumper.models;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.trent.awesomejumper.testing.CollisionBox;

/**
 * Created by Sinthu on 12.06.2015.
 */
public abstract class Entity {

    // MEMBERS & INSTANTCES
    // ---------------------------------------------------------------------------------------------

    Vector2 position = new Vector2();
    Vector2 center = new Vector2();
    Vector2 acceleration = new Vector2();
    Vector2 velocity = new Vector2();
    private Rectangle bounds;
    private CollisionBox collisionBox;
    //Array<Rectangle> body = new Array<>();
    Array<CollisionBox> body = new Array<>();

    private final float DEFAULT_SIZE = 1f;


    public enum State {
        IDLE, WALKING, JUMPING, FALLING, ATTACKING, DEAD
    }

    public final float SIZE;
    public float hitPoints;
    public State state;

    private boolean onGround = false;
    public boolean facingL = false;

    public float entityTime;

    // CONSTRUCTOR
    // ---------------------------------b------------------------------------------------------------

    public Entity(Vector2 position) {
        this.SIZE = DEFAULT_SIZE;
        this.hitPoints = 100f;
        this.state = State.IDLE;
        this.position = position;
        this.onGround = true;
        this.bounds = new Rectangle();
        this.collisionBox = new CollisionBox(position, DEFAULT_SIZE, DEFAULT_SIZE);
        this.center = new Vector2(position.x + 0.5f*DEFAULT_SIZE, position.y + 0.5f*DEFAULT_SIZE);
    }


    public Entity(Vector2 position, float size, float hitPoints) {
        this.position = position;
        this.SIZE = size;
        this.center = new Vector2(position.x + 0.5f*SIZE, position.y + 0.5f*SIZE);
        this.state = State.IDLE;
        this.hitPoints = hitPoints;
        this.bounds = new Rectangle(position.x, position.y, size, size);
        this.collisionBox = new CollisionBox(position,size,size);
        this.entityTime = 0f;
        Gdx.app.log("WORLD EVENT:   ", this.getClass().toString() + " INSTANTIATED AT" + position.x + "," + position.y + ";" + hitPoints + "(HP)" + "[" + state + "]");

    }

    // GETTER AND SETTER
    // ---------------------------------------------------------------------------------------------

    public Rectangle getBounds() {
        return bounds;
    }
    public void setBounds(Rectangle bounds) {
        this.bounds = bounds;
    }
    public void setBounds(float x, float y) {
        this.bounds.x = x;
        this.bounds.y = y;
    }


    public CollisionBox getCollisionBox() {
        return collisionBox;
    }

    public Array<CollisionBox> getBody() {
        return body;
    }

    public void setBoundDimensions(float width, float height) {
        this.bounds.width = width;
        this.bounds.height = height;

    }

    public Vector2 getCenter() {
        return center;
    }


    public Vector2 getPosition() {
        return position;
    }
    public void setPosition(Vector2 position) {
        this.position = position;
    }

    public float getPositionX() {
        return position.x;
    }
    public void setPositionX(float x) {
        this.position.x = x;
    }

    public float getPositionY() {
        return position.y;
    }

    public void setPositionY(float y) {
        this.position.y = y;
    }

    public Vector2 getAcceleration() {
        return acceleration;
    }

    public void setAcceleration(Vector2 acceleration) {
        this.acceleration = acceleration;
    }

    public void setAccelX(float ax) {
        this.acceleration.x = ax;
    }

    public void setAccelY(float ay) {
        this.acceleration.y = ay;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public float getWidth() {
        return bounds.getWidth();
    }

    public void setWidth(float width) {
        bounds.setWidth(width);
    }

    public Vector2 getVelocity() {
        return velocity;
    }

    public void setVelocity(float vx, float vy) {
        this.velocity.x = vx;
        this.velocity.y = vy;
    }

    public void setVelocityY(float vy) {
        this.velocity.y = vy;
    }

    public void setVelocityX(float vx) {
        this.velocity.x = vx;
    }

    public void groundEntity() {
        onGround = true;
    }

    public void elevateEntity() {
        onGround = false;
    }

    public boolean isOnGround() {
        return onGround;
    }

    public void setFacingL(boolean facingL) {
        this.facingL = facingL;
    }

    public float getEntityTime() {
        return entityTime;
    }

    public abstract void update(float delta);
    public abstract void setHitboxes(Vector2 position);



}
