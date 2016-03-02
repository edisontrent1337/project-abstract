package com.trent.awesomejumper.models.testing;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.trent.awesomejumper.engine.entity.Entity;
import com.trent.awesomejumper.engine.modelcomponents.Body;
import com.trent.awesomejumper.engine.modelcomponents.Graphics;
import com.trent.awesomejumper.engine.modelcomponents.Health;
import com.trent.awesomejumper.engine.modelcomponents.popups.PopUpFeed;
import com.trent.awesomejumper.engine.physics.CollisionBox;

/**
 * Created by Sinthu on 10.12.2015.
 */
public class Chest extends Entity {


    private final float WIDTH = 0.75f;               //  24 pixel
    private final float HEIGHT = 0.4375f;            //  19 Pixel - 5 Pixel "behind space
    private final float MASS = 0.4f;
    private final float FRICTION = 0.945f;
    private final float ELASTICITY = 0.45f;
    private final float SPRITE_WIDTH = 0.75f;           // 32 px width
    private final float SPRITE_HEIGHT = 0.75f;          // 32 px height
    private final float MAX_SPEED = 5f;
    private final Vector2 OFFSET = new Vector2(0.125f, 0); // 4 px space in chest sprite

    private final float FRAME_DURATION = 0.066f;

    public Chest(Vector2 position) {

        body = new Body(this, WIDTH,HEIGHT);
        graphics = new Graphics(this, 0f,"wood_chest", SPRITE_WIDTH,SPRITE_HEIGHT);
        popUpFeed = new PopUpFeed(this);
        body.setPosition(position);
        body.setMass(MASS);
        body.setFriction(FRICTION);
        body.setElasticity(ELASTICITY);
        body.setBounds(new CollisionBox(position, WIDTH, HEIGHT));
        body.getBounds().setOffset(OFFSET);
        body.setMaxVelocity(MAX_SPEED);
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
