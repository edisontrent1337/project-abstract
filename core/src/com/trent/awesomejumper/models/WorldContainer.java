package com.trent.awesomejumper.models;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.trent.awesomejumper.engine.entity.Entity;
import com.trent.awesomejumper.models.testing.Chest;
import com.trent.awesomejumper.tiles.Tile;
import static com.trent.awesomejumper.utils.Utilities.sub;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
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

    private HashSet<Entity> entities;
    private HashSet<Entity> projectiles;

    private Player player;
    private Chest chest;
    private Level level;
    //TODO Change these to HashSet
    private ArrayList tilesToBeDrawn, entitiesToBeDrawn;
    private ArrayList<Tile> collisionTiles = new ArrayList<>();


    // CONSTRUCTOR
    // ---------------------------------------------------------------------------------------------

    public WorldContainer() {

        player = new Player(new Vector2(12.5f, 7f));
        chest = new Chest(new Vector2(5,5));
        level = new Level();
        entities = new HashSet<>();
        entitiesToBeDrawn = new ArrayList<>();
        entities.add(player);
        entities.add(chest);

    }



    // ---------------------------------------------------------------------------------------------
    // LIST OF TILES TO BE DRAWN
    // ---------------------------------------------------------------------------------------------

    public ArrayList<Tile> getTilesToBeRendered(float camW, float camH) {

        // GET CURRENT FOV COORDINATES AND ONLY RENDER WHAT THE PLAYER SEES
        // BOTTOM LEFT CORNER
        int fovStartX = (int)(player.getPosition().x - camW/2 - 1);
        int fovStartY = (int)(player.getPosition().y - camH/2 - 1);

        // TOP RIGHT CORNER
        int fovEndX = fovStartX + (int) camW + 4;
        int fovEndY = fovStartY + (int) camH + 4;

        // KEEP BOUNDS
        if(fovStartX < 0) fovStartX = 0;
        if(fovStartY < 0) fovStartY = 0;


        if(fovEndX > level.getLevelWidth())
            fovEndX = level.getLevelWidth();

        if(fovEndY > level.getLevelHeight())
            fovEndY = level.getLevelHeight();

        tilesToBeDrawn = new ArrayList<>();
        Tile tile;

        for(int x = fovStartX; x < fovEndX; x++) {
            for(int y = fovStartY; y < fovEndY; y++) {

                tile = level.getTile(x,y);

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
                if (level.checkBounds(x, y)) {
                    if(level.getTile(x,y) != null) {
                        if(!level.getTile(x,y).isPassable())
                        collisionTiles.add(level.getTile(x, y));
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
    public ArrayList<Entity> getEntitiesToBeRendered(float camW, float camH) {
        int fovStartX = (int)(player.getPosition().x - camW/2);
        int fovStartY = (int)(player.getPosition().y - camH/2);

        // TOP RIGHT CORNER
        int fovEndX = fovStartX + (int)camW + 4;
        int fovEndY = fovStartY + (int)camH + 4;

        // KEEP BOUNDS
        if(fovStartX < 0) fovStartX = 0;
        if(fovStartY < 0) fovStartY = 0;

        if(fovEndX > level.getLevelWidth())
            fovEndX = level.getLevelWidth();

        if(fovEndY > level.getLevelHeight())
            fovEndY = level.getLevelHeight();

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

    // GETTER & SETTER
    // ---------------------------------------------------------------------------------------------

    public Player getPlayer() {
        return player;
    }

    public Level getLevel() {
        return level;
    }

    public ArrayList<Tile> getCollisionTiles() {
        return collisionTiles;
    }

    public HashSet<Entity> getEntities() {
        return entities;
    }

}
