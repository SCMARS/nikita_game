package com.nikita.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.physics.box2d.Box2D;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.Preferences;

public class GameScreen implements Screen {
    private OrthographicCamera camera;
    private SpriteBatch batch;
    private TiledMap map;
    private OrthogonalTiledMapRenderer mapRenderer;
    private World world;
    private Player player;
    private Array<Enemy> enemies;
    private String levelName;

    public GameScreen() {
        this("Level1.tmx");
    }

    public GameScreen(String levelName) {
        this.levelName = levelName;
        Box2D.init();
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 20, 11.25f); // 1280x720 / 64 (масштаб)
        batch = new SpriteBatch();
        map = new TmxMapLoader().load(levelName);
        mapRenderer = new OrthogonalTiledMapRenderer(map, 1f);
        world = new World(new Vector2(0, -12f), true);
        player = new Player(world, 2, 2);
        enemies = new Array<>();
        enemies.add(new Enemy(world, 8, 2, 7, 12));
        enemies.add(new Enemy(world, 15, 2, 14, 18));
        // TODO: генерация коллизий из карты
        // Сохраняем прогресс
        Preferences prefs = Gdx.app.getPreferences("save");
        prefs.putString("level", levelName);
        prefs.flush();
    }

    @Override
    public void show() {}

    @Override
    public void render(float delta) {
        world.step(delta, 6, 2);
        player.update(delta);
        for (Enemy e : enemies) e.update(delta, player.getPosition());
        camera.position.set(player.getPosition().x, player.getPosition().y, 0);
        camera.update();
        Gdx.gl.glClearColor(0.1f, 0.05f, 0.1f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        mapRenderer.setView(camera);
        mapRenderer.render();
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        player.render(batch);
        for (Enemy e : enemies) e.render(batch);
        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        camera.viewportWidth = 20f;
        camera.viewportHeight = 20f * height / (float)width;
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
        map.dispose();
        mapRenderer.dispose();
        batch.dispose();
        world.dispose();
    }
} 