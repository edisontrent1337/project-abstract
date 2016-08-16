package com.trent.awesomejumper.engine.modelcomponents;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.trent.awesomejumper.controller.EntityManager;
import com.trent.awesomejumper.controller.PopUpManager;
import com.trent.awesomejumper.engine.entity.Entity;
import com.trent.awesomejumper.engine.modelcomponents.popups.Message;
import com.trent.awesomejumper.models.weapons.Weapon;

import static com.trent.awesomejumper.controller.PopUpManager.PopUpCategories.*;

import java.util.ArrayList;

import static com.trent.awesomejumper.utils.Utilities.angle;

/**
 * Weapon inventory component for all entities that can carry weapons. Entities that want to attack in any
 * form need an instance of this ModelComponent.
 * Holds a reference to all weapon entities that are held by the owner of this weapon inventory.
 * Created by Sinthu on 09.12.2015.
 */
public class WeaponInventory extends ModelComponent {

    // MEMBERS & INSTANCES
    // ---------------------------------------------------------------------------------------------
    public enum Slot {
        PRIMARY(0),
        SECONDARY(1);

        private int slot;

        Slot(int slot) {
            this.slot = slot;
        }
    }

    private Weapon selectedWeapon;               // currently selected weapon entity
    private static int MAX_SLOTS = 3;            // number of weapon slots this inventory has
    private static final int NO_WEAPON = -1;     // constant representing id of no weapon

    private ArrayList<Integer> weapons;

    private boolean holdingAWeapon = false;
    private int inventoryPointer = 0;
    private int weaponsEquipped = 0;

    private int selectedWeaponID = NO_WEAPON;

    // CONSTRUCTOR
    // ---------------------------------------------------------------------------------------------

    public WeaponInventory(Entity entity) {
        this.entity = entity;
        this.weapons = new ArrayList<>(MAX_SLOTS);
        entity.hasWeaponInventory = true;

        for (int i = 0; i < MAX_SLOTS; i++) {
            weapons.add(NO_WEAPON);
        }

    }

    // ---------------------------------------------------------------------------------------------
    // METHODS & FUNCTIONS
    // ---------------------------------------------------------------------------------------------

    public void fire() {

        /**
         * Checking whether weapons have been equipped properly before trying to fire.
         */

        if (holdingAWeapon) {
            selectedWeapon.getWeaponComponent().fire();
        }

    }

    public void reload() {

        /**
         * Checking whether weapons have been equipped properly before trying to reload.
         */
        if (holdingAWeapon)
            selectedWeapon.getWeaponComponent().reload();

    }

