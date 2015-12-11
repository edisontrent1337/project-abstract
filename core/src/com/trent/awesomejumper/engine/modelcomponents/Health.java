package com.trent.awesomejumper.engine.modelcomponents;

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

        // enable health for entity
        entity.hasHealth = true;
    }



    public void heal(float heal) {
        hp += heal;

        if(hp > MAX_HP)
            hp = MAX_HP;
    }


    public void takeDamage(float dmg) {
        hp -= dmg;

        if(hp < 0) {
            entity.setState(Entity.State.DEAD);
        }

    }

    public float getHp() {
        return hp;
    }


}
