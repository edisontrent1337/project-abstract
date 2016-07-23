package com.trent.awesomejumper.controller;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.trent.awesomejumper.engine.entity.Entity;
import com.trent.awesomejumper.models.Player;
import com.trent.awesomejumper.models.testing.Room;
import com.trent.awesomejumper.tiles.DefaultTile;
import com.trent.awesomejumper.tiles.Tile;

import java.util.HashSet;
import java.util.Random;

/** //TODO: DOCUMENTATION
 * Controller responsible for generating random levels.
 * Uses procedural random level generation.
 * Created by Sinthu on 22.07.2016.
 */
public class RandomLevelGenerator {

    // MEMBERS & INSTANCES
    // ---------------------------------------------------------------------------------------------
    private Tile[][] levelData;
    private Array<Room> rooms;
    private int levelWidth;
    private int levelHeight;

    private HashSet<Entity> entities;
    private Player player;

    private long seed;

    private Random random;

    private final int MIN_LEVEL_WIDTH = 64;
    private final int MIN_LEVEL_HEIGHT = 64;
    private final int MAX_LEVEL_WIDTH = 128;
    private final int MAX_LEVEL_HEIGHT = 128;

    private final int MIN_ROOM_WIDTH = 7;
    private final int MAX_ROOM_WIDTH = 16;
    private final int MIN_ROOM_HEIGHT = 7;
    private final int MAX_ROOM_HEIGHT = 16;

    private final int MAX_INSERTION_TRYS = 512;

    // CONSTRUCTOR
    // ---------------------------------------------------------------------------------------------

    public RandomLevelGenerator() {
        Gdx.app.log("LEVEL", "START LOADING LEVEL");
        seed = System.currentTimeMillis();
        random = new Random(seed);
        //random = new Random(1469235218500l);
        levelWidth = MIN_LEVEL_WIDTH + random.nextInt((MAX_LEVEL_WIDTH - MIN_LEVEL_WIDTH) / 2) * 2;
        levelHeight = MIN_LEVEL_HEIGHT + random.nextInt((MAX_LEVEL_HEIGHT - MIN_LEVEL_HEIGHT) / 2) * 2;
        Gdx.app.log("LEVEVL", "LEVEL SEED: " + Long.toString(seed));
        Gdx.app.log("LEVEL", "LEVEL DIMENSIONS:" + "w: " + Integer.toString(levelWidth) + " h: "
                + Integer.toString(levelHeight));

        levelData = new Tile[levelWidth][levelHeight];
        this.rooms = new Array<>();
        entities = new HashSet<>();

    }

    // METHODS & FUNCTIONS
    // ---------------------------------------------------------------------------------------------
    public boolean init() {

        for (int x = 0; x < levelWidth; x++) {
            for (int y = 0; y < levelHeight; y++) {
                levelData[x][y] = new DefaultTile(new Vector2(x,y));
            }

        }
        return false;
    }

    //TODO: Add border around level.
    //TODO: Forbid touching rooms
    //TODO: Add corridors from room to room.

    public boolean load() {

        Room room;

        do {
            room = new Room(randomInRange(0, levelWidth),
                    randomInRange(0, levelHeight),
                    randomInRange(MIN_ROOM_WIDTH, MAX_ROOM_WIDTH),
                    randomInRange(MIN_ROOM_HEIGHT, MAX_ROOM_HEIGHT));
        }
        while ((room.getxPos() + room.getWidth()) > levelWidth || (room.getHeight() + room.getyPos()) > levelHeight);

        rooms.add(room);

        boolean fits;
        String conflictingRoom;
        for (int i = 0; i < MAX_INSERTION_TRYS; i++) {
            fits = true;
            conflictingRoom = "";
            room = new Room(randomInRange(0, levelWidth),
                    randomInRange(0, levelHeight),
                    randomInRange(MIN_ROOM_WIDTH, MAX_ROOM_WIDTH),
                    randomInRange(MIN_ROOM_HEIGHT, MAX_ROOM_HEIGHT));

            for (Room insertedRoom : rooms) {
                if (insertedRoom.overlaps(room)) {
                    fits = false;
                    conflictingRoom = insertedRoom.toString();
                    break;
                } else if ((room.getxPos() + room.getWidth()) > levelWidth || (room.getHeight() + room.getyPos()) > levelHeight) {
                    fits = false;
                    Gdx.app.log("ROOM OUT OF BOUNDS", room.toString());
                    break;
                } else {
                    continue;
                }
            }

            if (!fits) {
                Gdx.app.log("ROOM DID NOT FIT", room.toString());
                Gdx.app.log("CONFLICTING WITH", conflictingRoom);
            }
            if (fits) {
                rooms.add(room);
                Gdx.app.log("ROOM GENERATED", room.toString());
            }

        }


        for (Room r : rooms) {
            int startX = r.getxPos();
            int endX = startX + r.getWidth();
            int startY = r.getyPos();
            int endY = startY + r.getHeight();

            Gdx.app.log("ROOM TO BE FILLED", r.toString());
            Gdx.app.log("Dimensions(x,y,w,h)", Integer.toString(startX) + "," +   Integer.toString(startY) + "," + Integer.toString(endX) + "," + Integer.toString(endY));


            for (int x = startX; x < endX; x++) {
                for (int y = startY; y < endY; y++) {
                    levelData[x][y] = new Tile(new Vector2(x, y), Tile.TileType.BROWN_s, true);
                }
            }

        }


        placePlayer();

        return true;
    }


    private void placePlayer() {
        int homeRoomNumber = randomInRange(0, rooms.size);
        Room home = rooms.get(homeRoomNumber);
        Gdx.app.log("HOME ROOM", home.toString());
        player = new Player(home.getCenter());
        Gdx.app.log("NUMBER OF ROOMS",Integer.toString(rooms.size));
        Gdx.app.log("-------------------","-----------------------");
        Gdx.app.log("-------------------","-----------------------");

    }


    private int randomInRange(int a, int b) {
        return a + random.nextInt((b - a) + 1);
    }

    public boolean checkBounds(int x, int y) {
        if (x < 0 || x > levelWidth || y < 0 || y > levelHeight) {
            Gdx.app.log("ERROR: ", " OBJECT OUT OF BOUNDS");
            return false;
        }

        return true;

    }

    // GETTER & SETTER
    // ---------------------------------------------------------------------------------------------

    public Tile getTile(int x, int y) {
        return levelData[x][y];
    }


    public HashSet<Entity> getEntities() {
        return entities;
    }

    public int getLevelWidth() {
        return levelWidth;
    }

    public int getLevelHeight() {
        return levelHeight;
    }

    public Player getPlayer() {
        return player;
    }

}
