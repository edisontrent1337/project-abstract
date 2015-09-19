package com.trent.awesomejumper.tiles;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.trent.awesomejumper.models.Player;
import static com.trent.awesomejumper.utils.PhysicalConstants.*;

/**
 * Created by Sinthu on 26.06.2015.
 */
public class TrampolineTile extends Tile {


    public TrampolineTile(Vector2 position) {
        super(position, TileType.TRAMPOLINE, 1f, 1f, 0.8f,STANDARD_MAX_WALKING_VELOCITY, 7f, false, true);
    }

    @Override
    public void action(Player player, float delta) {
        Gdx.app.log("ACTION TRIGGERED:", "TRAMPOLINE AT" + "[" + getPosition().x + "|" + getPosition().y + "]");
        player.setVelocityY(MAX_TRAMPOLINE_SPEED);
        player.getVelocity().scl(delta);
    }
}
