package com.trent.awesomejumper.engine.modelcomponents;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.trent.awesomejumper.engine.entity.Entity;
import com.trent.awesomejumper.models.testing.Projectile;
import com.trent.awesomejumper.utils.PhysicalConstants;

import static com.trent.awesomejumper.utils.Utilities.angle;

/** // TODO DOCUMENTATION, IMPLEMENT RELOAD TIME
 * Weapon component class. Defines the behaviour and attributes of weapons in the game.
 * Every Weapon entity which wants to be able to fire must have a weapon component.
 * Holds information and manages the following stuff:
 * Ammo, number of clips, clip size, recover time, reload time, recoil
 * Functions like fire(), reload(), drop()
 * Created by Sinthu on 01.03.2016.
 */
public class WeaponComponent extends ModelComponent {


    // TODO: implement recoil with a vertex shader.
    private int ammo;
    private int clipSize;
    private int clips;
    private int currentClip;
    private float recoverTime, reloadTIme;
    private float speed;
    private float timeFired = 0f;


    public WeaponComponent(Entity weapon) {
        this.entity = weapon;
        entity.hasWeaponComponent = true;
    }


    // TODO: Fix high speed bullets
    public void fire() {
        if(ammo != 0) {
            if(currentClip == 0)
                reload();



            //TODO implement camera shaking when shooting
            if(entity.time - timeFired < recoverTime)
                return;

            currentClip--;
            ammo--;
            Projectile projectile = new Projectile(entity.getBody().getCenter().cpy(), entity.getHeight());
            projectile.getBody().setVelocity(entity.getBody().getOrientation().cpy().nor().scl(speed));
            projectile.getBody().setAngleOfRotation(entity.getBody().getAngleOfRotation());
            projectile.registerEntity();
            timeFired = entity.time;


            /*Gdx.app.log("WEAPON:","");
            Gdx.app.log("MOUSE:", entity.getBody().getAimReference().toString());
            Gdx.app.log("POSICENTER:", entity.getBody().getCenter().toString());
            Gdx.app.log("ORIENTATION:", entity.getBody().getOrientation().toString());
            Gdx.app.log("ANGLE;", Float.toString(entity.getBody().getAngleOfRotation()));
            Gdx.app.log("SPEED",projectile.getVelocity().toString());
            Gdx.app.log("-----------------------------------------------------","");*/



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

    public void setWeaoponTimings(float recoverTime, float reloadTime) {
        this.recoverTime = recoverTime;
        this.reloadTIme = reloadTime;
    }

    public void setProjectileSpeed(float speed) {
        this.speed = speed;
    }


}
