package com.trent.awesomejumper.tiles;

import com.badlogic.gdx.math.Vector2;
import com.trent.awesomejumper.models.Player;

/**
 * Created by Sinthu on 26.06.2015.
 */
public class StoneTile extends Tile {


    public StoneTile(Vector2 position) {
        super(position, TileType.STONE, 1f, 1f, 0.95f, 6.5f, 7f, false, false);
    }

    @Override
    public void action(Player player, float delta) {

    }
}
