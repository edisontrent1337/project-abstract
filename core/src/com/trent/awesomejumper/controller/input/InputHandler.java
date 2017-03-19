package com.trent.awesomejumper.controller.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.trent.awesomejumper.controller.WorldController;
import com.trent.awesomejumper.controller.entitymanagement.WorldContainer;
import com.trent.awesomejumper.controller.rendering.PopUpRenderer;
import com.trent.awesomejumper.controller.rendering.RenderingEngine;
import com.trent.awesomejumper.engine.entity.Entity;
import com.trent.awesomejumper.engine.entity.EntityInterface;
import com.trent.awesomejumper.engine.modelcomponents.popups.Message;
import com.trent.awesomejumper.engine.physics.Ray;
import com.trent.awesomejumper.game.AwesomeJumperMain;
import com.trent.awesomejumper.models.Player;
import com.trent.awesomejumper.models.lootable.Lootable;
import com.trent.awesomejumper.models.weapons.Weapon;
import com.trent.awesomejumper.utils.Utils;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;

import static com.badlogic.gdx.Input.Buttons;
import static com.badlogic.gdx.Input.Keys;
import static com.trent.awesomejumper.controller.input.InputHandler.KeyBindings.CLEAR_POPUPS;
import static com.trent.awesomejumper.controller.input.InputHandler.KeyBindings.DROP;
import static com.trent.awesomejumper.controller.input.InputHandler.KeyBindings.FULLSCREEN;
import static com.trent.awesomejumper.controller.input.InputHandler.KeyBindings.MOUSE1;
import static com.trent.awesomejumper.controller.input.InputHandler.KeyBindings.MOVE_DOWN;
import static com.trent.awesomejumper.controller.input.InputHandler.KeyBindings.MOVE_LEFT;
import static com.trent.awesomejumper.controller.input.InputHandler.KeyBindings.MOVE_RIGHT;
import static com.trent.awesomejumper.controller.input.InputHandler.KeyBindings.MOVE_UP;
import static com.trent.awesomejumper.controller.input.InputHandler.KeyBindings.PICKUP;
import static com.trent.awesomejumper.controller.input.InputHandler.KeyBindings.RAY_CASTING;
import static com.trent.awesomejumper.controller.input.InputHandler.KeyBindings.RELOAD;
import static com.trent.awesomejumper.controller.input.InputHandler.KeyBindings.SHOW_PENETRATION_POINTS;
import static com.trent.awesomejumper.controller.input.InputHandler.KeyBindings.TOGGLE_BODY_DRAWING;
import static com.trent.awesomejumper.controller.input.InputHandler.KeyBindings.TOGGLE_DEBUG;
import static com.trent.awesomejumper.controller.input.InputHandler.KeyBindings.TOGGLE_ENTITY_DRAWING;
import static com.trent.awesomejumper.controller.input.InputHandler.KeyBindings.TOGGLE_HITBOX_DRAWING;
import static com.trent.awesomejumper.controller.input.InputHandler.KeyBindings.TOGGLE_INFO_DRAWING;
import static com.trent.awesomejumper.controller.input.InputHandler.KeyBindings.TOGGLE_LOGGING;
import static com.trent.awesomejumper.controller.input.InputHandler.KeyBindings.TOGGLE_SPECIAL;
import static com.trent.awesomejumper.utils.PhysicalConstants.ACCELERATION;

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
        RELOAD(Keys.R,true),
        DROP(Keys.Q, true, 1.25f),
        PICKUP(Keys.SPACE),
        FULLSCREEN(Keys.F11, true),


        //DEBUGGING KEYS
        TOGGLE_DEBUG(Keys.T,true),
        TOGGLE_ENTITY_DRAWING(Keys.P,true),
        TOGGLE_HITBOX_DRAWING(Keys.H,true),
        TOGGLE_INFO_DRAWING(Keys.I,true),
        TOGGLE_BODY_DRAWING(Keys.B,true),
        CLEAR_POPUPS(Keys.C, true),
        TOGGLE_SPECIAL(Keys.U,true),
        SHOW_PENETRATION_POINTS(Keys.V, true),
        TOGGLE_LOGGING(Keys.L, true),
        RAY_CASTING(Keys.X, true),
        ;


        public final int keyCode;
        public final boolean isToggleBind;
        public final boolean isHoldBind;
        public float timePressed = 0f;
        public float threshold = 1.00f;
        KeyBindings(int keyCode) {
            this.keyCode = keyCode;
            this.isToggleBind = false;
            this.isHoldBind = false;
        }

        // Constructor for keys that should act like toggle keys e.g. toggle certain functionality.
        // Those bindings are only processed a single time per key press.
        KeyBindings(int keyCode, boolean isToggleBind) {
            this.keyCode = keyCode;
            this.isToggleBind = isToggleBind;
            this.isHoldBind = false;
        }

        // Constructor for keys that should act like keys that have to be held a certain amount
        // of time to toggle certain functionality.
        // Every frame, it is checked, whether or not the key was held long enough to trigger an action
        KeyBindings(int keyCode, boolean isHoldBind, float threshold) {
            this.keyCode = keyCode;
            this.isToggleBind = false;
            this.isHoldBind = isHoldBind;
            this.threshold = threshold;
        }
    }

    private Player player;
    private WorldContainer worldContainer;
    private RenderingEngine renderingEngine;
    static Map<KeyBindings, Boolean> pressedKeysMap = new EnumMap<>(KeyBindings.class);

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

        temp = new Vector3();
        for(Controller c : Controllers.getControllers()) {
            Utils.log("CONTROLLER: ", c.getName());
        }
    }


    // BUTTON PRESSING METHODS
    // ---------------------------------------------------------------------------------------------


    private boolean isPressed(KeyBindings binding) {
        return pressedKeysMap.get(binding);
    }

    private boolean isPressed(KeyBindings[] k) {
        return false;
    }

    public HashSet<KeyBindings> getPressedKeys() {

        HashSet<KeyBindings> pressedKeys = new HashSet<>();

        for(KeyBindings key : KeyBindings.values()) {
            if(isPressed(key))
                pressedKeys.add(key);
        }
       return pressedKeys;
    }



    //TODO: test and finish implementing this.
    public void changeWeapon(int direction) {
        // Resetting the drop pressed time prevents dropping weapons right after selecting them.
        dropPressed = player.time;
        player.getWeaponInventory().changeWeapon(direction);
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

        if (isPressed(PICKUP)) {

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

        //TODO: MOVE TIMINGS INTO dropWeapon() method
        if (isPressed(DROP) && (WorldController.worldTime - DROP.timePressed > DROP.threshold)) {
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

        if (isPressed(MOVE_UP) & !(isPressed(MOVE_DOWN) || isPressed(MOVE_RIGHT) || isPressed(MOVE_LEFT))) {
            player.setState(Entity.State.WALKING);
            player.setAccelY(ACCELERATION);
            player.setAccelX(0f);
            Utils.log("WALKING UP");
        }

        // WALKING MOVE_DOWN
        else if (isPressed(MOVE_DOWN) & !(isPressed(MOVE_UP) || isPressed(MOVE_RIGHT) || isPressed(MOVE_LEFT))) {
            player.setState(Entity.State.WALKING);
            player.setAccelY(-ACCELERATION);
            player.setAccelX(0f);
        }

        // WALKING RIGHT
        else if (isPressed(MOVE_RIGHT) &!(isPressed(MOVE_LEFT) || isPressed(MOVE_UP) || isPressed(MOVE_DOWN))) {
            player.setFacingL(false);
            player.setState(Entity.State.WALKING);
            player.setAccelX(ACCELERATION);
            player.setAccelY(0f);
        }

        // WALKING LEFT
        else if (isPressed(MOVE_LEFT) &! (isPressed(MOVE_RIGHT) || isPressed(MOVE_UP) || isPressed(MOVE_DOWN))) {
            player.setFacingL(true);
            player.setState(Entity.State.WALKING);
            player.setAccelX(-ACCELERATION);
            player.setAccelY(0f);

        }

        // WALKING UP RIGHT
        else if (isPressed(MOVE_UP) && isPressed(MOVE_RIGHT) &!(isPressed(MOVE_DOWN) || isPressed(MOVE_LEFT))) {
            player.setFacingL(false);
            player.setState(Entity.State.WALKING);
            player.setAccelX(ACCELERATION);
            player.setAccelY(ACCELERATION);
        }

        // WALKING UP LEFT
        else if (isPressed(MOVE_UP) && isPressed(MOVE_LEFT) &!(isPressed(MOVE_DOWN) || isPressed(MOVE_RIGHT))) {
            player.setFacingL(true);
            player.setState(Entity.State.WALKING);
            player.setAccelX(-ACCELERATION);
            player.setAccelY(ACCELERATION);
        }

        // WALKING DOWN RIGHT
        else if (isPressed(MOVE_DOWN) && isPressed(MOVE_RIGHT) &!(isPressed(MOVE_UP) || isPressed(MOVE_LEFT))) {
            player.setFacingL(false);
            player.setState(Entity.State.WALKING);
            player.setAccelX(ACCELERATION);
            player.setAccelY(-ACCELERATION);
        }

        // WALKING DOWN LEFT
        else if (isPressed(MOVE_DOWN) && isPressed(MOVE_LEFT) &!(isPressed(MOVE_UP) || isPressed(MOVE_RIGHT))) {
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
     * @return true, when the input was processed, false otherwise
     */
    @Override
    public boolean keyDown(int keycode) {
        for(KeyBindings binding : pressedKeysMap.keySet()) {
            if(binding.keyCode == keycode) {
                // If the binding is a key that toggles specific functionality,
                // the toggling is triggered right here once, and not multiple
                // times in the update loop.
                if(binding.isToggleBind) {
                    pressedKeysMap.put(binding, true);
                    Utils.log("KEYS DOWN", pressedKeysMap.toString());
                    processToggleInput();
                    pressedKeysMap.put(binding,false);
                    return true;
                }

                else if(binding.isHoldBind) {
                    timeInputPressed(binding);
                    Utils.log("KEYS DOWN", pressedKeysMap.toString());
                    return true;

                }
                else {
                    pressedKeysMap.put(binding, true);
                    Utils.log("KEYS DOWN", pressedKeysMap.toString());
                    return true;
                }

            }

        }
        return false;
    }

    /**
     * Updates the corresponding  entry in the pressedKeysMap to "false, if the released key
     * is one that has been bound to do something. This will trigger a corresponding method in the
     * update() method.
     * @param keycode integer keyCode by libgdx
     * @return true, when the input was processed, false otherwise
     */
    @Override
    public boolean keyUp(int keycode) {
        for(KeyBindings binding : pressedKeysMap.keySet()) {
            if(binding.keyCode == keycode) {
                pressedKeysMap.put(binding, false);
                Utils.log("KEYS UP", pressedKeysMap.toString());
                if(binding.isHoldBind) {
                    timeInputReleased(binding);
                }

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

        changeWeapon(amount);
        return false;
    }


    /**
     * Registers the point in time a button was pressed.
     * @param binding key we are interested in.
     */
    private void timeInputPressed(KeyBindings binding) {
        if(!isPressed(binding)) {
            binding.timePressed = player.time;
            PopUpRenderer.getInstance().addMessageToCategory(PopUpRenderer.PopUpCategories.HEAL, new Message(Float.toString(binding.timePressed), player.getPosition(), WorldController.worldTime, binding.threshold));
            pressedKeysMap.put(binding,true);
        }
    }

    /**
     * Registers the point in time a button was released.
     * @param binding key we are interested in.
     */
    private void timeInputReleased(KeyBindings binding) {
        PopUpRenderer.getInstance().addMessageToCategory(PopUpRenderer.PopUpCategories.DMG, new Message(Float.toString(WorldController.worldTime), player.getPosition(), WorldController.worldTime, binding.threshold));
        pressedKeysMap.put(binding,false);
    }


    /**
     * Processes all input that has toggle character. That means single button presses
     * which should only once trigger an action.
     */
    private void processToggleInput() {

        // DEBUG TOGGLE BUTTONS
        if(isPressed(TOGGLE_DEBUG))
            AwesomeJumperMain.toggleDebugMode();
        if(AwesomeJumperMain.onDebugMode()) {
            if (isPressed(TOGGLE_ENTITY_DRAWING))
                AwesomeJumperMain.toggleEntityDrawing();
            if (isPressed(TOGGLE_BODY_DRAWING))
                AwesomeJumperMain.toggleBodyDrawing();
            if (isPressed(TOGGLE_HITBOX_DRAWING))
                AwesomeJumperMain.toggleHitboxDrawing();
            if (isPressed(TOGGLE_INFO_DRAWING))
                AwesomeJumperMain.toggleInfoDrawing();
            if (isPressed(CLEAR_POPUPS))
                PopUpRenderer.getInstance().clear();
            if (isPressed(SHOW_PENETRATION_POINTS)) {
                for (Vector2 penetrationPoint : worldContainer.getPenetrationPoints()) {
                    PopUpRenderer.getInstance().addMessageToCategory(PopUpRenderer.PopUpCategories.HEAL, new Message(penetrationPoint.toString(), penetrationPoint, WorldController.worldTime, PopUpRenderer.INFINITE_MESSAGE));
                }
            }
            if(isPressed(TOGGLE_LOGGING))
                AwesomeJumperMain.toggleLogging();
            if(isPressed(RAY_CASTING)) {
                if(!player.getWeaponInventory().isHoldingAWeapon())
                    return;
                Ray r = new Ray(player.getWeaponInventory().getSelectedWeapon().getBody().getCenter(), player.getWeaponInventory().getSelectedWeapon().getBody().getOrientation().cpy(), Ray.INFINITE);

                worldContainer.rayCast(r);
            }
        }
        // RELOAD
        // acts like toggling a button, pressed once and processed only once
        if(isPressed(RELOAD)) {
            Utils.log("RELOADING.");
            player.getWeaponInventory().reload();
        }

        if(isPressed(FULLSCREEN)) {
            Graphics.DisplayMode mode = Gdx.graphics.getDisplayMode();
            if(Gdx.graphics.isFullscreen())
                Gdx.graphics.setWindowedMode(mode.width,mode.height);
            else
                Gdx.graphics.setFullscreenMode(mode);
        }

        if(isPressed(TOGGLE_SPECIAL)) {
            Lootable l = new Lootable(mouse.cpy());
            l.register();

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


