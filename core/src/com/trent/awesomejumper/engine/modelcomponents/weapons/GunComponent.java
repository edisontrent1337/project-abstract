package com.trent.awesomejumper.engine.modelcomponents.weapons;

import com.badlogic.gdx.math.Vector2;
import com.trent.awesomejumper.engine.entity.Entity;
import com.trent.awesomejumper.engine.physics.ProjectileRay;
import com.trent.awesomejumper.models.weapons.Weapon;
import com.trent.awesomejumper.utils.Interval;

import java.util.HashSet;

/**
 * Weapon component class. Defines the behaviour and attributes of weapons in the game.
 * Every Weapon entity which wants to be able to fire must have a weapon component.
 * Holds information and manages the following stuff:
 * Ammo, number of clips, clip size, recover time, reload time, recoil
 * Functions like fire(), reload(), drop()
 * Created by Sinthu on 01.03.2016.
 */
public class GunComponent extends WeaponComponent {

    enum GunType {
        SEMI_AUTOMATIC,
        FULL_AUTOMATIC
    }

    private GunType type;

    // ---------------------------------------------------------------------------------------------
    // MEMBERS
    // ---------------------------------------------------------------------------------------------
    private static final int DEFAULT_DMG = 100;             // default damage
    private static final float DEFAULT_PN_POW = 100;        // default penetration power
    private static final int DEFAULT_NUM_OF_RAYS = 1;       // default number of rays


    private static final float MAX_SPREAD = (float) Math.toRadians(45);    // maximum spread

    // TODO: implement recoil with a vertex shader.
    private int ammo;
    private int clipSize;
    private int clips;
    private int currentClip;
    private float recoverTime, reloadTime;
    private float speed;

    // DAMAGE DEALING & RANGE
    private float baseDamage;
    private float basePenetrationPower;
    private float basePenetrationDamage;
    private float baseRange;
    private float baseSpread;
    private float knockBack = 1f;


    private float timeFired = 0f;
    private float timeReloaded = 0f;

    private String name;
    private String weaponDesc;

    private boolean isEquipped;

    private final int NUMBER_OF_RAYS;

    // ---------------------------------------------------------------------------------------------
    // CONSTRUCTOR
    // ---------------------------------------------------------------------------------------------

    public GunComponent(Entity weapon, String name, int rays) {
        this.entity = weapon;
        this.name = name;
        this.NUMBER_OF_RAYS = rays;
        entity.enableComponent(ComponentID.WEAPON_COMPONENT);
    }

    /**
     * Constructor that can only be accessed via a gun builder.
     * @param builder gun builderre
     */
    private GunComponent(GunBuilder builder) {
        this.entity = builder.weapon;
        this.name = builder.name;
        this.ammo = builder.ammo;
        this.clipSize = builder.clipSize;
        this.recoverTime = builder.recoverTime;
        this.reloadTime = builder.reloadTime;
        this.baseDamage = builder.baseDamage;
        this.basePenetrationPower = builder.basePenetrationPower;
        this.basePenetrationDamage = builder.basePenetrationDamage;
        this.baseRange = builder.baseRange;
        this.baseSpread = builder.baseSpread;
        this.knockBack = builder.knockBack;
        this.NUMBER_OF_RAYS = builder.rays;
        setAmmoAndClips(ammo,clipSize);
    }

    // ---------------------------------------------------------------------------------------------
    // METHODS & FUNCTIONS
    // ---------------------------------------------------------------------------------------------

    /**
     * Called when weapon is fired. Creates a projectile.
     * Direction, speed and orientation are set depending on the owner of the weapon that
     * fires the projectile.
     */
    @Override
    public void fire() {
        if (entity.time - timeFired < recoverTime)
            return;
        if (entity.time - timeReloaded < reloadTime)
            return;

        if (currentClip != 0) {
            currentClip--;
            Vector2 origin = entity.getBody().getCenter().cpy();
            Vector2 direction = entity.getOwner().getBody().getOrientation().cpy().nor();
            ProjectileRay ray = new ProjectileRay(origin, direction, basePenetrationPower, basePenetrationDamage, baseDamage, knockBack);
            ray.register();

            timeFired = entity.time;
        }

        if (currentClip == 0) {
            reload();
        }
    }

