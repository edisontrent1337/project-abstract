package com.trent.awesomejumper.models.lootable;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.trent.awesomejumper.controller.entitymanagement.EntityManager;
import com.trent.awesomejumper.engine.entity.Entity;
import com.trent.awesomejumper.engine.entity.LivingEntity;
import com.trent.awesomejumper.engine.modelcomponents.Body;
import com.trent.awesomejumper.engine.modelcomponents.Graphics;
import com.trent.awesomejumper.engine.modelcomponents.Health;
import com.trent.awesomejumper.engine.modelcomponents.ModelComponent;
import com.trent.awesomejumper.engine.physics.CollisionBox;
import static com.trent.awesomejumper.engine.modelcomponents.Body.BodyBuilder;
/**
 * Created by Sinthu on 10.12.2015.
 */
public class Lootable extends Entity implements LivingEntity {


    private final int SPRITE_SIZE = 24;

    private final float WIDTH = 0.5625f;               //  32 pixel
    private final float HEIGHT = 13f/32f;            //  24 Pixel
    private final float MASS = 1.5f;
    private final float FRICTION = 0.895f;
    private final float ELASTICITY = 0.2f;// 0.45f;
    private final float SPRITE_WIDTH = 0.5625f;           // 18/32 px width
    private final float SPRITE_HEIGHT = 0.75f;          // 24/32 px height
    private final float MAX_SPEED = 25f;
    //private final Vector2 OFFSET = new Vector2(0.125f, 0); // 4 px space in chest sprite
    private final Vector2 OFFSET = new Vector2(0f, 0); // 4 px space in chest sprite
    Vector2 vel = new Vector2(0f, 0f);
    private final float FRAME_DURATION = 0.066f;

    JsonValue test = new JsonReader().parse(Gdx.files.internal("data/entities/lootable.json"));
    Array<ModelComponent> modelComponents = new Array<>();

    public Lootable(Vector2 position) {

        body = new BodyBuilder(this)
                .position(position)
                .width(WIDTH)
                .height(HEIGHT)
                .density(0.9f)
                .mass(MASS)
                .friction(FRICTION)
                .elasticity(ELASTICITY)
                .maxVelocity(MAX_SPEED)
                .offset(OFFSET)
                .bodyBuild();
        body.getBounds().setDamageCoefficient(0.85f);
        graphics = new Graphics(this, 0f,"wood_chest", SPRITE_WIDTH,SPRITE_HEIGHT);
        health = new Health(this, 1000);
        CollisionBox box = new CollisionBox(position.cpy(), WIDTH, HEIGHT);
        box.setOffset(OFFSET);
        box.setDamageCoefficient(0.5f);
        body.add(box);
        state = State.IDLE;
        type = Type.REGULAR_ENTITY;
        setOwner(this);
    }

    @Override
    public void update(float delta) {
        super.update(delta);

        if(body.isCollidedWithWorld())
            vel.x = -vel.x;
        getVelocity().add(vel);

    }

    @Override
    public void render(SpriteBatch spriteBatch) {
        super.render(spriteBatch);
    }


    @Override
    public void register() {
        EntityManager.getInstance().registerEntity(this);
    }
}
