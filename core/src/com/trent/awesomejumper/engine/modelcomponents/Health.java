package com.trent.awesomejumper.engine.modelcomponents;

import com.trent.awesomejumper.controller.PopUpManager;
import com.trent.awesomejumper.controller.RenderingEngine;
import com.trent.awesomejumper.engine.entity.Entity;
import com.trent.awesomejumper.engine.modelcomponents.popups.Message;
import com.trent.awesomejumper.engine.modelcomponents.popups.PopUpFeed;

/**
 * Created by Sinthu on 09.12.2015.
 */
public class Health extends ModelComponent {

    // MEMBERS & INSTANCES
    // ---------------------------------------------------------------------------------------------
    private int hp;
    private int maxHp;
    private float def;
    private float regeneration;

    private final float INVINCIBILITY_TIME;
    private float tookDamage = 0f;


    // CONSTRUCTOR
    // ---------------------------------------------------------------------------------------------

    public Health(Entity entity, int maxHp) {
        /**
         * Initialises all members with default values.
         * The constructor of the entity or the specific subclass then applies a more useful
         * start configuration to all values.
         */
        this.entity = entity;
        this.maxHp = maxHp;
        this.hp = maxHp;
        this.INVINCIBILITY_TIME = 0.33f;

        // enable health for entity
        entity.hasHealth = true;
    }



    public void heal(float heal) {
        hp += heal;

        if(hp > maxHp)
            hp = maxHp;
    }


    public boolean takeDamage(int dmg) {

        if(entity.time - tookDamage < INVINCIBILITY_TIME)
            return false;
        hp -= dmg;
        float time = entity.time;
        Message message = new Message("-" + Integer.toString(dmg),entity.getPosition().cpy(), time, 2.00f);
        //entity.getPopUpFeed().addMessageToCategory(PopUpFeed.PopUpCategories.DMG,message);
        RenderingEngine.getInstance().addPopUpMsg(PopUpManager.PopUpCategories.DMG, message);
        if(hp <= 0) {
            hp = 0;
            entity.setState(Entity.State.DEAD);
            entity.destroy();
            return true;
        }
        tookDamage = entity.time;

    return true;
    }

    // ---------------------------------------------------------------------------------------------
    // GETTER & SETTER
    // ---------------------------------------------------------------------------------------------

    public float getHp() {
        return hp;

    }

    @Override
    public String toString() {
        return Integer.toString(hp) + "/" + Integer.toString(maxHp);
    }


}
