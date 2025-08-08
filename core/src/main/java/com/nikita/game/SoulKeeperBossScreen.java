package com.nikita.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

/**
 * Screen for the Soul Keeper boss fight.
 */
public class SoulKeeperBossScreen implements Screen {
    private static final float WORLD_WIDTH = 32;
    private static final float WORLD_HEIGHT = 18;

    private NikitaGame game;
    private SpriteBatch batch;
    private OrthographicCamera camera;
    private Viewport viewport;
    private World world;
    private Box2DDebugRenderer debugRenderer;
    private ShapeRenderer shapeRenderer;

    private Texture background;
    private Texture groundTexture;
    private Player player;
    private SoulKeeperBoss boss;
    private DialogueManager dialogueManager;
    private SoundManager soundManager;

    private boolean bossDefeated = false;
    private boolean showingPhaseDialogue = false;
    private boolean showingDefeatDialogue = false;
    private float screenShakeTime = 0;
    private float screenShakeIntensity = 0;

    public SoulKeeperBossScreen(NikitaGame game, SoundManager soundManager) {
        this.game = game;
        this.soundManager = soundManager;

        // Initialize rendering
        batch = new SpriteBatch();
        camera = new OrthographicCamera();
        viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);
        debugRenderer = new Box2DDebugRenderer();
        shapeRenderer = new ShapeRenderer();

        // Initialize physics world
        world = new World(new Vector2(0, -9.8f), true);

        // Load textures (using existing assets)
        background = new Texture("start_menu_bg.png"); // Using menu background as arena background
        groundTexture = new Texture("tilesets/tileset.png"); // Using tileset for ground

        // Create player
        player = new Player(world, 5, 5);

        // Create boss
        boss = new SoulKeeperBoss(world, 25, 5, soundManager);

        // Set up boss defeat handler
        boss.setOnDefeatHandler(() -> {
            bossDefeated = true;
            showingDefeatDialogue = true;
            dialogueManager.addNode(boss.getDefeatDialogue());
            dialogueManager.startDialogue("defeat");

            // Play victory music (using try-catch to handle missing files)
            try {
                soundManager.playMusic("prologue_theme.mp3", false); // Use existing music as placeholder
            } catch (Exception e) {
                System.out.println("Music file not found: prologue_theme.mp3");
            }
        });

        // Initialize dialogue manager
        setupDialogueManager();

