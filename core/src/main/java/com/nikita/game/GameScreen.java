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
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.PolygonShape;

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
        createCollisionBodiesFromMap();
        loadGame(); // Автоматическая загрузка прогресса
        saveGame(); // Автоматическое сохранение при старте уровня
    }

    private void createCollisionBodiesFromMap() {
        
        int tileWidth = map.getProperties().get("tilewidth", Integer.class);
        int tileHeight = map.getProperties().get("tileheight", Integer.class);
        int width = map.getProperties().get("width", Integer.class);
        int height = map.getProperties().get("height", Integer.class);
        com.badlogic.gdx.maps.tiled.TiledMapTileLayer walls = (com.badlogic.gdx.maps.tiled.TiledMapTileLayer) map.getLayers().get("Walls");
        if (walls == null) return;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell cell = walls.getCell(x, y);
                if (cell != null && cell.getTile() != null) {
                    BodyDef bodyDef = new BodyDef();
                    bodyDef.type = BodyDef.BodyType.StaticBody;
                    // Центр тайла (Box2D: 1 юнит = 1 тайл)
                    bodyDef.position.set(x + 0.5f, y + 0.5f);
                    Body body = world.createBody(bodyDef);
                    PolygonShape shape = new PolygonShape();
                    shape.setAsBox(0.5f, 0.5f); // Полтайла в каждую сторону
                    body.createFixture(shape, 0);
                    shape.dispose();
                }
            }
        }
    }

    private void saveGame() {
        Preferences prefs = Gdx.app.getPreferences("save");
        prefs.putString("level", levelName);
        prefs.putFloat("player_x", player.getPosition().x);
        prefs.putFloat("player_y", player.getPosition().y);
        prefs.putInteger("player_health", player.getHealth());
        prefs.putInteger("player_keys", player.getKeys());
        prefs.putInteger("player_seals", player.getSeals());
        prefs.flush();
    }

    private void loadGame() {
        Preferences prefs = Gdx.app.getPreferences("save");
        if (prefs.contains("level") && prefs.getString("level").equals(levelName)) {
            float x = prefs.getFloat("player_x", 2f);
            float y = prefs.getFloat("player_y", 2f);
            player.body.setTransform(x, y, 0);
            player.setHealth(prefs.getInteger("player_health", 3));
            player.setKeys(prefs.getInteger("player_keys", 0));
            player.setSeals(prefs.getInteger("player_seals", 0));
        }
    }

    // Пример вызова автосохранения в конце уровня
    public void endLevel() {
        saveGame();
        // Здесь можно добавить переход на следующий уровень или экран победы
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