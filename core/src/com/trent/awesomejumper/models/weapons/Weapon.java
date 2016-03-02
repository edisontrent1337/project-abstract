package com.trent.awesomejumper.models.weapons;

import com.trent.awesomejumper.engine.entity.Entity;
import com.trent.awesomejumper.engine.modelcomponents.WeaponComponent;

/**
 * Created by Sinthu on 01.03.2016.
 */
public class Weapon extends Entity {

    public Weapon() {

    }

    public WeaponComponent getWeaponComponent() {
        return weaponComponent;
    }

}
