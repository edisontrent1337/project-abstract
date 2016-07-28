package com.trent.awesomejumper.controller;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.trent.awesomejumper.engine.entity.Entity;
import com.trent.awesomejumper.models.Player;
import com.trent.awesomejumper.models.testing.Room;
import com.trent.awesomejumper.tiles.DefaultTile;
import com.trent.awesomejumper.tiles.Tile;
import com.trent.awesomejumper.utils.Interval;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Random;
import java.util.Stack;

import static java.lang.Float.MAX_VALUE;

/**
 * //TODO: DOCUMENTATION
 * //TODO: implement chunks that use each a new random object with their position as the seed.
 * Controller responsible for generating random levels.
 * Uses procedural random level generation.
 * Created by Sinthu on 22.07.2016.
 */
public class RandomLevelGenerator {

    // MEMBERS & INSTANCES
    // ---------------------------------------------------------------------------------------------

    private final int NORTH = 0;
    private final int EAST = 1;
    private final int SOUTH = 2;
    private final int WEST = 3;

    private final int COORDINATE_ORDER_MODE = 0;
    private final int CLOSEST_NEIGHBOUR_MODE = 1;

    private Tile[][] levelData;
    private ArrayList<Room> rooms;
    private int levelWidth;
    private int levelHeight;

    private HashSet<Entity> entities;
    //TODO: use a map to implement functions like getRoomById()....
    private HashMap<Long, Room> roomMap;
    private Player player;

    /**
     * Seed for the level. All level generation steps should depend on the seed so a level can be
     * recreated using the seed.
     */
    private long seed;

    private Random random;

    /**
     * Minimum and maximum level dimensions
     */
    private final int MIN_LEVEL_WIDTH = 64;
    private final int MIN_LEVEL_HEIGHT = 64;
    private final int MAX_LEVEL_WIDTH = 96;
    private final int MAX_LEVEL_HEIGHT = 96;

    /**
     * Minimum and maximum room dimensions
     */
    private final int MAX_ROOM_RECTANGULARITY = 3;

    private final Interval SMALL_DIMS = new Interval(5, 7);
    private final Interval MEDIUM_DIMS = new Interval(8, 10);
    private final Interval BIG_DIMS = new Interval(12, 14);
    private final Interval GIANT_DIMS = new Interval(16, 20);

    private final Interval SMALL_PROB = new Interval(0, 4);
    private final Interval MEDIUM_PROB = new Interval(5, 64);
    private final Interval BIG_PROB = new Interval(65, 94);
    private final Interval GIANT_PROB = new Interval(95, 99);

    /**
     * Maximum tries for inserting a new room to the existing dungeon.
     */
    private final int MAX_INSERTION_TRIES = 2048;

    private final int MIN_BOUNDS_DISTANCE = 5;

    private int smallRooms;
    private int mediumRooms;
    private int bigRooms;
    private int giantRooms;

    // CONSTRUCTOR
    // ---------------------------------------------------------------------------------------------

    public RandomLevelGenerator() {
        Gdx.app.log("LEVEL", "START LOADING LEVEL");
        seed = System.currentTimeMillis();
        //seed = 1469663651728l;
        random = new Random(seed);
        levelWidth = MIN_LEVEL_WIDTH + random.nextInt((MAX_LEVEL_WIDTH - MIN_LEVEL_WIDTH) / 2) * 2;
        levelHeight = MIN_LEVEL_HEIGHT + random.nextInt((MAX_LEVEL_HEIGHT - MIN_LEVEL_HEIGHT) / 2) * 2;
        Gdx.app.log("LEVEL", "SEED: " + Long.toString(seed));
        Gdx.app.log("LEVEL", "DIMENSIONS:" + "w: " + Integer.toString(levelWidth) + " h: "
                + Integer.toString(levelHeight));

        levelData = new Tile[levelWidth][levelHeight];
        this.rooms = new ArrayList<>();
        entities = new HashSet<>();

    }

    // METHODS & FUNCTIONS
    // ---------------------------------------------------------------------------------------------

