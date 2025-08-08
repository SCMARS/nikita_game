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

    // Переменные для системы переключения между частями уровня
    private int currentLevelPart = 1; // Текущая часть уровня
    private int totalLevelParts = 3;  // Общее количество частей уровня
    private Array<Vector2> levelPartTransitions; // Точки перехода между частями уровня
    
    // Защита от зацикливания переходов
    private float lastTransitionTime = 0f;
    private final float TRANSITION_COOLDOWN = 2.0f; // Задержка между переходами в секундах
    private boolean transitionInProgress = false;
    private float gameTime = 0f; // Накопленное время игры
    
    // Переменные для перехода на следующий уровень
    private boolean levelCompleted = false;
    private float levelCompleteTimer = 0f;
    private final float LEVEL_COMPLETE_DELAY = 2f; // Задержка перед переходом на следующий уровень
    private String nextLevelName = "maps/level_1.tmx"; // Следующий уровень
    
    // Переменные для коллизий
    private Array<Body> collisionBodies;
    private boolean collisionEnabled = true;

    // Поля для диалога выхода
    private boolean showExitConfirm = false;
    private int exitSelected = 0; // 0 - Да, 1 - Нет
    private BitmapFont exitFont, exitTitleFont;
    private GlyphLayout exitLayout = new GlyphLayout();
    private ShapeRenderer exitShapeRenderer;

    public GameScreen() {
        this(null, "maps/test_small.tmx"); // Временно используем рабочую карту для отладки
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
        player = new Player(world, 2, 2);
        enemies = new Array<>();
        collisionBodies = new Array<>(); // Инициализируем массив коллизионных тел

        // Инициализация точек перехода между частями уровня
        levelPartTransitions = new Array<>();
        levelPartTransitions.add(new Vector2(17, 7.5f)); // Переход из части 1 в часть 2 (правый край карты)
        levelPartTransitions.add(new Vector2(3, 7.5f));  // Переход из части 2 в часть 1 (левый край карты)
        levelPartTransitions.add(new Vector2(10, 13));   // Переход из части 1/2 в часть 3 (верхний край карты)
        levelPartTransitions.add(new Vector2(10, 2));    // Переход из части 3 в часть 1 (нижний край карты)
        
        // Добавляем специальную точку перехода на следующий уровень
        levelPartTransitions.add(new Vector2(10, 12));   // Точка перехода на следующий уровень (более доступная)

        // Инициализация врагов для первой части уровня
        initLevelPart(currentLevelPart);

        createCollisionBodiesFromMap();
        loadGame(); // Автоматическая загрузка прогресса
        saveGame(); // Автоматическое сохранение при старте уровня
    }

    private void createCollisionBodiesFromMap() {
        // Очищаем старые коллизионные тела
        for (Body body : collisionBodies) {
            if (body != null) {
                world.destroyBody(body);
            }
        }
        collisionBodies.clear();
        
        int tileWidth = map.getProperties().get("tilewidth", Integer.class);
        int tileHeight = map.getProperties().get("tileheight", Integer.class);
        int width = map.getProperties().get("width", Integer.class);
        int height = map.getProperties().get("height", Integer.class);

        // Ищем слой Walls, если нет - используем Ground, но с умной логикой
        com.badlogic.gdx.maps.tiled.TiledMapTileLayer layer = (com.badlogic.gdx.maps.tiled.TiledMapTileLayer) map.getLayers().get("Walls");
        if (layer == null) {
            layer = (com.badlogic.gdx.maps.tiled.TiledMapTileLayer) map.getLayers().get("Ground");
            System.out.println("⚠️ Слой Walls не найден, используем Ground с фильтрацией тайлов");
        }
        if (layer == null) return;

        // ID тайлов, которые должны быть стенами (непроходимыми)
        // Уменьшаем список стен - оставляем только явные препятствия
        int[] wallTileIds = {15, 16, 17, 18, 19, 20}; // Только основные стены

        int collisionCount = 0;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell cell = layer.getCell(x, y);
                if (cell != null && cell.getTile() != null) {
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
                        BodyDef bodyDef = new BodyDef();
                        bodyDef.type = BodyDef.BodyType.StaticBody;
                        // Центр тайла (Box2D: 1 юнит = 1 тайл)
                        bodyDef.position.set(x + 0.5f, y + 0.5f);
                        Body body = world.createBody(bodyDef);
                        PolygonShape shape = new PolygonShape();
                        shape.setAsBox(0.4f, 0.4f); // Уменьшаем размер коллизии
                        body.createFixture(shape, 0);
                        shape.dispose();
                        collisionBodies.add(body); // Добавляем в массив для отслеживания
                        collisionCount++;
                    }
                }
            }
        }
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
            player.setHealth(prefs.getInteger("player_health", 4));
            player.setKeys(prefs.getInteger("player_keys", 0));
            player.setSeals(prefs.getInteger("player_seals", 0));
        }
    }

    // Пример вызова автосохранения в конце уровня
    public void endLevel() {
        saveGame();
        // Здесь можно добавить переход на следующий уровень или экран победы
    }
    
    /**
     * Проверяет, завершен ли уровень и инициирует переход на следующий
     */
    private void checkLevelCompletion() {
        if (levelCompleted) {
            levelCompleteTimer += Gdx.graphics.getDeltaTime();
            System.out.println("⏰ Таймер завершения уровня: " + levelCompleteTimer + "/" + LEVEL_COMPLETE_DELAY);
            
            if (levelCompleteTimer >= LEVEL_COMPLETE_DELAY) {
                // Переход на следующий уровень
                System.out.println("🚀 Выполняем переход на следующий уровень!");
                goToNextLevel();
            }
        }
    }
    
    /**
     * Отмечает уровень как завершенный
     */
    public void completeLevel() {
        if (!levelCompleted) {
            levelCompleted = true;
            levelCompleteTimer = 0f;
            System.out.println("🎯 Уровень отмечен как завершенный!");
            
            // Дополнительное сообщение для первого уровня
            if (levelName != null && (levelName.contains("level_0") || levelName.contains("test_small"))) {
                System.out.println("🎬 Подготовка к катсцене завершения первого уровня...");
            } else {
                System.out.println("🎉 Переход на следующий уровень...");
            }
        }
    }
    
    /**
     * Определяет следующий уровень на основе текущего
     */
    private String getNextLevelName() {
        if (levelName == null) return "maps/level_0.tmx";
        
        // Определяем следующий уровень на основе текущего
        if (levelName.contains("level_0") || levelName.contains("test_small")) {
            return "maps/level_2.tmx";
        } else if (levelName.contains("level_2")) {
            return "maps/level_true_fixed.tmx";
        } else if (levelName.contains("level_true") || levelName.contains("level_true_fixed")) {
            // Последний уровень - переход на экран победы или главное меню
            return "victory";
        } else {
            // По умолчанию возвращаем первый уровень
            return "maps/level_0.tmx";
        }
    }
    
    /**
     * Переходит на следующий уровень
     */
    private void goToNextLevel() {
        String nextLevel = getNextLevelName();
        
        if (nextLevel.equals("victory")) {
            // Переход на экран победы
            System.out.println("🎉 Победа! Игра завершена!");
            if (game != null) {
                game.setScreen(new FirstScreen(game));
            }
        } else {
            // Проверяем, нужно ли показать катсцену для первого уровня
            if (levelName != null && (levelName.contains("level_0") || levelName.contains("test_small"))) {
                System.out.println("🎬 Запуск катсцены завершения первого уровня!");
                if (game != null) {
                    game.setScreen(new Level1EndCutscene(game));
                }
            } else {
                // Переход на следующий уровень
                System.out.println("🎉 Переход на следующий уровень: " + nextLevel);
                if (game != null) {
                    game.setScreen(new GameScreen(game, nextLevel));
                }
            }
        }
    }

    /**
     * Инициализирует врагов для указанной части уровня
     * @param levelPart номер части уровня (1, 2, 3)
     */
    private void initLevelPart(int levelPart) {
        // Очищаем список врагов
        for (Enemy enemy : enemies) {
            if (enemy.body != null) {
                world.destroyBody(enemy.body);
            }
        }
        enemies.clear();

        // Добавляем врагов в зависимости от части уровня
        switch (levelPart) {
            case 1:
                // Враги для первой части уровня
                enemies.add(new Enemy(world, 8, 2, 7, 12));
                enemies.add(new Enemy(world, 15, 2, 14, 18));
                break;
            case 2:
                // Враги для второй части уровня
                enemies.add(new Enemy(world, 5, 5, 3, 8));
                enemies.add(new Enemy(world, 12, 8, 10, 15));
                enemies.add(new Enemy(world, 18, 3, 16, 19));
                break;
            case 3:
                // Враги для третьей части уровня (босс)
                enemies.add(new Enemy(world, 10, 7, 8, 12));
                enemies.add(new Enemy(world, 5, 10, 3, 7));
                enemies.add(new Enemy(world, 15, 10, 13, 17));
                break;
        }

        System.out.println("🔄 Инициализирована часть уровня " + levelPart + " с " + enemies.size + " врагами");
    }

    /**
     * Переключает на указанную часть уровня
     * @param newLevelPart номер новой части уровня
     */
    private void switchLevelPart(int newLevelPart) {
        if (newLevelPart < 1 || newLevelPart > totalLevelParts) {
            System.out.println("⚠️ Попытка перехода на несуществующую часть уровня: " + newLevelPart);
            return;
        }

        if (newLevelPart == currentLevelPart) {
            return; // Уже находимся в этой части
        }

        // Проверяем задержку между переходами
        if (gameTime - lastTransitionTime < TRANSITION_COOLDOWN) {
            System.out.println("⏳ Слишком рано для перехода, ждем...");
            return;
        }

        if (transitionInProgress) {
            System.out.println("⚠️ Переход уже в процессе");
            return;
        }

        transitionInProgress = true;
        lastTransitionTime = gameTime;

        System.out.println("🔄 Переход с части " + currentLevelPart + " на часть " + newLevelPart);

        // Сохраняем текущее положение игрока
        Vector2 playerPos = player.getPosition();

        // Устанавливаем новую позицию игрока в зависимости от перехода с буферной зоной
        if (currentLevelPart == 1 && newLevelPart == 2) {
            // Переход из части 1 в часть 2 (справа)
            player.body.setTransform(3, playerPos.y, 0);
        } else if (currentLevelPart == 2 && newLevelPart == 1) {
            // Переход из части 2 в часть 1 (слева)
            player.body.setTransform(17, playerPos.y, 0);
        } else if (newLevelPart == 3) {
            // Переход в часть 3 (сверху) - устанавливаем позицию подальше от точки перехода
            player.body.setTransform(10, 4, 0);
        } else if (currentLevelPart == 3) {
            // Переход из части 3 (снизу) - устанавливаем позицию подальше от точки перехода
            player.body.setTransform(10, 11, 0);
        }

        // Обновляем текущую часть уровня
        currentLevelPart = newLevelPart;

        // Инициализируем врагов для новой части уровня
        initLevelPart(currentLevelPart);

        // Сбрасываем флаг перехода через небольшую задержку
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                transitionInProgress = false;
            }
        });
    }

    @Override
    public void show() {}

    private boolean debugPrinted = false;

    @Override
    public void render(float delta) {
        // Накопление времени игры для защиты от зацикливания переходов
        gameTime += delta;
        
        // Обработка клавиши Escape для показа диалога выхода (в начале метода)
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            showExitConfirm = true;
            exitSelected = 0;
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
            
            // Рендерим диалог выхода
            renderExitDialog();
            return; // Не обрабатываем другие действия пока диалог открыт
        }

        world.step(delta, 6, 2);
        player.update(delta);

        // Проверяем завершение уровня
        checkLevelCompletion();

        // Обработка атак игрока по врагам
        if (player.canAttackHit()) {
            Vector2 playerPos = player.getPosition();
            float attackRange = 1.5f; // Дальность атаки игрока

            for (int i = 0; i < enemies.size; i++) {
                Enemy enemy = enemies.get(i);
                if (!enemy.isDead()) {
                    Vector2 enemyPos = enemy.getPosition();
                    float distance = playerPos.dst(enemyPos);

                    if (distance <= attackRange) {
                        enemy.takeDamage(1); // Наносим 1 урон (враг умрет после 2 ударов)
                        player.setAttackHit(); // Помечаем, что атака попала
                        System.out.println("🗡️ Игрок атаковал врага! Здоровье врага: " + enemy.getHealth());
                        break; // Атакуем только одного врага за раз
                    }
                }
            }
        }

        // Обновление врагов и проверка их атак на игрока
        for (int i = enemies.size - 1; i >= 0; i--) {
            Enemy enemy = enemies.get(i);

            if (enemy.isDead()) {
                // Удаляем мертвых врагов из мира и из списка
                world.destroyBody(enemy.body);
                enemies.removeIndex(i);
                System.out.println("💀 Враг удален из игрового мира!");
            } else {
                enemy.update(delta, player.getPosition());

                // Проверка атаки врага на игрока
                Vector2 playerPos = player.getPosition();
                Vector2 enemyPos = enemy.getPosition();
                float distance = playerPos.dst(enemyPos);

                if (distance <= 1.0f && !player.isInvulnerable()) {
                    player.takeDamage(1); // Наносим 1 урон игроку
                    System.out.println("⚔️ Враг атаковал игрока! Здоровье игрока: " + player.getHealth());
                }
            }
        }

        // Проверка завершения уровня (все враги убиты или игрок достиг точки перехода)
        if (enemies.size == 0 && !levelCompleted && currentLevelPart == totalLevelParts) {
            System.out.println("🎯 Все враги убиты! Уровень завершен!");
            completeLevel();
        }

        // Проверка смерти игрока
        if (player.isDead()) {
            // Обработка смерти игрока - переход на экран Game Over
            System.out.println("💀 Игрок умер! Переход на экран Game Over");

            // Задержка перед переходом на экран Game Over (3 секунды)
            // В реальной игре здесь можно добавить анимацию смерти
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // Переход на начальный экран (или специальный экран Game Over, если он будет создан)
            if (game != null) {
                game.setScreen(new FirstScreen(game));
            }
            return; // Прекращаем выполнение метода render
        }

        // Проверка перехода между частями уровня
        Vector2 playerPos = player.getPosition();

        // Проверяем все точки перехода
        float transitionDistance = 3.0f; // Увеличиваем расстояние для более легких переходов

        // Отладочный вывод позиции игрока для диагностики переходов
        if (Gdx.input.isKeyJustPressed(Input.Keys.F1)) {
            System.out.println("🔍 Отладка переходов:");
            System.out.println("   Позиция игрока: " + playerPos);
            System.out.println("   Текущая часть уровня: " + currentLevelPart);
            System.out.println("   💡 Удерживайте F1 для отображения точек перехода на экране");
            for (int i = 0; i < levelPartTransitions.size; i++) {
                Vector2 transPoint = levelPartTransitions.get(i);
                float dist = playerPos.dst(transPoint);
                System.out.println("   Расстояние до точки перехода " + i + " (" + transPoint.x + "," + transPoint.y + "): " + dist);
            }
        }

        // Проверка перехода на следующий уровень (когда игрок достигает определенной точки в последней части)
        if (currentLevelPart == totalLevelParts && playerPos.dst(levelPartTransitions.get(4)) < transitionDistance && !levelCompleted) {
            // Игрок достиг точки перехода на следующий уровень в последней части
            System.out.println("🎯 Игрок достиг точки перехода на следующий уровень!");
            System.out.println("   Позиция игрока: " + playerPos);
            System.out.println("   Точка перехода: " + levelPartTransitions.get(4));
            System.out.println("   Расстояние: " + playerPos.dst(levelPartTransitions.get(4)));
            System.out.println("   Текущая часть: " + currentLevelPart + "/" + totalLevelParts);
            completeLevel();
        }
        // Альтернативная проверка: если игрок находится в любой части и убил всех врагов
        else if (enemies.size == 0 && !levelCompleted) {
            System.out.println("🎯 Все враги убиты! Уровень завершен!");
            System.out.println("   Позиция игрока: " + playerPos);
            System.out.println("   Текущая часть: " + currentLevelPart + "/" + totalLevelParts);
            completeLevel();
        }
        // Переход из части 1 в часть 2 (правый край карты)
        else if (currentLevelPart == 1 && playerPos.dst(levelPartTransitions.get(0)) < transitionDistance && !transitionInProgress) {
            System.out.println("🔄 Переход из части 1 в часть 2 (правый край)");
            switchLevelPart(2);
        }
        // Переход из части 2 в часть 1 (левый край карты)
        else if (currentLevelPart == 2 && playerPos.dst(levelPartTransitions.get(1)) < transitionDistance && !transitionInProgress) {
            System.out.println("🔄 Переход из части 2 в часть 1 (левый край)");
            switchLevelPart(1);
        }
        // Переход из части 1 или 2 в часть 3 (верхний край карты)
        else if ((currentLevelPart == 1 || currentLevelPart == 2) &&
                 playerPos.dst(levelPartTransitions.get(2)) < transitionDistance && !transitionInProgress) {
            System.out.println("🔄 Переход из части " + currentLevelPart + " в часть 3 (верхний край)");
            switchLevelPart(3);
        }
        // Переход из части 3 в часть 1 (нижний край карты)
        else if (currentLevelPart == 3 && playerPos.dst(levelPartTransitions.get(3)) < transitionDistance && !transitionInProgress) {
            System.out.println("🔄 Переход из части 3 в часть 1 (нижний край)");
            switchLevelPart(1);
        }
        
        // Зафиксируем камеру в центре карты 20x15 тайлов
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

        // Рендерим игрока
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        player.render(batch);
        batch.end();

        // Рендерим врагов
        batch.begin();
        for (Enemy enemy : enemies) {
            enemy.render(batch);
        }
        batch.end();

        // Рендерим индикацию завершения уровня
        if (levelCompleted) {
            renderLevelCompleteIndicator();
        }

        // Рендерим точки перехода для отладки (только при нажатии F1)
        if (Gdx.input.isKeyPressed(Input.Keys.F1)) {
            renderTransitionPoints();
        }

        // Рендеринг диалога выхода
        // if (showExitConfirm) { // This block is now handled at the beginning of render
        //     renderExitDialog();
        // }
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

    /**
     * Рендерит индикацию завершения уровня
     */
    private void renderLevelCompleteIndicator() {
        // Рендерим полупрозрачный фон
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0, 0, 0, 0.7f);
        shapeRenderer.rect(0, 0, camera.viewportWidth, camera.viewportHeight);
        shapeRenderer.end();

        // Рендерим текст завершения уровня
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        
        String completeText = "🎯 Уровень завершен!";
        String nextLevelText = "Переход на следующий уровень...";
        String cutsceneText = "Подготовка к катсцене...";
        
        // Центрируем текст
        exitLayout.setText(exitTitleFont, completeText);
        float textX = (camera.viewportWidth - exitLayout.width) / 2;
        float textY = camera.viewportHeight / 2 + 80;
        
        exitTitleFont.setColor(Color.GOLD);
        exitTitleFont.draw(batch, completeText, textX, textY);
        
        // Текст о переходе
        exitLayout.setText(exitFont, nextLevelText);
        textX = (camera.viewportWidth - exitLayout.width) / 2;
        textY = camera.viewportHeight / 2;
        
        exitFont.setColor(Color.WHITE);
        exitFont.draw(batch, nextLevelText, textX, textY);
        
        // Дополнительный текст для катсцены
        if (levelName != null && (levelName.contains("level_0") || levelName.contains("test_small"))) {
            exitLayout.setText(exitFont, cutsceneText);
            textX = (camera.viewportWidth - exitLayout.width) / 2;
            textY = camera.viewportHeight / 2 - 80;
            
            exitFont.setColor(Color.CYAN);
            exitFont.draw(batch, cutsceneText, textX, textY);
        }
        
        batch.end();
    }

    /**
     * Рендерит точки перехода для отладки
     */
    private void renderTransitionPoints() {
        if (mapRenderer == null || map == null) return;

        // Рендерим точки перехода
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        
        for (int i = 0; i < levelPartTransitions.size; i++) {
            Vector2 transPoint = levelPartTransitions.get(i);
            
            // Разные цвета для разных типов переходов
            if (i == 4) {
                // Точка перехода на следующий уровень - красный цвет
                shapeRenderer.setColor(1, 0, 0, 0.7f);
            } else {
                // Обычные точки перехода между частями - зеленый цвет
                shapeRenderer.setColor(0, 1, 0, 0.5f);
            }
            
            shapeRenderer.circle(transPoint.x, transPoint.y, 0.5f);
        }
        
        shapeRenderer.end();

        // Рисуем текст рядом с точками
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        
        for (int i = 0; i < levelPartTransitions.size; i++) {
            Vector2 transPoint = levelPartTransitions.get(i);
            String label;
            if (i == 4) {
                label = "NEXT"; // Следующий уровень
            } else {
                label = "T" + i; // Обычные переходы
            }
            
            exitLayout.setText(exitFont, label);
            float textX = transPoint.x + 0.8f;
            float textY = transPoint.y + 0.3f;
            
            if (i == 4) {
                exitFont.setColor(Color.RED);
            } else {
                exitFont.setColor(Color.WHITE);
            }
            
            exitFont.draw(batch, label, textX, textY);
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
