package com.trent.awesomejumper.engine.physics;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.trent.awesomejumper.engine.modelcomponents.Body;
import com.trent.awesomejumper.utils.Utils;

/**
 * Created by Sinthu on 3/22/2017.
 */

public class ProjectileRay extends Ray {
    private float penetrationPower = 0f;
    private float remainingPower = 0f;
    private float baseDamage = 100;
    private float force = 1f;


    public ProjectileRay(Vector2 origin, Vector2 direction, float penetrationPower, float baseDamage, float force) {
        // Create an infinite ray in the specified direction, starting at origin
        super(origin, direction);
        // adding penetration power and other projectile specific stuff
        this.penetrationPower = penetrationPower;
        this.baseDamage = baseDamage;
        this.force = force;
        this.remainingPower = penetrationPower;
    }

    // ---------------------------------------------------------------------------------------------
    // METHODS & FUNCTIONS
    // ---------------------------------------------------------------------------------------------
    private void reducePenetrationPower(float density) {
        remainingPower -= (1-density)*penetrationPower;
        if(remainingPower < 0)
            remainingPower = 0;
    }

    public int dealDamage(CollisionBox collisionBox, Body body) {
        float coeff = 0.97f + (float)(Math.random()*0.03f);
        float dmgCoeff = collisionBox.getDamageCoefficient();
        float power = remainingPower/penetrationPower;
        float power3 = power*power*power;
        float mass = body.getMass();
        Utils.log("POWER ", power);
        Utils.log("REMAINING POWER", remainingPower);
        reducePenetrationPower(body.getDensity());
        Vector2 impulse = dir.cpy().scl(force*power3*1f/mass);
        //TODO: this impulse has to be a reflection impulse, using the method in cd
        //TODO: impulse creation should be outsourced to collision controller.
        Utils.log("----------IMPULSE CALC-------------");
        Utils.log("CURRENT ENTITY: ", body.getEntity().toString());
        Utils.log("IMPULSE", impulse);
        Utils.log("SCALED IMPULSE", impulse.cpy().scl(Gdx.graphics.getDeltaTime()));
        Utils.log("BODY", body.toString());
        //int damage = (int) (coeff * density * power * baseDamage);
        body.addImpulse(impulse);
        //body.setAcceleration(impulse.x,impulse.y);
        return (int) (coeff * dmgCoeff * power * baseDamage);
    }




}
