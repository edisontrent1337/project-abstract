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
 * TODO: maybe outsource render method to RenderingEngine because component should more ore less
 * only represent data and functions to create or hold data, not to modify it as this is the task of
 * the controllers.
 */
public class Graphics extends ModelComponent{

    // MEMBERS & INSTANCES
    // ---------------------------------------------------------------------------------------------
    public final int ANIMATIONS = 2; // supports 2 different animations at the moment
    private final float FRAME_DURATION; // frame duration for animations
    private String textureRegName;      // prefix of the name of textures
    private TextureRegion idleFrameR, idleFrameL, currentFrame;
    private Array<TextureRegion> walkLeftFrames, walkRightFrames;

    private Animation walkLeftAnimation, walkRightAnimation;
    private Array<Animation> animations;


    private float width, height, alpha;

    // CONSTRUCTOR
    // ---------------------------------------------------------------------------------------------
    // TODO: Idea: Use textureRegionName to identify different graphic components.
    // TODO: textureRegionName can be used to address already created components in a HashMap of flyweights.
    //
    public Graphics(Entity entity,float frameDuration, String textureRegionName, float width, float height) {
        /**
         * Initialises all members with default values.
         * The renderingEngine then loads textures from the asset manager and applies a more useful
         * start configuration to all values.
         */
        this.entity = entity;
        this.textureRegName = textureRegionName;
        this.animations = new Array<>(ANIMATIONS);
        this.walkLeftFrames = new Array<>();
        this.walkRightFrames = new Array<>();
        this.width = width;
        this.height = height;

        FRAME_DURATION = frameDuration;
        alpha = 1f;
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

        spriteBatch.setColor(spriteBatch.getColor().r, spriteBatch.getColor().g, spriteBatch.getColor().b, alpha);
        spriteBatch.draw(currentFrame, entity.getPosition().x, entity.getPosition().y, width, height);

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

    public float getFrameDuration() {
        return FRAME_DURATION;
    }

    public Array<Animation> getAnimations() {
        return animations;
    }

    public void setTextureRegName(String textureRegName) {
        this.textureRegName = textureRegName;
    }

    public void setAlpha(float alpha) {
        this.alpha = alpha;
    }

    public float getAlpha() {
        return alpha;
    }





}


