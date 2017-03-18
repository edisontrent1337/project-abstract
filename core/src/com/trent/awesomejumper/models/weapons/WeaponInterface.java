package com.trent.awesomejumper.models.weapons;

import com.trent.awesomejumper.engine.modelcomponents.weapons.WeaponComponent;

/**
 * Created by Sinthu on 20.08.2016.
 */
public interface WeaponInterface {

    void fire();
    void setEquipped(boolean equipped);
    void repair();

    WeaponComponent getWeaponComponent();


}
