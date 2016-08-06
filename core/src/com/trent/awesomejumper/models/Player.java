package com.trent.awesomejumper.models;


import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.trent.awesomejumper.controller.InputHandler;
import com.trent.awesomejumper.engine.entity.Entity;
import com.trent.awesomejumper.engine.modelcomponents.Body;
import com.trent.awesomejumper.engine.modelcomponents.Graphics;
import com.trent.awesomejumper.engine.modelcomponents.Health;
import com.trent.awesomejumper.engine.modelcomponents.WeaponInventory;
import com.trent.awesomejumper.engine.modelcomponents.popups.PopUpFeed;
import com.trent.awesomejumper.engine.physics.CollisionBox;

import static com.trent.awesomejumper.utils.Utilities.angle;
import static com.trent.awesomejumper.utils.Utilities.sub;

/**
 * Created by Sinthu on 12.06.2015.
 */
public class Player extends Entity {

    // MEMBERS & INSTANCES
    // ---------------------------------------------------------------------------------------------
    private final float headSize, legSize, torsoSize;

    // WORLD UNIT: 1 = 1m


    // TODO: Add JSON support to outsource this information
    // 17*22 px outlines, 10px offset to each side
    private final float GRAPHIC_SCALE = 1f;
    private final float X_OFFSET = 0.32f*GRAPHIC_SCALE;
    private final float MASS = 10f;
    private final float FRICTION = 0.66f;
    private final float ELASTICITY = 0f;
    private final float SPRITE_WIDTH = 1f*GRAPHIC_SCALE;  // 48px
    private final float SPRITE_HEIGHT = 1f*GRAPHIC_SCALE; // 48px
    private final float FRAME_DURATION = 0.05f;
    private final float MAX_SPEED = 3.8f;
    private int startHealth  = 2000;
    private final String SPRITE_PREFIX = "link";

    private final float WIDTH_X = 0.375f*GRAPHIC_SCALE;     // width in x direction on the xy plane
    private final float WIDTH_Y = 0.375f*GRAPHIC_SCALE;     // width in y direction on the xy plane
    private final float HEIGHT_Z = 1f;                      // height of the entity in z direction

    private CollisionBox head, torso, legs;

    private float playerDelta;

    // CONSTRUCTOR
    // ---------------------------------------------------------------------------------------------

    public Player(Vector2 position) {
        body = new Body(this, WIDTH_X, WIDTH_Y);                                                // enable physics
        graphics = new Graphics(this,FRAME_DURATION,SPRITE_PREFIX,SPRITE_WIDTH,SPRITE_HEIGHT);  // enable graphics
        health = new Health(this, startHealth);                                                 // enable health

        weaponInventory = new WeaponInventory(this, 2);


        body.setBounds(new CollisionBox(position, WIDTH_X, WIDTH_Y));
        body.setPosition(position);

        /**
         * Setting offset to ground bounding box so that it wraps around the texture of the entity
         * properly. Also initialize the z position to 0 and the height in z direction to 1.
         */
        body.getBounds().setOffset(X_OFFSET, 0f);
        body.setZOffset(0f);
        body.setHeightZ(HEIGHT_Z);

        body.setMass(MASS);
        body.setFriction(FRICTION);
        body.setElasticity(ELASTICITY);
        body.setMaxVelocity(MAX_SPEED);

        body.setOrientation(sub(body.getCenter(), InputHandler.mouse));
        body.setAngleOfRotation(angle(body.getOrientation()));

        headSize = 0.1f*HEIGHT_Z;   // 10% of the z height are occupied by the head
        torsoSize = 0.4f*HEIGHT_Z;  // 40% of the z height are occupied by the torso
        legSize = 0.4f*HEIGHT_Z;    // 40% of the z height are occupied by the legs


        head = new CollisionBox(position,WIDTH_X,headSize);
        head.setOffset(X_OFFSET, 0.8f * HEIGHT_Z);
        head.setDamageCoefficient(1f);

        torso = new CollisionBox(position,WIDTH_X,torsoSize);
        torso.setOffset(X_OFFSET, 0.3f * HEIGHT_Z);
        torso.setDamageCoefficient(0.4f);


        legs = new CollisionBox(position, WIDTH_X,legSize);
        legs.setOffset(X_OFFSET, 0f);
        legs.setDamageCoefficient(0.2f);



        /**
         * Add all body parts to the skeleton.
         */

        body.add(head);
        body.add(torso);
        body.add(legs);

        state = State.IDLE;
        type = Type.REGULAR_ENTITY;
        setOwner(this);
    }





    @Override
    public void update(float delta) {
        playerDelta = delta;
        super.update(delta);
        body.setAimReference(InputHandler.mouse);
        body.setOrientation(sub(body.getCenter(),body.getAimReference()));
        body.setAngleOfRotation(angle(body.getOrientation()));


    }

    @Override
    public void render(SpriteBatch spriteBatch) {
        super.render(spriteBatch);
    }

    @Override
    public void updateWeaponPositions() {
        super.updateWeaponPositions();
    }

    public float getPlayerDelta() {
        return playerDelta;
    }

}
