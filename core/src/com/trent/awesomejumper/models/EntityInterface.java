package com.trent.awesomejumper.models;

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

    com.trent.awesomejumper.engine.modelcomponents.Body getBody();
    void setBody(com.trent.awesomejumper.engine.modelcomponents.Body body);

    com.trent.awesomejumper.engine.modelcomponents.Graphics getGraphics();
    void setGraphics(com.trent.awesomejumper.engine.modelcomponents.Graphics graphics);

    com.trent.awesomejumper.engine.modelcomponents.Health getHealth();
    void setHealth(com.trent.awesomejumper.engine.modelcomponents.Health health);

    com.trent.awesomejumper.engine.modelcomponents.Weapon getWeapon();
    void setWeapon(com.trent.awesomejumper.engine.modelcomponents.Weapon weapon);





}
