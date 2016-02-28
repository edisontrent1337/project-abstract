package com.trent.awesomejumper.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.assets.AssetManager;
import com.trent.awesomejumper.screens.SplashScreen;

public class AwesomeJumperMain extends Game {

    private boolean debugMode = false;
    private boolean drawEntities = true;
    private boolean drawHitboxes = false;
    private boolean drawInfo = false;
    private boolean drawBody = false;

    public AssetManager assetManager = new AssetManager();
    @Override
    public void create() {
        setScreen(new SplashScreen(this));
    }

    public AssetManager getAssetManager() {
        return assetManager;
    }

    public void toggleDebugMode() {
        this.debugMode = !debugMode;
    }

    public boolean onDebugMode() {
        return debugMode;
    }

    public void toggleEntities() {
        drawEntities = !drawEntities;
    }

    public void toggleHitboxes() {
        drawHitboxes = !drawHitboxes;
    }

    public void toggleInfo() {
        drawInfo = !drawInfo;
    }

    public void toggleBody() {
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
