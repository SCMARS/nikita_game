package com.nikita.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.physics.box2d.*;

public class Altar extends InteractiveObject {
    private boolean activated = false;
    private Texture sprite = new Texture("altar.png");
    private SecretDoor secretDoor;
    private SoundManager soundManager;
    private DialogueSystem dialogueSystem;

    public Altar(World world, float x, float y, SecretDoor secretDoor, SoundManager soundManager, DialogueSystem dialogueSystem) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;
        bodyDef.position.set(x, y);
        body = world.createBody(bodyDef);
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(0.8f, 0.4f);
        body.createFixture(shape, 0);
        shape.dispose();
        this.secretDoor = secretDoor;
        this.soundManager = soundManager;
        this.dialogueSystem = dialogueSystem;
    }

    @Override
    public void update(float delta, Player player) {
        if (!activated && player.getPosition().dst(body.getPosition()) < 1.2f && Gdx.input.isKeyJustPressed(Input.Keys.E)) {
            activated = true;
            if (secretDoor != null) secretDoor.open();
            if (soundManager != null) soundManager.playSound("altar_activate.wav");
            if (dialogueSystem != null) dialogueSystem.startDialogue(new String[]{"Никита: Печать ритуала... Я должна идти дальше."});
        }
    }

    @Override
    public void render(SpriteBatch batch) {
        batch.draw(sprite, body.getPosition().x-0.8f, body.getPosition().y-0.4f, 1.6f, 0.8f);
    }

    @Override
    public boolean isActive() { return !activated; }
} 