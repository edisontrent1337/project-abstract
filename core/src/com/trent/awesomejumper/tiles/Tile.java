package com.trent.awesomejumper.tiles;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.trent.awesomejumper.models.Player;
import com.trent.awesomejumper.engine.physics.CollisionBox;

/**
 * Created by Sinthu on 12.06.2015.
 */
public class Tile {

    // MEMBERS & INSTANCES
    // ---------------------------------------------------------------------------------------------

    /**
     *  Type of a tile. Integer representation (value)
     *  can be used to identify different types of tiles.
     *  TODO: Add floor types and wall types.
     */
    public enum TileType {
        AIR(0),
        WALL(1),
        STONE(2),
        FLOOR(3),
        ICE(4),
        TRAMPOLINE(5);
        public int value;
        TileType(int value) {
            this.value = value;
        }
    }

    public int tileIndex = -1;

    public static final float SIZE = 1f;
    private long roomID = 0;
    Rectangle bounds = new Rectangle();

    private CollisionBox collisionBox;
    private Vector2 position;
    private TileType type;

    public float width, height, maxVelocity, maxJumpSpeed, friction;
    public boolean passable, interactable;

    private boolean visited = false;
    private Tile predecessor = null;

    // CONSTRUCTOR
    // ---------------------------------------------------------------------------------------------

    public Tile(Vector2 position) {
        this.position = position;
        this.bounds.width = SIZE;
        this.bounds.height = SIZE;
        this.bounds.x = position.x;
        this.bounds.y = position.y;
        this.collisionBox = new CollisionBox(position, SIZE, SIZE);
        this.friction = 1;
        this.passable = false;
    }

    public Tile(Vector2 position, TileType type, boolean passable) {
        this.position = position;
        this.bounds.width = SIZE;
        this.bounds.height = SIZE;
        this.bounds.x = position.x;
        this.bounds.y = position.y;
        this.type = type;
        this.passable = passable;
        this.collisionBox = new CollisionBox(position, SIZE, SIZE);
        this.friction = 1;
    }


    public Tile(Vector2 position, TileType type, float width, float height, float friction, float maxVelocity, float maxJumpSpeed, boolean passable, boolean interactable) {
        this.position = position;
        this.type = type;
        this.collisionBox = new CollisionBox(position, width, height);
        this.bounds.width = width;
        this.bounds.height = height;
        this.bounds.x = position.x;
        this.bounds.y = position.y;
        this.friction = friction;
        this.maxVelocity = maxVelocity;
        this.maxJumpSpeed = maxJumpSpeed;
        this.passable = passable;
        this.interactable = interactable;
    }


    // GETTER AND SETTER
    // ---------------------------------------------------------------------------------------------

    public Vector2 getPosition() {
        return position;
    }

    public void setPosition(float x, float y) {
        position.x = x;
        position.y = y;
        bounds.x = x;
        bounds.y = y;
        collisionBox.setPosition(x,y);
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public CollisionBox getCollisionBox() {
        return collisionBox;
    }

    public TileType getType() {
        return this.type;
    }

    public boolean isPassable() {
        return passable;
    }

    public boolean isInteractable() {
        return interactable;
    }

    public float getFriction() {
        return friction;
    }

    public float getMaxVelocity() {
        return maxVelocity;
    }

    public void action (Player player, float delta) {}

    public void setVisited(boolean visited) {
        this.visited = visited;
    }

    public boolean isVisited() {
        return visited;
    }

    public void setRoomID(long id) {
        this.roomID = id;
    }

    public long getRoomID() {
        return roomID;
    }

    public void setPredecessor(Tile predecessor) {
        this.predecessor = predecessor;
    }

    public Tile getPredecessor() {
        return predecessor;
    }

}
