package com.trent.awesomejumper.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.trent.awesomejumper.controller.InputHandler;
import com.trent.awesomejumper.controller.RenderingEngine;
import com.trent.awesomejumper.controller.WorldController;
import com.trent.awesomejumper.engine.entity.Entity;
import com.trent.awesomejumper.game.AwesomeJumperMain;
import com.trent.awesomejumper.models.WorldContainer;
import com.trent.awesomejumper.models.testing.Chest;
import com.trent.awesomejumper.models.testing.Projectile;

import java.util.Random;

/**
 * Created by Sinthu on 12.06.2015.
 */
public class GameScreen implements Screen, InputProcessor{

    // MEMBERS & INSTANCES
    //----------------------------------------------------------------------------------------------

    private WorldContainer worldContainer;
    private WorldController controller;
    private InputHandler inputHandler;
    private AwesomeJumperMain game;
    private RenderingEngine renderingEngine;

    private int WIDTH = Gdx.graphics.getWidth();
    private int HEIGHT = Gdx.graphics.getHeight();

    private OrthographicCamera gameCamera;

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
        inputHandler = new InputHandler(worldContainer.getPlayer());
        gameCamera = renderingEngine.getGameCamera();
        Gdx.input.setInputProcessor(this);

    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(1f, 247f/255f, 178f/255f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        // TODO: add domain specific methods: input, graphics, network, ai, sound, physics
        inputHandler.update();
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
            inputHandler.leftPressed();
        }
        if (keycode == Keys.RIGHT) {
            inputHandler.rightPressed();
        }
        if (keycode == Keys.UP) {
            inputHandler.upPressed();
        }

        if(keycode == Keys.DOWN) {
            inputHandler.downPressed();
        }


        if(keycode == Keys.U) {
            Entity e = new Chest(new Vector2(new Random().nextInt(5) + 5, new Random().nextInt(5) + 5));
            renderingEngine.initGraphics(e);
            worldContainer.getEntities().add(e);
        }
        if(keycode == Keys.P) {
            Entity p = new Projectile(new Vector2(5,6),0.7f);
            //Entity q = new Projectile(new Vector2(5,6), 0.7f);
            renderingEngine.initGraphics(p);
            //renderingEngine.initGraphics(q);
            worldContainer.getEntities().add(p);
            //worldContainer.getEntities().add(q);


        }


        /**
         * DEBUG KEYS
         */

        if (keycode == Keys.D) {
            game.toggleDebugMode();
        }
        if(keycode == Keys.E) {
            game.toggleEntities();
        }
        if(keycode == Keys.H) {
            game.toggleHitboxes();
        }
        if(keycode == Keys.I) {
            game.toggleInfo();
        }
        if(keycode == Keys.B) {
            game.toggleBody();
        }


        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        if (keycode == Keys.LEFT) {
            inputHandler.leftReleased();
        }
        if (keycode == Keys.RIGHT) {
            inputHandler.rightReleased();
        }
        if (keycode == Keys.UP) {
            inputHandler.upReleased();
        }
        if(keycode == Keys.DOWN) {
            inputHandler.downReleased();
        }

        return false;
    }

    @Override
    public boolean keyTyped(char character) {


        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {


        //TODO implement a register function for entities to add them to all relevant collections.
        if(button == Input.Buttons.LEFT) {
            Projectile p = inputHandler.fire();
            renderingEngine.initGraphics(p);
            worldContainer.getEntities().add(p);
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

        /**
         * Calculating the current mouse position in world units.
         * Start at the position of the camera, go back half of the viewport width (24) to get
         * to the starting coordinate of the current viewport. Then add the offset of the mouse in
         * world units to get the real position of the cursor.
         *
         */
        float x = gameCamera.position.x - gameCamera.viewportWidth/2f + screenX/RenderingEngine.ppuX;
        float y = gameCamera.position.y - gameCamera.viewportHeight/2f + (Gdx.graphics.getHeight() - screenY)/RenderingEngine.ppuY;

        inputHandler.mouseMoved(x,y);
        return false;
    }

    //TODO: Implement scrolling as a way to change height of shots

    @Override
    public boolean scrolled(int amount) {
        return false;
    }
}
