package com.trent.awesomejumper.engine.modelcomponents;

import com.badlogic.gdx.math.Vector2;
import com.trent.awesomejumper.engine.entity.Entity;
import com.trent.awesomejumper.models.testing.Projectile;

/**
 * Created by Sinthu on 09.12.2015.
 */
public class Weapon extends ModelComponent {

    private int ammo = 100;
    private final int CLIP_SIZE = 25;
    private int clips = ammo / CLIP_SIZE;
    private final float PROJECTILE_SPEED = 20f;

    public Weapon(Entity entity) {
        super(entity);
    }


    // TODO this method should not return a projectile.
    public Projectile fire() {
        ammo--;
        if(ammo % CLIP_SIZE == 0) {
            clips--;
        }

        //TODO overhaul way projectiles are created.

        Projectile projectile = new Projectile(entity.getPosition().cpy(),0.7f);
        Vector2 orientation = new Vector2(0f,0f);
        orientation.x = entity.getBody().getOrientation().cpy().x;
        orientation.y =entity.getBody().getOrientation().cpy().y - 0.7f;
        orientation.nor().scl(PROJECTILE_SPEED);
        projectile.getBody().setVelocity(orientation);
        projectile.getBody().setAngleOfRotation(entity.getBody().getAngleOfRotation());

        return projectile;
    }

}
