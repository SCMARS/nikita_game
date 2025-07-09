package com.nikita.game;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.nikita.game.items.Crystal;
import com.nikita.game.items.GameKey;
import com.nikita.game.items.HealthPotion;
import com.nikita.game.items.Item;

public class ItemManager {
    private World world;
    private Array<Item> items;

    public ItemManager(World world) {
        this.world = world;
        this.items = new Array<>();
    }

    public void update(float delta, Player player) {
        for (Item item : items) {
            item.update(delta, player);
        }
    }

    public void render(SpriteBatch batch) {
        for (Item item : items) {
            item.render(batch);
        }
    }

    public void addCrystal(float x, float y) {
        items.add(new Crystal(world, x, y));
    }

    public void addHealthPotion(float x, float y) {
        items.add(new HealthPotion(world, x, y));
    }

    public void addKey(float x, float y) {
        items.add(new GameKey(world, x, y));
    }

    public int getItemCount() {
        return items.size;
    }

    public void dispose() {
        for (Item item : items) {
            item.dispose();
        }
        items.clear();
    }
}
