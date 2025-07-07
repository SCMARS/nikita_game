package com.nikita.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.physics.box2d.*;

public class Note extends InteractiveObject {
    private boolean read = false;
    private Texture sprite = new Texture("note.png");
    private String[] text;
    private DialogueSystem dialogueSystem;

    public Note(World world, float x, float y, String[] text, DialogueSystem dialogueSystem) {
        this.text = text;
        this.dialogueSystem = dialogueSystem;
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;
        bodyDef.position.set(x, y);
        body = world.createBody(bodyDef);
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(0.3f, 0.3f);
        body.createFixture(shape, 0);
        shape.dispose();
    }

    @Override
    public void update(float delta, Player player) {
        if (!read && player.getPosition().dst(body.getPosition()) < 0.8f && Gdx.input.isKeyJustPressed(Input.Keys.E)) {
            read = true;
            dialogueSystem.startDialogue(text);
        }
    }

    @Override
    public void render(SpriteBatch batch) {
        if (!read)
            batch.draw(sprite, body.getPosition().x-0.3f, body.getPosition().y-0.3f, 0.6f, 0.6f);
    }

    @Override
    public boolean isActive() { return !read; }
} 