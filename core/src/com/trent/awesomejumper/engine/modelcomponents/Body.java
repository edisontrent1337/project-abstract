package com.trent.awesomejumper.engine.modelcomponents;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.trent.awesomejumper.engine.entity.Entity;
import com.trent.awesomejumper.engine.physics.CollisionBox;
import com.trent.awesomejumper.models.projectile.Projectile;

import java.util.HashSet;
import java.util.LinkedList;

/**
 * Created by Sinthu on 09.12.2015.
 * Body component implementation. Holds information about position (x,y,zOffset) , acceleration, velocity and
 * dimensions of entities. Also holds the hitbox skeleton used to calculate damage.
 * Also holds information about the entity neighbourhood of the entity that holds this very body.
 */
public class Body extends ModelComponent {

    // MEMBERS & INSTANCES
    // ---------------------------------------------------------------------------------------------


    // Movement & Locality
    private Vector2 position;        // position contains the x/y grid
    private float heightZ;           // height of entity in z direction
    private float zOffset = 0;       // z position over the floor. By default 0
    private Vector2 velocity;        // velocity on the xy grid
    private Vector2 acceleration;    // acceleration on the xy grid
    private Vector2 center;          // center of the body position
    private Vector2 aimReference;    // reference position of the object the entity wants to look at
    private Vector2 orientation;     // direction in which the entity currently looks
    private float angleOfRotation;   // angle which belongs towards the orientation for rendering

    private CollisionBox bounds;

    private boolean collidedWithWorld;

    private boolean collisionDetectionEnabled;

    // Physical parameters
    private float mass;             // mass of the body
    private float friction;         // frictional coefficient
    private float elasticity;       // coefficient for elasticity
    private float maxVelocity;      // maximum travel velocity
    private float density;          // material density


    // Hitboxes
    private Array<CollisionBox> hitboxSkeleton = new Array<>();
    private LinkedList<Vector2> impulses;
    private HashSet<Entity> entityNeighbourHood = new HashSet<>();

    // CONSTRUCTOR
    // ---------------------------------------------------------------------------------------------

    /**
     * Constructor for entities which lack collision capabilities. No mass, friction or elasticity
     * is needed here.
     */
    public Body(Entity entity, float width, float height) {
        this(entity, width, height, 0f, 0f, 0f, 0f);
    }

    public Body(Entity entity, float width, float height, float mass, float friction, float elasticity, float maxVelocity) {
        /**
         * Initialises all members with default values.
         * The constructor of the entity or the specific subclass should apply a more useful
         * start configuration to all values.
         */
        this.entity = entity;
        this.position = new Vector2(0f, 0f);
        this.velocity = new Vector2(0f, 0f);
        this.acceleration = new Vector2(0f, 0f);
        this.orientation = new Vector2(1f,0f);
        this.aimReference = new Vector2(0f,0f);
        this.collisionDetectionEnabled = true;
        this.bounds = new CollisionBox(position, width, height);
        this.impulses = new LinkedList<>();
        this.mass = mass;
        this.friction = friction;
        this.elasticity = elasticity;
        this.maxVelocity = maxVelocity;
        this.center = new Vector2(position.x + width / 2f, position.y + height / 2f);
        this.zOffset = 0f;
        hitboxSkeleton.clear();
        entity.enableComponent(ComponentID.BODY);
    }


    public void update(float delta) {

        if(entity.getClass().equals(Projectile.class)) {
            Gdx.app.log("VELO LEN", Float.toString(velocity.cpy().scl(delta).len()));
        }

        position.add(velocity.cpy().scl(delta));

        if(entity.getClass().equals(Projectile.class)) {
            Gdx.app.log("POSITION", position.toString());
        }

        /**
         * If collision detection on this body is enabled, the bounds collision box will be updated
         */
        bounds.update(position);
        center.set(bounds.getPositionAndOffset().x + getHalfDimensions().x, bounds.getPositionAndOffset().y + getHalfDimensions().y);


        /**
         * Update each CollisionBox contains the hitboxSkeleton
         */
        for (CollisionBox b : hitboxSkeleton) {
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
        bounds.update(position);
    }

    public void setPositionX(float x) {
        position.x = x;
    }

    public void setPositionY(float y) {
        position.y = y;
    }

    public void setPosition(float x, float y) {
        position.x = x;
        position.y = y;
    }

    public void setZOffset(float zOffset) {
        this.zOffset = zOffset;
    }

    public float getZOffset() {
        return zOffset;
    }

    public Vector2 getAimReference() {
        return aimReference;
    }

    public Vector2 getOrientation() {
        return orientation;
    }
    public float getAngleOfRotation() {
        return angleOfRotation;
    }

    public void setAngleOfRotation(float angleOfRotation) {

        this.angleOfRotation = angleOfRotation;
    }

    public Vector2 getCenter() {
        return center;
    }

    public Vector2 getHalfDimensions() {
        return new Vector2(bounds.getWidth()/2f, bounds.getHeight()/2f);
    }

    public void setAimReference(Vector2 aimReference) {
        this.aimReference = aimReference;
    }

    public void setOrientation(Vector2 orientation) {
        this.orientation = orientation;
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
        this.velocity = new Vector2(x, y);
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
    public float getWidthX() {
        return bounds.getWidth();
    }
    public float getWidthY() {
        return bounds.getHeight();
    }
    public float getHeightZ() {
        return heightZ;
    }


    public void setWidthX(float widthX) {
        bounds.setWidth(widthX);
    }

    public void setWidthY(float widthY) {
        bounds.setHeight(widthY);
    }

    public void setHeightZ(float heightZ) {
        this.heightZ = heightZ;
    }


    // Hitboxes
    public CollisionBox getBounds() {
        return bounds;
    }

    public void setBounds(CollisionBox bounds) {
        this.bounds = bounds;
    }

    public void setBounds(float x, float y) {
        bounds.setPosition(x, y);
    }

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

    // MASS, FRICTION, ELASTICITY, DENSITY
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

    public float getDensity() {
        return density;
    }

    public void setDensity(float density) {
        return density;
    }

    // COLLISION DETECTION

    public boolean isCollisionDetectionEnabled() {
        return collisionDetectionEnabled;
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
     *
     * @param box
     */
    public void add(CollisionBox box) {
        hitboxSkeleton.add(box);
    }

    /**
     * Adds ab impulse to the list of impulses.
     *
     * @param i
     */
    public void addImpulse(Vector2 i) {
        impulses.add(i);
    }


    /**
     * Enables collision detection
     */
    public void enableCollisionDetection() {
        collisionDetectionEnabled = true;
    }

    /**
     * Disables collision detection
     */
    public void disableCollisionDetection() {
        collisionDetectionEnabled = false;
    }


    /**
     * Resets the bodies aim reference and rotation.
     * Also re-enables collision detection
     */
    public void reset() {
        setAimReference(new Vector2(0f,0f));
        setAngleOfRotation(0);
        setVelocity(0,0);
        setAcceleration(0,0);
        entity.setOwner(entity);
        enableCollisionDetection();
    }

}


