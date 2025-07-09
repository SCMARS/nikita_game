package com.nikita.game.items;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.nikita.game.Player;

public class Crystal extends Item {
    private Texture texture;

    public Crystal(World world, float x, float y) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;
        bodyDef.position.set(x, y);
        body = world.createBody(bodyDef);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(0.3f, 0.3f);
        body.createFixture(shape, 0);
        shape.dispose();

        try {
            texture = new Texture("items/crystal.png");
        } catch (Exception e) {
            System.err.println("Не удалось загрузить текстуру кристалла");
            texture = null;
        }
    }

    @Override
    public void update(float delta, Player player) {
        if (collected) return;

        Vector2 playerPos = player.getPosition();
        Vector2 itemPos = body.getPosition();

        if (playerPos.dst(itemPos) < 0.8f) {
            player.addSeal();
            collect();
            System.out.println("💎 Собран кристалл!");
        }
    }

    @Override
    public void render(SpriteBatch batch) {
        if (collected || texture == null) return;

        Vector2 pos = body.getPosition();
        batch.draw(texture, pos.x - 0.3f, pos.y - 0.3f, 0.6f, 0.6f);
    }

    @Override
    public void dispose() {
        if (texture != null) texture.dispose();
    }
}
