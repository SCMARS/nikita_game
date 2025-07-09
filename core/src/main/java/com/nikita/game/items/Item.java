package com.nikita.game.items;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.physics.box2d.Body;
import com.nikita.game.Player;

public abstract class Item {
    protected Body body;
    protected boolean collected = false;
    
    public abstract void update(float delta, Player player);
    public abstract void render(SpriteBatch batch);
    public abstract void dispose();
    
    public boolean isCollected() {
        return collected;
    }
    
    protected void collect() {
        collected = true;
    }
}