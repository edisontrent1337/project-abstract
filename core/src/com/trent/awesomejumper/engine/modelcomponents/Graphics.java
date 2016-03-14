package com.trent.awesomejumper.engine.modelcomponents;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.trent.awesomejumper.controller.RenderingEngine;
import com.trent.awesomejumper.engine.entity.Entity;
import com.trent.awesomejumper.utils.PhysicalConstants;

import static com.trent.awesomejumper.utils.Utilities.dot;

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
    public final int ANIMATIONS = 4; // supports 3 different animations at the moment
    private final float FRAME_DURATION; // frame duration for animations
    private String textureRegName;      // prefix of the name of textures
    private TextureRegion idleFrameR, idleFrameL, idleFrameU, idleFrameD, currentFrame, shadow;
    private Array<TextureRegion> walkLeftFrames, walkRightFrames, walkUpFrames, walkDownFrames, idleFrames;

    private Animation walkLeftAnimation, walkRightAnimation, walkUpAnimation, walkDownAnimation;
    private Array<Animation> animations;


    private float width, height, alpha, originX, originY;
    private boolean supportsRotation = false, supportsShadowRotation = true;
    boolean flipX = false;
    boolean flipped = false;

    // CONSTRUCTOR
    // ---------------------------------------------------------------------------------------------
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
        this.originX = width / 2f;
        this.originY = height / 2f;

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
     * Adds idle textures to the entity. The right idle texture is mirrored and used as the left
     * idle texture.
     * @param texture Texture gathered from an asset manager
     * TODO: add idle animations
     */
    public void setIdleFrames(TextureRegion texture) {
        idleFrameR = texture;
        idleFrameL = new TextureRegion(idleFrameR);
        idleFrameL.flip(true, false);
    }


    /**
     * Adds shadow texture to the entity.
     */
    public void setShadow(TextureRegion shadow) {
        this.shadow = shadow;
    }

    /**
     * Renders the graphical component of the entity. Manages the current frame to be displayed.
     * @param sb sprite batch used to draw textures.
     */
    public void render(SpriteBatch sb) {
        switch (entity.getState()) {
            case IDLE: currentFrame = entity.facingL ? idleFrameL : idleFrameR;
                break;
            case WALKING: currentFrame = entity.facingL ? walkLeftAnimation.getKeyFrame(entity.time, true) :
                                                          walkRightAnimation.getKeyFrame(entity.time,true);
                break;
            default:
                break;
        }
        sb.setColor(sb.getColor().r, sb.getColor().g, sb.getColor().b, alpha);

        float x =(int) Math.floor(entity.getPosition().x * RenderingEngine.ppuX) / RenderingEngine.ppuX;
        float y =(int) Math.floor(entity.getPosition().y * RenderingEngine.ppuY) / RenderingEngine.ppuY;

        if(supportsRotation) {
            float originX = width / 2f;
            float originY = height / 2f;
            float angle = entity.getBody().getAngleOfRotation();

            if(angle > 90 && angle < 270 &! currentFrame.isFlipY()) {
                currentFrame.flip(false,true);
            }
           else if(((angle >= 270 && angle < 360) || (angle >= 0 && angle < 90)) && currentFrame.isFlipY()) {
                currentFrame.flip(false,true);
            }

                if(supportsShadowRotation)
                    sb.draw(shadow, x, y, originX, originY, width, height, 1, 1, entity.getBody().getAngleOfRotation());
                else
                    sb.draw(shadow, x, y, originX, originY, width, height, 1, 1, 0);
                sb.draw(currentFrame, x, y + entity.getBody().getZOffset(), originX, originY, width, height, 1, 1, entity.getBody().getAngleOfRotation());



        }
        else {
            sb.draw(shadow, x,  y, width, height);
            sb.draw(currentFrame, x,  y + entity.getBody().getZOffset(), width, height);
        }
        //TODO Draw head separately from body and let it rotate towards mouse
        //TODO Insert a flag that specifies whether or not an entity is rotatable or not
    }


    /**
     * Adds frames to the arrays holding the animation frames.
     * @param frame current frame to be added.
     */
    public void addKeyFrame(TextureRegion frame) {
        walkRightFrames.add(frame);
        try {
            walkLeftFrames.add(new TextureRegion(walkRightFrames.get(walkRightFrames.size - 1)));
            walkLeftFrames.get(walkLeftFrames.size - 1).flip(true, false);
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

    public void setNumberOfAnimations(int numberOfAnimations) {

    }

    public void enableRotations() {
        supportsRotation = true;
    }

    public void disableShadowRotations() {
        supportsShadowRotation = false;
    }



}


