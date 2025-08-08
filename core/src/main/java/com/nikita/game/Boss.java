package com.nikita.game;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;

import java.util.HashMap;
import java.util.Map;

/**
 * Base class for boss enemies with multiple phases and special attacks.
 */
public class Boss extends Enemy {
    protected int maxHealth;
    protected int currentPhase = 1;
    protected int totalPhases;
    protected Map<Integer, Integer> phaseHealthThresholds;
    protected Map<Integer, Array<String>> phaseActions;
    protected Map<Integer, DialogueNode> phaseDialogues;
    protected DialogueNode defeatDialogue;
    protected float actionTimer = 0;
    protected String currentAction;
    protected Map<String, Runnable> actionHandlers;
    protected boolean isDefeated = false;
    protected Runnable onDefeatHandler;

    /**
     * Creates a boss with the specified parameters.
     */
    public Boss(World world, float x, float y, int maxHealth, int totalPhases) {
        super(world, x, y, x - 10, x + 10); // Default patrol range
        this.maxHealth = maxHealth;
        // Note: We can't directly set health as it's private in Enemy
        // We'll override takeDamage to handle our custom health logic
        this.totalPhases = totalPhases;
        this.phaseHealthThresholds = new HashMap<>();
        this.phaseActions = new HashMap<>();
        this.phaseDialogues = new HashMap<>();
        this.actionHandlers = new HashMap<>();

        // We can't directly modify the sprite as it's private in Enemy
        // In a real implementation, we would need to modify Enemy to allow this
        // or create a custom rendering method for Boss
    }

    /**
     * Sets the health threshold for a phase transition.
     */
    public void setPhaseHealthThreshold(int phase, int healthThreshold) {
        phaseHealthThresholds.put(phase, healthThreshold);
    }

    /**
     * Adds an action to a phase.
     */
    public void addPhaseAction(int phase, String action) {
        if (!phaseActions.containsKey(phase)) {
            phaseActions.put(phase, new Array<>());
        }
        phaseActions.get(phase).add(action);
    }

    /**
     * Sets the dialogue to display when transitioning to a phase.
     */
    public void setPhaseDialogue(int phase, DialogueNode dialogue) {
        phaseDialogues.put(phase, dialogue);
    }

    /**
     * Sets the dialogue to display when the boss is defeated.
     */
    public void setDefeatDialogue(DialogueNode dialogue) {
        this.defeatDialogue = dialogue;
    }

    /**
     * Registers an action handler.
     */
    public void registerActionHandler(String actionName, Runnable handler) {
        actionHandlers.put(actionName, handler);
    }

    /**
     * Sets the handler to call when the boss is defeated.
     */
    public void setOnDefeatHandler(Runnable handler) {
        this.onDefeatHandler = handler;
    }

    /**
     * Gets the dialogue for the specified phase.
     */
    public DialogueNode getPhaseDialogue(int phase) {
        return phaseDialogues.get(phase);
    }

    /**
     * Gets the dialogue to display when the boss is defeated.
     */
    public DialogueNode getDefeatDialogue() {
        return defeatDialogue;
    }

    @Override
    public void update(float delta, Vector2 playerPos) {
        if (isDead()) return;

        // Check for phase transitions
        for (int phase = 2; phase <= totalPhases; phase++) {
            if (currentPhase < phase && getHealth() <= phaseHealthThresholds.get(phase)) {
                transitionToPhase(phase);
            }
        }

        // Update action timer
        actionTimer -= delta;
        if (actionTimer <= 0) {
            // Choose a random action from the current phase
            Array<String> actions = phaseActions.get(currentPhase);
            if (actions != null && actions.size > 0) {
                int actionIndex = (int)(Math.random() * actions.size);
                currentAction = actions.get(actionIndex);

                // Execute the action
                if (actionHandlers.containsKey(currentAction)) {
                    actionHandlers.get(currentAction).run();
                }

                // Reset timer (random between 3-5 seconds)
                actionTimer = 3 + (float)(Math.random() * 2);
            }
        }

        // Basic movement
        super.update(delta, playerPos);
    }

    /**
     * Transitions to a new phase.
     */
    protected void transitionToPhase(int phase) {
        currentPhase = phase;
        System.out.println("Boss transitioning to phase " + phase);

        // Trigger phase dialogue if available
        if (phaseDialogues.containsKey(phase)) {
            // This would be handled by the boss screen
        }
    }

    @Override
    public void takeDamage(int damage) {
        if (!isDead()) {
            // Call the parent method to handle health reduction and isDead setting
            super.takeDamage(damage);

            // Check if the boss is now dead after taking damage
            if (isDead()) {
                isDefeated = true;
                System.out.println("Boss defeated!");

                // Call the defeat handler if available
                if (onDefeatHandler != null) {
                    onDefeatHandler.run();
                }
            }
        }
    }

    /**
     * Returns whether the boss has been defeated.
     */
    public boolean isDefeated() {
        return isDefeated;
    }

    /**
     * Returns the current phase of the boss.
     */
    public int getCurrentPhase() {
        return currentPhase;
    }
}
