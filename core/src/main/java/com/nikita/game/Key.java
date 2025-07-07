package com.nikita.game;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.physics.box2d.*;

public class Key extends InteractiveObject {
    private boolean picked = false;
    private Texture sprite = new Texture("key.png");
    private SoundManager soundManager;

    public Key(World world, float x, float y, SoundManager soundManager) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;
        bodyDef.position.set(x, y);
        body = world.createBody(bodyDef);
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(0.3f, 0.3f);
        body.createFixture(shape, 0);
        shape.dispose();
        this.soundManager = soundManager;
    }

    @Override
    public void update(float delta, Player player) {
        if (!picked && player.getPosition().dst(body.getPosition()) < 0.8f) {
            picked = true;
            player.addKey();
            if (soundManager != null) soundManager.playSound("key_pickup.wav");
        }
    }

    @Override
    public void render(SpriteBatch batch) {
        if (!picked)
            batch.draw(sprite, body.getPosition().x-0.3f, body.getPosition().y-0.3f, 0.6f, 0.6f);
    }

    @Override
    public boolean isActive() { return !picked; }
} 