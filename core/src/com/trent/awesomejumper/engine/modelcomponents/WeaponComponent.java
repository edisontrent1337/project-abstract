package com.trent.awesomejumper.engine.modelcomponents;

import com.trent.awesomejumper.controller.EntityManager;
import com.trent.awesomejumper.engine.entity.Entity;
import com.trent.awesomejumper.models.testing.Projectile;

/**
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
    private float recoverTime, reloadTime;
    private float speed;
    private float timeFired = 0f;
    private float timeReloaded = 0f;

    private String weaponName;
    private String weaponDesc;

    public WeaponComponent(Entity weapon, String weaponName) {
        this.entity = weapon;
        this.weaponName = weaponName;
        entity.hasWeaponComponent = true;
    }

    // METHODS & FUNCTIONS
    // ---------------------------------------------------------------------------------------------

    /**
     * Called when weapon is fired. Creates a projectile.
     * Direction, speed and orientation are set depending on the owner of the weapon that
     * fires the projectile.
     */
    public void fire() {
        if (entity.time - timeFired < recoverTime)
            return;
        if(entity.time - timeReloaded < reloadTime)
            return;

        if (currentClip != 0) {

            //TODO implement camera shaking when shooting

            currentClip--;
            Projectile projectile = new Projectile(entity.getBody().getCenter().cpy(), entity.getHeight());
            projectile.getBody().setVelocity(entity.getBody().getOrientation().cpy().nor().scl(speed));
            projectile.getBody().setAngleOfRotation(entity.getBody().getAngleOfRotation());
            projectile.register();
            projectile.setOwner(entity.getOwner().getOwner());
            timeFired = entity.time;
        }

        if (currentClip == 0) {
            reload();
        }
    }

    public void reload() {

        if (ammo != 0) {
            int diff = Math.min(clipSize - currentClip, ammo);
            currentClip += diff;
            ammo -= diff;
            timeReloaded = entity.time;
        }

    }

    // ---------------------------------------------------------------------------------------------
    // GETTER AND SETTER
    // ---------------------------------------------------------------------------------------------

    public void setAmmoAndClips(int ammo, int CLIP_SIZE) {
        if (ammo % CLIP_SIZE != 0) {
            throw new IllegalArgumentException("Ammo is not a multiple of clipsize");
        }
        this.ammo = ammo - CLIP_SIZE;
        this.clipSize = CLIP_SIZE;
        this.clips = ammo / clipSize;
        this.currentClip = CLIP_SIZE;
    }

    public void setWeaponTimings(float recoverTime, float reloadTime) {
        this.recoverTime = recoverTime;
        this.reloadTime = reloadTime;
    }

    public void setProjectileSpeed(float speed) {
        this.speed = speed;
    }

    public String getWeaponName() {
        return weaponName;
    }

    public String getWeaponStatus() {
        String ammoString = Integer.toString(ammo);
        String currentClipString = Integer.toString(currentClip);
        return currentClipString + "/" + ammoString;
    }



}
