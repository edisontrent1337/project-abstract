package com.trent.awesomejumper.controller;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.trent.awesomejumper.game.AwesomeJumperMain;
import com.trent.awesomejumper.models.Entity;
import com.trent.awesomejumper.models.Environment;
import com.trent.awesomejumper.models.Player;
import com.trent.awesomejumper.models.SkyBox;
import com.trent.awesomejumper.models.WorldContainer;
import com.trent.awesomejumper.testing.CollisionBox;
import com.trent.awesomejumper.tiles.Tile;

import static com.trent.awesomejumper.utils.Utilities.WIDTH;
import static com.trent.awesomejumper.utils.Utilities.formVec;

/**
 * Created by trent on 12.06.2015.
 * Rendering Engine for AwesomeJumper.
 * Renders player, level, skybox and enemies on screen.
 */
public class RenderingEngine {

    // MEMBERS & INSTANCES
    // ---------------------------------------------------------------------------------------------

    private WorldContainer worldContainer;


    private Player player;
    private AwesomeJumperMain game;
    private SkyBox farSky01, farSky02, nearSky01, nearSky02;
    public OrthographicCamera cam, uiCam;
    static final int CAMERA_WIDTH = 32;
    static final int CAMERA_HEIGHT =18;
    static final float SIZE = 1f;

    /**
     * Pixel per unit scale. The screen shows 16 * 9 units, for pixel perfect accuracy we need
     * to know how many pixels are equal to 1 screen unit.
     */
    static float ppuX, ppuY;


    // TEXTURES: PLAYER
    private TextureRegion playerIdleLeft, playerIdleRight, playerJumpL, playerJumpR, currentPlayerFrame;

    // TEXTURES: TILES
    private TextureRegion brownNormal, greyNormal, brownShadow, iceNormal;

    // TEXTURES: ENVIRONMENT
    private TextureRegion grass01, grass01l, grass01r, grass02, snow01;

    // TEXTURES: SKY
    private TextureRegion background01, farSkyTexture, nearSkyTexture, sunTexture;

    // ANIMATIONS: PLAYER
    private Animation walkLeftAnimation, walkRightAnimation;

    // FONTS
    private BitmapFont consoleFont, uiFont;

    // DEBUG & STRINGS
    private String acc, vel, ste, pos, res, cps;
    private float zoom, dmp, grv;
    private boolean debug = false;
    ShapeRenderer debugRenderer = new ShapeRenderer();

    // FRAME DURATION
    private static final float PLAYER_RUN_FRAME_DURATION = 0.066f;

    // SPRITE BATCHES
    private SpriteBatch sb, uiBatch;


    // CONSTRUCTOR
    // ---------------------------------------------------------------------------------------------

    public RenderingEngine(WorldContainer worldContainer, AwesomeJumperMain game) {
        this.worldContainer = worldContainer;
        this.game = game;
        this.farSky01 = worldContainer.getLevel().getSkyBoxes().get(0);
        this.farSky02 = worldContainer.getLevel().getSkyBoxes().get(1);
        this.nearSky01 = worldContainer.getLevel().getSkyBoxes().get(2);
        this.nearSky02 = worldContainer.getLevel().getSkyBoxes().get(3);
        this.player = worldContainer.getPlayer();

        this.ppuX = Gdx.graphics.getWidth() / CAMERA_WIDTH;
        this.ppuX = Gdx.graphics.getHeight() / CAMERA_HEIGHT;


        // TESTING FOR SAT


        /** CAMERA SETUP: MAIN VIEW
         * ZOOM = 16.66667 , other options: initialize camera with parameters
         * 16,9 and leave zoom at 1.
          */
        cam = new OrthographicCamera(32f, 18f);
        cam.zoom = 0.75f;
        zoom = cam.zoom;
        cam.position.set(player.getPositionX(), player.getPositionY() + 2, 0);
        cam.update();

        // CAMERA SETUP: UI
        uiCam = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        uiCam.position.set(Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 2, 0);
        uiCam.update();

        // SPRITEBATCHES
        sb = new SpriteBatch();
        uiBatch = new SpriteBatch();
        loadTextures();
    }


    // LOAD TEXTURES
    // ---------------------------------------------------------------------------------------------

    public void loadTextures() {
        //FONTS
        consoleFont = new BitmapFont();
        consoleFont.setColor(Color.WHITE);
        consoleFont.setUseIntegerPositions(false);
        consoleFont.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);


