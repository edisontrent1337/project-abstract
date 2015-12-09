package com.trent.awesomejumper.models;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.trent.awesomejumper.engine.modelcomponents.Body;

/**
 * Created by Sinthu on 12.06.2015.
 */
public class SkyBox extends Entity {


    // CONSTRUCTOR
    // ---------------------------------------------------------------------------------------------

    //TODO: BUGGY!
    public SkyBox(Vector2 position, float width, float speed) {
        body = new Body(this);
        body.setBounds(new Rectangle(position.x, position.y, 2f * width, 2 * 0.5625f * width));
        body.setVelocity(speed, 0);
    }

    @Override
    public void update(float delta) {
        super.update(delta);
    }

    @Override
    public void setHitboxes(Vector2 position) {

    }

}
