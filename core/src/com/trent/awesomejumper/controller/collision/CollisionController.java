package com.trent.awesomejumper.controller.collision;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.trent.awesomejumper.controller.entitymanagement.WorldContainer;
import com.trent.awesomejumper.controller.rendering.PopUpRenderer;
import com.trent.awesomejumper.engine.entity.Entity;
import com.trent.awesomejumper.engine.modelcomponents.popups.Message;
import com.trent.awesomejumper.engine.physics.CollisionBox;
import com.trent.awesomejumper.engine.physics.ProjectileRay;
import com.trent.awesomejumper.engine.physics.Ray;
import com.trent.awesomejumper.models.Player;
import com.trent.awesomejumper.models.projectile.Projectile;
import com.trent.awesomejumper.models.weapons.Weapon;
import com.trent.awesomejumper.tiles.Tile;
import com.trent.awesomejumper.utils.Interval;
import com.trent.awesomejumper.utils.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.trent.awesomejumper.controller.entitymanagement.WorldContainer.SPATIAL_HASH_GRID_SIZE;
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
        float minOverlap = Float.MAX_VALUE;

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
        Utils.log("FORCE", force);

        Vector2 impulse = createReflectionImpulse(deltaVelocity, collisionNormal, entity.getBody().getElasticity());
        impulse.scl(force * force * 1.5f);


        /**
         * If the number of frames until the impact occurs is between 0 and 1, the collision
         * can happen in the next frame, so it has to be resolved by checkCollision
         */
        if (framesToImpact > 0 && framesToImpact < 1) {
            float step = 0.05f;
            float numberOfSteps = dst / step;
            Utils.log("NUMBER OF STEPS", Float.toString(numberOfSteps));
            for (float i = 0f; i <= numberOfSteps; i+=step) {
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
            for (float i = 0f; i <= numberOfSteps; i+=step) {
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

    // ---------------------------------------------------------------------------------------------
    // PROJECTILE / RAY CASTING
    // ---------------------------------------------------------------------------------------------


    /**
     * Casts a projectile ray through the scene in three steps:
     * 1) Gather an ordered list of hash cells the ray hits.
     * 2) Gather all entities from those hash cells and sort them by distance from the rays origin
     * 3) Execute penetration tests against the list of entities gathered in step 2.
     * @param ray
     */
    public void projectileRayCast(final ProjectileRay ray) {
        ArrayList<Vector2> spatialIndexes = generateCrossedIndexes(ray);
        ArrayList<Entity> entitiesFromCells = worldContainer.gatherEntitiesFromCells(spatialIndexes);


        // Sort all entities in order of distance from the rays origin
        Collections.sort(entitiesFromCells, new Comparator<Entity>() {
            @Override
            public int compare(Entity a, Entity b) {
                float dst1 = a.getBody().getBounds().getCenter().dst(ray.getOrigin());
                float dst2 = b.getBody().getBounds().getCenter().dst(ray.getOrigin());

                if(dst1 > dst2)
                    return 1;
                if(dst1 < dst2)
                    return -1;
                else
                    return 0;
            }
        });

        Utils.log("RAY START: ", ray.getOrigin());
        Utils.log("------HIT HASH CELLS------");
        for(Vector2 i : ray.getHitHashCells()) {
            Utils.log("INDEX: ", i);
        }

        Utils.log("-------ENTITIES SORTED BY DISTANCE------");
        for(Entity e: entitiesFromCells) {
            Utils.log("ENTITY: (DST =" + e.getBody().getBounds().getCenter().dst(ray.getOrigin())
                    + ")", e.toString());
        }

        penetrateEntities(ray,entitiesFromCells);
    }


    /**
     * Returns a list of passed hash cells from a starting point in a direction to the closest
     * wall. The list returned is in the order the ray passes through the hash cells.
     * This is important to later calculate damage induced by rays.
     * @return
     */
    public ArrayList<Vector2> generateCrossedIndexes(Ray ray) {

        Vector2 startCell = worldContainer.getSpatialIndex(ray.getOrigin());
        Vector2 currentCell = startCell;

        ArrayList<Vector2> rayHashCells = ray.getHitHashCells();
        ArrayList<Vector2> rayPenetrations = ray.getPenetrations();

        float deltaX = ray.getDir().x;
        float deltaY = ray.getDir().y;

        // Leading sign of the ray direction. Used to find next adjacent hashing cell
        int signX = deltaX > 0 ? 1 : -1;
        int signY = deltaY > 0 ? 1 : -1;

        rayHashCells.add(startCell);
        rayPenetrations.add(ray.getOrigin());

        boolean foundWall = false;

        /**
         * As long as no solid world tiles like walls have been found,
         * the ray continues to travel.
         */
        Utils.log("START OF RAYCASTING!");

        while (!foundWall && worldContainer.isValid(currentCell)) {

            HashSet<Ray> rays = new HashSet<>();
            List<Ray.Intersection> intersections = new ArrayList<>();
            // Choose the last penetration point as the starting point
            float lastPenetrationX = rayPenetrations.get(rayPenetrations.size() - 1).x;
            float lastPenetrationY = rayPenetrations.get(rayPenetrations.size() - 1).y;

            // Set the aim ray to start at the last penetration point

            ray = new Ray(lastPenetrationX, lastPenetrationY, deltaX, deltaY, Ray.INFINITE);

            Utils.log("-------CURRENT CELL--------", currentCell.toString());
            Utils.log("RAY START:", ray.toString());
            HashSet<Tile> tiles = worldContainer.getTilesForCell(currentCell);

            //TODO: add something like: if(aim.penetrationpower <= 0): break
            //TODO: if the ray has no punch left, break this loop early.

            rays.clear();


            // -------------------------------------------------------------------------------------
            // TILE / RAY COLLISION DETECTION
            // -------------------------------------------------------------------------------------

            Utils.log("CURRENT CELL", currentCell);
            Utils.log("START OF TILE RAY CASTING");
            Utils.log("TILES SIZE", tiles.size());

            for (Tile t : tiles) {
                Utils.log("", "-------------TILE------------:" + t.toString());
                rays.addAll(t.getCollisionBox().getRays());
            }

            Utils.log("RAYS SIZE (SHOULD BE MULTIPLE OF 4)", rays.size());

            if (rays.size() > 0) {
                HashSet<Ray.Intersection> tileIntersections = getIntersections(ray, rays);
                Utils.log("GENERATED INTERSECTIONS", tileIntersections.toString());
                if (tileIntersections.size() > 0) {
                    Ray.Intersection closestIntersection = Collections.min(tileIntersections);
                    Utils.log("CLOSEST INTERSECTION FOR TILE", closestIntersection.toString());
                    rayPenetrations.add(closestIntersection.result);
                    break;
                }

            }
            else {
                Utils.log("NO TILE INTERSECTION FOR: " + currentCell);
            }

            Vector2 lastPen = rayPenetrations.get(rayPenetrations.size()-1);
            ray = new Ray(lastPen.x,lastPen.y, deltaX, deltaY, Ray.INFINITE);
            rays.clear();


            // -------------------------------------------------------------------------------------
            // HASH CELL / RAY COLLISION DETECTION
            // -------------------------------------------------------------------------------------


            // Get the indices for the next hashing cells adjacent to the current one with regards
            // to the ray direction.
            // length = 2 (spatial_hash_grid_size), dir = (1,0)
            Vector2 nextXCell = worldContainer.getSpatialIndex(currentCell.x + signX * SPATIAL_HASH_GRID_SIZE, currentCell.y);
            // length = 2 (spatial_hash_grid_size), dir = (0,1)
            Vector2 nextYCell = worldContainer.getSpatialIndex(currentCell.x, currentCell.y + signY * SPATIAL_HASH_GRID_SIZE);


            Utils.log("-----------HASH CELL CD START----------");
            Utils.log("NEXT X CELL: ", nextXCell);
            Utils.log("NEXT Y CELL: ", nextYCell);

            /*
             * Creating rays from the current hash cell:
             * One for the x axis of the current cell.
             * One for the y axis of the current cell.
             * One for the x axis of the next cell in y direction.
             * One for the y axis of the next cell in x direction.
             */
            Ray currentXAxis = new Ray(currentCell.x, currentCell.y, 1, 0, Ray.INFINITE);
            Ray currentYAxis = new Ray(currentCell.x, currentCell.y, 0, 1, Ray.INFINITE);

            Ray nextYCellXAxis = new Ray(nextYCell.x, nextYCell.y, 1, 0, Ray.INFINITE);
            Ray nextXCellYAxis = new Ray(nextXCell.x, nextXCell.y, 0, 1, Ray.INFINITE);


            /*
             * Add all of these rays to the ray list.
             */
            rays.add(currentXAxis);
            rays.add(currentYAxis);
            rays.add(nextYCellXAxis);
            rays.add(nextXCellYAxis);


            Utils.log("NUMBER OF NEIGHBOUR HASH CELL RAYS (SHOULD BE 4)", rays.size());

            for(Ray r : rays) {
                Utils.log("RAY FOR HASH CD: ", r.toString());
            }

            /*
             * Calculation of the intersection for all surrounding hash cell rays with the aim ray.
             */
            for (Ray r : rays) {
                Ray.Intersection inter = ray.getIntersection(r);
                // We do not want to collide with the hash cell itself, so the distance has to be > 0
                if (inter.intersect && inter.distance > 0)
                    intersections.add(inter);
                Utils.log("-------NEXT RAY--------" + "\n");
            }


            if(intersections.size() > 0) {
                Utils.log("THERE WERE INTERSECTIONS WITH THE PROPOSED HASH CELLS");
                Ray.Intersection closestHashCellIntersection = Collections.min(intersections);
                Vector2 penetrationPoint = closestHashCellIntersection.result;
                rayPenetrations.add(penetrationPoint);

                //TODO: reset aim is broken
                rays.clear();


                if (closestHashCellIntersection.origin == currentXAxis || closestHashCellIntersection.origin == nextYCellXAxis) {
                    if(!rayHashCells.contains(nextYCell) && worldContainer.isValid(nextYCell)) {
                        rayHashCells.add(nextYCell);
                        currentCell = nextYCell;
                    }
                } else if (closestHashCellIntersection.origin == currentYAxis || closestHashCellIntersection.origin == nextXCellYAxis) {
                    if(!rayHashCells.contains(nextXCell) && worldContainer.isValid(nextXCell)) {
                        rayHashCells.add(nextXCell);
                        currentCell = nextXCell;
                    }
                }


            }
        }
        /*
        hitHashCells.addAll(rayHashCells);
        penetrationPoints.addAll(rayPenetrations);*/

        return rayHashCells;
    }



    /**
     * Responsible for ray penetration of entities in the scene. Calculates for a given set of
     * entities penetration points and, if applicable, triggers damage dealing.
     * @param ray ray that has to be processed
     * @param entities Set of entities previously calculated in
     * @link getHitHashCells()
     */
    private void penetrateEntities(ProjectileRay ray, ArrayList<Entity> entities) {

        for(Entity e : entities) {

            HashSet<Ray> hitboxRays = new HashSet<>();
            // If the current entity does not support collision, skip it.
            if(!e.getBody().isCollisionDetectionEnabled())
                continue;
            // Gather all rays from the hitbox of the entity
            hitboxRays.addAll(e.getBounds().getRays());

            // If there are hit box rays present, calculate intersections.
            if(!hitboxRays.isEmpty()) {
                HashSet<Ray.Intersection> intersections = getIntersections(ray, hitboxRays);
                if(!intersections.isEmpty()) {
                    // Get the minimum intersection and add the penetration point to all relevant
                    // collections.
                    Ray.Intersection i = Collections.min(intersections);
                    Vector2 point = i.result.cpy();
                    ray.getPenetrations().add(point);
                    ray.getPenetratedEntities().put(e.getID(),point);
                    if(e.has(HEALTH) && ray.getRemainingPower() > 0) {
                        int damage = ray.dealDamage(e.getBounds(), e.getBody());
                        if(damage > 0)
                            e.getHealth().takeDamage(damage);
                    }
                }

            }
        }

        Utils.log("--------PENETRATED ENTITY LIST--------");
        for(Map.Entry<Integer,Vector2> entry : ray.getPenetratedEntities().entrySet()) {
            Utils.log("ID: " + entry.getKey() + " , POINT: " + entry.getValue());
        }

        ray.destroy();
    }


    /**
     * Generates all intersection objects for a given reference ray against a set of other test rays.
     * Can be used to get the intersections of an aim ray against the rays of a collision box.
     * @param otherRays Set containing rays.
     * @param reference Reference ray that should collide with the set of other rays.
     * @return
     */

    public HashSet<Ray.Intersection> getIntersections(Ray reference, HashSet<Ray>otherRays) {

        HashSet<Ray.Intersection> intersections = new HashSet<>();
        for(Ray other : otherRays) {
            Ray.Intersection i = reference.getIntersection(other);
            // If the intersection object says, that an intersecton occurs, the current intersection
            // is added to the result set.
            if(i.intersect)
                intersections.add(i);
        }
        return intersections;
    }

    //TODO: change this.
    public void setPlayer() {
        this.player = worldContainer.getPlayer();
    }


}