        // TEXTURE ATLAS
        // -----------------------------------------------------------------------------------------

        TextureAtlas allTextures = game.getAssetManager().get(("img/textures.pack"), TextureAtlas.class);
        // PLAYER TEXTURES
        // IDLE
        playerIdleLeft = allTextures.findRegion("player-white-01");
        playerIdleRight = new TextureRegion(playerIdleLeft);
        playerIdleRight.flip(true, false);
        // JUMPING
        playerJumpL = allTextures.findRegion("player-white-07");
        playerJumpR = new TextureRegion(playerJumpL);
        playerJumpR.flip(true, false);
        // WALINKG ARRAYS & ANIMATIONS
        TextureRegion[] walkLFrames = new TextureRegion[5];
        TextureRegion[] walkRFrames = new TextureRegion[5];
        for (int i = 0; i < 5; i++) {
            walkLFrames[i] = allTextures.findRegion("player-white-0" + (i + 2));
            walkRFrames[i] = new TextureRegion(walkLFrames[i]);
            walkRFrames[i].flip(true, false);
        }
        walkLeftAnimation = new Animation(PLAYER_RUN_FRAME_DURATION, walkLFrames);
        walkRightAnimation = new Animation(PLAYER_RUN_FRAME_DURATION, walkRFrames);

        // TILE TEXTURES
        brownNormal = allTextures.findRegion("brown-01");
        brownShadow = allTextures.findRegion("brown-02");
        greyNormal = allTextures.findRegion("grey-01");
        iceNormal = allTextures.findRegion("ice-01");
        grass02 = allTextures.findRegion("grass-02");
        grass01 = allTextures.findRegion("grass-01");
        grass01r = allTextures.findRegion("grass-01r");
        grass01l = allTextures.findRegion("grass-01l");
        snow01 = allTextures.findRegion("snow-01");

