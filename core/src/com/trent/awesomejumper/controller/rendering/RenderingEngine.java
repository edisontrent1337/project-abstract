package com.trent.awesomejumper.controller.rendering;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.StringBuilder;
import com.trent.awesomejumper.controller.WorldContainer;
import com.trent.awesomejumper.controller.collision.CollisionController;
import com.trent.awesomejumper.controller.input.InputHandler;
import com.trent.awesomejumper.engine.entity.Entity;
import com.trent.awesomejumper.engine.modelcomponents.Graphics;
import com.trent.awesomejumper.engine.physics.CollisionBox;
import com.trent.awesomejumper.game.AwesomeJumperMain;
import com.trent.awesomejumper.models.Player;
import com.trent.awesomejumper.models.SkyBox;
import com.trent.awesomejumper.tiles.Tile;
import com.trent.awesomejumper.utils.Utilities;

import java.util.EnumMap;

import static com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import static com.trent.awesomejumper.controller.rendering.RenderingEngine.DEBUG_TAGS.*;
import static com.trent.awesomejumper.engine.modelcomponents.ModelComponent.ComponentID.GRAPHICS;
import static com.trent.awesomejumper.utils.Utilities.printVec;


/**
 * Created by trent on 12.06.2015.
 * Rendering Engine for AwesomeJumper.
 * Renders player, all other entities, pop ups, hud and debug info on screen.
 * Uses 2 sprite batches and cameras to render main view and debug info.
 */
public class RenderingEngine extends Renderer {

    // MEMBERS & INSTANCES
    // ---------------------------------------------------------------------------------------------

    private WorldContainer worldContainer;
    private HudRenderer hudRenderer;
    private PopUpRenderer popUpRenderer;
    private AssetManager assetManager;

    private Player player;
    private AwesomeJumperMain game;
    private SkyBox farSky01, farSky02, nearSky01, nearSky02;
    private OrthographicCamera debugCam;
    static final float CAMERA_WIDTH = 32;
    static final float CAMERA_HEIGHT = 18f;
    private final float LERP_FACTOR = 0.075f;
    private final float ZOOM = 0.6f;

    protected static Vector2 camPositionInPx;
    private Vector3 unprojectedMousePosition;


    // SPRITE BATCHE
    //private SpriteBatch spriteBatch, debugBatch;

    private SpriteBatch debugBatch;

    /**
     * Pixel per unit scale. The screen shows 16 * 9 units, for pixel perfect accuracy we need
     * to know how many pixels are equal to 1 screen unit.
     */
    public static float ppuX, ppuY;

    // TEXTURE ATLASES
    static TextureAtlas allTextures;

    // TEXTURES: TILES
    private Array<TextureAtlas.AtlasRegion> wallTextures;
    private Array<TextureAtlas.AtlasRegion> floorTextures;


    // TEXTURES: SKY
    private TextureRegion background01, farSkyTexture, nearSkyTexture, sunTexture;


    // FONTS
    private BitmapFont debugFont, hudFont, popUpFont;
    private FreeTypeFontGenerator fontGenerator;

    // DEBUG & STRINGS
    enum DEBUG_TAGS {
        DE_ACCELERATION("ACC"),
        DE_VELOCITY("VEL"),
        DE_STATE("STATE"),
        DE_POSITION("POS"),
        DE_POS_OFFSET("PAO"),
        DE_CAMERA_POSITION("CAM POSITION"),
        DE_ENTITY_COUNT("ENTITIY COUNT"),
        DE_RESOLUTION("RESOLUTION"),
        DE_CURSOR_POSITION("CURSOUR POS"),
        DE_ROOM_REGION("ROOM REGION"),
        DE_ENTITY_INSTANCE_COUNT("ENTITY INSTANCES"),
        DE_PROJECTILE_COUNT("PROJECTILES"),
        DE_WEAPON_DROP_COUNT("WEAPON DROPS"),
        DE_MOBILE_ENTITY_COUNT("MOBILE ENTITIES"),
        DE_LIVING_ENTITY_COUNT("LIVING ENTITIES"),
        DE_COLLISION_CHECKS("CD CHECKS PER FRAME"),
        DE_NUMBER_OF_TILES("NUMBER OF LEVEL TILES"),
        DE_CURRENT_HASH_CELL("CURRENT HASH CELL"),
        DE_ENTITIES_NEARBY("ENTITIES NEARBY"),
        DE_TILES_NEARBY("TILES NEARBY"),
        DE_HASH_CELLS_FOR_SELECTION("HASH CELLS FOR MOUSE POSITION"),

