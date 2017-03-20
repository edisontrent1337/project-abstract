package com.trent.awesomejumper.controller.entitymanagement;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.trent.awesomejumper.controller.levelgeneration.RandomLevelGenerator;
import com.trent.awesomejumper.engine.entity.Entity;
import com.trent.awesomejumper.engine.physics.Ray;
import com.trent.awesomejumper.game.AwesomeJumperMain;
import com.trent.awesomejumper.models.Player;
import com.trent.awesomejumper.models.pickups.Pickup;
import com.trent.awesomejumper.models.projectile.Projectile;
import com.trent.awesomejumper.models.weapons.Weapon;
import com.trent.awesomejumper.tiles.Tile;
import com.trent.awesomejumper.utils.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.trent.awesomejumper.engine.modelcomponents.ModelComponent.ComponentID.GRAPHICS;
import static com.trent.awesomejumper.utils.Utils.sub;

/**
 * Created by Sinthu on 12.06.2015.
 * Holds the player, the level with its environmental items such as random paths of dirt,
 * rocks, flowers etc. and also items, collectables and enemies.
 * Holds a list of all entities and manages all specific lists of entities needed by the controllers.
 * This class is managed by the {@link com.trent.awesomejumper.controller.entitymanagement.EntityManager}.
 */
public class WorldContainer {

    // MEMBERS & INSTANCES
    // ---------------------------------------------------------------------------------------------

    public static int renderNodes = 0;
    public static int registeredNodes = 0;

    private static final float NEIGHBOURHOOD_RANGE = 2.0f;

    /**
     * The following maps and sets manage all entities in the world.
     * Separate sets are needed for the collision detection to work properly, as there is a need
     * to differentiate between different types of entities.
     */

    private int SPATIAL_WIDTH = 0;
    private int SPATIAL_HEIGHT = 0;

    // Map containing all entities in the game. Maps entity IDs with entities.
    private HashMap<Integer, Entity> entities = new HashMap<>();

    // Map for spatial hashing
    //  private HashMap<Vector2, Set<Object>> spatialHashingData = new HashMap<>();
    private HashMap<Vector2, EntityTileContainer> spatialHashingData = new HashMap<>();

    private HashSet<Vector2> validHashIndexes = new HashSet<>();
    private final int SPATIAL_HASH_GRID_SIZE = 2;


    // Subset containing all projectiles
    private HashSet<Projectile> projectiles = new HashSet<>();

    private HashSet<Ray> rays = new HashSet<>();

    // Subset containing all pickups (ammo, medikits,...)
    private HashSet<Pickup> pickups = new HashSet<>();

    // Subset containing all mobile entities. These are entities that are able to move
    // and be affected by explosions
    private HashSet<Entity> mobileEntities = new HashSet<>();

    // Subset containing all dropped weapons the player can pick up.
    private HashSet<Weapon> weaponDrops = new HashSet<>();

    // Subset containing all living entities that can take damage
    private HashSet<Entity> livingEntities = new HashSet<>();   // subset of all entities that can take damage

    private Player player;

    private ArrayList<Tile> renderTileList = new ArrayList<>();
    private ArrayList<Entity> entitiesToBeDrawn = new ArrayList<>();
    private ArrayList<Tile> collisionTiles = new ArrayList<>();


    private RandomLevelGenerator randomLevelGenerator;


    // SPATIAL HASHING DEBUG

    private HashSet<Vector2> hitHashCells = new HashSet<>();

    private ArrayList<Vector2> penetrationPoints = new ArrayList<>();
    private ArrayList<Vector2> entityPenetrationPoints = new ArrayList<>();

    boolean draw = false;

    // CONSTRUCTOR
    // ---------------------------------------------------------------------------------------------

    public WorldContainer() {
        randomLevelGenerator = new RandomLevelGenerator();
        randomLevelGenerator.init();
        entities = randomLevelGenerator.load();
        player = randomLevelGenerator.getPlayer();
        initSpatialData();
    }


    // ---------------------------------------------------------------------------------------------
    // SPATIAL HASHING METHODS
    // ---------------------------------------------------------------------------------------------

    // DS MANAGEMENT
    // ---------------------------------------------------------------------------------------------

