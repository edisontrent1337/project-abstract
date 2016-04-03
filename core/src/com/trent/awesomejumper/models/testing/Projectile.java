package com.trent.awesomejumper.models.testing;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.trent.awesomejumper.engine.entity.Entity;
import com.trent.awesomejumper.engine.modelcomponents.Body;
import com.trent.awesomejumper.engine.modelcomponents.Graphics;
import com.trent.awesomejumper.engine.modelcomponents.popups.PopUpFeed;
import com.trent.awesomejumper.engine.physics.CollisionBox;


/**
 * Created by Sinthu on 20.12.2015.
 */
public class Projectile extends Entity {


    private final float WIDTH_X = 0.1875f*0.8f;
    private final float WIDTH_Y = 0.09375f*0.8f;
    private final float MASS = 0.1f;
    private final float FRICTION = 0.999f;
    private final float ELASTICITY = 0f;
    private final float SPRITE_WIDTH = 0.1875f*0.8f;
    private final float SPRITE_HEIGHT = 0.09375f*0.8f;
    private final float FRAME_DURATION = 0.066f;
    private final float MAX_SPEED = 128f; // fastest projectile ingame
    private final float TORQUE = 50f;

    //TESTING
    private final int baseDamage = 90;
    private CollisionBox projectileBox;



    public Projectile(Vector2 position, float z) {
        this.body = new Body(this, WIDTH_X, WIDTH_Y);
        this.graphics = new Graphics(this,FRAME_DURATION,"projectile", SPRITE_WIDTH, SPRITE_HEIGHT);
        this.popUpFeed = new PopUpFeed(this);

        body.setBounds(new CollisionBox(position, WIDTH_X, WIDTH_Y));
        body.setPosition(position);

        projectileBox = new CollisionBox(position,WIDTH_X,WIDTH_Y);
        projectileBox.setOffset(0, z);
        body.setZOffset(z);



        body.setMass(MASS);
        body.setFriction(FRICTION);
        body.setElasticity(ELASTICITY);
        body.setMaxVelocity(MAX_SPEED);
        state = State.IDLE;
        type = Type.PROJECTILE_ENTITY;
        setOwner(this);
        body.add(projectileBox);


        graphics.enableRotations();
    }

    @Override
    public void render(SpriteBatch spriteBatch) {
        super.render(spriteBatch);
    }


    //TESTING
    @Override
    public void update(float delta) {

        super.update(delta);
    }

    public int dealDamage(CollisionBox collisionBox) {
        return (int) (getRandomDamage() * collisionBox.getDamageCoefficient());
    }

    public int getRandomDamage() {
        float coeff = 0.90f + (float)(Math.random() * 0.2f);
        //Gdx.app.log("Coefficient", Float.toString(coeff));
        return (int) (coeff * baseDamage);
    }

    public CollisionBox getProjectileBox() {
        return projectileBox;
    }
}
