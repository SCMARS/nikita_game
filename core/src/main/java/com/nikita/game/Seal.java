package com.nikita.game;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.physics.box2d.*;

public class Seal extends InteractiveObject {
    private boolean collected = false;
    private Texture sprite = new Texture("seal.png");
    private SoundManager soundManager;

    public Seal(World world, float x, float y, SoundManager soundManager) {
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
        if (!collected && player.getPosition().dst(body.getPosition()) < 0.8f) {
            collected = true;
            player.addSeal();
            if (soundManager != null) soundManager.playSound("seal_pickup.wav");
        }
    }

    @Override
    public void render(SpriteBatch batch) {
        if (!collected)
            batch.draw(sprite, body.getPosition().x-0.3f, body.getPosition().y-0.3f, 0.6f, 0.6f);
    }

    @Override
    public boolean isActive() { return !collected; }
} 