package com.trent.awesomejumper.controller.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.sun.org.apache.regexp.internal.RE;
import com.trent.awesomejumper.controller.WorldController;
import com.trent.awesomejumper.controller.entitymanagement.WorldContainer;
import com.trent.awesomejumper.controller.rendering.RenderingEngine;
import com.trent.awesomejumper.engine.entity.Entity;
import com.trent.awesomejumper.engine.entity.EntityInterface;
import com.trent.awesomejumper.models.Player;
import com.trent.awesomejumper.models.weapons.Weapon;
import com.trent.awesomejumper.utils.Utils;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;

import static com.trent.awesomejumper.controller.input.InputHandler.KeyBindings.MOUSE1;
import static com.trent.awesomejumper.controller.input.InputHandler.KeyBindings.RELOAD;
import static com.trent.awesomejumper.utils.PhysicalConstants.ACCELERATION;
import static com.badlogic.gdx.Input.*;

import static com.trent.awesomejumper.utils.Utils.printVec;

/**
 * InputHandler class. Handles player input regarding the GameScreen.
 * Created by Sinthu on 08.12.2015.
 */
public class InputHandler implements InputProcessor {



    // MEMBERS & INSTANCES
    // ---------------------------------------------------------------------------------------------
    enum KeyBindings {
        MOVE_UP(Keys.W),
        MOVE_DOWN(Keys.S),
        MOVE_LEFT(Keys.A),
        MOVE_RIGHT(Keys.D),
        MOUSE1(Buttons.LEFT),
        MOUSE2(Buttons.RIGHT),
        RELOAD(Keys.R),
        DROP(Keys.Q),
        CLEAR_MESSAGES(Keys.C),
        PICKUP(Keys.SPACE),


        //DEBUGGING KEYS
        TOGGLE_DEBUG(Keys.T),
        TOGGLE_ENTITY_DRAWING(Keys.P),
        TOGGLE_HITBOX_DRAWING(Keys.H),
        TOGGLE_INFO_DRAWING(Keys.I),
        TOGGLE_BODY_DRAWING(Keys.B),
        ;


        public final int keyCode;
        KeyBindings(int keyCode) {
            this.keyCode = keyCode;
        }
    }

    private Player player;
    private WorldContainer worldContainer;
    private RenderingEngine renderingEngine;
    static Map<KeyBindings, Boolean> pressedKeysMap = new EnumMap<>(KeyBindings.class);

