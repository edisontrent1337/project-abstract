package com.trent.awesomejumper.models.testing;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.trent.awesomejumper.engine.entity.Entity;
import com.trent.awesomejumper.engine.modelcomponents.Body;
import com.trent.awesomejumper.engine.modelcomponents.Graphics;
import com.trent.awesomejumper.engine.physics.CollisionBox;

/**
 * Created by Sinthu on 10.12.2015.
 */
public class Chest extends Entity {


    private final float WIDTH = 1f;
    private final float HEIGHT = 1f;

    public Chest(Vector2 position) {
        this.body = new Body(this, WIDTH,HEIGHT);
        this.graphics = new Graphics(this, 0f,"wood_chest-0",1);

        body.setPosition(position);
        body.setBounds(new Rectangle(position.x, position.y, WIDTH, HEIGHT));
        body.add(new CollisionBox(position,WIDTH,HEIGHT));
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
