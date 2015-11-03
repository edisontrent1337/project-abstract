package com.trent.awesomejumper.controller;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.trent.awesomejumper.models.Entity.State;
import com.trent.awesomejumper.models.Level;
import com.trent.awesomejumper.models.Player;
import com.trent.awesomejumper.models.SkyBox;
import com.trent.awesomejumper.models.WorldContainer;
import com.trent.awesomejumper.testing.CollisionBox;
import com.trent.awesomejumper.testing.Interval;
import com.trent.awesomejumper.tiles.Tile;

import java.util.HashMap;
import java.util.Map;

import static com.trent.awesomejumper.utils.PhysicalConstants.*;
import static com.trent.awesomejumper.utils.Utilities.dPro;
import static com.trent.awesomejumper.utils.Utilities.formVec;
import static com.trent.awesomejumper.utils.Utilities.getOverlap;
import static com.trent.awesomejumper.utils.Utilities.getProjection;
import static com.trent.awesomejumper.utils.Utilities.overlaps;
import static com.trent.awesomejumper.utils.Utilities.subVec;

/**
 * Created by Sinthu on 12.06.2015.
 */
public class WorldController {

    enum Keys {
        UP, DOWN, LEFT, RIGHT, D, G, H
    }

    // MEMBERS & INSTANCES
    // ---------------------------------------------------------------------------------------------

    private WorldContainer worldContainer;
    private Player player;
    private Level level;
    private SkyBox farSky01, farSky02, nearSky01, nearSky02;
    private Array<Tile> collisionCandidateTiles = new Array<>();

