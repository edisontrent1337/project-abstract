package com.trent.awesomejumper.controller.collision;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.trent.awesomejumper.controller.entitymanagement.WorldContainer;
import com.trent.awesomejumper.controller.rendering.PopUpRenderer;
import com.trent.awesomejumper.engine.entity.Entity;
import com.trent.awesomejumper.engine.modelcomponents.popups.Message;
import com.trent.awesomejumper.engine.physics.CollisionBox;
import com.trent.awesomejumper.models.Player;
import com.trent.awesomejumper.models.projectile.Projectile;
import com.trent.awesomejumper.models.weapons.Weapon;
import com.trent.awesomejumper.tiles.Tile;
import com.trent.awesomejumper.utils.Interval;
import com.trent.awesomejumper.utils.Utils;

import java.util.Set;

import static com.trent.awesomejumper.controller.rendering.PopUpRenderer.PopUpCategories.MISC;
import static com.trent.awesomejumper.engine.modelcomponents.ModelComponent.ComponentID.HEALTH;
import static com.trent.awesomejumper.utils.PhysicalConstants.FRICTIONAL_COEFFICIENT;
import static com.trent.awesomejumper.utils.Utils.dot;
import static com.trent.awesomejumper.utils.Utils.getOverlap;
import static com.trent.awesomejumper.utils.Utils.getProjection;
import static com.trent.awesomejumper.utils.Utils.overlaps;
import static com.trent.awesomejumper.utils.Utils.sub;

/**
 * Collision controller. Resolves any relevant physical collisions between all entity types.
 * Created by Sinthu on 04.11.2015.
 */
public class CollisionController {

    private WorldContainer worldContainer;
    private Player player;
    private Vector2 resolutionVector;
    public static int calledPerFrame = 0;

    // Interval variables for determining collision candidates
    private int cdStartX, cdEndX;   // x axis
    private int cdStartY, cdEndY;   // y axis

    // CONSTRUCTOR
    // ---------------------------------------------------------------------------------------------

    public CollisionController(WorldContainer worldContainer) {
        this.worldContainer = worldContainer;
        this.player = worldContainer.getPlayer();
        this.resolutionVector = new Vector2(0f, 0f);
    }


    // METHODS
    // ---------------------------------------------------------------------------------------------


    // ---------------------------------------------------------------------------------------------
    // ENTITY / ENTITY COLLISION
    // ---------------------------------------------------------------------------------------------

