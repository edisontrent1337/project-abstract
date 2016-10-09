package com.trent.awesomejumper.controller.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.trent.awesomejumper.controller.WorldController;
import com.trent.awesomejumper.controller.rendering.PopUpRenderer;
import com.trent.awesomejumper.engine.entity.Entity;
import com.trent.awesomejumper.engine.entity.EntityInterface;
import com.trent.awesomejumper.engine.modelcomponents.popups.Message;
import com.trent.awesomejumper.models.Player;
import com.trent.awesomejumper.models.weapons.Weapon;
import com.trent.awesomejumper.utils.Utilities;

import java.util.HashMap;
import java.util.Map;

import static com.trent.awesomejumper.utils.PhysicalConstants.ACCELERATION;

/**
 * InputHandler class. Handles player input regarding the GameScreen.
 * Created by Sinthu on 08.12.2015.
 */
public class InputHandler {

    // MEMBERS & INSTANCES
    // ---------------------------------------------------------------------------------------------
    enum Keys {
        UP,
        DOWN,
        LEFT,
        RIGHT,
        MOUSE1,
        MOUSE2,
        RELOAD,
        DROP,
        PICKUP
    }

    private Player player;
    static Map<Keys, Boolean> keyMap = new HashMap<>();

    static {
        keyMap.put(Keys.UP, false);
        keyMap.put(Keys.DOWN, false);
        keyMap.put(Keys.LEFT, false);
        keyMap.put(Keys.RIGHT, false);
        keyMap.put(Keys.MOUSE1, false);
        keyMap.put(Keys.MOUSE2, false);
        keyMap.put(Keys.RELOAD, false);
        keyMap.put(Keys.DROP, false);
        keyMap.put(Keys.PICKUP, false);
    }


    public static Vector2 mouse = new Vector2(0f, 0f);
    private OrthographicCamera camera;

    private float dropPressed = 0f;
    private final float DROP_THRESHOLD = 0.75f;

    private final float EQUIP_DISTANCE = 0.75f;
    private final float EQUIP_THRESHOLD = 0.33f;

    Vector3 temp;

    // CONSTRUCTOR
    // ---------------------------------------------------------------------------------------------

