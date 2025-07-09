package com.nikita.game;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

public class Enemy {
    public Body body;
    private Sprite sprite;
    private float patrolMinX, patrolMaxX;
    private boolean movingRight = true;
    private float speed = 2f;
    private boolean chasing = false;
    private int health = 2; // Здоровье врага
    private boolean isDead = false;

    public Enemy(World world, float x, float y, float patrolMinX, float patrolMaxX) {
        this.patrolMinX = patrolMinX;
        this.patrolMaxX = patrolMaxX;
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(x, y);
        body = world.createBody(bodyDef);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(0.4f, 0.9f);
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = 1f;
        fixtureDef.friction = 0.2f;
        body.createFixture(fixtureDef);
        shape.dispose();

        sprite = new Sprite(new Texture("enemy.png"));
        sprite.setSize(0.8f, 1.8f);
        sprite.setOriginCenter();
    }

    public void update(float delta, Vector2 playerPos) {
        float distToPlayer = playerPos.dst(body.getPosition());
        if (distToPlayer < 4f) {
            chasing = true;
        } else if (distToPlayer > 6f) {
            chasing = false;
        }
        if (chasing) {
            float dir = Math.signum(playerPos.x - body.getPosition().x);
            body.setLinearVelocity(dir * speed, body.getLinearVelocity().y);
        } else {
            // Патрулирование
            if (movingRight) {
                body.setLinearVelocity(speed, body.getLinearVelocity().y);
                if (body.getPosition().x > patrolMaxX) movingRight = false;
            } else {
                body.setLinearVelocity(-speed, body.getLinearVelocity().y);
                if (body.getPosition().x < patrolMinX) movingRight = true;
            }
        }
    }

    public void render(SpriteBatch batch) {
        sprite.setPosition(body.getPosition().x - sprite.getWidth()/2, body.getPosition().y - sprite.getHeight()/2);
        sprite.draw(batch);
    }

    public Vector2 getPosition() {
        return body.getPosition();
    }

    public void takeDamage(int damage) {
        if (!isDead) {
            health -= damage;
            if (health <= 0) {
                isDead = true;
                System.out.println("💀 Враг убит!");
            }
        }
    }

    public boolean isDead() {
        return isDead;
    }

    public int getHealth() {
        return health;
    }
}
