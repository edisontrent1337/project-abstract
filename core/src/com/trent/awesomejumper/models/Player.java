package com.trent.awesomejumper.models;


import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.trent.awesomejumper.engine.modelcomponents.Body;
import com.trent.awesomejumper.engine.modelcomponents.Graphics;
import com.trent.awesomejumper.engine.modelcomponents.Health;
import com.trent.awesomejumper.engine.modelcomponents.Weapon;
import com.trent.awesomejumper.testing.CollisionBox;

/**
 * Created by Sinthu on 12.06.2015.
 */
public class Player extends Entity {

    // MEMBERS & INSTANCES
    // ---------------------------------------------------------------------------------------------
    private final float headHitboxSize, armHitBoxSize, legHitBoxSize;

    private CollisionBox head, rightArm, rightFoot, leftArm, leftFoot;

    private float playerDelta;

    // CONSTRUCTOR
    // ---------------------------------------------------------------------------------------------

    public Player(Vector2 position) {
        this.body = new Body(this);
        body.setPosition(position);
        body.setBounds(new Rectangle(position.x, position.y, 0.5f, 0.5f));
        headHitboxSize = 0.2f;
        armHitBoxSize = 0.2f;
        legHitBoxSize = 0.2f;

       /**
        *  head = new Rectangle(position.x + (SIZE - headHitboxSize)/2, position.y + SIZE / 1.6f, headHitboxSize, headHitboxSize);
        rightArm = new Rectangle(position.x + (SIZE - armHitBoxSize)/2 + 0.2f, position.y + SIZE / 2.8f, armHitBoxSize, armHitBoxSize);
        leftArm = new Rectangle(position.x + (SIZE - armHitBoxSize)/2 - 0.2f, position.y + SIZE / 2.8f, armHitBoxSize, armHitBoxSize);
        rightFoot = new Rectangle(position.x + (SIZE - legHitBoxSize)/2 + 0.2f, position.y, legHitBoxSize, legHitBoxSize);
        leftFoot = new Rectangle(position.x + (SIZE - legHitBoxSize)/2 - 0.2f, position.y, legHitBoxSize, legHitBoxSize);
        head = new CollisionBox(new Vector2(position.x + (SIZE - headHitboxSize) / 2, position.y + SIZE / 1.6f), headHitboxSize, headHitboxSize);
        body.add(head);
        body.add(rightArm);
        body.add(leftArm);
        body.add(rightFoot);
        body.add(leftFoot);
        */
        rightArm = new CollisionBox(new Vector2(position.x + (SIZE - armHitBoxSize) / 2 + 0.2f, position.y + SIZE / 2.8f), armHitBoxSize, armHitBoxSize);
        leftArm = new CollisionBox(new Vector2(position.x + (SIZE - armHitBoxSize) / 2 - 0.2f, position.y + SIZE / 2.8f), armHitBoxSize, armHitBoxSize);
        rightFoot = new CollisionBox(new Vector2(position.x + (SIZE - legHitBoxSize) / 2 + 0.2f, position.y), legHitBoxSize, legHitBoxSize);
        leftFoot = new CollisionBox(new Vector2(position.x + (SIZE - legHitBoxSize) / 2 - 0.2f, position.y), legHitBoxSize, legHitBoxSize);
        head = new CollisionBox(position, 0.5f, 0.5f);
        //body.add(head);
        body.getBodyHitboxes().add(head);

    }





    @Override
    public void update(float delta) {
        entityTime += delta;
        playerDelta = delta;
        super.update(delta);

    }

    @Override
    public void setHitboxes(Vector2 position) {
        head.setPosition(position.x + (SIZE - headHitboxSize) / 2, position.y + SIZE / 1.6f);
        rightArm.setPosition(position.x + (SIZE - armHitBoxSize) / 2 + 0.2f, position.y + SIZE / 2.8f);
        leftArm.setPosition(position.x + (SIZE - armHitBoxSize) / 2 - 0.2f, position.y + SIZE / 2.8f);
        rightFoot.setPosition(position.x + (SIZE - legHitBoxSize) / 2 + 0.2f, position.y);
        leftFoot.setPosition(position.x + (SIZE - legHitBoxSize) / 2 - 0.2f, position.y);
    }


    public float getPlayerDelta() {
        return playerDelta;
    }

}
