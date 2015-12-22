package com.trent.awesomejumper.models.testing;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.trent.awesomejumper.engine.entity.Entity;
import com.trent.awesomejumper.engine.modelcomponents.Body;
import com.trent.awesomejumper.engine.modelcomponents.Graphics;
import com.trent.awesomejumper.engine.modelcomponents.Weapon;
import com.trent.awesomejumper.engine.modelcomponents.popups.PopUpFeed;
import com.trent.awesomejumper.engine.physics.CollisionBox;

import java.util.Random;


/**
 * Created by Sinthu on 20.12.2015.
 */
public class Projectile extends Entity {


    private final float WIDTH = 0.1f;
    private final float HEIGHT = 0.1f;
    private final float MASS = 0.1f;
    private final float FRICTION = 0.999f;
    private final float ELASTICITY = 0f;
    private final float SPRITE_WIDTH = 0.5f;
    private final float SPRITE_HEIGHT = 0.5f;
    private final float FRAME_DURATION = 0.066f;
    private final float MAX_SPEED = 24f;

    //TESTING
    private final int baseDamage = 90;

    private Weapon weapon;

    public Projectile(Vector2 position) {
        this.body = new Body(this, WIDTH, HEIGHT);
        this.graphics = new Graphics(this,FRAME_DURATION,"projectile", SPRITE_WIDTH, SPRITE_HEIGHT);
        this.popUpFeed = new PopUpFeed(this);

        body.setBounds(new CollisionBox(position,WIDTH,HEIGHT));
        body.getBounds().setOffset(-0.5f, 0);
        body.setPosition(position);
        body.setMass(MASS);
        body.setFriction(FRICTION);
        body.setElasticity(ELASTICITY);
        body.setMaxVelocity(MAX_SPEED);
        body.setVelocity(MAX_SPEED, 0);
        state = State.IDLE;
    }

    @Override
    public void render(SpriteBatch spriteBatch) {
        super.render(spriteBatch);
    }

    @Override
    public void update(float delta) {
        super.update(delta);
    }

    public int dealDamage(CollisionBox collisionBox) {
        return (int) (getRandomDamage(baseDamage) * collisionBox.getDamageCoefficient());
    }

    public int getRandomDamage(int basedamage) {
        float coeff = 0.95f + (float)(Math.random() * 0.1f);
        Gdx.app.log("Coefficient", Float.toString(coeff));
        return (int) (coeff * baseDamage);
    }

}
