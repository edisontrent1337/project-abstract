package com.trent.awesomejumper.controller;


import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.trent.awesomejumper.models.Level;
import com.trent.awesomejumper.models.Player;
import com.trent.awesomejumper.models.WorldContainer;
import com.trent.awesomejumper.engine.physics.CollisionBox;
import com.trent.awesomejumper.utils.Interval;
import com.trent.awesomejumper.tiles.Tile;

import static com.trent.awesomejumper.utils.Utilities.dPro;
import static com.trent.awesomejumper.utils.Utilities.getOverlap;
import static com.trent.awesomejumper.utils.Utilities.getProjection;
import static com.trent.awesomejumper.utils.Utilities.overlaps;
import static com.trent.awesomejumper.utils.Utilities.subVec;

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
     * Detects and resolves any occurring collisions between the player and other objects in the world.
     * @param delta time which has passed since the last update frame
     */
    public void collisionDetection(float delta) {

        // reset resolutionVector to (0f,0f)
        resolutionVector.x = 0f;
        resolutionVector.y = 0f;
        player.getVelocity().scl(delta);

        // -----------------------------------------------------------------------------------------
        // VERTICAL COLLISION DETECTION
        // -----------------------------------------------------------------------------------------

        cdStartX = (int) (player.getBounds().x);
        cdEndX = (int)  (player.getBounds().x + player.getBounds().getWidth());

        /**
         * The players velocity is added here to cover tiles which might be in the range of the players
         * intended movement.
         */
        if(player.getVelocity().y <= 0)
            cdStartY = cdEndY = (int) Math.floor(player.getBounds().y);
        else
            cdStartY = cdEndY = (int) Math.floor(player.getBounds().y + player.getBounds().getHeight());

        // Create array of tiles surrounding the player which are covered by the collision detection
        worldContainer.createCollisionTiles(cdStartX, cdStartY, cdEndX, cdEndY);

        for(Tile tile: worldContainer.getCollisionTiles()) {
            // TODO: add a big bounding box for all objects which is used for game object / world collision detection only
            CollisionBox playerCollisionBox = player.getBodyHitboxes().get(0);
            CollisionBox tileBox = tile.getCollisionBox();

            /**
             * If a collision occurs between a solid world tile and the player the corresponding player's
             * velocity component will be reset to 0 and the resolutionVector is added to the player's
             * position to resolve the conflict.
             */
            if(checkCollision(tileBox, playerCollisionBox) &! tile.isPassable()) {

                if(resolutionVector.x != 0f)
                    player.setVelocityX(0f);

                if(resolutionVector.y != 0f)
                    player.setVelocityY(0f);

                player.getPosition().add(resolutionVector);
                player.getVelocity().scl(1/delta);
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

        cdStartY = (int) (player.getBounds().y);
        cdEndY = (int) (player.getBounds().y + player.getBounds().getHeight());

        /**
         * The players velocity is added here to cover tiles which might be in the range of the players
         * intended movement.
         */
        if (player.getVelocity().x <= 0) {
            cdStartX = cdEndX = (int) Math.floor(player.getBounds().x);
        } else {
            cdStartX = cdEndX = (int) Math.floor(player.getBounds().x + player.getBounds().getWidth());
        }

        // Create array of tiles surrounding the player which are covered by the collision detection
        worldContainer.createCollisionTiles(cdStartX, cdStartY, cdEndX, cdEndY);

        for(Tile tile: worldContainer.getCollisionTiles()) {
            // TODO: add a big bounding box for all objects which is used for gameobject / world collision detection only
            CollisionBox playerCollisionBox = player.getBodyHitboxes().get(0);
            CollisionBox tileBox = tile.getCollisionBox();

            /**
             * If a collision occurs between a solid world tile and the player the corresponding player's
             * velocity component will be reset to 0 and the resolutionVector is added to the player's
             * position to resolve the conflict.
             */
            if(checkCollision(tileBox, playerCollisionBox) &! tile.isPassable()) {

                if(resolutionVector.x != 0f)
                    player.setVelocityX(0f);

                if(resolutionVector.y != 0f)
                    player.setVelocityY(0f);

                player.getPosition().add(resolutionVector);
                player.getVelocity().scl(1/delta);
                return;

            }


        }

        /**
         * If we made it this far, no collision has occurred and the player's velocity can remain
         * as is and is scaled back to its normal value.
         */

        player.getVelocity().scl(1/delta);

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
        Array<Vector2> normalsA = aBox.getNormals();
        Array<Vector2> normalsB = bBox.getNormals();

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
            if(!overlaps(projectionA,projectionB))
                return false;

            else {

                // get the overlap of both projections
                float overlap = getOverlap(projectionA, projectionB);
                /**
                 * Early exit #2: Both projections are touching, but no overlap is present. Hence no
                 * real collision is occurring.
                 */
                if(overlap == 0f)
                    return false;

                /**
                 * A real overlap has occurred, the minimal overlap is updated and a resolution
                 * vector is constructed.
                 *
                 */
                if(Math.abs(overlap) < Math.abs(minOverlap)) {

                    minOverlap = overlap;
                    resolutionVector = new Vector2(normalA);
                    // TODO: test whether or not swapping arguments here has any influence...
                    Vector2 difference = subVec(aBox.getPosition(), bBox.getPosition());

                    /**
                     * The orientation of the resolution vector is checked. If the dot product
                     * between the relative vector between a and b and the resolution vector is < 0,
                     * the resolution vector is pointing in the wrong direction.
                     */
                    if(dPro(difference, resolutionVector) < 0.0f) {
                        resolutionVector.x = -resolutionVector.x;
                        resolutionVector.y = -resolutionVector.y;
                    }

                    // finally scaling the resolution vector
                    resolutionVector.scl(minOverlap);

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
            if(!overlaps(projectionA,projectionB))
                return false;

            else {

                // get the overlap of both projections
                float overlap = getOverlap(projectionA, projectionB);
                /**
                 * Early exit #2: Both projections are touching, but no overlap is present. Hence no
                 * real collision is occurring.
                 */
                if(overlap == 0f)
                    return false;

                /**
                 * A real overlap has occurred, the minimal overlap is updated and a resolution
                 * vector is constructed.
                 *
                 */
                if(Math.abs(overlap) < Math.abs(minOverlap)) {

                    minOverlap = overlap;
                    resolutionVector = new Vector2(normalB);
                    // TODO: test whether or not swapping arguments here has any influence...
                    Vector2 difference = subVec(bBox.getPosition(), aBox.getPosition());

                    /**
                     * The orientation of the resolution vector is checked. If the dot product
                     * between the relative vector between a and b and the resolution vector is < 0,
                     * the resolution vector is pointing in the wrong direction.
                     */
                    if(dPro(difference, resolutionVector) < 0.0f) {
                        resolutionVector.x = -resolutionVector.x;
                        resolutionVector.y = -resolutionVector.y;
                    }

                    // finally scaling the resolution vector
                    resolutionVector.scl(minOverlap);

                }

            }

        }


        return true;
    }






}
