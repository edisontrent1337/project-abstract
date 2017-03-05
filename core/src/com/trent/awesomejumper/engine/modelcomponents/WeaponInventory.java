package com.trent.awesomejumper.engine.modelcomponents;

import com.badlogic.gdx.math.Vector2;
import com.trent.awesomejumper.controller.entitymanagement.EntityManager;
import com.trent.awesomejumper.controller.rendering.PopUpRenderer;
import com.trent.awesomejumper.engine.entity.Entity;
import com.trent.awesomejumper.engine.modelcomponents.popups.Message;
import com.trent.awesomejumper.models.weapons.Weapon;
import com.trent.awesomejumper.utils.Utils;

import static com.trent.awesomejumper.controller.rendering.PopUpRenderer.PopUpCategories.*;

import java.util.ArrayList;

/**
 * Weapon inventory component for all entities that can carry weapons. Entities that want to attack in any
 * form need an instance of this ModelComponent.
 * Holds a reference to all weapon entities that are held by the owner of this weapon inventory.
 * Created by Sinthu on 09.12.2015.
 */
public class WeaponInventory extends ModelComponent {

    // MEMBERS & INSTANCES
    // ---------------------------------------------------------------------------------------------

    private Weapon selectedWeapon;               // currently selected weapon entity
    private static int MAX_SLOTS = 3;            // number of weapon slots this inventory has
    private static final int NO_WEAPON = -1;     // constant representing id of no weapon

    private final float WEAPON_DISTANCE = 0.50f;

    private ArrayList<Integer> weapons;

    private boolean holdingAWeapon = false;
    private int inventoryPointer = 0;           // points to the weapon currently being held
    private int weaponsEquipped = 0;

    private int selectedWeaponID = NO_WEAPON;

    public float equipTime = 0f;
    private boolean inventoryFull = false;

    // CONSTRUCTOR
    // ---------------------------------------------------------------------------------------------

    public WeaponInventory(Entity entity) {
        this.entity = entity;
        this.weapons = new ArrayList<>(MAX_SLOTS);
        entity.enableComponent(ComponentID.WEAPON_INVENTORY);

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

       /* Utils.log("EVENT", "START OF EQUIP");
        Utils.log("WEAPONS EQUIPPED", Integer.toString(weaponsEquipped));
        Utils.log("INVENTORY POINTER", Integer.toString(inventoryPointer));
        Utils.log("CURRENT WEAPON ID", Integer.toString(selectedWeaponID));*/

        /**
         * Every weapon picked up will have its collision detection disabled,
         * owner set to the entity that owns this weapon inventory and set its equipped flag to true.
         */
        weapon.getBody().disableCollisionDetection();
        weapon.setOwner(entity);
        weapon.setEquipped(true);

        // If the inventory is empty
        if (weaponsEquipped == 0) {
            inventoryPointer = 0;
            selectedWeapon = weapon;
            selectedWeaponID = weapon.getID();
            selectedWeapon.getBody().setPosition(entity.getPosition().cpy());
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
            // Iterating through all slots to find an empty one.
            for (int slot = 0; slot < MAX_SLOTS; slot++) {
                // If the current slot is empty, save the weapon here and hide it.
                if (weapons.get(slot) == NO_WEAPON) {
                    weapon.hide();
                    saveWeapon(weapon.getID(), slot);
                }
            }
            weaponsEquipped++;

            /**
             * If the inventory is full after the weapon was picked up, the first weapon is
             * auto equipped and shown.
             */
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
            holdingAWeapon = true;
            weaponsEquipped++;
        }
        /*Utils.log("EVENT", "END OF EQUIP");
        Utils.log("WEAPONS EQUIPPED", Integer.toString(weaponsEquipped));
        Utils.log("INVENTORY POINTER", Integer.toString(inventoryPointer));
        Utils.log("CURRENT WEAPON ID", Integer.toString(selectedWeaponID));*/

        Message equipMessage = new Message(weapon.getName(), entity.getBody().getCenter(), entity.time, 1.00f);
        PopUpRenderer.getInstance().addMessageToCategory(MISC, equipMessage);

        equipTime = entity.time;
        if (weaponsEquipped == MAX_SLOTS)
            inventoryFull = true;


        return false;

    }

