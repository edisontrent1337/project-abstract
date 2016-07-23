package com.trent.awesomejumper.models.testing;

import com.badlogic.gdx.math.Vector2;
import com.trent.awesomejumper.utils.Interval;
import com.trent.awesomejumper.utils.Utilities;

import java.util.concurrent.atomic.AtomicInteger;

/** //TODO: DOCUMENTATION
 * Created by Sinthu on 23.07.2016.
 */
public class Room {


    private final int ID;
    private static AtomicInteger idCounter = new AtomicInteger();
    private int xPos, yPos, width, height;

    private Interval xDimensions;
    private Interval yDimensions;

    public Room(int xPos, int yPos, int width, int height) {
        this.xPos = xPos;
        this.yPos = yPos;
        this.width = width;
        this.height = height;

        this.xDimensions = new Interval(xPos,xPos + width);
        this.yDimensions = new Interval(yPos, yPos + height);
        this.ID = idCounter.getAndIncrement();
    }


    public boolean overlaps(Room other) {
        return (Utilities.overlaps(xDimensions, other.getxDimensions()) && Utilities.overlaps(yDimensions, other.getyDimensions()));
    }

    @Override
    public String toString() {
        return "ID: " + String.format("%04d", ID) + "\n X: " + Integer.toString(xPos) + " Y: " + Integer.toString(yPos) +
                "\n W: " + Integer.toString(width) + " H: " + Integer.toString(height) +
                "\n AREA:" + Integer.toString(width*height);
    }


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

    public int getID() {
        return ID;
    }

    public Vector2 getCenter() {
        return new Vector2(xPos + width/2, yPos+height/2);
    }
}
