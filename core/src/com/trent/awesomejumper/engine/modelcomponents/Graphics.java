package com.trent.awesomejumper.engine.modelcomponents;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Array;
import com.trent.awesomejumper.controller.rendering.RenderingEngine;
import com.trent.awesomejumper.engine.entity.Entity;

import static com.trent.awesomejumper.engine.modelcomponents.ModelComponent.ComponentID;

/**
 * Created by Sinthu on 09.12.2015.
 * Graphics component implementation. Holds textures and animations of game entities.
 * Manages current frame to be displayed.
 */
public class Graphics extends ModelComponent {

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
    private boolean visible = false;

    private ShapeRenderer hpRenderer;

    private final float HEALTH_BAR_WIDTH = 1f;
    private final float HEALTH_BAR_HEIGHT = 0.075f;
    private final float HEALTH_BAR_YOFFSET = 0.7f;
    private final float HEALTH_ALPHA_TIMEOUT = 1.50f;

    private float fadeOutStart = 0f;
    private boolean startFade = false;
    private boolean fadedOut = false;
    private float healthBarAlpha = 1f;

    // CONSTRUCTOR
    // ---------------------------------------------------------------------------------------------
    //
    public Graphics(Entity entity, float frameDuration, String textureRegionName, float width, float height) {
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

        this.FRAME_DURATION = frameDuration;
        alpha = 1f;
        // Enable graphics component
        entity.enableComponent(ComponentID.GRAPHICS);
        visible = true;

        hpRenderer = new ShapeRenderer();
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
     *
     * @param texture Texture gathered from an asset manager
     *                TODO: add idle animations
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
     *
     * @param sb sprite batch used to draw textures.
     */
    public void render(SpriteBatch sb) {
        // If the sprite is set invisible, it will not be rendered.
        if (!visible)
            return;

        switch (entity.getState()) {
            case IDLE:
                currentFrame = entity.facingL ? idleFrameL : idleFrameR;
                break;
            case WALKING:
                currentFrame = entity.facingL ? walkLeftAnimation.getKeyFrame(entity.time, true) :
                        walkRightAnimation.getKeyFrame(entity.time, true);
                break;
            default:
                break;
        }
        sb.setColor(sb.getColor().r, sb.getColor().g, sb.getColor().b, alpha);

        float x = (int) Math.floor(entity.getPosition().x * RenderingEngine.ppuX) / RenderingEngine.ppuX;
        float y = (int) Math.floor(entity.getPosition().y * RenderingEngine.ppuY) / RenderingEngine.ppuY;

        if (supportsRotation) {
            float originX = width / 2f;
            float originY = height / 2f;
            float angle = entity.getBody().getAngleOfRotation();

            if (angle > 90 && angle < 270 & !currentFrame.isFlipY()) {
                currentFrame.flip(false, true);
            } else if (((angle >= 270 && angle < 360) || (angle >= 0 && angle < 90)) && currentFrame.isFlipY()) {
                currentFrame.flip(false, true);
            }

            if (supportsShadowRotation)
                sb.draw(shadow, x, y, originX, originY, width, height, 1, 1, entity.getBody().getAngleOfRotation());
            else
                sb.draw(shadow, x, y, originX, originY, width, height, 1, 1, 0);
            sb.draw(currentFrame, x, y + entity.getBody().getZOffset(), originX, originY, width, height, 1, 1, entity.getBody().getAngleOfRotation());


        } else {
            sb.draw(shadow, x, y, width, height);
            sb.draw(currentFrame, x, y + entity.getBody().getZOffset(), width, height);
        }

        /**
         * HP BAR Rendering
         */
        sb.end();

        /*hpRenderer.setProjectionMatrix(sb.getProjectionMatrix());
        hpRenderer.begin(ShapeRenderer.ShapeType.Point);
        hpRenderer.point(entity.getBody().getCenter().x,entity.getBody().getCenter().y,0);
        hpRenderer.end();*/


            {
                if(entity.has(ComponentID.HEALTH))
                renderHealthBar(sb.getProjectionMatrix());
            }

        sb.begin();


        //TODO Draw head separately from body and let it rotate towards mouse
    }


    private void renderHealthBar(Matrix4 projectionMatrix) {
            Health health = entity.getHealth();
            if (health.getHp() == health.getMaxHp())
                return;


            float time = entity.time;
            float tookDamageAt = health.tookDamageAt();


            if (time - tookDamageAt < HEALTH_ALPHA_TIMEOUT & !fadedOut) {
                startFade = true;
                fadeOutStart = entity.time;
            } else if (time - tookDamageAt >= HEALTH_ALPHA_TIMEOUT && fadedOut) {
                fadedOut = false;
            }


            if (startFade & !fadedOut) {
                float progress = (time - fadeOutStart) / 1;
                if (progress <= 1)
                    healthBarAlpha = 1 - 0.5f * progress * progress;
                else {
                    fadedOut = true;
                    startFade = false;
                }
            }


            Gdx.gl.glEnable(GL20.GL_BLEND);
            Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

            float x =(int) Math.floor((entity.getBody().getCenter().x - HEALTH_BAR_WIDTH / 2)*RenderingEngine.ppuX)/RenderingEngine.ppuX;
            float y =(int) Math.floor((entity.getBody().getCenter().y + HEALTH_BAR_YOFFSET)*RenderingEngine.ppuY)/RenderingEngine.ppuY;

            hpRenderer.setProjectionMatrix(projectionMatrix);
            hpRenderer.begin(ShapeRenderer.ShapeType.Filled);

            hpRenderer.setColor(new Color(1, 0, 0, healthBarAlpha));
            hpRenderer.rect(x, y, HEALTH_BAR_WIDTH, HEALTH_BAR_HEIGHT);

            float width = health.getHp() / health.getMaxHp();
            int squares = (int) (width / 0.1f);
            float remainder = width - squares*0.1f;
            //hpRenderer.setColor(new Color(1, 1f, 1f, healthBarAlpha));
            hpRenderer.rect(x,y,width,HEALTH_BAR_HEIGHT);


            hpRenderer.setColor(new Color(0, 1, 0, healthBarAlpha));
           /* for (int i = 0; i < squares; i++) {
                hpRenderer.rect(x + i*0.1f,y, 0.085f,HEALTH_BAR_HEIGHT);
            }
                hpRenderer.rect(x + (squares)*0.1f,y, remainder,HEALTH_BAR_HEIGHT);*/
            hpRenderer.rect(x,y, health.getHp() / health.getMaxHp(), HEALTH_BAR_HEIGHT);
            hpRenderer.setColor(new Color(74f/255, 97f/255, 63/255f, healthBarAlpha));
            hpRenderer.rect(x,y, health.getHp() / health.getMaxHp(), HEALTH_BAR_HEIGHT/4f);

            hpRenderer.setColor(1,1,0,healthBarAlpha);
            float progress = (time - tookDamageAt) / 1f;
            if(progress <= 1) {
                hpRenderer.rect(x + width, y, (1-progress*progress*progress) * (health.getLastDamage() / health.getMaxHp()), HEALTH_BAR_HEIGHT);
                Gdx.app.log("PRO", Double.toString(((float) Math.pow(2, -progress*progress)) * (health.getLastDamage() / health.getMaxHp())));
            }

            hpRenderer.end();

            Gdx.gl.glDisable(GL20.GL_BLEND);
    }


    /**
     * Adds frames to the arrays holding the animation frames.
     *
     * @param frame current frame to be added.
     */
    public void addKeyFrame(TextureRegion frame) {
        walkRightFrames.add(frame);
        try {
            walkLeftFrames.add(new TextureRegion(walkRightFrames.get(walkRightFrames.size - 1)));
            walkLeftFrames.get(walkLeftFrames.size - 1).flip(true, false);
        } catch (IndexOutOfBoundsException e) {
            Gdx.app.log("ERROR", "Could not add mirrored frames to walkRightArray.");
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

    public void show() {
        visible = true;
    }

    public void hide() {
        visible = false;
    }


}


