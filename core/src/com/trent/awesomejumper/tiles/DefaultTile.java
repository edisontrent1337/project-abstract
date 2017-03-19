package com.trent.awesomejumper.tiles;

import com.badlogic.gdx.math.Vector2;
import com.trent.awesomejumper.models.Player;
import static com.trent.awesomejumper.utils.PhysicalConstants.*;

/**
 * Created by Sinthu on 26.06.2015.
 */
public class DefaultTile extends Tile{


    public DefaultTile(Vector2 position) {
        super(position,TileType.WALL,1f,1f,0.8f,STANDARD_MAX_WALKING_VELOCITY,7f,false,false);
    }

    @Override
    public void action(Player player, float delta) {

    }
}
