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
public class GameScreen implements Screen, InputProcessor {

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
        //Gdx.input.setInputProcessor(this);
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


    @Override
    public boolean keyDown(int keycode) {

        if (keycode == Keys.L)
            game.toggleLogging();

        if (keycode == Keys.A) {
            inputHandler.leftPressed();
        }
        if (keycode == Keys.D) {
            inputHandler.rightPressed();
        }
        if (keycode == Keys.W) {
            inputHandler.upPressed();
        }

        if (keycode == Keys.S) {
            inputHandler.downPressed();
        }

        if (keycode == Keys.X) {
            //TODO: EDIT THIS!!!!
            entityManager.reset();
            inputHandler.setPlayer(worldContainer.getPlayer());
            controller.setPlayer();
        }

        if (keycode == Keys.U) {
            Entity e = new Lootable(worldContainer.getPlayer().getPosition().cpy().add(1, 2));
            e.register();
        }

        /**
         * DEBUG KEYS
         */

        if (keycode == Keys.T) {
            game.toggleDebugMode();
        }
        if (keycode == Keys.P) {
            game.toggleEntityDrawing();
        }
        if (keycode == Keys.H) {
            game.toggleHitboxDrawing();
        }
        if (keycode == Keys.I) {
            game.toggleInfoDrawing();
        }
        if (keycode == Keys.B) {
            game.toggleBody();
        }

        if (keycode == Keys.Q) {
            inputHandler.dropPressed();
        }

        if (keycode == Keys.E)
            inputHandler.pickUpPressed();

        if (keycode == Keys.R) {
            inputHandler.reload();
        }

        if (keycode == Keys.B) {
            worldContainer.getRandomLevelGenerator().connectRegions();
        }
        if (keycode == Keys.N) {
            worldContainer.getRandomLevelGenerator().removeExtraDoors();
        }
        if (keycode == Keys.M) {
            worldContainer.getRandomLevelGenerator().removeDeadEnds();
        }

        if (keycode == Keys.F11) {
            Graphics.DisplayMode mode = Gdx.graphics.getDisplayMode();
            Gdx.graphics.setFullscreenMode(mode);
        }


        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        if (keycode == Keys.A) {
            inputHandler.leftReleased();
        }
        if (keycode == Keys.D) {
            inputHandler.rightReleased();
        }
        if (keycode == Keys.W) {
            inputHandler.upReleased();
        }
        if (keycode == Keys.S) {
            inputHandler.downReleased();
        }

        if (keycode == Keys.Q) {
            inputHandler.dropReleased();
            inputHandler.pickUpReleased();
        }
        if (keycode == Keys.E)
            inputHandler.pickUpReleased();

        return false;
    }

    @Override
    public boolean keyTyped(char character) {


        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {


        if (button == Input.Buttons.LEFT) {
           /* Gdx.app.log("PLAYER:","");
            Gdx.app.log("MOUSE:", inputHandler.mouse.toString());
            Gdx.app.log("POSICENTER:", worldContainer.getPlayer().getBody().getCenter().toString());
            Gdx.app.log("ORIENTATION:", worldContainer.getPlayer().getBody().getOrientation().toString());
            Gdx.app.log("ANGLE;", Float.toString(worldContainer.getPlayer().getBody().getAngleOfRotation()));
            Gdx.app.log("SPEED", worldContainer.getPlayer().getBody().getOrientation().cpy().nor().scl(60).toString());
            Gdx.app.log("-----------------------------------------------------","");*/
            inputHandler.fire();

        }

        return true;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return true;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {

        return false;
    }


    @Override
    public boolean scrolled(int amount) {
        renderingEngine.setZoom(amount);
        //inputHandler.changeWeapon(amount);
        return false;
    }


}
