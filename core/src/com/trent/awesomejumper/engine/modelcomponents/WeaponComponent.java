package com.trent.awesomejumper.engine.modelcomponents;

import com.badlogic.gdx.math.Vector2;
import com.trent.awesomejumper.engine.entity.Entity;
import com.trent.awesomejumper.models.testing.Projectile;

/** // TODO DOCUMENTATION, IMPLEMENT RELOAD TIME
 * Created by Sinthu on 01.03.2016.
 */
public class WeaponComponent extends ModelComponent {

    private int ammo;
    private int clipSize;
    private int clips;
    private int currentClip;
    private float recoverTime, reloadTIme;
    private float speed;
    private float timeFired = 0f;


    public WeaponComponent(Entity weapon) {
        this.entity = weapon;
        this.ammo = 100;
        this.clipSize = 25;
        this.clips = 4;
        this.currentClip = 25;
        this.recoverTime = 0.15f; // 400 RPM

        entity.hasWeaponComponent = true;
    }



    public void fire(Vector2 direction) {
        if(ammo != 0) {
            if(currentClip == 0)
                reload();



            //TODO implement camera shaking when shooting
            if(entity.time - timeFired < recoverTime)
                return;
                currentClip--;
                ammo--;
                Projectile projectile = new Projectile(entity.getPosition().cpy(), 0.7f);
                direction.nor().scl(speed);
                projectile.getBody().setVelocity(direction);
                projectile.getBody().setAngleOfRotation(entity.getBody().getAngleOfRotation());
                projectile.registerEntity();
                timeFired = entity.time;


        }
    }

    public void reload() {
        if(ammo !=0 && clips != 0) {

            clips--;
            if(ammo > clipSize) {
                currentClip += clipSize;
                ammo -= clipSize;
                clips --;
            }
            else {
                currentClip+= ammo;
                ammo = 0;
                clips = 0;
            }
        }
    }

    public void drop() {

    }

    public void setAmmoAndClips(int ammo, int CLIP_SIZE) {
        if(ammo % CLIP_SIZE != 0) {
            throw new IllegalArgumentException("Ammo is not a multiple of clipsize");
        }
        this.ammo = ammo;
        this.clipSize = CLIP_SIZE;
        this.clips = ammo / clipSize;
    }

    public void setRecoverTime(float recoverTime) {
        this.recoverTime = recoverTime;
    }

    public void setProjectileSpeed(float speed) {
        this.speed = speed;
    }

}
