package com.trent.awesomejumper.controller;

import com.trent.awesomejumper.engine.entity.Entity;

/**
 * Manages the registration of entities at runtime. Adds entities to specified collections of
 * worldContainer and loads its textures with renderingEngine.
 * Implemented as singleton.
 * Created by Sinthu on 01.03.2016.
 */
public class EntityManager {

    public static EntityManager instance = null;

    // MEMBERS & INSTANCES
    // ---------------------------------------------------------------------------------------------

    private WorldContainer worldContainer;
    private RenderingEngine renderingEngine;

    private EntityManager(WorldContainer worldContainer, RenderingEngine renderingEngine) {
        this.renderingEngine = renderingEngine;
        this.worldContainer = worldContainer;
    }

    public static EntityManager getInstance() {
        if(instance != null)
            return instance;
        else {
            throw new NullPointerException("EntityManager was not initialized.");
        }
    }

    public static EntityManager createEntityManager(WorldContainer worldContainer, RenderingEngine renderingEngine) {
        instance = new EntityManager(worldContainer,renderingEngine);
        return instance;
    }


    // METHODS & FUNCTIONS
    // ---------------------------------------------------------------------------------------------


    public void registerEntity(Entity entity) {
        worldContainer.registerEntity(entity);
        renderingEngine.initGraphics(entity);
    }

    // TODO: Implement this.
    public Entity getEntityByID() {
        return null;
    }

}
