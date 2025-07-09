package com.nikita.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
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
    private NikitaGame game;
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

    // Поля для диалога выхода
    private boolean showExitConfirm = false;
    private int exitSelected = 0; // 0 - Да, 1 - Нет
    private BitmapFont exitFont, exitTitleFont;
    private GlyphLayout exitLayout = new GlyphLayout();
    private ShapeRenderer exitShapeRenderer;

    public GameScreen() {
        this(null, "maps/level_0.tmx"); // Рабочая карта 20x15 с настройками
    }

    public GameScreen(String levelName) {
        this(null, levelName);
    }

    public GameScreen(NikitaGame game, String levelName) {
        this.game = game;
        this.levelName = levelName;
        Box2D.init();
        camera = new OrthographicCamera();
        // Настройка камеры для карты 20x15 тайлов с отступами
        camera.setToOrtho(false, 22, 17); // Немного больше карты для отступов
        batch = new SpriteBatch();

        // Инициализируем ShapeRenderer и тестовую текстуру
        shapeRenderer = new com.badlogic.gdx.graphics.glutils.ShapeRenderer();
        exitShapeRenderer = new ShapeRenderer();

        // Инициализируем шрифты для диалога выхода
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("Roboto-Regular.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 32;
        parameter.characters = FreeTypeFontGenerator.DEFAULT_CHARS + "АБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯабвгдеёжзийклмнопрстуфхцчшщъыьэюя";
        exitFont = generator.generateFont(parameter);
        parameter.size = 40;
        parameter.color = Color.WHITE;
        exitTitleFont = generator.generateFont(parameter);
        generator.dispose();

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

            // Анализируем тайлы в центре карты для отладки (только первые 5 тайлов)
            com.badlogic.gdx.maps.tiled.TiledMapTileLayer groundLayer = (com.badlogic.gdx.maps.tiled.TiledMapTileLayer) map.getLayers().get(0);
            if (groundLayer != null) {
                System.out.println("🔍 Анализ тайлов в центре карты:");
                int count = 0;
                for (int x = 45; x <= 55 && count < 5; x++) {
                    for (int y = 8; y <= 12 && count < 5; y++) {
                        com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell cell = groundLayer.getCell(x, y);
                        if (cell != null && cell.getTile() != null) {
                            System.out.println("   Тайл (" + x + "," + y + "): ID " + cell.getTile().getId());
                            count++;
                        }
                    }
                }
            }

            mapRenderer = new OrthogonalTiledMapRenderer(map, 1f/32f); // Масштаб для тайлов 32x32
            System.out.println("✓ Рендерер карты создан");

        } catch (Exception e) {
            System.err.println("ОШИБКА при загрузке карты: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
        world = new World(new Vector2(0, 0f), true); // Убираем гравитацию для свободного движения

        // Устанавливаем игрока на координаты (13, 10)
        float playerStartX = 13f; // X = 13
        float playerStartY = 10f; // Y = 10

        System.out.println("🎮 Игрок спавнится на позиции: (" + playerStartX + ", " + playerStartY + ")");
        player = new Player(world, playerStartX, playerStartY);
        enemies = new Array<>();

        // Размещаем врагов в разных частях карты 20x15
        enemies.add(new Enemy(world, 5, 7, 3, 8));   // Левая часть
        enemies.add(new Enemy(world, 15, 7, 13, 17)); // Правая часть
        enemies.add(new Enemy(world, 10, 3, 8, 12));  // Центр внизу
        createCollisionBodiesFromMap();
        loadGame(); // Автоматическая загрузка прогресса
        saveGame(); // Автоматическое сохранение при старте уровня
    }

    private void createCollisionBodiesFromMap() {
        int tileWidth = map.getProperties().get("tilewidth", Integer.class);
        int tileHeight = map.getProperties().get("tileheight", Integer.class);
        int width = map.getProperties().get("width", Integer.class);
        int height = map.getProperties().get("height", Integer.class);

        // Ищем слой для коллизий - пробуем разные варианты названий
        com.badlogic.gdx.maps.tiled.TiledMapTileLayer layer = (com.badlogic.gdx.maps.tiled.TiledMapTileLayer) map.getLayers().get("Walls");
        if (layer == null) {
            layer = (com.badlogic.gdx.maps.tiled.TiledMapTileLayer) map.getLayers().get("Ground");
            System.out.println("⚠️ Слой Walls не найден, пробуем Ground");
        }
        if (layer == null) {
            layer = (com.badlogic.gdx.maps.tiled.TiledMapTileLayer) map.getLayers().get("Прошарок плиток 1");
            System.out.println("⚠️ Слой Ground не найден, пробуем 'Прошарок плиток 1'");
        }
        if (layer == null) {
            // Берём первый доступный слой
            if (map.getLayers().getCount() > 0) {
                layer = (com.badlogic.gdx.maps.tiled.TiledMapTileLayer) map.getLayers().get(0);
                System.out.println("⚠️ Используем первый доступный слой: " + layer.getName());
            }
        }
        if (layer == null) {
            System.out.println("❌ Не найден ни один слой для коллизий!");
            return;
        }

        System.out.println("🔧 Начинаем создание коллизий для карты " + width + "x" + height);
        System.out.println("🔧 Используем комбинированную логику: границы + стены посередине");

        // ID тайлов, которые должны быть стенами (создаем проход сверху)
        int[] wallTileIds = {
            // Границы карты с проходом сверху (убираем 2 тайла из середины верхней границы для прохода)
            200, 201, 202, 203, 204, 205, 206, 207, 208, 209, /* 210, 211, */ 212, 213, 214, 215, 216, 217, 218, 219, // верх с проходом
            543, 603, 663, 723, 2823, 2883, 2943, 3003, // левая граница
            1138, 1198, 1258, 1378, 1438, 1018, 1887, 2000, // правая граница
            987, 988, 989, 990, 991, 992, 993, 994, 995, 996, 997, 998, 2698, // низ
            // Стены посередине карты
            2132, 850
        };

        int collisionCount = 0;
        int totalTiles = 0;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell cell = layer.getCell(x, y);
                if (cell != null && cell.getTile() != null) {
                    totalTiles++;
                    int tileId = cell.getTile().getId();

                    // Проверяем, является ли этот тайл стеной
                    boolean isWall = false;
                    for (int wallId : wallTileIds) {
                        if (tileId == wallId) {
                            isWall = true;
                            break;
                        }
                    }

                    if (isWall) {
                        // Проверяем, не находится ли коллизия рядом с местом спавна игрока (двери)
                        float playerStartX = 13f;
                        float playerStartY = 10f;

                        // Убираем коллизии в области дверей (около места спавна игрока)
                        boolean nearPlayerSpawn = (Math.abs(x - playerStartX) <= 2 && Math.abs(y - playerStartY) <= 2);

                        if (!nearPlayerSpawn) {
                            System.out.println("🔧 Создаём стену ID " + tileId + " на позиции (" + x + "," + y + ")");
                            BodyDef bodyDef = new BodyDef();
                            bodyDef.type = BodyDef.BodyType.StaticBody;
                            // Возвращаем коллизии в нормальное положение
                            bodyDef.position.set(x + 0.5f, y + 0.5f);
                            Body body = world.createBody(bodyDef);
                            PolygonShape shape = new PolygonShape();
                            shape.setAsBox(0.5f, 0.5f);
                            body.createFixture(shape, 0);
                            shape.dispose();
                            collisionCount++;
                        } else {
                            System.out.println("🚪 Пропускаем коллизию в области дверей на позиции (" + x + "," + y + ")");
                        }
                    }
                }
            }
        }
        System.out.println("🔧 Всего тайлов в слое: " + totalTiles);
        System.out.println("✓ Создано " + collisionCount + " коллизионных тел");
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

        // Зафиксируем камеру в центре карты 20x15 тайлов
        Vector2 playerPos = player.getPosition();

        // Проверяем переход на новый уровень (правый нижний угол карты 20x15)
        if (playerPos.x > 17f && playerPos.y < 3f) { // Правый нижний угол для карты 20x15
            System.out.println("🎯 Переход на новый уровень!");
            // Переходим на следующий уровень
            if (game != null) {
                game.setScreen(new GameScreen(game, "maps/level_2.tmx")); // Переход на уровень 2
            }
        }

        camera.position.set(10f, 7.5f, 0); // Центр карты в единицах тайлов
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

        Gdx.gl.glClearColor(0.1f, 0.1f, 0.2f, 1); // Темно-синий фон для игры
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Рендерим карту
        if (mapRenderer != null && map != null) {
            mapRenderer.setView(camera);
            mapRenderer.render();
        }

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        // Включаем игрока для проверки рендеринга
        player.render(batch);
        for (Enemy e : enemies) e.render(batch);
        batch.end();

        // Рендеринг диалога выхода
        if (showExitConfirm) {
            renderExitDialog();
        }

        // Обработка диалога выхода
        if (showExitConfirm) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT) || Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) {
                exitSelected = 1 - exitSelected;
            }
            if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
                if (exitSelected == 0) { // Да
                    if (game != null) {
                        game.setScreen(new FirstScreen(game));
                    }
                } else { // Нет
                    showExitConfirm = false;
                }
            }
            if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
                showExitConfirm = false;
            }
            return; // Не обрабатываем другие действия пока диалог открыт
        }

        // Обработка клавиши Escape для показа диалога выхода
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            showExitConfirm = true;
            exitSelected = 0;
        }
    }

    @Override
    public void resize(int width, int height) {
        // Простое и надежное масштабирование для полноэкранного режима
        float mapWidth = 20f;  // Ширина карты в тайлах
        float mapHeight = 15f; // Высота карты в тайлах

        // Вычисляем соотношения сторон
        float screenAspectRatio = (float) width / height;
        float mapAspectRatio = mapWidth / mapHeight; // 20/15 = 1.33

        // Простая логика: всегда показываем всю карту с небольшими отступами
        float padding = 1.5f; // Отступ вокруг карты

        if (screenAspectRatio > mapAspectRatio) {
            // Экран шире карты - подгоняем по высоте
            camera.viewportHeight = mapHeight + padding;
            camera.viewportWidth = camera.viewportHeight * screenAspectRatio;
        } else {
            // Экран уже карты - подгоняем по ширине
            camera.viewportWidth = mapWidth + padding;
            camera.viewportHeight = camera.viewportWidth / screenAspectRatio;
        }

        // Убеждаемся, что камера всегда в центре карты
        camera.position.set(mapWidth / 2f, mapHeight / 2f, 0);
        camera.update();

        System.out.println("🔧 RESIZE DEBUG:");
        System.out.println("   Screen: " + width + "x" + height + " (aspect: " + String.format("%.2f", screenAspectRatio) + ")");
        System.out.println("   Map: " + mapWidth + "x" + mapHeight + " (aspect: " + String.format("%.2f", mapAspectRatio) + ")");
        System.out.println("   Viewport: " + String.format("%.1f", camera.viewportWidth) + "x" + String.format("%.1f", camera.viewportHeight));
        System.out.println("   Camera pos: " + String.format("%.1f", camera.position.x) + "," + String.format("%.1f", camera.position.y));
    }

    @Override
    public void pause() {}
    @Override
    public void resume() {}
    @Override
    public void hide() {}
    // Рендеринг диалога выхода
    private void renderExitDialog() {
        int winW = Gdx.graphics.getWidth();
        int winH = Gdx.graphics.getHeight();

        // Создаем камеру для UI
        OrthographicCamera uiCamera = new OrthographicCamera();
        uiCamera.setToOrtho(false, winW, winH);
        uiCamera.update();

        String msg = "Вы уверены, что хотите выйти в главное меню?";
        exitLayout.setText(exitTitleFont, msg);
        float boxW = Math.max(exitLayout.width + 80, 400); // минимальная ширина
        float boxH = 180;
        float boxX = (winW - boxW) / 2f;
        float boxY = (winH - boxH) / 2f;

        // Настраиваем проекцию для UI
        exitShapeRenderer.setProjectionMatrix(uiCamera.combined);

        // Полупрозрачный фон
        exitShapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        exitShapeRenderer.setColor(0, 0, 0, 0.7f);
        exitShapeRenderer.rect(0, 0, winW, winH);

        // Фон диалога
        exitShapeRenderer.setColor(0.2f, 0.2f, 0.2f, 0.95f);
        exitShapeRenderer.rect(boxX, boxY, boxW, boxH);

        // Рамка диалога
        exitShapeRenderer.setColor(0.6f, 0.6f, 0.6f, 1f);
        exitShapeRenderer.rect(boxX - 2, boxY - 2, boxW + 4, 2); // верх
        exitShapeRenderer.rect(boxX - 2, boxY + boxH, boxW + 4, 2); // низ
        exitShapeRenderer.rect(boxX - 2, boxY, 2, boxH); // лево
        exitShapeRenderer.rect(boxX + boxW, boxY, 2, boxH); // право
        exitShapeRenderer.end();

        // Настраиваем проекцию для текста
        batch.setProjectionMatrix(uiCamera.combined);
        batch.begin();

        // Текст диалога
        exitTitleFont.setColor(Color.WHITE);
        exitTitleFont.draw(batch, msg, boxX + (boxW - exitLayout.width) / 2f, boxY + boxH - 40);

        // Кнопки
        String yes = "Да";
        String no = "Нет";
        exitLayout.setText(exitFont, yes);
        float yesX = boxX + 60;
        float btnY = boxY + 60;
        exitLayout.setText(exitFont, no);
        float noX = boxX + boxW - 60 - exitLayout.width;

        // Выделение выбранной кнопки
        if (exitSelected == 0) {
            exitFont.setColor(Color.GOLD);
            exitFont.draw(batch, yes, yesX, btnY);
            exitFont.setColor(Color.LIGHT_GRAY);
            exitFont.draw(batch, no, noX, btnY);
        } else {
            exitFont.setColor(Color.LIGHT_GRAY);
            exitFont.draw(batch, yes, yesX, btnY);
            exitFont.setColor(Color.GOLD);
            exitFont.draw(batch, no, noX, btnY);
        }
        batch.end();
    }

    @Override
    public void dispose() {
        if (map != null) map.dispose();
        if (mapRenderer != null) mapRenderer.dispose();
        if (batch != null) batch.dispose();
        if (world != null) world.dispose();
        if (shapeRenderer != null) shapeRenderer.dispose();
        if (testTexture != null) testTexture.dispose();
        if (exitFont != null) exitFont.dispose();
        if (exitTitleFont != null) exitTitleFont.dispose();
        if (exitShapeRenderer != null) exitShapeRenderer.dispose();
    }
}
