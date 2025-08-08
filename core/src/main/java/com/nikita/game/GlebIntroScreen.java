package com.nikita.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;

import java.util.ArrayList;
import java.util.List;

/**
 * Screen for the first meeting with Gleb and the subsequent boss fight with Soul Keeper.
 */
public class GlebIntroScreen implements Screen {
    private NikitaGame game;
    private SpriteBatch batch;
    private OrthographicCamera camera;
    private Texture background;
    private Texture glebTexture;
    private DialogueManager dialogueManager;
    private boolean dialogueCompleted = false;
    private SoundManager soundManager;

    public GlebIntroScreen(NikitaGame game) {
        this.game = game;
        this.batch = new SpriteBatch();
        this.camera = new OrthographicCamera();
        this.camera.setToOrtho(false, 1280, 720);
        this.background = new Texture("start_menu_bg.png"); // Using existing background
        this.glebTexture = new Texture("gleb.png");
        this.soundManager = new SoundManager();

        // Play ambient music (using try-catch to handle missing files)
        try {
            soundManager.playMusic("prologue_theme.mp3", true); // Use existing music as placeholder
        } catch (Exception e) {
            System.out.println("Music file not found: prologue_theme.mp3");
        }

        // Initialize dialogue manager
        setupDialogue();
    }

    private void setupDialogue() {
        dialogueManager = new DialogueManager();

        // Load portraits
        dialogueManager.loadPortrait("Gleb", "gleb.png");

        // Create dialogue nodes
        // Start node - Gleb's greeting
        DialogueNode startNode = new DialogueNode("start", "Gleb", "gleb.png",
                "Никита! Я ждал твоего побега из замка.", "q_trust");
        dialogueManager.addNode(startNode);

        // Question node - Why trust Gleb?
        List<DialogueChoice> choices = new ArrayList<>();
        choices.add(new DialogueChoice("Кто вы такой?", "gleb_intro"));
        choices.add(new DialogueChoice("Я справлюсь сама.", "gleb_warn"));
        DialogueNode questionNode = new DialogueNode("q_trust", "Почему я должна тебе доверять?", choices);
        dialogueManager.addNode(questionNode);

        // Gleb introduces himself
        DialogueNode glebIntroNode = new DialogueNode("gleb_intro", "Gleb", "gleb.png",
                "Я — маг Глеб, реформаций древней магии. Твой отец заключил сделку, от которой мы должны спастись.", "end");
        dialogueManager.addNode(glebIntroNode);

        // Gleb warns Nikita
        DialogueNode glebWarnNode = new DialogueNode("gleb_warn", "Gleb", "gleb.png",
                "Если останешься одна, лес поглотит тебя. Иди со мной, или твоя история закончится здесь.", "end");
        dialogueManager.addNode(glebWarnNode);

        // End node - Action to start boss encounter
        DialogueNode endNode = new DialogueNode("end", "start_boss_encounter");
        dialogueManager.addNode(endNode);

        // Register action handler for starting boss encounter
        dialogueManager.registerActionHandler("start_boss_encounter", () -> {
            dialogueCompleted = true;
            startBossFight();
        });

        // Start the dialogue
        dialogueManager.startDialogue("start");
    }

    private void startBossFight() {
        // Create and transition to the boss fight screen
        game.setScreen(new SoulKeeperBossScreen(game, soundManager));
    }

    @Override
    public void show() {
        // Called when this screen becomes the current screen
    }

    @Override
    public void render(float delta) {
        // Clear the screen
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Update camera
        camera.update();
        batch.setProjectionMatrix(camera.combined);

        // Update dialogue
        dialogueManager.update();

        // Render scene
        batch.begin();

        // Draw background
        batch.draw(background, 0, 0, 1280, 720);

        // Draw Gleb
        batch.draw(glebTexture, 640, 300, 200, 400);

        // Draw dialogue
        dialogueManager.render(batch);

        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        camera.setToOrtho(false, width, height);
    }

    @Override
    public void pause() {
        // Called when the game is paused
    }

    @Override
    public void resume() {
        // Called when the game is resumed
    }

    @Override
    public void hide() {
        // Called when this screen is no longer the current screen
    }

    @Override
    public void dispose() {
        batch.dispose();
        background.dispose();
        glebTexture.dispose();
        dialogueManager.dispose();
        soundManager.dispose();
    }
}
