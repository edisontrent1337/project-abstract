package com.trent.awesomejumper.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.trent.awesomejumper.controller.entitymanagement.EntityManager;
import com.trent.awesomejumper.controller.entitymanagement.WorldContainer;
import com.trent.awesomejumper.controller.assets.AssetLoader;
import com.trent.awesomejumper.controller.collision.CollisionController;
import com.trent.awesomejumper.controller.input.InputHandler;
import com.trent.awesomejumper.controller.rendering.RenderingEngine;
import com.trent.awesomejumper.controller.WorldController;
import com.trent.awesomejumper.engine.entity.Entity;
import com.trent.awesomejumper.game.AwesomeJumperMain;
import com.trent.awesomejumper.models.lootable.Lootable;

/**
 * Created by Sinthu on 12.06.2015.
 */
public class GameScreen implements Screen {

    // MEMBERS & INSTANCES
    //----------------------------------------------------------------------------------------------

    private WorldContainer worldContainer;
    private WorldController controller;
    private InputHandler inputHandler;
    private AwesomeJumperMain game;
    private RenderingEngine renderingEngine;
    private EntityManager entityManager;
    private AssetLoader assetLoader;

    // CONSTRUCTOR
    // ---------------------------------------------------------------------------------------------
    public GameScreen(AwesomeJumperMain game) {
        this.game = game;
        entityManager = EntityManager.createEntityManager();
    }


    @Override
    public void show() {
        // TODO: load assets with asset loader here.
        // TODO: move input processor to input handler.
        worldContainer = new WorldContainer();
        renderingEngine = new RenderingEngine(worldContainer, game);
        entityManager.setControllers(worldContainer, renderingEngine);

        /**
         * Init all entity collections and sort all pre existing entities in their respective collections.
         */
        worldContainer.initAllEntities();
        controller = new WorldController(worldContainer);
        inputHandler = new InputHandler(worldContainer, renderingEngine);
        Gdx.input.setInputProcessor(inputHandler);
    }

    @Override
    public void render(float delta) {
        delta = Math.min(delta, 1 / 60f);
        //Gdx.gl.glClearColor(1f, 247f / 255f, 178f / 255f, 1);
        Gdx.gl.glClearColor(0.3f, 0.3f, 0.3f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        // TODO: add domain specific methods: input, graphics, network, ai, sound, physics
        inputHandler.update();
        controller.update(delta);
        renderingEngine.render();
        CollisionController.calledPerFrame = 0;
    }

    @Override
    public void resize(int width, int height) {
        renderingEngine.resize(width, height);
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {

    }




}
