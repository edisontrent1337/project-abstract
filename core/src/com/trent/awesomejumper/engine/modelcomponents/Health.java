package com.trent.awesomejumper.engine.modelcomponents;

import com.badlogic.gdx.Gdx;
import com.trent.awesomejumper.engine.entity.Entity;
import com.trent.awesomejumper.engine.modelcomponents.popups.Message;
import com.trent.awesomejumper.engine.modelcomponents.popups.PopUpFeed;

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
        this.INVINCIBILITY_TIME = 0.33f;

        // enable health for entity
        entity.hasHealth = true;
    }



    public void heal(float heal) {
        hp += heal;

        if(hp > MAX_HP)
            hp = MAX_HP;
    }


    public boolean takeDamage(int dmg) {

        if(entity.time - tookDamage < INVINCIBILITY_TIME)
            return false;
        hp -= dmg;
        entity.getPopUpFeed().addMessageToCategory(PopUpFeed.PopUpCategories.DMG, new Message("-" + Integer.toString(dmg), entity.time, 2.00f));
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
