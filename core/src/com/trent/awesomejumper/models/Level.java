package com.trent.awesomejumper.models;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.trent.awesomejumper.tiles.DefaultTile;
import com.trent.awesomejumper.tiles.IceTile;
import com.trent.awesomejumper.tiles.StoneTile;
import com.trent.awesomejumper.tiles.Tile;
import com.trent.awesomejumper.tiles.Tile.TileType;
import com.trent.awesomejumper.tiles.TrampolineTile;

import java.util.ArrayList;
import static com.trent.awesomejumper.tiles.Tile.TileType.*;

/**
 * Created by Sinthu on 12.06.2015.
 */
public class Level {

    // MEMBERS & INSTANCES
    // ---------------------------------------------------------------------------------------------

    private final int levelWidth, levelHeight;
    // TODO: change this into a two dimensional array list
    private Tile[][]tiles;
    private Array<SkyBox> skyBoxes;
    Array<Environment> environment = new Array<Environment>();

    // CONSTRUCTOR
    // ---------------------------------------------------------------------------------------------

    public Level() {
        levelHeight = 100;
        levelWidth = 100;
        loadDemoLevel();
    }

    private void loadDemoLevel() {
        tiles = new Tile[levelWidth][levelHeight];
        skyBoxes = new Array<>();
        // FAR AWAY CLOUDS
        skyBoxes.add(new SkyBox(new Vector2(0, 2f), 16.66667f, -0.3f));
        skyBoxes.add(new SkyBox(new Vector2(16.66667f, 2f), 16.6667f, -0.3f));
        // NEAR CLOUDS
        skyBoxes.add(new SkyBox(new Vector2(0, 2f), 16.66666667f, -0.6f));
        skyBoxes.add(new SkyBox(new Vector2(16.6667f, 2f), 16.6667f, -0.6f));

        // INIT LEVEL
        for(int x = 0; x < levelWidth - 1; x++) {
            for(int y = 0; y < levelHeight - 1; y++) {
                tiles[x][y] = null;
            }
        }

        loadLevelFile("level02");
        addEnvironments();

    }


    // READ LEVEL FILE
    // ---------------------------------------------------------------------------------------------

    private void loadLevelFile(String name) {
        FileHandle handle = Gdx.files.internal("data/" + name + ".txt");
        String levelLines[] = handle.readString().split("[\\n]+");
        ArrayList<ArrayList<String>> levelContent = new ArrayList<>();
        for(String line : levelLines) {
          //  Gdx.app.log("LEVEL:", line);
            ArrayList<String> row = new ArrayList<>();
            String[] values = line.trim().split(" ");
            for(String string : values) {
                if(!string.isEmpty())
                    row.add(string);
            }
            levelContent.add(row);

        }


        for(int y = 0; y < levelContent.size(); y++) {
            for (int x = 0; x < levelContent.get(0).size(); x++) {
                String type = levelContent.get(y).get(x);
                int yPos = levelContent.size() - y - 1;
                Vector2 position = new Vector2(x, yPos);

                switch (Integer.parseInt(type)) {

                    case 1:
                        tiles[x][yPos] = new DefaultTile(position);
                        break;
                    case 2:
                        tiles[x][yPos] = new StoneTile(position);
                        break;
                    case 3:
                        tiles[x][yPos] = new Tile(new Vector2(x, yPos), TileType.FLOOR, true);
                        break;
                    case 4:
                        tiles[x][yPos] = new IceTile(position);
                        break;
                    case 5:
                        tiles[x][yPos] = new TrampolineTile(position);
                        break;

                }

            }
        }

    }

    // ADD GRASS, MUSHROOMS, TALL GRASS ETC
    // ---------------------------------------------------------------------------------------------

    private void addEnvironments() {
        for (int x = 0; x < levelWidth - 1; x++) {
            for (int y = 0; y < levelHeight - 1; y++) {
                if (x != 0) {
                    Tile underLayer = tiles[x][y];
                    Tile upperLayer = tiles[x][y + 1];
                    Tile leftLayer = tiles[x - 1][y];
                    Tile rightLayer = tiles[x + 1][y];
                    if (underLayer != null && upperLayer == null && !underLayer.isPassable()) {
                        if (underLayer.getType() != ICE) {
                            if (leftLayer != null && rightLayer != null) {
                                environment.add(new Environment(new Vector2(x, y), Environment.EnvironmentType.GRASS));
                                //environment.add(new Environment(new Vector2(x, y + 1), "grass02"));
                            }
                            if (leftLayer == null) {
                                environment.add(new Environment(new Vector2(x, y), Environment.EnvironmentType.GRASSL));
                            }
                            if (rightLayer == null) {
                                environment.add(new Environment(new Vector2(x, y), Environment.EnvironmentType.GRASSR));
                            }

                        }

                        else {
                            environment.add(new Environment(new Vector2(x, y), Environment.EnvironmentType.SNOW));
                        }
                    }

                }

            }

        }


    }


    // GETTER AND SETTER
    // ---------------------------------------------------------------------------------------------

    public int getLevelWidth() {
        return levelWidth;
    }

    public int getLevelHeight() {
        return levelHeight;
    }

    public Tile getTile(int x, int y) {
        return tiles[x][y];
    }

    public Array<SkyBox> getSkyBoxes() {
        return skyBoxes;
    }

    public Array<Environment> getEnvironment() {
        return environment;
    }


    // UTILS
    // ---------------------------------------------------------------------------------------------

    public boolean checkBounds(int x, int y) {
        if (x < 0 || x > levelWidth || y < 0 || y > levelHeight) {
            Gdx.app.log("ERROR: "," OBJECT OUT OF BOUNDS");
            return false;
        }

        return true;

    }

}