    /**
     * Clears the spatial hashing data structure.
     */
    private void clearSpatialHashData() {

        spatialHashingData.clear();
        for (int x = 0; x < SPATIAL_WIDTH; x++) {
            for (int y = 0; y < SPATIAL_HEIGHT; y++) {
                spatialHashingData.put(new Vector2(x * SPATIAL_HASH_GRID_SIZE, y * SPATIAL_HASH_GRID_SIZE), new EntityTileContainer());
            }
        }
    }

    /**
     * Adds an entity to the corresponding cell in the spatial hashing data structure.
     *
     * @param entity Entity to be added.
     */
    private void addEntityToSpatialHashingData(Entity entity) {
        // generating a list of all spatial indexes the current entity covers
        HashSet<Vector2> spatialIndexes = getSpatialIndexes(entity);
        for (Vector2 index : spatialIndexes) {
            if (spatialHashingData.containsKey(index)) {
                spatialHashingData.get(index).addEntity(entity);
            } else {
                Utils.log(String.format("ERROR ADDING THE FOLLOWING ENTITY TO THE LOCATION %1s" +
                        "IN THE SPATIAL HASHING DATA: %2s", index.toString(), entity.toString()));
            }
        }
    }

    /**
     * Adds a tile to the corresponding cell in the spatial hashing data structure.
     *
     * @param t Tile to be added.
     */
    private void addTileToSpatialHashingData(Tile t) {
        // generating a list of all spatial indexes the current tile covers
        HashSet<Vector2> spatialIndexes = getSpatialIndexes(t);
        for (Vector2 index : spatialIndexes) {
            if (spatialHashingData.containsKey(index)) {
                spatialHashingData.get(index).addTile(t);
            } else {
                Utils.log(String.format("ERROR ADDING THE FOLLOWING TILE TO THE LOCATION %1s" +
                        "IN THE SPATIAL HASHING DATA: %2s", index.toString(), t.toString()));
            }
        }
    }

    /**
     * Initializes the spatial data structure. Adds an empty EntityTileContainer to each cell.
     *
     * @link EntityTileContainer
     */
    private void initSpatialData() {

        SPATIAL_WIDTH = (randomLevelGenerator.getLevelWidth() / SPATIAL_HASH_GRID_SIZE) + 1;
        SPATIAL_HEIGHT = (randomLevelGenerator.getLevelHeight() / SPATIAL_HASH_GRID_SIZE) + 1;

        Utils.log("SPATIAL DIMENSIONS:  " + Integer.toString(SPATIAL_WIDTH) + " :   " + Integer.toString(SPATIAL_HEIGHT));

        /**
         * Generate keys at valid Vector2 positions.
         * Every multiple of SPATIAL_HASH_GRID_SIZE is a valid key in the spatial hashing data structure.
         */
        for (int x = 0; x < SPATIAL_WIDTH; x++) {
            for (int y = 0; y < SPATIAL_HEIGHT; y++) {
                spatialHashingData.put(new Vector2(x * SPATIAL_HASH_GRID_SIZE, y * SPATIAL_HASH_GRID_SIZE), new EntityTileContainer());
            }
        }


        // REGISTER ALL VALID HASH INDEXES
        validHashIndexes.addAll(spatialHashingData.keySet());


        // DEBUG
        Utils.log("LIST OF VALID SPATIAL INDEXES: \n");
        int i = 0;
        for (Vector2 v : spatialHashingData.keySet()) {
            Utils.log("VALID INDEX " + String.format("%05d", i), v.toString());
            i++;
        }

        /**
         * Add each entity/tile to the Vector2-keys they touch with their Vec2-position.
         */
        for (Entity e : entities.values()) {
            addEntityToSpatialHashingData(e);
        }


        for (Tile t : renderTileList) {
            addTileToSpatialHashingData(t);
        }

    }

    /**
     * Updates the spatial hashing ds. Iterates through all entities and tiles,
     * calculates their place in the hashing ds and adds them to their respective
     * collection / HashSet.
     * TODO: might be optimized by only considering entities that moved during the last frame.
     */
    public void updateSpatialHashingData() {
        clearSpatialHashData();

        for (Entity e : entities.values()) {
            addEntityToSpatialHashingData(e);
        }

        for (Tile t : randomLevelGenerator.getCollidableTiles()) {
            addTileToSpatialHashingData(t);
        }
    }