        DE_RAY_DIRECTION("RAY DIRECTION"),
        DE_RAY_START("RAY START"),
        DE_RAY_END("RAY_END"),

        DE_LOGGING("LOGGING");

        private String tag;

        DEBUG_TAGS(String tag) {
            this.tag = tag;
        }


    }

    private EnumMap<DEBUG_TAGS, String> debugTags;
    private final int CONSOLE_LINE_HEIGHT = 24;
    ShapeRenderer shapeRenderer = new ShapeRenderer();


    // CONSTRUCTOR
    // ---------------------------------------------------------------------------------------------


    public RenderingEngine(WorldContainer worldContainer, AwesomeJumperMain game) {
        // Init renderer, start with a default camera and sprite batch
        super(CAMERA_WIDTH, CAMERA_HEIGHT);

        this.worldContainer = worldContainer;
        this.game = game;
        this.player = worldContainer.getPlayer();
        /*this.farSky01 = worldContainer.getLevel().getSkyBoxes().get(0);
        this.farSky02 = worldContainer.getLevel().getSkyBoxes().get(1);
        this.nearSky01 = worldContainer.getLevel().getSkyBoxes().get(2);
        this.nearSky02 = worldContainer.getLevel().getSkyBoxes().get(3);*/

        /**
         * Initialization of other renderers
         */
        this.hudRenderer = new HudRenderer(player);
        this.popUpRenderer = PopUpRenderer.createPopUpRenderer();
        camPositionInPx = new Vector2(0, 0);
        this.allTextures = new TextureAtlas();
        this.debugTags = new EnumMap<>(DEBUG_TAGS.class);
        for (DEBUG_TAGS c : DEBUG_TAGS.values()) {
            debugTags.put(c, c.tag);
        }

        /**
         * TODO: Implement Game states and different screens and let the states handle the
         * cursor.
         */

        /**
         * CURSOR SETUP
         */

        Pixmap mouse = new Pixmap(Gdx.files.internal("img/crosshair.png"));
        Cursor crosshair = Gdx.graphics.newCursor(mouse, 15, 15);
        Gdx.graphics.setCursor(crosshair);

        /** CAMERA SETUP:
         *  MAIN VIEW
         */
        camera.zoom = ZOOM;
        this.ppuX = Gdx.graphics.getWidth() / (CAMERA_WIDTH * camera.zoom);
        this.ppuY = Gdx.graphics.getHeight() / (CAMERA_HEIGHT * camera.zoom);
        camera.position.set(player.getPosition().x, player.getPosition().y, 0);
        camera.update();

        /**
         *CAMERA SETUP:
         * DEBUG
         */
        debugCam = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        debugCam.position.set(Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 2, 0);
        debugCam.update();

        debugBatch = new SpriteBatch();
        loadTexturesAndFonts();

        unprojectedMousePosition = new Vector3();


    }


    // LOAD TEXTURES
    // ---------------------------------------------------------------------------------------------

