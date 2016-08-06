package com.trent.awesomejumper.controller;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.trent.awesomejumper.controller.levelgeneration.RandomLevelGenerator;
import com.trent.awesomejumper.engine.entity.Entity;
import com.trent.awesomejumper.game.AwesomeJumperMain;
import com.trent.awesomejumper.models.Level;
import com.trent.awesomejumper.models.Player;
import com.trent.awesomejumper.models.testing.Chest;
import com.trent.awesomejumper.models.weapons.Pistol;
import com.trent.awesomejumper.tiles.Tile;

import static com.trent.awesomejumper.utils.Utilities.sub;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;

/**
 * Created by Sinthu on 12.06.2015.
 * Holds the player, the level with its environmental items such as random paths of dirt,
 * rocks, flowers etc. and also items, collectables and enemies.
 * Holds a list of all entities and manages all specific lists of entities needed by the controllers.
 *
 */
public class WorldContainer {

    // MEMBERS & INSTANCES
    // ---------------------------------------------------------------------------------------------

    public static int nodes = 0;

    private HashSet<Entity> entities = new HashSet<>();
    private HashSet<Entity> projectiles = new HashSet<>();
    private HashSet<Entity> pickups = new HashSet<>();
    private HashSet<Entity> enemies = new HashSet<>();
    private HashSet<Entity> weaponDrops = new HashSet<>();

    private Player player;
    private Chest chest;
    private Pistol pistol, pistol2;
    private Level level;
    //TODO Change these to HashSet
    private ArrayList tilesToBeDrawn = new ArrayList();
    private ArrayList entitiesToBeDrawn = new ArrayList<>();
    private ArrayList<Tile> collisionTiles = new ArrayList<>();

    // TESTING

    private com.trent.awesomejumper.controller.levelgeneration.RandomLevelGenerator randomLevelGenerator;


    // CONSTRUCTOR
    // ---------------------------------------------------------------------------------------------

    public WorldContainer() {
        entities = new HashSet<>();
        pickups = new HashSet<>();
        weaponDrops = new HashSet<>();
        entitiesToBeDrawn = new ArrayList<>();
        projectiles = new HashSet<>();


       /* player = new Player(new Vector2(12.5f, 7f));
        chest = new Chest(new Vector2(5,5));
        pistol = new Pistol(new Vector2(6f,7f));
        pistol2 = new Pistol(new Vector2(5f, 6f));
        level = new Level();*/

        randomLevelGenerator = new RandomLevelGenerator();
        randomLevelGenerator.init();
        randomLevelGenerator.load();

        entities = randomLevelGenerator.getEntities();

        for(Entity e: entities) {
            registerEntity(e);
        }

        player = randomLevelGenerator.getPlayer();

        registerEntity(player);
        /*
        registerEntity(chest);
        registerEntity(pistol);
        registerEntity(pistol2);*/
    }






    // ---------------------------------------------------------------------------------------------
    // LIST OF TILES TO BE DRAWN
    // ---------------------------------------------------------------------------------------------

