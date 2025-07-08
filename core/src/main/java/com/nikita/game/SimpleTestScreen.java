package com.nikita.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.Color;

/**
 * Simple test screen for macOS debugging
 * This screen tests basic rendering without complex map loading
 */
public class SimpleTestScreen implements Screen {
    
    private OrthographicCamera camera;
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private Texture testTexture;
    
    @Override
    public void show() {
        System.out.println("🧪 SimpleTestScreen: Initializing...");
        
        // Print system info
        System.out.println("🍎 System Information:");
        System.out.println("   OS: " + System.getProperty("os.name"));
        System.out.println("   Java: " + System.getProperty("java.version"));
        System.out.println("   Architecture: " + System.getProperty("os.arch"));
        
        try {
            System.out.println("   OpenGL Vendor: " + Gdx.gl.glGetString(Gdx.gl.GL_VENDOR));
            System.out.println("   OpenGL Renderer: " + Gdx.gl.glGetString(Gdx.gl.GL_RENDERER));
            System.out.println("   OpenGL Version: " + Gdx.gl.glGetString(Gdx.gl.GL_VERSION));
        } catch (Exception e) {
            System.err.println("   OpenGL Info Error: " + e.getMessage());
        }
        
        // Initialize camera
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 800, 600);
        
        // Initialize renderers
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        
        // Create a simple test texture
        try {
            com.badlogic.gdx.graphics.Pixmap pixmap = new com.badlogic.gdx.graphics.Pixmap(64, 64, com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
            pixmap.setColor(Color.WHITE);
            pixmap.fill();
            pixmap.setColor(Color.RED);
            pixmap.drawRectangle(0, 0, 64, 64);
            testTexture = new Texture(pixmap);
            pixmap.dispose();
            System.out.println("✓ Test texture created");
        } catch (Exception e) {
            System.err.println("✗ Failed to create test texture: " + e.getMessage());
        }
        
        System.out.println("✓ SimpleTestScreen initialized successfully");
    }
    
    @Override
    public void render(float delta) {
        // Clear screen with green background
        Gdx.gl.glClearColor(0.0f, 1.0f, 0.0f, 1.0f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        
        camera.update();
        
        // Test 1: Shape rendering
        try {
            shapeRenderer.setProjectionMatrix(camera.combined);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            
            // Draw colored rectangles
            shapeRenderer.setColor(Color.RED);
            shapeRenderer.rect(100, 100, 100, 100);
            
            shapeRenderer.setColor(Color.BLUE);
            shapeRenderer.rect(250, 100, 100, 100);
            
            shapeRenderer.setColor(Color.YELLOW);
            shapeRenderer.rect(400, 100, 100, 100);
            
            shapeRenderer.end();
        } catch (Exception e) {
            System.err.println("Shape rendering error: " + e.getMessage());
        }
        
        // Test 2: Sprite rendering
        if (testTexture != null) {
            try {
                batch.setProjectionMatrix(camera.combined);
                batch.begin();
                
                // Draw test textures
                batch.draw(testTexture, 100, 300);
                batch.draw(testTexture, 250, 300);
                batch.draw(testTexture, 400, 300);
                
                batch.end();
            } catch (Exception e) {
                System.err.println("Sprite rendering error: " + e.getMessage());
            }
        }
        
        // Print success message once
        if (Gdx.graphics.getFrameId() == 60) { // After 1 second at 60fps
            System.out.println("🎉 Rendering test successful!");
            System.out.println("   You should see:");
            System.out.println("   - Green background");
            System.out.println("   - 3 colored rectangles (red, blue, yellow)");
            System.out.println("   - 3 white squares with red borders");
        }
    }
    
    @Override
    public void resize(int width, int height) {
        camera.viewportWidth = width;
        camera.viewportHeight = height;
        camera.update();
    }
    
    @Override
    public void pause() {}
    
    @Override
    public void resume() {}
    
    @Override
    public void hide() {}
    
    @Override
    public void dispose() {
        if (batch != null) batch.dispose();
        if (shapeRenderer != null) shapeRenderer.dispose();
        if (testTexture != null) testTexture.dispose();
        System.out.println("🧹 SimpleTestScreen disposed");
    }
}
