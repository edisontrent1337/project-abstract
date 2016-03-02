package com.trent.awesomejumper.engine.modelcomponents;

import com.badlogic.gdx.math.Vector2;
import com.trent.awesomejumper.engine.entity.Entity;
import com.trent.awesomejumper.exceptions.InvalidWeaponSlotException;
import com.trent.awesomejumper.models.weapons.Weapon;

import static com.trent.awesomejumper.utils.Utilities.angle;
import static com.trent.awesomejumper.utils.Utilities.sub;

/**
 * Created by Sinthu on 09.12.2015.
 */
public class WeaponSlots extends ModelComponent {


    private Weapon weaponPrimary;       // primary wapon equipped on this slot
    private Weapon weaponSecondary;     // secondary wapon equipped on this slot

    private final float PRIMARY_XOFFSET = - 0.5f;
    private final float SECONDARY_XOFFSET =  1.5f;

    private boolean primaryEquipped = false, secondaryEquipped = false;

    public WeaponSlots(Entity entity) {
        this.entity = entity;
        entity.hasWeaponSlot = true;
    }


    public void fire(boolean slot1, boolean slot2) {


        if(slot1 && primaryEquipped) {
            weaponPrimary.getWeaponComponent().fire(weaponPrimary.getBody().getOrientation());
        }
        if(slot2 && secondaryEquipped) {
            weaponSecondary.getWeaponComponent().fire(weaponSecondary.getBody().getOrientation());
        }

    }

    public void reload() {
        if(primaryEquipped)
            weaponPrimary.getWeaponComponent().reload();
        if(secondaryEquipped)
            weaponSecondary.getWeaponComponent().reload();
    }

    public void equipWeapon(Entity weapon, int slot) throws InvalidWeaponSlotException {
        if(slot > 2 || slot < 1) {
            throw new InvalidWeaponSlotException("The weapon slot" + slot + "is invalid.");
        }

        if(slot == 1) {
            weaponPrimary = (Weapon) weapon;
            weaponPrimary.getBody().setPosition(entity.getPosition().cpy().x - 1f, entity.getPosition().y);
            primaryEquipped = true;
        }
        else {
            weaponSecondary = (Weapon) weapon;
            weaponSecondary.setPosition(entity.getPosition().cpy());
            secondaryEquipped = true;
        }


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
            weaponPrimary.getBody().setOrientation(sub(weaponPrimary.getPosition(),entity.getBody().getReference()));
            weaponPrimary.getBody().setAngleOfRotation(angle(weaponPrimary.getBody().getOrientation()));
            weaponPrimary.getBody().setPosition(entity.getPosition().cpy().x + PRIMARY_XOFFSET, entity.getPosition().cpy().y);
        }
        if(secondaryEquipped) {
            weaponSecondary.getBody().setOrientation(sub(weaponSecondary.getPosition(),entity.getBody().getReference()));
            weaponSecondary.getBody().setAngleOfRotation(angle(weaponSecondary.getBody().getOrientation()));
            weaponSecondary.getBody().setPosition(entity.getPosition().cpy().x + SECONDARY_XOFFSET, entity.getPosition().cpy().y);
        }
    }

}
