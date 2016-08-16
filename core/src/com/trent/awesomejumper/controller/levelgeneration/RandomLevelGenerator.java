package com.trent.awesomejumper.controller.levelgeneration;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.trent.awesomejumper.engine.entity.Entity;
import com.trent.awesomejumper.models.Player;
import com.trent.awesomejumper.models.testing.Chest;
import com.trent.awesomejumper.models.weapons.Pistol;
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
 * Controller responsible for generating random levels.
 * Uses procedural random level generation.
 * Created by Sinthu on 22.07.2016.
 */
public class RandomLevelGenerator {

    // MEMBERS & INSTANCES
    // ---------------------------------------------------------------------------------------------

    private Tile[][] levelData;
    private ArrayList<Room> rooms;
    private int levelWidth;
    private int levelHeight;

    //private HashSet<Entity> entities;
    private HashMap<Integer,Entity> entities;
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
    private final int MIN_LEVEL_WIDTH = 72;
    private final int MIN_LEVEL_HEIGHT = 72;
    private final int MAX_LEVEL_WIDTH = 96;
    private final int MAX_LEVEL_HEIGHT = 96;


    /**
     * Probabilities for room sizes.
     * 15 % for tiny room
     * 25 % for small room
     * 40 % for medium room
     * 15 % for big room
     * 5 % for giant room.
     */
    private final Interval TINY_PROB = new Interval(0, 14); // 15
    private final Interval SMALL_PROB = new Interval(15, 39); // 25
    private final Interval MEDIUM_PROB = new Interval(40, 79);  // 40
    private final Interval BIG_PROB = new Interval(80, 94); // 15
    private final Interval GIANT_PROB = new Interval(95, 99); // 5

    /**
     * Maximum tries for inserting a new room to the existing dungeon.
     */
    private final int MAX_INSERTION_TRIES = 1024;

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
    /**
     * Number of different regions. A region can be a room, a corridor, or a connection of multiple
     * rooms / corridors.
     */
    private int totalRegions = 0;

    // CONSTRUCTOR
    // ---------------------------------------------------------------------------------------------

    public RandomLevelGenerator() {
        Gdx.app.log("LEVEL", "START LOADING LEVEL");
        /**
         * Init seed and level size. The level dimensions must be odd.
         */
        seed = System.currentTimeMillis();
        //seed = 1470478995911l;//
        random = new Random(seed);
        levelWidth = MIN_LEVEL_WIDTH + random.nextInt((MAX_LEVEL_WIDTH - MIN_LEVEL_WIDTH) / 2) * 2 + 1;
        levelHeight = MIN_LEVEL_HEIGHT + random.nextInt((MAX_LEVEL_HEIGHT - MIN_LEVEL_HEIGHT) / 2) * 2 + 1;

        Gdx.app.log("LEVEL", "SEED: " + Long.toString(seed));
        Gdx.app.log("LEVEL", "DIMENSIONS:" + "w: " + Integer.toString(levelWidth) + " h: "
                + Integer.toString(levelHeight));

        this.levelData = new Tile[levelWidth][levelHeight];
        this.regions = new int[levelWidth][levelHeight];
        this.rooms = new ArrayList<>();
        //this.entities = new HashSet<>();
        this.entities = new HashMap<>();

    }

    // METHODS & FUNCTIONS
    // ---------------------------------------------------------------------------------------------