    /**
     * Gets a list of indexes for the spatial data structure. The list of indexes represent the
     * quadrants an entity is positioned in.
     * An entity can take up to 4 spatial indexes if its hitbox happens to overlap all of them.
     *
     * @param x x position of the entity / tile
     * @param y y position of the entity / tile
     * @param w width of the entity / tile
     * @param h height of the entity / tile
     * @return HashSet of Vector2 indexes
     */
    private HashSet<Vector2> getSpatialIndexes(float x, float y, float w, float h) {

        /*Utils.log("ENTITY/TILE Position ", Utils.printVec(x,y));
        Utils.log("ENTITY/TILE Width ", Float.toString(w));
        Utils.log("ENTITY/TILE Height ", Float.toString(h));*/

        int spatialX = (int) Math.floor(x);
        int spatialY = (int) Math.floor(y);
        int spatialWx = (int) Math.floor(x + w);
        int spatialHy = (int) Math.floor(y + h);

        /*Utils.log("SPATIAL LOWER X BOUND", Integer.toString(spatialX));
        Utils.log("SPATIAL LOWER Y BOUND", Integer.toString(spatialY));
        Utils.log("SPATIAL HIGHER X BOUND", Integer.toString(spatialWx));
        Utils.log("SPATIAL HIGHER Y BOUND", Integer.toString(spatialHy));*/

        spatialX -= spatialX % SPATIAL_HASH_GRID_SIZE;
        spatialY -= spatialY % SPATIAL_HASH_GRID_SIZE;
        spatialWx -= spatialWx % SPATIAL_HASH_GRID_SIZE;
        spatialHy -= spatialHy % SPATIAL_HASH_GRID_SIZE;

        HashSet<Vector2> result = new HashSet<>();

        result.add(new Vector2(spatialX, spatialY));
        result.add(new Vector2(spatialWx, spatialY));
        result.add(new Vector2(spatialX, spatialHy));
        result.add(new Vector2(spatialWx, spatialHy));


        /**
         * Check, if the generated indexes in the result set are valid.
         * Removes all invalid indexes from the result.
         */
        for(Iterator<Vector2> iterator = result.iterator(); iterator.hasNext();) {
            Vector2 index = iterator.next();
            if(!validHashIndexes.contains(index)) {
                iterator.remove();
            }
        }


        return result;
    }

    public HashSet<Vector2> getSpatialIndexes(Entity e) {
        /**
         * Needs to use the position and offset of the entities bounding hitbox to generate
         * correct results.
         */
        Vector2 positionAndOffset = e.getBody().getBounds().getPositionAndOffset().cpy();
        return getSpatialIndexes(positionAndOffset.x, positionAndOffset.y, e.getWidth(), e.getHeight());
    }


    /**
     * Returns a set containing one spatial hash cell index for a given tile t.
     * @param t tile
     * @return hash cell index of the tile.
     */
    public HashSet<Vector2> getSpatialIndexes(Tile t) {

        float x = t.getPosition().x;
        float y = t.getPosition().y;
        int spatialX = (int) Math.floor(x);
        int spatialY = (int) Math.floor(y);
        spatialX -= spatialX % SPATIAL_HASH_GRID_SIZE;
        spatialY -= spatialY % SPATIAL_HASH_GRID_SIZE;
        HashSet<Vector2> result = new HashSet<>();
        result.add(new Vector2(spatialX, spatialY));
        return result;

    }

    private Vector2 getSpatialIndex(float x, float y) {
        int spatialX = (int) Math.floor(x);
        int spatialY = (int) Math.floor(y);

        spatialX -= spatialX % SPATIAL_HASH_GRID_SIZE;
        spatialY -= spatialY % SPATIAL_HASH_GRID_SIZE;
        return new Vector2(spatialX, spatialY);
    }

    private Vector2 getSpatialIndex(Vector2 v) {
        return getSpatialIndex(v.x,v.y);
    }

