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

    private EntityManager() {

    }

    public static EntityManager getInstance() {
        return EntityManager.instance;
    }

    public static synchronized EntityManager createEntityManager() {
        if(EntityManager.instance == null)
            EntityManager.instance = new EntityManager();

        return instance;
    }


    // METHODS & FUNCTIONS
    // ---------------------------------------------------------------------------------------------

    public void setControllers(WorldContainer worldContainer, RenderingEngine renderingEngine) {
        this.worldContainer = worldContainer;
        this.renderingEngine = renderingEngine;
    }


    public void registerPickUp(Entity entity) {

        worldContainer.placeEntity(entity);
    }

    /**
     * Adds the entity to all relevant collections. Decides with the help of the entities type
     * to which collections the entity should be added.
     * @param entity
     */
    public void registerEntity(Entity entity) {

        /**
         * Add the entity to the main entity collection that holds all entities.
         */
        worldContainer.registerEntity(entity);
        renderingEngine.initGraphics(entity);

        /**
         * Add the entity to different sub collections which are used for specific collision
         * events such as collecting pickups or equipping weapons.
         */
        switch (entity.getType()) {
            case PICKUP_ENTITY:
                worldContainer.getPickups().add(entity);
                worldContainer.placeEntity(entity);
                entity.getBody().reset();
                break;
            case PROJECTILE_ENTITY:
                worldContainer.getProjectiles().add(entity);
                renderingEngine.initGraphics(entity);
                break;
            case REGULAR_ENTITY:
                break;
            case DROPPED_WEAPON_ENTITY:
                worldContainer.getWeaponDrops().add(entity);
                worldContainer.placeEntity(entity);
                entity.getBody().reset();
                break;
            default:
                break;
        }


    }


}
