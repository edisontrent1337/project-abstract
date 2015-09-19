package com.trent.awesomejumper.models;

import com.badlogic.gdx.math.Vector2;

/**
 * Created by Sinthu on 12.06.2015.
 */
public class SkyBox extends Entity {


    // CONSTRUCTOR
    // ---------------------------------------------------------------------------------------------

    public SkyBox(Vector2 position, float width, float speed) {
        super(position);
        setBoundDimensions(2f*width,2*0.5625f*width);
        setVelocity(speed,0);
    }

    @Override
    public void update(float delta) {
        position.add(velocity.cpy().scl(delta));
    }

    @Override
    public void setHitboxes(Vector2 position) {

    }

}
