package com.trent.awesomejumper.controller;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.trent.awesomejumper.models.Player;
import com.trent.awesomejumper.models.weapons.Weapon;

/**
 * Created by Sinthu on 27.02.2016.
 */
public class HUDRenderer {


    private OrthographicCamera hudCam;
    private SpriteBatch hudBatch;
    private BitmapFont hudFont;
    private Player player;

    private Weapon primaryWeapon;
    private Weapon secondaryWeapon;

    public HUDRenderer(Player player) {
        this.hudCam = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        this.hudBatch = new SpriteBatch();
        this.player = player;
        hudCam.position.set(Gdx.graphics.getWidth()/2, Gdx.graphics.getHeight()/2, 0);

    }


    // ---------------------------------------------------------------------------------------------
    // METHODS & FUNCTIONS
    // ---------------------------------------------------------------------------------------------


    public void loadTextures() {
        hudFont = new BitmapFont(Gdx.files.internal("fonts/munro_outlined.fnt"),Gdx.files.internal("fonts/munro_outlined_0.png"),false);
        hudFont.setColor(Color.WHITE);
        hudFont.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        hudCam.update();
    }


    public void render() {

        hudCam.update();
        primaryWeapon = player.getWeaponInventory().getWeaponPrimary();
        secondaryWeapon = player.getWeaponInventory().getWeaponSecondary();

        hudBatch.setProjectionMatrix(hudCam.combined);
        hudBatch.begin();


        if(!player.getWeaponInventory().isPrimaryEquipped()) {
            hudFont.draw(hudBatch, "NO WEAPON",Gdx.graphics.getWidth() - 200,50);
        }

        else {
            hudFont.draw(hudBatch, primaryWeapon.getWeaponComponent().getWeaponName(), Gdx.graphics.getWidth() - 200, 50);
            hudFont.draw(hudBatch, primaryWeapon.getWeaponComponent().getWeaponStatus(), Gdx.graphics.getWidth() - 100, 50);

        }

        if(!player.getWeaponInventory().isSecondaryEquipped()) {
            hudFont.draw(hudBatch, "NO WEAPON",Gdx.graphics.getWidth() - 200,25);
        }

        else {
            hudFont.draw(hudBatch, secondaryWeapon.getWeaponComponent().getWeaponName(), Gdx.graphics.getWidth() - 200, 25);
            hudFont.draw(hudBatch, secondaryWeapon.getWeaponComponent().getWeaponStatus(), Gdx.graphics.getWidth() - 100, 25);

        }


        hudFont.draw(hudBatch,player.getHealth().toString(), 100,Gdx.graphics.getHeight() - 50);

        hudBatch.end();

    }


    public void resize(int w, int h) {
        hudCam = new OrthographicCamera(w,h);
        hudCam.position.set(w/2,h/2,0);
    }

}
