package com.trent.awesomejumper.models.weapons;


import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.trent.awesomejumper.engine.modelcomponents.Body;
import com.trent.awesomejumper.engine.modelcomponents.Graphics;
import com.trent.awesomejumper.engine.modelcomponents.WeaponComponent;

/**
 *
 * Pistol entity class. Has a body and a sprite component to represent pistol as pickup or as an
 * equipped weapon inventory item.
 * Created by Sinthu on 01.03.2016.
 */
public class Pistol extends Weapon {


    private final float WIDTH = 0.75f;               //  24 pixel
    private final float HEIGHT = 0.4375f;            //  19 Pixel - 5 Pixel "behind space
    private final float MASS = 0.150f;
    private final float FRICTION = 0.945f;
    private final float ELASTICITY = 0.45f;
    private final float SPRITE_WIDTH = 0.75f;           // 32 px width
    private final float SPRITE_HEIGHT = 0.75f;          // 32 px height
    private final float MAX_SPEED = 5f;

    private final int AMMO = 100;
    private final int CLIPS = 4;
    private final float PROJECTILE_SPEED = 20f;


    public Pistol(Vector2 position) {
        this.body = new Body(this, WIDTH, HEIGHT);
        this.graphics = new Graphics(this, 0f, "fiveseven", SPRITE_WIDTH, SPRITE_HEIGHT);
        this.weaponComponent = new WeaponComponent(this);

        body.setMass(MASS);
        body.setFriction(FRICTION);
        body.setElasticity(ELASTICITY);
        body.setPosition(position);
        body.setMaxVelocity(MAX_SPEED);

        weaponComponent.setAmmoAndClips(AMMO, CLIPS);
        weaponComponent.setProjectileSpeed(PROJECTILE_SPEED);

        graphics.enableRotations();
        state = State.IDLE;
    }



    @Override
    public void update(float delta) {
        super.update(delta);
    }

    @Override
    public void render(SpriteBatch spriteBatch) {
        super.render(spriteBatch);
    }





}
