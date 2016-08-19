package com.trent.awesomejumper.controller.rendering;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.trent.awesomejumper.models.Player;
import com.trent.awesomejumper.models.weapons.Weapon;

/**
 * Created by Sinthu on 27.02.2016.
 * TODO: DOCUMENTATION
 * TODO: HUD RENDERER NEEDS OWN FONTS.
 */
public class HudRenderer extends Renderer {


    private final String NO_WEAPON = "NO WEAPON";

    private final int WEAPON_OFFSET_X = 200;
    private final int WEAPON_OFFSET_Y = 100;
    private final int SHADOW_OFFSET = 4;

    private static final int XY_PADDING = 100;

   // private OrthographicCamera hudCam;
   // private SpriteBatch hudBatch;


    // FONTS:
    private BitmapFont hudFont;
    private FreeTypeFontParameter hudFontParams;

    private Player player;

    private Weapon selectedWeapon;
    private int w, h;

    private static final float CAMERA_WIDTH = Gdx.graphics.getWidth();
    private static final float CAMERA_HEIGHT = Gdx.graphics.getHeight();

    public HudRenderer(Player player) {
        // Init renderer, start with a default camera and sprite batch
        super(CAMERA_WIDTH,CAMERA_HEIGHT);
        camera.position.set(CAMERA_WIDTH / 2, CAMERA_HEIGHT / 2, 0);
        //this.hudCam = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        //this.hudBatch = new SpriteBatch();
        this.player = player;

        this.w = Gdx.graphics.getWidth();
        this.h = Gdx.graphics.getHeight();

    }


    // ---------------------------------------------------------------------------------------------
    // METHODS & FUNCTIONS
    // ---------------------------------------------------------------------------------------------


    public void loadTexturesAndFonts(FreeTypeFontGenerator generator) {
        hudFontParams = new FreeTypeFontParameter();
        hudFontParams.color = Color.WHITE;
        hudFontParams.size = 32;
        hudFontParams.borderColor = Color.BLACK;
        hudFontParams.shadowOffsetX = 2;
        hudFontParams.shadowOffsetY = 2;
        hudFontParams.minFilter = Texture.TextureFilter.Nearest;
        hudFontParams.magFilter = Texture.TextureFilter.Nearest;

        hudFont = generator.generateFont(hudFontParams);



        /*hudFont = new BitmapFont(Gdx.files.internal("fonts/munro_outlined.fnt"), Gdx.files.internal("fonts/munro_outlined_0.png"), false);
        hudFont.setColor(Color.WHITE);
        hudFont.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);*/
        camera.update();
    }


    public void render() {

        camera.update();
        spriteBatch.setProjectionMatrix(camera.combined);
        spriteBatch.begin();
            drawHealthAndExp();
            drawTime();
            drawMiniMap();
            drawWeaponHUD();
            hudFont.draw(spriteBatch, player.getHealth().toString(), 100, Gdx.graphics.getHeight() - 50);
        spriteBatch.end();
        /**
         * Render weapon information
         *
         */
        /*GlyphLayout weapon = new GlyphLayout();
        GlyphLayout ammo = new GlyphLayout();
        ammo.setText(hudFont, selectedWeapon.getWeaponComponent().getStatus());
        weapon.setText(hudFont, selectedWeapon.getWeaponComponent().getName());

        if(!player.getWeaponInventory().isHoldingAWeapon()) {
            drawFontWithShadow(NO_WEAPON, Gdx.graphics.getWidth() - WEAPON_OFFSET_X, WEAPON_OFFSET_Y);
        }

        else {
            drawFontWithShadow(selectedWeapon.getWeaponComponent().getName(), Gdx.graphics.getWidth() - WEAPON_OFFSET_X, WEAPON_OFFSET_Y);
            drawFontWithShadow(selectedWeapon.getWeaponComponent().getStatus(), Gdx.graphics.getWidth() - WEAPON_OFFSET_X / 2, WEAPON_OFFSET_Y);

        }*/





    }


    public void resize(int w, int h) {
        camera = new OrthographicCamera(w, h);
        camera.position.set(w / 2, h / 2, 0);
        this.w = w;
        this.h = h;
        camera.update();
    }


    private void drawFontWithShadow(String text, int x, int y) {
        hudFont.setColor(Color.BLACK);
        hudFont.draw(spriteBatch, text, x + SHADOW_OFFSET, y - SHADOW_OFFSET);
        hudFont.setColor(Color.WHITE);
        hudFont.draw(spriteBatch, text, x, y);
    }


    /**
     * Draws the weapon HUD. This should include a silhouette of the equipped weapon,
     * its name and ammunition.
     */
    private void drawWeaponHUD() {
        GlyphLayout weaponName = new GlyphLayout();
        GlyphLayout ammo = new GlyphLayout();

        if (player.getWeaponInventory().isHoldingAWeapon()) {
            selectedWeapon = player.getWeaponInventory().getSelectedWeapon();
            weaponName.setText(hudFont, selectedWeapon.getName());
            ammo.setText(hudFont, selectedWeapon.getStatus());
        }
        else {
            weaponName.setText(hudFont,NO_WEAPON);
            ammo.setText(hudFont,"");
        }

        float nameWidth = weaponName.width;
        float ammoWidth = ammo.width;

        hudFont.draw(spriteBatch, weaponName, w - (nameWidth +50), 50);
        hudFont.draw(spriteBatch, ammo, w - (ammoWidth + 50), 100);


    }

    private void drawHealthAndExp() {

    }

    private void drawTime() {

    }

    private void drawMiniMap() {

    }


}
