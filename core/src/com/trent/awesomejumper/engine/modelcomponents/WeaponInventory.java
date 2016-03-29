package com.trent.awesomejumper.engine.modelcomponents;

import com.badlogic.gdx.math.Vector2;
import com.trent.awesomejumper.engine.entity.Entity;
import com.trent.awesomejumper.engine.modelcomponents.popups.Message;
import com.trent.awesomejumper.engine.modelcomponents.popups.PopUpFeed;
import com.trent.awesomejumper.exceptions.InvalidWeaponSlotException;
import com.trent.awesomejumper.models.weapons.Weapon;

import static com.trent.awesomejumper.utils.Utilities.angle;
import static com.trent.awesomejumper.utils.Utilities.checkNotNull;
import static com.trent.awesomejumper.utils.Utilities.getNormal;

/**
 * WeaponSlot component for all entities that can carry weapons. Entities that want to attack in any
 * form need an instance of this ModelComponent.
 * Holds a reference to all weapon entities that are held by the owner of this weapon inventory.
 * Created by Sinthu on 09.12.2015.
 */
public class WeaponInventory extends ModelComponent {

    // MEMBERS & INSTANCES
    // ---------------------------------------------------------------------------------------------
    public enum Slot{
        PRIMARY(0),
        SECONDARY(1);

        private int slot;
        Slot(int slot) {
            this.slot = slot;
        }
    }

    private Weapon weaponPrimary;       // primary wapon equipped on this slot
    private Weapon weaponSecondary;     // secondary wapon equipped on this slot
    private int numberOfSlots;            // number of weapon slots this inventory has

    private boolean primaryEquipped = false, secondaryEquipped = false;

    // CONSTRUCTOR
    // ---------------------------------------------------------------------------------------------

    public WeaponInventory(Entity entity, int slots) {
        if(slots < 0 || slots > 2) {
            throw new IllegalArgumentException("The number of slots must be 1 or 2. It was:" + slots);
        }

        this.entity = entity;
        this.numberOfSlots = slots;
        entity.hasWeaponInventory = true;
    }

    // METHODS & FUNCTIONS
    // ---------------------------------------------------------------------------------------------

    public void fire(boolean slot1, boolean slot2) {

        /**
         * Checking whether weapons have been equipped properly before trying to fire.
         */

        if(slot1 && primaryEquipped) {
            checkNotNull("Weaponslot 1 is empty. weapon is null", weaponPrimary);
            weaponPrimary.getWeaponComponent().fire();
        }
        if(slot2 && secondaryEquipped) {
            checkNotNull("Weaponslot 2 is empty. weapon is null", weaponSecondary);
            weaponSecondary.getWeaponComponent().fire();
        }

    }

    public void reload() {
        if(primaryEquipped)
            weaponPrimary.getWeaponComponent().reload();
        if(secondaryEquipped)
            weaponSecondary.getWeaponComponent().reload();
    }

    public boolean equipWeapon(Entity weapon, int slot) throws InvalidWeaponSlotException {
        if(slot > 2 || slot < 1) {
            throw new InvalidWeaponSlotException("The weapon slot" + slot + "is invalid.");
        }

        if(slot == 1 &! primaryEquipped) {
            weaponPrimary = (Weapon) weapon;
            weaponPrimary.getBody().setPosition(entity.getPosition().cpy());
            weaponPrimary.getBody().disableCollisionDetection();
            primaryEquipped = true;
            entity.getPopUpFeed().addMessageToCategory(PopUpFeed.PopUpCategories.MISC,
                    new Message("NEW WEAPON: " + weaponPrimary.getWeaponComponent().getWeaopnName(), entity.time, 2.00f));
            weaponPrimary.setOwner(entity);
            return primaryEquipped;
        }
        else if(slot == 2 &! secondaryEquipped){
            weaponSecondary = (Weapon) weapon;
            weaponSecondary.setPosition(entity.getPosition().cpy());
            weaponSecondary.getBody().disableCollisionDetection();
            secondaryEquipped = true;
            entity.getPopUpFeed().addMessageToCategory(PopUpFeed.PopUpCategories.MISC,
                    new Message("NEW WEAPON: " + weaponPrimary.getWeaponComponent().getWeaopnName(), entity.time, 2.00f));
            weaponSecondary.setOwner(entity);
            return secondaryEquipped;
        }
        return false;

    }

    public Weapon getWeaponPrimary() {
        return weaponPrimary;
    }

    public Weapon getWeaponSecondary() {
        return weaponSecondary;
    }

    public boolean isPrimaryEquipped() {
        return primaryEquipped;
    }

    public boolean isSecondaryEquipped() {
        return secondaryEquipped;
    }


    public void updateWeaponPositions() {
        if(primaryEquipped) {
            weaponPrimary.getBody().setPosition(entity.getBody().getCenter().cpy().sub(weaponPrimary.getBody().getHalfDimensions()));
            weaponPrimary.getBody().setAimReference(entity.getBody().getAimReference());
            weaponPrimary.getBody().setOrientation(entity.getBody().getOrientation());
            weaponPrimary.getBody().setAngleOfRotation(angle(weaponPrimary.getBody().getOrientation()));
            Vector2 circle = entity.getBody().getOrientation().cpy();
            if(circle.len2() > 0.75f)
                circle.nor().scl(0.75f);
            weaponPrimary.getBody().getPosition().add(circle);
        }
        if(secondaryEquipped) {
            weaponSecondary.getBody().setPosition(entity.getBody().getCenter().cpy().sub(weaponSecondary.getBody().getHalfDimensions()));
            weaponPrimary.getBody().setAimReference(entity.getBody().getAimReference());
            weaponSecondary.getBody().setOrientation(entity.getBody().getOrientation());
            weaponSecondary.getBody().setAngleOfRotation(angle(weaponSecondary.getBody().getOrientation()));
            Vector2 circle = new Vector2(getNormal(entity.getBody().getOrientation()));
            if(circle.len2() > 0.75f)
                circle.nor().scl(0.75f);
            weaponSecondary.getBody().getPosition().add(circle);
        }
    }


    /**
     * Drops the weapon equipped in the specified slot back to the world as a pickup.
     * Re-enables collision detection and resets texture orientation to 0.
     * @param slot
     * @return
     */
    public boolean dropWeapon(int slot) {

        if(slot == 1 && primaryEquipped) {
            primaryEquipped = false;
            weaponPrimary.register();
            weaponPrimary.setOwner(weaponPrimary);
            return true;
        }
        else if(slot == 2 && secondaryEquipped) {
            secondaryEquipped = false;
            weaponSecondary.register();
            weaponSecondary.setOwner(weaponSecondary);
            return true;
        }

        return false;
    }

}
