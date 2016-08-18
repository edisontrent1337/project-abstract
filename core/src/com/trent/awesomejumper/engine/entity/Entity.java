package com.trent.awesomejumper.engine.entity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.trent.awesomejumper.controller.EntityManager;
import com.trent.awesomejumper.engine.modelcomponents.Body;
import com.trent.awesomejumper.engine.modelcomponents.Graphics;
import com.trent.awesomejumper.engine.modelcomponents.Health;
import com.trent.awesomejumper.engine.modelcomponents.WeaponComponent;
import com.trent.awesomejumper.engine.modelcomponents.WeaponInventory;
import com.trent.awesomejumper.engine.modelcomponents.popups.PopUpFeed;
import com.trent.awesomejumper.engine.physics.CollisionBox;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Sinthu on 12.06.2015.
 */
public class Entity implements EntityInterface {

    // MEMBERS & INSTANCES
    // ---------------------------------------------------------------------------------------------

    private static AtomicInteger idCounter = new AtomicInteger();

    private final int ID;

    public enum ComponentIndex {
        BODY(0),
        GRAPHICS(1),
        HEALTH(2),
        POP_UPS(3),
        ;

        private int value;

        ComponentIndex(int value) {
            this.value = value;
        }
    }


    public static int entityCount = 0;
    public boolean hasBody = false, hasGraphics = false, hasHealth = false, hasWeaponInventory = false,
    hasPopUps = false, hasWeaponComponent = false;
    protected Body body;
    protected Graphics graphics;
    protected Health health;
    protected WeaponInventory weaponInventory;
    protected WeaponComponent weaponComponent;
    protected PopUpFeed popUpFeed;

    protected Entity owner;


    protected Type type;

    public enum State {
        IDLE(0),
        WALKING(1),
        JUMPING(2),
        FALLING(3),
        ATTACKING(4),
        HIT(5),
        DEAD(6);

        private final int value;
        State(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

    }

    public State state;

    public boolean facingL = false;
    private boolean alive = true;

    public float time;
    public float registerTime;

    // CONSTRUCTOR
    // ---------------------------------------------------------------------------------------------

    public Entity(){
        entityCount++;
        ID = createID();
        owner = this;
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
        return body.getWidthX();
    }

    public void setWidth(float width) {
        body.setWidthX(width);
    }

    public float getHeight() {
        return body.getWidthY();
    }

    public void setHeight(float height) {
        body.setWidthY(height);
    }

    public Vector2 getVelocity() {
        return body.getVelocity();
    }
    public float getMaxVelocity() {
        return body.getMaxVelocity();
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


    // LIFE AND DEATH
    // ---------------------------------------------------------------------------------------------

    @Override
    public void destroy() {
        alive = false;
        entityCount--;
    }
    @Override
    public boolean isAlive() {
        return alive;
    }

    // BODY
    // ---------------------------------------------------------------------------------------------

    @Override
    public Body getBody() {
        return body;
    }

    @Override
    public void setBody(Body body) {

    }

    @Override
    public void update(float delta) {
        time += delta;

        if(hasBody)
            body.update(delta);

    }

    // GRAPHICS
    // ---------------------------------------------------------------------------------------------

    @Override
    public Graphics getGraphics() {
        return graphics;
    }

    @Override
    public void setGraphics(Graphics graphics) {

    }

    @Override
    public void render(SpriteBatch spriteBatch) {
        if(hasGraphics)
            graphics.render(spriteBatch);
    }

    public void hide() {
        if(hasGraphics)
            graphics.hide();

    }

    public void show() {
        if(hasGraphics)
            graphics.show();
    }
    // HEALTH
    // ---------------------------------------------------------------------------------------------

    @Override
    public Health getHealth() {
        return health;
    }

    @Override
    public void setHealth(Health health) {

    }

    // WEAPON INVENTORY
    // ---------------------------------------------------------------------------------------------

    @Override
    public WeaponInventory getWeaponInventory() {
        return weaponInventory;
    }

    @Override
    public void setWeaponInventory(WeaponInventory weaponInventory) {

    }

    @Override
    public void updateWeaponPositions() {
        if(hasWeaponInventory)
            weaponInventory.updateWeaponPositions();
    }

    // POP UP RENDERING
    // ---------------------------------------------------------------------------------------------

    @Override
    public PopUpFeed getPopUpFeed() {
        return popUpFeed;
    }

    @Override
    public void setPopUpFeed(PopUpFeed popUpFeed) {
        this.popUpFeed = popUpFeed;
    }


    // REGISTER ENTITY IN GAME SYSTEM AT RUNTIME
    // ---------------------------------------------------------------------------------------------

    /**
     * Adds this entity to all relevant collections and loads textures for it. This method shall be
     * called whenever new entities are created at runtime.
     */
    @Override
    public void register() {

        EntityManager.getInstance().registerEntity(this);
    }

    // REGISTER ENTITY IN GAME SYSTEM AS DROP
    // ---------------------------------------------------------------------------------------------


    private int createID() {
        return idCounter.incrementAndGet();
    }

    public int getID() {
        return ID;
    }


    // TYPE OF ENTITY
    // ---------------------------------------------------------------------------------------------
    public Type getType() {
        return type;
    }

    public void setOwner(Entity owner) {
        this.owner = owner;
    }

    public Entity getOwner() {
        return owner;
    }


}