    /**
     * Initialises the level data with default tiles everywhere.
     *
     * @return
     */
    public void init() {

        for (int x = 0; x < levelWidth; x++) {
            for (int y = 0; y < levelHeight; y++) {
                levelData[x][y] = new DefaultTile(new Vector2(x, y));
            }

        }
        Gdx.app.log("LEVEL", "INITIALISED LEVEL DATA.");

    }

    //TODO: Add corridors from room to room.
    //TODO: This class must provide a method which returns a collection of all pre-game-play existing
    // entities such as enemies, loot and weapons.

    /**
     * Loads a dungeon using the current seed.
     *
     * @return
     */
    public boolean load() {
        int baseRoomSize; // holds the general size of the room. Random rectangularity is added later.
        /**
         * First phase: generate rooms and store them in an array.
         */
        Room room = new Room();
        /**
         * Adding one legit room to the array to start the for-loop below. This do-while loop is
         * executed as long as the created room is not legit / is out of bounds or too close to the bounds.
         */
        do {

            /**
             * Getting a basic room size.
             */
            baseRoomSize = decideRoomSize(room);
            if (baseRoomSize == -1) {
                Gdx.app.log("ERROR", "INVALID ROOM SIZE. WAS -1");
            }


            room = new Room(randomInRange(0, levelWidth),
                    randomInRange(0, levelHeight),
                    randomInRange(baseRoomSize, baseRoomSize + randomInRange(0, MAX_ROOM_RECTANGULARITY)),
                    randomInRange(baseRoomSize, baseRoomSize + randomInRange(0, MAX_ROOM_RECTANGULARITY)));
        }
        while (tooCloseToBounds(room)); // while too close to the bounds, look for a new room.

        rooms.add(room);

        boolean fits;
        String conflictingRoom;

        /**
         * This for loop is executed 1024 times, trying to add more rooms to the level.
         */
        for (int i = 0; i < MAX_INSERTION_TRIES; i++) {
            fits = true;
            conflictingRoom = "";
            baseRoomSize = decideRoomSize(room);
            room = new Room(randomInRange(0, levelWidth),
                    randomInRange(0, levelHeight),
                    randomInRange(baseRoomSize, baseRoomSize + randomInRange(0, MAX_ROOM_RECTANGULARITY)),
                    randomInRange(baseRoomSize, baseRoomSize + randomInRange(0, MAX_ROOM_RECTANGULARITY)));

            /**
             * Checks, whether the new room overlaps with other existing rooms. If this is the case,
             * the new room will be dismissed.
             */
            for (Room insertedRoom : rooms) {
                if (insertedRoom.overlaps(room)) {
                    fits = false;
                    conflictingRoom = insertedRoom.toString();
                    break;
                } else if (tooCloseToBounds(room)) {
                    fits = false;
                    Gdx.app.log("ROOM OUT OF BOUNDS", room.toString());
                    break;
                }

            }

            if (fits) {
                rooms.add(room);
                Gdx.app.log("ROOM GENERATED", room.toString());
            } else {
               /* Gdx.app.log("ROOM DID NOT FIT", room.toString());
                Gdx.app.log("CONFLICTING WITH", conflictingRoom);*/
            }

        }

        /**
         * Finally filling each room with floor tiles.
         */
        for (Room r : rooms) {
            int startX = r.getxPos();
            int endX = startX + r.getWidth();
            int startY = r.getyPos();
            int endY = startY + r.getHeight();

          /*  Gdx.app.log("ROOM TO BE FILLED", r.toString());
            Gdx.app.log("Dimensions(x,y,w,h)", Integer.toString(startX) + "," + Integer.toString(startY) + "," + Integer.toString(endX) + "," + Integer.toString(endY));*/


            for (int x = startX; x < endX; x++) {
                for (int y = startY; y < endY; y++) {
                    Tile tile = new Tile(new Vector2(x, y), Tile.TileType.BROWN_s, true);
                    tile.setRoomID(r.getID());
                    levelData[x][y] = tile;
                }
            }

            /**
             * Counting rooms by type.
             */
            switch (r.getType()) {
                case SMALL:
                    smallRooms++;
                    break;
                case MEDIUM:
                    mediumRooms++;
                    break;
                case BIG:
                    bigRooms++;
                    break;
                case GIANT:
                    giantRooms++;
                    break;
                default:
                    break;
            }

        }


        /**
         * Calculating nearest neighbours
         */

        for (Room currentRoom : rooms) {
            float distance = 0;
            float minimum = 10000000f;
            for (Room otherRoom : rooms) {
                if (currentRoom.equals(otherRoom))
                    continue;
                distance = currentRoom.getCenter().dst(otherRoom.getCenter());
                if (distance < minimum & !otherRoom.getClosestNeighbour().equals(currentRoom)) {
                    currentRoom.setClosestNeighbour(otherRoom);
                    minimum = distance;
                }
            }

            /*Gdx.app.log("ROOM", Long.toString(currentRoom.getID()));
            Gdx.app.log("NEIGHBOUR" , Long.toString(currentRoom.getClosestNeighbour().getID()));
            Gdx.app.log("DISTANCE", Float.toString(distance));*/
            System.out.format("%5d%5d%10f", currentRoom.getID(), currentRoom.getClosestNeighbour().getID(), distance);
            System.out.println();

        }


        /**
         * The rooms are sorted by their coordinates. Lower x and higher y coordinates are located
         * higher in the list. A custom comparator is used.
         */
        Collections.sort(rooms, new Comparator<Room>() {
            @Override
            public int compare(Room r1, Room r2) {


                int resultX = Float.compare(r1.getxPos(), r2.getxPos());
                int resultY = Float.compare(r1.getyPos(), r2.getyPos());
               /* Gdx.app.log("COMPARE", "START");
                Gdx.app.log(Float.toString(resultX), Float.toString(resultY));*/

                /**
                 * The two rooms are at the exact same position.
                 */
                if (resultX == 0 && resultY == 0) {
                    return 0;
                }
                /**
                 * The first room r1 is located higher in y direction, or
                 * the first room r1 is further to the left in x direction and both rooms are on the
                 * same y height. This means that room r1 comes before room r2.
                 */
                else if ((resultY >= 1) || (resultX < 0 && resultY == 0)) {
                    return -1;
                }
                /**
                 * Otherwise, the first room r1 comes after r2.
                 */
                return 1;


            }
        });


        Gdx.app.log("LEVEL", "STARTING CORRIDOR GENERATION");

        createCorridors(COORDINATE_ORDER_MODE);
        createCorridors(CLOSEST_NEIGHBOUR_MODE);
        /**
         * After all rooms have been filled and sorted, a random room is selected and the player is placed
         * in there.
         */
        placePlayer();
        return true;
    }


