package com.trent.awesomejumper.controller;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.trent.awesomejumper.controller.levelgeneration.RandomLevelGenerator;
import com.trent.awesomejumper.engine.entity.Entity;
import com.trent.awesomejumper.game.AwesomeJumperMain;
import com.trent.awesomejumper.models.Player;
import com.trent.awesomejumper.models.pickups.Pickup;
import com.trent.awesomejumper.models.projectile.Projectile;
import com.trent.awesomejumper.models.weapons.Weapon;
import com.trent.awesomejumper.tiles.Tile;
import com.trent.awesomejumper.utils.Utilities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import static com.trent.awesomejumper.engine.modelcomponents.ModelComponent.ComponentID.GRAPHICS;
import static com.trent.awesomejumper.utils.Utilities.sub;

/**
 * Created by Sinthu on 12.06.2015.
 * Holds the player, the level with its environmental items such as random paths of dirt,
 * rocks, flowers etc. and also items, collectables and enemies.
 * Holds a list of all entities and manages all specific lists of entities needed by the controllers.
 * This class is managed by the {@link EntityManager}.
 */
public class WorldContainer {

    // MEMBERS & INSTANCES
    // ---------------------------------------------------------------------------------------------

    public static int renderNodes = 0;
    public static int registredNodes = 0;

    private static final float NEIGHBOURHOOD_RANGE = 2.0f;

    /**
     * The following maps and sets manage all entities in the world.
     * Separate sets are needed for the collision detection to work properly, as there is a need
     * to differentiate between different types of entities.
     */
    // Map containing all entities in the game. Maps entity IDs with entities.
    private HashMap<Integer, Entity> entities;

    // Subset containing all projectiles
    private HashSet<Projectile> projectiles = new HashSet<>();

    // Subset containing all pickups (ammo, medikits,...)
    private HashSet<Pickup> pickups = new HashSet<>();

    // Subset containing all mobile entities. These are entities that are able to move
    // and be affected by explosions
    private HashSet<Entity> mobileEntities = new HashSet<>();

    // Subset containing all dropped weapons the player can pick up.
    private HashSet<Weapon> weaponDrops = new HashSet<>();

    // Subset containing all living entities that can take damage
    private HashSet<Entity> livingEntities = new HashSet<>();   // subset of all entities that can take damage

    private Player player;

    private ArrayList tilesToBeDrawn = new ArrayList();
    private ArrayList<Entity> entitiesToBeDrawn = new ArrayList<>();
    private ArrayList<Tile> collisionTiles = new ArrayList<>();


    private RandomLevelGenerator randomLevelGenerator;


    // CONSTRUCTOR
    // ---------------------------------------------------------------------------------------------

    public WorldContainer() {
        entities = new HashMap<>();
        pickups = new HashSet<>();
        weaponDrops = new HashSet<>();
        entitiesToBeDrawn = new ArrayList<>();
        projectiles = new HashSet<>();
        randomLevelGenerator = new RandomLevelGenerator();
        randomLevelGenerator.init();
        entities = randomLevelGenerator.load();
        player = randomLevelGenerator.getPlayer();
    }


    public void initAllEntities() {
        for (Iterator<? extends Entity> it = entities.values().iterator(); it.hasNext(); ) {
            Entity e = it.next();
            e.register();
        }
    }


    // ---------------------------------------------------------------------------------------------
    // LIST OF TILES TO BE DRAWN
    // ---------------------------------------------------------------------------------------------

