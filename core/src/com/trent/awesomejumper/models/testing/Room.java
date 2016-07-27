package com.trent.awesomejumper.models.testing;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.trent.awesomejumper.utils.Interval;
import com.trent.awesomejumper.utils.Utilities;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Class that holds all information and methods related to in-game rooms.
 * Created by Sinthu on 23.07.2016.
 */
public class Room {

    // ID to identify the room in the game data
    private final int ID;
    private static AtomicInteger idCounter = new AtomicInteger();
    private int xPos, yPos, width, height;
    private Vector2 center;

    private Door northDoor, eastDoor, southDoor, westDoor;

    private Array<Door> doors = new Array<>();

    // Minimum distance between two adjacent rooms
    private final int MIN_ROOM_DISTANCE = 3;

    private boolean visited = false;

    public enum Type {
        SMALL(0),
        MEDIUM(1),
        BIG(2),
        GIANT(3);

        private int value;

        Type(int value) {
            this.value = value;
        }

    }

    private Type type;

    /**
     * x and y ranges the room covers in the world.
     */
    private Interval xDimensions;
    private Interval yDimensions;

    private Interval xDimensionsAndDistance;
    private Interval yDimensionsAndDistance;

    // CONSTRUCTOR
    // ---------------------------------------------------------------------------------------------
    public Room(int xPos, int yPos, int width, int height) {
        this.xPos = xPos;
        this.yPos = yPos;
        this.width = width;
        this.height = height;

        this.xDimensions = new Interval(xPos, xPos + width);
        this.yDimensions = new Interval(yPos, yPos + height);

        this.xDimensionsAndDistance = new Interval(xDimensions.min - MIN_ROOM_DISTANCE, xDimensions.max + MIN_ROOM_DISTANCE);
        this.yDimensionsAndDistance = new Interval(yDimensions.min - MIN_ROOM_DISTANCE, yDimensions.max + MIN_ROOM_DISTANCE);
        this.ID = idCounter.getAndIncrement();

        this.type = Type.SMALL;
        this.center = new Vector2(xPos + width / 2, yPos + height / 2);
        initDoorData();
    }

    public Room() {
        this.xPos = 0;
        this.yPos = 0;
        this.width = 0;
        this.height = 0;

        this.xDimensions = new Interval(xPos, xPos + width);
        this.yDimensions = new Interval(yPos, yPos + height);

        this.xDimensionsAndDistance = new Interval(xDimensions.min - MIN_ROOM_DISTANCE, xDimensions.max + MIN_ROOM_DISTANCE);
        this.yDimensionsAndDistance = new Interval(yDimensions.min - MIN_ROOM_DISTANCE, yDimensions.max + MIN_ROOM_DISTANCE);
        this.ID = idCounter.getAndIncrement();
        this.type = Type.SMALL;

        this.center = new Vector2(xPos + width / 2, yPos + height / 2);
        initDoorData();

    }

    // METHODS & FUNCTIONS
    // ---------------------------------------------------------------------------------------------

    private void initDoorData() {
        northDoor = new Door(Math.round(center.x), (int) yDimensions.max);
        eastDoor = new Door((int) xDimensions.max, Math.round(center.y));
        southDoor = new Door(Math.round(center.x), (int) yDimensions.min);
        westDoor = new Door((int) xDimensions.min, Math.round(center.y));

        doors.add(northDoor);
        doors.add(eastDoor);
        doors.add(southDoor);
        doors.add(westDoor);

    }

    /**
     * Checks whether two rooms overlap. Uses the overlaps() method from the utility class.
     * Uses Intervals with added padding (MIN_ROOM_DISTANCE)
     *
     * @param other
     * @return
     */
    public boolean overlaps(Room other) {
        return (Utilities.overlaps(xDimensionsAndDistance, other.getxDimensionsAndDistance()) && Utilities.overlaps(yDimensionsAndDistance, other.getyDimensionsAndDistance()));
    }

    @Override
    public String toString() {
        return "ID: " + String.format("%04d", ID) + "\n X: " + Integer.toString(xPos) + " Y: " + Integer.toString(yPos) +
                "\n W: " + Integer.toString(width) + " H: " + Integer.toString(height) +
                "\n AREA:" + Integer.toString(width * height) +
                "\n DOORS:(N|E|S|W)" + northDoor.toString() + eastDoor.toString() + southDoor.toString() + westDoor.toString();
    }


    // GETTER & SETTER
    // ---------------------------------------------------------------------------------------------

    public int getxPos() {
        return xPos;
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public int getyPos() {
        return yPos;
    }

    public Interval getxDimensions() {
        return xDimensions;
    }

    public Interval getyDimensions() {
        return yDimensions;
    }

    public Interval getxDimensionsAndDistance() {
        return xDimensionsAndDistance;
    }

    public Interval getyDimensionsAndDistance() {
        return yDimensionsAndDistance;
    }

    public int getID() {
        return ID;
    }

    public Vector2 getCenter() {
        return new Vector2(xPos + width / 2, yPos + height / 2);
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public boolean isVisited() {
        return visited;
    }

    public void setVisited() {
        visited = true;
    }

    public Array<Door> getDoors() {
        return doors;
    }


    public class Door {
        int x, y;

        private Door(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public int getX() {
            return x;

        }

        public int getY() {
            return y;

        }

        @Override
        public String toString() {
            return " x: " + Integer.toString(x) + " y: " + Integer.toString(y) + " ";
        }

    }

}
