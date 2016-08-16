package com.trent.awesomejumper.engine.modelcomponents;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.trent.awesomejumper.controller.EntityManager;
import com.trent.awesomejumper.controller.WorldController;
import com.trent.awesomejumper.engine.entity.Entity;
import com.trent.awesomejumper.models.weapons.Weapon;

import java.util.ArrayList;

import static com.trent.awesomejumper.utils.Utilities.angle;

/**
 * Weapon inventory component for all entities that can carry weapons. Entities that want to attack in any
 * form need an instance of this ModelComponent.
 * Holds a reference to all weapon entities that are held by the owner of this weapon inventory.
 * Currently designed to manage 2 weapon slots.
 * //TODO: change so that only one weapon can be hold at a time.
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

    private Weapon currentWeapon;       // primary weapon equipped on this slot
    //private int currentWeapon;       // primary weapon equipped on this slot
    private Weapon weaponSecondary;     // secondary weapon equipped on this slot
    private static final int MAX_SLOTS = 3;            // number of weapon slots this inventory has
    private static final int NO_WEAPON = -1;

    private ArrayList<Integer> weapons;

    private boolean weaponInHand = false, secondarySlotInUse = false;
    private int inventoryPointer = 0;
    private int weaponsEquipped = 0;

    private int currentWeaponID = NO_WEAPON;
    private boolean changing = false;

    // CONSTRUCTOR
    // ---------------------------------------------------------------------------------------------

    public WeaponInventory(Entity entity, int slots) {
        if (slots < 0 || slots > MAX_SLOTS) {
            throw new IllegalArgumentException("The number of slots must be 1 or 2. It was:" + slots);
        }

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

        if (weaponInHand) {
            currentWeapon.getWeaponComponent().fire();
        }

    }

    public void reload() {
        if (weaponInHand)
            currentWeapon.getWeaponComponent().reload();

    }

    /**
     * Executed when a weapon is picked up.
     *TODO: DOCUMENTATION.
     * TODO: IMPLEMENT PICKUP BY PRESSING AND HOLDING A BUTTON
     * @param weapon Weapon entity to be equipped
     * @return true, if weapon was equipped successfully
     */
    public boolean equipWeapon(Weapon weapon) {
        Gdx.app.log("EVENT", "START OF EQUIP");
        Gdx.app.log("WEAPONS EQUIPPED", Integer.toString(weaponsEquipped));
        Gdx.app.log("INVENTORY POINTER", Integer.toString(inventoryPointer));
        Gdx.app.log("CURRENT WEAPON ID", Integer.toString(currentWeaponID));


        // If the inventory is empty
        if (weaponsEquipped == 0) {
            inventoryPointer = 0;
            currentWeapon = weapon;
            currentWeaponID = weapon.getID();
            currentWeapon.getBody().disableCollisionDetection();
            currentWeapon.getBody().setPosition(entity.getPosition().cpy());
            currentWeapon.setOwner(entity);
            currentWeapon.setEquipped(true);
            saveWeapon(currentWeaponID, inventoryPointer);
            weaponInHand = true;
            weaponsEquipped++;
        }
        // If the inventory contains some weapons
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
            if(weaponsEquipped == MAX_SLOTS) {
                currentWeaponID = weapons.get(inventoryPointer);
                currentWeapon = (Weapon) EntityManager.getInstance().getEntityByID(currentWeaponID);
                currentWeapon.show();
                currentWeapon.getBody().disableCollisionDetection();
                currentWeapon.getBody().setPosition(entity.getPosition().cpy());
                currentWeapon.setOwner(entity);
                currentWeapon.setEquipped(true);
                weaponInHand = true;
            }

        }

        //If the inventory is full
        else if (weaponsEquipped == MAX_SLOTS) {
            dropWeapon();
            currentWeapon = weapon;
            currentWeaponID = weapon.getID();
            saveWeapon(currentWeaponID, inventoryPointer);
            currentWeapon.show();
            currentWeapon.getBody().setPosition(entity.getPosition().cpy());
            currentWeapon.getBody().disableCollisionDetection();
            currentWeapon.setOwner(entity);
            currentWeapon.setEquipped(true);
            weaponInHand = true;
            weaponsEquipped++;
        }
        Gdx.app.log("EVENT", "END OF EQUIP");
        Gdx.app.log("WEAPONS EQUIPPED", Integer.toString(weaponsEquipped));
        Gdx.app.log("INVENTORY POINTER", Integer.toString(inventoryPointer));
        Gdx.app.log("CURRENT WEAPON ID", Integer.toString(currentWeaponID));
        return false;

    }

    /**
     * TODO: get ammo and status of new weapon
     * //* @param direction
     */
    public void changeWeapon(int direction) {
        Gdx.app.log("EVENT", "START OF CHANGE");
        Gdx.app.log("WEAPONS EQUIPPED", Integer.toString(weaponsEquipped));
        Gdx.app.log("INVENTORY", Integer.toString(inventoryPointer));
        Gdx.app.log("CURRENT WEAPON ID", Integer.toString(currentWeaponID));
        if (direction < 0)
            direction = -1;
        else
            direction = 1;

        saveWeapon(currentWeaponID, inventoryPointer);
        if (currentWeaponID != NO_WEAPON) {
            currentWeapon.hide();
        }
        inventoryPointer = (inventoryPointer + direction + MAX_SLOTS) % MAX_SLOTS;
        currentWeaponID = weapons.get(inventoryPointer);
        if (currentWeaponID != NO_WEAPON) {
            currentWeapon = (Weapon) EntityManager.getInstance().getEntityByID(currentWeaponID);
            currentWeapon.show();
            currentWeapon.getBody().disableCollisionDetection();
            currentWeapon.getBody().setPosition(entity.getPosition().cpy());
            currentWeapon.setOwner(entity);
            weaponInHand = true;
        }
        else {
            weaponInHand = false;
        }
        Gdx.app.log("EVENT", "END OF CHANGE");
        Gdx.app.log("WEAPONS EQUIPPED", Integer.toString(weaponsEquipped));
        Gdx.app.log("INVENTORY POINTER", Integer.toString(inventoryPointer));
        Gdx.app.log("CURRENT WEAPON ID", Integer.toString(currentWeaponID));

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


    public Weapon getCurrentWeapon() {
        return currentWeapon;
    }

    public Weapon getWeaponSecondary() {
        return weaponSecondary;
    }

    public boolean isWeaponInHand() {
        return weaponInHand;
    }

    public boolean isSecondarySlotInUse() {
        return secondarySlotInUse;
    }


    public void updateWeaponPositions() {
        if (currentWeaponID != NO_WEAPON) {
            currentWeapon = (Weapon) EntityManager.getInstance().getEntityByID(currentWeaponID);
            currentWeapon.getBody().setPosition(entity.getBody().getCenter().cpy().sub(currentWeapon.getBody().getHalfDimensions()));
            currentWeapon.getBody().setAimReference(entity.getBody().getAimReference());
            currentWeapon.getBody().setOrientation(entity.getBody().getOrientation());
            currentWeapon.getBody().setAngleOfRotation(angle(currentWeapon.getBody().getOrientation()));
            Vector2 circle = entity.getBody().getOrientation().cpy();
            if (circle.len2() > 0.5f)
                circle.nor().scl(0.5f);
            currentWeapon.getBody().getPosition().add(circle);
        }

    }


    /**
     * Drops the weapon equipped in the specified slot back to the world as a pickup.
     * Re-enables collision detection and resets texture orientation to 0.
     *
     */
    public void dropWeapon() {



        if (currentWeaponID != NO_WEAPON) {
            Gdx.app.log("EVENT", "START OF DROP");
            Gdx.app.log("WEAPONS EQUIPPED", Integer.toString(weaponsEquipped));
            Gdx.app.log("INVENTORY", Integer.toString(inventoryPointer));
            Gdx.app.log("CURRENT WEAPON ID", Integer.toString(currentWeaponID));

            Weapon drop = (Weapon) EntityManager.getInstance().getEntityByID(currentWeaponID);
            drop.setEquipped(false);
            drop.register();
            weapons.set(inventoryPointer, NO_WEAPON);
            weaponsEquipped--;
            weaponInHand = false;
            currentWeaponID = NO_WEAPON;

            Gdx.app.log("EVENT", "END OF DROP");
            Gdx.app.log("WEAPONS EQUIPPED", Integer.toString(weaponsEquipped));
            Gdx.app.log("INVENTORY", Integer.toString(inventoryPointer));
            Gdx.app.log("CURRENT WEAPON ID", Integer.toString(currentWeaponID));

        }

    }


    public String getInventoryStatus() {
        if (!weaponInHand)
            return "NO WEAPON";
        else
            return currentWeapon.getWeaponComponent().getWeaponStatus();
    }


}
