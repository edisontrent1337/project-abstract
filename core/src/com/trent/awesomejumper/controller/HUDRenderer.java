package com.trent.awesomejumper.controller;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.trent.awesomejumper.models.Player;
import com.trent.awesomejumper.models.weapons.Weapon;

/**
 * Created by Sinthu on 27.02.2016.
 * TODO: DOCUMENTATION
 * TODO: HUD RENDERER NEEDS OWN FONTS.
 */
public class HUDRenderer {


    private final String NO_WEAPON = "NO WEAPON";

    private final int WEAPON_OFFSET_X = 200;
    private final int WEAPON_OFFSET_Y = 100;
    private final int SHADOW_OFFSET = 4;

    private static final int XY_PADDING = 100;

    private OrthographicCamera hudCam;
    private SpriteBatch hudBatch;
    private BitmapFont hudFont;
    private Player player;

    private Weapon selectedWeapon;
    private int w, h;

    public HUDRenderer(Player player) {
        this.hudCam = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        this.hudBatch = new SpriteBatch();
        this.player = player;
        hudCam.position.set(Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 2, 0);

        this.w = Gdx.graphics.getWidth();
        this.h = Gdx.graphics.getHeight();

    }


    // ---------------------------------------------------------------------------------------------
    // METHODS & FUNCTIONS
    // ---------------------------------------------------------------------------------------------


    public void loadTextures() {
        hudFont = new BitmapFont(Gdx.files.internal("fonts/munro_outlined.fnt"), Gdx.files.internal("fonts/munro_outlined_0.png"), false);
        hudFont.setColor(Color.WHITE);
        hudFont.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        hudCam.update();
    }


    public void render() {

        hudCam.update();

        hudBatch.setProjectionMatrix(hudCam.combined);
        hudBatch.begin();


        drawHealthAndExp();
        drawTime();
        drawMiniMap();
        drawWeaponHUD();

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


        hudFont.draw(hudBatch, player.getHealth().toString(), 100, Gdx.graphics.getHeight() - 50);


        hudBatch.end();

    }


    public void resize(int w, int h) {
        hudCam = new OrthographicCamera(w, h);
        hudCam.position.set(w / 2, h / 2, 0);
        this.w = w;
        this.h = h;
    }


    private void drawFontWithShadow(String text, int x, int y) {
        hudFont.setColor(Color.BLACK);
        hudFont.draw(hudBatch, text, x + SHADOW_OFFSET, y - SHADOW_OFFSET);
        hudFont.setColor(Color.WHITE);
        hudFont.draw(hudBatch, text, x, y);
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

        hudFont.draw(hudBatch, weaponName, w - (nameWidth +50), 50);
        hudFont.draw(hudBatch, ammo, w - (ammoWidth + 50), 100);


    }

    private void drawHealthAndExp() {

    }

    private void drawTime() {

    }

    private void drawMiniMap() {

    }


}