    /**
     * Executed when a weapon is picked up.
     * TODO: IMPLEMENT PICKUP BY PRESSING AND HOLDING A BUTTON
     *
     * @param weapon Weapon entity to be equipped
     * @return true, if weapon was equipped successfully
     */
    public boolean equipWeapon(Weapon weapon) {
        Gdx.app.log("EVENT", "START OF EQUIP");
        Gdx.app.log("WEAPONS EQUIPPED", Integer.toString(weaponsEquipped));
        Gdx.app.log("INVENTORY POINTER", Integer.toString(inventoryPointer));
        Gdx.app.log("CURRENT WEAPON ID", Integer.toString(selectedWeaponID));


        // If the inventory is empty
        if (weaponsEquipped == 0) {
            inventoryPointer = 0;
            selectedWeapon = weapon;
            selectedWeaponID = weapon.getID();
            selectedWeapon.getBody().disableCollisionDetection();
            selectedWeapon.getBody().setPosition(entity.getPosition().cpy());
            selectedWeapon.setOwner(entity);
            selectedWeapon.setEquipped(true);
            saveWeapon(selectedWeaponID, inventoryPointer);
            holdingAWeapon = true;
            weaponsEquipped++;
        }
        /**
         * If the inventory contains some weapons, the weapon picked up is hidden and then saved in
         * the inventory.
         *
         */
        else if (weaponsEquipped < MAX_SLOTS) {
            for (int i = 0; i < MAX_SLOTS; i++) {
                if (weapons.get(i) == NO_WEAPON) {
                    weapon.setOwner(entity);
                    weapon.setEquipped(true);
                    weapon.hide();
                    saveWeapon(weapon.getID(), i);
                }
            }
            weaponsEquipped++;
            if (weaponsEquipped == MAX_SLOTS) {
                selectedWeaponID = weapons.get(inventoryPointer);
                selectedWeapon = (Weapon) EntityManager.getInstance().getEntityByID(selectedWeaponID);
                selectedWeapon.show();
                selectedWeapon.getBody().disableCollisionDetection();
                selectedWeapon.getBody().setPosition(entity.getPosition().cpy());
                selectedWeapon.setOwner(entity);
                selectedWeapon.setEquipped(true);
                holdingAWeapon = true;
            }

        }

        /**
         * If the inventory is full, we drop the current weapon, the new current weapon becomes
         * the weapon to be equipped and it is saved in the inventory.
         *
         */
        else if (weaponsEquipped == MAX_SLOTS) {
            dropWeapon();
            selectedWeapon = weapon;
            selectedWeaponID = weapon.getID();
            saveWeapon(selectedWeaponID, inventoryPointer);
            selectedWeapon.show();
            selectedWeapon.getBody().setPosition(entity.getPosition().cpy());
            // disabeling collision detection as the weapon is now part of the entities inventory.
            selectedWeapon.getBody().disableCollisionDetection();
            selectedWeapon.setOwner(entity);
            selectedWeapon.setEquipped(true);
            holdingAWeapon = true;
            weaponsEquipped++;
        }
        Gdx.app.log("EVENT", "END OF EQUIP");
        Gdx.app.log("WEAPONS EQUIPPED", Integer.toString(weaponsEquipped));
        Gdx.app.log("INVENTORY POINTER", Integer.toString(inventoryPointer));
        Gdx.app.log("CURRENT WEAPON ID", Integer.toString(selectedWeaponID));

        Message equipMessage = new Message(weapon.getName(), entity.getPosition().cpy(), entity.time, 2.00f);
        PopUpManager.getInstance().addMessageToCategory(MISC, equipMessage);

        return false;

    }

    /**
     * Executed when the player changes to another weapon.
     *
     * @param direction positive means previous weapon, negative means next weapon.
     */
    public void changeWeapon(int direction) {
        Gdx.app.log("EVENT", "START OF CHANGE");
        Gdx.app.log("WEAPONS EQUIPPED", Integer.toString(weaponsEquipped));
        Gdx.app.log("INVENTORY", Integer.toString(inventoryPointer));
        Gdx.app.log("CURRENT WEAPON ID", Integer.toString(selectedWeaponID));
        if (direction < 0)
            direction = -1;
        else
            direction = 1;

        Message changeMessage;
        /**
         *  Save the current weapon in the inventory. If the player held a weapon before the change,
         *  this weapon will be hidden.
         */
        saveWeapon(selectedWeaponID, inventoryPointer);
        if (selectedWeaponID != NO_WEAPON) {
            selectedWeapon.hide();
        }
        // Moving to the next slot and getting the saved weapon
        inventoryPointer = (inventoryPointer + direction + MAX_SLOTS) % MAX_SLOTS;
        selectedWeaponID = weapons.get(inventoryPointer);
        /**
         * If the next weapon slot contains a weapon, get it from the entity manager,
         * show it and disable collision detection.
         */
        if (selectedWeaponID != NO_WEAPON) {
            selectedWeapon = (Weapon) EntityManager.getInstance().getEntityByID(selectedWeaponID);
            selectedWeapon.show();
            selectedWeapon.getBody().disableCollisionDetection();
            selectedWeapon.getBody().setPosition(entity.getPosition().cpy());
            selectedWeapon.setOwner(entity);
            holdingAWeapon = true;
            changeMessage = new Message(selectedWeapon.getName(), entity.getPosition().cpy(), entity.time, 2.00f);
        } else {
            // The player has no weapon in his hand.
            holdingAWeapon = false;
            selectedWeapon = null;
            changeMessage = new Message("NO WEAPON", entity.getPosition().cpy(), entity.time, 2.00f);
        }

        PopUpManager.getInstance().addMessageToCategory(MISC, changeMessage);
        Gdx.app.log("EVENT", "END OF CHANGE");
        Gdx.app.log("WEAPONS EQUIPPED", Integer.toString(weaponsEquipped));
        Gdx.app.log("INVENTORY POINTER", Integer.toString(inventoryPointer));
        Gdx.app.log("CURRENT WEAPON ID", Integer.toString(selectedWeaponID));

    }

