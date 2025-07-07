package com.nikita.game;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.physics.box2d.Body;

public abstract class InteractiveObject {
    protected Body body;
    public abstract void update(float delta, Player player);
    public abstract void render(SpriteBatch batch);
    public abstract boolean isActive();
} 