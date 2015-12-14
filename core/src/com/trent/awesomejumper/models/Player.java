package com.trent.awesomejumper.models;


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

    private final float WIDTH = 0.8f;
    private final float HEIGHT = 0.8f;

    private CollisionBox head, rightArm, rightFoot, leftArm, leftFoot;

    private float playerDelta;
    private static final float PLAYER_RUN_FRAME_DURATION = 0.066f;

    // CONSTRUCTOR
    // ---------------------------------------------------------------------------------------------

    public Player(Vector2 position) {

        this.body = new Body(this, WIDTH, HEIGHT);
        this.graphics = new Graphics(this,PLAYER_RUN_FRAME_DURATION, "player-white-0",5);
        graphics.putMessageCategory("HEALTH");
        this.health = new Health(this, 2000);

        body.setPosition(position);
        body.setBounds(new CollisionBox(position, WIDTH, HEIGHT));
        headHitboxSize = 0.2f;
        armHitBoxSize = 0.2f;
        legHitBoxSize = 0.2f;

        rightArm = new CollisionBox(position, armHitBoxSize, armHitBoxSize, CollisionBox.BoxType.RECTANGLE, new float[]{
                0.0f,0.0f,
                0.0f,1.0f,
                1.0f,1.0f,
                2.5f,1.3f
        }
        );

        //rightArm = new CollisionBox(position, armHitBoxSize, armHitBoxSize);
        rightArm.setOffset((WIDTH - armHitBoxSize) / 2 + 0.2f, HEIGHT / 2.8f);

        leftArm = new CollisionBox(position, armHitBoxSize, armHitBoxSize);
        leftArm.setOffset((WIDTH - armHitBoxSize) / 2 - 0.2f, HEIGHT / 2.8f);

        rightFoot = new CollisionBox(position, legHitBoxSize, legHitBoxSize);
        rightFoot.setOffset((WIDTH - legHitBoxSize) / 2 + 0.2f, 0f);

        leftFoot = new CollisionBox(position, legHitBoxSize, legHitBoxSize);
        leftFoot.setOffset((WIDTH - legHitBoxSize) / 2 - 0.2f, 0f);

        head = new CollisionBox(position, WIDTH, HEIGHT);
        body.add(head);
        body.add(rightArm);
        body.add(leftArm);
        body.add(rightFoot);
        body.add(leftFoot);

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
