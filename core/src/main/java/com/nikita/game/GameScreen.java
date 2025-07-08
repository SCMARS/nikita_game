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
    private com.badlogic.gdx.graphics.glutils.ShapeRenderer shapeRenderer;
    private com.badlogic.gdx.graphics.Texture testTexture;

    public GameScreen() {
        this("Level1.tmx"); // Используем рабочую карту Level1
    }

    public GameScreen(String levelName) {
        this.levelName = levelName;
        Box2D.init();
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 20, 15); // Настройки для карты Level1
        batch = new SpriteBatch();

        // Инициализируем ShapeRenderer и тестовую текстуру
        shapeRenderer = new com.badlogic.gdx.graphics.glutils.ShapeRenderer();

        // Создаем простую белую текстуру для тестирования
        com.badlogic.gdx.graphics.Pixmap pixmap = new com.badlogic.gdx.graphics.Pixmap(100, 100, com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
        pixmap.setColor(com.badlogic.gdx.graphics.Color.WHITE);
        pixmap.fill();
        testTexture = new com.badlogic.gdx.graphics.Texture(pixmap);
        pixmap.dispose();
        try {
            System.out.println("=== ОТЛАДКА ЗАГРУЗКИ КАРТЫ ===");
            System.out.println("🍎 macOS Debug Info:");
            System.out.println("   OS: " + System.getProperty("os.name"));
            System.out.println("   Java: " + System.getProperty("java.version"));
            System.out.println("   OpenGL Vendor: " + Gdx.gl.glGetString(Gdx.gl.GL_VENDOR));
            System.out.println("   OpenGL Renderer: " + Gdx.gl.glGetString(Gdx.gl.GL_RENDERER));
            System.out.println("   OpenGL Version: " + Gdx.gl.glGetString(Gdx.gl.GL_VERSION));

            System.out.println("Загружаем карту: " + levelName);

            // Проверяем существование файла
            if (!Gdx.files.internal(levelName).exists()) {
                System.err.println("ОШИБКА: Файл карты не найден: " + levelName);
                throw new RuntimeException("Файл карты не найден: " + levelName);
            }
            System.out.println("✓ Файл карты найден");

            map = new TmxMapLoader().load(levelName);
            System.out.println("✓ Карта загружена успешно");

            // Информация о карте
            System.out.println("Размер карты: " + map.getProperties().get("width") + "x" + map.getProperties().get("height"));
            System.out.println("Размер тайла: " + map.getProperties().get("tilewidth") + "x" + map.getProperties().get("tileheight"));
            System.out.println("Количество слоев: " + map.getLayers().getCount());

            for (int i = 0; i < map.getLayers().getCount(); i++) {
                System.out.println("Слой " + i + ": " + map.getLayers().get(i).getName() + " (тип: " + map.getLayers().get(i).getClass().getSimpleName() + ")");
            }

            mapRenderer = new OrthogonalTiledMapRenderer(map, 1f); // Масштаб 1:1 для лучшего отображения
            System.out.println("✓ Рендерер карты создан");

        } catch (Exception e) {
            System.err.println("ОШИБКА при загрузке карты: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
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
        if (walls == null) {
            // Если слоя Walls нет, попробуем использовать первый слой
            walls = (com.badlogic.gdx.maps.tiled.TiledMapTileLayer) map.getLayers().get(0);
        }
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

    private boolean debugPrinted = false;

    @Override
    public void render(float delta) {
        world.step(delta, 6, 2);
        player.update(delta);
        for (Enemy e : enemies) e.update(delta, player.getPosition());
        // Зафиксируем камеру в центре карты Level1
        camera.position.set(10, 7.5f, 0); // Центр карты Level1
        camera.update();

        if (!debugPrinted) {
            System.out.println("=== ОТЛАДКА РЕНДЕРИНГА ===");
            System.out.println("Позиция камеры: " + camera.position);
            System.out.println("Размер viewport камеры: " + camera.viewportWidth + "x" + camera.viewportHeight);
            System.out.println("Позиция игрока: " + player.getPosition());
            System.out.println("Карта не null: " + (map != null));
            System.out.println("Рендерер не null: " + (mapRenderer != null));
            debugPrinted = true;
        }

        Gdx.gl.glClearColor(0.0f, 0.0f, 1.0f, 1); // СИНИЙ фон для теста
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Рендерим карту
        if (mapRenderer != null && map != null) {
            mapRenderer.setView(camera);
            mapRenderer.render();
        }

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        // Временно отключаем игрока для тестирования карты
        // player.render(batch);
        // for (Enemy e : enemies) e.render(batch);
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
        if (map != null) map.dispose();
        if (mapRenderer != null) mapRenderer.dispose();
        if (batch != null) batch.dispose();
        if (world != null) world.dispose();
        if (shapeRenderer != null) shapeRenderer.dispose();
        if (testTexture != null) testTexture.dispose();
    }
}