    /**
     * Detects and resolves any occurring collisions between entities in the world.
     * @param entity entity whose collisions should be resolved.
     * @param others any set implenentation containing any entities.
     * @param delta  time which has passed since the last update frame
     */
    public void resolveEntityCollisions(Entity entity, Set<? extends Entity> others, float delta) {

        entity.getVelocity().scl(delta);

        for (Entity other : others) {
            /**
             * If the two participants are the same, one of them is part of the other such as equipped weapons etc.
             * or one of them is declared "dead", move to the next entity in neighbourhood
             */
            if (other.equals(entity) || !other.isAlive() || !other.getBody().isCollisionDetectionEnabled() || other.getOwner().equals(entity) || entity.getOwner().equals(other))
                continue;

            CollisionBox entityBox = entity.getBounds();
            CollisionBox otherBox = other.getBounds();

            /**
             * TODO: implement better way of pickup collision
             * TODO: if inventory is full, collision detection based equipping should not trigger.
             * Weapon/pickup collision detection
             */
            if (entity.equals(player) && !player.getWeaponInventory().isInventoryFull()) {
                switch (other.getType()) {
                    case DROPPED_WEAPON_ENTITY:
                        if (checkCollision(entityBox, otherBox)) {
                            player.getWeaponInventory().equipWeapon((Weapon) other);
                            continue;
                        }
                        break;

                    case PICKUP_ENTITY:
                        break;
                }

            }

            if (other.equals(player) && !player.getWeaponInventory().isInventoryFull()) {
                switch (entity.getType()) {
                    case DROPPED_WEAPON_ENTITY:
                        if (checkCollision(entityBox, otherBox)) {
                            player.getWeaponInventory().equipWeapon((Weapon) entity);
                            continue;
                        }
                        break;
                }
            } else if (checkCollision(otherBox, entityBox)) {

                /**
                 * deltaVelocity - Relative velocity between both participants of the collision.
                 * collisionNormal - normal to the collision plane
                 */
                Vector2 deltaVelocity = sub(other.getVelocity(), entity.getVelocity().cpy().scl(1 / delta));
                Vector2 collisionNormal = resolutionVector.cpy().nor();
                /**
                 * Calculate impulses to be added to each entity. Adding tangential and normal
                 * components to form one impulse vector. The magnitude in direction of the collision
                 * normal is negatively! scaled with the elasticity of the entity to push both entities
                 * away from each other.
                 */
                Vector2 impulseEntity = createReflectionImpulse(deltaVelocity, collisionNormal, entity.getBody().getElasticity());
                Vector2 impulseOther = createReflectionImpulse(deltaVelocity, collisionNormal, other.getBody().getElasticity());

                float entityMass = entity.getBody().getMass();
                float otherMass = other.getBody().getMass();
                float massSum = entityMass + otherMass;

                /**
                 * If the current entity did not collide with the world earlier, it can receive the
                 * impulse calculated above.
                 */
                if (!entity.getBody().isCollidedWithWorld()) {
                    entity.getBody().addImpulse(impulseEntity.cpy().scl(otherMass / massSum));
                }

                /**
                 * If the opponent entity (other) did not collide with the world earlier, it can
                 * receive the impulse. Also, the opposing entity is pushed backwards to resolve
                 * the collision.
                 */

                if (!other.getBody().isCollidedWithWorld()) {
                    other.getPosition().add(resolutionVector.cpy().scl(-1f));
                    other.getBody().addImpulse(impulseOther.cpy().scl(-entityMass / massSum));
                }
                /**
                 * If the opponent did collide with the world, we need to stop our movement.
                 * The resolution vector is added to our position so the collision can be resolved.
                 */
                else {
                    if (resolutionVector.x != 0f)
                        entity.setVelocityX(0f);
                    if (resolutionVector.y != 0f)
                        entity.setVelocityY(0f);
                    if (other.getBody().isCollidedWithWorld())
                        entity.getBody().setCollidedWithWorld(true);
                    entity.getPosition().add(resolutionVector);
                }

                entity.getVelocity().scl(1 / delta);
                return;

            }

        }

        /**
         * If we made it this far, no collision has occurred and the entities velocity can remain
         * as is and is scaled back to its normal value.
         */
        entity.getVelocity().scl(1 / delta);

    }


    // ---------------------------------------------------------------------------------------------
    // ENTITY / WORLD COLLISION
    // ---------------------------------------------------------------------------------------------

