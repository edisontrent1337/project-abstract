package com.trent.awesomejumper.engine.entity;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.trent.awesomejumper.engine.modelcomponents.Body;
import com.trent.awesomejumper.engine.modelcomponents.Graphics;
import com.trent.awesomejumper.engine.modelcomponents.Health;
import com.trent.awesomejumper.engine.modelcomponents.ModelComponent;
import com.trent.awesomejumper.engine.modelcomponents.popups.PopUpFeed;
import com.trent.awesomejumper.engine.modelcomponents.Weapon;
import com.trent.awesomejumper.engine.physics.CollisionBox;

import java.util.HashMap;

/**
 * Created by Sinthu on 12.06.2015.
 */
public class Entity implements EntityInterface {

    // MEMBERS & INSTANCES
    // ---------------------------------------------------------------------------------------------

    public enum ComponentIndex {
        BODY(0),
        GRAPHICS(1),
        HEALTH(2);

        private int value;

        ComponentIndex(int value) {
            this.value = value;
        }
    }

    public static int entityCount = 0;
    public boolean hasBody = false, hasGraphics = false, hasHealth = false, hasWeapon = false;
    protected Body body;
    protected Graphics graphics;
    protected Health health;
    protected Weapon weapon;
    protected PopUpFeed popUpFeed;

    // TODO: Idea on how to eliminate all getters for components.
    protected HashMap<ComponentIndex,ModelComponent> modelComponents;


    public enum State {
        IDLE(0),
        WALKING(1),
        JUMPING(2),
        FALLING(3),
        ATTACKING(4),
        DEAD(5);

        private final int value;
        State(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

    }

    public float hitPoints;
    public State state;

    public boolean facingL = false;

    public float time;

    // CONSTRUCTOR
    // ---------------------------------b------------------------------------------------------------

    public Entity(){
        entityCount++;
    }


    // GETTER AND SETTER
    // ---------------------------------------------------------------------------------------------

    public CollisionBox getBounds() {
        return body.getBounds();
    }
    public void setBounds(float x, float y) {
        body.setBounds(x, y);

    }

    public Array<CollisionBox> getBodyHitboxes() {
        return body.getHitboxSkeleton();
    }

    public Vector2 getPosition() {
        return body.getPosition();
    }

    public void setPosition(Vector2 position) {
        body.setPosition(position);
    }



    public void setPositionX(float x) {
        body.setPositionX(x);
    }


    public void setPositionY(float y) {
        body.setPositionY(y);
    }

    public Vector2 getAcceleration() {
        return body.getAcceleration();
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

    public float getHeight() {
        return body.getHeight();
    }

    public void setHeight(float height) {
        body.setHeight(height);
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



    @Override
    public void update(float delta) {
        time += delta;
        if(hasBody)
            body.update(delta);
    }

    @Override
    public void render(SpriteBatch spriteBatch) {
        if(hasGraphics)
            graphics.render(spriteBatch);
    }

    @Override
    public void destroy() {

    }

    @Override
    public Body getBody() {
        return body;
    }

    @Override
    public void setBody(Body body) {

    }

    @Override
    public Graphics getGraphics() {
        return graphics;
    }

    @Override
    public void setGraphics(Graphics graphics) {

    }

    @Override
    public Health getHealth() {
        return health;
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

    @Override
    public PopUpFeed getPopUpFeed() {
        return popUpFeed;
    }

    @Override
    public void setPopUpFeed(PopUpFeed popUpFeed) {
        this.popUpFeed = popUpFeed;
    }


}
