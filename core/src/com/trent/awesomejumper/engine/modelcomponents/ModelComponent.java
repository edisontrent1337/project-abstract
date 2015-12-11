package com.trent.awesomejumper.engine.modelcomponents;

import com.trent.awesomejumper.engine.entity.Entity;

/**
 * Created by Sinthu on 10.12.2015.
 */
public abstract class ModelComponent {

    protected Entity entity;

    public ModelComponent() {

    }

    public ModelComponent(Entity entity) {
        this.entity = entity;
    }

}
