package com.trent.awesomejumper.models;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.trent.awesomejumper.engine.entity.Entity;
import com.trent.awesomejumper.engine.modelcomponents.Body;
import com.trent.awesomejumper.engine.modelcomponents.Graphics;
import com.trent.awesomejumper.engine.modelcomponents.Health;
import com.trent.awesomejumper.engine.physics.CollisionBox;

/**
 * Created by Sinthu on 12.06.2015.
 */
public class Player extends Entity {

    // MEMBERS & INSTANCES
    // ---------------------------------------------------------------------------------------------
    private final float headHitboxSize, armHitBoxSize, legHitBoxSize;

    private static final float WIDTH = 0.5f;
    private static final float HEIGHT = 0.625f;
    private static final float SPRITE_WIDTH = 1f; //
    private static final float SPRITE_HEIGHT = 1f;
    private static final float PLAYER_RUN_FRAME_DURATION = 0.066f;

    private CollisionBox head, rightArm, rightFoot, leftArm, leftFoot;

    private float playerDelta;

    // CONSTRUCTOR
    // ---------------------------------------------------------------------------------------------

    public Player(Vector2 position) {

        body = new Body(this, WIDTH, HEIGHT);
        graphics = new Graphics(this,PLAYER_RUN_FRAME_DURATION, "player-white-0",5,SPRITE_WIDTH,SPRITE_HEIGHT);
        graphics.putMessageCategory("HEALTH");
        health = new Health(this, 2000);

        body.setPosition(position);
        // TODO: Implement method that calculates smallest bounding box around skeleton
        body.setBounds(new CollisionBox(position, 0.5f, (float) 20 / 32));
        body.setMass(10f);
        body.setFriction(0.66f);
        body.setElasticity(0f);
        body.getBounds().setOffset(0.2f,0f);
        headHitboxSize = 0.2f;
        armHitBoxSize = 0.2f;
        legHitBoxSize = 0.2f;

        /*rightArm = new CollisionBox(position, armHitBoxSize, armHitBoxSize, CollisionBox.BoxType.TRIANGLE, new float[]{
                0.0f,0.0f,
                0.0f,0.2f,
                0.2f,0.2f,
        }
        );*/

        rightArm = new CollisionBox(position, armHitBoxSize, armHitBoxSize);
        rightArm.setOffset((WIDTH - armHitBoxSize) / 2 + 0.2f, HEIGHT / 2.8f);

        leftArm = new CollisionBox(position, armHitBoxSize, armHitBoxSize);
        leftArm.setOffset((WIDTH - armHitBoxSize) / 2 - 0.2f, HEIGHT / 2.8f);

        rightFoot = new CollisionBox(position, legHitBoxSize, legHitBoxSize);
        rightFoot.setOffset((WIDTH - legHitBoxSize) / 2 + 0.2f, 0f);

        leftFoot = new CollisionBox(position, legHitBoxSize, legHitBoxSize);
        leftFoot.setOffset((WIDTH - legHitBoxSize) / 2 - 0.2f, 0f);

        //head = new CollisionBox(position, WIDTH, HEIGHT);
        //body.add(head);
        body.add(rightArm);
        body.add(leftArm);
        body.add(rightFoot);
        body.add(leftFoot);

        for(Vector2 n : rightArm.getNormals()) {
            Gdx.app.log("NORMAL", n.toString());
        }

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
