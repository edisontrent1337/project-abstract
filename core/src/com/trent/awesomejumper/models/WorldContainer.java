package com.trent.awesomejumper.models;

import com.badlogic.gdx.Gdx;
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
    private ArrayList<Tile> collisionTiles = new ArrayList<>();

    public CollisionBox c1, c2;


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

    // LIST OF TILES FOR COLLISION

    public void createCollisionTiles(int sx, int sy, int ex, int ey) {
        collisionTiles.clear();
        for (int x = sx; x <= ex; x++) {
            for (int y = sy; y <= ey; y++) {
                // CHECK WHETHER TILE IS IN LEVEL BOUNDS
                if (level.checkBounds(x, y)) {
                    collisionTiles.add(level.getTile(x, y));
                }
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
}
