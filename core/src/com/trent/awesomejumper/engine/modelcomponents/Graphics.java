package com.trent.awesomejumper.engine.modelcomponents;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.trent.awesomejumper.engine.entity.Entity;

/**
 * Created by Sinthu on 09.12.2015.
 * Graphics component implementation. Holds textures and animations of game entities.
 * Manages current frame to be displayed.
 */
public class Graphics {

    // MEMBERS & INSTANCES
    // ---------------------------------------------------------------------------------------------
    public final int ANIMATIONS = 2; // supports 2 different animations at the moment
    public final int FRAMES = 5;     // 5 frames per animation

    private final float FRAME_DURATION;
    private String textureRegName;
    private TextureRegion idleFrameR, idleFrameL, currentFrame;
    private Array<TextureRegion> walkLeftFrames, walkRightFrames;

    private Animation walkLeftAnimation, walkRightAnimation;
    private Array<Animation> animations;

    private Entity entity;


    // CONSTRUCTOR
    // ---------------------------------------------------------------------------------------------
    public Graphics(float duration, Entity entity, String textureRegionName) {
        this.entity = entity;
        this.textureRegName = textureRegionName;
        this.animations = new Array<>(ANIMATIONS);
        this.walkLeftFrames = new Array<>(FRAMES);
        this.walkRightFrames = new Array<>(FRAMES);
        FRAME_DURATION = duration;

        // Enable graphics component
        entity.hasGraphics = true;
    }


    // METHODS
    // ---------------------------------------------------------------------------------------------

    /**
     * Creates the (walking) animations for the entity. Animations are kept in an array.
     */
    public void createWalkAnimations() {
        walkLeftAnimation = new Animation(FRAME_DURATION, walkLeftFrames);
        walkRightAnimation = new Animation(FRAME_DURATION, walkRightFrames);
        animations.add(walkLeftAnimation);
        animations.add(walkRightAnimation);
    }


    /**
     * Adds idle textures to the entity. The left idle texture is mirrored and used as the right
     * idle texture.
     * @param texture Texture gathered from an asset manager
     * TODO: add support for idle animations
     */
    public void setIdleFrames(TextureRegion texture) {
        idleFrameL = texture;
        idleFrameR = new TextureRegion(idleFrameL);
        idleFrameR.flip(true, false);
    }

    /**
     * Renders the graphical component of the entity. Manages the current frame to be displayed.
     * @param spriteBatch sprite batch used to draw textures.
     */
    public void render(SpriteBatch spriteBatch) {
        switch (entity.getState()) {
            case IDLE: currentFrame = entity.facingL ? idleFrameL : idleFrameR;
                break;
            case WALKING: currentFrame = entity.facingL ? walkLeftAnimation.getKeyFrame(entity.time, true) :
                                                          walkRightAnimation.getKeyFrame(entity.time,true);
                break;
            default:
                break;
        }

        spriteBatch.draw(currentFrame, entity.getPositionX(), entity.getPositionY(), entity.getWidth(), entity.getHeight());

    }


    /**
     * Adds frames to the arrays holding the animation frames.
     * @param frame current frame to be added.
     */
    public void addKeyFrame(TextureRegion frame) {
        walkLeftFrames.add(frame);
        try {
            walkRightFrames.add(new TextureRegion(walkLeftFrames.get(walkLeftFrames.size - 1)));
            walkRightFrames.get(walkRightFrames.size - 1).flip(true, false);
        }
        catch (IndexOutOfBoundsException e) {
            Gdx.app.log("ERROR","Could not add mirrored frames to walkRightArray.");
            e.printStackTrace();
        }
    }



    // GETTER AND SETTER
    // ---------------------------------------------------------------------------------------------
    public String getTextureRegName() {
        return textureRegName;
    }

    public Array<Animation> getAnimations() {
        return animations;
    }



}