    private ArrayList<Tile> calculatePath(Tile from, Tile to) {
        ArrayList<Tile> path = new ArrayList<>();
        Tile current = from;
        while (!current.equals(to)) {
            if (current.getPredecessor() == null)
                Gdx.app.log("PREDESSESOR", "WAS NULL");
            current = current.getPredecessor();
            path.add(current);
        }

        Collections.reverse(path);

        for (Tile t : path)
            Gdx.app.log("PATH", t.getPosition().toString());

        return path;
    }

    private void placePlayer() {
        int homeRoomID = randomInRange(0, rooms.size() - 1);
        Room home = rooms.get(homeRoomID);
        Gdx.app.log("HOME ROOM", home.toString() + "\n CENTER:" + home.getCenter().toString());
        player = new Player(home.getCenter());

        Gdx.app.log("NUMBER OF ROOMS", Integer.toString(rooms.size()));
        Gdx.app.log("-------------------", "-----------------------");
        Gdx.app.log("NUMBER OF SMALL ROOMS", Integer.toString(smallRooms));
        Gdx.app.log("NUMBER OF MEDIUM ROOMS", Integer.toString(mediumRooms));
        Gdx.app.log("NUMBER OF BIG ROOMS", Integer.toString(bigRooms));
        Gdx.app.log("NUMBER OF GIANT ROOMS", Integer.toString(giantRooms));
    }


    /**
     * Returns a random value within the range [a,b]. Uses the seed to generate this random number.
     *
     * @param a
     * @param b
     * @return random value
     */
    private int randomInRange(int a, int b) {
        if (a == b) {
            return a;
        }

        if (a < b) {
            return a + random.nextInt((b - a) + 1);
        } else {
            return b + random.nextInt((a - b) + 1);
        }
    }

