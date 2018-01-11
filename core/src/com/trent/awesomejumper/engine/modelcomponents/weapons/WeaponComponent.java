package com.trent.awesomejumper.engine.modelcomponents.weapons;

import com.badlogic.gdx.math.Vector2;
import com.trent.awesomejumper.engine.modelcomponents.ModelComponent;
import com.trent.awesomejumper.engine.physics.ProjectileRay;
import com.trent.awesomejumper.models.weapons.WeaponInterface;

import java.util.HashSet;

/**
 * Created by Sinthu on 20.08.2016.
 */
public abstract class WeaponComponent extends ModelComponent {
    public void fire() {

    }


    protected abstract HashSet<ProjectileRay> buildRays();

    public void setEquipped(boolean equipped) {

    }

    public void repair() {

    }
}
