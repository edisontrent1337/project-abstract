package com.trent.awesomejumper.models;

import com.badlogic.gdx.math.Vector2;

/**
 * Created by Sinthu on 16.06.2015.
 */
public class Environment {

    // MEMBERS & INSTANCES
    // ---------------------------------------------------------------------------------------------

    public static final float SIZE = 0.5f;
    private Vector2 position;
    public enum EnvironmentType {
        GRASS(0),
        GRASSL(1),
        GRASSR(2),
        SNOW(3),
        FLOWER(4),
        RED_MUSHROOM(5),
        GREEN_MUSHROOM(6);
        private int value;

        EnvironmentType(int value) {
            this.value = value;
        }

    }
    private EnvironmentType type;

    // CONSTRUCTOR
    // ---------------------------------------------------------------------------------------------

    public Environment(Vector2 position, EnvironmentType type) {
        this.position = position;
        this.type = type;
    }

    // GETTER & SETTER
    // ---------------------------------------------------------------------------------------------

    public EnvironmentType getType() {
        return type;
    }

    public Vector2 getPosition() {
        return position;
    }


}
