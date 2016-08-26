package com.trent.awesomejumper.engine.modelcomponents;

import com.badlogic.gdx.Gdx;
import com.trent.awesomejumper.controller.rendering.PopUpRenderer;
import com.trent.awesomejumper.controller.WorldController;
import com.trent.awesomejumper.engine.entity.Entity;
import com.trent.awesomejumper.engine.modelcomponents.popups.Message;

import java.util.Random;

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
    private int lastDamage;

    private final float INVINCIBILITY_TIME;
    private float tookDamageAt = 0f;

    private Random random;

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
        this.random = new Random(System.currentTimeMillis());
        this.lastDamage = 0;
        // enable health for entity
        entity.enableComponent(ComponentID.HEALTH);

    }


    public void heal(float heal) {
        hp += heal;

        if (hp > maxHp)
            hp = maxHp;
    }


    public boolean takeDamage(int dmg) {

        if (entity.time - tookDamageAt < INVINCIBILITY_TIME)
            return false;
        Message message;

        if (random.nextInt(100) > 95) {
            dmg *= 1.25f;
            hp -= dmg;
            message = new Message("-" + Integer.toString(dmg), entity.getBody().getCenter(), WorldController.worldTime, 1.5f);
            PopUpRenderer.getInstance().addMessageToCategory(PopUpRenderer.PopUpCategories.CRT, message);
            Gdx.app.log("CRIT","YEA");
        } else {

            hp -= dmg;
            message = new Message("-" + Integer.toString(dmg), entity.getBody().getCenter(), WorldController.worldTime, 1.50f);
            PopUpRenderer.getInstance().addMessageToCategory(PopUpRenderer.PopUpCategories.DMG, message);
        }


        /**
         * Access popup manager globally to add damage popup.
         */
        if (hp <= 0) {
            hp = 0;
            entity.setState(Entity.State.DEAD);
            entity.destroy();
            return true;
        }
        tookDamageAt = entity.time;
        lastDamage = dmg;

        return true;
    }

    // ---------------------------------------------------------------------------------------------
    // GETTER & SETTER
    // ---------------------------------------------------------------------------------------------

    public float getHp() {
        return hp;

    }

    public float getMaxHp() {
        return maxHp;
    }

    public float tookDamageAt() {
        return tookDamageAt;
    }

    public int getLastDamage() {
        return lastDamage;
    }

    @Override
    public String toString() {
        return Integer.toString(hp) + "/" + Integer.toString(maxHp);
    }


}