    /**
     * Gathers all entities near a given entity e from the spatial hashing data structure.
     *
     * @param e entity whose neighbourhood we are interested in
     * @return set of nearby entities.
     */
    public HashSet<Entity> getEntitiesNearby(Entity e) {

        HashSet<Entity> result = new HashSet<>();
        for (Vector2 index : getSpatialIndexes(e)) {
            EntityTileContainer container = spatialHashingData.get(index);
            result.addAll(container.entities);
        }
        return result;
    }

    /**
     * Gathers all tiles near a given entity e from the spatial hashing data structure.
     *
     * @param e entity whose neighbourhood we are interested in
     * @return set of nearby tiles.
     */
    public HashSet<Tile> getTilesNearby(Entity e) {
        HashSet<Tile> result = new HashSet<>();
        for (Vector2 index : getSpatialIndexes(e)) {
            EntityTileContainer container = spatialHashingData.get(index);
            result.addAll(container.tiles);

        }
        return result;
    }

    public HashSet<Entity> getEntitiesForCell(Vector2 index) {

        if(!validHashIndexes.contains(index)) {

            throw new IllegalArgumentException("THE INDEX " + index + "IS INVALID " +
                    "THE FOLLOWING INDEXES ARE VALID:" + validHashIndexes.toString());

        }

        return spatialHashingData.get(index).getEntities();
    }

    public HashSet<Tile> getTilesForCell(Vector2 index) {

        if(!validHashIndexes.contains(index)) {

            throw new IllegalArgumentException("THE INDEX " + index + "IS INVALID " +
                    "THE FOLLOWING INDEXES ARE VALID:" + validHashIndexes.toString());

        }

        return spatialHashingData.get(index).getTiles();
    }


    /**
     * Returns all entities for a set of hash cells.
     * This method is used to
     *
     * @param hashIndexes set of hash cells.
     * @return set of entities.
     */
    private ArrayList<Entity> gatherEntitiesFromCells(ArrayList<Vector2> hashIndexes) {
        // Using a set to eliminate duplicates
        HashSet<Entity> result = new HashSet<>();
        for(Vector2 index : hashIndexes) {
            result.addAll(getEntitiesForCell(index));
        }
        return new ArrayList<>(result);
    }

    /**
     * Casts a ray through the hash cell structure, calculating all collisions and penetration points
     * that a given ray produces.
     * @param ray Ray to be processed
     */
    public void rayCast(final Ray ray) {
        generateCrossedIndexes(ray);
        ArrayList<Entity> entitiesFromCells = gatherEntitiesFromCells(ray.getHitHashCells());

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

        penetrateEntities(ray, entitiesFromCells);
        // get all hash cells the ray covers
        // get the final tile the ray touches
        // get all entities from the touched hash cells
        //
    }

    /**
     * Method that calculates damage caused by a ray sent through the scene.
     * @param ray ray that has to be processed
     * @param entities Set of entities previously calculated in
     * @link getHitHashCells()
     * TODO: DEBUG THIS METHOD.
     */
    private void penetrateEntities(Ray ray, ArrayList<Entity> entities) {
        HashSet<Ray> hitboxRays = new HashSet<>();
        entityPenetrationPoints.clear();

        for(Entity e : entities) {
            hitboxRays.clear();
            if(!e.getBody().isCollisionDetectionEnabled())
                continue;
            hitboxRays.addAll(e.getBody().getBounds().getRays());


            if(!hitboxRays.isEmpty()) {
                HashSet<Ray.Intersection> intersections = getIntersections(ray, hitboxRays);
                if(!intersections.isEmpty()) {
                    Ray.Intersection i = Collections.min(intersections);
                    Vector2 point = i.result.cpy();
                    entityPenetrationPoints.add(point);
                    ray.getPenetrations().add(point);
                    ray.getPenetratedEntities().put(e.getID(),point);
                }

                /*for(Ray.Intersection i : intersections) {

                }*/


            }
        }

        Utils.log("--------PENETRATED ENTITY LIST--------");
        for(Map.Entry<Integer,Vector2> entry : ray.getPenetratedEntities().entrySet()) {
            Utils.log("ID: " + entry.getKey() + " , POINT: " + entry.getValue());
        }

    }


