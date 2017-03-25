package com.trent.awesomejumper.models.weapons;


import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.trent.awesomejumper.engine.modelcomponents.Body;
import com.trent.awesomejumper.engine.modelcomponents.Graphics;
import com.trent.awesomejumper.engine.modelcomponents.weapons.GunComponent;
import com.trent.awesomejumper.engine.physics.CollisionBox;

/**
 * TODO: implement range
 * Pistol entity class. Has a body and a sprite component to represent pistol as pickup or as an
 * equipped weapon inventory item.
 * Created by Sinthu on 01.03.2016.
 */
public class Pistol extends Weapon {


    private final float WIDTH = 0.375f;               //  24 pixel
    private final float HEIGHT = 0.21875f;            //  19 Pixel - 5 Pixel "behind space
    private final float MASS = 0.150f;
    private final float FRICTION = 0.945f;
    private final float ELASTICITY = 0.45f;
    private final float SPRITE_WIDTH = 0.5f;           // 24 px width
    private final float SPRITE_HEIGHT = 0.5f;          // 24 px height
    private final float MAX_SPEED = 5f;

    private final int AMMO = 100;
    private final int CLIP_SIZE = 25;
    private final int BASE_DMG = 100;
    private final float PENETRATION_POWER = 100;
    private final float PROJECTILE_SPEED = 48f;
    private final float RECOVER_TIME = 0.2f; // 400 RPM
    private final float RELOAD_TIME = 2.25f;


    public Pistol(Vector2 position) {
        this.body = new Body(this, WIDTH, HEIGHT);
        this.graphics = new Graphics(this, 0f, "fiveseven", SPRITE_WIDTH, SPRITE_HEIGHT);
        this.gunComponent = new GunComponent(this, "FIVE-SEVEN",1);


        body.setMass(MASS);
        body.setFriction(FRICTION);
        body.setElasticity(ELASTICITY);
        body.setPosition(position);
        body.setMaxVelocity(MAX_SPEED);
        body.setBounds(new CollisionBox(position, WIDTH, HEIGHT));

        gunComponent.setAmmoAndClips(AMMO, CLIP_SIZE);
        gunComponent.setProjectileSpeed(PROJECTILE_SPEED);
        gunComponent.setWeaponTimings(RECOVER_TIME, RELOAD_TIME);

        graphics.enableRotations();
        graphics.disableShadowRotations();
        state = State.IDLE;
        type = Type.DROPPED_WEAPON_ENTITY;
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
