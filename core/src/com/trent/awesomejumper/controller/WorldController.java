package com.trent.awesomejumper.controller;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.trent.awesomejumper.models.Entity.State;
import com.trent.awesomejumper.models.Level;
import com.trent.awesomejumper.models.Player;
import com.trent.awesomejumper.models.SkyBox;
import com.trent.awesomejumper.models.WorldContainer;
import com.trent.awesomejumper.testing.CollisionBox;
import com.trent.awesomejumper.tiles.Tile;
import com.trent.awesomejumper.utils.Utilites;

import java.util.HashMap;
import java.util.Map;

import static com.trent.awesomejumper.utils.PhysicalConstants.*;
import static com.trent.awesomejumper.utils.Utilites.dPro;
import static com.trent.awesomejumper.utils.Utilites.formVec;
import static com.trent.awesomejumper.utils.Utilites.getOverlap;
import static com.trent.awesomejumper.utils.Utilites.getProjection;
import static com.trent.awesomejumper.utils.Utilites.overlaps;

/**
 * Created by Sinthu on 12.06.2015.
 */
public class WorldController {

    enum Keys {
        UP, DOWN, LEFT, RIGHT, D
    }

    // MEMBERS & INSTANCES
    // ---------------------------------------------------------------------------------------------

    private WorldContainer worldContainer;
    private Player player;
    private Level level;
    private SkyBox farSky01, farSky02, nearSky01, nearSky02;
    private Array<Tile> collisionCandidateTiles = new Array<>();

    // JUMPING AND TIMING
    private long pressedJumpTime;
    private boolean pressedJump = false;
    private boolean grounded = false;

    // MAXIMUM VELOCITY  & DAMPING DETERMINED BY TILE
    public float MAX_VELOCITY, DAMPING;

    // WEATHER SIMULATION
    private float windSpeed, windDelta;

    //KEY MAP
    static Map<Keys, Boolean> keyMap = new HashMap<>();

    static {
        keyMap.put(Keys.LEFT, false);
        keyMap.put(Keys.DOWN, false);
        keyMap.put(Keys.RIGHT, false);
        keyMap.put(Keys.UP, false);
        keyMap.put(Keys.D, false);
    }


    // CONSTRUCTOR
    // ---------------------------------------------------------------------------------------------

    public WorldController(WorldContainer worldContainer) {
        this.worldContainer = worldContainer;
        this.player = worldContainer.getPlayer();
        this.level = worldContainer.getLevel();
        this.farSky01 = level.getSkyBoxes().get(0);
        this.farSky02 = level.getSkyBoxes().get(1);
        this.nearSky01 = level.getSkyBoxes().get(2);
        this.nearSky02 = level.getSkyBoxes().get(3);
    }

    // BUTTON PRESSING METHODS
    // ---------------------------------------------------------------------------------------------

    public void leftPressed() {
        keyMap.put(Keys.LEFT, true);
    }

    public void upPressed() {
        keyMap.put(Keys.UP, true);
    }

    public void rightPressed() {
        keyMap.put(Keys.RIGHT, true);
    }

    public void downPressed() {
        keyMap.put(Keys.DOWN, true);
    }

    public void activateDebugMode() {
        keyMap.put(Keys.D, true);
    }

    public void leftReleased() {
        keyMap.put(Keys.LEFT, false);
    }

    public void upReleased() {
        keyMap.put(Keys.UP, false);
    }

    public void rightReleased() {
        keyMap.put(Keys.RIGHT, false);
    }

    public void downReleased() {
        keyMap.put(Keys.DOWN, false);
    }

    public void deactivateDebugMode() {
        keyMap.put(Keys.D, false);
    }

    // UPDATE FUNCTION: INPUT PROCRESSING & COLLISION DETECTION
    // ---------------------------------------------------------------------------------------------

