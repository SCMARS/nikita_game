package com.nikita.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.physics.box2d.*;

public class SecretDoor extends InteractiveObject {
    private boolean open = false;
    private Texture closedSprite = new Texture("secret_door_closed.png");
    private Texture openSprite = new Texture("secret_door_open.png");
    private SoundManager soundManager;

    public SecretDoor(World world, float x, float y, SoundManager soundManager) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;
        bodyDef.position.set(x, y);
        body = world.createBody(bodyDef);
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(0.8f, 1.2f);
        body.createFixture(shape, 0);
        shape.dispose();
        this.soundManager = soundManager;
    }

    @Override
    public void update(float delta, Player player) {
        // Можно оставить только подсказку, если дверь еще не открыта
    }

    @Override
    public void render(SpriteBatch batch) {
        Texture sprite = open ? openSprite : closedSprite;
        batch.draw(sprite, body.getPosition().x-0.8f, body.getPosition().y-1.2f, 1.6f, 2.4f);
    }

    @Override
    public boolean isActive() { return !open; }

    public void open() {
        if (!open) {
            open = true;
            if (soundManager != null) soundManager.playSound("door_open.wav");
            if (body != null && body.getWorld() != null) body.getWorld().destroyBody(body);
        }
    }
} 