    public ArrayList<Tile> getTilesToBeRendered(Vector2 cameraPosition, float camW, float camH) {

        // GET CURRENT FOV COORDINATES AND ONLY RENDER WHAT THE PLAYER SEES
        // BOTTOM LEFT CORNER
        int fovStartX = (int) (cameraPosition.x - camW / 2 - 1);
        int fovStartY = (int) (cameraPosition.y - camH / 2 - 1);

        // TOP RIGHT CORNER
        int fovEndX = fovStartX + (int) camW + 4;
        int fovEndY = fovStartY + (int) camH + 4;

        // KEEP BOUNDS
        if (fovStartX < 0) fovStartX = 0;
        if (fovStartY < 0) fovStartY = 0;


        if (fovEndX > randomLevelGenerator.getLevelWidth())
            fovEndX = randomLevelGenerator.getLevelWidth();

        if (fovEndY > randomLevelGenerator.getLevelHeight())
            fovEndY = randomLevelGenerator.getLevelHeight();

        tilesToBeDrawn = new ArrayList<>();
        Tile tile;

        for (int x = fovStartX; x < fovEndX; x++) {
            for (int y = fovStartY; y < fovEndY; y++) {

                // tile = level.getTile(x,y);
                tile = randomLevelGenerator.getTile(x, y);
                if (tile != null)
                    tilesToBeDrawn.add(tile);

            }
        }

        return tilesToBeDrawn;

    }

    // ---------------------------------------------------------------------------------------------
    // LIST OF TILES FOR COLLISION DETECTION
    // ---------------------------------------------------------------------------------------------

    /**
     * Fills collisionTiles with all tile objects that are in the specified ranges.
     *
     * @param sx x starting point
     * @param sy y starting point
     * @param ex x endpoint
     * @param ey y endpoint
     */
    public void fillCollideableTiles(int sx, int sy, int ex, int ey) {
        collisionTiles.clear();
        for (int x = sx; x <= ex; x++) {
            for (int y = sy; y <= ey; y++) {
                // CHECK WHETHER TILE IS IN LEVEL BOUNDS
                if (randomLevelGenerator.checkBounds(x, y)) {
                    if (randomLevelGenerator.getTile(x, y) != null) {
                        if (!randomLevelGenerator.getTile(x, y).isPassable())
                            collisionTiles.add(randomLevelGenerator.getTile(x, y));
                    }
                }
            }
        }

    }

    // ---------------------------------------------------------------------------------------------
    // ORDERED LIST OF ENTITIES TO BE DRAWN
    // ---------------------------------------------------------------------------------------------

    /**
     * Returns only those entities visible to the players field of view to be rendered.
     * Orders the entities according to their y-position to render them properly on top of
     * each other.
     *
     * @param camW camera width
     * @param camH camera height
     * @return ArrayList with entities
     */
    public ArrayList<Entity> getEntitiesToBeRendered(Vector2 position, float camW, float camH) {
        int fovStartX = (int) (position.x - camW / 2);
        int fovStartY = (int) (position.y - camH / 2);

        // TOP RIGHT CORNER
        int fovEndX = fovStartX + (int) camW + 4;
        int fovEndY = fovStartY + (int) camH + 4;

        // KEEP BOUNDS
        if (fovStartX < 0) fovStartX = 0;
        if (fovStartY < 0) fovStartY = 0;

        if (fovEndX > randomLevelGenerator.getLevelWidth())
            fovEndX = randomLevelGenerator.getLevelWidth();

        if (fovEndY > randomLevelGenerator.getLevelHeight())
            fovEndY = randomLevelGenerator.getLevelHeight();

        for (Entity e : entities.values()) {
            if (e.getBody().getPosition().x > fovStartX &&
                    e.getBody().getPosition().x <= fovEndX &&
                    e.getBody().getPosition().y > fovStartY &&
                    e.getBody().getPosition().y <= fovEndY) {
                if (!entitiesToBeDrawn.contains(e) && e.has(GRAPHICS))
                    entitiesToBeDrawn.add(e);
            } else {
                entitiesToBeDrawn.remove(e);
            }
        }

        /**
         * Sort entitiesToBeDrawn by using a comparator that compares the y value of the
         * entities positions and then reversing the whole list.
         */
        Collections.sort(entitiesToBeDrawn, new Comparator<Entity>() {
            @Override
            public int compare(Entity e1, Entity e2) {
                return Float.compare(e1.getPosition().y, e2.getPosition().y);
            }
        });
        Collections.reverse(entitiesToBeDrawn);
        renderNodes = entitiesToBeDrawn.size();
        registredNodes = entities.size();
        return entitiesToBeDrawn;
    }


