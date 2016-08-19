package com.trent.awesomejumper.engine.entity;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.trent.awesomejumper.engine.modelcomponents.*;

/**
 * Created by Sinthu on 09.12.2015.
 */
public interface EntityInterface {


    /**
     * General category of entities.
     * This enum is used by the CollisionController
     */
    enum Type {
        REGULAR_ENTITY(0),
        PICKUP_ENTITY(1),
        DROPPED_WEAPON_ENTITY(2),
        PROJECTILE_ENTITY(3);


        private final int value;
        Type(int value) {
            this.value = value;
        }
        public int getValue() {
            return value;
        }
    }


    /**
     * Actions an entity can perform.
     */
    void update(float delta);
    void render(SpriteBatch spriteBatch);
    void destroy();
    void register();
    boolean isAlive();

    Body getBody();
    void setBody(Body body);

    Graphics getGraphics();
    void setGraphics(Graphics graphics);

    Health getHealth();
    void setHealth(Health health);

    WeaponInventory getWeaponInventory();
    void setWeaponInventory(WeaponInventory weaponInventory);
    void updateWeaponPositions();



    Type getType();



}
