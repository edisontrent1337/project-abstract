package com.trent.awesomejumper.controller;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.trent.awesomejumper.engine.entity.Entity;
import com.trent.awesomejumper.exceptions.InvalidWeaponSlotException;
import com.trent.awesomejumper.models.Level;
import com.trent.awesomejumper.models.Player;
import com.trent.awesomejumper.models.testing.Chest;
import com.trent.awesomejumper.models.weapons.Pistol;
import com.trent.awesomejumper.tiles.Tile;

import static com.trent.awesomejumper.utils.Utilities.pythagoras;
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

    private HashSet<Entity> entities;
    private HashSet<Entity> projectiles;
    private HashSet<Entity> pickups;
    private HashSet<Entity> enemies;

    private Player player;
    private Chest chest;
    private Pistol pistol, pistol2;
    private Level level;
    //TODO Change these to HashSet
    private ArrayList tilesToBeDrawn, entitiesToBeDrawn;
    private ArrayList<Tile> collisionTiles = new ArrayList<>();


    // CONSTRUCTOR
    // ---------------------------------------------------------------------------------------------

    public WorldContainer() {
        entities = new HashSet<>();
        pickups = new HashSet<>();
        entitiesToBeDrawn = new ArrayList<>();
        player = new Player(new Vector2(12.5f, 7f));
        chest = new Chest(new Vector2(5,5));
        pistol = new Pistol(new Vector2(6f,7f));
        pistol2 = new Pistol(new Vector2(5f, 6f));
        level = new Level();
        registerEntity(player);
        registerEntity(chest);
        registerEntity(pistol);
        registerEntity(pistol2);
        try {
            player.getWeaponInventory().equipWeapon(pistol, 1);
            player.getWeaponInventory().equipWeapon(pistol2,2);
        }
        catch (InvalidWeaponSlotException e){
            Gdx.app.log("ERROR",e.getMessage());
        }
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


    // ---------------------------------------------------------------------------------------------
    // REGISTER ENTITIES
    // ---------------------------------------------------------------------------------------------

    public void registerEntity(Entity entity) {
        entities.add(entity);
    }


    // ---------------------------------------------------------------------------------------------
    // PLACE ENTITY IN WORLD
    // ---------------------------------------------------------------------------------------------


    //TODO: EDIT THIS. The direction towards the entity that drops the other entity must be included
    public boolean placeEntity(Entity entity) {


        Vector2 center = player.getBody().getCenter().cpy();
        float radius = pythagoras(player.getWidth(), player.getHeight()) + 0.5f;
        float dropAngle = (float) Math.random()*360f;
        Gdx.app.log("ANGLE", Float.toString(dropAngle));
        Vector2 dropPosition = new Vector2((float)Math.cos(dropAngle), (float)Math.sin(dropAngle)).nor().scl(radius).add(center);

        Gdx.app.log("CIRCLE AROUND PLAYER", "CENTER:" + center.toString() + ", RADIUS:" + Float.toString(radius) + "DROPPOSITION: " +  dropPosition.toString());

        entity.setPosition(dropPosition);

        pickups.add(entity);
        return true;
        // TODO: move this method to collision controller and iterate over pickup collection
        // TODO: implement something like: entity.getOwner to reference the entity that drops the other
        // TODO: implement random drop position in a circle around owner, radius must be bigger than hypotenuse of hitbox of owner
        /*entity.setPosition(player.getPosition().cpy());
        entity.getBody().enableCollisionDetection();
        return true;*/
        /*int startX = (int) entity.getBounds().getPositionAndOffset().x;
        int endX = (int) (startX + entity.getBounds().getWidth()) + 2;
        int startY = (int) entity.getBounds().getPositionAndOffset().y;
        int endY = (int) (startY + entity.getBounds().getHeight()) + 2;
        Gdx.app.log("SX, EX, SY, EY", startX +";" + endX + ";" + startY +";" + endY);
        Gdx.app.log("BOUNDSOFFSET", entity.getBounds().getPositionAndOffset().toString());
        if(level.getTile(startX,startY) != null) {
            if(level.getTile(startX, startY).isPassable()) {
                Gdx.app.log("EARLY: OBJECT POSITION:", entity.getBounds().getPositionAndOffset().toString());
                entity.getBody().enableCollisionDetection();
                return true;
            }
            if(player.getPosition().x - entity.getPosition().x < 0) {
                startX--;
                endX--;
            }
            else {
                startX++;
                endX++;
            }

            if(player.getPosition().y - entity.getPosition().y < 0) {
                startY--;
                endY--;
            }

            else {
                startY++;
                endY++;
            }
        }
        for(int x = startX; x < endX; x++) {
            for(int y = startY; y < endY; y++) {
                if(level.checkBounds(x,y) && level.getTile(x,y) != null) {
                    if(level.getTile(x,y).isPassable()) {
                        Gdx.app.log("LATE: OBJECT POSITION:", entity.getBounds().getPositionAndOffset().toString());
                        Gdx.app.log("NEW: OBJECT POSITION:", new Vector2(x,y).toString());
                        entity.setVelocity(0f, 0f);
                        entity.setPosition(new Vector2(x, y));
                        entity.getBody().enableCollisionDetection();
                        return true;
                    }
                }
            }
        }*/

       // return false;
    }

    // ---------------------------------------------------------------------------------------------
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