    // ---------------------------------------------------------------------------------------------
    // ENTITY NEIGHBOURHOOD MANAGEMENT
    // ---------------------------------------------------------------------------------------------

    public class WeaponAndDistance {
        Weapon weapon;
        float distance;

        public WeaponAndDistance(Weapon w, float distance) {
            this.weapon = w;
            this.distance = distance;
        }

    }

    /**
     * Updates the list of entities close to the specified entity e.
     * The neighbourhood list describes the perimeter of radius 3 around the entity and is used by
     * the collision controller to solve entity/entity collision.
     *
     * @param e entity
     * @return modified HashSet with neighbours
     */

    public HashSet<Entity> updatedEntityNeighbourHood(Entity e) {

        HashSet<Entity> entityNeighbourhood = e.getBody().getEntityNeighbourHood();

        for (Entity other : mobileEntities) {
            if (other.equals(e))
                continue;


            float dst = sub(e.getBody().getCenter(), other.getBody().getCenter()).len(); // distance between entities
            if (dst <= NEIGHBOURHOOD_RANGE) {
                if (!entityNeighbourhood.contains(other) && other.getBody().isCollisionDetectionEnabled())
                    entityNeighbourhood.add(other);

                if (entityNeighbourhood.contains(other) && !other.getBody().isCollisionDetectionEnabled())
                    entityNeighbourhood.remove(other);

            } else {
                entityNeighbourhood.remove(other);
            }


        }
        return entityNeighbourhood;
    }


    public HashSet<WeaponAndDistance> updateNearbyPickups(){

        return null;
    }

    // ---------------------------------------------------------------------------------------------
    // REMOVAL OF DEAD ENTITIES
    // ---------------------------------------------------------------------------------------------

    /**
     * Removes all entities which have their alive flag set to false from all relevant collections.
     * Modifies and cleans the global entity collection, the "toBeDrawn" subset and the
     * neighbourhood of each entity.
     */
    public void garbageRemoval() {


        /**
         * Remove dead entities from the main entity map.
         */
        for (Iterator<Map.Entry<Integer, Entity>> it = entities.entrySet().iterator(); it.hasNext(); ) {
            Entity e = it.next().getValue();
            if (!e.isAlive())
                it.remove();
        }

        /**
         * Remove dead entities from the entitiesToBeDrawn set.
         */
        for (Iterator<Entity> it = entitiesToBeDrawn.iterator(); it.hasNext(); ) {
            if (!it.next().isAlive()) {
                it.remove();
            }
        }

        /**
         * Remove dead entities from the entity neighbourhoods.
         */
        for (Entity e : entities.values()) {
            for (Iterator<Entity> it = e.getBody().getEntityNeighbourHood().iterator(); it.hasNext(); ) {
                if (!it.next().isAlive())
                    it.remove();

            }
        }


        /**
         * Remove dead projectiles.
         */
        for (Iterator<? extends Entity> it = projectiles.iterator(); it.hasNext(); ) {
            if (!it.next().isAlive()) {
                it.remove();
            }
        }


        for (Iterator<Weapon> it = weaponDrops.iterator(); it.hasNext(); ) {
            if (!it.next().isAlive())
                it.remove();
        }

        /**
         * Remove dead mobile entities.
         */
        for (Iterator<Entity> it = mobileEntities.iterator(); it.hasNext(); ) {
            if (!it.next().isAlive())
                it.remove();
        }


        for (Iterator<Entity> it = livingEntities.iterator(); it.hasNext(); ) {
            if (!it.next().isAlive())
                it.remove();
        }


    }


    public void garbageRemoval(Set<? extends Entity> entities) {
        for (Iterator<? extends Entity> it = entities.iterator(); it.hasNext(); ) {
            if (!it.next().isAlive()) {
                it.remove();
            }
        }
    }

    // ---------------------------------------------------------------------------------------------
    // REGISTER ENTITIES
    // ---------------------------------------------------------------------------------------------

