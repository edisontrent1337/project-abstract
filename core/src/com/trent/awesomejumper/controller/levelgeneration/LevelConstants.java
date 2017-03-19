package com.trent.awesomejumper.controller.levelgeneration;

import com.badlogic.gdx.math.Vector2;

/**
 * Created by Sinthu on 28.07.2016.
 */
public final class LevelConstants {
    public static final int NORTH = 0;
    public static final int EAST = 1;
    public static final int SOUTH = 2;
    public static final int WEST = 3;

    /**
     * Constants for auto tiling
     */
    public static final int VOID = 0;
    public static final int NORTH_WALL = 1;
    public static final int EAST_WALL = 2;
    public static final int SOUTH_WALL = 3;
    public static final int WEST_WALL = 4;
    public static final int SOUTH_EAST_INNER = 5;
    public static final int SOUTH_WEST_INNER = 6;
    public static final int NORTH_EAST_INNER = 7;
    public static final int NORTH_WEST_INNER = 8;

    public static final int SOUTH_EAST_OUTER = 9;
    public static final int SOUTH_WEST_OUTER = 10;
    public static final int NORTH_WEST_OUTER = 11;
    public static final int NORTH_EAST_OUTER = 12;

    public static final Vector2 N_DIR = new Vector2(0,1);
    public static final Vector2 E_DIR = new Vector2(1,0);
    public static final Vector2 S_DIR = new Vector2(0,-1);
    public static final Vector2 W_DIR = new Vector2(-1,0);

    public static final Vector2 NE_DIR = new Vector2(1,1);
    public static final Vector2 SE_DIR = new Vector2(1,-1);
    public static final Vector2 SW_DIR = new Vector2(-1,-1);
    public static final Vector2 NW_DIR = new Vector2(-1,1);


    public static final Vector2[] CARDINAL_DIRS = new Vector2[] {
            N_DIR,
            E_DIR,
            S_DIR,
            W_DIR};

    public static final Vector2[] CARDINALS_AND_INTERMEDIATES = new Vector2[] {
            NW_DIR,
            N_DIR,
            NE_DIR,
            W_DIR,
            E_DIR,
            SW_DIR,
            S_DIR,
            SE_DIR};
}
