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
import com.badlogic.gdx.utils.Array;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Soul Keeper boss with multiple phases and special attacks.
 */
public class SoulKeeperBoss {
    public Body body;
    private Sprite sprite;
    private float speed = 3f;
    private int health = 100;
    private int maxHealth = 100;
    private boolean isDead = false;

    // Boss-specific properties
    private int currentPhase = 1;
    private int totalPhases = 2;
    private Map<Integer, Integer> phaseHealthThresholds;
    private Map<Integer, List<String>> phaseActions;
    private Map<String, Runnable> actionHandlers;
    private float actionTimer = 0;
    private String currentAction;
    private boolean isDefeated = false;
    private Runnable onDefeatHandler;
    private DialogueNode phaseChangeDialogue;
    private DialogueNode defeatDialogue;
    private SoundManager soundManager;

    // Phantom summon properties
    private List<Enemy> phantoms;
    private float phantomTimer = 0;
    private static final float PHANTOM_SUMMON_INTERVAL = 10f; // 10 seconds

    /**
     * Creates the Soul Keeper boss.
     */
    public SoulKeeperBoss(World world, float x, float y, SoundManager soundManager) {
        this.soundManager = soundManager;

        // Create physics body
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(x, y);
        body = world.createBody(bodyDef);

        // Create collision shape (larger than regular enemies)
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(0.8f, 1.8f);
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = 1f;
        fixtureDef.friction = 0.2f;
        body.createFixture(fixtureDef);
        shape.dispose();

        // Create sprite (using existing enemy.png asset)
        sprite = new Sprite(new Texture("enemy.png"));
        sprite.setSize(1.6f, 3.6f);
        sprite.setOriginCenter();

        // Initialize boss properties
        phaseHealthThresholds = new HashMap<>();
        phaseHealthThresholds.put(2, 50); // Phase 2 starts at 50% health

        phaseActions = new HashMap<>();
        List<String> phase1Actions = new ArrayList<>();
        phase1Actions.add("summon_phantom");
        phase1Actions.add("tentacle_swipe");
        phaseActions.put(1, phase1Actions);

        List<String> phase2Actions = new ArrayList<>();
        phase2Actions.add("rapid_tentacles");
        phase2Actions.add("ground_pound");
        phaseActions.put(2, phase2Actions);

        actionHandlers = new HashMap<>();
        phantoms = new ArrayList<>();

        // Set up action handlers
        setupActionHandlers(world);

        // Set up dialogues
        setupDialogues();
    }

    /**
     * Sets up the action handlers for the boss's special attacks.
     */
    private void setupActionHandlers(World world) {
        // Summon phantom action
        actionHandlers.put("summon_phantom", () -> {
            System.out.println("Soul Keeper summons a phantom!");
            Vector2 position = body.getPosition();
            float offsetX = (float) (Math.random() * 4 - 2); // Random offset between -2 and 2
            Enemy phantom = new Enemy(world, position.x + offsetX, position.y + 1,
                    position.x - 5, position.x + 5);
            phantoms.add(phantom);

            // Play sound effect (using try-catch to handle missing files)
            try {
                soundManager.playSound("prologue_theme.mp3"); // Use existing sound as placeholder
            } catch (Exception e) {
                System.out.println("Sound file not found: prologue_theme.mp3");
            }
        });

        // Tentacle swipe action
        actionHandlers.put("tentacle_swipe", () -> {
            System.out.println("Soul Keeper performs a tentacle swipe!");
            // In a real implementation, this would create a hitbox for the attack

            // Play sound effect (using try-catch to handle missing files)
            try {
                soundManager.playSound("prologue_theme.mp3"); // Use existing sound as placeholder
            } catch (Exception e) {
                System.out.println("Sound file not found: prologue_theme.mp3");
            }
        });

        // Rapid tentacles action (phase 2)
        actionHandlers.put("rapid_tentacles", () -> {
            System.out.println("Soul Keeper unleashes rapid tentacle attacks!");
            // In a real implementation, this would create multiple hitboxes for the attack

            // Play sound effect (using try-catch to handle missing files)
            try {
                soundManager.playSound("prologue_theme.mp3"); // Use existing sound as placeholder
            } catch (Exception e) {
                System.out.println("Sound file not found: prologue_theme.mp3");
            }
        });

        // Ground pound action (phase 2)
        actionHandlers.put("ground_pound", () -> {
            System.out.println("Soul Keeper performs a ground pound!");
            // In a real implementation, this would create a shockwave effect

            // Play sound effect (using try-catch to handle missing files)
            try {
                soundManager.playSound("prologue_theme.mp3"); // Use existing sound as placeholder
            } catch (Exception e) {
                System.out.println("Sound file not found: prologue_theme.mp3");
            }

            // Screen shake effect would be handled by the boss screen
        });
    }

