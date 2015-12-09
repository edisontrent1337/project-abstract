package com.trent.awesomejumper.controller;

import com.badlogic.gdx.math.Vector2;
import com.trent.awesomejumper.models.Entity.State;
import com.trent.awesomejumper.models.Level;
import com.trent.awesomejumper.models.Player;
import com.trent.awesomejumper.models.WorldContainer;

import java.util.HashMap;
import java.util.Map;

import static com.trent.awesomejumper.utils.PhysicalConstants.*;
import static com.trent.awesomejumper.utils.Utilities.dPro;
import static com.trent.awesomejumper.utils.Utilities.formVec;

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
    private CollisionController collisionController;

    // MAXIMUM VELOCITY  & DAMPING DETERMINED BY TILE
    public float MAX_VELOCITY, DAMPING;

    // WEATHER SIMULATION
    private float windSpeed, windDelta;

    // COLLISION RESOLUTION VECTORS

    private Vector2 resolutionVector;

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
        this.collisionController = new CollisionController(worldContainer);
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



    // UPDATE FUNCTION: INPUT PROCRESSING & COLLISION DETECTION
    // ---------------------------------------------------------------------------------------------

    public void update(float delta) {
        DAMPING = 0.55f;
        MAX_VELOCITY = 3.8f;
        // INPUT PROCCESSING
        processUserInput();
        player.getAcceleration().scl(delta);
        player.getVelocity().add(player.getAcceleration());

        collisionController.collisionDetection(delta);
        managePlayerSpeed();

        player.update(delta);
        player.setBounds(player.getPositionX(), player.getPositionY());

        /**
         * TODO: FOG IMPLEMENTATION WITH THE OLD SKYBOXES
         */
        /*for (SkyBox s : level.getSkyBoxes()) {
            s.update(delta);
        }*/

    }


    // PROCESS USER INPUT
    // ---------------------------------------------------------------------------------------------

    private void processUserInput() {
        // WALKING UP

        if (keyMap.get(Keys.UP) &!(keyMap.get(Keys.DOWN) || keyMap.get(Keys.RIGHT) || keyMap.get(Keys.LEFT))) {
            player.setState(State.WALKING);
            player.setAccelY(ACCELERATION);
            player.setAccelX(0f);
        }
        // WALKING DOWN
        else if (keyMap.get(Keys.DOWN) &!(keyMap.get(Keys.UP) || keyMap.get(Keys.RIGHT) || keyMap.get(Keys.LEFT))) {
            player.setState(State.WALKING);
            player.setAccelY(-ACCELERATION);
            player.setAccelX(0f);
        }

        // WALKING RIGHT
        else if (keyMap.get(Keys.RIGHT) &!(keyMap.get(Keys.LEFT) || keyMap.get(Keys.UP) || keyMap.get(Keys.DOWN))) {
            player.setFacingL(false);
            player.setState(State.WALKING);
            player.setAccelX(ACCELERATION);
            player.setAccelY(0f);
        }

        // WALKING LEFT
        else if (keyMap.get(Keys.LEFT) &!(keyMap.get(Keys.RIGHT) || keyMap.get(Keys.UP) || keyMap.get(Keys.DOWN))) {
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


    private void managePlayerSpeed() {

        if (player.getAcceleration().x == 0) {
            player.getVelocity().x *= DAMPING;

            if (Math.abs(player.getVelocity().x) < MIN_WALKING_SPEED) {
                player.setVelocityX(0f);
            }
        }

        if (player.getAcceleration().y == 0) {
            player.getVelocity().y *= DAMPING;

            if (Math.abs(player.getVelocity().y) < MIN_WALKING_SPEED) {
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
