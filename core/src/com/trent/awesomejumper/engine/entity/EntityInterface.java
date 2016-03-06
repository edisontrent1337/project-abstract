package com.trent.awesomejumper.engine.entity;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.trent.awesomejumper.engine.modelcomponents.*;
import com.trent.awesomejumper.engine.modelcomponents.popups.PopUpFeed;

/**
 * Created by Sinthu on 09.12.2015.
 */
public interface EntityInterface {


    /**
     * Actions an entity can perform.
     */
    void update(float delta);
    void render(SpriteBatch spriteBatch);
    void destroy();
    void registerEntity();
    void dropToWorld();
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

    PopUpFeed getPopUpFeed();
    void setPopUpFeed(PopUpFeed feed);





}
