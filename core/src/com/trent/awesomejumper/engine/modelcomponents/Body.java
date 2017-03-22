package com.trent.awesomejumper.engine.modelcomponents;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.sun.scenario.effect.impl.state.LinearConvolveKernel;
import com.trent.awesomejumper.engine.entity.Entity;
import com.trent.awesomejumper.engine.physics.CollisionBox;
import com.trent.awesomejumper.models.projectile.Projectile;

import java.util.HashSet;
import java.util.LinkedList;

/**
 * Created by edisontrent1337 on 09.12.2015.
 * Body component implementation. Holds information about position (x,y,zOffset) , acceleration, velocity and
 * dimensions of entities. Also holds the hitbox skeleton used to calculate damage.
 * Also holds information about the entity neighbourhood of the entity that holds this very body.
 */
public class Body extends ModelComponent {

    // ---------------------------------------------------------------------------------------------
    // MEMBERS & INSTANCES
    // ---------------------------------------------------------------------------------------------

    // Movement & Locality
    private float heightZ;           // height of entity in z direction
    private float zOffset = 0;       // z position over the floor. By default 0
    private Vector2 position;        // position contains the x/y grid
    private Vector2 velocity;        // velocity on the xy grid
    private Vector2 acceleration;    // acceleration on the xy grid
    private Vector2 center;          // center of the body position
    private CollisionBox bounds;     // bounding box

    // PRE-DEFINED VALUES
    private Vector2 aimReference = new Vector2(0f,0f);      // reference position of the object the entity wants to look at
    private Vector2 orientation = new Vector2(0f,0f);       // direction in which the entity currently looks
    private float angleOfRotation = 0f;                     // angle which belongs towards the orientation for rendering
    private boolean collidedWithWorld = false;              // has the entity collided with the world in the previous frame?
    private boolean collisionDetectionEnabled = true;       // is cd enabled?
    private Array<CollisionBox> hitboxSkeleton = new Array<>();     // hitboxes
    private LinkedList<Vector2> impulses = new LinkedList<>();      // impulse list
    private HashSet<Entity> entityNeighbourHood = new HashSet<>();  // entity neighbourhood

    // Physical parameters
    private float mass;
    private float friction;
    private float density;
    private float armor;
    private float elasticity;
    private float maxVelocity;

    // ---------------------------------------------------------------------------------------------
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

    // BUILDER CONSTRUCTOR
    private Body(BodyBuilder builder) {
        this.entity = builder.entity;
        this.position = builder.position;
        this.velocity = builder.velocity;
        this.acceleration = builder.acceleration;
        this.mass = builder.mass;
        this.elasticity = builder.elasticity;
        this.density = builder.density;
        this.friction = builder.friction;
        this.center = new Vector2(position.x + builder.width / 2f, position.y + builder.height / 2f);
        this.maxVelocity = builder.maxVelocity;

        this.bounds = new CollisionBox(position,builder.width, builder.height);
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


    // ---------------------------------------------------------------------------------------------
    // BUILDER
    // ---------------------------------------------------------------------------------------------

    /**
     * We need a body builder as the body class has too many arguements... (pun intended)
     */
    public static class BodyBuilder {
        // Movement & Locality
        private Vector2 position = new Vector2(0f,0f);        // position contains the x/y grid
        private Vector2 velocity = new Vector2(0f,0f);        // velocity on the xy grid
        private Vector2 acceleration = new Vector2(0f,0f);    // acceleration on the xy grid
        private Vector2 offset = new Vector2(0f,0f);
        // Entity
        private Entity entity;
        // Physical parameters
        private float width, height;
        private float mass = 0;
        private float friction = 0;
        private float elasticity = 0;
        private float maxVelocity = 0;
        private float armor = 0;
        private float density = 1;

        public BodyBuilder(Entity entity) {
            this.entity = entity;
        }

        public BodyBuilder position(Vector2 position) {
            this.position = position;
            return this;
        }

        public BodyBuilder width(float width) {
            this.width = width;
            return this;
        }

        public BodyBuilder height(float height) {
            this.height = height;
            return this;
        }

        public BodyBuilder mass(float mass) {
            this.mass = mass;
            return this;
        }

        public BodyBuilder friction(float friction) {
            this.friction = friction;
            return this;
        }

        public BodyBuilder elasticity(float elasticity) {
            this.elasticity = elasticity;
            return this;
        }

        public BodyBuilder maxVelocity(float maxVelocity) {
            this.maxVelocity = maxVelocity;
            return this;
        }

        public BodyBuilder density(float density) {
            this.density = density;
            return this;
        }

        public BodyBuilder armor(float armor) {
            this.armor = armor;
            return this;
        }

        public BodyBuilder offset(Vector2 offset) {
            this.offset = offset;
            return this;
        }

        public Body bodyBuild() {
            return new Body(this);
        }

    }


    // ---------------------------------------------------------------------------------------------
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


