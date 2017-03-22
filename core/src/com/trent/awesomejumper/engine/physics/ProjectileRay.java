package com.trent.awesomejumper.engine.physics;

import com.badlogic.gdx.math.Vector2;

/**
 * Created by Sinthu on 3/22/2017.
 */

public class ProjectileRay extends Ray {
    private float penetrationPower = 0f;
    private float remainingPower = 0f;
    private float baseDamage = 100;


    public ProjectileRay(Vector2 origin, Vector2 direction, float penetrationPower, float baseDamage) {
        // Create an infinite ray in the specified direction, starting at origin
        super(origin, direction);
        // adding penetration power and other projectile specific stuff
        this.penetrationPower = penetrationPower;
        this.remainingPower = penetrationPower;
        this.baseDamage = baseDamage;
    }


    public void redudePenetrationPower(float density) {
        remainingPower -= density*penetrationPower;
        if(remainingPower < 0)
            remainingPower = 0;
    }

    public int dealdamage(CollisionBox collisionBox) {
        float coeff = 0.9f * (float)(Math.random()*0.2f);
        float density = collisionBox.getDamageCoefficient();
        float power = remainingPower/penetrationPower;
        return (int) (coeff * density * power * baseDamage);
    }




}