    /**
     * Returns a list of passed hash cells from a starting point in a direction to the closest
     * wall. The list returned is in the order the ray passes through the hash cells.
     * This is important to later calculate damage induced by rays.
     * @return
     */
    public ArrayList<Vector2> generateCrossedIndexes(Ray ray) {

        Vector2 startCell = getSpatialIndex(ray.getOrigin());
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

        while (!foundWall && validHashIndexes.contains(currentCell)) {


            //List<Ray> rays = new ArrayList<>();
            HashSet<Ray> rays = new HashSet<>();
            List<Ray.Intersection> intersections = new ArrayList<>();
            // Choose the last penetration point as the starting point
            float lastPenetrationX = rayPenetrations.get(rayPenetrations.size() - 1).x;
            float lastPenetrationY = rayPenetrations.get(rayPenetrations.size() - 1).y;

            // Set the aim ray to start at the last penetration point

            ray = new Ray(lastPenetrationX, lastPenetrationY, deltaX, deltaY, Ray.INFINITE);

            Utils.log("-------CURRENT CELL--------", currentCell.toString());
            Utils.log("RAY START:", ray.toString());
            HashSet<Tile> tiles = getTilesForCell(currentCell);

            //TODO: add something like: if(aim.penetrationpower <= 0): break
            //TODO: if the ray has no punch left, break this loop early.

            rays.clear();


            // -------------------------------------------------------------------------------------
            // TILE / RAY COLLISION DETECTION
            // -------------------------------------------------------------------------------------

            Utils.log("CURRENT CELL", currentCell);
            Utils.log("START OF TILE RAY CASTING");
            Utils.log("TILES SIZE", tiles.size());

            for (Tile t : getTilesForCell(currentCell)) {
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
            Vector2 nextXCell = getSpatialIndex(currentCell.x + signX * SPATIAL_HASH_GRID_SIZE, currentCell.y);
            // length = 2 (spatial_hash_grid_size), dir = (0,1)
            Vector2 nextYCell = getSpatialIndex(currentCell.x, currentCell.y + signY * SPATIAL_HASH_GRID_SIZE);


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
                //}

                //TODO: reset aim is broken
                rays.clear();


                if (closestHashCellIntersection.origin == currentXAxis || closestHashCellIntersection.origin == nextYCellXAxis) {
                    if(!rayHashCells.contains(nextYCell) && validHashIndexes.contains(nextYCell)) {
                        rayHashCells.add(nextYCell);
                        currentCell = nextYCell;
                    }
                } else if (closestHashCellIntersection.origin == currentYAxis || closestHashCellIntersection.origin == nextXCellYAxis) {
                    if(!rayHashCells.contains(nextXCell) && validHashIndexes.contains(nextXCell)) {
                        rayHashCells.add(nextXCell);
                        currentCell = nextXCell;
                    }
                }


            }
            /*else {
                break;
            }*/
        }
        hitHashCells.addAll(rayHashCells);
        penetrationPoints.addAll(rayPenetrations);

        // restore origin of ray

        return rayHashCells;

    }

    public Ray.Intersection getClosestIntersection(Ray reference, HashSet<Ray>otherRays) {
        return Collections.min(getIntersections(reference, otherRays));
    }

    /**
     * Generates all intersection objects for a given reference ray against a set of othertest rays.
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

    /**
     * Registers all entities and puts them in their respective collection
     */
    public void initAllEntities() {
        for (Iterator<? extends Entity> it = entities.values().iterator(); it.hasNext(); ) {
            Entity e = it.next();
            e.register();
        }
    }


    // ---------------------------------------------------------------------------------------------
    // LIST OF TILES TO BE DRAWN
    // ---------------------------------------------------------------------------------------------

