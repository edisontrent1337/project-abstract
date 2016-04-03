package com.trent.awesomejumper.engine.modelcomponents.popups;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.trent.awesomejumper.controller.PopUpManager;
import com.trent.awesomejumper.controller.RenderingEngine;
import com.trent.awesomejumper.engine.entity.Entity;
import com.trent.awesomejumper.engine.modelcomponents.ModelComponent;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

/**
 * Component responsible for holding information about small messages ("popups") which have to be rendered.
 * Holds a queue of messages to be rendered. Decides, how to render messages depending on their category.
 * Created by Sinthu on 19.12.2015.
 */
public class PopUpFeed extends ModelComponent {

    private Entity entity;
    private HashMap<PopUpManager.PopUpCategories, LinkedList<Message>> messages;
    private final float MSG_FREQ = 12f;      // default frequency with which the message offset is modified
    private final float MSG_AMP = 0.125f;   // amplitude with which the message offset is modified
    private final float CRT_AMP = 5f;
    private final float CRT_FREQ = 0.75f;

    private static Color HEAL = new Color(0.6784f,1f,0.1843f,1);
    private static Color DMG = new Color(1f,0.0745f,0.1137f,1);
    private static Color CRT = new Color(0.9098f,0.2666f,0.0745f, 1f);
    private static Color LVL_UP = new Color(0.9098f,0.9098f,0.0745f,1);
    private static Color MISC = Color.WHITE;


    /*public enum PopUpCategories {
        DMG,     // Damage
        HEAL,       // Heal
        CRT,   // Critical Hit
        LVL_UP,     // Level Up
        MISC        // Miscellaneous


    }*/
    

    // CONSTRUCTOR
    // ---------------------------------------------------------------------------------------------
    public PopUpFeed(Entity entity) {
        this.entity = entity;
        this.messages = new HashMap<>();
        this.putMessageCategory(PopUpManager.PopUpCategories.DMG);
        this.putMessageCategory(PopUpManager.PopUpCategories.HEAL);
        this.putMessageCategory(PopUpManager.PopUpCategories.CRT);
        this.putMessageCategory(PopUpManager.PopUpCategories.LVL_UP);
        this.putMessageCategory(PopUpManager.PopUpCategories.MISC);
        entity.hasPopUps = true;
    }


    // METHODS
    // ---------------------------------------------------------------------------------------------
    /**
     * Renders event messages sorted by category e.g. information when entities take damage or level
     * up using the class Message which can hold Strings and Textures to be displayed.
     * @param spriteBatch
     * @param font
     */
    public void render(SpriteBatch spriteBatch, BitmapFont font) {
        if (messages.isEmpty())
            return;
        /**
         * Iterate over the entrySet of the message HashMap and render each message by category.
         */
        for (Map.Entry<PopUpManager.PopUpCategories, LinkedList<Message>> entry : messages.entrySet()) {
            LinkedList<Message> messageList = entry.getValue();

            for (Iterator<Message> it = messageList.iterator(); it.hasNext(); ) {
                Message message = it.next(); // get the current message
                float progress = (entity.time - message.getTimeStamp()) / message.getDuration();
                /**
                 * If the messages timeStamp is older than its duration, it will get removed
                 * from the message list.
                 */

                if(progress >= 1) {
                    it.remove();
                    continue;
                }
                float xOffset = 0f;
                float yOffset = 3*progress;
                switch (entry.getKey()) {
                    case DMG:
                        font.setColor(DMG.r, DMG.g, DMG.b, 1 - progress);
                        xOffset = (float) Math.cos((entity.time - message.getTimeStamp()) * MSG_FREQ) * MSG_AMP;
                        break;
                    case HEAL:
                        font.setColor(HEAL.r, HEAL.g, HEAL.b, 1-progress);
                        break;
                    case CRT:
                        font.setColor(CRT.r + (float)Math.cos(entity.time*CRT_AMP*progress)*CRT_FREQ,
                                      CRT.g + (float)Math.sin(entity.time*CRT_AMP*progress)*CRT_FREQ,
                                      CRT.b + (float)Math.tan(entity.time*CRT_AMP*progress)*CRT_FREQ,
                                      1-progress);
                        break;
                    case LVL_UP:
                        font.setColor(LVL_UP.r, LVL_UP.g, LVL_UP.b, 1-progress);
                        float scaleX =  font.getScaleX() + 0.02f*progress;
                        float scaleY =  font.getScaleY() + 0.02f*progress;
                        font.getData().setScale(scaleX,scaleY);
                        break;
                    case MISC:
                        font.setColor(MISC.r, MISC.g, MISC.b, 1-progress);
                        break;

                }
                /**
                 * Draw message to screen. Always reset the scaling after rendering.
                 */
                font.draw(spriteBatch, message.getMessage(), entity.getPosition().x + entity.getWidth() / 2 + xOffset, entity.getPosition().y + entity.getHeight() + yOffset);
                font.getData().setScale(1f/RenderingEngine.ppuX, 1f/RenderingEngine.ppuY);

            }

        }


    }




    /**
     * Adds a new category and an empty message queue to the message hash map.
     * @param category category
     */
    public void putMessageCategory(PopUpManager.PopUpCategories category) {
        if(messages.containsKey(category))
            return;
        messages.put(category, new LinkedList<Message>());
    }

    /**
     * Adds a new category and an initial message queue to the message hash map.
     * @param category category
     * @param messageList message queue
     */
    public void putMessageList(PopUpManager.PopUpCategories category, LinkedList<Message>messageList) {
        if(messages.containsKey(category)) {
            return;
        }
        messages.put(category, messageList);

    }

    /**
     * Adds a message to a corresponding category in the message hash map.
     * @param category category
     * @param message message
     */
    public void addMessageToCategory(PopUpManager.PopUpCategories category, Message message) {
        if(!messages.containsKey(category))
            return;
        messages.get(category).add(message);
    }

    /**
     * Adds a message queue to a corresponding category in the message hash map.
     * @param category category
     * @param messageList message queue
     */
    public void addMessageListToCategory(PopUpManager.PopUpCategories category, LinkedList<Message> messageList) {
        if(!messages.containsKey(category))
            return;
        messages.get(category).addAll(messageList);
    }
    /**
     * Removes the last message from the message queue of the corresponding category.
     * @param category category String
     */
    public void removeMessageFromCategory(PopUpManager.PopUpCategories category) {
        if(!messages.containsKey(category))
            return;
        messages.get(category).removeFirst();

    }

    public HashMap<PopUpManager.PopUpCategories, LinkedList<Message>> getMap() {
        return messages;
    }

}