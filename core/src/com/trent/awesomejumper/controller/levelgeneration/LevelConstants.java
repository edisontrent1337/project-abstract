package com.trent.awesomejumper.controller.levelgeneration;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

/**
 * Created by Sinthu on 28.07.2016.
 */
public final class LevelConstants {
    public static final int NORTH = 0;
    public static final int EAST = 1;
    public static final int SOUTH = 2;
    public static final int WEST = 3;

    public static final Vector2 N_DIR = new Vector2(0,1);
    public static final Vector2 E_DIR = new Vector2(1,0);
    public static final Vector2 S_DIR = new Vector2(0,-1);
    public static final Vector2 W_DIR = new Vector2(-1,0);

    public static final Vector2[] CARDINALS = new Vector2[] {N_DIR,E_DIR,S_DIR,W_DIR};
}