    public void update(float delta) {

        // INPUT PROCCESSING
        processUserInput();

        if (player.isOnGround() && player.getState().equals(State.JUMPING)) {
            player.setState(State.IDLE);
        }

        if (player.getVelocity().y < 0 && !player.isOnGround() && !player.getState().equals(State.IDLE)) {
            player.setState(State.FALLING);
        }
        if (player.getVelocity().y > 0) {
            player.elevateEntity();
        }

        player.setAccelY(GRAVITY);
        player.getAcceleration().scl(delta);
        player.getVelocity().add(player.getAcceleration());
        collisionDetection(delta);
        managePlayerSpeed();
        //float test = dPro(new Vector2(0,1), new Vector2(34.45f, 192.63f));
        //Gdx.app.log("DOT PRODUCT TEST", Float.toString(test));


        /*CollisionBox box1 = new CollisionBox(new Vector2(0, 0), 2, 2);
        CollisionBox box2 = new CollisionBox(new Vector2(3, 0), 2, 2);

        Vector2 test = getProjection(box1, box1.getNormals().get(2));
        Vector2 test2 = getProjection(box2, box1.getNormals().get(2));

        Gdx.app.log("PROJECTION1", test.toString());
        Gdx.app.log("PROJECTION2", test2.toString());

        Gdx.app.log("CHECK OVERLAP", Boolean.toString(overlaps(test2, test)));*/

        player.update(delta);
        player.setBounds(player.getPositionX(), player.getPositionY());
        // player.setHitboxes(player.getPosition());

        for (SkyBox s : level.getSkyBoxes()) {
            s.update(delta);
        }

    }


    // PROCESS USER INPUT
    // ---------------------------------------------------------------------------------------------

    private void processUserInput() {

        // JUMPING
        if (keyMap.get(Keys.UP)) {
            if (!player.getState().equals(State.JUMPING) && player.isOnGround()) {
                player.setState(State.JUMPING);
                player.elevateEntity();
                pressedJump = true;
                pressedJumpTime = System.currentTimeMillis();
                player.setVelocityY(MAX_JUMPING_VELOCITY);
            } else {
                if (pressedJump && System.currentTimeMillis() - pressedJumpTime >= JUMP_THRESHOLD) {
                    pressedJump = false;
                } else if (pressedJump) {
                    player.setVelocityY(MAX_JUMPING_VELOCITY);
                }
            }

        }

        // WALKING
        // RIGHT

        if (keyMap.get(Keys.RIGHT) && !keyMap.get(Keys.LEFT)) {
            player.setFacingL(false);
            if (!player.getState().equals(State.JUMPING)) {
                player.setState(State.WALKING);
            }
            player.setAccelX(ACCELERATION);
        }

        // LEFT

        else if (keyMap.get(Keys.LEFT) && !keyMap.get(Keys.RIGHT)) {
            player.setFacingL(true);
            if (!player.getState().equals(State.JUMPING)) {
                player.setState(State.WALKING);
            }
            player.setAccelX(-ACCELERATION);

        }

        // IDLE

      /*  else {
            if (!player.getState().equals(State.JUMPING)) {
                player.setState(State.IDLE);
            }
            player.setAccelX(0f);
        }*/

    }

    // COLLISION DETECTION
    // ---------------------------------------------------------------------------------------------

