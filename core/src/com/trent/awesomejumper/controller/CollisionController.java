package com.trent.awesomejumper.controller;


import com.badlogic.gdx.math.Vector2;
import com.trent.awesomejumper.models.Level;
import com.trent.awesomejumper.models.Player;
import com.trent.awesomejumper.models.WorldContainer;
import com.trent.awesomejumper.testing.CollisionBox;
import com.trent.awesomejumper.tiles.Tile;

import java.util.ArrayList;

/**
 * Created by Sinthu on 04.11.2015.
 */
public class CollisionController {

    private WorldContainer worldContainer;
    private Level level;
    private Player player;

    private Vector2 minimumTranslateVector;
    private float overlap;

    private boolean isColliding;


    /**
     *
     * Idee:
     *
     * Zunächst soll die vertikale Kollision erkannt werden.
     * Unter allen Tiles die in Frage kommen soll die Kollision gefunden werden,
     * deren minimum translation vector am kleinsten ist.
     * Man sucht das Minimum aus allen Tiles und CollisionBoxes.
     * Erst, wenn das komplette Array aus Tiles durchlaufen wurde, wird der minimum translation
     * vector auf den Spieler angwandt und somit die Kollision resolviert.
     *
     * Erst, wenn die vertikale Kollision resolviert wurde, kann die horizontale Kollision erkannt
     * und resolviert werden.
     *
     */



    // CONSTRUCTOR
    // ---------------------------------------------------------------------------------------------

    public CollisionController(WorldContainer worldContainer) {

        this.worldContainer = worldContainer;
        this.player = worldContainer.getPlayer();
        this.level = worldContainer.getLevel();

    }


    // METHODS
    // ---------------------------------------------------------------------------------------------

    /**
     * The main functions which checks and resolves any occurring collisions
     */
    public void checkCollisions(float delta) {

        /**
         * Vertical Collision first
         */
        player.getVelocity().scl(delta);
        int cdStartX, cdEndX, cdStartY, cdEndY;




        cdStartY = (int) (player.getBounds().y);
        cdEndY = (int) (player.getBounds().y + player.getBounds().height);


        // X AXIS INTERVAL DEPENDS ON PLAYERS MOVEMENT DIRECTION
        if (player.getVelocity().x < 0) {
            cdStartX = cdEndX = (int) (Math.floor(player.getBounds().x + player.getVelocity().x));
        } else {
            cdStartX = cdEndX = (int) (Math.floor(player.getBounds().x + player.getBounds().width
                    + player.getVelocity().x));

        }

       worldContainer.createCollisionTiles(cdStartX, cdEndX, cdStartY, cdEndY);

        for(Tile tile : worldContainer.getCollisionTiles()) {
            /**
             * If the current tile is an air tile, go to the next one.
             */
            if(tile == null) {
                continue;
            }


            /**
             * For every collisionBox of the player the collision detection is repeated.
             */
            for(CollisionBox collisionBox : player.getBody()) {



            }





        }







    }


    public boolean isOverlapping(Tile tile, CollisionBox collisionBox) {

        return false;
    }




}
