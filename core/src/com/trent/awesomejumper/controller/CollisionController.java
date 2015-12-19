package com.trent.awesomejumper.controller;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.trent.awesomejumper.engine.entity.Entity;
import com.trent.awesomejumper.engine.modelcomponents.popups.PopUpFeed;
import com.trent.awesomejumper.models.Level;
import com.trent.awesomejumper.models.Player;
import com.trent.awesomejumper.models.WorldContainer;
import com.trent.awesomejumper.engine.physics.CollisionBox;
import com.trent.awesomejumper.utils.Interval;
import com.trent.awesomejumper.tiles.Tile;
import com.trent.awesomejumper.engine.modelcomponents.popups.Message;



import static com.trent.awesomejumper.utils.Utilities.*;
import static com.trent.awesomejumper.utils.PhysicalConstants.*;
/**
 * Created by Sinthu on 04.11.2015.
 */
public class CollisionController {

    private WorldContainer worldContainer;
    private Level level;
    private Player player;
    private Vector2 resolutionVector;

    // Interval variables for determining collision candidates
    private int cdStartX, cdEndX;   // x axis
    private int cdStartY, cdEndY;   // y axis




    // CONSTRUCTOR
    // ---------------------------------------------------------------------------------------------

    public CollisionController(WorldContainer worldContainer) {
        this.worldContainer = worldContainer;
        this.player = worldContainer.getPlayer();
        this.level = worldContainer.getLevel();
        this.resolutionVector = new Vector2(0f,0f);

    }


    // METHODS
    // ---------------------------------------------------------------------------------------------

