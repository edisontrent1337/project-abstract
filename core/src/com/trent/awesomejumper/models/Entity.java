package com.trent.awesomejumper.models;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.trent.awesomejumper.engine.modelcomponents.Body;
import com.trent.awesomejumper.engine.modelcomponents.Graphics;
import com.trent.awesomejumper.engine.modelcomponents.Health;
import com.trent.awesomejumper.engine.modelcomponents.Weapon;
import com.trent.awesomejumper.testing.CollisionBox;

/**
 * Created by Sinthu on 12.06.2015.
 */
public class Entity implements EntityInterface{

    // MEMBERS & INSTANCES
    // ---------------------------------------------------------------------------------------------

    public static int entityCount = 0;
    Body body;
    Graphics graphics;
    Health health;
    Weapon weapon;



    public enum State {
        IDLE, WALKING, JUMPING, FALLING, ATTACKING, DEAD
    }

    public float SIZE = 1f;
    public float hitPoints;
    public State state;

    public boolean facingL = false;

    public float entityTime;

    // CONSTRUCTOR
    // ---------------------------------b------------------------------------------------------------

    public Entity(){
        entityCount++;
    }


    // GETTER AND SETTER
    // ---------------------------------------------------------------------------------------------

    public Rectangle getBounds() {
        return body.getBounds();
    }
    public void setBounds(Rectangle bounds) {
        body.setBounds(bounds);
    }
    public void setBounds(float x, float y) {
        body.setBounds(x, y);

    }


    public Array<CollisionBox> getBodyHitboxes() {
        return body.getBodyHitboxes();
    }

    public void setBoundDimensions(float width, float height) {
        body.getBounds().width = width;
        body.getBounds().height = height;

    }

    public Vector2 getPosition() {
        return body.getPosition();
    }

    public void setPosition(Vector2 position) {
        body.setPosition(position);
    }

    public float getPositionX() {
        return body.getPositionX();
    }

    public void setPositionX(float x) {
        body.setPositionX(x);
    }

    public float getPositionY() {
        return body.getPositionY();
    }
    public void setPositionY(float y) {
        body.setPositionY(y);
    }

    public Vector2 getAcceleration() {
        return body.getAcceleration();
    }

    public void setAcceleration(Vector2 acceleration) {
        body.setAcceleration(acceleration);
    }

    public void setAccelX(float ax) {
        body.setAccelerationX(ax);
    }

    public void setAccelY(float ay) {
        body.setAccelerationY(ay);
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public float getWidth() {
        return body.getWidth();
    }

    public void setWidth(float width) {
        body.setWidth(width);
    }

    public Vector2 getVelocity() {
        return body.getVelocity();
    }

    public void setVelocity(float vx, float vy) {
        body.setVelocity(vx, vy);
    }

    public void setVelocityY(float vy) {
        body.setVelocityY(vy);
    }

    public void setVelocityX(float vx) {
        body.setVelocityX(vx);
    }

    public void setFacingL(boolean facingL) {
        this.facingL = facingL;
    }

    public float getEntityTime() {
        return entityTime;
    }

    public  void setHitboxes(Vector2 position) {}









    @Override
    public void update(float delta) {
        body.update(delta);
    }

    @Override
    public void render() {

    }

    @Override
    public void destroy() {

    }

    @Override
    public Body getBody() {
        return null;
    }

    @Override
    public void setBody(Body body) {

    }

    @Override
    public Graphics getGraphics() {
        return null;
    }

    @Override
    public void setGraphics(Graphics graphics) {

    }

    @Override
    public Health getHealth() {
        return null;
    }

    @Override
    public void setHealth(Health health) {

    }

    @Override
    public Weapon getWeapon() {
        return null;
    }

    @Override
    public void setWeapon(Weapon weapon) {

    }




}
