package com.trent.awesomejumper.engine.modelcomponents;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.trent.awesomejumper.engine.entity.Entity;

/**
 * Created by Sinthu on 09.12.2015.
 * Graphics component implementation. Holds textures and animations of game entities.
 */
public class Graphics {

    private final float FRAME_DURATION;
    private TextureRegion idleFrameR, idleFrameL;
    private TextureRegion[] walkLeftFrames, walkRightFrames;

    private Animation walkLeftAnimation, walkRightAnimation;

    private Entity entity;

    public Graphics(float duration, Entity entity) {
        this.entity = entity;
        entity.hasGraphics = true;
        FRAME_DURATION = duration;
    }



    public void createWalkAnimations() {
        walkLeftAnimation = new Animation(FRAME_DURATION, walkLeftFrames);
        walkRightAnimation = new Animation(FRAME_DURATION, walkRightFrames);

    }

    public void setTexture(TextureRegion texture) {

    }

    public void render(SpriteBatch spriteBatch) {

    }


}
