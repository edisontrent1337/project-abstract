package com.trent.awesomejumper.controller;

import com.badlogic.gdx.math.Vector2;
import com.trent.awesomejumper.controller.collision.CollisionController;
import com.trent.awesomejumper.engine.entity.Entity;
import com.trent.awesomejumper.engine.physics.ProjectileRay;
import com.trent.awesomejumper.engine.physics.Ray;
import com.trent.awesomejumper.models.projectile.Projectile;
import com.trent.awesomejumper.utils.Utils;

import java.util.Iterator;
import java.util.LinkedList;

import static com.trent.awesomejumper.utils.PhysicalConstants.MIN_WALKING_SPEED;

/**
 * Created by Sinthu on 12.06.2015.
 */
public class WorldController {


    // MEMBERS & INSTANCES
    // ---------------------------------------------------------------------------------------------

    private com.trent.awesomejumper.controller.entitymanagement.WorldContainer worldContainer;
    private CollisionController collisionController;

    public static float worldTime = 0f;


    // CONSTRUCTOR
    // ---------------------------------------------------------------------------------------------

    public WorldController(com.trent.awesomejumper.controller.entitymanagement.WorldContainer worldContainer) {
        this.worldContainer = worldContainer;
        this.collisionController = new CollisionController(worldContainer);
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
     *
     * @param delta time between frames
     */
    public void update(float delta) {


        worldTime += delta;

        for (Entity e : worldContainer.getEntities()) {
            e.getAcceleration().scl(delta);
            e.getVelocity().add(e.getAcceleration());
        }


        /**
         * Resolve entity/world collisions.
         * Affected entities: all.
         */
        for (Entity e : worldContainer.getEntities()) {
            if (!e.isAlive() || !e.getBody().isCollisionDetectionEnabled())
                continue;
            collisionController.resolveWorldCollisions(e, delta);
        }


        /**
         * Resolve entity/entity collisions.
         * Affected entities: only those who can move.
         */
        for (Entity e : worldContainer.getMobileEntities()) {
            if (!e.isAlive() || !e.getBody().isCollisionDetectionEnabled())
                continue;
            collisionController.resolveEntityCollisions(e, worldContainer.getEntitiesNearby(e), delta);
        }

        /**
         * Resolve entity/projectile collisions.
         * Here, only living entities that can take damage are considered.
         * TODO: not projectiles, but penetration points need to be addressed here.
         */

        for (Entity e : worldContainer.getLivingEntities()) {
            for (Projectile p : worldContainer.getProjectiles())
                collisionController.projectileCollisionDetection(e, p, delta);
        }

        for(ProjectileRay r : worldContainer.getProjectileRays()) {
            collisionController.projectileRayCast(r);
        }
        
        worldContainer.garbageRemoval();
        worldContainer.updateSpatialHashingData();
        addImpulses();
        manageEntitySpeed();

        for (Entity e : worldContainer.getEntities()) {
            e.update(delta);
        }


        /**
         * TODO: FOG IMPLEMENTATION WITH THE OLD SKYBOXES
         */
        /*for (SkyBox s : level.getSkyBoxes()) {
            s.update(delta);
        }*/

    }


    private void addImpulses() {
        for (Entity e : worldContainer.getEntities()) {
            LinkedList<Vector2> impulseList = e.getBody().getImpulses();
            for (Iterator<Vector2> it = impulseList.iterator(); it.hasNext(); ) {
                e.getVelocity().add(it.next());
                it.remove();
            }

        }
    }


    /**
     * Caps all entities movement speed at their maximum value.
     * TODO: TIE MIN/MAX SPEED ON ENTITIES
     */
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



        }

    }

    public void setPlayer() {
        collisionController.setPlayer();
    }
}
