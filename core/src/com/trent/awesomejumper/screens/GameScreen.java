package com.trent.awesomejumper.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.trent.awesomejumper.controller.RenderingEngine;
import com.trent.awesomejumper.controller.WorldController;
import com.trent.awesomejumper.game.AwesomeJumperMain;
import com.trent.awesomejumper.models.WorldContainer;

/**
 * Created by Sinthu on 12.06.2015.
 */
public class GameScreen implements Screen, InputProcessor{

    // MEMBERS & INSTANCES
    //----------------------------------------------------------------------------------------------

    private WorldContainer worldContainer;
    private WorldController controller;
    private AwesomeJumperMain game;
    private RenderingEngine renderingEngine;

    private int WIDTH = Gdx.graphics.getWidth();
    private int HEIGHT = Gdx.graphics.getHeight();

    // TODO: Add rectangles or even a segmented circle to catch touch events and control the player.
    // TODO: The coordinate system for touch events is flipped on the y axis!

    // CONSTRUCTOR
    // ---------------------------------------------------------------------------------------------
    public GameScreen (AwesomeJumperMain game) {
        this.game = game;
    }


    @Override
    public void show() {
        worldContainer = new WorldContainer();
        controller = new WorldController(worldContainer);
        renderingEngine = new RenderingEngine(worldContainer, game);
        Gdx.input.setInputProcessor(this);

    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(1f, 247f/255f, 178f/255f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        // TODO: add domain specific methods: input, graphics, network, ai, sound, physics
        controller.update(delta);
        renderingEngine.render();
    }

    @Override
    public void resize(int width, int height) {
        this.WIDTH = width;
        this.HEIGHT = height;
        renderingEngine.resize(width,height);
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
        if (keycode == Keys.LEFT) {
            controller.leftPressed();
        }
        if (keycode == Keys.RIGHT) {
            controller.rightPressed();
        }
        if (keycode == Keys.UP) {
            controller.upPressed();
        }

        if(keycode == Keys.DOWN) {
            controller.downPressed();
        }

        if (keycode == Keys.D) {
            game.setDebugMode(!game.onDebugMode());
        }


        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        if (keycode == Keys.LEFT) {
            controller.leftReleased();
        }
        if (keycode == Keys.RIGHT) {
            controller.rightReleased();
        }
        if (keycode == Keys.UP) {
            controller.upReleased();
        }
        if(keycode == Keys.DOWN) {
            controller.downReleased();
        }

        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        if(character == 'D') {
            game.setDebugMode(!game.onDebugMode());
        }

        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if(screenX <= 1/8 * WIDTH && screenY > HEIGHT / 5) {
            controller.leftPressed();
        }
        else if(screenX > 1/8 * WIDTH && screenX <= 2/8 * WIDTH && screenY > HEIGHT / 5) {
            controller.rightPressed();
        }
        else if(screenX > 7/8 * WIDTH && screenY > HEIGHT / 5) {
            controller.upPressed();
        }

        Gdx.app.log("TOUCH EVENT: PRESSED", "COORDINATES:[" + screenX + "|" + screenY + "]" );
        return true;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if(screenX > 2/8*WIDTH && screenX <=7/8*WIDTH && screenY > HEIGHT / 5) {
            controller.leftReleased();
            controller.rightReleased();
        }
        controller.upReleased();
        Gdx.app.log("TOUCH EVENT: RELEASED", "COORDINATES:[" + screenX + "|" + screenY + "]" );
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
        return false;
    }
}
