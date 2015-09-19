package com.trent.awesomejumper.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.assets.AssetManager;
import com.trent.awesomejumper.screens.SplashScreen;

public class AwesomeJumperMain extends Game {

    private boolean debugMode = false;

    public AssetManager assetManager = new AssetManager();
    @Override
    public void create() {
        setScreen(new SplashScreen(this));
    }

    public AssetManager getAssetManager() {
        return assetManager;
    }

    public void setDebugMode(boolean mode) {
        this.debugMode = mode;
    }

    public boolean onDebugMode() {
        return debugMode;
    }


}