    public ArrayList<Tile> getTilesToBeRendered(Vector2 cameraPosition, float camW, float camH) {

        // GET CURRENT FOV COORDINATES AND ONLY RENDER WHAT THE PLAYER SEES
        // BOTTOM LEFT CORNER
        int fovStartX = (int)(cameraPosition.x - camW/2 - 1);
        int fovStartY = (int)(cameraPosition.y - camH/2 - 1);

        // TOP RIGHT CORNER
        int fovEndX = fovStartX + (int) camW + 4;
        int fovEndY = fovStartY + (int) camH + 4;

        // KEEP BOUNDS
        if(fovStartX < 0) fovStartX = 0;
        if(fovStartY < 0) fovStartY = 0;


        if(fovEndX > randomLevelGenerator.getLevelWidth())
            fovEndX = randomLevelGenerator.getLevelWidth();

        if(fovEndY > randomLevelGenerator.getLevelHeight())
            fovEndY = randomLevelGenerator.getLevelHeight();

        tilesToBeDrawn = new ArrayList<>();
        Tile tile;

        for(int x = fovStartX; x < fovEndX; x++) {
            for(int y = fovStartY; y < fovEndY; y++) {

               // tile = level.getTile(x,y);
                tile = randomLevelGenerator.getTile(x,y);
                if(tile != null)
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
                    if(randomLevelGenerator.getTile(x,y) != null) {
                        if(!randomLevelGenerator.getTile(x,y).isPassable())
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
     * @param camW camera width
     * @param camH camera height
     * @return ArrayList with entities
     */
    public ArrayList<Entity> getEntitiesToBeRendered(Vector2 position, float camW, float camH) {
        int fovStartX = (int)(position.x - camW/2);
        int fovStartY = (int)(position.y - camH/2);

        // TOP RIGHT CORNER
        int fovEndX = fovStartX + (int)camW + 4;
        int fovEndY = fovStartY + (int)camH + 4;

        // KEEP BOUNDS
        if(fovStartX < 0) fovStartX = 0;
        if(fovStartY < 0) fovStartY = 0;

        if(fovEndX > randomLevelGenerator.getLevelWidth())
            fovEndX = randomLevelGenerator.getLevelWidth();

        if(fovEndY > randomLevelGenerator.getLevelHeight())
            fovEndY = randomLevelGenerator.getLevelHeight();

        for (Entity e : entities) {
            if(e.getBody().getPosition().x > fovStartX &&
                    e.getBody().getPosition().x <= fovEndX &&
                    e.getBody().getPosition().y > fovStartY &&
                    e.getBody().getPosition().y <= fovEndY) {
                if(!entitiesToBeDrawn.contains(e) && e.hasGraphics)
                    entitiesToBeDrawn.add(e);
            }
            else {
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
        nodes = entitiesToBeDrawn.size();
        return entitiesToBeDrawn;
    }



    // ---------------------------------------------------------------------------------------------
    // ENTITY NEIGHBOURHOOD MANAGEMENT
    // ---------------------------------------------------------------------------------------------
    /**
     * Updates the list of entities close to the specified entity e.
     * The neighbourhood list describes the perimeter of radius 3 around the entity and is used by
     * the collision controller to solve entity/entity collision.
     * @param e entity
     * @return  modified HashSet with neighbours
     */

    public HashSet<Entity> updatedEntityNeighbourHood(Entity e) {

        HashSet<Entity> entityNeighbourhood = e.getBody().getEntityNeighbourHood();

        for(Entity other: entities) {
            if(other.equals(e))
                continue;
            float dst = sub(e.getPosition(), other.getPosition()).len2(); // distance between entities
            if(dst <= 3 ) {
                if(!entityNeighbourhood.contains(other))
                    entityNeighbourhood.add(other);
            }
            else {
                entityNeighbourhood.remove(other);
            }


        }
        return entityNeighbourhood;
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

        for(Iterator<Entity> it = entities.iterator(); it.hasNext();) {
            if(!it.next().isAlive()) {
                it.remove();
            }
        }

        for(Iterator<Entity> it = entitiesToBeDrawn.iterator(); it.hasNext();) {
            if(!it.next().isAlive()) {
                it.remove();
            }
        }

        for(Entity e: entities) {
            for(Iterator<Entity> it = e.getBody().getEntityNeighbourHood().iterator(); it.hasNext();) {
                if(!it.next().isAlive())
                    it.remove();

            }
        }

    }


    // ---------------------------------------------------------------------------------------------
    // REGISTER ENTITIES
    // ---------------------------------------------------------------------------------------------

    public void registerEntity(Entity entity) {

        entities.add(entity);
        entity.registerTime = entity.time;
        if(AwesomeJumperMain.onDebugMode()) {
            Gdx.app.log("Registrated entity at", Float.toString(entity.registerTime));
            Gdx.app.log("Entity registrated", String.format("%04d",(entity.getID())));
        }
    }


    // ---------------------------------------------------------------------------------------------
    // PLACE ENTITY IN WORLD
    // ---------------------------------------------------------------------------------------------


    /**
     * Places an entity on the specified position in the world.
     * @param entity entity to be placed
     * @param position position where entity should be dropped.
     * @return
     */
    public boolean placeEntity(Entity entity, Vector2 position) {
        Gdx.app.log("Weapon pos before drop", entity.getPosition().toString());
        entity.setPosition(position.cpy());
        Gdx.app.log("Weapon pos after drop", entity.getPosition().toString());
        //TODO: add here this switch
        /*switch (Entity.Type) {
            case DROPPED_WEAPON_ENTITY:
        }*/
        pickups.add(entity);
        return false;
    }


    public void reset() {
        entities.clear();
        player.destroy();
        randomLevelGenerator = new com.trent.awesomejumper.controller.levelgeneration.RandomLevelGenerator();
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
        return entities;
    }

    public HashSet<Entity> getWeaponDrops() {
        return weaponDrops;
    }

    public HashSet<Entity> getPickups() {
        return pickups;
    }
    public HashSet<Entity> getProjectiles() {
        return projectiles;
    }

    public com.trent.awesomejumper.controller.levelgeneration.RandomLevelGenerator getRandomLevelGenerator() {
        return randomLevelGenerator;
    }

}