    /**
     * Returns an ordered list of entities currently in view.
     *
     * @param cameraPosition position of the game camera
     * @param camW           camera width
     * @param camH           camera height
     * @return list of entities
     */
    public ArrayList<Tile> getTilesToBeRendered(Vector2 cameraPosition, float camW, float camH) {

        // GET CURRENT FOV COORDINATES AND ONLY RENDER WHAT THE PLAYER SEES
        // BOTTOM MOVE_LEFT CORNER
        int fovStartX = (int) (cameraPosition.x - camW / 2 - 1);
        int fovStartY = (int) (cameraPosition.y - camH / 2 - 1);

        // TOP RIGHT CORNER
        int fovEndX = fovStartX + (int) camW + 4;
        int fovEndY = fovStartY + (int) camH + 4;

        // KEEP BOUNDS
        if (fovStartX < 0) fovStartX = 0;
        if (fovStartY < 0) fovStartY = 0;


        if (fovEndX > randomLevelGenerator.getLevelWidth())
            fovEndX = randomLevelGenerator.getLevelWidth();

        if (fovEndY > randomLevelGenerator.getLevelHeight())
            fovEndY = randomLevelGenerator.getLevelHeight();

        renderTileList = new ArrayList<>();
        Tile tile;

        for (int x = fovStartX; x < fovEndX; x++) {
            for (int y = fovStartY; y < fovEndY; y++) {

                // tile = level.getTile(x,y);
                tile = randomLevelGenerator.getTile(x, y);
                if (tile != null)
                    renderTileList.add(tile);

            }
        }

        return renderTileList;

    }



    // ---------------------------------------------------------------------------------------------
    // ORDERED LIST OF ENTITIES TO BE DRAWN
    // ---------------------------------------------------------------------------------------------

    /**
     * Returns only those entities visible to the players field of view to be rendered.
     * Orders the entities according to their y-position to render them properly on top of
     * each other.
     *
     * @param camW camera width
     * @param camH camera height
     * @return ArrayList with entities
     */
    public ArrayList<Entity> getEntitiesToBeRendered(Vector2 position, float camW, float camH) {
        int fovStartX = (int) (position.x - camW / 2);
        int fovStartY = (int) (position.y - camH / 2);

        // TOP RIGHT CORNER
        int fovEndX = fovStartX + (int) camW + 4;
        int fovEndY = fovStartY + (int) camH + 4;

        // KEEP BOUNDS
        if (fovStartX < 0) fovStartX = 0;
        if (fovStartY < 0) fovStartY = 0;

        if (fovEndX > randomLevelGenerator.getLevelWidth())
            fovEndX = randomLevelGenerator.getLevelWidth();

        if (fovEndY > randomLevelGenerator.getLevelHeight())
            fovEndY = randomLevelGenerator.getLevelHeight();

        for (Entity e : entities.values()) {
            if (e.getBody().getPosition().x > fovStartX &&
                    e.getBody().getPosition().x <= fovEndX &&
                    e.getBody().getPosition().y > fovStartY &&
                    e.getBody().getPosition().y <= fovEndY) {
                if (!entitiesToBeDrawn.contains(e) && e.has(GRAPHICS))
                    entitiesToBeDrawn.add(e);
            } else {
                entitiesToBeDrawn.remove(e);
            }
        }

        /**
         * Sort entitiesToBeDrawn by using a comparator that compares the y value of the
         * entities positions and then reversing the whole list.
         */
        Collections.sort(entitiesToBeDrawn, new Comparator<Entity>() {
            @Override
            public int compare(Entity e1, Entity e2) {
                return Float.compare(e1.getPosition().y, e2.getPosition().y);
            }
        });
        Collections.reverse(entitiesToBeDrawn);
        renderNodes = entitiesToBeDrawn.size();
        registeredNodes = entities.size();
        return entitiesToBeDrawn;
    }


    // ---------------------------------------------------------------------------------------------
    // ENTITY NEIGHBOURHOOD MANAGEMENT
    // ---------------------------------------------------------------------------------------------


    /**
     * Updates the list of entities close to the specified entity e.
     * The neighbourhood list describes the perimeter of radius 3 around the entity and is used by
     * the collision controller to solve entity/entity collision.
     *
     * @param e entity
     * @return modified HashSet with neighbours
     * @deprecated
     */

