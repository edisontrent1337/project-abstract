package com.trent.awesomejumper.controller;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.trent.awesomejumper.engine.modelcomponents.Graphics;
import com.trent.awesomejumper.game.AwesomeJumperMain;
import com.trent.awesomejumper.engine.entity.Entity;
import com.trent.awesomejumper.models.Player;
import com.trent.awesomejumper.models.SkyBox;
import com.trent.awesomejumper.engine.physics.CollisionBox;
import com.trent.awesomejumper.tiles.Tile;

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
    private HUDRenderer hudRenderer;
    private PopUpManager popUpManager;
    private AssetManager assetManager;

    private Player player;
    private AwesomeJumperMain game;
    private SkyBox farSky01, farSky02, nearSky01, nearSky02;
    public OrthographicCamera cam, uiCam;
    static final float CAMERA_WIDTH = 32;
    static final float CAMERA_HEIGHT =18f;
    private final float LERP_FACTOR = 0.075f;

    /**
     * Pixel per unit scale. The screen shows 16 * 9 units, for pixel perfect accuracy we need
     * to know how many pixels are equal to 1 screen unit.
     */
    public static float ppuX, ppuY;

    // TEXTURE ATLASES
    static TextureAtlas allTextures;

    // TEXTURES: TILES
    private TextureRegion brownNormal, greyNormal, brownShadow, iceNormal;

    // TEXTURES: ENVIRONMENT
    private TextureRegion grass01, grass01l, grass01r, grass02, snow01;

    // TEXTURES: SKY
    private TextureRegion background01, farSkyTexture, nearSkyTexture, sunTexture;


    // FONTS
    private BitmapFont consoleFont, uiFont, messageFont;

    // DEBUG & STRINGS

    static final int DE_ACCELERATION = 0;
    static final int DE_VELOCITY = 1;
    static final int DE_STATE = 2;
    static final int DE_POSITION = 3;
    static final int DE_POSITION_OFFSET = 4;
    static final int DE_CAMERA_POSITION = 5;
    static final int DE_ENTITIES = 6;
    static final int DE_RESOLUTION = 7;
    static final int DE_CURSOR = 8;
    static final int DE_REGION = 9;


    public static Array<String> debugStrings;
    private float zoom;
    private final int CONSOLE_LINE_HEIGHT = 32;
    ShapeRenderer debugRenderer = new ShapeRenderer();

    // SPRITE BATCHES
    private SpriteBatch sb, debugBatch;

    // CONSTRUCTOR
    // ---------------------------------------------------------------------------------------------

    public RenderingEngine(WorldContainer worldContainer, AwesomeJumperMain game) {
        this.worldContainer = worldContainer;
        this.game = game;
        /*this.farSky01 = worldContainer.getLevel().getSkyBoxes().get(0);
        this.farSky02 = worldContainer.getLevel().getSkyBoxes().get(1);
        this.nearSky01 = worldContainer.getLevel().getSkyBoxes().get(2);
        this.nearSky02 = worldContainer.getLevel().getSkyBoxes().get(3);*/
        this.player = worldContainer.getPlayer();
        this.hudRenderer = new HUDRenderer(player);
        this.popUpManager = PopUpManager.createPopUpManager();
        this.allTextures = new TextureAtlas();
        this.debugStrings = new Array<>(10);
        for(int i = 0; i < 10; i++) {
            debugStrings.add(new String(""));
        }


        /** CAMERA SETUP: MAIN VIEW
         * ZOOM = 16.66667 , other options: initialize camera with parameters
         * 16,9 and leave zoom at 1.
          */
        cam = new OrthographicCamera(CAMERA_WIDTH, CAMERA_HEIGHT);
        cam.zoom = 1f;
        zoom = cam.zoom;
        this.ppuX = Gdx.graphics.getWidth() / (CAMERA_WIDTH * zoom);
        this.ppuY = Gdx.graphics.getHeight() / (CAMERA_HEIGHT * zoom);
        cam.position.set(player.getPosition().x, player.getPosition().y + 2, 0);
        cam.update();

        // CAMERA SETUP: UI
        uiCam = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        uiCam.position.set(Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 2, 0);
        uiCam.update();

        // SPRITEBATCHES
        sb = new SpriteBatch();
        debugBatch = new SpriteBatch();
        loadTextures();


    }


    // LOAD TEXTURES
    // ---------------------------------------------------------------------------------------------

    /**
     * Loads all textures from the asset manager and generates corresponding animations for each entity.
     * Also loads world tile textures.
     */
    public void loadTextures() {

        // HUD
        hudRenderer.loadTextures();
        //FONTS
        consoleFont = new BitmapFont(Gdx.files.internal("fonts/munro_regular_14.fnt"),Gdx.files.internal("fonts/munro_regular_14_0.png"),false);
        consoleFont.setColor(Color.WHITE);
        consoleFont.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        messageFont = new BitmapFont(Gdx.files.internal("fonts/munro_outlined.fnt"),Gdx.files.internal("fonts/munro_outlined_0.png"),false);
        messageFont.getRegion().getTexture().setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        messageFont.getData().setScale(1f/ppuX, 1f/ppuY);



        // TEXTURE ATLAS
        // -----------------------------------------------------------------------------------------

        allTextures = game.getAssetManager().get(("img/textures.pack"), TextureAtlas.class);
        /**
         * Iterate over all entities and manipulate their graphics component.
         */

        for(Entity e : worldContainer.getEntities()) {
            initGraphics(e);
        }


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

    /**
     * Initializes the graphics component of the entity e.
     * Loads animations, idle frames and other sprites and bundles them into a graphic
     * component.
     * @param e
     */
    public void initGraphics(Entity e) {

        if(e.hasGraphics) {
            Graphics g = e.getGraphics();
            Array<TextureAtlas.AtlasRegion> regions = allTextures.findRegions(g.getTextureRegName());
            g.setIdleFrames(regions.first());
            g.setShadow(allTextures.findRegion(g.getTextureRegName() + "_shadow"));

            for(int i = 1; i < regions.size; i++) {
                g.addKeyFrame(regions.get(i));
            }
            g.createWalkAnimations();
        }

    }

    // RENDERING
    // ---------------------------------------------------------------------------------------------
    public void render() {

        /**
         * SPRITEBATCH sb IS USED TO DRAW EVERYTHING TO THE SCREEN
         * SPRITEBATCH debugBatch IS USED TO DRAW UI INFORMATION
         */


        uiCam.update();
        moveCamera(player.getPosition().x, player.getPosition().y);
        sb.setProjectionMatrix(cam.combined);
        sb.begin();
        //drawBg();
        drawTiles();
        if(game.hitboxesEnabled()) {
                sb.end();
                drawHitboxes();
                sb.begin();
            }

        if(game.entitiesEnabled()) {
            drawEntities();
            drawPopUps();
        }
        sb.end();
        debugBatch.setProjectionMatrix(uiCam.combined);
        debugBatch.begin();
        if(game.infoEnabled()) {
            drawInfo();
        }
        debugBatch.end();
        hudRenderer.render();
    }

    // CAMERA MOVEMENT

    /**
     * Moves the camera smoothly behind the player and stops it at the edges of the level
     * @param playerX player's x position
     * @param playerY player's y position
     */
    private void moveCamera(float playerX, float playerY) {

        float tempX = cam.position.x;
        float tempY = cam.position.y;

        float xLerp = (playerX - tempX) * LERP_FACTOR;
        tempX += xLerp;

        float yLerp =  (playerY - tempY) * LERP_FACTOR;
        tempY += yLerp;


        float updatedX, updatedY;
        if(playerX - cam.position.x >= 0f) {
            updatedX = ((int) (Math.floor(tempX * ppuX))) / ppuX;
        }
        else {
            updatedX = ((int) (Math.ceil(tempX * ppuX))) / ppuX;

        }

        if(playerY - cam.position.y >= 0f) {
            updatedY = ((int) (Math.floor(tempY * ppuY))) / ppuY;
        }
        else {
            updatedY = ((int) (Math.ceil(tempY * ppuY))) / ppuY;

        }

        if(updatedX < 4)
            updatedX = 4;
        if(updatedY < 4)
            updatedY = 4;

        cam.position.set(updatedX, updatedY, 0);
        cam.update();
    }


    // DRAW ENTITIES
    // ---------------------------------------------------------------------------------------------

    /**
     * Iterates over all entities in the world and renders them on the screen.
     */
    public void drawEntities() {
        Vector2 cameraPosition = new Vector2(cam.position.x, cam.position.y);
        for(Entity e : worldContainer.getEntitiesToBeRendered(cameraPosition, CAMERA_WIDTH, CAMERA_HEIGHT)) {
            e.render(sb);
        }

    }

    /**
     * Draws popups from game events (Picking up items/weapons, dealing damage, healing)
     */
    public void drawPopUps() {
        popUpManager.render(sb, messageFont);
    }

    // DRAW TILES
    // ---------------------------------------------------------------------------------------------

    private void drawTiles() {
        Vector2 cameraPosition = new Vector2(cam.position.x, cam.position.y);
        for(Tile newTile : worldContainer.getTilesToBeRendered(cameraPosition, CAMERA_WIDTH*cam.zoom, CAMERA_HEIGHT*cam.zoom)) {
            Tile.TileType type = newTile.getType();
            Vector2 position = newTile.getPosition();
            if(!game.onDebugMode()) {
                switch (type) {
                    case WALL:
                        sb.draw(brownNormal, position.x, position.y, Tile.SIZE, Tile.SIZE);
                        break;
                    case STONE:
                        sb.draw(greyNormal, position.x, position.y, Tile.SIZE, Tile.SIZE);
                        break;
                    case FLOOR:
                        sb.draw(brownShadow, position.x, position.y, Tile.SIZE, Tile.SIZE);
                        break;
                    case ICE:
                        sb.draw(iceNormal, position.x, position.y, Tile.SIZE, Tile.SIZE);
                        break;
                    case TRAMPOLINE:
                        sb.draw(iceNormal, position.x, position.y, Tile.SIZE, Tile.SIZE);
                        break;

                }

            }

            if (game.onDebugMode()) {
                sb.end();
                debugRenderer.setProjectionMatrix(cam.combined);
                debugRenderer.begin(ShapeRenderer.ShapeType.Line);
                if(!newTile.isPassable())
                newTile.getCollisionBox().draw(debugRenderer);
                debugRenderer.end();
                sb.begin();
            }
        }

    }

    // DRAW BACKGROUND
    // ---------------------------------------------------------------------------------------------

    public void drawBg() {
        float skyBoxPos01X = farSky01.getPosition().x;
        float skyBoxPos02X = farSky02.getPosition().y;

        // ----- FAR AWAY CLOUDS
        // VECTOR: X = X POSITION, Y = WIDTH, Z = HEIGHT;
        Vector3 fClouds01 = new Vector3(farSky01.getPosition().x, farSky01.getBounds().getWidth(), farSky01.getBounds().getHeight());
        Vector3 fClouds02 = new Vector3(farSky01.getPosition().x, farSky02.getBounds().getHeight(), farSky02.getBounds().getHeight());
        // ----- NEAR CLOUDS
        Vector3 nClouds01 = new Vector3(nearSky01.getPosition().x, nearSky01.getBounds().getWidth(), nearSky01.getBounds().getHeight());
        Vector3 nClouds02 = new Vector3(nearSky02.getPosition().x, nearSky02.getBounds().getWidth(), nearSky02.getBounds().getHeight());
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
        debugStrings.set(DE_ACCELERATION,"ACC: " +  player.getAcceleration() );
        debugStrings.set(DE_VELOCITY, "VEL: " +  player.getVelocity());
        debugStrings.set(DE_STATE, "STATE: " + player.getState().toString());
        debugStrings.set(DE_POSITION, "POS: " + player.getPosition());
        debugStrings.set(DE_POSITION_OFFSET, "PAO: " + player.getBody().getBounds().getPositionAndOffset());
        debugStrings.set(DE_CAMERA_POSITION, "CAM: " + formVec(cam.position.x, cam.position.y));
        debugStrings.set(DE_ENTITIES,"Entities,Drawn :" + Integer.toString(Entity.entityCount) + " , " + Integer.toString(WorldContainer.nodes));
        debugStrings.set(DE_RESOLUTION, Gdx.graphics.getWidth() + "*" + Gdx.graphics.getHeight() + ", ZOOM: " + zoom + ", FPS :" + Gdx.graphics.getFramesPerSecond());
        debugStrings.set(DE_CURSOR, Double.toString(Math.floor(InputHandler.mouse.x)) + " | " + Double.toString(Math.floor(InputHandler.mouse.y)));
        debugStrings.set(DE_REGION, Integer.toString(worldContainer.getRandomLevelGenerator().getRegion(InputHandler.mouse)));
        consoleFont.setColor(Color.BLUE);
        for(int i = 0; i < debugStrings.size; i++) {
            consoleFont.draw(debugBatch, debugStrings.get(i), 14, CONSOLE_LINE_HEIGHT *i);
        }
    }


    public void drawHitboxes() {
        debugRenderer.setProjectionMatrix(cam.combined);
        debugRenderer.begin(ShapeRenderer.ShapeType.Line);


        for(Entity e : worldContainer.getEntities()) {
            /**
             * Only if the body flag is enabled, body hitboxes will be drawn.
             */
            debugRenderer.setColor(1, 0, 0, 1);
            e.getBounds().draw(debugRenderer);
            if(game.bodyEnabled()) {
                for (CollisionBox r : e.getBodyHitboxes()) {
                    debugRenderer.setColor(Color.YELLOW);
                    r.draw(debugRenderer);
                }
            }

            if(e.getBody().isCollidedWithWorld()) {
                debugRenderer.end();
                debugRenderer.begin(ShapeRenderer.ShapeType.Filled);
                debugRenderer.setColor(Color.RED);
                debugRenderer.rect(e.getBounds().getPositionAndOffset().x, e.getBounds().getPositionAndOffset().y, e.getBounds().getWidth(), e.getBounds().getHeight());
                debugRenderer.end();
                debugRenderer.begin(ShapeRenderer.ShapeType.Line);
            }
        }
        debugRenderer.end();
        // HITBOXES OF TILES AFFECTED BY COLLISION DETECTION
        Gdx.gl.glEnable(GL20.GL_BLEND);
        debugRenderer.begin(ShapeRenderer.ShapeType.Filled);
        debugRenderer.setColor(0, 0.5f, 0.5f, 1);
        /**
         * vertical and horizontal components are separated here to show  which impact is bigger
         */
        Vector2 playerX = new Vector2(player.getVelocity().x,0f).cpy().scl(player.getPlayerDelta()).scl(25);
        Vector2 playerY = new Vector2(0f,player.getVelocity().y).cpy().scl(player.getPlayerDelta()).scl(25);

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


    public void resize(int w, int h) {
        uiCam = new OrthographicCamera(w,h);
        uiCam.position.set(w/2,h/2,0);
        ppuX = Gdx.graphics.getWidth() / (CAMERA_WIDTH*zoom);
        ppuY = Gdx.graphics.getHeight() / (CAMERA_HEIGHT*zoom);
        messageFont.getData().setScale(1/ppuX, 1/ppuY);
        hudRenderer.resize(w,h);
    }



    // GETTER AND SETTER
    // ---------------------------------------------------------------------------------------------

    public OrthographicCamera getGameCamera() {
        return cam;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public void setZoom(float zoom) {
            float oldZoom = cam.zoom;
            oldZoom += 0.1d * zoom;
            cam.zoom = oldZoom;
            ppuX = Gdx.graphics.getWidth() / (CAMERA_WIDTH*cam.zoom);
            ppuY = Gdx.graphics.getHeight() / (CAMERA_HEIGHT*cam.zoom);
    }

}
