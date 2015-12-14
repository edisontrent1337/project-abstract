package com.trent.awesomejumper.engine.modelcomponents;

import com.badlogic.gdx.Gdx;
import com.trent.awesomejumper.engine.entity.Entity;

/**
 * Created by Sinthu on 09.12.2015.
 */
public class Health extends ModelComponent {

    // MEMBERS & INSTANCES
    // ---------------------------------------------------------------------------------------------
    private float hp;
    private final float MAX_HP;
    private float def;
    private float regeneration;

    private final float INVINCIBILITY_TIME;
    private float tookDamage = 0f;


    // TODO: dicussion worthy.
    private Entity.State state;

    // CONSTRUCTOR
    // ---------------------------------------------------------------------------------------------

    public Health(Entity entity, float MAX_HP) {
        /**
         * Initialises all members with default values.
         * The constructor of the entity or the specific subclass then applies a more useful
         * start configuration to all values.
         */
        this.entity = entity;
        this.MAX_HP = MAX_HP;
        this.hp = MAX_HP;
        this.INVINCIBILITY_TIME = 1.00f;

        // enable health for entity
        entity.hasHealth = true;
    }



    public void heal(float heal) {
        hp += heal;

        if(hp > MAX_HP)
            hp = MAX_HP;
    }


    public boolean takeDamage(float dmg) {

        if(entity.time - tookDamage < INVINCIBILITY_TIME)
            return false;
        hp -= dmg;

        if(hp <= 0) {
            hp = 0;
            entity.setState(Entity.State.DEAD);
        }
        tookDamage = entity.time;

    return true;
    }

    public float getHp() {
        return hp;
    }


}
