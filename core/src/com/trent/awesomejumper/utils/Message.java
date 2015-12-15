package com.trent.awesomejumper.utils;


import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;

/**
 * Created by Sinthu on 13.12.2015.
 * Graphical representation of small game events. Holds information about entity events such as
 * taking damage, leveling up or other small messages that might be displayed.
 */
public class Message<E> {


    private TextureRegion texture;
    private Color color;
    private String message;
    private Array<E> messageData;
    private float timeStamp, duration;

    private final float DEFAULT_DURATION = 1.00f;
    private final Color DEFALUT_COLOR = new Color(1,1,1,1);

    public final float ANIMATION_FREQUNCY = 7f;
    public final float ANIMATION_ = 7f;


    // CONSTRUCTOR
    // ---------------------------------------------------------------------------------------------
    public Message(String message, float timeStamp) {
        this.message = message;
        this.timeStamp = timeStamp;
        this.duration = DEFAULT_DURATION;
        this.color = DEFALUT_COLOR;
    }


    public Message(String message, float timeStamp, float duration, Color color) {
        this(message,null,timeStamp,duration,color);
    }


    public Message(String message, TextureRegion texture, float timeStamp, float duration, Color color) {
        this.message = message;
        this.texture = texture;
        this.timeStamp = timeStamp;
        this.duration = duration;
        this.color = color;

    }

    // GETTER & SETTER
    // ---------------------------------------------------------------------------------------------

    public String getMessage() {
        return message;
    }

    public float getTimeStamp() {
        return timeStamp;
    }

    public TextureRegion getTexture() {
        return texture;
    }

    public void setTexure(TextureRegion texture) {
        this.texture = texture;
    }


    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Array<E> getMessageData() {
        return messageData;
    }

    public void setMessageData(Array<E> messageData) {
        this.messageData = messageData;
    }


    public float getDuration() {
        return duration;
    }

    public void setDuration(float duration) {
        this.duration = duration;
    }

}
