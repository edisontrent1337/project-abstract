package com.trent.awesomejumper.models;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.trent.awesomejumper.engine.entity.Entity;
import com.trent.awesomejumper.engine.modelcomponents.Body;
import com.trent.awesomejumper.engine.physics.CollisionBox;

/**
 * Created by Sinthu on 12.06.2015.
 */
public class SkyBox extends Entity {


    // CONSTRUCTOR
    // ---------------------------------------------------------------------------------------------

    public SkyBox(Vector2 position, float width, float speed) {
        body = new Body(this, width, width);
        body.setBounds(new CollisionBox(position, 2f * width, 2 * 0.5625f * width));
        body.setVelocity(speed, 0);
    }

    @Override
    public void update(float delta) {
        super.update(delta);
    }


}
