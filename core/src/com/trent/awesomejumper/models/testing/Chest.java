package com.trent.awesomejumper.models.testing;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.trent.awesomejumper.engine.entity.Entity;
import com.trent.awesomejumper.engine.modelcomponents.Body;
import com.trent.awesomejumper.engine.modelcomponents.Graphics;
import com.trent.awesomejumper.engine.modelcomponents.PopUpFeed;
import com.trent.awesomejumper.engine.physics.CollisionBox;

/**
 * Created by Sinthu on 10.12.2015.
 */
public class Chest extends Entity {


    private static final float WIDTH = 0.75f;               //  24 pixel
    private static final float HEIGHT = 0.4375f;            //  19 Pixel - 5 Pixel "behind space
    private static final float SPRITE_WIDTH = 1f;           // 32 px width
    private static final float SPRITE_HEIGHT = 1f;          // 32 px height

    public Chest(Vector2 position) {

        this.body = new Body(this, WIDTH,HEIGHT);
        this.graphics = new Graphics(this, 0f,"wood_chest", SPRITE_WIDTH,SPRITE_HEIGHT);
        this.popUpFeed = new PopUpFeed(this);
        this.body.setPosition(position);
        this.body.setMass(0.4f);
        this.body.setFriction(0.9457f);
        this.body.setElasticity(0.45f);
        CollisionBox bounds = new CollisionBox(position, 0.75f, 0.4375f);
        bounds.setOffset(0.125f,0f);
        this.body.setBounds(bounds);
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