    /**
     * Initialises the level data with default wall tiles everywhere.
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

    //TODO: This class must provide a method which returns a collection of all pre-game-play existing
    // entities such as enemies, loot and weapons.

    /**
     * Loads a dungeon using the current seed.
     * The generation of a dungeon goes through the following steps:
     * 1) adding randomly scattered rooms across the level
     * 2) exclude all room corners from the list of potential connector positions
     * 3) count all rooms by type
     * 4) calculate for every room the nearest neighbour room
     * 5) sort the room list by coordinates
     * 6) generate mazes between the rooms
     * 7) connect the maze and the rooms at random locations
     * 8) remove all extra doors
     * 9) remove all dead ends
     * 10) finally placing the player in a random room.
     */
    public void load() {
        Room.Type baseRoomType; // holds the general size of the room. Random rectangularity is added later.
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
         * This for loop is executed MAX_INSERTION_TRIES times, trying to add more rooms to the level.
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
                if (insertedRoom.overlaps(room) || tooCloseToBounds(room)) {
                    fits = false;
                    break;
                }

            }
            /**
             * If the room fits, it will be added to the room list.
             */
            if (fits) {
                rooms.add(room);
                Gdx.app.log("ROOM GENERATED", room.toString());
            }

        }

        /**
         * Phases 2 - 10
         */
        fillRoomsAndMarkCorners();
        countRoomsByType();
        calculateNearestNeighbours();
        sortRoomList();

        Gdx.app.log("LEVEL", "STARTING MAZE GENERATION");

        for (int x = MIN_BOUNDS_DISTANCE; x < levelWidth - MIN_BOUNDS_DISTANCE; x += 3) {
            for (int y = MIN_BOUNDS_DISTANCE; y < levelHeight - MIN_BOUNDS_DISTANCE; y += 3) {
                Vector2 position = new Vector2(x, y);
                if (!getTile(position).getType().equals(WALL)) {
                    continue;
                }
                generateMaze(position);
            }
        }

        Gdx.app.log("LEVEL", "STARTING TO CONNECT REGIONS");
        connectRegions();
        Gdx.app.log("LEVEL", "REMOVING EXTRA DOORS");
        removeExtraDoors();
        Gdx.app.log("LEVEL", "REMOVING DEAD ENDS");
        removeDeadEnds();
        calculateBitMasks();
        placePlayer();
    }


    /**
     * Fills all rooms with floor tiles and adds the corners to the roomCorners list.
     * This list is used to forbid connectors at room corners.
     */
    private void fillRoomsAndMarkCorners() {
        roomCorners = new ArrayList<>();

        for (Room r : rooms) {
            int startX = r.getxPos();
            int endX = startX + r.getWidth();
            int startY = r.getyPos();
            int endY = startY + r.getHeight();

            // bottom left corner
            roomCorners.add(new Vector2(startX - 1, startY));
            roomCorners.add(new Vector2(startX - 2, startY));
            roomCorners.add(new Vector2(startX, startY - 1));
            roomCorners.add(new Vector2(startX, startY - 2));

            // bottom right corner
            roomCorners.add(new Vector2(endX - 1, startY - 1));
            roomCorners.add(new Vector2(endX - 1, startY - 2));
            roomCorners.add(new Vector2(endX, startY));
            roomCorners.add(new Vector2(endX + 1, startY));

            // top left corner
            roomCorners.add(new Vector2(startX - 1, endY - 1));
            roomCorners.add(new Vector2(startX - 2, endY - 1));
            roomCorners.add(new Vector2(startX, endY));
            roomCorners.add(new Vector2(startX, endY + 1));

            //top right corner
            roomCorners.add(new Vector2(endX - 1, endY));
            roomCorners.add(new Vector2(endX - 1, endY + 1));
            roomCorners.add(new Vector2(endX, endY - 1));
            roomCorners.add(new Vector2(endX + 1, endY - 1));


            System.out.format("\n X: %3d %3d Y: %3d %3d", startX, endX, startY, endY);

            for (int x = startX; x < endX; x++) {
                for (int y = startY; y < endY; y++) {
                    Tile tile = new Tile(new Vector2(x, y), Tile.TileType.FLOOR, true);
                    tile.setRoomID(r.getID());
                    levelData[x][y] = tile;
                    regions[x][y] = totalRegions;

                }
            }
            totalRegions++;

            Pistol p = new Pistol(r.getCenter());
            Pistol q = new Pistol(r.getCenter().add(1,1));
            Pistol s = new Pistol(r.getCenter().add(1,2));
            Pistol t = new Pistol(r.getCenter().add(2,1));
            Chest c = new Chest(r.getCenter().cpy().add(1,1));

            /*entities.add(p);
            entities.add(c);*/

            entities.put(p.getID(),p);
            entities.put(q.getID(),q);
            entities.put(s.getID(),s);
            entities.put(t.getID(),t);
            entities.put(c.getID(),c);

        }
    }


    /**
     * Counts all rooms by type.
     */
    private void countRoomsByType() {
        for (Room r : rooms) {
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
    }


    /**
     * Calculates for each room its nearest neighbour room.
     */
    private void calculateNearestNeighbours() {
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

            System.out.format("%5d%5d%10f", currentRoom.getType().size, currentRoom.getClosestNeighbour().getID(), distance);
            System.out.println();

        }

    }


    /**
     * Sorts the room array by the rooms coordinates.
     */
    private void sortRoomList() {
        /**
         * The rooms are sorted by their coordinates. Lower x and higher y coordinates are located
         * higher in the list. A custom comparator is used.
         */
        Collections.sort(rooms, new Comparator<Room>() {
            @Override
            public int compare(Room r1, Room r2) {
                int resultX = Float.compare(r1.getxPos(), r2.getxPos());
                int resultY = Float.compare(r1.getyPos(), r2.getyPos());
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
    }


    /**
     * Removes all extra connectors for each room by scanning each wall of the room for extra doors.
     */
    public void removeExtraDoors() {
        for (Room r : rooms) {
            // The coordinates of the walls around each room
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

    /**
     * Decides which door will stay as the single door for the wall of a room.
     * The decision is based on the distance between the "real" door inside the room data.
     *
     * @param door          door data of the room
     * @param start         start of one wall coordinate
     * @param end           end of one wall coordinate
     * @param constant      constant coordinate of the wall
     * @param xAddressFirst decides whether it is a north/south wall or an east/west wall
     */
    private void decideDoor(Room.Door door, int start, int end, int constant, boolean xAddressFirst) {
        ArrayList<Vector2> doorsToBeRemoved = new ArrayList<>();
        Vector2 remainingDoor;
        // Collecting all doors in list
        for (int i = start; i < end; i++) {
            if (xAddressFirst) {
                // If there is a floor tile, we found a door in the wall
                if (getTile(i, constant).getType() == FLOOR) {
                    doorsToBeRemoved.add(new Vector2(i, constant));
                }
            } else {
                // If there is a floor tile, we found a door in the wall
                if (getTile(constant, i).getType() == FLOOR) {
                    doorsToBeRemoved.add(new Vector2(constant, i));
                }
            }
        }

        // If there is only one door, we can leave this wall
        if (doorsToBeRemoved.size() < 1)
            return;

        // Calculating the closest door towards the dummy door data
        remainingDoor = doorsToBeRemoved.get(0);
        float min = Float.MAX_VALUE;
        for (Vector2 d : doorsToBeRemoved) {
            float dst = d.dst(door.position);
            if (dst < min) {
                min = dst;
                remainingDoor = d;
            }

        }

        // Fill the doors to be removed with wall tiles.
        for (Vector2 otherDoor : doorsToBeRemoved) {
            if (otherDoor.equals(remainingDoor))
                continue;

            setLevelData(otherDoor, WALL);

        }
    }

    /**
     * Connects all different regions until only one region is left.
     * This method ensures connectivity in the level so that the player can reach every room.
     */
    public void connectRegions() {
        /**
         * Maps a position to a set of integers. The position is mapped to the region numbers of
         * of the surrounding tiles. If we find 2 or more different region numbers, the
         * tile on the position is a connector.
         */

        Gdx.app.log("EVENT", "CONNECTING REGIONS");
        HashMap<Vector2, HashMap<Integer, Integer>> connectors = new HashMap<>();

        for (int x = 0; x < levelWidth; x++) {
            for (int y = 0; y < levelHeight; y++) {

                /**
                 * If the current position is one of the rooms corners, it is not considered
                 * to be a potential connector.
                 */
                if (roomCorners.contains(new Vector2(x, y)))
                    continue;
                HashMap<Integer, Integer> adjacentRegions = new HashMap<>();

                /**
                 * Check all 4 cardinal neighbours from the current position [x,y]
                 */
                for (int i = 0; i < CARDINAL_DIRS.length; i++) {
                    Vector2 direction = CARDINAL_DIRS[i];
                    Vector2 destination = new Vector2(x, y).add(direction.cpy().scl(2));
                    //Vector2 destination = d.position.cpy().add(direction.cpy().scl(2));
                    if (checkBounds(destination)) {
                        int regionAtDestination = getRegion(destination);
                        /**
                         * If the tile at the destination belongs to a region e.g.
                         * if it is part of a room or corridor, save the adjacent region number
                         * and the direction in which it was found.
                         */
                        //
                        if (regionAtDestination != -1) {
                            if (!adjacentRegions.values().contains(regionAtDestination))
                                adjacentRegions.put(i, regionAtDestination);
                        }

                    }

                }

                /** If there are less than 2 adjacent regions, this tile does not connect 2 different regions
                 *  Therefore it can be dismissed.
                 */
                if (adjacentRegions.size() < 2)
                    continue;
                /**
                 * Otherwise, the current position and the list of adjacent region numbers and the directions
                 * they were found in are added to the connectors map.
                 */
                connectors.put(new Vector2(x, y), adjacentRegions);

            }
        }

        for (Vector2 test : connectors.keySet()) {
            Gdx.app.log("POSITION:" + test.toString(), " DIRECTIONS AROUND:" + connectors.get(test).toString());
        }

        /**
         * Maps a region number to the region number it was merged with.
         */
        int[] mergedTo = new int[totalRegions];

        /**
         * Keeps track of all open regions left which still have to be merged.
         */
        HashSet<Integer> openRegions = new HashSet<>();

        for (int i = 0; i < totalRegions; i++) {
            // At the beginning, region i is merged to itself.
            mergedTo[i] = i;
            // Also, all regions are open/ can be merged.
            openRegions.add(i);
        }


        while (openRegions.size() > 1) {

            ArrayList<Vector2> connectorList = new ArrayList<>(connectors.keySet());
            /**
             *Picking a random connector from the list
             */
            Vector2 connectorPosition = connectorList.get(random.nextInt(connectorList.size()));
            // Gdx.app.log("CHOSE FOLLOWING CONNECTOR", connectorPosition.toString());
            /**
             * Getting all directions and all regions of the current connector.
             */
            ArrayList<Integer> directionList = new ArrayList<>(connectors.get(connectorPosition).keySet());
            ArrayList<Integer> regionList = new ArrayList<>(connectors.get(connectorPosition).values());
            // Gdx.app.log("DIRECTIONS", directionList.toString());
            // Gdx.app.log("REGIONS", regionList.toString());

            /**
             * Picking a random direction and a door to connect corridor and room.
             */
            int direction = directionList.get(random.nextInt(directionList.size()));
            addJunction(connectorPosition, direction);

            /**
             * Choose 1 of the 2 regions connected and remove it from the region list.
             * The rest of the region list is treated as the region numbers which where merged with
             * the first chosen region number.
             */
            int destination = regionList.get(0);
            regionList.remove(Integer.valueOf(destination));
            // Merge all affected regions
            for (int i = 0; i < totalRegions; i++) {
                if (regionList.contains(mergedTo[i]))
                    mergedTo[i] = destination;
            }

            /**
             * The now merged regions are removed from the open regions list.
             */
            openRegions.removeAll(regionList);

            /**
             * Now we collect all the region numbers left that are connected by the current
             * connector position. This is done by getting the original region numbers
             * and then collect all region numbers that are left after merging in a set.
             */
            Collection<Integer> originalRegions = connectors.get(connectorPosition).values();
            HashSet<Integer> regionsLeft = new HashSet<>();
            for (Integer i : originalRegions) {
                regionsLeft.add(mergedTo[i]);
            }

            /**
             * Remove all entries in the connector list that are too close to the recently added door.
             * Those positions do not qualify anymore as future connectors and are dismissed.
             */
            for (Iterator<Map.Entry<Vector2, HashMap<Integer, Integer>>> it = connectors.entrySet().iterator(); it.hasNext(); ) {
                Map.Entry<Vector2, HashMap<Integer, Integer>> entry = it.next();
                if (entry.getKey().dst(connectorPosition) < 2) {
                    it.remove();
                }
            }

            /**
             * If the current connector does not span more than 2 different regions, it can also be
             * removed from the connector list.
             */
            if (regionsLeft.size() < 2) {
                connectors.remove(connectorPosition);
            }


        }


    }

    /**
     * Removes all dead ends from the level.
     */
    public void removeDeadEnds() {

        boolean deadEndFound = true;
        while (deadEndFound) {
            deadEndFound = false;
            for (int x = 0; x < levelWidth; x++) {
                for (int y = 0; y < levelHeight; y++) {
                    if (getTile(x, y).getType() == WALL)
                        continue;

                    int exits = 0;
                    for (Vector2 c : CARDINAL_DIRS) {
                        if (getTile(new Vector2(x, y).add(c)).getType() != WALL)
                            exits++;
                    }

                    /**
                     * If there exist less than 2 exits, we found a dead end.
                     * The dead end is converted into a wall.
                     */
                    if (exits < 2) {
                        deadEndFound = true;
                        setLevelData(new Vector2(x, y), WALL);
                    }

                }
            }
        }
    }

    /**
     * Adds a door between a room and a corridor.
     * The tile behind the door, the tile on the connector position and the tile after
     * the connector position are changed to floor.
     *
     * @param connector connector position
     * @param direction direction in which the door will be carved.
     */
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


    private void calculateBitMasks() {

        /**
         * Setting all tiles with no floor neighbours to void tiles.
         */
        for (int x = 0; x < levelWidth; x++) {
            for (int y = 0; y < levelHeight; y++) {

                int floorNeighbours = 0;

                for (Vector2 c : CARDINALS_AND_INTERMEDIATES) {
                    if (checkBounds(new Vector2(x, y).add(c))) {
                        if (getTile(new Vector2(x, y).add(c)).getType() == FLOOR) {
                            floorNeighbours++;
                        }
                    }
                }

                if (floorNeighbours == 0) {
                    getTile(x, y).tileIndex = VOID;
                }


            }
        }

        for(int x = 0; x < levelWidth; x++) {
            for(int y = 0; y < levelHeight; y++) {

                if(getTile(x,y).getType() == FLOOR || getTile(x,y).tileIndex == VOID)
                    continue;


                if(isFloor(N_DIR,x,y)) {
                   if(isFloor(NW_DIR,x,y) && isFloor(W_DIR,x,y)) {
                       getTile(x,y).tileIndex = SOUTH_EAST_INNER; // 5
                   }

                    else if(isFloor(NE_DIR,x,y) && isFloor(E_DIR,x,y)) {
                        getTile(x,y).tileIndex = SOUTH_WEST_INNER; // 6
                    }

                    else
                        getTile(x,y).tileIndex = SOUTH_WALL; // 3

                }


                else if(isFloor(E_DIR,x,y)) {
                     if(isFloor(N_DIR,x,y) && isFloor(NE_DIR,x,y)) {
                         getTile(x,y).tileIndex = SOUTH_WEST_INNER; // 6
                     }
                    else if(isFloor(S_DIR,x,y) && isFloor(SE_DIR,x,y)) {
                         getTile(x,y).tileIndex = NORTH_EAST_INNER; // 7
                     }

                    else {
                         getTile(x,y).tileIndex = WEST_WALL; // 4
                     }


                }


                else if(isFloor(S_DIR,x,y)) {
                    if(isFloor(E_DIR,x,y) && isFloor(SE_DIR,x,y)) {
                        getTile(x,y).tileIndex = NORTH_EAST_INNER; // 7
                    }
                    else if(isFloor(W_DIR,x,y) && isFloor(SW_DIR,x,y)) {
                        getTile(x,y).tileIndex = NORTH_WEST_INNER; // 8
                    }
                    else {
                        getTile(x,y).tileIndex = NORTH_WALL; // 1
                    }


                }


                else if(isFloor(W_DIR,x,y)) {
                    if(isFloor(S_DIR,x,y) && isFloor(SW_DIR,x,y)) {
                        getTile(x,y).tileIndex = NORTH_WEST_INNER; // 8
                    }
                    else if(isFloor(N_DIR,x,y) && isFloor(NW_DIR,x,y)) {
                        getTile(x,y).tileIndex = SOUTH_EAST_INNER; // 5
                    }
                    else {
                        getTile(x,y).tileIndex =EAST_WALL;
                    }
                }

                else if(isFloor(SE_DIR,x,y)) {
                    getTile(x,y).tileIndex = SOUTH_EAST_OUTER;
                }

                else if(isFloor(SW_DIR,x,y)) {
                    getTile(x,y).tileIndex = SOUTH_WEST_OUTER;
                }

                else if(isFloor(NW_DIR,x,y)) {
                    getTile(x,y).tileIndex = NORTH_WEST_OUTER;
                }

                else if(isFloor(NE_DIR,x,y)) {
                    getTile(x,y).tileIndex = NORTH_EAST_OUTER;
                }


            }

        }


    }

    /**
     * Checks whether an adjacent tile is a floor tile.
     * @param direction direction in which the check is performed
     * @param x x position
     * @param y y position
     * @return
     */
    private boolean isFloor(Vector2 direction, int x, int y) {
        return getTile(new Vector2(x,y).add(direction)).getType() == FLOOR;
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
     * Returns a random multiple of 3  within the range [a,b], shifted by 1 to the left.
     * Uses the seed to generate this random number.
     *
     * @param a limit a
     * @param b limit b
     * @return random value
     */
    private int multipleOfThree(int a, int b) {
        if (a < b) {
            return 3 * (a / 3 + random.nextInt(((b - a) / 3) + 1)) - 1;
        } else {
            return 3 * (b / 2 + random.nextInt(((a - b) / 3) + 1)) - 1;
        }

    }

    /**
     * Decides depending on the seed the size for a room.
     *
     * @return room type which determines its size.
     */
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
     * @param start start position
     */
    public void generateMaze(Vector2 start) {

        LinkedList<Vector2> cells = new LinkedList<>();
        setLevelData(new Tile(start.cpy(), Tile.TileType.FLOOR, true));
        cells.add(start);


        Vector2 lastDirection = N_DIR;
        while (!cells.isEmpty()) {
            Vector2 currentPos = cells.getLast();
            /*Gdx.app.log("NEXT CELL", "------------------");
            Gdx.app.log("CURRENT POS", currentPos.toString());
            Gdx.app.log("CELLS SIZE", Integer.toString(cells.size()));*/
            /**
             * Get all the valid neighbour tiles
             */
            ArrayList<Vector2> adjacentDirections = checkNeighbours(currentPos);
            //Gdx.app.log("ADJACENT SIZE", Integer.toString(adjacentDirections.size()));

            // If there are neighbours we can grow a corridor.
            if (!adjacentDirections.isEmpty()) {
                Vector2 dir;
                if (adjacentDirections.contains(lastDirection) && random.nextInt(100) > 30)
                    dir = lastDirection;
                else
                    dir = adjacentDirections.get(random.nextInt(adjacentDirections.size())).cpy();
                Vector2 adjacentPos = currentPos.cpy().add(dir);
                Vector2 posAfter = adjacentPos.cpy().add(dir);

                /**
                 * Setting the next 3 tiles to be floor. This is done to create walls that are at
                 * least 2 tiles wide.
                 */
                setLevelData(adjacentPos, FLOOR);
                setLevelData(posAfter, FLOOR);
                setLevelData(posAfter.cpy().add(dir), FLOOR);
                /**
                 * Finally add the position of the last carved floor tile to the list to start
                 * growing a part of the maze from this position.
                 */
                cells.add(new Vector2(posAfter.cpy().add(dir)));
                lastDirection = dir;
            } else {
                cells.removeLast();
            }

        }
        // The maze opens a new region, so the total number of regions is increased.
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
        for (Vector2 dir : CARDINAL_DIRS) {
            // add the direction to the tiles position to get to the neighbour tile.
            if (canBeCarved(position, dir)) {
                // If there exists a adjacent tile, add the direction in which it lies to the array.
                neighbourDirections.add(dir);
            }
        }
        return neighbourDirections;
    }

    /**
     * Checks whether the given coordinates x and y are inside the bounds of the level.
     *
     * @param x x position
     * @param y y position
     * @return true if inside the bounds, false otherwise.
     */
    public boolean checkBounds(float x, float y) {
        return (x >= 0 && x < levelWidth && y >= 0 && y < levelHeight);

    }

    public boolean checkBounds(Vector2 test) {
        return checkBounds(test.x, test.y);
    }

    /**
     * Decides whether a corridor can be carved in the given direction
     *
     * @param position  position
     * @param direction direction
     * @return
     */
    private boolean canBeCarved(Vector2 position, Vector2 direction) {
        /**
         * The newly carved corridor should not end in territory that is too close to the bounds
         */
        if (tooCloseToBounds((position.cpy().add(direction.cpy().scl(4))))) {
            //Gdx.app.log("EXIT", "FALSE");
            return false;
        }
        /**
         * The final tile should be a wall.
         */
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

    //public HashSet<Entity> getEntities() {
    public HashMap<Integer,Entity> getEntities() {
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
