package com.trent.awesomejumper.engine.modelcomponents.weapons;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.trent.awesomejumper.engine.entity.Entity;
import com.trent.awesomejumper.engine.physics.ProjectileRay;
import com.trent.awesomejumper.models.projectile.Projectile;
import com.trent.awesomejumper.models.weapons.Weapon;
import com.trent.awesomejumper.utils.Utils;

/**
 * Weapon component class. Defines the behaviour and attributes of weapons in the game.
 * Every Weapon entity which wants to be able to fire must have a weapon component.
 * Holds information and manages the following stuff:
 * Ammo, number of clips, clip size, recover time, reload time, recoil
 * Functions like fire(), reload(), drop()
 * Created by Sinthu on 01.03.2016.
 */
public class GunComponent extends WeaponComponent {


    private static final int DEFAULT_DMG = 100;             // default damage
    private static final float DEFAULT_PN_POW = 100;        // default penetration power
    private static final int DEFAULT_NUM_OF_RAYS = 1;       // default number of rays

    // TODO: implement recoil with a vertex shader.
    private int ammo;
    private int clipSize;
    private int clips;
    private int currentClip;
    private float recoverTime, reloadTime;
    private float speed;
    private float baseDamage;
    private float penetrationPower;
    private float timeFired = 0f;
    private float timeReloaded = 0f;
    private float knockBack = 1f;

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
        this.TAG = "GUN COMPONENT";
        entity.enableComponent(ComponentID.WEAPON_COMPONENT);
    }

    /**
     * Constructor that can only be accessed via a gun builder.
     * @param builder gun builder
     */
    private GunComponent(GunBuilder builder) {
        this.entity = builder.weapon;
        this.name = builder.name;
        this.ammo = builder.ammo;
        this.clipSize = builder.clipSize;
        this.recoverTime = builder.recoverTime;
        this.reloadTime = builder.reloadTime;
        this.baseDamage = builder.baseDamage;
        this.penetrationPower = builder.penetrationPower;
        this.knockBack = builder.knockback;
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
           // ProjectileRay ray = new ProjectileRay()
           /* Vector2 direction = entity.getOwner().getBody().getOrientation().cpy().nor();
            Projectile projectile = new Projectile(entity.getBody().getCenter().cpy(),direction, entity.getBody().getHeightZ());
          //  projectile.getBody().setVelocity(entity.getBody().getOrientation().cpy().nor().scl(speed));
            Utils.log("INITIAL PROJECTILE POSITION: " + projectile.getPosition());
            projectile.getBody().addImpulse(direction.scl(speed));
            Gdx.app.log("SCALED",projectile.getBody().getImpulses().getFirst().cpy().nor().scl(48f/60f).toString());
            projectile.getBody().getPosition().sub(projectile.getBody().getImpulses().get(0).cpy().scl(1/60f));
            projectile.getBody().setAngleOfRotation(entity.getBody().getAngleOfRotation());
            projectile.setOwner(entity.getOwner().getOwner());
            projectile.register();*/
            Vector2 origin = entity.getBody().getCenter().cpy();
            Vector2 direction = entity.getOwner().getBody().getOrientation().cpy().nor();
            ProjectileRay ray = new ProjectileRay(origin, direction, penetrationPower, baseDamage, knockBack);
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


    public static class GunBuilder {
        int ammo, clipSize;
        float recoverTime, reloadTime;
        float baseDamage, penetrationPower = 100;
        float knockback = 1f;
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

        public GunBuilder penetrationPower(float penetrationPower) {
            this.penetrationPower = penetrationPower;
            return this;
        }

        public GunBuilder rays(int rays) {
            this.rays = rays;
            return this;
        }

        public GunBuilder knockBack(float knockBack) {
            this.knockback = knockBack;
            return this;
        }
        public GunComponent assemble() {
            return new GunComponent(this);
        }

    }

}