    //TODO: world collision does not have to be split by horizontal or vertical cd.
    //TODO: remove all aspects that have to do with a cd split by horizontal or vertical cd
    //TODO: because spatial hashing works!!! - not quite yet, noob :/
    public void resolveWorldCollisions(Entity entity, float delta) {


        // reset resolutionVector to (0f,0f)
        resolutionVector.x = 0f;
        resolutionVector.y = 0f;
        entity.getVelocity().scl(delta);
        entity.getBody().setCollidedWithWorld(false);

        // -----------------------------------------------------------------------------------------
        // VERTICAL COLLISION DETECTION
        // -----------------------------------------------------------------------------------------

        cdStartX = (int) (entity.getBounds().getPositionAndOffset().x);
        cdEndX = (int) (entity.getBounds().getPositionAndOffset().x + entity.getBounds().getWidth());

        /**
         * Cover tiles which might be in the range of the entities intended movement.
         */
        if (entity.getVelocity().y <= 0)
            cdStartY = cdEndY = (int) Math.floor(entity.getBounds().getPositionAndOffset().y + entity.getVelocity().y);
        else
            cdStartY = cdEndY = (int) Math.floor(entity.getBounds().getPositionAndOffset().y + entity.getBounds().getHeight() + entity.getVelocity().y);

        // Create array of tiles surrounding the entity which are covered by the collision detection
        worldContainer.fillCollideableTiles(cdStartX, cdStartY, cdEndX, cdEndY);

        // USING NOW HASH DS
        for (Tile tile : worldContainer.getTilesNearby(entity)) {

            CollisionBox entityCollisionBox = entity.getBounds();
            CollisionBox tileBox = tile.getCollisionBox();

            /**
             * If the entity is a projectile, projectile/world collision has to be resolved.
             */
            if (entity.getClass() == Projectile.class) {
                if (projectileCollisionDetection((Projectile) entity, tile))
                    return;    // exit collision routine
                else
                    continue; // continue with next tile
            }
            /**
             * If a collision occurs between a solid world tile and the entity the corresponding entities
             * velocity component will be reset to 0 and the resolutionVector is added to the entities
             * position to resolve the conflict.
             * Also, an orthogonal impulse is created and added to the entities list of impulses.
             */
            if (checkCollision(tileBox, entityCollisionBox) & !tile.isPassable()) {
                entity.getBody().setCollidedWithWorld(true);
                if (!entity.equals(player)) {
                    entity.getBody().getImpulses().clear();
                    entity.getBody().addImpulse(createReflectionImpulse(entity.getVelocity().cpy().scl(1 / delta),
                            resolutionVector.cpy().nor(),
                            entity.getBody().getElasticity()));
                }
                if (resolutionVector.x != 0f)
                    entity.setVelocityX(0f);

                if (resolutionVector.y != 0f)
                    entity.setVelocityY(0f);

                entity.getPosition().add(resolutionVector);
                entity.getVelocity().scl(1 / delta);
                return;

            }

        }


        /**
         * If no collision was found regarding the y axis, the process is repeated for the x axis
         * with different parameters for the fillCollideableTiles method.
         */

        // Reset the resolutionVector to (0f,0f)

        resolutionVector.x = 0f;
        resolutionVector.y = 0f;

        // -----------------------------------------------------------------------------------------
        // HORIZONTAL COLLISION DETECTION
        // -----------------------------------------------------------------------------------------

        cdStartY = (int) (entity.getBounds().getPositionAndOffset().y);
        cdEndY = (int) (entity.getBounds().getPositionAndOffset().y + entity.getBounds().getHeight());

        /**
         * Cover tiles which might be in the range of the entities intended movement.
         */

        if (entity.getVelocity().x <= 0) {
            cdStartX = cdEndX = (int) Math.floor(entity.getBounds().getPositionAndOffset().x + entity.getVelocity().x);
        } else {
            cdStartX = cdEndX = (int) Math.floor(entity.getBounds().getPositionAndOffset().x + entity.getBounds().getWidth() + entity.getVelocity().x);
        }

        // Create array of tiles surrounding the player which are covered by the collision detection
        worldContainer.fillCollideableTiles(cdStartX, cdStartY, cdEndX, cdEndY);

        for (Tile tile : worldContainer.getTilesNearby(entity)) {

            CollisionBox entityCollisionBox = entity.getBounds();
            CollisionBox tileBox = tile.getCollisionBox();

            /**
             * If the entity is a projectile, projectile/world collision has to be resolved.
             */
            if (entity.getClass() == Projectile.class) {
                if (projectileCollisionDetection((Projectile) entity, tile))
                    return;     // exit collision routine
                else
                    continue; // continue with next tile
            }
            /**
             * If a collision occurs between a solid world tile and the entity the corresponding entities
             * velocity component will be reset to 0 and the resolutionVector is added to the entities
             * position to resolve the conflict.
             * Also, an orthogonal impulse is created and added to the entities list of impulses.
             */
            if (checkCollision(tileBox, entityCollisionBox) & !tile.isPassable()) {
                entity.getBody().setCollidedWithWorld(true);
                if (!entity.equals(player)) {
                    entity.getBody().getImpulses().clear();
                    entity.getBody().addImpulse(createReflectionImpulse(entity.getVelocity().cpy().scl(1 / delta),
                            resolutionVector.cpy().nor(),
                            entity.getBody().getElasticity()));
                }
                if (resolutionVector.x != 0f)
                    entity.setVelocityX(0f);

                if (resolutionVector.y != 0f)
                    entity.setVelocityY(0f);

                entity.getPosition().add(resolutionVector);
                entity.getVelocity().scl(1 / delta);
                return;

            }
        }


        entity.getVelocity().scl(1 / delta);
    }


