package com.trent.awesomejumper.controller.rendering;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.trent.awesomejumper.utils.Utilities;

/**
 * Created by Sinthu on 18.08.2016.
 */
public abstract class Renderer {


    protected static final Color SHADOW_COLOR = Utilities.color(33,33,33,0.5f);
    protected static final Color BORDER_COLOR = Utilities.color(50,50,50,0.77f);

    protected SpriteBatch spriteBatch;
    protected OrthographicCamera camera;
    protected ShapeRenderer rectRenderer;



    public Renderer(float viewPortWidth, float viewPortHeight) {
        this.camera = new OrthographicCamera(viewPortWidth, viewPortHeight);
        this.spriteBatch = new SpriteBatch();
        this.rectRenderer = new ShapeRenderer();

    }

    public abstract void render();

    public abstract void resize(int w, int h);

}
