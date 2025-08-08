package com.nikita.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Box2D;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

public class Player {
    public Body body;
    private Texture walkTexture, attackTexture;
    private TextureRegion currentFrame;
    private Animation<TextureRegion> walkAnimation, attackAnimation;
    private Animation<TextureRegion> currentAnimation;
    private float stateTime = 0f;
    private Direction currentDirection = Direction.DOWN;
    private boolean isMoving = false;
    public boolean isAttacking = false; // Делаем публичным для доступа из GameScreen
    private float attackTime = 0f;
    private final float ATTACK_DURATION = 0.6f; // Длительность атаки (6 кадров * 0.1с)
    private boolean attackHit = false; // Флаг, чтобы атака срабатывала только один раз

    private boolean canJump = false;
    private int health = 4;
    private int maxHealth = 4;
    private int keys = 0;
    private int seals = 0; // Печати/кристаллы
    private boolean isDead = false;
    private float invulnerabilityTime = 0f; // Время неуязвимости после получения урона
    private final float INVULNERABILITY_DURATION = 1.5f; // 1.5 секунды неуязвимости

    // Направления движения
    public enum Direction {
        UP, DOWN, LEFT, RIGHT
    }

    public Player(World world, float x, float y) {
        // Создание физического тела
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(x, y);
        body = world.createBody(bodyDef);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(0.4f, 0.9f);
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = 1f;
        fixtureDef.friction = 0.2f;
        body.createFixture(fixtureDef);
        shape.dispose();

        // Загрузка текстур персонажа
        walkTexture = new Texture("walk_cycle.png");     // Анимация ходьбы
        attackTexture = new Texture("attack_cycle.png"); // Анимация атаки

        // Создаем анимации (пока используем один кадр, но структура готова для спрайт-листов)
        createAnimations();

        // Устанавливаем начальный кадр
        currentFrame = walkAnimation.getKeyFrame(0);
    }

    private void createAnimations() {
        // Анимация ходьбы - 6 кадров в горизонтальном ряду
        int walkFrameWidth = walkTexture.getWidth() / 6;  // 6 кадров ходьбы (256px каждый)
        int walkFrameHeight = walkTexture.getHeight();

        // Обрезаем левую часть и увеличиваем правую часть для правильного центрирования
        int cropLeft = walkFrameWidth / 2;  // Обрезаем половину ширины слева
        int extraRight = walkFrameWidth / 3;  // Добавляем треть ширины справа (уменьшено с половины)

        TextureRegion[] walkFrames = new TextureRegion[6];
        for (int i = 0; i < 6; i++) {
            // Создаем регион с обрезкой слева и расширением справа
            walkFrames[i] = new TextureRegion(walkTexture,
                i * walkFrameWidth + cropLeft, 0,  // Начинаем с середины кадра (обрезаем левую часть)
                walkFrameWidth - cropLeft + extraRight, walkFrameHeight);  // Расширяем правую часть
        }

        // Анимация атаки - 6 кадров в горизонтальном ряду
        int attackFrameWidth = attackTexture.getWidth() / 6;  // 6 кадров атаки (256px каждый)
        int attackFrameHeight = attackTexture.getHeight();

        // Применяем ту же логику обрезки и расширения для атаки
        // Используем то же значение extraRight (walkFrameWidth/3) для согласованности
        TextureRegion[] attackFrames = new TextureRegion[6];
        for (int i = 0; i < 6; i++) {
            // Создаем регион с обрезкой слева и уменьшенным расширением справа
            attackFrames[i] = new TextureRegion(attackTexture,
                i * attackFrameWidth + cropLeft, 0,  // Начинаем с середины кадра (обрезаем левую часть)
                attackFrameWidth - cropLeft + (attackFrameWidth / 3), attackFrameHeight);  // Расширяем правую часть на треть ширины
        }

        // Настройки времени анимации
        float walkFrameDuration = 0.12f;   // Плавная ходьба
        float attackFrameDuration = 0.08f;  // Быстрая атака

        // Создаем анимации
        walkAnimation = new Animation<>(walkFrameDuration, walkFrames);
        walkAnimation.setPlayMode(Animation.PlayMode.LOOP);

        attackAnimation = new Animation<>(attackFrameDuration, attackFrames);
        attackAnimation.setPlayMode(Animation.PlayMode.NORMAL); // Один раз

        // Устанавливаем начальную анимацию
        currentAnimation = walkAnimation;

        System.out.println("✅ Анимации созданы:");
        System.out.println("   Ходьба: " + walkFrames.length + " кадров (" + walkFrameWidth + "x" + walkFrameHeight + ")");
        System.out.println("   Атака: " + attackFrames.length + " кадров (" + attackFrameWidth + "x" + attackFrameHeight + ")");
        System.out.println("   Размер текстур: walk=" + walkTexture.getWidth() + "x" + walkTexture.getHeight() +
                          ", attack=" + attackTexture.getWidth() + "x" + attackTexture.getHeight());
        System.out.println("   Обрезка: слева=" + cropLeft + "px, расширение справа=" + extraRight +
                          "px (уменьшено), итоговая ширина=" + (walkFrameWidth - cropLeft + extraRight) + "px");
    }

