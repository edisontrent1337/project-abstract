package com.trent.awesomejumper.engine.physics;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.trent.awesomejumper.controller.entitymanagement.EntityManager;
import com.trent.awesomejumper.engine.entity.Entity;
import com.trent.awesomejumper.engine.modelcomponents.Body;
import com.trent.awesomejumper.utils.Utils;

/**
 * Created by Sinthu on 3/22/2017.
 */

public class ProjectileRay extends Ray {
    private float penetrationPower = 0f;
    private float penetrationDmgScale = 1f;
    private float remainingPower = 0f;
    private float baseDamage = 100;
    private float knockBack = 1f;

    public static final int PENETRATION_LOSS = 12;

    // ---------------------------------------------------------------------------------------------
    // CONSTRUCTOR
    // ---------------------------------------------------------------------------------------------
    public ProjectileRay(Vector2 origin, Vector2 direction, float penetrationPower, float penetrationDmgScale, float baseDamage, float knockBack) {
        // Create an infinite ray in the specified direction, starting at origin
        super(origin, direction);
        // adding penetration power and other projectile specific stuff
        this.penetrationPower = penetrationPower;
        this.penetrationDmgScale = penetrationDmgScale;
        this.baseDamage = baseDamage;
        this.knockBack = knockBack;
        this.remainingPower = penetrationPower;
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
        float coeff = 0.97f + (float)(Math.random()*0.06f);
        float dmgCoeff = collisionBox.getDamageCoefficient();
        float power = remainingPower/penetrationPower;
        float dmgPenetrationScale = (float) Math.pow(power,1f/(0.1f*penetrationDmgScale));
        float power3 = power*power*power;
        float mass = body.getMass();
        Utils.log("POWER ", power);
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
