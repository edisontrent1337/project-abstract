package com.trent.awesomejumper.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.trent.awesomejumper.game.AwesomeJumperMain;
import com.trent.awesomejumper.utils.Utilites;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeIn;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.delay;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeOut;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.forever;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence;

/**
 * Created by Sinthu on 12.06.2015.
 */
public class SplashScreen implements Screen, InputProcessor {
    // ---------------------------------------------------------------------------------------------
    // INSTANCES & MEMBERS

    private AwesomeJumperMain game;
    private Stage stage;
    private AssetManager assetManager;

    private float screenScalingX, screenScalingY;
    private boolean touchedScreen;

    // IMAGES
    private Image splashImage, awesome, jumper, tap, loading;
    private NinePatch loadingBar;

    //  SOUNDS
    private Sound confirmationSound;

    // ACTIONS
    private Action nextScreen;

    // ---------------------------------------------------------------------------------------------
    // CONSTRUCTOR

    public SplashScreen(AwesomeJumperMain game) {
        this.game = game;
        this.assetManager = game.getAssetManager();
        this.stage = new Stage(new ExtendViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight()));
    }

    // INIT SPLASH SCREEN AND COMPONENTS
    // ---------------------------------------------------------------------------------------------

    @Override
    public void show() {
        //  REGISTER THIS CLASS AS INPUT PROCESSOR
        Gdx.input.setInputProcessor(this);

        // LOAD ALL TEXTURES AND LOADING BAR NINEPATCH
        TextureAtlas atlas = new TextureAtlas(Gdx.files.internal("menu/textures.pack"));
        splashImage = new Image(atlas.findRegion("bg-01"));
        awesome = new Image(atlas.findRegion("awesome-01"));
        jumper = new Image(atlas.findRegion("jumper-01"));
        tap = new Image(atlas.findRegion("tap-01"));
        loadingBar = new NinePatch(atlas.findRegion("loading-01"), 4,4,4,4);
        loading = new Image(loadingBar);

        //INIT IMAGE OPACITY TO 0
        Utilites.resetOpacity(splashImage);
        Utilites.resetOpacity(awesome);
        Utilites.resetOpacity(jumper);
        Utilites.resetOpacity(tap);

        // INIT SOUNDS
        confirmationSound = Gdx.audio.newSound(Gdx.files.internal("sound/test.mp3"));

        // SET NEXT SCREEN
        nextScreen = new Action() {
            @Override
            public boolean act(float delta) {
                loading.remove();
                confirmationSound.dispose();
                game.setScreen(new GameScreen(game));
                return false;
            }
        };


        // INIT ANIMATION SEQUENCES

        splashImage.addAction(new SequenceAction(fadeIn(0.75f), delay(3f)));
        awesome.addAction(forever(new SequenceAction(fadeIn(0.75f), delay(0.3f), fadeOut(0.75f))));
        jumper.addAction(new SequenceAction(fadeIn(0.75f), delay(3f)));
        tap.addAction(forever(new SequenceAction(fadeIn(0.1f), delay(0.05f), fadeOut(0.1f))));

        //LOADING ASSETS FOR MAIN GAME

        assetManager.load("img/textures.pack", TextureAtlas.class);

        //ADD ACTORS TO STAGE

        stage.addActor(splashImage);
        stage.addActor(awesome);
        stage.addActor(jumper);
        stage.addActor(tap);
        stage.addActor(loading);

    }

    // RENDERING
    // ---------------------------------------------------------------------------------------------

    @Override
    public void render(float delta) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(Gdx.graphics.getDeltaTime());
        stage.draw();

        if(assetManager.update() && touchedScreen) {
            stage.addAction(sequence(fadeOut(1f), nextScreen));
        }

    }

    // RESIZING AND SCALING
    // ---------------------------------------------------------------------------------------------

    @Override
    public void resize(int width, int height) {

        // 800 * 480 UPSCALING

        screenScalingX = (float) Gdx.graphics.getWidth() / Utilites.WIDTH;
        screenScalingY = (float) Gdx.graphics.getHeight() / Utilites.HEIGHT;
        splashImage.setScale(screenScalingX,screenScalingY);
        float titleScale =1f;

        // RESIZE SPLASH SCREEN IF RESOLUTION BIGGER THAN HD

        if(width >= 1280 && height >= 720) {
            titleScale = 2f;
            awesome.setScale(titleScale);
            jumper.setScale(titleScale);
            tap.setScale(titleScale);
        }

        // SET POSITION

        awesome.setPosition((width - jumper.getWidth()*titleScale) / 2 + 50, height / 2 + awesome.getHeight()*titleScale + 10);
        jumper.setPosition((width - jumper.getWidth()*titleScale)/2, (height - jumper.getHeight()*titleScale)/2);
        tap.setPosition((width - tap.getWidth()*titleScale)/2, 100);
        loading.setPosition(50,50);

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

    // DISPOSING ASSETS
    //----------------------------------------------------------------------------------------------
    @Override
    public void dispose() {
        confirmationSound.dispose();
        stage.dispose();

    }


    // ---------------------------------------------------------------------------------------------
    // INPUT PROCESSING

    @Override
    public boolean keyDown(int keycode) {
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        touchedScreen = true;
        confirmationSound.play(1.0f);

        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
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
