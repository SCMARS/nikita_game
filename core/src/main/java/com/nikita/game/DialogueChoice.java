package com.nikita.game;

/**
 * Represents a dialogue choice option that the player can select.
 * Each choice has text to display and the ID of the next dialogue node to navigate to.
 */
public class DialogueChoice {
    private String text;
    private String next;

    public DialogueChoice(String text, String next) {
        this.text = text;
        this.next = next;
    }

    public String getText() {
        return text;
    }

    public String getNext() {
        return next;
    }
}
