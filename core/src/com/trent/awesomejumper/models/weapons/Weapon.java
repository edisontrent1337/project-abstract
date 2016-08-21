package com.trent.awesomejumper.models.weapons;

import com.trent.awesomejumper.engine.entity.Entity;
import com.trent.awesomejumper.engine.modelcomponents.weapons.GunComponent;

/**
 * Created by Sinthu on 01.03.2016.
 */
public class Weapon extends Entity implements WeaponInterface {

    protected static final float EQUIP_TIMEOUT = 1.5f;

    public Weapon() {

    }


    @Override
    public void update(float delta) {
        super.update(delta);

        /**
         * Resets the owner of this weapon after 1.5 seconds of being unequipped so it can
         * be grabbed and equipped again.
         * TODO: implement weapon pickup with the press of a button
         */
        if (time - registerTime > EQUIP_TIMEOUT && !gunComponent.isEquipped()) {
            setOwner(this);
        }

    }


    // METHODS & FUNCTIONS
    @Override
    public void fire() {
        gunComponent.fire();
    }



    @Override
    public void setEquipped(boolean equipped) {

        gunComponent.setEquipped(equipped);
    }

    @Override
    public void repair() {

    }


    // GETTER AND SETTER
    // ---------------------------------------------------------------------------------------------

    public GunComponent getWeaponComponent() {
        return gunComponent;
    }

    public String getStatus() {
        return gunComponent.getStatus();
    }

    public String getName() {
        return gunComponent.getName();
    }

    public boolean isEquipped() {
        return gunComponent.isEquipped();
    }


}