    public InputHandler(Player player, OrthographicCamera camera) {

        this.player = player;
        this.camera = camera;
        this.mouse = new Vector2(0f, 0f);
        player.getBody().setAimReference(mouse);

        temp = new Vector3();
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

    public void dropPressed() {
        if (!keyMap.get(Keys.DROP))
            dropPressed = player.time;
        Utilities.log("DROP PRESSED", Float.toString(dropPressed));
        keyMap.put(Keys.DROP, true);


    }

    public void dropReleased() {
        Utilities.log("DROP RELEASED", Float.toString(WorldController.worldTime));
        dropPressed = 0f;
        keyMap.put(Keys.DROP, false);
    }

    public void fire() {
        player.getWeaponInventory().fire();
    }

    public void pickUpPressed() {
        keyMap.put(Keys.PICKUP, true);
    }

    public void pickUpReleased() {
        keyMap.put(Keys.PICKUP, false);
    }


    public void changeWeapon(int direction) {
        player.getWeaponInventory().changeWeapon(direction);
    }

    public void dropWeapon() {
        player.getWeaponInventory().dropWeapon();
    }


    public void reload() {
        player.getWeaponInventory().reload();
    }


    // UPDATE FUNCTION: INPUT PROCESSING
    // -------------------------------------------------------------------------------------------

    public void update() {

        /**
         * Updating mouse position for aiming
         * Calculating the current mouse position in world units.
         * Start at the position of the camera, go back half of the viewport width (24) to get
         * to the starting coordinate of the current viewport. Then add the offset of the mouse in
         * world units to get the real position of the cursor.
         *
         */

        // PICK UP WEAPON

        if (keyMap.get(Keys.PICKUP)) {

            float mindst = Float.MAX_VALUE;
            float dst = 0;
            Entity target = null;
            for (Entity e : player.getBody().getEntityNeighbourHood()) {
                if (e.getType().equals(EntityInterface.Type.DROPPED_WEAPON_ENTITY)) {

                    dst = e.getBody().getCenter().cpy().dst(player.getBody().getCenter().cpy());
                    if (dst > EQUIP_DISTANCE)
                        continue;

                    if (dst < mindst) {
                        target = e;
                        mindst = dst;
                    }
                }
                if (target != null) {
                    if (player.time - player.getWeaponInventory().equipTime > EQUIP_THRESHOLD)
                        player.getWeaponInventory().equipWeapon((Weapon) target);

                }
            }

        }

        // DROP WEAPON

        if (keyMap.get(Keys.DROP) && (WorldController.worldTime - dropPressed > DROP_THRESHOLD)) {
            Utilities.log("DROPPED WEAPON");
            player.getWeaponInventory().dropWeapon();
        }

        temp.set(Gdx.input.getX(), Gdx.input.getY(), 0);
        camera.unproject(temp);

        mouse.x = temp.x;
        mouse.y = temp.y;

        // WALKING UP

        if (keyMap.get(Keys.UP) & !(keyMap.get(Keys.DOWN) || keyMap.get(Keys.RIGHT) || keyMap.get(Keys.LEFT))) {
            player.setState(Entity.State.WALKING);
            player.setAccelY(ACCELERATION);
            player.setAccelX(0f);
        }
        // WALKING DOWN
        else if (keyMap.get(Keys.DOWN) & !(keyMap.get(Keys.UP) || keyMap.get(Keys.RIGHT) || keyMap.get(Keys.LEFT))) {
            player.setState(Entity.State.WALKING);
            player.setAccelY(-ACCELERATION);
            player.setAccelX(0f);
        }

        // WALKING RIGHT
        else if (keyMap.get(Keys.RIGHT) & !(keyMap.get(Keys.LEFT) || keyMap.get(Keys.UP) || keyMap.get(Keys.DOWN))) {
            player.setFacingL(false);
            player.setState(Entity.State.WALKING);
            player.setAccelX(ACCELERATION);
            player.setAccelY(0f);
        }

        // WALKING LEFT
        else if (keyMap.get(Keys.LEFT) & !(keyMap.get(Keys.RIGHT) || keyMap.get(Keys.UP) || keyMap.get(Keys.DOWN))) {
            player.setFacingL(true);
            player.setState(Entity.State.WALKING);
            player.setAccelX(-ACCELERATION);
            player.setAccelY(0f);

        }

        // WALKING UP RIGHT
        else if (keyMap.get(Keys.UP) && keyMap.get(Keys.RIGHT) & !(keyMap.get(Keys.DOWN) || keyMap.get(Keys.LEFT))) {
            player.setFacingL(false);
            player.setState(Entity.State.WALKING);
            player.setAccelX(ACCELERATION);
            player.setAccelY(ACCELERATION);
        }

        // WALKING UP LEFT
        else if (keyMap.get(Keys.UP) && keyMap.get(Keys.LEFT) & !(keyMap.get(Keys.DOWN) || keyMap.get(Keys.RIGHT))) {
            player.setFacingL(true);
            player.setState(Entity.State.WALKING);
            player.setAccelX(-ACCELERATION);
            player.setAccelY(ACCELERATION);
        }

        // WALKING DOWN RIGHT
        else if (keyMap.get(Keys.DOWN) && keyMap.get(Keys.RIGHT) & !(keyMap.get(Keys.UP) || keyMap.get(Keys.LEFT))) {
            player.setFacingL(false);
            player.setState(Entity.State.WALKING);
            player.setAccelX(ACCELERATION);
            player.setAccelY(-ACCELERATION);
        }

        // WALKING DOWN LEFT
        else if (keyMap.get(Keys.DOWN) && keyMap.get(Keys.LEFT) & !(keyMap.get(Keys.UP) || keyMap.get(Keys.RIGHT))) {
            player.setFacingL(true);
            player.setState(Entity.State.WALKING);
            player.setAccelX(-ACCELERATION);
            player.setAccelY(-ACCELERATION);
        }


        // IDLE
        else {
            player.setState(Entity.State.IDLE);
            player.setAccelX(0f);
            player.setAccelY(0f);
        }

    }


    public void setPlayer(Player player) {
        this.player = player;
    }

}