        // SKY TEXTURES
        background01 = allTextures.findRegion("background-01");
        farSkyTexture = allTextures.findRegion("farclouds-01");
        nearSkyTexture = allTextures.findRegion("nearclouds-01");
        sunTexture = allTextures.findRegion("sun-01");
    }

    // RENDERING
    // ---------------------------------------------------------------------------------------------

    public void render() {

        /**
         * SPRITEBATCH sb IS USED TO DRAW EVERYTHING TO THE SCREEN
         * SPRITEBATCH uiBatch IS USED TO DRAW UI INFORMATION
         */
        moveCamera(player.getPositionX(), player.getPositionY());
        sb.setProjectionMatrix(cam.combined);
        sb.begin();
        if(!game.onDebugMode())
            drawBg();
            drawTiles();
        if(game.onDebugMode()) {
                sb.end();
                drawDebugInfo();
                sb.begin();
            }
        if(!game.onDebugMode())
            drawPlayer();
        sb.end();


        // UI, DEBUG ETC
        uiBatch.setProjectionMatrix(uiCam.combined);
        uiBatch.begin();
        if (game.onDebugMode())
            drawInfo();
        uiBatch.end();
    }

    // CAMERA MOVEMENT
    private void moveCamera(float x, float y) {
        cam.position.set(x, y, 0);
        cam.update();
    }


    // DRAW PLAYER
    // ---------------------------------------------------------------------------------------------

    public void drawPlayer() {
        currentPlayerFrame = player.facingL ? playerIdleLeft : playerIdleRight;
        if (player.getState().equals(Entity.State.JUMPING) || player.getState().equals(Entity.State.FALLING)) {
            currentPlayerFrame = player.facingL ? playerJumpL : playerJumpR;
        }
        if (player.getState().equals(Entity.State.WALKING)) {
            currentPlayerFrame = player.facingL ? walkLeftAnimation.getKeyFrame(player.getEntityTime(), true) : walkRightAnimation.getKeyFrame(player.getEntityTime(), true);
        }
        sb.draw(currentPlayerFrame, player.getPosition().x, player.getPosition().y, player.SIZE, player.SIZE);

    }

    // DRAW TILES
    // ---------------------------------------------------------------------------------------------

    private void drawTiles() {
        for(Tile newTile : worldContainer.getTilesToBeRendered(CAMERA_WIDTH,CAMERA_HEIGHT)) {
            int type = newTile.getType();
            Vector2 position = newTile.getPosition();
            if(!game.onDebugMode()) {
                switch (type) {
                    case 1:
                        sb.draw(brownNormal, position.x, position.y, Tile.SIZE, Tile.SIZE);
                        break;
                    case 2:
                        sb.draw(greyNormal, position.x, position.y, Tile.SIZE, Tile.SIZE);
                        break;
                    case 3:
                        sb.draw(brownShadow, position.x, position.y, Tile.SIZE, Tile.SIZE);
                        break;
                    case 4:
                        sb.draw(iceNormal, position.x, position.y, Tile.SIZE, Tile.SIZE);
                        break;
                    case 5:
                        sb.draw(iceNormal, position.x, position.y, Tile.SIZE, Tile.SIZE);
                        break;

                }

                // ENVIRONMENT, GRASS ETC

                for(Environment environment : worldContainer.getLevel().getEnvironment()) {
                    if(environment.getType().equals(Environment.EnvironmentType.GRASS)) {
                        sb.draw(grass01, environment.getPosition().x, environment.getPosition().y, Environment.SIZE * 2, Environment.SIZE * 2);
                    }
                }
            }



            if (game.onDebugMode()) {
                sb.end();
                debugRenderer.setProjectionMatrix(cam.combined);
                //moveCamera(player.getPositionX(),player.getPositionY());
                debugRenderer.begin(ShapeRenderer.ShapeType.Line);
                if(!newTile.isPassable())
                newTile.getCollisionBox().draw(debugRenderer);
               // debugRenderer.setColor(Color.GREEN);
               // debugRenderer.rect(position.x, position.y, Tile.SIZE, Tile.SIZE);
                debugRenderer.end();
                sb.begin();
            }



        }

    }



    // DRAW BACKGROUND
    // ---------------------------------------------------------------------------------------------

    public void drawBg() {
        float skyBoxPos01X = farSky01.getPositionX();
        float skyBoxPos02X = farSky02.getPositionX();

        // ----- FAR AWAY CLOUDS
        // VECTOR: X = X POSITION, Y = WIDTH, Z = HEIGHT;
        Vector3 fClouds01 = new Vector3(farSky01.getPosition().x, farSky01.getBounds().width, farSky01.getBounds().height);
        Vector3 fClouds02 = new Vector3(farSky01.getPosition().x, farSky02.getBounds().width, farSky02.getBounds().height);
        // ----- NEAR CLOUDS
        Vector3 nClouds01 = new Vector3(nearSky01.getPosition().x, nearSky01.getBounds().width, nearSky01.getBounds().height);
        Vector3 nClouds02 = new Vector3(nearSky02.getPosition().x, nearSky02.getBounds().width, nearSky02.getBounds().height);
        // ----- BACKGROUND SKY
        sb.draw(background01, skyBoxPos01X, 0, fClouds01.y, fClouds01.z);
        sb.draw(background01, skyBoxPos02X, 0, fClouds01.y, fClouds01.z);
        sb.draw(sunTexture, fClouds02.x, 0, fClouds01.y, fClouds01.z);
        // ----- FAR AWAY CLOUDS
        sb.draw(farSkyTexture, fClouds01.x, 0, fClouds01.y, fClouds01.z);
        sb.draw(farSkyTexture, fClouds02.x, 0, fClouds02.y, fClouds02.z);
        // ----- NEAR CLOUDS
        sb.draw(nearSkyTexture, nClouds01.x, 0, nClouds01.y, nClouds01.z);
        sb.draw(nearSkyTexture, nClouds02.x, 0, nClouds02.y, nClouds02.z);

        if (fClouds01.x + fClouds01.y <= cam.position.x - zoom / 2) {
            farSky01.setPositionX(fClouds02.x + fClouds02.y);
        }

        if (fClouds02.x + fClouds02.y <= cam.position.x - zoom / 2) {
            farSky02.setPositionX(fClouds01.x + fClouds01.y);
        }

        if (fClouds01.x > cam.position.x - zoom / 2) {
            farSky02.setPositionX(fClouds01.x - fClouds01.y);
        }

        if (fClouds02.x > cam.position.x - zoom / 2) {
            farSky01.setPositionX(fClouds02.x - fClouds02.y);
        }

        if (nClouds01.x + nClouds01.y <= cam.position.x - zoom / 2) {
            nearSky01.setPositionX(nClouds02.x + nClouds02.y);
        }

        if (nClouds02.x + nClouds02.y <= cam.position.x - zoom / 2) {
            nearSky02.setPositionX(nClouds01.x + nClouds01.y);
        }

        if (nClouds01.x > cam.position.x - zoom / 2) {
            nearSky02.setPositionX(nClouds01.x - nClouds01.y);
        }

        if (nClouds02.x > cam.position.x - zoom / 2) {
            nearSky01.setPositionX(nClouds02.x - nClouds02.y);
        }

    }


    // DEBUG INFO
    // ---------------------------------------------------------------------------------------------

    public void drawInfo() {
        acc = "ACC: " +  player.getAcceleration();
        vel = "VEL: " +  player.getVelocity();
        ste = "STATE: " + player.getState().toString();
        pos = "POS: " + player.getPosition();
        cps = "CAM: " + formVec(cam.position.x, cam.position.y);
        res = Gdx.graphics.getWidth() + "*" + Gdx.graphics.getHeight() + ", ZOOM: " + zoom + ", FPS :" + Gdx.graphics.getFramesPerSecond();
        consoleFont.draw(uiBatch, acc, 15,15);
        consoleFont.draw(uiBatch, vel, 15,30);
        consoleFont.setColor(Color.GREEN);
        consoleFont.draw(uiBatch, ste, 15,45);
        consoleFont.draw(uiBatch, pos, 15,60);
        consoleFont.draw(uiBatch, res, 15,75);
        consoleFont.draw(uiBatch, cps, 15,90);
        consoleFont.draw(uiBatch, "COLLISION BOX" + player.getCollisionBox().getPosition().toString(), 15,105);
        consoleFont.draw(uiBatch, "a:" + player.getCollisionBox().getVertices().get(0).toString(), 15,120);
        consoleFont.draw(uiBatch, "b:" + player.getCollisionBox().getVertices().get(1).toString(), 15,135);
        consoleFont.draw(uiBatch, "c:" + player.getCollisionBox().getVertices().get(2).toString(), 15,150);
        consoleFont.draw(uiBatch, "d:" + player.getCollisionBox().getVertices().get(3).toString(), 15,165);
    }


    public void drawDebugInfo() {
        debugRenderer.setProjectionMatrix(cam.combined);
        debugRenderer.begin(ShapeRenderer.ShapeType.Line);


        debugRenderer.setColor(0, 1, 0, 1);
        // PLAYER HITBOXES

        debugRenderer.rect(player.getPositionX() + player.getVelocity().cpy().scl(player.getPlayerDelta()).x,
                           player.getPositionY() + player.getVelocity().cpy().scl(player.getPlayerDelta()).y,
                           player.getBody().get(0).getWidth(),
                           player.getBody().get(0).getHeight());




        for(CollisionBox r: player.getBody()) {
            r.draw(debugRenderer);
        }
       //debugRenderer.rect(player.getBounds().x, player.getBounds().y, player.getBounds().width, player.getBounds().height);

        debugRenderer.end();
        // HITBOXES OF TILES AFFECTED BY COLLISION DETECTION
        Gdx.gl.glEnable(GL20.GL_BLEND);
        debugRenderer.begin(ShapeRenderer.ShapeType.Filled);
        debugRenderer.setColor(0, 0.5f, 0.5f, 1);
        /**
         * vertical and horizontal components are separated here to show  which impact is bigger
         */
        Vector2 playerX = new Vector2(player.getVelocity().x,0f).cpy().scl(player.getPlayerDelta()).scl(250);
        Vector2 playerY = new Vector2(0f,player.getVelocity().y).cpy().scl(player.getPlayerDelta()).scl(250);

        debugRenderer.rectLine(player.getPosition(), player.getPosition().cpy().add(playerX), 5 * (1 / ppuX));
        debugRenderer.rectLine(player.getPosition(), player.getPosition().cpy().add(playerY), 5 * (1 / ppuY));

        debugRenderer.setColor(1f, 0f, 0f, 0.5f);
        for (Tile t : worldContainer.getCollisionTiles()) {
            if(t != null)
                debugRenderer.rect(t.getPosition().x, t.getPosition().y, Tile.SIZE, Tile.SIZE);
        }
        debugRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }


}