        // Play boss music (using try-catch to handle missing files)
        try {
            soundManager.playMusic("prologue_theme.mp3", true); // Use existing music as placeholder
        } catch (Exception e) {
            System.out.println("Music file not found: prologue_theme.mp3");
        }
    }

    private void setupDialogueManager() {
        dialogueManager = new DialogueManager();

        // Load portraits (using existing assets)
        dialogueManager.loadPortrait("SoulKeeper", "enemy.png");
        dialogueManager.loadPortrait("Gleb", "gleb.png");

        // Register action handler for granting crystal
        dialogueManager.registerActionHandler("grant_crystal_1", () -> {
            // Add crystal to player inventory
            ItemManager itemManager = new ItemManager(world);
            itemManager.addCrystal(player.getPosition().x, player.getPosition().y + 2);

            // Transition to next level
            game.setScreen(new GameScreen(game, "maps/swamp_level.tmx"));
        });

        // Add action node for granting crystal
        DialogueNode grantCrystalNode = new DialogueNode("grant_crystal", "grant_crystal_1");
        dialogueManager.addNode(grantCrystalNode);

        // Update defeat dialogue to link to the grant crystal action
        DialogueNode defeatDialogue = boss.getDefeatDialogue();
        // We need to create a new node with the same properties but different next value
        DialogueNode updatedDefeatDialogue = new DialogueNode("defeat", defeatDialogue.getSpeaker(),
                defeatDialogue.getPortrait(), defeatDialogue.getText(), "grant_crystal");
        dialogueManager.addNode(updatedDefeatDialogue);
    }

    @Override
    public void show() {
        // Called when this screen becomes the current screen
    }

    @Override
    public void render(float delta) {
        // Update
        update(delta);

        // Clear the screen
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Apply screen shake if active
        if (screenShakeTime > 0) {
            camera.position.x = WORLD_WIDTH / 2 + (float) Math.random() * screenShakeIntensity * 2 - screenShakeIntensity;
            camera.position.y = WORLD_HEIGHT / 2 + (float) Math.random() * screenShakeIntensity * 2 - screenShakeIntensity;
        } else {
            camera.position.x = WORLD_WIDTH / 2;
            camera.position.y = WORLD_HEIGHT / 2;
        }

        camera.update();
        batch.setProjectionMatrix(camera.combined);

        // Render scene
        batch.begin();

        // Draw background
        batch.draw(background, 0, 0, WORLD_WIDTH, WORLD_HEIGHT);

        // Draw ground
        batch.draw(groundTexture, 0, 0, WORLD_WIDTH, 2);

        // Draw player
        player.render(batch);

        // Draw boss
        boss.render(batch);

        // Draw health bars
        drawHealthBars();

        batch.end();

        // Debug rendering
        // debugRenderer.render(world, camera.combined);

        // Render dialogue
        batch.begin();
        dialogueManager.render(batch);
        batch.end();
    }

    private void update(float delta) {
        // Update dialogue
        dialogueManager.update();

        // Don't update game if dialogue is active
        if (dialogueManager.isActive()) {
            return;
        }

        // Check for phase change dialogue
        if (boss.getCurrentPhase() == 2 && !showingPhaseDialogue) {
            showingPhaseDialogue = true;
            dialogueManager.addNode(boss.getPhaseChangeDialogue());
            dialogueManager.startDialogue("phase_change");

            // Add screen shake effect
            addScreenShake(0.5f, 0.2f);

            return;
        }

        // Update physics world
        world.step(1/60f, 6, 2);

        // Update player
        player.update(delta);

        // Update boss
        boss.update(delta, player.getPosition());

        // Check for player-boss collision
        // In a real implementation, this would check for hitbox collisions
        // For simplicity, we'll just use distance
        float distToBoss = player.getPosition().dst(boss.getPosition());
        if (distToBoss < 2f && player.canAttackHit()) {
            boss.takeDamage(1);
            player.setAttackHit();
        }

        // Check for boss attacks hitting player
        // In a real implementation, this would check for hitbox collisions
        // For simplicity, we'll just use distance and random chance
        if (distToBoss < 3f && Math.random() < 0.01) {
            player.takeDamage(1);
        }

        // Update screen shake
        if (screenShakeTime > 0) {
            screenShakeTime -= delta;
        }
    }

    private void drawHealthBars() {
        // Draw boss health bar
        float bossHealthPercentage = (float) boss.getHealth() / boss.getMaxHealth();
        float bossHealthBarWidth = 10;
        float bossHealthBarHeight = 0.5f;
        float bossHealthBarX = WORLD_WIDTH - bossHealthBarWidth - 1;
        float bossHealthBarY = WORLD_HEIGHT - bossHealthBarHeight - 1;

        // Draw background
        batch.setColor(0.3f, 0.3f, 0.3f, 1);
        batch.draw(groundTexture, bossHealthBarX, bossHealthBarY, bossHealthBarWidth, bossHealthBarHeight);

        // Draw health
        batch.setColor(0.8f, 0.2f, 0.2f, 1);
        batch.draw(groundTexture, bossHealthBarX, bossHealthBarY, bossHealthBarWidth * bossHealthPercentage, bossHealthBarHeight);

        // Reset color
        batch.setColor(1, 1, 1, 1);

        // Draw player health as hearts
        for (int i = 0; i < player.getHealth(); i++) {
            // In a real implementation, this would draw heart icons
            batch.setColor(1, 0.2f, 0.2f, 1);
            batch.draw(groundTexture, 1 + i * 1.2f, WORLD_HEIGHT - 1.5f, 1, 1);
            batch.setColor(1, 1, 1, 1);
        }
    }

    private void addScreenShake(float duration, float intensity) {
        screenShakeTime = duration;
        screenShakeIntensity = intensity;
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
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
        world.dispose();
        debugRenderer.dispose();
        shapeRenderer.dispose();
        background.dispose();
        groundTexture.dispose();
        dialogueManager.dispose();
    }
}
