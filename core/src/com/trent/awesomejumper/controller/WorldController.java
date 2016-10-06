package com.trent.awesomejumper.controller;

import com.badlogic.gdx.math.Vector2;
import com.trent.awesomejumper.controller.collision.CollisionController;
import com.trent.awesomejumper.engine.entity.Entity;
import com.trent.awesomejumper.models.projectile.Projectile;

import java.util.Iterator;
import java.util.LinkedList;

import static com.trent.awesomejumper.utils.PhysicalConstants.*;

/**
 * Created by Sinthu on 12.06.2015.
 */
public class WorldController {



    // MEMBERS & INSTANCES
    // ---------------------------------------------------------------------------------------------

    private WorldContainer worldContainer;
    private CollisionController collisionController;

    public static float worldTime = 0f;



    // CONSTRUCTOR
    // ---------------------------------------------------------------------------------------------

    public WorldController(WorldContainer worldContainer) {
        this.worldContainer = worldContainer;
        this.collisionController = new com.trent.awesomejumper.controller.collision.CollisionController(worldContainer);
    }





    // UPDATE FUNCTION: COLLISION DETECTION
    // ---------------------------------------------------------------------------------------------

    /**
     * Updates all entities in the world in the following order:
     * - resolve collisions between only alive entities, kill for example collided projectiles
     * - remove all dead entities from the game data (garbage collection)
     * - apply impulses to all entities that are left alive
     * - limit entity speed
     * - finally update positional information of all entities
     * @param delta time between frames
     */
    public void update(float delta) {



        worldTime += delta;

        for(Entity e: worldContainer.getEntities()) {
            e.getAcceleration().scl(delta);
            e.getVelocity().add(e.getAcceleration());
        }


        for(Entity e : worldContainer.getEntities()) {
            if(!e.isAlive() || !e.getBody().isCollisionDetectionEnabled())
                continue;
            collisionController.resolveWorldCollisions(e,delta);
        }


        for(Entity e: worldContainer.getMobileEntities()) {
            if(!e.isAlive() || !e.getBody().isCollisionDetectionEnabled())
                continue;
            collisionController.resolveEntityCollisions(e,delta);
        }

        for(Entity e : worldContainer.getLivingEntities()) {
            for(Projectile p: worldContainer.getProjectiles())
                collisionController.projectileCollisionDetection(e,p,delta);
        }

        worldContainer.garbageRemoval();

        // TODO: add a function called applyImpulses()

        for(Entity e: worldContainer.getEntities()) {
            LinkedList<Vector2> impulseList = e.getBody().getImpulses();
            for(Iterator<Vector2> it = impulseList.iterator(); it.hasNext();) {
                e.getVelocity().add(it.next());
                it.remove();
            }

        }



        manageEntitySpeed();

        for(Entity e : worldContainer.getEntities()) {
            e.update(delta);
        }


        /**
         * TODO: FOG IMPLEMENTATION WITH THE OLD SKYBOXES
         */
        /*for (SkyBox s : level.getSkyBoxes()) {
            s.update(delta);
        }*/
        collisionController.calledPerFrame = 0;
    }



    private void manageEntitySpeed() {
        for (Entity entity : worldContainer.getEntities()) {
            if (entity.getAcceleration().x == 0) {
                entity.getVelocity().x *= entity.getBody().getFriction();

                if (Math.abs(entity.getVelocity().x) < MIN_WALKING_SPEED) {
                    entity.setVelocityX(0f);
                }
            }

            if (entity.getAcceleration().y == 0) {
                entity.getVelocity().y *= entity.getBody().getFriction();

                if (Math.abs(entity.getVelocity().y) < MIN_WALKING_SPEED) {
                    entity.setVelocityY(0f);
                }

            }

            if (entity.getVelocity().x > entity.getMaxVelocity()) {
                entity.setVelocityX(entity.getMaxVelocity());
            }
            if (entity.getVelocity().x < -entity.getMaxVelocity()) {
                entity.setVelocityX(-entity.getMaxVelocity());
            }
            if (entity.getVelocity().y > entity.getMaxVelocity()) {
                entity.setVelocityY(entity.getMaxVelocity());
            }

            if (entity.getVelocity().y < -entity.getMaxVelocity()) {
                entity.setVelocityY(-entity.getMaxVelocity());
            }

            // IF ENTITY FALLS OUT OF BOUNDS, IT IS PUT BACK TO THE START
            /*if (!randomLevelGenerator.checkBounds((int) entity.getPosition().x, (int) entity.getPosition().y)) {
                entity.setPosition(new Vector2(5f, 12f));
                entity.update(delta);
            }*/


        }

    }

    public void setPlayer() {
        collisionController.setPlayer();
    }
}
