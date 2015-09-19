package com.trent.awesomejumper.models;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.trent.awesomejumper.testing.CollisionBox;
import com.trent.awesomejumper.tiles.Tile;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sinthu on 12.06.2015.
 */
public class WorldContainer {

    // MEMBERS & INSTANCES
    // ---------------------------------------------------------------------------------------------

    private Array<Tile> tiles = new Array<>();
    private Player player;
    private Level level;
    private ArrayList tilesToBeDrawn;
    private Array<Rectangle> collisionRectangles = new Array<>();

    public CollisionBox c1, c2;

    private Array<Rectangle> cRectanglesAhead = new Array<>();

    // CONSTRUCTOR
    // ---------------------------------------------------------------------------------------------

    public WorldContainer() {
        createWorld();

    }

    private void createWorld() {
        player = new Player(new Vector2(6f, 10f));
        level = new Level();
    }

    // LIST OF TILES TO BE DRAWN
    // ---------------------------------------------------------------------------------------------

    public ArrayList<Tile> getTilesToBeRendered(int camW, int camH) {

        // GET CURRENT FOV COORDINATES AND ONLY RENDER WHAT THE PLAYER SEES
        // BOTTOM LEFT CORNER
        int fovStartX = (int)(player.getPositionX() - camW/2 - 1);
        int fovStartY = (int)(player.getPositionY() - camH/2 - 1);

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

    // GETTER & SETTER
    // ---------------------------------------------------------------------------------------------

    public Player getPlayer() {
        return player;
    }

    public Level getLevel() {
        return level;
    }

    public Array<Rectangle> getCollisionRectangles() {
        return collisionRectangles;
    }

    public Array<Rectangle> getcRectanglesAhead() {
        return cRectanglesAhead;
    }


}
