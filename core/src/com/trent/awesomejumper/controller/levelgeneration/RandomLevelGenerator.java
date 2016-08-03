package com.trent.awesomejumper.controller.levelgeneration;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.trent.awesomejumper.engine.entity.Entity;
import com.trent.awesomejumper.models.Player;
import com.trent.awesomejumper.tiles.DefaultTile;
import com.trent.awesomejumper.tiles.Tile;
import com.trent.awesomejumper.utils.Interval;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;

import static com.trent.awesomejumper.controller.levelgeneration.LevelConstants.*;
import static com.trent.awesomejumper.tiles.Tile.TileType.*;
import static com.trent.awesomejumper.controller.levelgeneration.Room.Type.*;

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

    private final int COORDINATE_ORDER_MODE = 0;
    private final int CLOSEST_NEIGHBOUR_MODE = 1;

    private Tile[][] levelData;
    private ArrayList<Room> rooms;
    private int levelWidth;
    private int levelHeight;

    private HashSet<Entity> entities;
    //TODO: use a map to implement functions like getRoomById()....
    //TODO: insert smaller walls inside rooms to make them more random
    private HashMap<Long, Room> roomMap;
    private Player player;
    private ArrayList<Vector2> roomCorners;

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

    private final Interval TINY_PROB = new Interval(0, 14); // 15
    private final Interval SMALL_PROB = new Interval(15, 39); // 25
    private final Interval MEDIUM_PROB = new Interval(40, 79);  // 40
    private final Interval BIG_PROB = new Interval(80, 94); // 15
    private final Interval GIANT_PROB = new Interval(95, 99); // 5

    /**
     * Maximum tries for inserting a new room to the existing dungeon.
     */
    private final int MAX_INSERTION_TRIES = 32768;

    private final int MIN_BOUNDS_DISTANCE = 11;

    /**
     * Number of small, medium, big and giant rooms.
     */
    private int tinyRooms;
    private int smallRooms;
    private int mediumRooms;
    private int bigRooms;
    private int giantRooms;

    /**
     * Array that keeps track of the region a tile in the level belongs to.
     * This is used to ensure connectivity in the dungeon.
     */
    private int[][] regions;
    private int totalRegions = 0;
    private int start;
    private int end;
    private int constant;
    private boolean xAddressFirst;

    // CONSTRUCTOR
    // ---------------------------------------------------------------------------------------------

    public RandomLevelGenerator() {
        Gdx.app.log("LEVEL", "START LOADING LEVEL");
        //seed = System.currentTimeMillis();
        seed = 1470169692227l;
        random = new Random(seed);
        levelWidth = MIN_LEVEL_WIDTH + random.nextInt((MAX_LEVEL_WIDTH - MIN_LEVEL_WIDTH) / 2) * 2 + 1;
        levelHeight = MIN_LEVEL_HEIGHT + random.nextInt((MAX_LEVEL_HEIGHT - MIN_LEVEL_HEIGHT) / 2) * 2 + 1;
        /*levelWidth = multipleOfThree(MIN_LEVEL_WIDTH, MAX_LEVEL_WIDTH);
        levelWidth = multipleOfThree(MIN_LEVEL_HEIGHT, MAX_LEVEL_HEIGHT);*/

        Gdx.app.log("LEVEL", "SEED: " + Long.toString(seed));
        Gdx.app.log("LEVEL", "DIMENSIONS:" + "w: " + Integer.toString(levelWidth) + " h: "
                + Integer.toString(levelHeight));

        this.levelData = new Tile[levelWidth][levelHeight];
        this.regions = new int[levelWidth][levelHeight];
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
                regions[x][y] = -1;
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
        Room.Type baseRoomType; // holds the general size of the room. Random rectangularity is added later.
        int rectangularity;
        /**
         * First phase: generate rooms and store them in an array.
         */
        Room room;
        /**
         * Adding one legit room to the array to start the for-loop below. This do-while loop is
         * executed as long as the created room is not legit / is out of bounds or too close to the bounds.
         */
        do {

            /**
             * Getting a basic room size.
             */
            baseRoomType = decideRoomType();

            room = new Room(multipleOfThree(2, levelWidth),
                    multipleOfThree(2, levelHeight),
                    baseRoomType.size,
                    baseRoomType.size);
            room.setType(baseRoomType);
        }
        while (tooCloseToBounds(room)); // while too close to the bounds, look for a new room.

        rooms.add(room);

        boolean fits;


        /**
         * This for loop is executed 4096 times, trying to add more rooms to the level.
         */
        for (int i = 0; i < MAX_INSERTION_TRIES; i++) {
            fits = true;
            baseRoomType = decideRoomType();

            room = new Room(multipleOfThree(2, levelWidth),
                    multipleOfThree(2, levelHeight),
                    baseRoomType.size,
                    baseRoomType.size);
            room.setType(baseRoomType);
            /**
             * Checks, whether the new room overlaps with other existing rooms. If this is the case,
             * the new room will be dismissed.
             */
            for (Room insertedRoom : rooms) {
                if (insertedRoom.overlaps(room)) {
                    fits = false;
                    break;
                } else if (tooCloseToBounds(room)) {
                    fits = false;
                    //Gdx.app.log("ROOM OUT OF BOUNDS", room.toString());
                    break;
                }

            }

            if (fits) {
                rooms.add(room);


                Gdx.app.log("ROOM GENERATED", room.toString());
            }

        }

        /**
         * Finally filling each room with floor tiles.
         */
        roomCorners = new ArrayList<>();

        for (Room r : rooms) {
            int startX = r.getxPos();
            int endX = startX + r.getWidth();
            int startY = r.getyPos();
            int endY = startY + r.getHeight();

            roomCorners.add(new Vector2(startX - 1, startY));
            roomCorners.add(new Vector2(startX - 2, startY));
            roomCorners.add(new Vector2(startX, startY - 1));
            roomCorners.add(new Vector2(startX, startY - 2));

            roomCorners.add(new Vector2(endX - 1, startY - 1));
            roomCorners.add(new Vector2(endX - 1, startY - 2));
            roomCorners.add(new Vector2(endX, startY));
            roomCorners.add(new Vector2(endX + 1, startY));

            roomCorners.add(new Vector2(startX - 1, endY - 1));
            roomCorners.add(new Vector2(startX - 2, endY - 1));
            roomCorners.add(new Vector2(startX, endY));
            roomCorners.add(new Vector2(startX, endY + 1));

            roomCorners.add(new Vector2(endX - 1, endY));
            roomCorners.add(new Vector2(endX - 1, endY + 1));
            roomCorners.add(new Vector2(endX, endY - 1));
            roomCorners.add(new Vector2(endX + 1, endY - 1));


            System.out.format("\n X: %3d %3d Y: %3d %3d", startX, endX, startY, endY);

          /*  Gdx.app.log("ROOM TO BE FILLED", r.toString());
            Gdx.app.log("Dimensions(x,y,w,h)", Integer.toString(startX) + "," + Integer.toString(startY) + "," + Integer.toString(endX) + "," + Integer.toString(endY));*/


            for (int x = startX; x < endX; x++) {
                for (int y = startY; y < endY; y++) {
                    Tile tile = new Tile(new Vector2(x, y), Tile.TileType.FLOOR, true);
                    tile.setRoomID(r.getID());
                    levelData[x][y] = tile;
                    regions[x][y] = totalRegions;

                }
            }
            totalRegions++;
            /**
             * Counting rooms by type.
             */
            switch (r.getType()) {
                case TINY:
                    tinyRooms++;
                    break;
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


        for (Vector2 v : roomCorners) {
            Gdx.app.log("CORNER", v.toString());
        }

        /**
         * Calculating nearest neighbours
         */

        for (Room currentRoom : rooms) {
            float distance = 0;
            float minimum = Float.MAX_VALUE;
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
            System.out.format("%5d%5d%10f", currentRoom.getType().size, currentRoom.getClosestNeighbour().getID(), distance);
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


        for (int x = MIN_BOUNDS_DISTANCE; x < levelWidth - MIN_BOUNDS_DISTANCE; x += 3) {
            for (int y = MIN_BOUNDS_DISTANCE; y < levelHeight - MIN_BOUNDS_DISTANCE; y += 3) {
                Vector2 position = new Vector2(x, y);
                if (!getTile(position).getType().equals(WALL)) {
                    continue;
                }
                generateMaze(position);
            }
        }

        // connectRegions();
       /*removeExtraDoors();
        removeDeadEnds();*/
        /*createCorridors(COORDINATE_ORDER_MODE);
        createCorridors(CLOSEST_NEIGHBOUR_MODE);*/
        /**
         * After all rooms have been filled and sorted, a random room is selected and the player is placed
         * in there.
         */
        placePlayer();
        return true;
    }


    public void removeExtraDoors() {
        for (Room r : rooms) {
            int startX = r.getxPos() - 1;
            int startY = r.getyPos() - 1;
            int endX = startX + r.getWidth() + 2;
            int endY = startY + r.getHeight() + 2;
            // north wall
            decideDoor(r.getDoors().get(NORTH), startX, endX, endY, true);
            // east wall
            decideDoor(r.getDoors().get(EAST), startY, endY, endX, false);
            // south wall
            decideDoor(r.getDoors().get(SOUTH), startX, endX, startY, true);
            // west wall
            decideDoor(r.getDoors().get(WEST), startY, endY, startX, false);

        }
    }

    private void decideDoor(Room.Door door, int start, int end, int constant, boolean xAddressFirst) {
        ArrayList<Vector2> doorList = new ArrayList<>();
        Vector2 remainingDoor;
        for (int i = start; i < end; i++) {
            if (xAddressFirst) {
                if (getTile(i, constant).getType() == FLOOR) {
                    doorList.add(new Vector2(i, constant));
                }
            } else {
                if (getTile(constant, i).getType() == FLOOR) {
                    doorList.add(new Vector2(constant, i));
                }
            }
        }

        if (doorList.size() < 1)
            return;

        remainingDoor = doorList.get(0);
        float min = Float.MAX_VALUE;
        for (Vector2 d : doorList) {
            float dst = d.dst(door.position);
            if (dst < min) {
                min = dst;
                remainingDoor = d;
            }

        }

        //remainingDoor = doorList.get(random.nextInt(doorList.size()));

        for (Vector2 otherDoor : doorList) {
            if (otherDoor.equals(remainingDoor))
                continue;

            setLevelData(otherDoor, WALL);

        }
    }

    public void connectRegions() {
        /**
         * Maps a position to a set of integers. The position is mapped to the region numbers of
         * of the surrounding tiles. If we find 2 or more different region numbers, the
         * tile on the position is a connector.
         * TODO: remove duplicates from HashSet. Not two of the same regions should be inside the HashSet <Array<Integer>>
         */

        Gdx.app.log("EVENT", "CONNECTING REGIONS");
        HashMap<Vector2, HashMap<Integer, Integer>> connectors = new HashMap<>();
        //HashMap<Vector2, HashSet<Integer>> connectors = new HashMap();

        for (int x = 0; x < levelWidth; x++) {
            for (int y = 0; y < levelHeight; y++) {

                /**
                 * If the current position is one of the rooms corners, it is not considered
                 * to be a potential connector.
                 */
                if (roomCorners.contains(new Vector2(x, y)))
                    continue;
                // for (Room r : rooms) {
                //    for (Room.Door d : r.getDoors()) {
                // If the current tile is not a wall, it can't be a connector between two regions.
               /* if (!getTile(x, y).getType().equals(Tile.TileType.WALL))
                    continue;*/
                HashMap<Integer, Integer> adjacentRegions = new HashMap<>();
                // HashSet<Integer> adjacentRegions = new HashSet<>();
                //Vector2 regionAndDirection = new Vector2(0,0);

                /**
                 * Check all 4 cardinal neighbours from the current position [x,y]
                 */
                for (int i = 0; i < CARDINALS.length; i++) {
                    Vector2 direction = CARDINALS[i];
                    Vector2 destination = new Vector2(x, y).add(direction.cpy().scl(2));
                    //Vector2 destination = d.position.cpy().add(direction.cpy().scl(2));
                    if (checkBounds(destination)) {
                        int regionAtDestination = getRegion(destination);
                        if (regionAtDestination != -1) {
                            if (!adjacentRegions.values().contains(regionAtDestination))
                                adjacentRegions.put(i, regionAtDestination);
                        }

                    }

                }

                if (adjacentRegions.size() < 2)
                    continue;

                connectors.put(new Vector2(x, y), adjacentRegions);
                //connectors.put(d.position.cpy(), adjacentRegions);


            }
        }

        for (Vector2 test : connectors.keySet()) {
            Gdx.app.log("POSITION:" + test.toString(), " DIRECTIONS AROUND:" + connectors.get(test).toString());
        }

        // Retrieve a list of all potential connectors.

        int[] mergedTo = new int[totalRegions];
        HashSet<Integer> openRegions = new HashSet<>();

        for (int i = 0; i < totalRegions; i++) {
            // At the beginning, region i is merged to itself.
            mergedTo[i] = i;
            // Also, all regions are open/ can be merged.
            openRegions.add(i);
        }


        while (openRegions.size() > 1) {

            ArrayList<Vector2> connectorList = new ArrayList<>(connectors.keySet());
            Vector2 connectorPosition = connectorList.get(random.nextInt(connectorList.size()));
            // Gdx.app.log("CHOSE FOLLOWING CONNECTOR", connectorPosition.toString());
            ArrayList<Integer> directionList = new ArrayList<>(connectors.get(connectorPosition).keySet());
            ArrayList<Integer> regionList = new ArrayList<>(connectors.get(connectorPosition).values());
            // Gdx.app.log("DIRECTIONS", directionList.toString());
            // Gdx.app.log("REGIONS", regionList.toString());

            int direction = directionList.get(random.nextInt(directionList.size()));


            addJunction(connectorPosition, direction);

            int destination = regionList.get(0);
            regionList.remove(Integer.valueOf(destination));

            /*Gdx.app.log("DESTINATION", Integer.toString(destination));
            Gdx.app.log("SOURCES", regionList.toString());*/

            for (int i = 0; i < totalRegions; i++) {
                if (regionList.contains(mergedTo[i]))
                    mergedTo[i] = destination;

            }


            /*for (int i = 0; i < mergedTo.length; i++) {
                Gdx.app.log(Integer.toString(i), "MAPPED TO " + Integer.toString(mergedTo[i]));
            }*/


            openRegions.removeAll(regionList);

            Collection<Integer> originalRegions = connectors.get(connectorPosition).values();
            HashSet<Integer> regionsLeft = new HashSet<>();
            for (Integer i : originalRegions) {
                regionsLeft.add(mergedTo[i]);
            }

            for (Iterator<Map.Entry<Vector2, HashMap<Integer, Integer>>> it = connectors.entrySet().iterator(); it.hasNext(); ) {
                Map.Entry<Vector2, HashMap<Integer, Integer>> entry = it.next();
                if (entry.getKey().dst(connectorPosition) < 2) {
                    it.remove();
                }
            }


            if (regionsLeft.size() < 2) {
                connectors.remove(connectorPosition);
            }


        }


    }


    public void removeDeadEnds() {
        boolean done = false;

        while (!done) {
            done = true;
            for (int x = 0; x < levelWidth; x++) {
                for (int y = 0; y < levelHeight; y++) {
                    if (getTile(x, y).getType() == WALL)
                        continue;

                    int exits = 0;
                    for (Vector2 c : CARDINALS) {
                        if (getTile(new Vector2(x, y).add(c)).getType() != WALL)
                            exits++;
                    }

                    if (exits != 1)
                        continue;

                    done = false;

                    setLevelData(new Vector2(x, y), WALL);


                }
            }


        }


    }

    private void addJunction(Vector2 connector, int direction) {

        if (direction == NORTH || direction == SOUTH) {
            setLevelData(connector, FLOOR);
            setLevelData(connector.cpy().add(N_DIR), FLOOR);
            setLevelData(connector.cpy().add(S_DIR), FLOOR);
        } else {
            setLevelData(connector, FLOOR);
            setLevelData(connector.cpy().add(W_DIR), FLOOR);
            setLevelData(connector.cpy().add(E_DIR), FLOOR);
        }
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
        int homeRoomID = random.nextInt(rooms.size());
        Room home = rooms.get(homeRoomID);
        Gdx.app.log("HOME ROOM", home.toString() + "\n CENTER:" + home.getCenter().toString());
        player = new Player(home.getCenter());

        Gdx.app.log("NUMBER OF ROOMS", Integer.toString(rooms.size()));
        Gdx.app.log("-------------------", "-----------------------");
        Gdx.app.log("NUMBER OF TINY ROOMS", Integer.toString(tinyRooms));
        Gdx.app.log("NUMBER OF SMALL ROOMS", Integer.toString(smallRooms));
        Gdx.app.log("NUMBER OF MEDIUM ROOMS", Integer.toString(mediumRooms));
        Gdx.app.log("NUMBER OF BIG ROOMS", Integer.toString(bigRooms));
        Gdx.app.log("NUMBER OF GIANT ROOMS", Integer.toString(giantRooms));
        Gdx.app.log("-------------------", "-----------------------");
        Gdx.app.log("NUMBER OF REGIONS", Integer.toString(totalRegions));


    }


    /**
     * Returns a random odd value within the range [a,b]. Uses the seed to generate this random number.
     *
     * @param a
     * @param b
     * @return random value
     */
    private int rndOddInRange(int a, int b) {
        if (a == b) {
            return a;
        }

        if (a < b) {
            // return 2 * (a + random.nextInt(((b - a) + 1) / 2)) + 1;

            return 2 * (a / 2 + random.nextInt((b - a) / 2) + 1) + 1;
        } else {
            return 2 * (b / 2 + random.nextInt((a - b) / 2) + 1) + 1;
        }
    }

    private int rndEvenInRange(int a, int b) {

        if (a < b) {
            return 2 * (a / 2 + random.nextInt(((b - a) / 2) + 1));
        } else {
            return 2 * (b / 2 + random.nextInt(((a - b) / 2) + 1));
        }

    }


    private int multipleOfThree(int a, int b) {

        if (a < b) {
            return 3 * (a / 3 + random.nextInt(((b - a) / 3) + 1)) - 1;
        } else {
            return 3 * (b / 2 + random.nextInt(((a - b) / 3) + 1)) - 1;
        }


    }

    private int rndOddInRange(float a, float b) {
        return rndOddInRange(Math.round(a), Math.round(b));
    }


    private Room.Type decideRoomType() {
        int probability = random.nextInt(100);
        if (TINY_PROB.contains(probability))
            return TINY;
        else if (SMALL_PROB.contains(probability))
            return SMALL;

        else if (MEDIUM_PROB.contains(probability))
            return MEDIUM;
        else if (BIG_PROB.contains(probability))
            return BIG;
        else
            return GIANT;

    }


    /**
     * Generates a maze between the rooms using the growing tree algorithm.
     *
     * @param start
     */
    public void generateMaze(Vector2 start) {

        LinkedList<Vector2> cells = new LinkedList<>();
        setLevelData(new Tile(start.cpy(), Tile.TileType.FLOOR, true));
        cells.add(start);

        // The maze opens a new region, so the total number of regions is increased.


        Vector2 lastDirection = N_DIR;
        while (!cells.isEmpty()) {
            Vector2 currentPos = cells.getLast();
            /*Gdx.app.log("NEXT CELL", "------------------");
            Gdx.app.log("CURRENT POS", currentPos.toString());
            Gdx.app.log("CELLS SIZE", Integer.toString(cells.size()));*/
            // Get all the valid neighbour tiles
            ArrayList<Vector2> adjacentDirections = checkNeighbours(currentPos);
            //Gdx.app.log("ADJACENT SIZE", Integer.toString(adjacentDirections.size()));

            // If there are neighbours, one
            if (!adjacentDirections.isEmpty()) {
                Vector2 dir;
                if (adjacentDirections.contains(lastDirection) && random.nextInt(100) > 60)
                    dir = lastDirection;
                else
                    dir = adjacentDirections.get(random.nextInt(adjacentDirections.size())).cpy();
                Vector2 adjacentPos = currentPos.cpy().add(dir);
                Vector2 posAfter = adjacentPos.cpy().add(dir);

               /* Gdx.app.log("DIRECTION BEFORE SET LEVEL DATA", dir.toString());
                Gdx.app.log("ADJACENT POS", adjacentPos.toString());
                Gdx.app.log("POS AFTER", posAfter.toString());*/
                setLevelData(new Tile(adjacentPos, Tile.TileType.FLOOR, true));
                setLevelData(new Tile(posAfter, Tile.TileType.FLOOR, true));
                setLevelData(new Tile(posAfter.cpy().add(dir), Tile.TileType.FLOOR, true));
                //setLevelData(new Tile(posAfter.cpy().add(dir).cpy().add(dir), Tile.TileType.FLOOR, true));

               /* Gdx.app.log("TYPE 1", Integer.toString(getTile(adjacentPos).getType().value));
                Gdx.app.log("TYPE 2", Integer.toString(getTile(posAfter).getType().value));*/

                //cells.add(new Vector2(posAfter));
                cells.add(new Vector2(posAfter.cpy().add(dir)));
                //cells.add(new Vector2(posAfter.cpy().add(dir).cpy().add(dir)));
                lastDirection = dir;
            } else {
                cells.removeLast();
            }

        }
        totalRegions++;
    }


    /**
     * Returns a list of directions in which adjacent tiles exist. Checks whether or not a neighbour
     * is inside the level bounds before adding it to the list.
     *
     * @param position position of the tile to be investigated.
     * @return list of neighbour directions.
     */
    private ArrayList<Vector2> checkNeighbours(Vector2 position) {

        ArrayList<Vector2> neighbourDirections = new ArrayList<>();
        // Checking all 4 cardinal directions
        for (Vector2 dir : CARDINALS) {
            // add the direction to the tiles position to get to the neighbour tile.
            Vector2 neighbourAddress = position;
            if (canBeCarved(neighbourAddress, dir)) {
                // If there exists a adjacent tile, add the direction in which it lies to the array.
                neighbourDirections.add(dir);
            }
        }
        return neighbourDirections;
    }

    /**
     * Checks whether the given coordinates x and y are inside the bounds of the level.
     *
     * @param x
     * @param y
     * @return true if inside the bounds, false otherwise.
     */
    public boolean checkBounds(float x, float y) {
        return (x >= 0 && x < levelWidth && y >= 0 && y < levelHeight);

    }

    public boolean checkBounds(Vector2 test) {
        return checkBounds(test.x, test.y);
    }


    private boolean canBeCarved(Vector2 position, Vector2 direction) {
        /*Gdx.app.log("CARVING", "START");
        Gdx.app.log("POSITION", position.toString());
        Gdx.app.log("DIRECTION", direction.toString());*/
        boolean noRoomsAround = true;

        if (tooCloseToBounds((position.cpy().add(direction.cpy().scl(4))))) { // 3
            //Gdx.app.log("EXIT", "FALSE");
            return false;
        }
        return getTile(position.cpy().add(direction.cpy().scl(3))).getType().equals(WALL); // 2

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


    private boolean tooCloseToBounds(Vector2 position) {
        if (position.x - MIN_BOUNDS_DISTANCE < 0 || position.x + MIN_BOUNDS_DISTANCE > levelWidth
                || position.y - MIN_BOUNDS_DISTANCE < 0 || position.y + MIN_BOUNDS_DISTANCE > levelHeight) {
            return true;
        }
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
            Tile newTile = new Tile(new Vector2(x, y), Tile.TileType.FLOOR, true);
            setLevelData(newTile);
        }

    }

    private void createHorizontalCorridor(float xPos1, float xPos2, int y) {
        int start = (int) Math.min(xPos1, xPos2);
        int end = (int) Math.max(xPos1, xPos2);

        Gdx.app.log("HORIZONTAL FROM", Integer.toString(start) + " to " + Integer.toString(end));

        for (int x = start; x <= end; x++) {
            Tile newTile = new Tile(new Vector2(x, y), Tile.TileType.FLOOR, true);
            setLevelData(newTile);
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

            if ((doorDirectionA == NORTH || doorDirectionA == WEST) && (doorDirectionB == EAST || doorDirectionB == WEST)) {
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


    /**
     * Inserts the specified tile into the level data. Checks, whether the tiles coordinates are whole numbers
     * and inside the level bounds before trying to access the levelData array.
     * Also maps a region number to the specified tile.
     *
     * @param tile tile to be added
     */
    private void setLevelData(Tile tile) {
        Vector2 address = tile.getPosition().cpy();
        if (address.x != Math.ceil(address.x) || address.y != Math.ceil(address.y)) {
            Gdx.app.log("ERROR", "COULD NOT INSERT LEVEL DATA AT" + address.toString());
            Gdx.app.log("ERROR", "THE ADRESS DOES NOT CONSIST OF WHOLE NUMBERS.");
            return;
        }
        int x = (int) address.x;
        int y = (int) address.y;
        if (!checkBounds(x, y)) {
            System.out.format("ERROR: ADDRESS %1d | %1d LAYS OUTSIDE BOUNDS", x, y);
        } else {
            /**
             * Set level data and set region id.
             */
            levelData[x][y] = tile;
            regions[x][y] = totalRegions;
        }
    }


    private void setLevelData(Vector2 position, Tile.TileType type) {
        Tile t;
        switch (type) {
            case WALL:
                t = new DefaultTile(position);
                setLevelData(t);
                break;
            case FLOOR:
                t = new Tile(position, Tile.TileType.FLOOR, true);
                setLevelData(t);
                break;
            default:
                break;

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

    public int getRegion(float x, float y) {
        try {
            return regions[(int) x][(int) y];
        } catch (ArrayIndexOutOfBoundsException e) {
            // Gdx.app.log("ERROR", e.toString());
        }

        return -1;
    }

    public int getRegion(Vector2 position) {
        return getRegion(position.x, position.y);
    }

}