    @Override
    public void setEquipped(boolean isEquipped) {
        this.isEquipped = isEquipped;
    }

    /**
     * Reload method.
     */
    public void reload() {

        if (ammo != 0) {
            int diff = Math.min(clipSize - currentClip, ammo);
            currentClip += diff;
            ammo -= diff;
            timeReloaded = entity.time;
        }

    }


    public void modifyPenetrationPower(float increase) {
        basePenetrationPower += increase;
    }

    // ---------------------------------------------------------------------------------------------
    // GETTER AND SETTER
    // ---------------------------------------------------------------------------------------------

    public void setAmmoAndClips(int ammo, int CLIP_SIZE) {
        if (ammo % CLIP_SIZE != 0) {
            throw new IllegalArgumentException("Ammo is not a multiple of clipSize");
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

    public String getName() {
        return name;
    }


    public boolean isEquipped() {
        return isEquipped;
    }


    public String getStatus() {
        String ammoString = Integer.toString(ammo);
        String currentClipString = Integer.toString(currentClip);
        return currentClipString + "/" + ammoString;
    }


    /**
     * Builds a number of rays specified by NUMBER_OF_RAYS and registers them in the system.
     * @return the set of rays if needed for further computations.
     */
    @Override
    protected HashSet<ProjectileRay> buildRays() {
        Vector2 origin = entity.getBody().getCenter().cpy();
        Vector2 direction = entity.getOwner().getBody().getOrientation().cpy().nor();
        // calculate an array of angles which are used to spread the rays
        // maximum spread should be 45 degrees
        float currentMaxSpread = baseSpread * MAX_SPREAD;
        float angleStep = (2 * currentMaxSpread) / NUMBER_OF_RAYS;
        float[] angles = new float[NUMBER_OF_RAYS];
        for(int i = 0; i < NUMBER_OF_RAYS; i++) {
            angles[i] = -currentMaxSpread + i*angleStep;
        }

        return null;
    }

    // ---------------------------------------------------------------------------------------------
    // BUILDER
    // ---------------------------------------------------------------------------------------------

    /**
     * Builder class used to assemble different gun components.
     */
    public static class GunBuilder {
        int ammo, clips, clipSize;
        float recoverTime, reloadTime;
        float baseDamage, basePenetrationPower = 100;
        float basePenetrationDamage = 1f;
        float baseRange = 2f;
        float baseSpread = 0f;
        float knockBack = 1f;
        public String name;
        int rays;

        private Weapon weapon;

        public GunBuilder(Weapon weapon) {
            this.weapon = weapon;
        }

        public GunBuilder name(String name) {
            this.name = name;
            return this;
        }

        public GunBuilder ammo(int ammo) {
            this.ammo = ammo;
            return this;
        }

        public GunBuilder clipSize(int clipSize) {
            this.clipSize = clipSize;
            return this;
        }

        public GunBuilder clips(int clips) {
            this.clips = clips;
            return this;
        }

        public GunBuilder recoverTime(float recoverTime) {
            this.recoverTime = recoverTime;
            return this;
        }

        public GunBuilder reloadTime(float reloadTime) {
            this.reloadTime = reloadTime;
            return this;
        }

        public GunBuilder baseDamage(float baseDamage) {
            this.baseDamage = baseDamage;
            return this;
        }

        public GunBuilder penetrationPower(float basePenetrationPower) {
            this.basePenetrationPower = basePenetrationPower;
            return this;
        }

        public GunBuilder penetrationDamage(float basePenetrationDamage){
            this.basePenetrationDamage = basePenetrationDamage;
            return this;
        }

        public GunBuilder range(float baseRange) {
            this.baseRange = baseRange;
            return this;
        }

        public GunBuilder rays(int rays) {
            this.rays = rays;
            return this;
        }

        public GunBuilder knockBack(float knockBack) {
            this.knockBack = knockBack;
            return this;
        }

        public GunBuilder spread(float baseSpread) {
            this.baseSpread = baseSpread;
            return this;
        }

        public GunComponent assemble() {
            return new GunComponent(this);
        }

    }

}
