package com.trent.awesomejumper.controller;

import com.badlogic.gdx.Gdx;
import com.trent.awesomejumper.controller.rendering.RenderingEngine;
import com.trent.awesomejumper.engine.entity.Entity;
import com.trent.awesomejumper.models.Player;
import com.trent.awesomejumper.models.lootable.Lootable;
import com.trent.awesomejumper.models.projectile.Projectile;
import com.trent.awesomejumper.models.weapons.Weapon;

import static com.trent.awesomejumper.engine.modelcomponents.ModelComponent.ComponentID.HEALTH;
/**
 * Manages the registration of entities at runtime. Adds entities to specified collections of
 * worldContainer and loads its textures with renderingEngine.
 * Implemented as singleton. Can be seen as the interface that connects loading graphics of
 * entities and placing them in the world.
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


    /**
     * Adds the entity to all relevant collections. Decides with the help of the entities type
     * to which collections the entity should be added.
     */

    /**
     * Register projectile
     * @param projectile
     */
    public void registerEntity(Projectile projectile) {
        worldContainer.registerEntity(projectile);
        worldContainer.getProjectiles().add(projectile);
        renderingEngine.initGraphics(projectile);
    }

    /**
     * Register weapon
     * @param weapon
     */
    public void registerEntity(Weapon weapon) {
        Gdx.app.log("EVENT", "REGISTRED WEAPON");
        worldContainer.registerEntity(weapon);
        worldContainer.getWeaponDrops().add(weapon);
        worldContainer.getMobileEntities().add(weapon);
        worldContainer.placeEntity(weapon, weapon.getOwner().getBounds().getPositionAndOffset());
        weapon.getBody().reset();
        renderingEngine.initGraphics(weapon);
    }

    /**
     * Register lootable
     * @param lootable
     */
    public void registerEntity(Lootable lootable) {
        worldContainer.registerEntity(lootable);
        worldContainer.getMobileEntities().add(lootable);
        if(lootable.has(HEALTH))
            worldContainer.getLivingEntities().add(lootable);
        renderingEngine.initGraphics(lootable);
    }

    /**
     * Register player
     * @param player
     */
    public void registerEntity(Player player) {
        worldContainer.registerEntity(player);
        worldContainer.getMobileEntities().add(player);
        worldContainer.getLivingEntities().add(player);
        renderingEngine.initGraphics(player);
    }

    /**
     * If none of the above method signatures apply, this is the default registration method.
     */
    public void registerEntity(Entity entity) {
        worldContainer.registerEntity(entity);
        worldContainer.getMobileEntities().add(entity);
        if(entity.has(HEALTH))
            worldContainer.getLivingEntities().add(entity);
        renderingEngine.initGraphics(entity);
    }


    public void reset() {
        worldContainer.reset();
        renderingEngine.loadTexturesAndFonts();
        renderingEngine.initGraphics(worldContainer.getPlayer());
        renderingEngine.setPlayer(worldContainer.getPlayer());
    }

    public Entity getEntityByID(int id) {
        return worldContainer.getEntityByID(id);
    }



}
