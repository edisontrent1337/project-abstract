package com.trent.awesomejumper.models;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.trent.awesomejumper.engine.entity.Entity;
import com.trent.awesomejumper.models.testing.Chest;
import com.trent.awesomejumper.tiles.Tile;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.TreeSet;

/**
 * Created by Sinthu on 12.06.2015.
 * Holds the player, the level with its environmental items such as random paths of dirt,
 * rocks, flowers etc. and also items, collectables and enemies.
 *
 */
public class WorldContainer {

    // MEMBERS & INSTANCES
    // ---------------------------------------------------------------------------------------------

    public static int nodes = 0;

    private HashSet<Entity> entities;

    private Array<Tile> tiles = new Array<>();
    private Player player;
    private Chest chest;
    private Level level;
    private ArrayList tilesToBeDrawn, entitiesToBeDrawn;
    private ArrayList<Tile> collisionTiles = new ArrayList<>();


    // CONSTRUCTOR
    // ---------------------------------------------------------------------------------------------

    public WorldContainer() {
        createWorld();

    }

    private void createWorld() {
        player = new Player(new Vector2(12.5f, 7f));
        chest = new Chest(new Vector2(5,5));
        level = new Level();
        entities = new HashSet<>();
        entitiesToBeDrawn = new ArrayList<>();
        entities.add(player);
        entities.add(chest);
        /*for(int x = 1; x < 12; x++) {
            for(int y = 5; y < 12; y++) {
                entities.add(new Chest(new Vector2((float)x/1.2f,(float)y/1.2f)));
            }
        }*/
    }


    // ---------------------------------------------------------------------------------------------
    // LIST OF TILES TO BE DRAWN
    // ---------------------------------------------------------------------------------------------

    public ArrayList<Tile> getTilesToBeRendered(int camW, int camH) {

        // GET CURRENT FOV COORDINATES AND ONLY RENDER WHAT THE PLAYER SEES
        // BOTTOM LEFT CORNER
        int fovStartX = (int)(player.getPosition().x - camW/2 - 1);
        int fovStartY = (int)(player.getPosition().y - camH/2 - 1);

        // TOP RIGHT CORNER
        int fovEndX = fovStartX + camW + 3;
        int fovEndY = fovStartY + camH + 2;

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
    public ArrayList<Entity> getEntitiesToBeRendered(int camW, int camH) {
        int fovStartX = (int)(player.getPosition().x - camW/2);
        int fovStartY = (int)(player.getPosition().y - camH/2);

        // TOP RIGHT CORNER
        int fovEndX = fovStartX + camW + 1;
        int fovEndY = fovStartY + camH + 1;

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
                    if(!entitiesToBeDrawn.contains(e))
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



    // LIST OF TILES FOR COLLISION

    public void createCollisionTiles(int sx, int sy, int ex, int ey) {
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

    /**
     * Updates the list of entities close to the specified entity e.
     * The neighbourhood list is used by the collision controller to solve entity/entity collision
     * @param e entity
     * @return  modified HashSet with neighbours
     */

    public HashSet<Entity> updatedEntityNeighbourHood(Entity e) {

        HashSet<Entity> entityNeighbourhood = e.getBody().getEntityNeighbourHood();
        int rangeStartX = (int) e.getPosition().x - 2;
        int rangeEndX = (int) e.getPosition().x + 2;
        int rangeStartY = (int) e.getPosition().y - 2;
        int rangeEndY = (int) e.getPosition().y + 2;

        if(rangeStartX < 0)
            rangeStartX = 0;
        if(rangeStartY < 0)
            rangeEndX = 0;
        if(rangeEndX > level.getLevelWidth())
            rangeEndX = level.getLevelWidth();
        if(rangeEndY > level.getLevelHeight())
            rangeEndY = level.getLevelHeight();

        for(Entity other: entities) {
            if(other.equals(e))
                continue;
            if(other.getPosition().x >= rangeStartX &&
                    other.getPosition().x < rangeEndX &&
                    other.getPosition().y >= rangeStartY &&
                    other.getPosition().y < rangeEndY) {
                if(!entityNeighbourhood.contains(other))
                    entityNeighbourhood.add(other);
            }
            else {
                entityNeighbourhood.remove(other);
                }


        }
        return entityNeighbourhood;
    }



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
