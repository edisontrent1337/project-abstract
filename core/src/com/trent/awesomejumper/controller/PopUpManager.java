package com.trent.awesomejumper.controller;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.trent.awesomejumper.engine.entity.Entity;
import com.trent.awesomejumper.engine.modelcomponents.popups.Message;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

/**
 * Manager class responsible for holding information about small messages ("popups") which have to be rendered.
 * Holds a queue of messages to be rendered. Decides, how to render messages depending on their category.
 * Does not depend on entity or event that creates the popup.
 * Better implementation than the previous popup component as the popup rendering was tied to the
 * existence of the entity.
 * Created by Sinthu on 30.03.2016.
 */
public class PopUpManager {

    private HashMap<PopUpCategories, LinkedList<Message>> messages;
    private final float MSG_FREQ = 12f;      // default frequency with which the message offset is modified
    private final float MSG_AMP = 0.125f;   // amplitude with which the message offset is modified
    private final float CRT_AMP = 5f;
    private final float CRT_FREQ = 0.75f;

    private static PopUpManager instance = null;

    private static Color HEAL = new Color(0.6784f,1f,0.1843f,1);
    private static Color DMG = new Color(1f,0.0745f,0.1137f,1);
    private static Color CRT = new Color(0.9098f,0.2666f,0.0745f, 1f);
    private static Color LVL_UP = new Color(0.9098f,0.9098f,0.0745f,1);
    private static Color MISC = Color.WHITE;


    public enum PopUpCategories {
        DMG,     // Damage
        HEAL,       // Heal
        CRT,   // Critical Hit
        LVL_UP,     // Level Up
        MISC        // Miscellaneous
    }


    public static PopUpManager getInstance() {
        return instance;
    }

    public static synchronized PopUpManager createPopUpManager() {
        if(instance == null)
            instance = new PopUpManager();
        return instance;
    }


    private float time = 0f;

    private PopUpManager() {
        this.messages = new HashMap<>();
        messages.put(PopUpCategories.DMG, new LinkedList<Message>());
        messages.put(PopUpCategories.HEAL, new LinkedList<Message>());
        messages.put(PopUpCategories.CRT, new LinkedList<Message>());
        messages.put(PopUpCategories.LVL_UP, new LinkedList<Message>());
        messages.put(PopUpCategories.MISC, new LinkedList<Message>());
    }





    public void render(SpriteBatch spriteBatch, BitmapFont font) {
        if (messages.isEmpty())
            return;
        /**
         * Iterate over the entrySet of the message HashMap and render each message by category.
         */
        for (Map.Entry<PopUpCategories, LinkedList<Message>> entry : messages.entrySet()) {
            LinkedList<Message> messageList = entry.getValue();
            time = WorldController.worldTime;

            for (Iterator<Message> it = messageList.iterator(); it.hasNext(); ) {
                Message message = it.next(); // get the current message
                float progress = (time - message.getTimeStamp()) / message.getDuration();
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
                        xOffset = (float) Math.cos((time- message.getTimeStamp()) * MSG_FREQ) * MSG_AMP;
                        break;
                    case HEAL:
                        font.setColor(HEAL.r, HEAL.g, HEAL.b, 1-progress);
                        break;
                    case CRT:
                        font.setColor(CRT.r + (float)Math.cos(time*CRT_AMP*progress)*CRT_FREQ,
                                CRT.g + (float)Math.sin(time*CRT_AMP*progress)*CRT_FREQ,
                                CRT.b + (float)Math.tan(time*CRT_AMP*progress)*CRT_FREQ,
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
                font.draw(spriteBatch, message.getMessage(), message.getPosition().x + xOffset, message.getPosition().y + yOffset);
                font.getData().setScale(1f/RenderingEngine.ppuX, 1f/RenderingEngine.ppuY);

            }

        }


    }




    /**
     * Adds a message to a corresponding category in the message hash map.
     * @param category category
     * @param message message
     */
    public void addMessageToCategory(PopUpCategories category, Message message) {
        if(!messages.containsKey(category))
            return;
        messages.get(category).add(message);
    }




}