    private void collisionDetection(float delta) {

        delta *= 100f;

        player.getVelocity().scl(delta);

        // -----------------------------------------------------------------------------------------
        // HORIZONTAL COLLISION DETECTION
        // -----------------------------------------------------------------------------------------

        int cdStartX, cdEndX;

        // Y AXIS INTERVAL
        int cdStartY = (int) (player.getBounds().y);
        int cdEndY = (int) (player.getBounds().y + player.getBounds().height);

        // X AXIS INTERVAL DEPENDS ON PLAYERS MOVEMENT DIRECTION
        if (player.getVelocity().x < 0) {
            cdStartX = cdEndX = (int) (Math.floor(player.getBounds().x + player.getVelocity().x));
        } else {
            cdStartX = cdEndX = (int) (Math.floor(player.getBounds().x + player.getBounds().width
                    + player.getVelocity().x));
        }

        // FIND ALL TILES THE PLAYER CAN COLLIDE WITH
        findCollisionCandidates(cdStartX, cdStartY, cdEndX, cdEndY);


        for (Tile tile : collisionCandidateTiles) {
            if (tile == null) {
                continue;
            }
            Gdx.app.log("ENTERING COLLISION DETECTION", tile.getPosition().toString());
            // ALL HITBOXES ARE SENT FORWARD AND IF ANY OVERLAP WITH TILES, VELOCITY IS SET TO 0

            // float ahead = player.getVelocity().x;

            for (CollisionBox collisionBox : player.getBody()) {


               /* float minOverlapMagnitude = 10000f;
                Vector2 resolutionAxis = null;

                // GET NORMALS OF EACH SHAPE
                Array<Vector2> normalsA = collisionBox.getNormals();
                Array<Vector2> normalsB = tile.getCollisionBox().getNormals();

                // CALCULATE FOR EVERY NORMAL AND BOTH SHAPES THEIR PROJECTION
                // NORMALS OF SHAPE A
                for (int i = 0; i < normalsA.size; i++) {
                    Vector2 projectionA = getProjection(collisionBox, normalsA.get(i));
                    Vector2 projectionB = getProjection(tile.getCollisionBox(), normalsA.get(i));

                    if (!overlaps(projectionA, projectionB)) {
                        Gdx.app.log("EVENT", "RETURN X");
                        break;
                    } else {
                        float overlap = getOverlap(projectionA, projectionB);

                        if (overlap < minOverlapMagnitude) {
                            minOverlapMagnitude = overlap;
                            resolutionAxis = normalsA.get(i);
                        }
                    }

                }
                Gdx.app.log("COLLISON DETECTED X AXIS SHAPE A", Float.toString(minOverlapMagnitude) + resolutionAxis.toString());
                // NORMALS OF SHAPE B
                for (int i = 0; i < normalsB.size; i++) {
                    Vector2 projectionA = getProjection(collisionBox, normalsB.get(i));
                    Vector2 projectionB = getProjection(tile.getCollisionBox(), normalsB.get(i));

                    if (!overlaps(projectionA, projectionB)) {
                        Gdx.app.log("EVENT", "RETURN X");
                        break;
                    } else {
                        float overlap = getOverlap(projectionA, projectionB);

                        if (overlap < minOverlapMagnitude) {
                            minOverlapMagnitude = overlap;
                            resolutionAxis = normalsB.get(i);
                        }
                    }

                }
                        Gdx.app.log("COLLISON DETECTED X AXIS SHAPE B", Float.toString(minOverlapMagnitude) + resolutionAxis.toString());


                //APPLY FORCE TO SEPARATE PLAYER AND WORLD
                // RESOLUTION AXIS * MAGNITUDE
*/

                Vector3 resolutionAndMagnitude = checkCollision(tile, collisionBox);


                if(resolutionAndMagnitude != null && !tile.isPassable()) {
                    Gdx.app.log("RESOLUTION AND MAGANITUDE", resolutionAndMagnitude.toString());
                    Vector2 resolutionDir = new Vector2(resolutionAndMagnitude.x, resolutionAndMagnitude.y);
                    float magnitude = resolutionAndMagnitude.z;
                    player.getVelocity().add(resolutionDir.cpy().scl(magnitude).scl(delta));
                }
               /* collisionBox.x += ahead;
                if (Intersector.overlaps(collisionBox, tile.getBounds())) {
                    if (!tile.isPassable()) {
                        player.setVelocityX(0f);
                        worldContainer.getCollisionRectangles().add(tile.getBounds());
                        worldContainer.getCollisionRectangles().add(collisionBox);
                    }
                    player.setHitboxes(player.getPosition());
                    break;
                }
                */
            }
        }

        // RESET HITBOXES
        // player.setHitboxes(player.getPosition());
        // player.setBounds(player.getPositionX(), player.getPositionY());


        // -----------------------------------------------------------------------------------------
        // VERTICAL COLLISION DETECTION
        // -----------------------------------------------------------------------------------------

        cdStartX = (int) player.getBounds().x;
        cdEndX = (int) (player.getBounds().x + player.getBounds().width);
        if (player.getVelocity().y < 0) {
            cdStartY = cdEndY = (int) (Math.floor(player.getBounds().y + player.getVelocity().y));
        } else {
            cdStartY = cdEndY = (int) (Math.floor(player.getBounds().y + player.getBounds().height + player.getVelocity().y));
        }

        findCollisionCandidates(cdStartX, cdStartY, cdEndX, cdEndY);

        for (Tile tile : collisionCandidateTiles) {
            if (tile == null) {
                continue;
            }

            Gdx.app.log("ENTERING COLLISION DETECTION", tile.getPosition().toString());


            // float ahead = player.getVelocity().y;


            for (CollisionBox collisionBox : player.getBody()) {



                // HERE!!!!
                Vector3 resolutionAndMagnitude = checkCollision(tile, collisionBox);


              /*  float minOverlapMagnitude = 10000f;
                Vector2 resolutionAxis = null;

                // GET NORMALS OF EACH SHAPE
                Array<Vector2> normalsA = collisionBox.getNormals();
                Array<Vector2> normalsB = tile.getCollisionBox().getNormals();

                // CALCULATE FOR EVERY NORMAL AND BOTH SHAPES THEIR PROJECTION
                // NORMALS OF SHAPE A
                for (int i = 0; i < normalsA.size; i++) {
                    Vector2 projectionA = getProjection(collisionBox, normalsA.get(i));
                    Vector2 projectionB = getProjection(tile.getCollisionBox(), normalsA.get(i));

                    if (!overlaps(projectionA, projectionB)) {
                        Gdx.app.log("EVENT", "RETURN Y");
                        break;
                    } else {
                        float overlap = getOverlap(projectionA, projectionB);
                        if (overlap < minOverlapMagnitude) {
                            minOverlapMagnitude = overlap;
                            resolutionAxis = normalsA.get(i);
                        }
                    }

                }
                Gdx.app.log("COLLISON DETECTED SHAPE A AXIS Y", Float.toString(minOverlapMagnitude) + resolutionAxis.toString());
                // NORMALS OF SHAPE B
                for (int i = 0; i < normalsB.size; i++) {
                    Vector2 projectionA = getProjection(collisionBox, normalsB.get(i));
                    Vector2 projectionB = getProjection(tile.getCollisionBox(), normalsB.get(i));

                    if (!overlaps(projectionA, projectionB)) {
                        Gdx.app.log("EVENT", "RETURN Y");
                        break;
                    } else {
                        float overlap = getOverlap(projectionA, projectionB);

                        if (overlap < minOverlapMagnitude) {
                            minOverlapMagnitude = overlap;
                            resolutionAxis = normalsB.get(i);
                        }
                    }

                }
                Gdx.app.log("COLLISON DETECTED SHAPE B AXIS Y", Float.toString(minOverlapMagnitude) + resolutionAxis.toString());
*/

                if (!tile.isPassable()) {
                    if (player.getVelocity().y < 0) {
                        player.groundEntity();
                    }
                    // player.setVelocityY(0f);
                    //player.getVelocity().add(resolutionAndMagnitude.cpy().scl(minOverlapMagnitude).scl(delta));

                    if(resolutionAndMagnitude != null) {
                        Gdx.app.log("RESOLUTION AND MAGANITUDE", resolutionAndMagnitude.toString());
                        Vector2 resolutionDir = new Vector2(resolutionAndMagnitude.x, resolutionAndMagnitude.y);
                        float magnitude = resolutionAndMagnitude.z;
                        player.getVelocity().add(resolutionDir.cpy().scl(magnitude).scl(delta));
                    }
                    DAMPING = tile.getFriction();
                    MAX_VELOCITY = tile.getMaxVelocity();
                    break;
                }


              /*  collisionBox.y += ahead;
                // IF A COLLISION IS FOUND
                worldContainer.getcRectanglesAhead().add(new Rectangle(collisionBox.x, collisionBox.y, collisionBox.width, collisionBox.height));
                if (collisionBox.overlaps(tile.getBounds())) {
                    if (!tile.isPassable()) {

                        // WHEN PLAYER WAS FALLING, HE NOW IS GROUNDED
                        if (player.getVelocity().y < 0) {
                            player.groundEntity();
                        }

                        // UPDATE PHYSICAL INFORMATION ABOUT CURRENT TILE THE PLAYER STANDS ON
                        player.setVelocityY(0f);
                        // Gdx.app.log("EVENT", "Y VELOCITY SET to 0");
                        DAMPING = tile.getFriction();
                        MAX_VELOCITY = tile.getMaxVelocity();

                        // FOR DEBUG PURPOSES
                        worldContainer.getCollisionRectangles().add(tile.getBounds());
                        worldContainer.getCollisionRectangles().add(collisionBox);

                        // ACTION OF TILES WILL BE TRIGGERED HERE IF EXISTING

                        if (tile.isInteractable()) {
                            tile.action(player, delta);
                            Gdx.app.log("TILE ACTION:", "[" + tile.getType() + "]" + "ACTION TRIGGERED AT" + formVec(player.getPosition()));
                        }
                        player.setHitboxes(player.getPosition());

                        break;


                    }

                }*/
            }

        }

        player.getVelocity().scl(1 / delta);

    }