    private int randomInRange(float a, float b) {
        return randomInRange(Math.round(a), Math.round(b));
    }


    private int decideRoomSize(Room room) {
        int probability = random.nextInt(100);
        if (SMALL_PROB.contains(probability)) {
            room.setType(Room.Type.SMALL);
            return randomInRange(SMALL_DIMS.min, SMALL_DIMS.max);

        } else if (MEDIUM_PROB.contains(probability)) {
            room.setType(Room.Type.MEDIUM);
            return randomInRange(MEDIUM_DIMS.min, MEDIUM_DIMS.max);
        } else if (BIG_PROB.contains(probability)) {
            room.setType(Room.Type.BIG);
            return randomInRange(BIG_DIMS.min, BIG_DIMS.max);
        } else if (GIANT_PROB.contains(probability)) {
            room.setType(Room.Type.GIANT);
            return randomInRange(GIANT_DIMS.min, GIANT_DIMS.max);
        }


        return -1;
    }


    /**
     * Checks whether the given coordinates x and y are contains the bounds of the level.
     *
     * @param x
     * @param y
     * @return true if contains the bounds, false otherwise.
     */
    public boolean checkBounds(float x, float y) {
        return !(x < 0 || x >= levelWidth || y < 0 || y >= levelHeight);

    }

    public boolean checkBounds(Vector2 test) {
        return checkBounds(test.x, test.y);
    }

    /**
     * Uses the MIN_BOUNDS_DISTANCE to determine whether a room is too close to the level bounds.
     *
     * @param room
     * @return true if the room is too close, false otherwise.
     */
    private boolean tooCloseToBounds(Room room) {
        if (room.getxDimensions().min - MIN_BOUNDS_DISTANCE < 0 || room.getxDimensions().max + MIN_BOUNDS_DISTANCE > levelWidth
                || room.getyDimensions().min - MIN_BOUNDS_DISTANCE < 0 || room.getyDimensions().max + MIN_BOUNDS_DISTANCE > levelHeight)
            return true;
        return false;
    }


    private void markRoomTilesAsVisited(Room r, boolean visited) {
        int startX = r.getxPos();
        int endX = startX + r.getWidth();
        int startY = r.getyPos();
        int endY = startY + r.getHeight();

        for (int x = startX; x < endX; x++) {
            for (int y = startY; y < endY; y++) {
                levelData[x][y].setVisited(visited);
            }
        }
    }

    private void markRoomAsVisted(Room r) {
        r.setVisited();
    }


    private void createVerticalCorridor(float yPos1, float yPos2, int x) {

        int start = (int) Math.min(yPos1, yPos2);
        int end = (int) Math.max(yPos1, yPos2);
        Gdx.app.log("VERTICAL FROM", Integer.toString(start) + " to " + Integer.toString(end));

        for (int y = start; y <= end; y++) {
            levelData[x][y] = new Tile(new Vector2(x, y), Tile.TileType.BROWN_s, true);
        }

    }

    private void createHorizontalCorridor(float xPos1, float xPos2, int y) {
        int start = (int) Math.min(xPos1, xPos2);
        int end = (int) Math.max(xPos1, xPos2);

        Gdx.app.log("HORIZONTAL FROM", Integer.toString(start) + " to " + Integer.toString(end));

        for (int x = start; x <= end; x++) {
            levelData[x][y] = new Tile(new Vector2(x, y), Tile.TileType.BROWN_s, true);
        }
    }


