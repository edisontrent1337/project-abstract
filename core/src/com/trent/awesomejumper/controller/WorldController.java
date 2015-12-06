package com.trent.awesomejumper.controller;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.trent.awesomejumper.models.Entity.State;
import com.trent.awesomejumper.models.Level;
import com.trent.awesomejumper.models.Player;
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

    // MAXIMUM VELOCITY  & DAMPING DETERMINED BY TILE
    public float MAX_VELOCITY, DAMPING;

    // WEATHER SIMULATION
    private float windSpeed, windDelta;

    // COLLISION RESOLUTION VECTORS

    private Vector2 resolutionVector;
    private boolean collidedX, collidedY;

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
        this.resolutionVector = new Vector2(0.0f, 0.0f);
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

    public void increaseGravity() {
        player.setVelocityY(0f);
    }

    public void decreaseGravity() {
        player.setVelocityY(0f);
    }

    // UPDATE FUNCTION: INPUT PROCRESSING & COLLISION DETECTION
    // ---------------------------------------------------------------------------------------------

    public void update(float delta) {
        DAMPING = 0.55f;
        MAX_VELOCITY = 5f;
        // INPUT PROCCESSING
        processUserInput();

        player.getAcceleration().scl(delta);
        player.getVelocity().add(player.getAcceleration());



        //Vector2 y = collisionDetectionY(delta);
        //if (y != null)
          //  resultantY.add(y).scl(delta);

      /*  float a = player.getPositionX();
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
        }*/

        /*if (Math.abs(resultantX.x) < Math.abs(resultantY.y)) {
            player.getPosition().add(resultantX);
            Gdx.app.log("EVENT", "ADDED" + resultantX.toString() + "TO X");
        } else {
            player.getPosition().add(resultantY);
            Gdx.app.log("EVENT", "ADDED" + resultantY.toString() + "TO Y");
        }*/


        //TOUCH FLOOR
        //AREA 1

        // player position
        float px = player.getPositionX();
        float endx = player.getPositionX() + player.getBody().get(0).getWidth();
        float py = player.getPositionY();
        float endy = player.getPositionY() + player.getBody().get(0).getHeight();

        // intersection points with world
        float cpx = (float) Math.ceil(px);
        float cpy = (float) Math.ceil(py);




       // collisionDetectionY(delta);
        collisionDetection(delta);


        Gdx.app.log("RESULT OF COLLISIONDETECTION BEFORE ADDING", resolutionVector.toString());
        player.getPosition().add(resolutionVector);
        resolutionVector.x = 0f;
        resolutionVector.y = 0f;
        /*if(Math.abs(resolutionVector.x) >= Math.abs(resolutionVector.y)) {
            player.getPosition().y += resolutionVector.y;
            Gdx.app.log("ADDED, X GREATER Y", Float.toString(resolutionVector.y));
        }

        if(Math.abs(resolutionVector.x) <= Math.abs(resolutionVector.y) ){

            player.getPosition().x += resolutionVector.x;
            Gdx.app.log("ADDED, X SMALLER Y", Float.toString(resolutionVector.x));
        }*/


       // player.getPosition().add(resolutionVector);
        managePlayerSpeed();

        player.update(delta);
        player.setBounds(player.getPositionX(), player.getPositionY());

        /*for (SkyBox s : level.getSkyBoxes()) {
            s.update(delta);
        }*/

    }


    // PROCESS USER INPUT
    // ---------------------------------------------------------------------------------------------

    private void processUserInput() {
        // WALKING UP

        if (keyMap.get(Keys.UP) & !(keyMap.get(Keys.DOWN) || keyMap.get(Keys.RIGHT) || keyMap.get(Keys.LEFT))) {
            player.setState(State.WALKING);
            player.setAccelY(ACCELERATION);
            player.setAccelX(0f);
        }
        // WALKING DOWN
        else if (keyMap.get(Keys.DOWN) & !(keyMap.get(Keys.UP) || keyMap.get(Keys.RIGHT) || keyMap.get(Keys.LEFT))) {
            player.setState(State.WALKING);
            player.setAccelY(-ACCELERATION);
            player.setAccelX(0f);
        }

        // WALKING RIGHT
        else if (keyMap.get(Keys.RIGHT) & !(keyMap.get(Keys.LEFT) || keyMap.get(Keys.UP) || keyMap.get(Keys.DOWN))) {
            player.setFacingL(false);
            player.setState(State.WALKING);
            player.setAccelX(ACCELERATION);
            player.setAccelY(0f);
        }

        // WALKING LEFT
        else if (keyMap.get(Keys.LEFT) & !(keyMap.get(Keys.RIGHT) || keyMap.get(Keys.UP) || keyMap.get(Keys.DOWN))) {
            player.setFacingL(true);
            player.setState(State.WALKING);
            player.setAccelX(-ACCELERATION);
            player.setAccelY(0f);

        }

        // WALKING UP RIGHT
        else if (keyMap.get(Keys.UP) && keyMap.get(Keys.RIGHT) & !(keyMap.get(Keys.DOWN) || keyMap.get(Keys.LEFT))) {
            player.setFacingL(false);
            player.setState(State.WALKING);
            player.setAccelX(ACCELERATION);
            player.setAccelY(ACCELERATION);
        }

        // WALKING UP LEFT
        else if (keyMap.get(Keys.UP) && keyMap.get(Keys.LEFT) & !(keyMap.get(Keys.DOWN) || keyMap.get(Keys.RIGHT))) {
            player.setFacingL(true);
            player.setState(State.WALKING);
            player.setAccelX(-ACCELERATION);
            player.setAccelY(ACCELERATION);
        }

        // WALKING DOWN RIGHT
        else if (keyMap.get(Keys.DOWN) && keyMap.get(Keys.RIGHT) & !(keyMap.get(Keys.UP) || keyMap.get(Keys.LEFT))) {
            player.setFacingL(false);
            player.setState(State.WALKING);
            player.setAccelX(ACCELERATION);
            player.setAccelY(-ACCELERATION);
        }

        // WALKING DOWN LEFT
        else if (keyMap.get(Keys.DOWN) && keyMap.get(Keys.LEFT) & !(keyMap.get(Keys.UP) || keyMap.get(Keys.RIGHT))) {
            player.setFacingL(true);
            player.setState(State.WALKING);
            player.setAccelX(-ACCELERATION);
            player.setAccelY(-ACCELERATION);
        }


        // IDLE
        else {
            player.setState(State.IDLE);
            player.setAccelX(0f);
            player.setAccelY(0f);
        }

    }

    /**
     *
     * TODO: move vertical collision algorithm here and put an if condition wheter overlap is still present
     * then go to x collision detection :D
     */

    // COLLISION DETECTION
    // ---------------------------------------------------------------------------------------------

    private void collisionDetection(float delta) {
        /**
         * Reset the resolution vector to (0,0)
         */


        resolutionVector.y = 0f;
        resolutionVector.x = 0f;

        player.getVelocity().scl(delta);
        int cdStartX, cdEndX, cdStartY, cdEndY;
        // -----------------------------------------------------------------------------------------
        // VERTICAL COLLISION DETECTION
        // -----------------------------------------------------------------------------------------

        cdStartX = (int) player.getBounds().x;
        cdEndX = (int) (player.getBounds().x + player.getBody().get(0).getWidth());
        if (player.getVelocity().y <= 0) {
            cdStartY = cdEndY = (int) (Math.floor(player.getBounds().y));
        } else {
            cdStartY = cdEndY = (int) (Math.floor(player.getBounds().y + player.getBody().get(0).getHeight()));
        }


        worldContainer.createCollisionTiles(cdStartX, cdStartY, cdEndX, cdEndY);
        Gdx.app.log("EVENT", "ENTERING VERTICAL COLLISION DETECTION.");
        Gdx.app.log("~~~~~~~~~~~~~~", "----------------------------------------------");
        for (Tile tile : worldContainer.getCollisionTiles()) {
            Gdx.app.log("CANDIDATE:", tile.getPosition().toString());
        }
        Gdx.app.log("~~~~~~~~~~~~~~", "----------------------------------------------");

        for (Tile tile : worldContainer.getCollisionTiles()) {

            Gdx.app.log("PLAYER POSITION", formVec(player.getPosition()));
            Gdx.app.log("TILE", formVec(tile.getPosition()));

            CollisionBox collisionBox = player.getBody().get(0);
            

            if (checkCollision(tile, collisionBox) && !tile.isPassable()) {
                if(resolutionVector.y != 0f) {
                    Gdx.app.log("RESOLUTION AND MAGNITUDE", resolutionVector.toString());
                    player.setVelocityY(0f);
                    Gdx.app.log("VERTICAL CD", "RESET VY TO 0");
                }
                return;

            }
            else {
                break;
            }


        }


        // -----------------------------------------------------------------------------------------
        // HORIZONTAL COLLISION DETECTION
        // -----------------------------------------------------------------------------------------

        cdStartY = (int) (player.getBounds().y);
        cdEndY = (int) (player.getBounds().y + player.getBody().get(0).getHeight());
        // X AXIS INTERVAL DEPENDS ON PLAYERS MOVEMENT DIRECTION
        if (player.getVelocity().x <= 0) {
            cdStartX = cdEndX = (int) (Math.floor(player.getBounds().x));
        } else {
            cdStartX = cdEndX = (int) (Math.floor(player.getBounds().x + player.getBody().get(0).getWidth()));
        }


        // FIND ALL TILES THE PLAYER CAN COLLIDE WITH
        /**
         * Fill the collisionCandidateArray with tiles surrounding the player
         * TODO: maybe other entities like enemies will go in there too.
         */
        worldContainer.createCollisionTiles(cdStartX, cdStartY, cdEndX, cdEndY);
        Gdx.app.log("EVENT", "ENTERING HORIZONTAL COLLISION DETECTION.");
        Gdx.app.log("~~~~~~~~~~~~~~", "----------------------------------------------");

        Gdx.app.log("EVENT:","CANDIDATES FOR COLLISION DETECTION");
        for (Tile tile : worldContainer.getCollisionTiles()) {
            Gdx.app.log("CANDIDATE:", tile.getPosition().toString());
        }
        Gdx.app.log("~~~~~~~~~~~~~~", "----------------------------------------------");

        // ACTUAL CD
        for (Tile tile : worldContainer.getCollisionTiles()) {

            // LOGGING
            Gdx.app.log("PLAYER POSITION", formVec(player.getPosition()));
            Gdx.app.log("TILE POSITION", formVec(tile.getPosition()));
            /**
             * For every collisionBox of the players body, the collision check is
             * performed.*/
            CollisionBox collisionBox = player.getBody().get(0);


                // If there is an overlap and the tile is passable
                // checkCollision modifies the global resolution vector which is added
                // at the end of the day to the players position
                if (checkCollision(tile,collisionBox) && !tile.isPassable()) {

                    if(resolutionVector.x != 0) {
                        Gdx.app.log("RESOLUTION AND MAGNITUDE", resolutionVector.toString());
                        player.setVelocityX(0f);
                        Gdx.app.log("HORIZONTAL CD", "RESET VX TO 0");
                    }

                        return;


                }
            else {
                    resolutionVector.x = 0f;
                }



        }
        player.getVelocity().scl(1 / delta);
    }


    private void collisionDetectionY(float delta) {
        resolutionVector.y = 0f;
        player.getVelocity().scl(delta);
        int cdStartX, cdEndX, cdStartY, cdEndY;
        // -----------------------------------------------------------------------------------------
        // VERTICAL COLLISION DETECTION
        // -----------------------------------------------------------------------------------------

        cdStartX = (int) player.getBounds().x;
        cdEndX = (int) (player.getBounds().x + player.getBody().get(0).getWidth());
        if (player.getVelocity().y <= 0) {
            cdStartY = cdEndY = (int) (Math.floor(player.getBounds().y));
        } else {
            cdStartY = cdEndY = (int) (Math.floor(player.getBounds().y + player.getBody().get(0).getHeight()));
        }


        worldContainer.createCollisionTiles(cdStartX, cdStartY, cdEndX, cdEndY);
        Gdx.app.log("EVENT", "ENTERING VERTICAL COLLISION DETECTION.");
        Gdx.app.log("~~~~~~~~~~~~~~", "----------------------------------------------");
        for (Tile tile : worldContainer.getCollisionTiles()) {
            Gdx.app.log("CANDIDATE:", tile.getPosition().toString());
        }
        Gdx.app.log("~~~~~~~~~~~~~~", "----------------------------------------------");

        for (Tile tile : worldContainer.getCollisionTiles()) {

            Gdx.app.log("PLAYER POSITION", formVec(player.getPosition()));
            Gdx.app.log("TILE", formVec(tile.getPosition()));

            CollisionBox collisionBox = player.getBody().get(0);



                    if (checkCollision(tile,collisionBox) && !tile.isPassable()) {

                        Gdx.app.log("RESOLUTION AND MAGNITUDE", resolutionVector.toString());
                        if(resolutionVector.y != 0) {
                            player.setVelocityY(0f);
                            Gdx.app.log("VERTICAL CD", "RESET VY TO 0");
                        }

                }


        }

        player.getVelocity().scl(1 / delta);

    }


    // TEST: x,y: Vector2 resolutionAxis, z: magnitude


    private boolean checkCollision(Tile tile, CollisionBox collisionBox) {
        float minOverlapMagnitude = 100000f;

        // GET NORMALS OF EACH SHAPE
        Array<Vector2> normalsA = collisionBox.getNormals();
        Array<Vector2> normalsB = tile.getCollisionBox().getNormals();

        for(Vector2 n : normalsA) {
            Gdx.app.log("NORMAL OF A:", n.toString());
        }


        for(Vector2 n : normalsB) {
            Gdx.app.log("NORMAL OF B:", n.toString());
        }


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
                Gdx.app.log("EVENT", "NO OVERLAP. EXITING COLLISION DETECTION FOR CURRENT TILE.");
                Gdx.app.log("-", "----------------------------------------------");
                Gdx.app.log("-", "----------------------------------------------");
                return false;
            } else {

                /**
                 * projectionA  : player!
                 */

                float overlap = getOverlap(projectionA, projectionB);


                if (Math.abs(overlap) < Math.abs(minOverlapMagnitude)) {
                    minOverlapMagnitude = overlap;
                    if(minOverlapMagnitude == 0)
                        return false;
                    Gdx.app.log("NEW MAGNITUDE", Float.toString(minOverlapMagnitude));
                    Vector2 playerTileDifference = subVec(tile.getPosition(), collisionBox.getPosition());

                    resolutionVector = new Vector2(normalsA.get(i));

                    if (dPro(playerTileDifference, resolutionVector) < 0.0f) {
                        resolutionVector.x = -resolutionVector.x;
                        resolutionVector.y = -resolutionVector.y;
                    }

                    resolutionVector.scl(minOverlapMagnitude);

                    Gdx.app.log("NEW RESOLUTION VECTOR [x,y,mag]", resolutionVector.toString() + Float.toString(minOverlapMagnitude));

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
                Gdx.app.log("EVENT", "NO OVERLAP. EXITING COLLISION DETECTION FOR CURRENT TILE.");
                return false;
            } else {

                float overlap = getOverlap(projectionA, projectionB);


                if (Math.abs(overlap) < Math.abs(minOverlapMagnitude)) {
                    minOverlapMagnitude = overlap;
                    if(minOverlapMagnitude == 0)
                        return false;
                    Gdx.app.log("NEW MAGNITUDE", Float.toString(minOverlapMagnitude));

                    Vector2 playerTileDifference = subVec(collisionBox.getPosition(), tile.getPosition());

                    resolutionVector = new Vector2(normalsB.get(i));

                    if (dPro(playerTileDifference, resolutionVector) < 0.0f) {
                        resolutionVector.x = -resolutionVector.x;
                        resolutionVector.y = -resolutionVector.y;
                    }

                    resolutionVector.scl(minOverlapMagnitude);

                    Gdx.app.log("NEW RESOLUTION VECTOR [x,y,mag]", resolutionVector.toString() + Float.toString(minOverlapMagnitude));
                }

            }

        }

        return true;

    }


    private void managePlayerSpeed() {

        if (player.getAcceleration().x == 0) {
            player.getVelocity().x *= DAMPING;

            if (Math.abs(player.getVelocity().x) < 0.001f) {
                player.setVelocityX(0f);
            }
        }

        if (player.getAcceleration().y == 0) {
            player.getVelocity().y *= DAMPING;

            if (Math.abs(player.getVelocity().y) < 0.001f) {
                player.setVelocityY(0f);
            }

        }

        if (player.getVelocity().x > MAX_VELOCITY) {
            player.setVelocityX(MAX_VELOCITY);
        }
        if (player.getVelocity().x < -MAX_VELOCITY) {
            player.setVelocityX(-MAX_VELOCITY);
        }
        if (player.getVelocity().y > MAX_VELOCITY) {
            player.setVelocityY(MAX_VELOCITY);
        }

        if (player.getVelocity().y < -MAX_VELOCITY) {
            player.setVelocityY(-MAX_VELOCITY);
        }

        // IF PLAYER FALLS OUT OF BOUNDS, HE IS PUT BACK TO THE START
        if (!level.checkBounds((int) player.getPositionX(), (int) player.getPositionY())) {
            player.setPosition(new Vector2(5f, 12f));
            player.setBounds(player.getPositionX(), player.getPositionY());
            player.getCollisionBox().setPosition(new Vector2(5f, 12f));
        }


    }

}
