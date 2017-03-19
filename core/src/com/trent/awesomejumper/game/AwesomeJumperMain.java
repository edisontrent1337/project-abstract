package com.trent.awesomejumper.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.assets.AssetManager;
import com.trent.awesomejumper.screens.SplashScreen;

public class AwesomeJumperMain extends Game {

    private static boolean debugMode = false;
    private static boolean logging = true;
    private static boolean drawEntities = true;
    private static boolean drawHitboxes = false;
    private static boolean drawInfo = false;
    private static boolean drawBody = false;

    public AssetManager assetManager = new AssetManager();

    @Override
    public void create() {
        setScreen(new SplashScreen(this));
    }

    public AssetManager getAssetManager() {
        return assetManager;
    }

    public static void toggleDebugMode() {debugMode = !debugMode;
    }

    public static void toggleLogging() {
        logging = !logging;
    }

    public static boolean onLogging() {
        return logging;
    }

    public static boolean onDebugMode() {
        return debugMode;
    }

    public static void toggleEntityDrawing() {
        drawEntities = !drawEntities;
    }

    public static void toggleHitboxDrawing() {
        drawHitboxes = !drawHitboxes;
    }

    public static void toggleInfoDrawing() {
        drawInfo = !drawInfo;
    }

    public static void toggleBodyDrawing() {
        drawBody = !drawBody;
    }


    public boolean entitiesEnabled() {
        return drawEntities;
    }

    public boolean hitboxesEnabled() {
        return drawHitboxes;
    }

    public boolean infoEnabled() {
        return drawInfo;
    }

    public boolean bodyEnabled() {
        return drawBody;
    }

}
