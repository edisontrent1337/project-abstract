package com.trent.awesomejumper.models;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.trent.awesomejumper.engine.entity.Entity;
import com.trent.awesomejumper.engine.modelcomponents.Body;
import com.trent.awesomejumper.engine.modelcomponents.Graphics;
import com.trent.awesomejumper.engine.modelcomponents.Health;
import com.trent.awesomejumper.engine.modelcomponents.popups.PopUpFeed;
import com.trent.awesomejumper.engine.physics.CollisionBox;

/**
 * Created by Sinthu on 12.06.2015.
 */
public class Player extends Entity {

    // MEMBERS & INSTANCES
    // ---------------------------------------------------------------------------------------------
    private final float headHitboxSize, armHitBoxSize, legHitBoxSize;

    // TODO: Add JSON support to outsource this information
    private final float WIDTH = 0.5f;
    private final float HEIGHT = 0.625f;
    private final float MASS = 100f;
    private final float FRICTION = 0.66f;
    private final float ELASTICITY = 0f;
    private final float SPRITE_WIDTH = 1f;
    private final float SPRITE_HEIGHT = 1f;
    private final float FRAME_DURATION = 0.066f;
    private final float MAX_SPEED = 5f;
    private int startHealth  = 2000;
    private final String SPRITE_PREFIX = "player";

    private CollisionBox head, torso,  rightArm, rightFoot, leftArm, leftFoot;

    private float playerDelta;

    // CONSTRUCTOR
    // ---------------------------------------------------------------------------------------------

    public Player(Vector2 position) {
        body = new Body(this, WIDTH, HEIGHT); // enable physics
        graphics = new Graphics(this,FRAME_DURATION,SPRITE_PREFIX,SPRITE_WIDTH,SPRITE_HEIGHT); // enable graphics
        popUpFeed = new PopUpFeed(this); // enable capability to render popups
        health = new Health(this, startHealth); // enable health

        // TODO: Implement method that calculates smallest bounding box around skeleton
        body.setBounds(new CollisionBox(position, WIDTH, HEIGHT));
        body.setPosition(position);
        body.getBounds().setOffset(0.2f, 0f);
        body.setMass(MASS);
        body.setFriction(FRICTION);
        body.setElasticity(ELASTICITY);
        body.setMaxVelocity(MAX_SPEED);
        headHitboxSize = 0.2f;
        armHitBoxSize = 0.2f;
        legHitBoxSize = 0.2f;

       /* rightArm = new CollisionBox(position, armHitBoxSize, armHitBoxSize, CollisionBox.BoxType.TRIANGLE, new float[]{
                0.0f,0.0f,
                0.0f,0.2f,
                0.2f,0.2f,
        }
        );*/

        head = new CollisionBox(position,headHitboxSize,headHitboxSize);
        head.setOffset((WIDTH - headHitboxSize)/2 + 0.2f, HEIGHT - headHitboxSize);
        head.setDamageCoefficient(1f);


        rightArm = new CollisionBox(position, armHitBoxSize, armHitBoxSize);
        rightArm.setOffset((WIDTH - armHitBoxSize) / 2 + 0.4f, HEIGHT / 2.8f);
        rightArm.setDamageCoefficient(0.3f);

        leftArm = new CollisionBox(position, armHitBoxSize, armHitBoxSize);
        leftArm.setOffset((WIDTH - armHitBoxSize) / 2, HEIGHT / 2.8f);
        leftArm.setDamageCoefficient(0.3f);

        rightFoot = new CollisionBox(position, legHitBoxSize, legHitBoxSize + 0.1f);
        rightFoot.setOffset((WIDTH - legHitBoxSize) / 2 + 0.4f, 0f);
        rightFoot.setDamageCoefficient(0.2f);

        leftFoot = new CollisionBox(position, legHitBoxSize, legHitBoxSize + 0.1f);
        leftFoot.setOffset((WIDTH - legHitBoxSize) / 2, 0f);
        leftFoot.setDamageCoefficient(0.2f);

        /**
         * Add all body parts to the skeleton.
         */

        body.add(head);
        body.add(rightArm);
        body.add(leftArm);
        body.add(rightFoot);
        body.add(leftFoot);

        //TODO: After the skeleton is completed, the bounding box has to be calculated.


        state = State.IDLE;
    }





    @Override
    public void update(float delta) {
        playerDelta = delta;
        super.update(delta);

    }

    @Override
    public void render(SpriteBatch spriteBatch) {
        super.render(spriteBatch);
    }

    public float getPlayerDelta() {
        return playerDelta;
    }

}
