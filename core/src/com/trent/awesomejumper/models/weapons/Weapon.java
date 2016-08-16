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


    public Weapon(final Weapon weapon) {
        this.body = weapon.getBody();
        this.graphics = weapon.getGraphics();
        this.weaponComponent = weapon.getWeaponComponent();
        this.type = weapon.getType();
    }

    public WeaponComponent getWeaponComponent() {
        return weaponComponent;
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


    public void setEquipped(boolean equipped) {
        weaponComponent.setEquipped(equipped);
    }
    public boolean isEquipped() {
        return weaponComponent.isEquipped();
    }


}