    private void saveWeapon(int id, int currentSlot) {
        if (!weapons.contains(id)) {
            weapons.set(currentSlot, id);

        }
        Gdx.app.log("SAVED WEAPONS", "LIST");
        for (Integer i : weapons) {
            Gdx.app.log("WEAPON ID", Integer.toString(i));
        }

    }


    public Weapon getSelectedWeapon() {
        return selectedWeapon;
    }


    public boolean isHoldingAWeapon() {
        return holdingAWeapon;
    }


    public void updateWeaponPositions() {
        if (selectedWeaponID != NO_WEAPON) {
            selectedWeapon = (Weapon) EntityManager.getInstance().getEntityByID(selectedWeaponID);
            selectedWeapon.getBody().setPosition(entity.getBody().getCenter().cpy().sub(selectedWeapon.getBody().getHalfDimensions()));
            selectedWeapon.getBody().setAimReference(entity.getBody().getAimReference());
            selectedWeapon.getBody().setOrientation(entity.getBody().getOrientation());
            selectedWeapon.getBody().setAngleOfRotation(angle(selectedWeapon.getBody().getOrientation()));
            Vector2 circle = entity.getBody().getOrientation().cpy();
            if (circle.len2() > 0.5f)
                circle.nor().scl(0.5f);
            selectedWeapon.getBody().getPosition().add(circle);
        }

    }


    /**
     * Drops the weapon equipped in the specified slot back to the world as a pickup.
     * Re-enables collision detection and resets texture orientation to 0.
     */
    public void dropWeapon() {

        /**
         * We can only drop a weapon if we currently hold one.
         */
        if (selectedWeaponID != NO_WEAPON) {
            Gdx.app.log("EVENT", "START OF DROP");
            Gdx.app.log("WEAPONS EQUIPPED", Integer.toString(weaponsEquipped));
            Gdx.app.log("INVENTORY", Integer.toString(inventoryPointer));
            Gdx.app.log("CURRENT WEAPON ID", Integer.toString(selectedWeaponID));

            /**
             * Get the weapon to be dropped by the entity manager, un-equip it and drop it to the world.
             *
             */
            Weapon drop = (Weapon) EntityManager.getInstance().getEntityByID(selectedWeaponID);
            drop.setEquipped(false);
            drop.register();
            weapons.set(inventoryPointer, NO_WEAPON);
            weaponsEquipped--;
            holdingAWeapon = false;
            selectedWeaponID = NO_WEAPON;
            selectedWeapon = null;

            Gdx.app.log("EVENT", "END OF DROP");
            Gdx.app.log("WEAPONS EQUIPPED", Integer.toString(weaponsEquipped));
            Gdx.app.log("INVENTORY", Integer.toString(inventoryPointer));
            Gdx.app.log("CURRENT WEAPON ID", Integer.toString(selectedWeaponID));

        }

    }


    public String getInventoryStatus() {
        if (!holdingAWeapon)
            return "NO WEAPON";
        else
            return selectedWeapon.getWeaponComponent().getStatus();
    }

    public void increaseCapacity() {
        MAX_SLOTS++;
    }


}
