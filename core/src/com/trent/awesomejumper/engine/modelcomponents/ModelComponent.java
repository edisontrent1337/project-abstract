package com.trent.awesomejumper.engine.modelcomponents;

import com.trent.awesomejumper.engine.entity.Entity;

/**
 * Created by Sinthu on 10.12.2015.
 */
public abstract class ModelComponent {

    protected Entity entity;

    /**
     * Enum used to identify components of entities.
     */
    public enum ComponentID {
        BODY,
        GRAPHICS,
        HEALTH,
        WEAPON_INVENTORY,
        WEAPON_COMPONENT;
    }


    public ModelComponent() {

    }


}
