package com.trent.awesomejumper.engine.modelcomponents;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.trent.awesomejumper.engine.entity.Entity;
import com.trent.awesomejumper.engine.physics.CollisionBox;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

/**
 * Created by Sinthu on 09.12.2015.
 * Body component implementation. Holds information about position, acceleration, velocity and
 * dimensions of entities. Also holds the hitbox skeleton used to calculate damage.
 * Also holds information about the neighbourhood of the entity that holds this very body.
 */
public class Body extends ModelComponent{

    // MEMBERS & INSTANCES
    // ---------------------------------------------------------------------------------------------


    // Movement
    private Vector2 position;
    private Vector2 velocity;
    private Vector2 acceleration;
    private Vector2 center;

    private CollisionBox bounds;

    private boolean collidedWithWorld;

    private float mass;
    private float friction;
    private float elasticity;
    private float maxVelocity;


    // Hitboxes
    // TODO: Either address this array with an enum to get Head, Arm, Leg etc... or
    // TODO: use a HashMap where the key represents the kind of hitbox.
    // TODO: Replace current state of bounds by calculating a minimal bounding box that contains the location of all hitboxes in the
    // skeleton. The bounds are used for World / Entity collision, the skeleton for entity/bullet
    // collision.
    private Array<CollisionBox> hitboxSkeleton = new Array<>();
    private LinkedList<Vector2> impulses;
    private HashSet<Entity> entityNeighbourHood = new HashSet<>();



    /**
     * Constructor for entities which lack collision capabilities. No mass, friction or elasticity
     * is needed here.
     */
    public Body(Entity entity, float width, float height) {
        this(entity,width,height,0f,0f,0f,0f);
    }

    public Body(Entity entity, float width, float height, float mass, float friction, float elasticity, float maxVelocity) {
        /**
         * Initialises all members with default values.
         * The constructor of the entity or the specific subclass then applies a more useful
         * start configuration to all values.
         */
        this.entity = entity;
        this.position = new Vector2(0f,0f);
        this.velocity = new Vector2(0f,0f);
        this.acceleration = new Vector2(0f,0f);
        this.bounds = new CollisionBox(position, width, height);
        this.impulses = new LinkedList<>();
        this.mass = mass;
        this.friction = friction;
        this.elasticity = elasticity;
        this.maxVelocity = maxVelocity;
        this.center = new Vector2(position.x + width / 2f, position.y + height / 2f);
        hitboxSkeleton.clear();
        entity.hasBody = true;
    }


    public void update(float delta) {
        position.add(velocity.cpy().scl(delta));
        center.set(position.x + getWidth(), position.y + getHeight());
        // update outer general bounds
        bounds.update(position);
        // update skeleton
        for(CollisionBox b : hitboxSkeleton) {
            b.update(position);
        }

    }


    // GETTER AND SETTER
    // ---------------------------------------------------------------------------------------------

    // Entity
    public Entity getEntity() {
        return entity;
    }

    // Position
    public Vector2 getPosition() {
        return position;
    }

    public void setPosition(Vector2 position) {
        this.position = position;
    }

    public void setPositionX(float x) {
        position.x = x;
    }
    public void setPositionY(float y) {
        position.y = y;
    }

    // Velocity
    public Vector2 getVelocity() {
        return velocity;
    }
    public float getMaxVelocity() {
        return maxVelocity;
    }

    public void setVelocity(Vector2 velocity) {
        this.velocity = velocity;
    }
    public void setVelocity(float x, float y) {
        this.velocity = new Vector2(x,y);
    }
    public void setVelocityX(float x) {
        velocity.x = x;
    }
    public void setVelocityY(float y) {
        velocity.y = y;
    }

    public void setMaxVelocity(float maxVelocity) {
        this.maxVelocity = maxVelocity;
    }

    // Acceleration
    public Vector2 getAcceleration() {
        return acceleration;
    }
    public void setAcceleration(float x, float y) {
        acceleration.x = x;
        acceleration.y = y;
    }
    public void setAccelerationX(float x) {
        acceleration.x = x;
    }
    public void setAccelerationY(float y) {
        acceleration.y = y;
    }

    // Dimensions
    public float getWidth() {
        return bounds.getWidth();
    }
    public float getHeight(){
        return bounds.getHeight();
    }
    public CollisionBox getBounds() {
        return bounds;
    }
    public void setWidth(float width) {
        bounds.setWidth(width);
    }
    public void setHeight(float height) {
        bounds.setHeight(height);
    }
    public void setBounds(CollisionBox bounds) {
        this.bounds = bounds;
    }
    public void setBounds(float x, float y) {
        bounds.setPosition(x, y);
    }

    // Hitboxes
    public Array<CollisionBox> getHitboxSkeleton() {
        return hitboxSkeleton;
    }

    public LinkedList<Vector2> getImpulses() {
        return impulses;
    }

    public boolean isCollidedWithWorld() {
        return collidedWithWorld;
    }

    public void setCollidedWithWorld(boolean bool) {
        collidedWithWorld = bool;
    }

    // MASS, FRICTION, ELASTICITY
    public float getMass() {
        return mass;
    }

    public void setMass(float mass) {
        this.mass = mass;
    }

    public void setFriction(float friction) {
        this.friction = friction;
    }
    public float getFriction() {
        return friction;
    }

    public float getElasticity() {
        return elasticity;
    }

    public void setElasticity(float elasticity) {
        this.elasticity = elasticity;
    }

    // ENTITY NEIGHBOURHOOD

    public HashSet<Entity> getEntityNeighbourHood() {
        return entityNeighbourHood;
    }
    // ---------------------------------------------------------------------------------------------
    // METHODS AND FUNCTIONS
    // ---------------------------------------------------------------------------------------------
    /**
     * Adds a collisionBox to the skeleton of the entity.
     * @param box
     */
    public void add(CollisionBox box) {
        hitboxSkeleton.add(box);
    }

    /**
     * Adds ab impulse to the list of impulses.
     * @param i
     */
    public void addImpulse(Vector2 i) {
        impulses.add(i);
    }




}


