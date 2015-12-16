package com.trent.awesomejumper.models.testing;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.trent.awesomejumper.engine.entity.Entity;
import com.trent.awesomejumper.engine.modelcomponents.Body;
import com.trent.awesomejumper.engine.modelcomponents.Graphics;
import com.trent.awesomejumper.engine.modelcomponents.Health;
import com.trent.awesomejumper.engine.physics.CollisionBox;

/**
 * Created by Sinthu on 10.12.2015.
 */
public class Chest extends Entity {


    private static final float WIDTH = 0.75f;               //  24 pixel
    private static final float HEIGHT = 0.4375f;            //  19 Pixel - 5 Pixel
    private static final float SPRITE_WIDTH = 1f;
    private static final float SPRITE_HEIGHT = 1f;

    public Chest(Vector2 position) {

        this.body = new Body(this, WIDTH,HEIGHT);
        this.graphics = new Graphics(this, 0f,"wood_chest-0",1,SPRITE_WIDTH,SPRITE_HEIGHT);
        graphics.putMessageCategory("HEALTH");
        this.health = new Health(this, 250);

        body.setPosition(position);
        body.setMass(0.7f);
        body.setFriction(0.9675f);
        CollisionBox bounds = new CollisionBox(position, 0.75f, 0.4375f);
        bounds.setOffset(0.125f,0f);
        body.setBounds(bounds);
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
