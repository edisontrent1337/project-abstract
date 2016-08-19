package com.trent.awesomejumper.controller.rendering;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.math.Vector2;
import com.trent.awesomejumper.controller.WorldController;
import com.trent.awesomejumper.engine.modelcomponents.popups.Message;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import static com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.*;

/**
 * Manager class responsible for holding information about small messages ("popups") which have to be rendered.
 * Holds a queue of messages to be rendered. Decides, how to render messages depending on their category.
 * Does not depend on entity or event that creates the popup.
 * Better implementation than the previous popup component as the popup rendering was tied to the
 * existence of the entity.
 * TODO: GIVE POPUP ITS OWN FONT
 * Created by Sinthu on 30.03.2016.
 */
public class PopUpRenderer extends Renderer {

    private HashMap<PopUpCategories, LinkedList<Message>> messages;
    private final float MSG_FREQ = 12f;      // default frequency with which the message offset is modified
    private final float MSG_AMP = 8f;   // amplitude with which the message offset is modified
    private final float CRT_AMP = 12f;
    private final float CRT_FREQ = 25f;


    private static PopUpRenderer instance = null;

    private static Color HEAL = new Color(0.6784f, 1f, 0.1843f, 1);
    private static Color DMG = new Color(1f, 0.0745f, 0.1137f, 1);
    private static Color CRT = new Color(0.9098f, 0.2666f, 0.0745f, 1f);
    private static Color LVL_UP = new Color(0.9098f, 0.9098f, 0.0745f, 1);
    private static Color MISC = Color.WHITE;

    private BitmapFont popUpFont;

    public enum PopUpCategories {
        DMG,     // Damage
        HEAL,       // Heal
        CRT,   // Critical Hit
        LVL_UP,     // Level Up
        MISC        // Miscellaneous
    }

    private float r,g,b,a;

    public static PopUpRenderer getInstance() {
        return instance;
    }

    public static synchronized PopUpRenderer createPopUpRenderer() {
        if (instance == null)
            instance = new PopUpRenderer();
        return instance;
    }


    private float time = 0f;


    private static final float CAMERA_WIDTH = Gdx.graphics.getWidth();
    private static final float CAMERA_HEIGHT = Gdx.graphics.getHeight();

    private PopUpRenderer() {
        // Init renderer, start with a default camera and sprite batch
        super(CAMERA_WIDTH, CAMERA_HEIGHT);
        camera.position.set(CAMERA_WIDTH / 2, CAMERA_HEIGHT / 2, 0);
        this.messages = new HashMap<>();
        messages.put(PopUpCategories.DMG, new LinkedList<Message>());
        messages.put(PopUpCategories.HEAL, new LinkedList<Message>());
        messages.put(PopUpCategories.CRT, new LinkedList<Message>());
        messages.put(PopUpCategories.LVL_UP, new LinkedList<Message>());
        messages.put(PopUpCategories.MISC, new LinkedList<Message>());

    }

    @Override
    public void render() {
        if (messages.isEmpty())
            return;

        /**
         * Camera setup. The camera works in screen coordinates (pixel perfect precision).
         * Uses the pixel perfect camera position of the main rendering engine.
         */
        camera.update();
        camera.position.x = RenderingEngine.camPositionInPx.x;
        camera.position.y = RenderingEngine.camPositionInPx.y;
        spriteBatch.setProjectionMatrix(camera.combined);
        spriteBatch.begin();
        {
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

                    if (progress >= 1) {
                        it.remove();
                        continue;
                    }
                    float xOffset = 0f;
                    float yOffset = -1/(progress*progress+0.01f) + 100;
                    switch (entry.getKey()) {
                        case DMG:
                            popUpFont.setColor(DMG.r, DMG.g, DMG.b, 1 - progress*progress);
                           // xOffset = (float) Math.cos((time - message.getTimeStamp()) * MSG_FREQ) * MSG_AMP;
                            break;
                        case HEAL:
                            popUpFont.setColor(HEAL.r, HEAL.g, HEAL.b, 1 - progress);
                            break;
                        case CRT:
                            r += Gdx.graphics.getDeltaTime();
                            g += Gdx.graphics.getDeltaTime();
                            b += Gdx.graphics.getDeltaTime();
                            popUpFont.setColor(CRT.r + (float)Math.sin(r*CRT_FREQ),CRT.g + (float) Math.cos(g*CRT_FREQ),CRT.b + (float) Math.tan(b*CRT_FREQ),1-progress*progress);
                            break;
                        case LVL_UP:
                            popUpFont.setColor(LVL_UP.r, LVL_UP.g, LVL_UP.b, 1 - progress);
                            float scaleX = popUpFont.getScaleX() + 0.02f * progress;
                            float scaleY = popUpFont.getScaleY() + 0.02f * progress;
                            popUpFont.getData().setScale(scaleX, scaleY);
                            break;
                        case MISC:
                            popUpFont.setColor(MISC.r, MISC.g, MISC.b, 1 - progress*progress);
                            break;

                    }
                    /**
                     * Draw message to screen.
                     */

                    GlyphLayout glyphLayout = new GlyphLayout();
                    glyphLayout.setText(popUpFont, message.getMessage());
                    float messageWidth = glyphLayout.width;
                    Vector2 unprojectedMessagePos = unprojectXToPixels(message.getPosition());
                    unprojectedMessagePos.x += xOffset;
                    unprojectedMessagePos.x -= messageWidth / 2f;
                    unprojectedMessagePos.y += yOffset;

                    popUpFont.draw(spriteBatch, glyphLayout, unprojectedMessagePos.x, unprojectedMessagePos.y);

                }

            }
        }
        spriteBatch.end();


    }

    /**
     * Adds a message to a corresponding category in the message hash map.
     *
     * @param category category
     * @param message  message
     */
    public void addMessageToCategory(PopUpCategories category, Message message) {
        if (!messages.containsKey(category))
            return;
        messages.get(category).add(message);
    }


    /**
     *
     */
    public void loadTexturesAndFonts(FreeTypeFontGenerator generator) {
        FreeTypeFontParameter popUpParams = new FreeTypeFontParameter();
        popUpParams.size = 32;
        popUpParams.shadowColor = new Color(99/255, 66/255,66/255,1);
        popUpParams.shadowOffsetX = 2;
        popUpParams.shadowOffsetY = 2;
        popUpFont = generator.generateFont(popUpParams);
    }

    public void resize(int w, int h) {
        camera = new OrthographicCamera(w,h);
        camera.update();
    }


    private Vector2 unprojectXToPixels(Vector2 position) {
        Vector2 result = new Vector2(0,0);
        result.x =(int) (position.x * RenderingEngine.ppuX );
        result.y =(int) (position.y * RenderingEngine.ppuY );
        return result;

    }

}