    //Utils.Pair<Integer, Boolean> p = new Utils.Pair(1,false);
    /**
     * Initialization of the pressedKeysMap in a static block so that it is immediately ready to use.
     * All keys are set to false (not pressed) in the beginning.
     */
    static {
        for(KeyBindings binding : KeyBindings.values()) {
            pressedKeysMap.put(binding,false);
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

    public InputHandler(WorldContainer worldContainer, RenderingEngine renderingEngine) {

        this.worldContainer = worldContainer;
        this.renderingEngine = renderingEngine;
        this.player = worldContainer.getPlayer();
        this.camera = renderingEngine.getGameCamera();
        this.mouse = new Vector2(0f, 0f);
        player.getBody().setAimReference(mouse);

        temp = new Vector3();
        Gdx.input.setInputProcessor(this);
    }


    // BUTTON PRESSING METHODS
    // ---------------------------------------------------------------------------------------------


    //TODO: change this to one method. BETTER: move input processor to here!
    public void leftPressed() {
        pressedKeysMap.put(KeyBindings.MOVE_LEFT, true);
    }

    public void upPressed() {
        pressedKeysMap.put(KeyBindings.MOVE_UP, true);
    }

    public void rightPressed() {
        pressedKeysMap.put(KeyBindings.MOVE_RIGHT, true);
    }

    public void downPressed() {
        pressedKeysMap.put(KeyBindings.MOVE_DOWN, true);
    }

    //TODO: change these to one single method.
    public void leftReleased() {
        pressedKeysMap.put(KeyBindings.MOVE_LEFT, false);
    }

    public void upReleased() {
        pressedKeysMap.put(KeyBindings.MOVE_UP, false);
    }

    public void rightReleased() {
        pressedKeysMap.put(KeyBindings.MOVE_RIGHT, false);
    }

    public void downReleased() {
        pressedKeysMap.put(KeyBindings.MOVE_DOWN, false);
    }

    private boolean isPressed(KeyBindings k) {
        return pressedKeysMap.get(k);
    }

    //TODO: implement this.
    public HashSet<KeyBindings> getPressedKeys() {

        HashSet<KeyBindings> pressedKeys = new HashSet<>();

        for(KeyBindings key : KeyBindings.values()) {
            if(pressedKeysMap.get(key))
                pressedKeys.add(key);
        }
       return pressedKeys;
    }

    public void dropPressed() {
        // Should only trigger once.
        if (!pressedKeysMap.get(KeyBindings.DROP))
            dropPressed = player.time;
        Utils.log("DROP PRESSED", Float.toString(dropPressed));
        pressedKeysMap.put(KeyBindings.DROP, true);
    }

    public void dropReleased() {
        Utils.log("DROP RELEASED", Float.toString(WorldController.worldTime));
        dropPressed = 0f;
        pressedKeysMap.put(KeyBindings.DROP, false);
    }

    public void fire() {
        player.getWeaponInventory().fire();
    }

    public void pickUpPressed() {
        pressedKeysMap.put(KeyBindings.PICKUP, true);
    }

    public void pickUpReleased() {
        pressedKeysMap.put(KeyBindings.PICKUP, false);
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

        if(isPressed(RELOAD))
            player.getWeaponInventory().reload();

        // PICK MOVE_UP WEAPON

        if (pressedKeysMap.get(KeyBindings.PICKUP)) {

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
        //TODO: MOVE TIMINGS INTO dropWeapon() method
        if (pressedKeysMap.get(KeyBindings.DROP) && (WorldController.worldTime - dropPressed > DROP_THRESHOLD)) {
            if (player.getWeaponInventory().isHoldingAWeapon()) {
                Utils.log("DROPPED WEAPON");
                player.getWeaponInventory().dropWeapon();
            }
        }

        temp.set(Gdx.input.getX(), Gdx.input.getY(), 0);
        camera.unproject(temp);

        mouse.x = temp.x;
        mouse.y = temp.y;

        // WALKING MOVE_UP

        if (pressedKeysMap.get(KeyBindings.MOVE_UP) & !(pressedKeysMap.get(KeyBindings.MOVE_DOWN) || pressedKeysMap.get(KeyBindings.MOVE_RIGHT) || pressedKeysMap.get(KeyBindings.MOVE_LEFT))) {
            player.setState(Entity.State.WALKING);
            player.setAccelY(ACCELERATION);
            player.setAccelX(0f);
        }
        // WALKING MOVE_DOWN
        else if (pressedKeysMap.get(KeyBindings.MOVE_DOWN) & !(pressedKeysMap.get(KeyBindings.MOVE_UP) || pressedKeysMap.get(KeyBindings.MOVE_RIGHT) || pressedKeysMap.get(KeyBindings.MOVE_LEFT))) {
            player.setState(Entity.State.WALKING);
            player.setAccelY(-ACCELERATION);
            player.setAccelX(0f);
        }

        // WALKING RIGHT
        else if (pressedKeysMap.get(KeyBindings.MOVE_RIGHT) & !(pressedKeysMap.get(KeyBindings.MOVE_LEFT) || pressedKeysMap.get(KeyBindings.MOVE_UP) || pressedKeysMap.get(KeyBindings.MOVE_DOWN))) {
            player.setFacingL(false);
            player.setState(Entity.State.WALKING);
            player.setAccelX(ACCELERATION);
            player.setAccelY(0f);
        }

        // WALKING MOVE_LEFT
        else if (pressedKeysMap.get(KeyBindings.MOVE_LEFT) & !(pressedKeysMap.get(KeyBindings.MOVE_RIGHT) || pressedKeysMap.get(KeyBindings.MOVE_UP) || pressedKeysMap.get(KeyBindings.MOVE_DOWN))) {
            player.setFacingL(true);
            player.setState(Entity.State.WALKING);
            player.setAccelX(-ACCELERATION);
            player.setAccelY(0f);

        }

        // WALKING MOVE_UP RIGHT
        else if (pressedKeysMap.get(KeyBindings.MOVE_UP) && pressedKeysMap.get(KeyBindings.MOVE_RIGHT) & !(pressedKeysMap.get(KeyBindings.MOVE_DOWN) || pressedKeysMap.get(KeyBindings.MOVE_LEFT))) {
            player.setFacingL(false);
            player.setState(Entity.State.WALKING);
            player.setAccelX(ACCELERATION);
            player.setAccelY(ACCELERATION);
        }

        // WALKING MOVE_UP MOVE_LEFT
        else if (pressedKeysMap.get(KeyBindings.MOVE_UP) && pressedKeysMap.get(KeyBindings.MOVE_LEFT) & !(pressedKeysMap.get(KeyBindings.MOVE_DOWN) || pressedKeysMap.get(KeyBindings.MOVE_RIGHT))) {
            player.setFacingL(true);
            player.setState(Entity.State.WALKING);
            player.setAccelX(-ACCELERATION);
            player.setAccelY(ACCELERATION);
        }

        // WALKING MOVE_DOWN RIGHT
        else if (pressedKeysMap.get(KeyBindings.MOVE_DOWN) && pressedKeysMap.get(KeyBindings.MOVE_RIGHT) & !(pressedKeysMap.get(KeyBindings.MOVE_UP) || pressedKeysMap.get(KeyBindings.MOVE_LEFT))) {
            player.setFacingL(false);
            player.setState(Entity.State.WALKING);
            player.setAccelX(ACCELERATION);
            player.setAccelY(-ACCELERATION);
        }

        // WALKING MOVE_DOWN MOVE_LEFT
        else if (pressedKeysMap.get(KeyBindings.MOVE_DOWN) && pressedKeysMap.get(KeyBindings.MOVE_LEFT) & !(pressedKeysMap.get(KeyBindings.MOVE_UP) || pressedKeysMap.get(KeyBindings.MOVE_RIGHT))) {
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


    /**
     * Updates the corresponding  entry in the pressedKeysMap to "true", if the pressed key
     * is one that has been bound to do something. This will trigger a corresponding method in the
     * update() method.
     * @param keycode integer keyCode by libgdx
     * @return false
     */
    @Override
    public boolean keyDown(int keycode) {
        for(KeyBindings binding : pressedKeysMap.keySet()) {
            if(binding.keyCode == keycode)
                pressedKeysMap.put(binding,true);
        }
        return false;
    }

    /**
     * Updates the corresponding  entry in the pressedKeysMap to "false, if the released key
     * is one that has been bound to do something. This will trigger a corresponding method in the
     * update() method.
     * @param keycode integer keyCode by libgdx
     * @return false
     */
    @Override
    public boolean keyUp(int keycode) {
        for(KeyBindings binding : pressedKeysMap.keySet()) {
            if(binding.keyCode == keycode) {
                pressedKeysMap.put(binding,false);
            }
        }
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if(button == MOUSE1.keyCode) {
            pressedKeysMap.put(MOUSE1, true);
            player.getWeaponInventory().fire();

        }
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if(button == MOUSE1.keyCode) {
            pressedKeysMap.put(MOUSE1,false);
        }
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(int amount) {
        return false;
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


