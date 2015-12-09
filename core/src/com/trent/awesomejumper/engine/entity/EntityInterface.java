package com.trent.awesomejumper.engine.entity;

import com.trent.awesomejumper.engine.modelcomponents.*;

/**
 * Created by Sinthu on 09.12.2015.
 */
public interface EntityInterface {

    /**
     * Actions an entity can perform.
     */
    void update(float delta);
    void render();
    void destroy();

    Body getBody();
    void setBody(Body body);

    Graphics getGraphics();
    void setGraphics(Graphics graphics);

    Health getHealth();
    void setHealth(Health health);

    Weapon getWeapon();
    void setWeapon(Weapon weapon);





}