    public void update(float delta) {
        stateTime += delta;

        // Обновляем время неуязвимости
        if (invulnerabilityTime > 0) {
            invulnerabilityTime -= delta;
        }

        // Обновляем время атаки
        if (isAttacking) {
            attackTime += delta;
            if (attackTime >= ATTACK_DURATION) {
                isAttacking = false;
                attackTime = 0f;
                attackHit = false; // Сбрасываем флаг атаки
                stateTime = 0f; // Сбрасываем время для плавного перехода
            }
        }

        // Проверяем атаку (пробел или Enter)
        if (!isAttacking && (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) || Gdx.input.isKeyJustPressed(Input.Keys.ENTER))) {
            isAttacking = true;
            attackTime = 0f;
            attackHit = false; // Сбрасываем флаг атаки
            stateTime = 0f;
            System.out.println("🗡️ Атака!");
        }

        // Если атакуем, не обрабатываем движение
        if (isAttacking) {
            body.setLinearVelocity(0, 0); // Останавливаем движение во время атаки
            updateAnimation();
            return;
        }

        // Управление во всех направлениях (WASD + стрелки)
        float moveX = 0;
        float moveY = 0;
        Direction newDirection = currentDirection;
        boolean wasMoving = isMoving;

        // Горизонтальное движение
        if (Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            moveX = -1;
            newDirection = Direction.LEFT;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            moveX = 1;
            newDirection = Direction.RIGHT;
        }

