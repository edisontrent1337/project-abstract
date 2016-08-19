package com.trent.awesomejumper.controller.rendering;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

/**
 * Created by Sinthu on 18.08.2016.
 */
public abstract class Renderer {

    protected SpriteBatch spriteBatch;
    protected OrthographicCamera camera;
    protected ShapeRenderer rectRenderer;



    public Renderer(float viewPortWidth, float viewPortHeight) {
        this.camera = new OrthographicCamera(viewPortWidth, viewPortHeight);
        this.spriteBatch = new SpriteBatch();
        this.rectRenderer = new ShapeRenderer();

    }

    public void render() {

    }

    public void resize(int w, int h) {

    }

}