    /**
     * Detects and resolves any occurring collisions between entities and other objects in the world.
     * Entity/World collision is fully supported.
     * Entity/Entity collision is a work in progress.
     * @param delta time which has passed since the last update frame
     */
    public void collisionDetection(Entity entity, float delta) {

        // reset resolutionVector to (0f,0f)
        resolutionVector.x = 0f;
        resolutionVector.y = 0f;
        entity.getVelocity().scl(delta);
        entity.getBody().setCollidedWithWorld(false);

        // -----------------------------------------------------------------------------------------
        // VERTICAL COLLISION DETECTION
        // -----------------------------------------------------------------------------------------

        cdStartX = (int) (entity.getBounds().getPositionAndOffset().x);
        cdEndX = (int)  (entity.getBounds().getPositionAndOffset().x + entity.getBounds().getWidth());

        /**
         * The players velocity is added here to cover tiles which might be in the range of the players
         * intended movement.
         */
        if(entity.getVelocity().y <= 0)
            cdStartY = cdEndY = (int) Math.floor(entity.getBounds().getPositionAndOffset().y);
        else
            cdStartY = cdEndY = (int) Math.floor(entity.getBounds().getPositionAndOffset().y + entity.getBounds().getHeight());

        // Create array of tiles surrounding the entity which are covered by the collision detection
        worldContainer.createCollisionTiles(cdStartX, cdStartY, cdEndX, cdEndY);

        for(Tile tile: worldContainer.getCollisionTiles()) {

            CollisionBox entityCollisionBox = entity.getBounds();
            CollisionBox tileBox = tile.getCollisionBox();

                /**
                 * If a collision occurs between a solid world tile and the entity the corresponding entities
                 * velocity component will be reset to 0 and the resolutionVector is added to the entities
                 * position to resolve the conflict.
                 */
                if (checkCollision(tileBox, entityCollisionBox) &! tile.isPassable()) {
                    entity.getBody().setCollidedWithWorld(true);
                    if(!entity.equals(player)) {
                        entity.getBody().getImpulses().clear();
                        entity.getBody().addImpulse(createReflectionImpulse(entity, entity.getVelocity().cpy().scl(1 / delta), resolutionVector.cpy().nor()));
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
         * with different parameters for the createCollisionTiles method.
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
         * The players velocity is added here to cover tiles which might be in the range of the players
         * intended movement.
         */
        if (entity.getVelocity().x <= 0) {
            cdStartX = cdEndX = (int) Math.floor(entity.getBounds().getPositionAndOffset().x);
        } else {
            cdStartX = cdEndX = (int) Math.floor(entity.getBounds().getPositionAndOffset().x + entity.getBounds().getWidth());
        }

        // Create array of tiles surrounding the player which are covered by the collision detection
        worldContainer.createCollisionTiles(cdStartX, cdStartY, cdEndX, cdEndY);

        for(Tile tile: worldContainer.getCollisionTiles()) {

            CollisionBox entityCollisionBox = entity.getBounds();
            CollisionBox tileBox = tile.getCollisionBox();

            /**
             * If a collision occurs between a solid world tile and the entity the corresponding entities
             * velocity component will be reset to 0 and the resolutionVector is added to the entities
             * position to resolve the conflict.
             */
            if (checkCollision(tileBox, entityCollisionBox) & !tile.isPassable()) {
                entity.getBody().setCollidedWithWorld(true);
                    if(!entity.equals(player)) {
                        entity.getBody().getImpulses().clear();
                        entity.getBody().addImpulse(createReflectionImpulse(entity, entity.getVelocity().cpy().scl(1 / delta), resolutionVector.cpy().nor()));
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


        // -----------------------------------------------------------------------------------------
        // ENTITY ENTITY COLLISION DETECTION
        // -----------------------------------------------------------------------------------------


        //TODO: add a container for entities that are in range of other entities

        for(Entity other: worldContainer.getEntities()) {
            if(other.equals(entity))
                continue;
            CollisionBox playerCollisionBox = entity.getBounds();
            CollisionBox b = other.getBounds();

            if(checkCollision(b, playerCollisionBox)) {
                //TODO: Edit damage font to look thicker and add a white border (optional)
                    int dmg = (int) (Math.random() * 170) + 130;
                    if(entity.hasHealth) {
                        if (entity.getHealth().takeDamage(dmg)) {
                            if(dmg > 250)
                            entity.getPopUpFeed().addMessageToCategory(PopUpFeed.PopUpCategories.CRT, new Message("-" + Integer.toString(dmg), entity.time, 2.00f));
                            else
                            entity.getPopUpFeed().addMessageToCategory(PopUpFeed.PopUpCategories.DMG, new Message("-" + Integer.toString(dmg), entity.time, 2.00f));

                            other.getPopUpFeed().addMessageToCategory(PopUpFeed.PopUpCategories.HEAL, new Message("+" + Integer.toString(dmg), other.time, 4.00f));
                            other.getPopUpFeed().addMessageToCategory(PopUpFeed.PopUpCategories.LVL_UP, new Message("LEVEL UP! " + Integer.toString(dmg), other.time, 2.00f));
                        }
                    }


                /**
                 * deltaVelocity - Relative velocity between both participants of the collision.
                 * collisionNormal - normal to the collision plane
                 * vNorm - relative velocity projected onto the collisionNormal
                 * vTang - sub vector of normal and relative velocity = tangential component
                 */
                Vector2 deltaVelocity = subVec(other.getVelocity(),entity.getVelocity().cpy().scl(1/delta));
                Vector2 collisionNormal = resolutionVector.cpy().nor();
                Vector2 vNorm = collisionNormal.cpy().scl(dPro(deltaVelocity, collisionNormal));
                Vector2 vTang = subVec(vNorm, deltaVelocity).scl(-FRICTIONAL_COEFFICIENT);
                /**
                 * Calculate impulses to be added to each entity. Adding tangential and normal
                 * components to form one impulse vector. The magnitude in direction of the collision
                 * normal is negatively! scaled with the elasticity of the entity to push both entities
                 * away from each other.
                 */
                Vector2 impulseEntity =vTang.cpy().add(vNorm.cpy().scl(-(1 - entity.getBody().getElasticity())));
                Vector2 impulseOther = vTang.cpy().add(vNorm.cpy().scl(-(1 - other.getBody().getElasticity())));

                float entityMass = entity.getBody().getMass();
                float otherMass = other.getBody().getMass();
                float massSum = entityMass + otherMass;

                /**
                 * If the current entity did not collide with the world earlier, it can receive the
                 * impulse calculated above.
                 */
                if (!entity.getBody().isCollidedWithWorld())  {
                    entity.getBody().addImpulse(impulseEntity.cpy().scl(otherMass / massSum));
                }

                /**
                 * If the opponent entity (other) did not collide with the world earlier, it can
                 * receive the impulse. Also, the opposing entity is pushed backwards.
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
                        if(resolutionVector.x != 0f)
                        entity.setVelocityX(0f);
                        if(resolutionVector.y != 0f)
                        entity.setVelocityY(0f);
                        if(other.getBody().isCollidedWithWorld())
                        entity.getBody().setCollidedWithWorld(true);
                        entity.getPosition().add(resolutionVector);
                    }

                entity.getVelocity().scl(1/delta);
                return;

            }

        }

        /**
         * If we made it this far, no collision has occurred and the entities velocity can remain
         * as is and is scaled back to its normal value.
         */
        entity.getVelocity().scl(1 / delta);
    }




    /**
     * Calculates the minimum translation vector needed two push two
     * actors away from each other to resolve a collision
     * @param aBox collision box of object a
     * @param bBox collision box of object b
     * @return false, when no collision was detected
     *         true, when an collision was detected. resolutionVector holds the information
     *         about how to resolve the collision.
     */

    private boolean checkCollision(CollisionBox aBox, CollisionBox bBox) {
        /**
         * The minimal overlap is initialized with a very large value.
         */
        float minOverlap = 10000f;

        // Get normals of each shape
        Array<Vector2> normalsA = aBox.getNormals(); // WORLD
        Array<Vector2> normalsB = bBox.getNormals(); // PLAYER

        /**
         * Calculating the projection of both shapes onto the each of the normals of shape A.
         * The projections are saved in intervals containing the min and max value of the projection.
         */
        for(Vector2 normalA : normalsA) {

            Interval projectionA = getProjection(aBox, normalA);
            Interval projectionB = getProjection(bBox, normalA);

            /**
             * Early exit #1: When there is no overlap between both projections, the separating axis
             * theorem states that there is no way a collision can still occur.
             */
            if(!overlaps(projectionA,projectionB)) {
                return false;
            }

            else {

                // get the overlap of both projections
                float overlap = getOverlap(projectionA, projectionB);
                /**
                 * Early exit #2: Both projections are touching, but no overlap is present. Hence no
                 * real collision is occurring.
                 */
                if(overlap == 0f) {
                    return false;
                }

                /**
                 * A real overlap has occurred, the minimal overlap is updated and a resolution
                 * vector is constructed.
                 *
                 */
                if(Math.abs(overlap) < Math.abs(minOverlap)) {

                    minOverlap = overlap;
                    resolutionVector = new Vector2(normalA);
                    Vector2 difference = subVec(bBox.getPosition(), aBox.getPosition());

                    /**
                     * The orientation of the resolution vector is checked. If the dot product
                     * between the relative vector between a and b and the resolution vector is < 0,
                     * the resolution vector is pointing in the wrong direction.
                     */
                     // finally scaling the resolution vector
                     resolutionVector.scl(minOverlap);
                     if(dPro(difference, resolutionVector) > 0.0f) {
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
        for(Vector2 normalB : normalsB) {

            Interval projectionA = getProjection(aBox, normalB);
            Interval projectionB = getProjection(bBox, normalB);

            /**
             * Early exit #1: When there is no overlap between both projections, the separating axis
             * theorem states that there is no way a collision can still occur.
             */
            if(!overlaps(projectionA,projectionB)) {
                return false;
            }

            else {

                // get the overlap of both projections
                float overlap = getOverlap(projectionA, projectionB);
                /**
                 * Early exit #2: Both projections are touching, but no overlap is present. Hence no
                 * real collision is occurring.
                 */
                if(overlap == 0f) {
                    return false;
                }

                /**
                 * A real overlap has occurred, the minimal overlap is updated and a resolution
                 * vector is constructed.
                 *
                 */
                if(Math.abs(overlap) < Math.abs(minOverlap)) {

                    minOverlap = overlap;
                    resolutionVector = new Vector2(normalB);
                    Vector2 difference = subVec(bBox.getPosition(), aBox.getPosition());

                    /**
                     * The orientation of the resolution vector is checked. If the dot product
                     * between the relative vector between a and b and the resolution vector is < 0,
                     * the resolution vector is pointing in the wrong direction.
                     */
                    // finally scaling the resolution vector
                    resolutionVector.scl(minOverlap);
                    if(dPro(difference, resolutionVector) > 0.0f) {
                        resolutionVector.x = -resolutionVector.x;
                        resolutionVector.y = -resolutionVector.y;
                    }


                }

            }

        }


        return true;
    }



    public Vector2 createReflectionImpulse(Entity e, Vector2 velocity, Vector2 collisionNormal) {

        Gdx.app.log("REFLECTION ENTER/VEL", collisionNormal.toString() + velocity.toString());
        Vector2 vNorm = collisionNormal.cpy().scl(dPro(velocity, collisionNormal));
        Vector2 vTang = subVec(velocity, vNorm).scl(-FRICTIONAL_COEFFICIENT);


        Vector2 reflection = vTang.cpy().add(vNorm.cpy().scl(-(1 - e.getBody().getElasticity())));

        Gdx.app.log("REFLECTION", reflection.toString());
        return reflection;
    }

}
