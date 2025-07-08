package com.nikita.game;

import com.badlogic.gdx.Game;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class NikitaGame extends Game {
    @Override
    public void create() {
        // Check for test mode via system property
        String testMode = System.getProperty("nikita.test.mode");

        if ("simple".equals(testMode)) {
            System.out.println("🧪 Starting in simple test mode");
            setScreen(new SimpleTestScreen());
        } else {
            System.out.println("🎮 Starting normal game");
            setScreen(new FirstScreen(this));
        }
    }
}