    /**
     * Loads all textures from the asset manager and generates corresponding animations for each entity.
     * Also loads world wall and floor textures.
     * Initializes all other render-classes such as HudRenderer and PopUpRenderer
     */
    public void loadTexturesAndFonts() {


        fontGenerator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/munro_regular.ttf"));
        // HUD
        hudRenderer.loadTexturesAndFonts(fontGenerator);

        // POPUPMANAGER
        popUpRenderer.loadTexturesAndFonts(fontGenerator);

        FreeTypeFontParameter debugFontParams = new FreeTypeFontParameter();
        debugFontParams.size = 24;
        debugFontParams.color = Color.WHITE;
        debugFontParams.shadowColor = Color.BLACK;
        debugFontParams.shadowOffsetX = 2;
        debugFontParams.shadowOffsetY = 2;
        debugFontParams.minFilter = Texture.TextureFilter.Nearest;
        debugFontParams.magFilter = Texture.TextureFilter.Nearest;

        debugFont = fontGenerator.generateFont(debugFontParams);

        // TEXTURE ATLAS
        // -----------------------------------------------------------------------------------------

        allTextures = game.getAssetManager().get(("img/textures.pack"), TextureAtlas.class);

        // TILE TEXTURES
        wallTextures = allTextures.findRegions("wall");
        floorTextures = allTextures.findRegions("floor");


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
     *
     * @param e
     */
    public void initGraphics(Entity e) {

        if (e.has(GRAPHICS)) {
            Graphics g = e.getGraphics();
            Array<TextureAtlas.AtlasRegion> regions = allTextures.findRegions(g.getTextureRegName());
            g.setIdleFrames(regions.first());
            g.setShadow(allTextures.findRegion(g.getTextureRegName() + "_shadow"));

            for (int i = 1; i < regions.size; i++) {
                g.addKeyFrame(regions.get(i));
            }
            g.createWalkAnimations();
        }

    }

    // RENDERING
    // ---------------------------------------------------------------------------------------------
    @Override
    public void render() {

        /**
         * SPRITEBATCH spriteBatch IS USED TO DRAW EVERYTHING TO THE SCREEN
         * SPRITEBATCH debugBatch IS USED TO DRAW DEBUG INFORMATION
         */

        debugCam.update();
        moveCamera(player.getPosition().x, player.getPosition().y);

        unprojectedMousePosition.set(Gdx.input.getX(), Gdx.input.getY(), 0f);
        camera.unproject(unprojectedMousePosition);

        camPositionInPx.set(camera.position.x * ppuX, camera.position.y * ppuY);

        spriteBatch.setProjectionMatrix(camera.combined);
        spriteBatch.begin();
        {
            // TILES
            renderTiles();
            // HITBOXES
            if (game.hitboxesEnabled()) {
                spriteBatch.end();
                drawHitboxes();
                spriteBatch.begin();
            }
            // ENTITIES
            if (game.entitiesEnabled()) {
                renderEntities();
            }
        }
        spriteBatch.end();

        // DEBUG INFO
        if (game.infoEnabled()) {
            renderDebugInfo();
        }

        // OTHER RENDERERS
        popUpRenderer.render();
        hudRenderer.render();
    }

    // CAMERA MOVEMENT
    // ---------------------------------------------------------------------------------------------

    /**
     * Moves the camera smoothly behind the player and stops it at the edges of the level
     *
     * @param playerX player's x position
     * @param playerY player's y position
     */
    private void moveCamera(float playerX, float playerY) {

        float tempX = camera.position.x;
        float tempY = camera.position.y;

        float xLerp = (playerX - tempX) * LERP_FACTOR;
        tempX += xLerp;

        float yLerp = (playerY - tempY) * LERP_FACTOR;
        tempY += yLerp;


        float updatedX, updatedY;
        if (playerX - camera.position.x >= 0f) {
            updatedX = ((int) (Math.floor(tempX * ppuX))) / ppuX;
        } else {
            updatedX = ((int) (Math.ceil(tempX * ppuX))) / ppuX;

        }

        if (playerY - camera.position.y >= 0f) {
            updatedY = ((int) (Math.floor(tempY * ppuY))) / ppuY;
        } else {
            updatedY = ((int) (Math.ceil(tempY * ppuY))) / ppuY;

        }

        if (updatedX < 4)
            updatedX = 4;
        if (updatedY < 4)
            updatedY = 4;

        camera.position.set(updatedX, updatedY, 0);
        camera.update();
    }


    // ENTITY RENDERING
    // ---------------------------------------------------------------------------------------------

    /**
     * Iterates over all entities in the world and renders them on the screen.
     */
    public void renderEntities() {
        Vector2 cameraPosition = new Vector2(camera.position.x, camera.position.y);
        for (Entity e : worldContainer.getEntitiesToBeRendered(cameraPosition, CAMERA_WIDTH, CAMERA_HEIGHT)) {
            e.render(spriteBatch);
        }

    }

    // DRAW TILES
    // ---------------------------------------------------------------------------------------------

    private void renderTiles() {
        Vector2 cameraPosition = new Vector2(camera.position.x, camera.position.y);
        TextureRegion wall;
        TextureRegion floor;
        for (Tile newTile : worldContainer.getTilesToBeRendered(cameraPosition, CAMERA_WIDTH * camera.zoom, CAMERA_HEIGHT * camera.zoom)) {
            Tile.TileType type = newTile.getType();
            Vector2 position = newTile.getPosition();
            if (!game.onDebugMode()) {
                switch (type) {
                    case WALL:
                        wall = wallTextures.get(newTile.tileIndex);
                        spriteBatch.draw(wall, position.x, position.y, Tile.SIZE, Tile.SIZE);
                        break;
                    case FLOOR:
                        floor = floorTextures.get(0);
                        spriteBatch.draw(floor, position.x, position.y, Tile.SIZE, Tile.SIZE);
                        break;
                    default:
                        break;

                }

            }

            if (game.onDebugMode()) {
                spriteBatch.end();
                shapeRenderer.setProjectionMatrix(camera.combined);
                shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
                if (!newTile.isPassable())
                    newTile.getCollisionBox().draw(shapeRenderer);
                shapeRenderer.end();
                spriteBatch.begin();
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
        spriteBatch.draw(background01, skyBoxPos01X, 0, fClouds01.y, fClouds01.z);
        spriteBatch.draw(background01, skyBoxPos02X, 0, fClouds01.y, fClouds01.z);
        spriteBatch.draw(sunTexture, fClouds02.x, 0, fClouds01.y, fClouds01.z);
        // ----- FAR AWAY CLOUDS
        spriteBatch.draw(farSkyTexture, fClouds01.x, 0, fClouds01.y, fClouds01.z);
        spriteBatch.draw(farSkyTexture, fClouds02.x, 0, fClouds02.y, fClouds02.z);
        // ----- NEAR CLOUDS
        spriteBatch.draw(nearSkyTexture, nClouds01.x, 0, nClouds01.y, nClouds01.z);
        spriteBatch.draw(nearSkyTexture, nClouds02.x, 0, nClouds02.y, nClouds02.z);

        if (fClouds01.x + fClouds01.y <= camera.position.x - camera.zoom / 2) {
            farSky01.setPositionX(fClouds02.x + fClouds02.y);
        }

        if (fClouds02.x + fClouds02.y <= camera.position.x - camera.zoom / 2) {
            farSky02.setPositionX(fClouds01.x + fClouds01.y);
        }

        if (fClouds01.x > camera.position.x - camera.zoom / 2) {
            farSky02.setPositionX(fClouds01.x - fClouds01.y);
        }

        if (fClouds02.x > camera.position.x - camera.zoom / 2) {
            farSky01.setPositionX(fClouds02.x - fClouds02.y);
        }

        if (nClouds01.x + nClouds01.y <= camera.position.x - camera.zoom / 2) {
            nearSky01.setPositionX(nClouds02.x + nClouds02.y);
        }

        if (nClouds02.x + nClouds02.y <= camera.position.x - camera.zoom / 2) {
            nearSky02.setPositionX(nClouds01.x + nClouds01.y);
        }

        if (nClouds01.x > camera.position.x - camera.zoom / 2) {
            nearSky02.setPositionX(nClouds01.x - nClouds01.y);
        }

        if (nClouds02.x > camera.position.x - camera.zoom / 2) {
            nearSky01.setPositionX(nClouds02.x - nClouds02.y);
        }

    }


    // DEBUG INFO
    // ---------------------------------------------------------------------------------------------

    private void renderDebugInfo() {
        // updating the debug information
        updateDebugStrings();
        debugBatch.setProjectionMatrix(debugCam.combined);
        debugBatch.begin();
        int i = 0;
        for (DEBUG_TAGS c : debugTags.keySet()) {
            debugFont.draw(debugBatch, debugTags.get(c), 14, debugCam.viewportHeight - CONSOLE_LINE_HEIGHT * (i + 1));
            i++;
        }
        debugBatch.end();
    }

    /**
     * INSERT HERE ALL NEW DEBUG STRINGS THAT SHOULD BE DISPLAYED IN GAME.
     * USE STRING BUILDER IF THE STRING CONTAINS OF MORE THAN 2 SUB STRINGS.
     */
    private void updateDebugStrings() {
        StringBuilder builder = new StringBuilder();

        // PLAYER STATE, VELOCITY, ACCELERATION ETC.
        setDebugTag(DE_ACCELERATION, "" + player.getAcceleration());
        setDebugTag(DE_ACCELERATION, "" + player.getAcceleration());
        setDebugTag(DE_VELOCITY, "" + player.getVelocity());
        setDebugTag(DE_STATE, player.getState().toString());
        setDebugTag(DE_POSITION, "" + player.getPosition());
        setDebugTag(DE_POS_OFFSET, "" + player.getBody().getBounds().getPositionAndOffset());

        // CAMERA POSITION
        builder.append(printVec(camera.position.x, camera.position.y));
        builder.append(" CAM PX: ").append(printVec(new Vector2(camera.position.cpy().x * ppuX, camera.position.cpy().y * ppuY)));
        setDebugTag(DE_CAMERA_POSITION, builder.toString());
        builder.setLength(0);

        // ENTITY COUNT
        builder.append(Entity.entityCount);
        builder.append(" REGISTERED: ").append(WorldContainer.registeredNodes);
        builder.append(" DRAWN: ").append(WorldContainer.renderNodes);
        setDebugTag(DE_ENTITY_COUNT, builder.toString());
        builder.setLength(0);

        // RESOLUTION, ZOOM & FPS
        builder.append(Gdx.graphics.getWidth()).append(" * ").append(Gdx.graphics.getHeight());
        builder.append(" ZOOM: ").append(camera.zoom).append(", FPS: ").append(Gdx.graphics.getFramesPerSecond());
        setDebugTag(DE_RESOLUTION, builder.toString());
        builder.setLength(0);

        // CURSOR POSITION
        builder.append(InputHandler.getCursorPosition()).append(" PIXEL: ").append(printVec(Gdx.input.getX(), Gdx.input.getY()));
        builder.append(" UNPRO: ").append(printVec(unprojectedMousePosition.x, unprojectedMousePosition.y));
        setDebugTag(DE_CURSOR_POSITION, builder.toString());
        builder.setLength(0);

        // ROOM REGION
        setDebugTag(DE_ROOM_REGION, worldContainer.getRandomLevelGenerator().getRegion(InputHandler.mouse));

        // DIFFERENT ENTITY COLLECTIONS
        setDebugTag(DE_ENTITY_INSTANCE_COUNT, worldContainer.getEntities().size());
        setDebugTag(DE_ENTITY_INSTANCE_COUNT, worldContainer.getEntities().size());
        setDebugTag(DE_PROJECTILE_COUNT, worldContainer.getProjectiles().size());
        setDebugTag(DE_MOBILE_ENTITY_COUNT, worldContainer.getMobileEntities().size());
        setDebugTag(DE_LIVING_ENTITY_COUNT, worldContainer.getLivingEntities().size());

        // WEAPON DROPS AND EQUIPPED WEAPONS
        builder.append(worldContainer.getWeaponDrops().size());
        builder.append(" EQUIPPED: ").append(player.getWeaponInventory().getWeaponsEquipped());
        setDebugTag(DE_WEAPON_DROP_COUNT, builder.toString());
        builder.setLength(0);

        // NUMBER OF COLLISION CHECKS PER FRAME
        setDebugTag(DE_COLLISION_CHECKS, Float.toString(CollisionController.calledPerFrame));
        setDebugTag(DE_NUMBER_OF_TILES, worldContainer.getRandomLevelGenerator().getCollidableTiles().size());

        // CURRENT HASH CELLS OCCUPIED BY PLAYER AND THEIR SIZE
        for (Vector2 index : worldContainer.getSpatialIndexes(player)) {
            builder.append(Utilities.printVec(index)).append(worldContainer.getHashCellSize(index));
        }
        setDebugTag(DE_CURRENT_HASH_CELL, builder.toString());
        builder.setLength(0);

        // NEARBY ENTITIES IN SAME HASH CELL
        setDebugTag(DE_ENTITIES_NEARBY, worldContainer.getEntitiesNearby(player).size());

        // NEARBY TILES IN THE SAME HASH CELL
        for(Tile t: worldContainer.getTilesNearby(player)) {
            builder.append(t.getPosition());
        }

        setDebugTag(DE_TILES_NEARBY, builder.toString());
        builder.setLength(0);

        // HASH CELLS FOR THE CURRENT CURSOR POSITION

        for(Vector2 index : worldContainer.getSpatialIndexes(worldContainer.getRandomLevelGenerator().getTile(InputHandler.getCursorPosition()))) {
            builder.append(Utilities.printVec(index));
        }
        setDebugTag(DE_HASH_CELLS_FOR_SELECTION, builder.toString());
        builder.setLength(0);
//        setDebugTag(DE_TILES_NEARBY, worldContainer.getTilesNearby(player).size());


        // RAY CASTING
        setDebugTag(DE_RAY_START, "" + worldContainer.getPlayer().getBody().getCenter());

        setDebugTag(DE_RAY_DIRECTION, "" + worldContainer.getPlayer().getBody().getOrientation());

        setDebugTag(DE_RAY_END, ""+worldContainer.getCrossedIndexes(worldContainer.getPlayer().getPosition(), worldContainer.getPlayer().getBody().getOrientation()));


        // LOGGING
        setDebugTag(DE_LOGGING, Boolean.toString(AwesomeJumperMain.onLogging()));

    }

    /**
     * Helper method that updates the given debug tag and adds the String s to the tag,
     * so it can be displayed in the debug view.
     *
     * @param tag tag to be altered
     * @param s   additional information in form of a String
     */
    private void setDebugTag(DEBUG_TAGS tag, String s) {
        String prefixTag = String.format("%-4s", tag.tag+":");
        debugTags.put(tag, prefixTag + s);
    }

    private void setDebugTag(DEBUG_TAGS tag, int i) {
        setDebugTag(tag, Integer.toString(i));
    }

    private void setDebugTag(DEBUG_TAGS tag, Float f) {
        setDebugTag(tag, Float.toString(f));
    }

    // HITBOXES
    // ---------------------------------------------------------------------------------------------
    public void drawHitboxes() {
        shapeRenderer.setProjectionMatrix(camera.combined);

        /**
         * Drawing entity hitboxes.
         */
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        {
            for (Entity e : worldContainer.getEntities()) {
                /**
                 * Only if the body flag is enabled, body hitboxes will be drawn.
                 */
                shapeRenderer.setColor(1, 0, 0, 1);
                e.getBounds().draw(shapeRenderer);
                if (game.bodyEnabled()) {
                    for (CollisionBox r : e.getBodyHitboxes()) {
                        shapeRenderer.setColor(Color.YELLOW);
                        r.draw(shapeRenderer);
                    }
                }

                if (e.getBody().isCollidedWithWorld()) {
                    shapeRenderer.end();
                    shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
                    shapeRenderer.setColor(Color.RED);
                    shapeRenderer.rect(e.getBounds().getPositionAndOffset().x, e.getBounds().getPositionAndOffset().y, e.getBounds().getWidth(), e.getBounds().getHeight());
                    shapeRenderer.end();
                    shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
                }
            }
        }
        shapeRenderer.end();

        /**
         * Drawing a line to the point of aim.
         */
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        {
            shapeRenderer.setColor(Color.GREEN);
            if (player.getWeaponInventory().isHoldingAWeapon())
                shapeRenderer.line(player.getWeaponInventory().getSelectedWeapon().getBody().getCenter(), player.getWeaponInventory().getSelectedWeapon().getBody().getAimReference());

            /**
             * Drawing a line to all entities in the neighbourhood.
             */
            shapeRenderer.setColor(Color.BLUE);
            for (Entity e : player.getBody().getEntityNeighbourHood()) {
                shapeRenderer.line(player.getBody().getCenter(), e.getBody().getCenter());
            }
        }
        shapeRenderer.end();

        // DRAW SPATIAL HASHING CELLS
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        {
            for (Vector2 index : worldContainer.getSpatialHashingData().keySet()) {

                int numberOfEntities = worldContainer.getEntitiesForCell(index).size();
                int numberOfTiles = worldContainer.getTilesForCell(index).size();


                if (numberOfTiles > 0) {
                    if (numberOfTiles == 1)
                        shapeRenderer.setColor(Color.RED);
                    else if (numberOfTiles == 2)
                        shapeRenderer.setColor(Color.GREEN);
                    else if (numberOfTiles == 3)
                        shapeRenderer.setColor(Color.BLUE);
                    else if (numberOfTiles > 3)
                        shapeRenderer.setColor(Color.PINK);
                    shapeRenderer.rect(index.x, index.y, worldContainer.getSpatialFactor(), worldContainer.getSpatialFactor());
                }
            }
            //Utilities.log(String.format("%1d|%2d|%3d%4d", x*spatialSize,y*spatialSize, worldContainer.getSpatialFactor(), worldContainer.getSpatialFactor()));
        }
        shapeRenderer.end();


        // HITBOXES OF TILES AFFECTED BY COLLISION DETECTION
        Gdx.gl.glEnable(GL20.GL_BLEND);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        {
            shapeRenderer.setColor(0, 0.5f, 0.5f, 1);

            /**
             * vertical and horizontal components are separated here to show  which impact is bigger
             */
            Vector2 playerX = new Vector2(player.getVelocity().x, 0f).cpy().scl(player.getPlayerDelta()).scl(25);
            Vector2 playerY = new Vector2(0f, player.getVelocity().y).cpy().scl(player.getPlayerDelta()).scl(25);

            shapeRenderer.rectLine(player.getPosition(), player.getPosition().cpy().add(playerX), 5 * (1 / ppuX));
            shapeRenderer.rectLine(player.getPosition(), player.getPosition().cpy().add(playerY), 5 * (1 / ppuY));

            shapeRenderer.setColor(1f, 0f, 0f, 0.5f);
            for (Tile t : worldContainer.getCollisionTiles()) {
                if (t != null) {
                    shapeRenderer.rect(t.getPosition().x, t.getPosition().y, Tile.SIZE, Tile.SIZE);
                }
            }
        }
        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }


    public void resize(int w, int h) {
        debugCam = new OrthographicCamera(w, h);
        debugCam.position.set(w / 2f, h / 2f, 0);
        ppuX = Gdx.graphics.getWidth() / (CAMERA_WIDTH * camera.zoom);
        ppuY = Gdx.graphics.getHeight() / (CAMERA_HEIGHT * camera.zoom);
        hudRenderer.resize(w, h);
        popUpRenderer.resize(w, h);
    }


    // GETTER AND SETTER
    // ---------------------------------------------------------------------------------------------

    public OrthographicCamera getGameCamera() {
        return camera;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public void setZoom(float zoom) {
        float oldZoom = camera.zoom;
        oldZoom += 0.1d * zoom;
        camera.zoom = oldZoom;
        ppuX = Gdx.graphics.getWidth() / (CAMERA_WIDTH * camera.zoom);
        ppuY = Gdx.graphics.getHeight() / (CAMERA_HEIGHT * camera.zoom);
    }

}
