package com.trent.awesomejumper.models.weapons;

import com.trent.awesomejumper.engine.entity.Entity;
import com.trent.awesomejumper.engine.modelcomponents.WeaponComponent;

/**
 * Created by Sinthu on 01.03.2016.
 */
public class Weapon extends Entity {

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
        if(time - registerTime > EQUIP_TIMEOUT && !weaponComponent.isEquipped()) {
            setOwner(this);
        }

    }

    // GETTER AND SETTER
    // ---------------------------------------------------------------------------------------------

    public WeaponComponent getWeaponComponent() {
        return weaponComponent;
    }

    public String getStatus() {
        return  weaponComponent.getStatus();
    }

    public String getName() {
        return weaponComponent.getName();
    }

    public void setEquipped(boolean equipped) {
        weaponComponent.setEquipped(equipped);
    }
    public boolean isEquipped() {
        return weaponComponent.isEquipped();
    }


}
