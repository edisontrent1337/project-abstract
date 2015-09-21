package com.trent.awesomejumper.models;


import com.badlogic.gdx.math.Vector2;
import com.trent.awesomejumper.testing.CollisionBox;

/**
 * Created by Sinthu on 12.06.2015.
 */
public class Player extends Entity {

    // MEMBERS & INSTANCES
    // ---------------------------------------------------------------------------------------------
    private final float headHitboxSize, armHitBoxSize, legHitBoxSize;
    // private Rectangle head, rightArm, rightFoot, leftArm, leftFoot;

    private CollisionBox head, rightArm, rightFoot, leftArm, leftFoot;

    // CONSTRUCTOR
    // ---------------------------------------------------------------------------------------------

    public Player(Vector2 position) {
        super(position, 1f, 100f);
        headHitboxSize = 0.2f;
        armHitBoxSize = 0.2f;
        legHitBoxSize = 0.2f;
       /* head = new Rectangle(position.x + (SIZE - headHitboxSize)/2, position.y + SIZE / 1.6f, headHitboxSize, headHitboxSize);
        rightArm = new Rectangle(position.x + (SIZE - armHitBoxSize)/2 + 0.2f, position.y + SIZE / 2.8f, armHitBoxSize, armHitBoxSize);
        leftArm = new Rectangle(position.x + (SIZE - armHitBoxSize)/2 - 0.2f, position.y + SIZE / 2.8f, armHitBoxSize, armHitBoxSize);
        rightFoot = new Rectangle(position.x + (SIZE - legHitBoxSize)/2 + 0.2f, position.y, legHitBoxSize, legHitBoxSize);
        leftFoot = new Rectangle(position.x + (SIZE - legHitBoxSize)/2 - 0.2f, position.y, legHitBoxSize, legHitBoxSize);*/
        /*head = new CollisionBox(new Vector2(position.x + (SIZE - headHitboxSize) / 2, position.y + SIZE / 1.6f), headHitboxSize, headHitboxSize);
        rightArm = new CollisionBox(new Vector2(position.x + (SIZE - armHitBoxSize) / 2 + 0.2f, position.y + SIZE / 2.8f), armHitBoxSize, armHitBoxSize);
        leftArm = new CollisionBox(new Vector2(position.x + (SIZE - armHitBoxSize) / 2 - 0.2f, position.y + SIZE / 2.8f), armHitBoxSize, armHitBoxSize);
        rightFoot = new CollisionBox(new Vector2(position.x + (SIZE - legHitBoxSize) / 2 + 0.2f, position.y), legHitBoxSize, legHitBoxSize);
        leftFoot = new CollisionBox(new Vector2(position.x + (SIZE - legHitBoxSize) / 2 - 0.2f, position.y), legHitBoxSize, legHitBoxSize);
        body.add(head);
        body.add(rightArm);
        body.add(leftArm);
        body.add(rightFoot);
        body.add(leftFoot);*/
        head = new CollisionBox(position, 1f, 1f);
        body.add(head);

    }

    @Override
    public void update(float delta) {
        entityTime += delta;
        position.add(velocity.cpy().scl(delta));
        // UPDATE POSITION OF ALL VERTICES
        for (CollisionBox b : getBody()) {
            b.update(delta, velocity);
        }

       // head.getPosition().add(velocity.cpy().scl(delta));


    }

    @Override
    public void setHitboxes(Vector2 position) {
        head.setPosition(position.x + (SIZE - headHitboxSize) / 2, position.y + SIZE / 1.6f);
        rightArm.setPosition(position.x + (SIZE - armHitBoxSize) / 2 + 0.2f, position.y + SIZE / 2.8f);
        leftArm.setPosition(position.x + (SIZE - armHitBoxSize) / 2 - 0.2f, position.y + SIZE / 2.8f);
        rightFoot.setPosition(position.x + (SIZE - legHitBoxSize) / 2 + 0.2f, position.y);
        leftFoot.setPosition(position.x + (SIZE - legHitBoxSize) / 2 - 0.2f, position.y);
    }
}