    private void createCorridors(int mode) {

        for (int i = 0; i < rooms.size(); i++) {
            Room currentRoom = rooms.get(i);
            currentRoom.setVisited();
            markRoomTilesAsVisited(currentRoom, true);
            Gdx.app.log("START ROOM", currentRoom.toString());

            Vector2 doorAPosition = new Vector2(0, 0);
            Vector2 doorBPosition = new Vector2(0, 0);

            int doorDirectionA = 0;
            int doorDirectionB = 0;

            float distance;
            float minimum = Float.MAX_VALUE;
            Room next;
            for (Room.Door d : currentRoom.getDoors()) {
                if (mode == CLOSEST_NEIGHBOUR_MODE) {
                    next = currentRoom.getClosestNeighbour();
                } else {
                    next = rooms.get((i + 1) % rooms.size());
                }

                for (Room.Door otherDoor : next.getDoors()) {
                    Vector2 doorConnection = new Vector2(d.getX() - otherDoor.getX(), d.getY() - otherDoor.getY());
                    distance = doorConnection.len();
                    if (distance < minimum) {
                        minimum = distance;
                        doorAPosition = new Vector2(d.getX(), d.getY());
                        doorBPosition = new Vector2(otherDoor.getX(), otherDoor.getY());

                        doorDirectionA = d.getDirection();
                        doorDirectionB = otherDoor.getDirection();
                    }
                }

            }


            float xDistance = Math.abs(doorAPosition.x - doorBPosition.x);
            float yDistance = Math.abs(doorAPosition.y - doorBPosition.y);
            int xStart, xEnd, yStart, yEnd, xHalf, yHalf;

            if ((doorDirectionA == EAST || doorDirectionA == WEST) && (doorDirectionB == EAST || doorDirectionB == WEST)) {
                xStart = (int) (Math.min(doorAPosition.x, doorBPosition.x));
                xHalf = xStart + (int) Math.floor(xDistance / 2);
                xEnd = (int) Math.max(doorBPosition.x, doorAPosition.x);


                if (doorAPosition.x < doorBPosition.x) {
                    yStart = (int) doorAPosition.y;
                    yEnd = (int) doorBPosition.y;
                } else {
                    yStart = (int) doorBPosition.y;
                    yEnd = (int) doorAPosition.y;
                }


                System.out.format("\n Start: %3d Mid: %3d End %3d", xStart, xHalf, xEnd);

                createHorizontalCorridor(xStart, xHalf, yStart);
                createVerticalCorridor(doorAPosition.y, doorBPosition.y, xHalf);
                createHorizontalCorridor(xHalf, xEnd, yEnd);
            } else if ((doorDirectionA == NORTH || doorDirectionA == SOUTH) && (doorDirectionB == NORTH || doorDirectionB == SOUTH)) {
                yStart = (int) Math.min(doorAPosition.y, doorBPosition.y);
                yHalf = yStart + (int) Math.floor(yDistance / 2);
                yEnd = (int) Math.max(doorBPosition.y, doorAPosition.y);

                if (doorAPosition.y < doorBPosition.y) {
                    xStart = (int) doorAPosition.x;
                    xEnd = (int) doorBPosition.x;

                } else {
                    xStart = (int) doorBPosition.x;
                    xEnd = (int) doorAPosition.x;

                }

                System.out.format("\n Start: %3d Mid: %3d End %3d", yStart, yHalf, yEnd);

                createVerticalCorridor(yStart, yHalf, xStart);
                createHorizontalCorridor(doorAPosition.x, doorBPosition.x, yHalf);
                createVerticalCorridor(yHalf, yEnd, xEnd);
            } else if ((doorDirectionA == NORTH || doorDirectionA == SOUTH) && (doorDirectionB == WEST || doorDirectionB == EAST)) {
                xStart = (int) Math.min(doorBPosition.x, doorAPosition.x);
                xEnd = (int) Math.max(doorBPosition.x, doorAPosition.x);

                if (doorAPosition.x < doorBPosition.x) {
                    yStart = (int) doorAPosition.y;
                    yEnd = (int) doorBPosition.y;
                } else {
                    yStart = (int) doorBPosition.y;
                    yEnd = (int) doorAPosition.y;
                }


                createVerticalCorridor(yStart, yEnd, (int) doorAPosition.x);
                createHorizontalCorridor(xStart, xEnd, (int) doorBPosition.y);

            }


        }

    }

    // GETTER & SETTER
    // ---------------------------------------------------------------------------------------------

    public Tile getTile(int x, int y) {
        return levelData[x][y];
    }

    public Tile getTile(Vector2 position) {
        return levelData[(int) position.x][(int) position.y];
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
