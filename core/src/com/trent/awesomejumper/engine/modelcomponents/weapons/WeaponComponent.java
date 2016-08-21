package com.trent.awesomejumper.engine.modelcomponents.weapons;

import com.trent.awesomejumper.engine.modelcomponents.ModelComponent;
import com.trent.awesomejumper.models.weapons.WeaponInterface;

/**
 * Created by Sinthu on 20.08.2016.
 */
public abstract class WeaponComponent extends ModelComponent implements WeaponInterface {
    @Override
    public void fire() {

    }


    @Override
    public void setEquipped(boolean equipped) {

    }

    @Override
    public void repair() {

    }
}