    // ---------------------------------------------------------------------------------------------
    // COLLISION CHECK AND RESOLUTION BETWEEN 2 COLLISIONBOXES
    // ---------------------------------------------------------------------------------------------

    /**
     * Calculates the minimum translation vector needed two push two
     * actors away from each other to resolve a collision
     *
     * @param aBox collision box of object a
     * @param bBox collision box of object b
     * @return false, when no collision was detected
     * true, when an collision was detected. resolutionVector holds the information
     * about how to resolve the collision.
     */

    private boolean checkCollision(CollisionBox aBox, CollisionBox bBox) {
        calledPerFrame++;
        /**
         * The minimal overlap is initialized with a very large value.
         */
        float minOverlap = 10000f;

        // Get normals of each shape
        Array<Vector2> normalsA = aBox.getNormals();
        Array<Vector2> normalsB = bBox.getNormals();

        /**
         * Calculating the projection of both shapes onto the each of the normals of shape A.
         * The projections are saved in intervals containing the min and max value of the projection.
         */
        for (Vector2 normalA : normalsA) {

            Interval projectionA = getProjection(aBox, normalA);
            Interval projectionB = getProjection(bBox, normalA);

            /**
             * Early exit #1: When there is no overlap between both projections, the separating axis
             * theorem states that there is no way a collision can still occur.
             */
            if (!overlaps(projectionA, projectionB)) {
                return false;
            } else {

                // get the overlap of both projections
                float overlap = getOverlap(projectionA, projectionB);
                /**
                 * Early exit #2: Both projections are touching, but no overlap is present. Hence no
                 * real collision is occurring.
                 */
                if (overlap == 0f) {
                    return false;
                }

                /**
                 * A real overlap has occurred, the minimal overlap is updated and a resolution
                 * vector is constructed.
                 *
                 */
                if (Math.abs(overlap) < Math.abs(minOverlap)) {

                    minOverlap = overlap;
                    resolutionVector = new Vector2(normalA);
                    Vector2 difference = sub(bBox.getPosition(), aBox.getPosition());

                    /**
                     * The orientation of the resolution vector is checked. If the dot product
                     * between the relative vector between a and b and the resolution vector is < 0,
                     * the resolution vector is pointing in the wrong direction.
                     */
                    // finally scaling the resolution vector
                    resolutionVector.scl(minOverlap);
                    if (dot(difference, resolutionVector) > 0.0f) {
                        resolutionVector.x = -resolutionVector.x;
                        resolutionVector.y = -resolutionVector.y;
                    }

                }

            }

        }


        /**
         * Calculating the projection of both shapes onto the each of the normals of shape B.
         * The projections are saved in intervals containing the min and max value of the projection.
         */
        for (Vector2 normalB : normalsB) {

            Interval projectionA = getProjection(aBox, normalB);
            Interval projectionB = getProjection(bBox, normalB);

            /**
             * Early exit #1: When there is no overlap between both projections, the separating axis
             * theorem states that there is no way a collision can still occur.
             */
            if (!overlaps(projectionA, projectionB)) {
                return false;
            } else {

                // get the overlap of both projections
                float overlap = getOverlap(projectionA, projectionB);
                /**
                 * Early exit #2: Both projections are touching, but no overlap is present. Hence no
                 * real collision is occurring.
                 */
                if (overlap == 0f) {
                    return false;
                }

                /**
                 * A real overlap has occurred, the minimal overlap is updated and a resolution
                 * vector is constructed.
                 *
                 */
                if (Math.abs(overlap) < Math.abs(minOverlap)) {

                    minOverlap = overlap;
                    resolutionVector = new Vector2(normalB);
                    Vector2 difference = sub(bBox.getPosition(), aBox.getPosition());

                    /**
                     * The orientation of the resolution vector is checked. If the dot product
                     * between the relative vector between a and b and the resolution vector is < 0,
                     * the resolution vector is pointing in the wrong direction.
                     */
                    // finally scaling the resolution vector
                    resolutionVector.scl(minOverlap);
                    if (dot(difference, resolutionVector) > 0.0f) {
                        resolutionVector.x = -resolutionVector.x;
                        resolutionVector.y = -resolutionVector.y;
                    }


                }

            }

        }


        return true;
    }