    // FIND COLLISION DETECTION CANDIDATES
    // ---------------------------------------------------------------------------------------------

    private void findCollisionCandidates(int sx, int sy, int ex, int ey) {
        collisionCandidateTiles.clear();
        for (int x = sx; x <= ex; x++) {
            for (int y = sy; y <= ey; y++) {
                // CHECK WHETHER TILE IS IN LEVEL BOUNDS
                if (level.checkBounds(x, y)) {
                    collisionCandidateTiles.add(level.getTile(x, y));
                }
            }
        }

    }




    // TEST: x,y: Vector2 resolutionAxis, z: magnitude


    private Vector3 checkCollision(Tile tile, CollisionBox collisionBox) {
        float minOverlapMagnitude = 10000f;
        Vector3 resolutionAndMagnitude = null;

        // GET NORMALS OF EACH SHAPE
        Array<Vector2> normalsA = collisionBox.getNormals();
        Array<Vector2> normalsB = tile.getCollisionBox().getNormals();

        // CALCULATE FOR EVERY NORMAL AND BOTH SHAPES THEIR PROJECTION
        // NORMALS OF SHAPE A
        for (int i = 0; i < normalsA.size; i++) {
            Vector2 projectionA = getProjection(collisionBox, normalsA.get(i));
            Vector2 projectionB = getProjection(tile.getCollisionBox(), normalsA.get(i));

            Gdx.app.log("PROJECTION A", projectionA.toString());
            Gdx.app.log("PROJECTION B", projectionB.toString());
            if (!overlaps(projectionA, projectionB)) {
                Gdx.app.log("EVENT", "NO OVERLAP. EXITING COLLISION DETECTION.");
                return null;
            } else {
                float overlap = getOverlap(projectionA, projectionB);
                if (overlap < minOverlapMagnitude) {
                    minOverlapMagnitude = overlap;

                    // x: RESOLUTION AXIS X COMPONENT, y: RESOLUTION AXIS Y COMPONENT, Z: MAGNITUDE
                    resolutionAndMagnitude = new Vector3(normalsA.get(i).x, normalsA.get(i).y, minOverlapMagnitude);
                }
            }

        }
        if(!resolutionAndMagnitude.equals(null))
        Gdx.app.log("COLLISON DETECTED SHAPE A AXIS Y", Float.toString(minOverlapMagnitude) + resolutionAndMagnitude.toString());
        // NORMALS OF SHAPE B
        for (int i = 0; i < normalsB.size; i++) {
            Vector2 projectionA = getProjection(collisionBox, normalsB.get(i));
            Vector2 projectionB = getProjection(tile.getCollisionBox(), normalsB.get(i));

            Gdx.app.log("PROJECTION A", projectionA.toString());
            Gdx.app.log("PROJECTION B", projectionB.toString());

            if (!overlaps(projectionA, projectionB)) {
                Gdx.app.log("EVENT", "NO OVERLAP. EXITING COLLISION DETECTION.");
                return null;
            } else {
                float overlap = getOverlap(projectionA, projectionB);

                if (overlap < minOverlapMagnitude) {
                    minOverlapMagnitude = overlap;
                    resolutionAndMagnitude = new Vector3(normalsB.get(i).x, normalsB.get(i).y, minOverlapMagnitude);
                    Gdx.app.log("UPDATED resolutionAndMagnitude TO", resolutionAndMagnitude.toString());
                }
            }

        }

        return resolutionAndMagnitude;

    }


    private void managePlayerSpeed() {

        if (player.getAcceleration().x == 0) {
            player.getVelocity().x *= DAMPING;

            if (Math.abs(player.getVelocity().x) < 0.001f) {
                player.setVelocityX(0f);
            }

        }

        if (player.getVelocity().x > MAX_VELOCITY) {
            player.setVelocityX(MAX_VELOCITY);
        }
        if (player.getVelocity().x < -MAX_VELOCITY) {
            player.setVelocityX(-MAX_VELOCITY);
        }

        if (player.getVelocity().y < -MAX_FALLING_SPEED) {
            player.setVelocityY(-MAX_FALLING_SPEED);
        }

        // IF PLAYER FALLS OUT OF BOUNDS, HE IS PUT BACK TO THE START

        if (!level.checkBounds((int) player.getPositionX(), (int) player.getPositionY())) {
            player.setPosition(new Vector2(5f, 12f));
        }


    }


    // GETTER & SETTER
    // ---------------------------------------------------------------------------------------------


}
