package com.trent.awesomejumper.controller.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.trent.awesomejumper.controller.WorldController;
import com.trent.awesomejumper.controller.rendering.PopUpRenderer;
import com.trent.awesomejumper.engine.entity.Entity;
import com.trent.awesomejumper.engine.entity.EntityInterface;
import com.trent.awesomejumper.models.Player;
import com.trent.awesomejumper.models.weapons.Weapon;
import com.trent.awesomejumper.utils.Utils;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import static com.trent.awesomejumper.utils.PhysicalConstants.ACCELERATION;
import static com.trent.awesomejumper.utils.Utils.printVec;

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
        CLEAR_MESSAGES,
        PICKUP
    }

    private Player player;
    static Map<Keys, Boolean> keyMap = new EnumMap<>(Keys.class);

    /**
     * Initialization of the keyMap in a static block so that it is immediately ready to use.
     * All keys are set to false (not pressed) in the beginning.
     */
    static {
        for(Keys k : Keys.values()) {
            keyMap.put(k,false);
        }

    }



    public static Vector2 mouse = new Vector2(0f, 0f);
    private OrthographicCamera camera;

    private float dropPressed = 0f;
    /**
     * Determines how long the drop button has to be pressed to drop a weapon.
     */
    private final float DROP_THRESHOLD = 0.75f;

    private final float EQUIP_DISTANCE = 0.66f;
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


    //TODO: change this to one method. BETTER: move input processor to here!
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

    //TODO: change these to one single method.
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

    private boolean isPressed(Keys k) {
        return keyMap.get(k);
    }

    //TODO: implement this.
    public HashSet<Keys> getPressedKeys() {

        HashSet<Keys> pressedKeys = new HashSet<>();

        for(Keys key : Keys.values()) {
            if(keyMap.get(key))
                pressedKeys.add(key);
        }
       return pressedKeys;
    }

    public void dropPressed() {
        // Should only trigger once.
        if (!keyMap.get(Keys.DROP))
            dropPressed = player.time;
        Utils.log("DROP PRESSED", Float.toString(dropPressed));
        keyMap.put(Keys.DROP, true);
    }

    public void dropReleased() {
        Utils.log("DROP RELEASED", Float.toString(WorldController.worldTime));
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

    //TODO: test and finish implementing this.
    public void changeWeapon(int direction) {
        // Resetting the drop pressed time prevents dropping weapons right after selecting them.
        dropPressed = player.time;
        player.getWeaponInventory().changeWeapon(direction);
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

        //TODO: ADD POPUP WHICH SHOWS TIMINGS FOR DROPPING THE WEAPON
        if (keyMap.get(Keys.DROP) && (WorldController.worldTime - dropPressed > DROP_THRESHOLD)) {
            if (player.getWeaponInventory().isHoldingAWeapon()) {
                Utils.log("DROPPED WEAPON");
                player.getWeaponInventory().dropWeapon();
            }
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


    // GETTER & SETTER
    // ---------------------------------------------------------------------------------------------

    public void setPlayer(Player player) {
        this.player = player;
    }

    public static Vector2 getCursorPosition() {
        return new Vector2((float)Math.floor(mouse.x), (float)Math.floor(mouse.y));
    }

}


