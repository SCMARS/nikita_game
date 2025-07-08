package com.nikita.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Box2D;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

public class Player {
    public Body body;
    private Sprite sprite;
    private boolean canJump = false;
    private int health = 3;
    private int maxHealth = 3;
    private int keys = 0;
    private int seals = 0;

    public Player(World world, float x, float y) {
        // Создание физического тела
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(x, y);
        body = world.createBody(bodyDef);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(0.4f, 0.9f); // Размеры игрока (в метрах)
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = 1f;
        fixtureDef.friction = 0.2f;
        body.createFixture(fixtureDef);
        shape.dispose();

        // Загрузка спрайта
        sprite = new Sprite(new Texture("player.png"));
        sprite.setSize(0.8f, 1.8f);
        sprite.setOriginCenter();
    }

    public void update(float delta) {
        // Управление
        float move = 0;
        if (Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT)) move = -1;
        if (Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT)) move = 1;
        body.setLinearVelocity(move * 3f, body.getLinearVelocity().y);

        // Прыжок
        if ((Gdx.input.isKeyJustPressed(Input.Keys.SPACE) || Gdx.input.isKeyJustPressed(Input.Keys.W)) && canJump) {
            body.applyLinearImpulse(new Vector2(0, 6f), body.getWorldCenter(), true);
            canJump = false;
        }
    }

    public void render(SpriteBatch batch) {
        sprite.setPosition(body.getPosition().x - sprite.getWidth()/2, body.getPosition().y - sprite.getHeight()/2);
        sprite.draw(batch);
    }

    public void setCanJump(boolean value) {
        canJump = value;
    }

    public Vector2 getPosition() {
        return body.getPosition();
    }

    public void heal(int amount) {
        health = Math.min(health + amount, maxHealth);
    }

    public void addKey() {
        keys++;
    }

    public void addSeal() {
        seals++;
    }

    public int getHealth() { return health; }

    public int getKeys() { return keys; }

    public int getSeals() { return seals; }

    public void setHealth(int value) {
        this.health = Math.max(0, Math.min(value, maxHealth));
    }
    public void setKeys(int value) {
        this.keys = Math.max(0, value);
    }
    public void setSeals(int value) {
        this.seals = Math.max(0, value);
    }
} 