    /**
     * Sets up the dialogues for phase change and defeat.
     */
    private void setupDialogues() {
        // Phase change dialogue (using enemy.png for SoulKeeper portrait)
        phaseChangeDialogue = new DialogueNode("phase_change", "SoulKeeper", "enemy.png",
                "Ты нарушила покой моих подопечных!", null);

        // Defeat dialogue (using gleb.png for Gleb portrait)
        defeatDialogue = new DialogueNode("defeat", "Gleb", "gleb.png",
                "Первая душа свободна... И первый кристалл наш.", null);
    }

    /**
     * Updates the boss's state.
     */
    public void update(float delta, Vector2 playerPos) {
        if (isDead) return;

        // Check for phase transitions
        if (currentPhase == 1 && health <= phaseHealthThresholds.get(2)) {
            transitionToPhase(2);
        }

        // Update phantom summon timer in phase 1
        if (currentPhase == 1) {
            phantomTimer -= delta;
            if (phantomTimer <= 0) {
                if (actionHandlers.containsKey("summon_phantom")) {
                    actionHandlers.get("summon_phantom").run();
                }
                phantomTimer = PHANTOM_SUMMON_INTERVAL;
            }
        }

        // Update action timer
        actionTimer -= delta;
        if (actionTimer <= 0) {
            // Choose a random action from the current phase
            List<String> actions = phaseActions.get(currentPhase);
            if (actions != null && !actions.isEmpty()) {
                int actionIndex = (int)(Math.random() * actions.size());
                currentAction = actions.get(actionIndex);

                // Execute the action
                if (actionHandlers.containsKey(currentAction)) {
                    actionHandlers.get(currentAction).run();
                }

                // Reset timer (random between 3-5 seconds)
                actionTimer = 3 + (float)(Math.random() * 2);

                // Shorter timer in phase 2 for more aggressive attacks
                if (currentPhase == 2) {
                    actionTimer *= 0.7f;
                }
            }
        }

        // Basic movement - move towards player
        float distToPlayer = playerPos.dst(body.getPosition());
        if (distToPlayer < 10f) { // Larger detection range than regular enemies
            float dir = Math.signum(playerPos.x - body.getPosition().x);
            body.setLinearVelocity(dir * speed, body.getLinearVelocity().y);
        }

        // Update phantoms
        for (int i = phantoms.size() - 1; i >= 0; i--) {
            Enemy phantom = phantoms.get(i);
            phantom.update(delta, playerPos);

            // Remove dead phantoms
            if (phantom.isDead()) {
                phantoms.remove(i);
            }
        }
    }

    /**
     * Renders the boss and its phantoms.
     */
    public void render(SpriteBatch batch) {
        // Render phantoms
        for (Enemy phantom : phantoms) {
            phantom.render(batch);
        }

        // Render boss
        sprite.setPosition(body.getPosition().x - sprite.getWidth()/2, body.getPosition().y - sprite.getHeight()/2);
        sprite.draw(batch);
    }

    /**
     * Transitions to a new phase.
     */
    private void transitionToPhase(int phase) {
        currentPhase = phase;
        System.out.println("Soul Keeper transitioning to phase " + phase);

        // In phase 2, increase speed
        if (phase == 2) {
            speed = 4.5f;
        }
    }

    /**
     * Inflicts damage on the boss.
     */
    public void takeDamage(int damage) {
        if (!isDead) {
            health -= damage;
            if (health <= 0) {
                isDead = true;
                isDefeated = true;
                System.out.println("Soul Keeper defeated!");

                // Call the defeat handler if available
                if (onDefeatHandler != null) {
                    onDefeatHandler.run();
                }
            }
        }
    }

    /**
     * Returns the boss's position.
     */
    public Vector2 getPosition() {
        return body.getPosition();
    }

    /**
     * Returns whether the boss is dead.
     */
    public boolean isDead() {
        return isDead;
    }

    /**
     * Returns the boss's current health.
     */
    public int getHealth() {
        return health;
    }

    /**
     * Returns the boss's maximum health.
     */
    public int getMaxHealth() {
        return maxHealth;
    }

    /**
     * Returns whether the boss has been defeated.
     */
    public boolean isDefeated() {
        return isDefeated;
    }

    /**
     * Returns the boss's current phase.
     */
    public int getCurrentPhase() {
        return currentPhase;
    }

    /**
     * Gets the dialogue for phase change.
     */
    public DialogueNode getPhaseChangeDialogue() {
        return phaseChangeDialogue;
    }

    /**
     * Gets the dialogue to display when the boss is defeated.
     */
    public DialogueNode getDefeatDialogue() {
        return defeatDialogue;
    }

    /**
     * Sets the handler to call when the boss is defeated.
     */
    public void setOnDefeatHandler(Runnable handler) {
        this.onDefeatHandler = handler;
    }

    /**
     * Returns the list of active phantoms.
     */
    public List<Enemy> getPhantoms() {
        return phantoms;
    }
}
