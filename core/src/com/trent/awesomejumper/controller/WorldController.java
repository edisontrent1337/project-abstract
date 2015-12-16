package com.trent.awesomejumper.controller;

import com.badlogic.gdx.math.Vector2;
import com.trent.awesomejumper.engine.entity.Entity;
import com.trent.awesomejumper.engine.entity.Entity.State;
import com.trent.awesomejumper.models.Level;
import com.trent.awesomejumper.models.Player;
import com.trent.awesomejumper.models.SkyBox;
import com.trent.awesomejumper.models.WorldContainer;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import static com.trent.awesomejumper.utils.PhysicalConstants.*;

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



    // UPDATE FUNCTION: INPUT PROCESSING & COLLISION DETECTION
    // ---------------------------------------------------------------------------------------------

    public void update(float delta) {
        DAMPING = 0.95f;
        MAX_VELOCITY = 5f;
        processUserInput();

        for(Entity e: worldContainer.getEntities()) {
            e.getAcceleration().scl(delta);
            e.getVelocity().add(e.getAcceleration());
        }


        //TODO: Cycle: detect collisions, when collisions occur, send signal, and after that update all entities.

        for(Entity e: worldContainer.getEntities()) {
            collisionController.collisionDetection(e, delta);
        }

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
        for (SkyBox s : level.getSkyBoxes()) {
            s.update(delta);
        }

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

            if (entity.getVelocity().x > MAX_VELOCITY) {
                entity.setVelocityX(MAX_VELOCITY);
            }
            if (entity.getVelocity().x < -MAX_VELOCITY) {
                entity.setVelocityX(-MAX_VELOCITY);
            }
            if (entity.getVelocity().y > MAX_VELOCITY) {
                entity.setVelocityY(MAX_VELOCITY);
            }

            if (entity.getVelocity().y < -MAX_VELOCITY) {
                entity.setVelocityY(-MAX_VELOCITY);
            }

            // IF PLAYER FALLS OUT OF BOUNDS, HE IS PUT BACK TO THE START
            if (!level.checkBounds((int) player.getPosition().x, (int) player.getPosition().y)) {
                player.setPosition(new Vector2(5f, 12f));
                player.setBounds(player.getPosition().x, player.getPosition().y);
            }


        }

    }
}
