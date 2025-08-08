package com.nikita.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages dialogue flow, handles user input for choices,
 * and triggers actions when dialogue nodes with actions are encountered.
 */
public class DialogueManager {
    private static final float DIALOGUE_BOX_HEIGHT = 0.3f; // 30% of screen height
    private static final float PORTRAIT_SIZE = 128f;
    private static final float PADDING = 20f;

    private ObjectMap<String, DialogueNode> nodes;
    private DialogueNode currentNode;
    private boolean active;
    private BitmapFont font;
    private BitmapFont speakerFont;
    private ObjectMap<String, Texture> portraits;
    private int selectedChoice;
    private Map<String, Runnable> actionHandlers;

    public DialogueManager() {
        nodes = new ObjectMap<>();
        portraits = new ObjectMap<>();
        actionHandlers = new HashMap<>();

        // Initialize fonts
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/pixel.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 16;
        parameter.color = Color.WHITE;
        font = generator.generateFont(parameter);

        parameter.size = 20;
        parameter.color = Color.YELLOW;
        speakerFont = generator.generateFont(parameter);

        generator.dispose();
    }

    /**
     * Adds a dialogue node to the manager.
     */
    public void addNode(DialogueNode node) {
        nodes.put(node.getId(), node);
    }

    /**
     * Loads a portrait texture for a speaker.
     */
    public void loadPortrait(String speakerName, String portraitPath) {
        if (!portraits.containsKey(speakerName)) {
            portraits.put(speakerName, new Texture(Gdx.files.internal(portraitPath)));
        }
    }

    /**
     * Registers an action handler that will be called when an action node is encountered.
     */
    public void registerActionHandler(String actionName, Runnable handler) {
        actionHandlers.put(actionName, handler);
    }

    /**
     * Starts a dialogue from the specified node ID.
     */
    public void startDialogue(String nodeId) {
        if (nodes.containsKey(nodeId)) {
            currentNode = nodes.get(nodeId);
            active = true;
            selectedChoice = 0;
        } else {
            System.err.println("Dialogue node not found: " + nodeId);
        }
    }

    /**
     * Updates the dialogue state based on user input.
     */
    public void update() {
        if (!active || currentNode == null) return;

        if (currentNode.isTextNode()) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.E) || Gdx.input.justTouched()) {
                if (currentNode.getNext() != null) {
                    currentNode = nodes.get(currentNode.getNext());
                } else {
                    active = false;
                }
            }
        } else if (currentNode.isChoiceNode()) {
            // Handle choice selection
            if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
                selectedChoice = Math.max(0, selectedChoice - 1);
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
                selectedChoice = Math.min(currentNode.getChoices().size() - 1, selectedChoice + 1);
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.E) || Gdx.input.justTouched()) {
                // Select the current choice
                DialogueChoice choice = currentNode.getChoices().get(selectedChoice);
                currentNode = nodes.get(choice.getNext());
            }
        } else if (currentNode.isActionNode()) {
            // Execute the action
            String actionName = currentNode.getAction();
            if (actionHandlers.containsKey(actionName)) {
                actionHandlers.get(actionName).run();
            } else {
                System.err.println("Action handler not found: " + actionName);
            }
            active = false;
        }
    }

    /**
     * Renders the dialogue UI.
     */
    public void render(SpriteBatch batch) {
        if (!active || currentNode == null) return;

        float screenWidth = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();
        float boxHeight = screenHeight * DIALOGUE_BOX_HEIGHT;
        float boxY = 0;

        // Draw dialogue box background
        // This would typically be a 9-patch or texture, but for simplicity we'll just describe it
        // In a real implementation, you would draw a proper dialogue box texture here

        if (currentNode.isTextNode()) {
            // Draw portrait if available
            if (currentNode.getPortrait() != null && portraits.containsKey(currentNode.getSpeaker())) {
                Texture portrait = portraits.get(currentNode.getSpeaker());
                batch.draw(portrait, PADDING, boxY + boxHeight - PORTRAIT_SIZE - PADDING, PORTRAIT_SIZE, PORTRAIT_SIZE);
            }

            // Draw speaker name
            if (currentNode.getSpeaker() != null) {
                speakerFont.draw(batch, currentNode.getSpeaker(), PADDING * 2 + PORTRAIT_SIZE, boxY + boxHeight - PADDING);
            }

            // Draw text
            font.draw(batch, currentNode.getText(), PADDING * 2 + PORTRAIT_SIZE, boxY + boxHeight - PADDING * 3);

            // Draw "press E to continue" prompt
            font.draw(batch, "Press E to continue", screenWidth - 200, boxY + PADDING);
        } else if (currentNode.isChoiceNode()) {
            // Draw prompt
            font.draw(batch, currentNode.getPrompt(), PADDING, boxY + boxHeight - PADDING);

            // Draw choices
            List<DialogueChoice> choices = currentNode.getChoices();
            for (int i = 0; i < choices.size(); i++) {
                String prefix = (i == selectedChoice) ? "> " : "  ";
                Color originalColor = font.getColor();
                if (i == selectedChoice) {
                    font.setColor(Color.YELLOW);
                }
                font.draw(batch, prefix + choices.get(i).getText(), PADDING, boxY + boxHeight - PADDING * 3 - i * 30);
                font.setColor(originalColor);
            }
        }
    }

    /**
     * Returns whether the dialogue is currently active.
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Disposes of resources.
     */
    public void dispose() {
        font.dispose();
        speakerFont.dispose();
        for (Texture texture : portraits.values()) {
            texture.dispose();
        }
    }
}
