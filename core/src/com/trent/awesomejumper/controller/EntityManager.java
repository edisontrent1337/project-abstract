package com.trent.awesomejumper.controller;

import com.badlogic.gdx.math.Vector2;
import com.trent.awesomejumper.engine.entity.Entity;
import com.trent.awesomejumper.engine.entity.EntityInterface;

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

    public void registerPickUp(Entity entity) {

        worldContainer.placeEntity(entity);
    }


    public void registerEntity(Entity entity, EntityInterface.Type type) {

        /**
         * Add the entity to the main entity collection that holds all entities.
         */
        worldContainer.registerEntity(entity);

        /**
         * Add the entity to different sub collections which are used for specific collision
         * events such as collecting pickups or equipping weapons.
         */
        switch (type) {
            case PICKUP:
                worldContainer.getPickups().add(entity);
                worldContainer.placeEntity(entity);
                renderingEngine.initGraphics(entity);
                break;
            case PROJECTILE:
                worldContainer.getProjectiles().add(entity);
                renderingEngine.initGraphics(entity);
                break;
            case ENEMY:
                break;
            case WEAPON:
                worldContainer.getWeaponDrops().add(entity);
                worldContainer.placeEntity(entity);
                entity.getBody().enableCollisionDetection();
                entity.getBody().setAngleOfRotation(0f);
                entity.getBody().setAimReference(new Vector2(0f,0f));
                break;
            default:
                break;
        }


    }

    // TODO: Implement this.
    public Entity getEntityByID() {
        return null;
    }

}