    private Collider collider;

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
        keyMap.put(Keys.G, false);
        keyMap.put(Keys.H, false);
    }


    // CONSTRUCTOR
    // ---------------------------------------------------------------------------------------------

    public WorldController(WorldContainer worldContainer) {
        this.worldContainer = worldContainer;
        this.level = worldContainer.getLevel();
        this.player = worldContainer.getPlayer();
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

    public void increaseGravity() {

        // player.setAccelY(++GRAVITY);
        player.setVelocityY(0f);
    }

    public void decreaseGravity() {
        // player.setAccelY(--GRAVITY);
        player.setVelocityY(0f);
    }

    // UPDATE FUNCTION: INPUT PROCRESSING & COLLISION DETECTION
    // ---------------------------------------------------------------------------------------------

    public void update(float delta) {
        DAMPING = 0.8f;
        MAX_VELOCITY = 6f;
        // INPUT PROCCESSING
        Vector2 resultantX = new Vector2(0f, 0f);
        Vector2 resultantY = new Vector2(0f, 0f);
        processUserInput();

        if (player.isOnGround() && player.getState().equals(State.JUMPING)) {
            player.setState(State.IDLE);
            player.setVelocityY(0f);
        }

        if (player.getVelocity().y < 0 && !player.isOnGround()) {
            player.setState(State.FALLING);
        }
        if (player.getVelocity().y > 0) {
            player.elevateEntity();
        }


        player.setAccelY(GRAVITY);
        player.getAcceleration().scl(delta);
        player.getVelocity().add(player.getAcceleration());

      /*  if(Math.abs(player.getVelocity().y) > Math.abs(player.getVelocity().x)) {
            resultantY.add(collisionDetectionY(delta));
            player.getVelocity().add(resultantY);
            resultantX.add(collisionDetectionX(delta));
            player.getVelocity().add(resultantX);
        }

        else {
            resultantX.add(collisionDetectionX(delta));
            player.getVelocity().add(resultantX);
            resultantY.add(collisionDetectionY(delta));
            player.getVelocity().add(resultantY);
        }*/

        Vector2 x = collisionDetectionX(delta);
        if(x != null)
        resultantX.add(x);
        Vector2 y = collisionDetectionY(delta);
        if(y != null)
        resultantY.add(y);

        float a = player.getPositionX();
        float mid = (float) Math.ceil(player.getPositionX());
        float b = player.getPositionX() + player.getBody().get(0).getWidth();

        Gdx.app.log("A", Float.toString(a));
        Gdx.app.log("MID", Float.toString(mid));
        Gdx.app.log("B", Float.toString(b));

        float areaX =  (mid - a)*resultantY.y;
        float areaY =  (b - mid)*resultantY.y;

        Gdx.app.log("AREAX", Float.toString(areaX));
        Gdx.app.log("AREAY", Float.toString(areaY));

        if(Math.abs(areaX) > Math.abs(areaY)) {
        player.getPosition().add(resultantX.scl(delta).scl(1.6f));
        player.getPosition().add(resultantY.scl(delta).scl(1.6f));
        }
        else {
        player.getPosition().add(resultantY.scl(delta).scl(1.6f));
        player.getPosition().add(resultantX.scl(delta).scl(1.6f));
        }

        //player.getPosition().add(resultantY.scl(delta).scl(1.6f));
        //player.getPosition().add(resultantX.scl(delta).scl(1.6f));
        managePlayerSpeed();

        player.update(delta);
        player.setBounds(player.getPositionX(), player.getPositionY());

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
        else {
            if (!player.getState().equals(State.JUMPING)) {
                player.setState(State.IDLE);
            }
            player.setAccelX(0f);
        }

    }

    // COLLISION DETECTION
    // ---------------------------------------------------------------------------------------------

    private Vector2 collisionDetectionX(float delta) {
        Vector2 collisionResultant = new Vector2(0f, 0f);
        player.getVelocity().scl(delta);
        // -----------------------------------------------------------------------------------------
        // HORIZONTAL COLLISION DETECTION
        // -----------------------------------------------------------------------------------------


        /**
         * TODO
         *
         * VERTICAL CD HAS TO ONLY LOOK AT A CERTAIN COLLISIONBOX AND CERTAIN NORMALS
         *
         */

        int cdStartX, cdEndX, cdStartY, cdEndY;



       // if((Math.abs(player.getVelocity().x) < 0.001f))
       //     dummy.getPosition().x += player.getVelocity().x;
        // Y AXIS INTERVAL
        cdStartY = (int) (player.getBounds().y);
        cdEndY = (int) (player.getBounds().y + player.getBounds().height);


        // X AXIS INTERVAL DEPENDS ON PLAYERS MOVEMENT DIRECTION
        if (player.getVelocity().x < 0) {
            cdStartX = cdEndX = (int) (Math.floor(player.getBounds().x + player.getVelocity().x));
        } else {
            cdStartX = cdEndX = (int) (Math.floor(player.getBounds().x + player.getBounds().width
                    + player.getVelocity().x));
            // cdStartX = (int) Math.floor(player.getBounds().x) + 2;
            // cdEndX = cdStartX - 1;

        }
        // FIND ALL TILES THE PLAYER CAN COLLIDE WITH
        worldContainer.createCollisionTiles(cdStartX, cdStartY, cdEndX, cdEndY);

        // ACTUAL CD
        for (Tile tile : worldContainer.getCollisionTiles()) {


            if (tile == null) { // air tiles
                continue;
            }


            /*CollisionBox dummy = new CollisionBox(player.getPositionX() + player.getVelocity().x,
                    player.getPositionY(),
                    player.getBody().get(0).getWidth(),
                    player.getBody().get(0).getHeight());

            if(Math.abs(subVec(player.getPosition(), tile.getPosition()).x) > 0.01f) {
                //Gdx.app.log("WHAT","THE FCK");
                dummy.setPosition(dummy.getPosition().x + player.getVelocity().x, dummy.getPosition().y);
            }*/

            // LOGGING
           /* Gdx.app.log("EVENT", "ENTERING HORIZONTAL COLLISION DETECTION.");
            Gdx.app.log("PLAYER POSITION", formVec(player.getPosition()));
            Gdx.app.log("TILE", formVec(tile.getPosition()));

            /**
             * For every collisionBox of the players body, the collision check is
             * performed.*/

            for (CollisionBox collisionBox : player.getBody()) {

                CollisionBox dummy = new CollisionBox(collisionBox.getPosition().x + player.getVelocity().x,
                    collisionBox.getPosition().y,
                    collisionBox.getWidth(),
                    collisionBox.getHeight());



                Vector2 resolutionAndMagnitude = checkCollision(tile, dummy);
                if (resolutionAndMagnitude != null && !tile.isPassable()) {
                  //  Gdx.app.log("RESOLUTION AND MAGANITUDE", resolutionAndMagnitude.toString());
                    collisionResultant.add(resolutionAndMagnitude);
                 //   Gdx.app.log("HORIZONTAL CD", "RESET VX = 0");
                   if(collisionResultant.x != 0f) {
                        player.setVelocityX(0f);
                        player.setState(State.IDLE);
                   }

                    break;

                }
                else {
                    player.getVelocity().scl(1/delta);
                    return null;

                }


            }

        }
        player.getVelocity().scl(1/delta);
        return collisionResultant;
    }


    private Vector2 collisionDetectionY(float delta) {
        Vector2 collisionResultant = new Vector2(0f, 0f);
        player.getVelocity().scl(delta);
        int cdStartX, cdEndX, cdStartY, cdEndY;
        // -----------------------------------------------------------------------------------------
        // VERTICAL COLLISION DETECTION
        // -----------------------------------------------------------------------------------------

        /**
         * TODO
         *
         * VERTICAL CD HAS TO ONLY LOOK AT A CERTAIN COLLISIONBOX AND CERTAIN NORMALS
         *
         */


        cdStartX = (int) player.getBounds().x;
        cdEndX = (int) (player.getBounds().x + player.getBounds().width);
        if (player.getVelocity().y < 0) {
            cdStartY = cdEndY = (int) (Math.floor(player.getBounds().y + player.getVelocity().y));
        } else {
            cdStartY = cdEndY = (int) (Math.floor(player.getBounds().y + player.getBounds().height + player.getVelocity().y));
        }

        worldContainer.createCollisionTiles(cdStartX, cdStartY, cdEndX, cdEndY);

        for (Tile tile : worldContainer.getCollisionTiles()) {


            if (tile == null) {
                continue;
            }

            Gdx.app.log("EVENT", "ENTERING VERTICAL COLLISION DETECTION.");
            Gdx.app.log("PLAYER POSITION", formVec(player.getPosition()));
            Gdx.app.log("TILE", formVec(tile.getPosition()));

        /*    CollisionBox dummy = new CollisionBox(player.getPositionX(),
                    player.getPositionY() + player.getVelocity().y,
                    player.getBody().get(0).getWidth(),
                    player.getBody().get(0).getHeight());


            if(Math.abs(subVec(player.getPosition(), tile.getPosition()).y) > 0.1f)
                dummy.setPosition(dummy.getPosition().x, dummy.getPosition().y + player.getVelocity().y);*/

            for (CollisionBox collisionBox : player.getBody()) {
                CollisionBox dummy = new CollisionBox(collisionBox.getPosition().x,
                       collisionBox.getPosition().y + player.getVelocity().y,
                       collisionBox.getWidth(),
                       collisionBox.getHeight());

                //if(Math.abs(subVec(player.getPosition(), tile.getPosition()).y) > 0.01f)
                  //  dummy.setPosition(dummy.getPosition().x, dummy.getPosition().y + player.getVelocity().y);
                Vector2 resolutionAndMagnitude = checkCollision(tile, dummy);
                if (!tile.isPassable()) {
                    if (player.getVelocity().y < 0) {
                        player.groundEntity();
                    }

                    if (resolutionAndMagnitude != null) {
                        Gdx.app.log("RESOLUTION AND MAGNITUDE", resolutionAndMagnitude.toString());
                        collisionResultant.add(resolutionAndMagnitude);
                        Gdx.app.log("VERTICAL CD", "RESET VY= 0");
                        if(collisionResultant.y != 0) {
                            player.setVelocityY(0f);
                           // break;
                       }
                        break;

                    }
                    /**
                     * TODO:
                     * PROBLEM: EARLY EXIT FROM THIS FUNCTION WHEN THE FIRST TILE TESTED RETURNS NULL...
                     * WE EXIT THIS FUNCTION TOO EARLY, NOT CHECKING POTENTIAL OTHER CANDIDATES IN
                     * THE ARRAY collisioncandidates.
                     * SOLUTION: COLLIDER CLASS WHICH WORKS WITH BOOLS AND MANIPULATES GLOBAL RESOLUTION VECTORS
                     * */

                     else {

                        player.getVelocity().scl(1/delta);
                        return null;

                    }
                    //DAMPING = tile.getFriction();
                    //MAX_VELOCITY = tile.getMaxVelocity();

                }

            }

        }

        player.getVelocity().scl(1 / delta);
        return collisionResultant;

    }




    // TEST: x,y: Vector2 resolutionAxis, z: magnitude


    private Vector2 checkCollision(Tile tile, CollisionBox collisionBox) {
        float minOverlapMagnitude = 100000f;
        Vector2 mtd = null;

        // GET NORMALS OF EACH SHAPE
        Array<Vector2> normalsA = collisionBox.getNormals();
        Array<Vector2> normalsB = tile.getCollisionBox().getNormals();





        // CALCULATE FOR EVERY NORMAL AND BOTH SHAPES THEIR PROJECTION
        // NORMALS OF SHAPE A
        Gdx.app.log("PLAYER BOX (SHAPE A", "...");
        for (int i = 0; i < normalsA.size; i++) {


            Gdx.app.log("CHECKING NORMAL", normalsA.get(i).toString());
            Interval projectionA = getProjection(collisionBox, normalsA.get(i));
            Interval projectionB = getProjection(tile.getCollisionBox(), normalsA.get(i));

            Gdx.app.log("PROJECTION PLAYER", projectionA.toString());
            Gdx.app.log("PROJECTION B", projectionB.toString());
            if (!overlaps(projectionA, projectionB)) {
                Gdx.app.log("EVENT", "NO OVERLAP. EXITING COLLISION DETECTION.");
                Gdx.app.log("-", "----------------------------------------------");
                Gdx.app.log("-", "----------------------------------------------");
                return null;
            } else {

                /**
                 * projectionA  : player!
                 */

                float overlap = getOverlap(projectionA,projectionB);


                if (Math.abs(overlap) < Math.abs(minOverlapMagnitude) && overlap != 0) {
                    minOverlapMagnitude = overlap;
                   Gdx.app.log("NEW MAGNITUDE", Float.toString(minOverlapMagnitude));
                    Vector2 playerTileDifference = subVec(tile.getPosition(), collisionBox.getPosition());

                    mtd = new Vector2(normalsA.get(i));

                    if(dPro(playerTileDifference,mtd) < 0.0f) {
                        mtd.x = -mtd.x;
                        mtd.y = -mtd.y;
                    }

                    mtd.scl(minOverlapMagnitude);

                    Gdx.app.log("NEW RESOLUTION VECTOR [x,y,mag]", mtd.toString() + Float.toString(minOverlapMagnitude));

                }
            }

        }




        // NORMALS OF SHAPE B
        Gdx.app.log("TILE BOX (SHAPE B)", "...");
        for (int i = 0; i < normalsB.size; i++) {



            Gdx.app.log("CHECKING NORMAL", normalsB.get(i).toString());
            Interval projectionA = getProjection(collisionBox, normalsB.get(i));
            Interval projectionB = getProjection(tile.getCollisionBox(), normalsB.get(i));

            Gdx.app.log("PROJECTION A", projectionA.toString());
            Gdx.app.log("PROJECTION B", projectionB.toString());

            if (!overlaps(projectionA, projectionB)) {
                Gdx.app.log("EVENT", "NO OVERLAP. EXITING COLLISION DETECTION.");
                return null;
            } else {

                float overlap = getOverlap(projectionA,projectionB);


                if (Math.abs(overlap) < Math.abs(minOverlapMagnitude) && overlap != 0) {
                    minOverlapMagnitude = overlap;
                    Gdx.app.log("NEW MAGNITUDE", Float.toString(minOverlapMagnitude));

                    Vector2 playerTileDifference = subVec(collisionBox.getPosition(),tile.getPosition());

                    mtd = new Vector2(normalsB.get(i));

                    if(dPro(playerTileDifference,mtd) < 0.0f) {
                        mtd.x = -mtd.x;
                        mtd.y = -mtd.y;
                    }

                    mtd.scl(minOverlapMagnitude);

                    Gdx.app.log("NEW RESOLUTION VECTOR [x,y,mag]", mtd.toString() + Float.toString(minOverlapMagnitude));
                }

            }

        }


        return mtd;

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
            player.setBounds(player.getPositionX(), player.getPositionY());
            player.getCollisionBox().setPosition(new Vector2(5f, 12f));
        }


    }


    // GETTER & SETTER
    // ---------------------------------------------------------------------------------------------

    public Array<Tile> getCollisionCandidates() {
        return collisionCandidateTiles;
    }

}