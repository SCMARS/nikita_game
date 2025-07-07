package com.nikita.game;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

public class DialogueSystem {
    private String[] lines;
    private int currentLine = 0;
    private boolean active = false;
    private BitmapFont font;

    public DialogueSystem() {
        font = new BitmapFont();
        font.setColor(Color.WHITE);
    }

    public void startDialogue(String[] lines) {
        this.lines = lines;
        currentLine = 0;
        active = true;
    }

    public void update() {
        if (active && Gdx.input.isKeyJustPressed(Input.Keys.E)) {
            currentLine++;
            if (currentLine >= lines.length) {
                active = false;
            }
        }
    }

    public void render(SpriteBatch batch) {
        if (active && lines != null && currentLine < lines.length) {
            font.draw(batch, lines[currentLine], 50, 100);
        }
    }

    public boolean isActive() {
        return active;
    }
} 