    /**
     * Executed when the player changes to another weapon.
     *
     * @param direction positive means previous weapon, negative means next weapon.
     */
    public void changeWeapon(int direction) {
        Utils.log("EVENT", "START OF CHANGE");
        Utils.log("WEAPONS EQUIPPED", Integer.toString(weaponsEquipped));
        Utils.log("INVENTORY", Integer.toString(inventoryPointer));
        Utils.log("CURRENT WEAPON ID", Integer.toString(selectedWeaponID));
        if (direction < 0)
            direction = -1;
        else
            direction = 1;

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
        } else {
            // The player has no weapon in his hand.
            holdingAWeapon = false;
            selectedWeapon = null;
        }

        /*Utils.log("EVENT", "END OF CHANGE");
        Utils.log("WEAPONS EQUIPPED", Integer.toString(weaponsEquipped));
        Utils.log("INVENTORY POINTER", Integer.toString(inventoryPointer));
        Utils.log("CURRENT WEAPON ID", Integer.toString(selectedWeaponID));*/

    }

    private void saveWeapon(int id, int currentSlot) {
        if (!weapons.contains(id)) {
            weapons.set(currentSlot, id);

        }
        // Utils.log("SAVED WEAPONS", "LIST");
        for (Integer i : weapons) {
            // Utils.log("WEAPON ID", Integer.toString(i));
        }

    }


    public Weapon getSelectedWeapon() {
        return selectedWeapon;
    }


    public boolean isHoldingAWeapon() {
        return holdingAWeapon;
    }


    public void updateWeaponPositions() {

        Vector2 weaponDirection = entity.getBody().getOrientation().cpy();
        weaponDirection.nor().scl(WEAPON_DISTANCE);

        for (Integer weaponID : weapons) {
            if (weaponID == NO_WEAPON)
                continue;
            Weapon w = (Weapon) EntityManager.getInstance().getEntityByID(weaponID);
            w.getBody().setPosition(entity.getBody().getCenter().cpy().sub(w.getBody().getHalfDimensions()));
            w.getBody().setAimReference(entity.getBody().getAimReference());
            w.getBody().setOrientation(entity.getBody().getOrientation());
            w.getBody().setAngleOfRotation(entity.getBody().getAngleOfRotation());
            w.getBody().getPosition().add(weaponDirection);
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
                /*Utils.log("EVENT", "START OF DROP");
                Utils.log("NUMBER OF WEAPONS EQUIPPED", Integer.toString(weaponsEquipped));
                Utils.log("INVENTORY", Integer.toString(inventoryPointer));
                Utils.log("CURRENT WEAPON ID", Integer.toString(selectedWeaponID));*/

            /**
             * Get the weapon to be dropped by the entity manager, un-equip it and drop it to the world.
             *
             */
            Weapon drop = (Weapon) EntityManager.getInstance().getEntityByID(selectedWeaponID);
            if (drop != null) {
                drop.setEquipped(false);
                drop.register();
               /* Message message = new Message(drop.getPosition().toString(),drop.getBody().getCenter(),drop.time,3.00f);
                PopUpRenderer.getInstance().addMessageToCategory(MISC,message);*/
                weapons.set(inventoryPointer, NO_WEAPON);
                weaponsEquipped--;
                holdingAWeapon = false;
                selectedWeaponID = NO_WEAPON;
                selectedWeapon = null;
            } else {
                throw new NullPointerException("ERROR:  THE WEAPON WAS NOT FOUND BY THE ENTITY MANAGER.");
            }

               /* Utils.log("EVENT", "END OF DROP");
                Utils.log("NUMBER OF WEAPONS EQUIPPED", Integer.toString(weaponsEquipped));
                Utils.log("INVENTORY", Integer.toString(inventoryPointer));
                Utils.log("CURRENT WEAPON ID", Integer.toString(selectedWeaponID));*/
            inventoryFull = false;
        }

    }

    public int getWeaponsEquipped() {
        return weaponsEquipped;
    }

    public String getInventoryStatus() {
        if (!holdingAWeapon)
            return "NO WEAPON";
        else
            return selectedWeapon.getWeaponComponent().getStatus();
    }

    public boolean isInventoryFull() {
        return inventoryFull;
    }

    public void increaseCapacity() {
        MAX_SLOTS++;
    }


}
