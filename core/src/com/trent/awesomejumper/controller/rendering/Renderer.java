package com.trent.awesomejumper.controller.rendering;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

/**
 * Created by Sinthu on 18.08.2016.
 */
public abstract class Renderer {

    protected SpriteBatch sb;
    protected OrthographicCamera cam;
    protected ShapeRenderer rectRenderer;



    public Renderer(float viewPortWidth, float viewPortHeight) {
        this.cam = new OrthographicCamera(viewPortWidth, viewPortHeight);
        this.sb = new SpriteBatch();
        this.rectRenderer = new ShapeRenderer();

    }

    public void render() {

    }

    public void resize(int w, int h) {

    }

}
