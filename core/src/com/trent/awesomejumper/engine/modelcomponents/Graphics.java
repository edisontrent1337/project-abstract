package com.trent.awesomejumper.engine.modelcomponents;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.trent.awesomejumper.engine.entity.Entity;
import com.trent.awesomejumper.utils.Message;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

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
    public final int FRAMES;     // 5 frames per animation

    private final float FRAME_DURATION; // frame duration for animations
    private String textureRegName;      // prefix of the name of textures
    private TextureRegion idleFrameR, idleFrameL, currentFrame;
    private Array<TextureRegion> walkLeftFrames, walkRightFrames;

    private Animation walkLeftAnimation, walkRightAnimation;
    private Array<Animation> animations;

    // messages to be rendered by category
    private HashMap<String,LinkedList<Message>> messages;


    // CONSTRUCTOR
    // ---------------------------------------------------------------------------------------------
    public Graphics(Entity entity,float frameDuration, String textureRegionName, int frames) {
        /**
         * Initialises all members with default values.
         * The renderingEngine then loads textures from the asset manager and applies a more useful
         * start configuration to all values.
         */
        FRAMES = frames;
        this.entity = entity;
        this.textureRegName = textureRegionName;
        this.animations = new Array<>(ANIMATIONS);
        this.walkLeftFrames = new Array<>(FRAMES);
        this.walkRightFrames = new Array<>(FRAMES);
        this.messages = new HashMap<>();

        FRAME_DURATION = frameDuration;

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

        spriteBatch.draw(currentFrame, entity.getPosition().x, entity.getPosition().y, entity.getWidth(), entity.getHeight());
        //TODO: Call here renderMessages e.g. if(!messages.isEmpty) renderMessages...

    }

    /**
     * Renders event messages sorted by category e.g. information when entities take damage or level
     * up using the class Message which can hold Strings and Textures to be displayed.
     * @param spriteBatch
     * @param font
     */
    public void renderMessages(SpriteBatch spriteBatch, BitmapFont font) {
        for(Map.Entry<String, LinkedList<Message>> entry : messages.entrySet()) {
            LinkedList<Message> messageList = entry.getValue();
            for(Iterator<Message> it = messageList.iterator(); it.hasNext();) {
                Message m = it.next();
                if(entity.time - m.getTimeStamp() > m.getDuration()) {
                    it.remove();
                    continue;
                }

                //TODO: add constants here for different parameters.
                float alpha = (entity.time - m.getTimeStamp()) / m.getDuration();
                float offset = (float)Math.cos(entity.time*7f)*0.125f;

                font.setColor(m.getColor().r, m.getColor().g, m.getColor().b, 1 - alpha);
                font.draw(spriteBatch, m.getMessage(), entity.getPosition().x + entity.getWidth() / 2 + offset, entity.getPosition().y + entity.getHeight() + 2*alpha);

            }

        }

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

    /**
     * Adds a new category and an empty message queue to the message hash map.
     * @param category category String
     */
    public void putMessageCategory(String category) {
        if(messages.containsKey(category))
            return;
        messages.put(category, new LinkedList<Message>());
    }

    /**
     * Adds a new category and an initial message queue to the message hash map.
     * @param category category String
     * @param messageList message queue
     */
    public void putMessageList(String category, LinkedList<Message>messageList) {
        if(messages.containsKey(category)) {
            return;
        }
        messages.put(category, messageList);

    }

    /**
     * Adds a message to a corresponding category in the message hash map.
     * @param category category String
     * @param message message String
     */
    public void addMessageToCategory(String category, Message message) {
        if(!messages.containsKey(category))
            return;
        messages.get(category).add(message);
    }

    /**
     * Adds a message queue to a corresponding category in the message hash map.
     * @param category category String
     * @param messageList message queue
     */
    public void addMessageListToCategory(String category, LinkedList<Message>messageList) {
        if(!messages.containsKey(category))
            return;
        messages.get(category).addAll(messageList);
    }
    /**
     * Removes the last message from the message queue of the corresponding category.
     * @param category category String
     */
    public void removeMessageFromCategory(String category) {
        if(!messages.containsKey(category))
            return;
        messages.get(category).removeFirst();

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





}