    // ---------------------------------------------------------------------------------------------
    // IMPULSE CREATION
    // ---------------------------------------------------------------------------------------------

    /**
     * Creates an impulse vector with orthogonal orientation towards the incoming relativeVelocity.
     * The entities elasticity is used to scale the impulse properly.
     * The FRICTIONAL_COEFFICIENT is a global constant to simulate friction along the impact plane.
     *
     * @param relativeVelocity relativeVelocity of the entity
     * @param collisionNormal  axis on which the relativeVelocity should be mirrored
     * @param elasticity       elasticity coefficient of the entity
     * @return reflection impulse
     */
    public Vector2 createReflectionImpulse(Vector2 relativeVelocity, Vector2 collisionNormal, float elasticity) {

        Vector2 vNorm = collisionNormal.cpy().scl(dot(relativeVelocity, collisionNormal));
        Vector2 vTang = sub(vNorm, relativeVelocity).scl(-FRICTIONAL_COEFFICIENT);
        Vector2 reflection = vTang.cpy().add(vNorm.cpy().scl(-(1 - elasticity)));
        return reflection;
    }


    // ---------------------------------------------------------------------------------------------
    // PROJECTILE / ENTITY COLLISION
    // ---------------------------------------------------------------------------------------------

    public void projectileCollisionDetection(Entity e, Projectile p, float delta) {
        Vector2 entityVelo = e.getVelocity().cpy().scl(delta);
        Vector2 projectileVelo = p.getVelocity().cpy().scl(delta);

        projectileCollisionDetection(p, e, projectileVelo, entityVelo, delta);
    }

    /**
     * Resolution of entity / projectile collision with the method of continuous collision detection.
     * On successful hit, the projectile deals damage according to its damage coefficient and the damage
     * coefficient of the collision box. The projectile is destroyed afterwards.
     *
     * @param projectile         Projectile entity
     * @param entity             Entity to be hit
     * @param projectileVelocity positional change of projectile per delta time unit
     * @param entityVelocity     positional change of entity per delta time unit
     * @return true, if a collision occurred, false otherwise.
     */
    private boolean projectileCollisionDetection(Projectile projectile, Entity entity, Vector2 projectileVelocity, Vector2 entityVelocity, float delta) {


        //TODO: REMOVE THIS.
        if (entity instanceof Weapon) {
            Gdx.app.log("WEAPON", "COLLIDED WITH PROJECTILE");
        }

        if (projectile.getOwner().equals(entity))
            return false;

        // If the other entity can not be hurt, ignore the collision detection.
        if (!entity.has(HEALTH))
            return false;


        CollisionBox entityBounds = entity.getBounds();
        // relative velocity between projectile and entity
        float relativeVelocity = sub(projectileVelocity, entityVelocity).len();
        // distance between projectile and current hitbox
        float dst = sub(projectile.getPosition(), entityBounds.getPositionAndOffset()).len();
        // time in frame steps remaining before collision occurs
        float framesToImpact = dst / relativeVelocity;

        Vector2 deltaVelocity = sub(projectileVelocity, entityVelocity);
        Vector2 collisionNormal = resolutionVector.cpy().nor();
        float force = projectile.getBody().getMass() * projectileVelocity.cpy().scl(1 / delta).len() * (1 / entity.getBody().getMass());

        Vector2 impulse = createReflectionImpulse(deltaVelocity, collisionNormal, entity.getBody().getElasticity());
        impulse.scl(force * force);


        /**
         * If the number of frames until the impact occurs is between 0 and 1, the collision
         * can happen in the next frame, so it has to be resolved by checkCollision
         */
        if (framesToImpact > 0 && framesToImpact < 1) {
            float step = 0.05f;
            float numberOfSteps = dst / step;
            Utils.log("NUMBER OF STEPS", Float.toString(numberOfSteps));
            for (float i = 0f; i <= numberOfSteps; i++) {
                Vector2 projectilePosition = projectile.getPosition();
                Vector2 frameStep = deltaVelocity.cpy().nor().scl(step);
                Utils.log("OLD POSITION", projectilePosition.toString());
                Utils.log("FRAME STEP", frameStep.toString());
                projectilePosition.add(frameStep);
                Utils.log("NEW POSITION", projectilePosition.toString());
                projectile.setPosition(projectilePosition);

                if (checkCollision(projectile.getBounds(), entityBounds)) {
                    Utils.log("Collision detected.");
                    break;
                }
            }

            Message m = new Message(projectile.getPosition().cpy().toString(), projectile.getPosition().cpy(), entity.time, 3.00f);
            PopUpRenderer.getInstance().addMessageToCategory(MISC, m);

            /**
             * If the bounds/shadows of the projectile and the entity collide,
             * another check has to pass: whether or not the projectile is in the correct
             * z-height to hit the entity or not.
             * TODO: z component should only be cosmetic as it hinders gameplay. maybe scrap idea of hitbox skeleton.
             */
            projectile.setVelocity(0f, 0f);
            if (entity.has(HEALTH)) {
                entity.getHealth().takeDamage(projectile.dealDamage(entityBounds));
                entity.getBody().addImpulse(impulse);
            }
            projectile.destroy();
            return true;
        } else if (checkCollision(projectile.getBounds(), entityBounds)) {
            Message m = new Message(projectile.getPosition().cpy().toString(), projectile.getPosition().cpy(), entity.time, 3.00f);
            PopUpRenderer.getInstance().addMessageToCategory(MISC, m);

            projectile.setVelocity(0f, 0f);
            if (entity.has(HEALTH)) {
                entity.getHealth().takeDamage(projectile.dealDamage(entityBounds));

                entity.getBody().addImpulse(impulse);
            }
            projectile.destroy();
            return true;
        }

        return false;
    }