    public HashSet<Entity> updatedEntityNeighbourHood(Entity e) {

        HashSet<Entity> entityNeighbourhood = e.getBody().getEntityNeighbourHood();

        for (Entity other : mobileEntities) {
            if (other.equals(e))
                continue;


            float dst = sub(e.getBody().getCenter(), other.getBody().getCenter()).len(); // distance between entities
            if (dst <= NEIGHBOURHOOD_RANGE) {
                if (!entityNeighbourhood.contains(other) && other.getBody().isCollisionDetectionEnabled())
                    entityNeighbourhood.add(other);

                if (entityNeighbourhood.contains(other) && !other.getBody().isCollisionDetectionEnabled())
                    entityNeighbourhood.remove(other);

            } else {
                entityNeighbourhood.remove(other);
            }


        }
        return entityNeighbourhood;
    }


    // ---------------------------------------------------------------------------------------------
    // REMOVAL OF DEAD ENTITIES
    // ---------------------------------------------------------------------------------------------

    /**
     * Removes all entities which have their alive flag set to false from all relevant collections.
     * Modifies and cleans the global entity collection, the "toBeDrawn" subset and the
     * neighbourhood of each entity.
     */
    public void garbageRemoval() {


        /**
         * Remove dead entities from the main entity map.
         */
        for (Iterator<Map.Entry<Integer, Entity>> it = entities.entrySet().iterator(); it.hasNext(); ) {
            Entity e = it.next().getValue();
            if (!e.isAlive())
                it.remove();
        }

        /**
         * Remove dead entities from the entitiesToBeDrawn set.
         */
        for (Iterator<Entity> it = entitiesToBeDrawn.iterator(); it.hasNext(); ) {
            if (!it.next().isAlive()) {
                it.remove();
            }
        }

        /**
         * Remove dead entities from the entity neighbourhoods.
         */
        for (Entity e : entities.values()) {
            for (Iterator<Entity> it = e.getBody().getEntityNeighbourHood().iterator(); it.hasNext(); ) {
                if (!it.next().isAlive())
                    it.remove();

            }
        }


        /**
         * Remove dead projectiles.
         */
        for (Iterator<? extends Entity> it = projectiles.iterator(); it.hasNext(); ) {
            if (!it.next().isAlive()) {
                it.remove();
            }
        }


        for (Iterator<Weapon> it = weaponDrops.iterator(); it.hasNext(); ) {
            if (!it.next().isAlive())
                it.remove();
        }

        /**
         * Remove dead mobile entities.
         */
        for (Iterator<Entity> it = mobileEntities.iterator(); it.hasNext(); ) {
            if (!it.next().isAlive())
                it.remove();
        }


        for (Iterator<Entity> it = livingEntities.iterator(); it.hasNext(); ) {
            if (!it.next().isAlive())
                it.remove();
        }

        /**
         * Remove penetration points from ray casting
         */

    }

    public void garbageRemoval(Set<? extends Entity> entities) {
        for (Iterator<? extends Entity> it = entities.iterator(); it.hasNext(); ) {
            if (!it.next().isAlive()) {
                it.remove();
            }
        }
    }

    // ---------------------------------------------------------------------------------------------
    // REGISTER ENTITIES
    // ---------------------------------------------------------------------------------------------

    public void registerEntity(Entity entity) {

        entities.put(entity.getID(), entity);
        entity.registerTime = entity.time;
        if (AwesomeJumperMain.onDebugMode()) {
            Gdx.app.log("Registered entity at", Float.toString(entity.registerTime));
            Gdx.app.log("Entity registered", String.format("%04d", (entity.getID())));
        }
    }

    public void registerEntities(Set<Entity> entities) {
        for (Entity e : entities) {
            registerEntity(e);
        }
    }


    // ---------------------------------------------------------------------------------------------
    // PLACE ENTITY IN WORLD
    // ---------------------------------------------------------------------------------------------


