package com.trent.awesomejumper.controller.assets;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;

/**
 * Created by Sinthu on 17.08.2016.
 */
public class AssetLoader {


    public static AssetManager assetManager = new AssetManager();

    public static final AssetDescriptor<TextureAtlas> textureAtlas = new AssetDescriptor<TextureAtlas>("img/textures.pack", TextureAtlas.class);
    public static final String path = "img/textures.pack";


    public static void load() {
        assetManager.load(path,TextureAtlas.class);
        assetManager.load(textureAtlas);
    }

}