        // Вертикальное движение
        if (Gdx.input.isKeyPressed(Input.Keys.S) || Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            moveY = -1;
            newDirection = Direction.DOWN;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.W) || Gdx.input.isKeyPressed(Input.Keys.UP)) {
            moveY = 1;
            newDirection = Direction.UP;
        }

        // Определяем, движется ли персонаж
        isMoving = (moveX != 0 || moveY != 0);

        // Если направление изменилось или начал/прекратил движение, сбрасываем время анимации
        if (newDirection != currentDirection || isMoving != wasMoving) {
            stateTime = 0f;
            currentDirection = newDirection;
        }

        // Применяем движение с простой проверкой границ
        float speed = 3f;
        Vector2 currentPos = body.getPosition();
        
        // Проверяем границы карты перед применением движения
        float newX = currentPos.x + moveX * speed * delta;
        float newY = currentPos.y + moveY * speed * delta;
        
        // Ограничиваем движение границами карты (20x15 тайлов)
        if (newX < 0.5f) newX = 0.5f;
        if (newX > 19.5f) newX = 19.5f;
        if (newY < 0.5f) newY = 0.5f;
        if (newY > 14.5f) newY = 14.5f;
        
        // Применяем движение
        body.setLinearVelocity((newX - currentPos.x) / delta, (newY - currentPos.y) / delta);

        // Обновляем текущий кадр анимации
        updateAnimation();

        // Прыжок (на клавишу J)
        if (Gdx.input.isKeyJustPressed(Input.Keys.J) && canJump) {
            body.applyLinearImpulse(new Vector2(0, 6f), body.getWorldCenter(), true);
            canJump = false;
            System.out.println("🦘 Прыжок!");
        }
    }

    private void updateAnimation() {
        stateTime += Gdx.graphics.getDeltaTime();

        Animation<TextureRegion> newAnimation;

        // Приоритет у атаки
        if (isAttacking) {
            newAnimation = attackAnimation;
        } else if (isMoving) {
            newAnimation = walkAnimation;
        } else {
            newAnimation = null; // Нет анимации для покоя
        }

        // Сбрасываем время анимации при смене анимации
        if (currentAnimation != newAnimation) {
            currentAnimation = newAnimation;
            stateTime = 0;
        }

        // Получаем текущий кадр анимации
        if (currentAnimation != null) {
            if (isAttacking) {
                // Для атаки - без зацикливания
                currentFrame = currentAnimation.getKeyFrame(stateTime, false);
            } else {
                // Для ходьбы - с зацикливанием
                currentFrame = currentAnimation.getKeyFrame(stateTime, true);
            }
        } else {
            // Для покоя - используем первый кадр ходьбы
            currentFrame = walkAnimation.getKeyFrame(0, false);
        }
    }

    public void render(SpriteBatch batch) {
        Vector2 position = body.getPosition();
        float width = 1.2f;   // Увеличенная ширина спрайта
        float height = 1.6f;  // Увеличенная высота спрайта

        // Включаем блендинг для правильной прозрачности
        batch.enableBlending();
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        // Мигание при неуязвимости
        if (isInvulnerable()) {
            // Мигаем каждые 0.1 секунды
            float blinkTime = invulnerabilityTime % 0.2f;
            if (blinkTime > 0.1f) {
                batch.setColor(1f, 1f, 1f, 0.5f); // Полупрозрачный
            } else {
                batch.setColor(1f, 1f, 1f, 1f); // Обычный
            }
        } else {
            batch.setColor(1f, 1f, 1f, 1f); // Обычный цвет
        }

        // Рисуем текущий кадр анимации с учетом модифицированных TextureRegion
        // Поскольку мы уже обрезали левую часть и расширили правую часть в TextureRegion,
        // нам нужно только небольшое смещение для точной настройки
        float offsetX = 0.5f;  // Небольшое дополнительное смещение для точной настройки
        float widthMultiplier = 1.2f;  // Небольшое увеличение ширины для точной настройки

        batch.draw(currentFrame,
                   position.x - width/2 - offsetX,    // Смещаем немного влево для точной настройки
                   position.y - height/2,   // Центрируем по Y
                   width * widthMultiplier, height);  // Немного увеличиваем ширину для точной настройки

        // Восстанавливаем цвет
        batch.setColor(1f, 1f, 1f, 1f);
    }

    public void setCanJump(boolean value) {
        canJump = value;
    }

    public Vector2 getPosition() {
        return body.getPosition();
    }

    public void heal(int amount) {
        health = Math.min(health + amount, maxHealth);
    }

    public void addKey() {
        keys++;
    }

    public void addSeal() {
        seals++;
    }

    public int getHealth() { return health; }

    public int getKeys() { return keys; }

    public int getSeals() { return seals; }

    public boolean hasKey() {
        return keys > 0;
    }

    public void useKey() {
        if (keys > 0) {
            keys--;
        }
    }

    public void setHealth(int value) {
        this.health = Math.max(0, Math.min(value, maxHealth));
    }
    public void setKeys(int value) {
        this.keys = Math.max(0, value);
    }
    public void setSeals(int value) {
        this.seals = Math.max(0, value);
    }

    public void takeDamage(int damage) {
        if (invulnerabilityTime <= 0 && !isDead) {
            health -= damage;
            invulnerabilityTime = INVULNERABILITY_DURATION;
            System.out.println("💔 Игрок получил урон! Здоровье: " + health);

            if (health <= 0) {
                isDead = true;
                health = 0;
                System.out.println("💀 Игрок умер!");
            }
        }
    }

    public boolean isDead() {
        return isDead;
    }

    public boolean isInvulnerable() {
        return invulnerabilityTime > 0;
    }

    public boolean canAttackHit() {
        return isAttacking && !attackHit;
    }

    public void setAttackHit() {
        attackHit = true;
    }

    public void dispose() {
        if (walkTexture != null) {
            walkTexture.dispose();
        }
        if (attackTexture != null) {
            attackTexture.dispose();
        }
    }
}