    /**
     * Places an entity on the specified position in the world.
     *
     * @param entity   entity to be placed
     * @param position position where entity should be dropped.
     * @return
     */
    public boolean placeEntity(Entity entity, Vector2 position) {

        Vector2 destination = position.cpy();
        if (randomLevelGenerator.getTile(position).getType().equals(Tile.TileType.WALL)) {
            Utils.log("WEAPON OUT OF BOUNDS", entity.getPosition().toString());
            Utils.log("INSIDE TILE", randomLevelGenerator.getTile(position).getPosition().toString());
            Utils.log("NEIGHBOURHOOD SIZE", Float.toString(randomLevelGenerator.getTile(position).getNeighbourHood().size()));
            float minDst = Float.MAX_VALUE;
            for (Tile t : randomLevelGenerator.getTile(position).getNeighbourHood()) {
                Utils.log("NEIGHBOUR TILE", t.getPosition().toString());

                float dst;

                if (!entity.getOwner().equals(entity))
                    dst = position.dst(t.getPosition());
                else
                    dst = entity.getOwner().getPosition().dst(t.getPosition());

                if (dst < minDst) {
                    minDst = dst;
                    Utils.log("NEW MINIMAL DST", Float.toString(minDst));
                    Utils.log("NEW CENTER", t.getCollisionBox().getCenter().toString());
                    destination = t.getCollisionBox().getCenter().cpy();

                }

            }
            Utils.log(destination.toString());
        }
        entity.setPosition(destination);

        Utils.log(entity.getPosition().toString());

        return false;
    }


    //TODO: fix the reset feature
    public void reset() {
        entities.clear();
        player.destroy();
        randomLevelGenerator = new RandomLevelGenerator();
        randomLevelGenerator.init();
        randomLevelGenerator.load();
        // TODO: insert here: entities = randomLevelGenerator.getEntities()
        player = randomLevelGenerator.getPlayer();
        registerEntity(player);
    }

    // ---------------------------------------------------------------------------------------------
    // GETTER & SETTER
    // ---------------------------------------------------------------------------------------------

    public Player getPlayer() {
        return player;
    }

    public ArrayList<Tile> getCollisionTiles() {
        return collisionTiles;
    }

    public HashSet<Entity> getEntities() {
        return new HashSet<>(entities.values());
    }

    public HashMap<Integer, Entity> getEntityMap() {
        return entities;
    }

    public HashSet<Weapon> getWeaponDrops() {
        return weaponDrops;
    }

    public HashSet<Pickup> getPickups() {
        return pickups;
    }

    public HashSet<Entity> getMobileEntities() {
        return mobileEntities;
    }

    public HashSet<Entity> getLivingEntities() {
        return livingEntities;
    }

    public HashSet<Projectile> getProjectiles() {
        return projectiles;
    }

    public RandomLevelGenerator getRandomLevelGenerator() {
        return randomLevelGenerator;
    }


    public HashSet<Ray> getRays() {
        return rays;
    }


    public Entity getEntityByID(int id) {
        Entity e = entities.get(id);
        if (e == null) {
            Gdx.app.log("ERROR", "THE REQUESTED ENTITY WAS NOT FOUND. ENTITY ID: " + Integer.toString(id));
        }
        return entities.get(id);
    }

    // GETTER FOR SPATIAL DIMENSIONS
    public int getSpatialWidth() {
        return SPATIAL_WIDTH;
    }

    public int getSpatialHeight() {
        return SPATIAL_HEIGHT;
    }

    public int getSpatialFactor() {
        return SPATIAL_HASH_GRID_SIZE;
    }

    public HashMap<Vector2, EntityTileContainer> getSpatialHashingData() {
        return spatialHashingData;
    }

    public int getHashCellSize(Vector2 index) {
        return spatialHashingData.get(index).getEntities().size() +
                spatialHashingData.get(index).getTiles().size();
    }



    public HashSet<Vector2> getHitHashCells() {

        return hitHashCells;

    }

    public ArrayList<Vector2> getPenetrationPoints() {
        return penetrationPoints;
    }

    public ArrayList<Vector2> getEntityPenetrationPoints() {
        return entityPenetrationPoints;
    }


    /**
     * Inner class that manages entities and tiles for a given hashcell.
     * Each hash cell contains an EntityTileContainer.
     */
    private class EntityTileContainer {

        private HashSet<Tile> tiles;
        private HashSet<Entity> entities;

        EntityTileContainer() {
            this.tiles = new HashSet<>();
            this.entities = new HashSet<>();
        }

        void addEntity(Entity e) {
            entities.add(e);
        }

        void addTile(Tile t) {
            tiles.add(t);
        }

        HashSet<Entity> getEntities() {
            return entities;
        }

        HashSet<Tile> getTiles() {
            return tiles;
        }


    }


}


