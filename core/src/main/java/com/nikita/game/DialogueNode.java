package com.nikita.game;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a single node in a dialogue tree.
 * Can be a text node with a speaker, a choice node with multiple options,
 * or an action node that triggers game events.
 */
public class DialogueNode {
    private String id;
    private String speaker;
    private String portrait;
    private String text;
    private String next;
    private String prompt;
    private List<DialogueChoice> choices;
    private String action;

    public DialogueNode(String id) {
        this.id = id;
        this.choices = new ArrayList<>();
    }

    // Text node constructor
    public DialogueNode(String id, String speaker, String portrait, String text, String next) {
        this(id);
        this.speaker = speaker;
        this.portrait = portrait;
        this.text = text;
        this.next = next;
    }

    // Choice node constructor
    public DialogueNode(String id, String prompt, List<DialogueChoice> choices) {
        this(id);
        this.prompt = prompt;
        this.choices = choices;
    }

    // Action node constructor
    public DialogueNode(String id, String action) {
        this(id);
        this.action = action;
    }

    public String getId() {
        return id;
    }

    public String getSpeaker() {
        return speaker;
    }

    public String getPortrait() {
        return portrait;
    }

    public String getText() {
        return text;
    }

    public String getNext() {
        return next;
    }

    public String getPrompt() {
        return prompt;
    }

    public List<DialogueChoice> getChoices() {
        return choices;
    }

    public String getAction() {
        return action;
    }

    public boolean isTextNode() {
        return text != null && speaker != null;
    }

    public boolean isChoiceNode() {
        return prompt != null && choices != null && !choices.isEmpty();
    }

    public boolean isActionNode() {
        return action != null;
    }

    public void addChoice(DialogueChoice choice) {
        if (choices == null) {
            choices = new ArrayList<>();
        }
        choices.add(choice);
    }
}
