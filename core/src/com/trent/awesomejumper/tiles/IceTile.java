package com.trent.awesomejumper.tiles;

import com.badlogic.gdx.math.Vector2;
import com.trent.awesomejumper.models.Player;
import static com.trent.awesomejumper.utils.PhysicalConstants.*;

/**
 * Created by Sinthu on 26.06.2015.
 */
public class IceTile extends Tile {


    public IceTile(Vector2 position) {
        super(position,TileType.ICE, 1f,1f, 0.95f, STANDARD_MAX_WALKING_VELOCITY, 7f, false, false);
    }

    @Override
    public void action(Player player, float delta) {

    }
}