    // ---------------------------------------------------------------------------------------------
    // PROJECTILE / WORLD COLLISION
    // ---------------------------------------------------------------------------------------------

    /**
     * Resolution of projectile / world collision with the method of continuous collision detection.
     * On successful hit, the projectile is destroyed.
     *
     * @param projectile Projectile entity
     * @param tile       Tile to be hit
     * @return true, if a collision occurred, false otherwise.
     */
    private boolean projectileCollisionDetection(Projectile projectile, Tile tile) {

        float dst = sub(projectile.getPosition(), tile.getPosition()).len();
        float framesTillImpact = dst / projectile.getVelocity().len();

        // FAST BULLET
        if (framesTillImpact > 0 && framesTillImpact < 1) {
            float step = 0.0125f;
            float numberOfSteps = dst / step;
            Utils.log("NUMBER OF STEPS", Float.toString(numberOfSteps));
            for (float i = 0f; i <= numberOfSteps; i++) {
                Vector2 projectilePosition = projectile.getPosition();
                Vector2 frameStep = projectile.getVelocity().cpy().nor().scl(step);
                Utils.log("OLD POSITION", projectilePosition.toString());
                Utils.log("FRAME STEP", frameStep.toString());
                projectilePosition.add(frameStep);
                Utils.log("NEW POSITION", projectilePosition.toString());
                projectile.setPosition(projectilePosition);

                if (checkCollision(projectile.getBounds(), tile.getCollisionBox())) {
                    Utils.log("Collision detected.");
                    return true;
                }
            }
            Message m = new Message(projectile.getPosition().cpy().toString(), projectile.getPosition().cpy(), projectile.time, 3.00f);
            PopUpRenderer.getInstance().addMessageToCategory(MISC, m);
            projectile.setVelocity(0f, 0f);
            projectile.destroy();

            return true;
        }
        // SLOW BULLET
        else if (checkCollision(projectile.getBounds(), tile.getCollisionBox())) {

            Message m = new Message(projectile.getPosition().cpy().toString(), projectile.getPosition().cpy(), projectile.time, 3.00f);
            PopUpRenderer.getInstance().addMessageToCategory(MISC, m);

            projectile.setVelocity(0f, 0f);
            projectile.destroy();
            return true;
        }
        return false;
    }

    //TODO: change this.
    public void setPlayer() {
        this.player = worldContainer.getPlayer();
    }


}