    public void registerEntity(Entity entity) {

        entities.put(entity.getID(), entity);
        entity.registerTime = entity.time;
        if (AwesomeJumperMain.onDebugMode()) {
            Gdx.app.log("Registrated entity at", Float.toString(entity.registerTime));
            Gdx.app.log("Entity registrated", String.format("%04d", (entity.getID())));
        }
    }

    public void registerEntities(Set<Entity> entities) {
        for (Entity e : entities) {
            registerEntity(e);
        }
    }


    // ---------------------------------------------------------------------------------------------
    // PLACE ENTITY IN WORLD
    // ---------------------------------------------------------------------------------------------


    /**
     * Places an entity on the specified position in the world.
     *
     * @param entity   entity to be placed
     * @param position position where entity should be dropped.
     * @return
     */
    public boolean placeEntity(Entity entity, Vector2 position) {

        Vector2 destination = position.cpy();
        if (randomLevelGenerator.getTile(position).getType().equals(Tile.TileType.WALL)) {
            Utilities.log("WEAPON OUT OF BOUNDS", entity.getPosition().toString());
            Utilities.log("INSIDE TILE", randomLevelGenerator.getTile(position).getPosition().toString());
            Utilities.log("NEIGHBOURHOOD SIZE", Float.toString(randomLevelGenerator.getTile(position).getNeighbourHood().size()));
            float minDst = Float.MAX_VALUE;
            for (Tile t : randomLevelGenerator.getTile(position).getNeighbourHood()) {
                Utilities.log("NEIGHBOUR TILE", t.getPosition().toString());

                float dst;

                if (!entity.getOwner().equals(entity))
                    dst = position.dst(t.getPosition());
                else
                    dst = entity.getOwner().getPosition().dst(t.getPosition());

                if (dst < minDst) {
                    minDst = dst;
                    Utilities.log("NEW MINIMAL DST", Float.toString(minDst));
                    Utilities.log("NEW CENTER", t.getCollisionBox().getCenter().toString());
                    destination = t.getCollisionBox().getCenter().cpy();

                }

            }
            Utilities.log(destination.toString());
        }
        entity.setPosition(destination);

        //entity.setPosition(new Vector2(Math.round(player.getPosition().cpy().x - 1), Math.round(player.getPosition().cpy().y - 1 )));
        Utilities.log(entity.getPosition().toString());

        return false;
    }


    public void reset() {
        entities.clear();
        player.destroy();
        randomLevelGenerator = new RandomLevelGenerator();
        randomLevelGenerator.init();
        randomLevelGenerator.load();
        // TODO: insert here: entities = randomLevelGenerator.getEntities()
        player = randomLevelGenerator.getPlayer();
        registerEntity(player);
    }

    // ---------------------------------------------------------------------------------------------
    // GETTER & SETTER
    // ---------------------------------------------------------------------------------------------

    public Player getPlayer() {
        return player;
    }

    public ArrayList<Tile> getCollisionTiles() {
        return collisionTiles;
    }


    public HashSet<Entity> getEntities() {
        return new HashSet<>(entities.values());
    }

    public HashMap<Integer, Entity> getEntityMap() {
        return entities;
    }

    public HashSet<Weapon> getWeaponDrops() {
        return weaponDrops;
    }

    public HashSet<Pickup> getPickups() {
        return pickups;
    }

    public HashSet<Entity> getMobileEntities() {
        return mobileEntities;
    }

    public HashSet<Entity> getLivingEntities() {
        return livingEntities;
    }

    public HashSet<Projectile> getProjectiles() {
        return projectiles;
    }

    public RandomLevelGenerator getRandomLevelGenerator() {
        return randomLevelGenerator;
    }

    public Entity getEntityByID(int id) {
        Entity e = entities.get(id);
        if (e == null) {
            Gdx.app.log("ERROR", "THE REQUESTED ENTITY WAS NOT FOUND. ENTITY ID: " + Integer.toString(id));
        }
        return entities.get(id);
    }


}
