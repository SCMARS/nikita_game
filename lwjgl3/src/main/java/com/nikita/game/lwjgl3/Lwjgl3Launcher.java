package com.nikita.game.lwjgl3;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.nikita.game.NikitaGame;

/** Launches the desktop (LWJGL3) application. */
public class Lwjgl3Launcher {
    public static void main(String[] args) {
        if (StartupHelper.startNewJvmIfRequired()) return; // This handles macOS support and helps on Windows.
        createApplication();
    }

    private static Lwjgl3Application createApplication() {
        return new Lwjgl3Application(new NikitaGame(), getDefaultConfiguration());
    }

    private static Lwjgl3ApplicationConfiguration getDefaultConfiguration() {
        Lwjgl3ApplicationConfiguration configuration = new Lwjgl3ApplicationConfiguration();
        configuration.setTitle("NikitaEscape");

        // Enhanced macOS compatibility settings
        String osName = System.getProperty("os.name").toLowerCase();
        boolean isMac = osName.contains("mac");

        if (isMac) {
            System.out.println("🍎 Detected macOS - applying compatibility settings");
            // More conservative settings for macOS
            configuration.useVsync(false); // Disable VSync on macOS to avoid issues
            configuration.setForegroundFPS(60); // Fixed 60 FPS for stability
            configuration.setWindowedMode(1280, 720); // Start in windowed mode
            configuration.setResizable(true);
        } else {
            //// Vsync limits the frames per second to what your hardware can display, and helps eliminate
            //// screen tearing. This setting doesn't always work on Linux, so the line after is a safeguard.
            configuration.useVsync(true);
            //// Limits FPS to the refresh rate of the currently active monitor, plus 1 to try to match fractional
            //// refresh rates. The Vsync setting above should limit the actual FPS to match the monitor.
            configuration.setForegroundFPS(Lwjgl3ApplicationConfiguration.getDisplayMode().refreshRate + 1);
            configuration.setFullscreenMode(Lwjgl3ApplicationConfiguration.getDisplayMode());
        }

        //// If you remove the above line and set Vsync to false, you can get unlimited FPS, which can be
        //// useful for testing performance, but can also be very stressful to some hardware.
        //// You may also need to configure GPU drivers to fully disable Vsync; this can cause screen tearing.

        //// You can change these files; they are in lwjgl3/src/main/resources/ .
        //// They can also be loaded from the root of assets/ .
        configuration.setWindowIcon("libgdx128.png", "libgdx64.png", "libgdx32.png", "libgdx16.png");

        // Additional debugging for macOS
        if (isMac) {
            System.out.println("🔧 Configuration applied:");
            System.out.println("   - VSync: " + (isMac ? "disabled" : "enabled"));
            System.out.println("   - FPS: " + (isMac ? "60" : "auto"));
            System.out.println("   - Window mode: " + (isMac ? "windowed 1280x720" : "fullscreen"));
        }

        return configuration;
    }
}
