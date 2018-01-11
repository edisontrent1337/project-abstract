package com.trent.awesomejumper.engine.physics;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.trent.awesomejumper.controller.entitymanagement.EntityManager;
import com.trent.awesomejumper.engine.modelcomponents.Body;
import com.trent.awesomejumper.utils.Utils;

/**
 * Created by Sinthu on 3/22/2017.
 */

public class ProjectileRay extends Ray {
    private float basePenetrationPower = 0f;
    private float basePenetrationDamage = 1f;
    private float remainingPower = 0f;
    private float baseDamage = 100;
    private float knockBack = 1f;

    public static final int PENETRATION_LOSS = 12;

    // ---------------------------------------------------------------------------------------------
    // CONSTRUCTOR
    // ---------------------------------------------------------------------------------------------
    public ProjectileRay(Vector2 origin, Vector2 direction, float basePenetrationPower, float basePenetrationDamage, float baseDamage, float knockBack) {
        // Create an infinite ray in the specified direction, starting at origin
        super(origin, direction);
        // adding penetration power and other projectile specific stuff
        this.basePenetrationPower = basePenetrationPower;
        this.basePenetrationDamage = basePenetrationDamage;
        this.baseDamage = baseDamage;
        this.knockBack = knockBack;
        this.remainingPower = basePenetrationPower;
    }

    // ---------------------------------------------------------------------------------------------
    // METHODS & FUNCTIONS
    // ---------------------------------------------------------------------------------------------
    private void reducePenetrationPower(float density) {
        remainingPower -= density*PENETRATION_LOSS;
        if(remainingPower < 0)
            remainingPower = 0;
    }

    /**
     * Damage method. Needs a lot of balancing and rework.
     * @param collisionBox The hitbox the damage is applied to.
     * @param body The body object of the entity.
     * @return amount of damage dealt
     */
    public int dealDamage(CollisionBox collisionBox, Body body) {


        /**
         * baseDamage               := base damage of the projectile
         * currentBaseDamage        := base damage adjusted regarding the range
         *
         * basePenetrationPower     := base penetration power. describes, how many entities can be penetrated.
         * remainingPower           := remaining penetration power
         * currentPower             := percentage of remaining power
         *
         * basePenetrationDamage    := determines, how much of the damage remains after passing several entities
         * currentPenetrationDamage := current penetration damage, calculated on basis of currentPower
         *
         * randomness               := random coefficient
         * armor                    := amount of damage the collision box simply blocks
         */


        float coeff = 0.97f + (float)(Math.random()*0.06f);
        float dmgCoeff = collisionBox.getDamageCoefficient();
        float currentPower = remainingPower/ basePenetrationPower;
        float dmgPenetrationScale = (float) Math.pow(currentPower,1f/(0.1f* basePenetrationDamage));
        float power3 = currentPower*currentPower*currentPower;
        float mass = body.getMass();
        Utils.log("POWER ", currentPower);
        Utils.log("REMAINING POWER", remainingPower);
        reducePenetrationPower(body.getDensity());
        Vector2 impulse = dir.cpy().scl(knockBack*power3*1f/mass);
        Utils.log("----------IMPULSE CALC-------------");
        Utils.log("CURRENT ENTITY: ", body.getEntity().toString());
        Utils.log("IMPULSE", impulse);
        Utils.log("SCALED IMPULSE", impulse.cpy().scl(Gdx.graphics.getDeltaTime()));
        Utils.log("BODY", body.toString());
        body.addImpulse(impulse);
        return (int) (coeff * dmgCoeff * dmgPenetrationScale * baseDamage);
    }



    @Override
    public void register() {
        EntityManager.getInstance().registerRay(this);
    }

    // ---------------------------------------------------------------------------------------------
    // GETTER & SETTER
    // ---------------------------------------------------------------------------------------------

    public float getRemainingPower() {
        return remainingPower;
    }
}
