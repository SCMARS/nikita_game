package com.nikita.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class GameOverScreen implements Screen {
    private NikitaGame game;
    private SpriteBatch batch;
    private BitmapFont titleFont, buttonFont;
    private ShapeRenderer shapeRenderer;
    private OrthographicCamera camera;
    private GlyphLayout layout = new GlyphLayout();

    private int selectedOption = 0; // 0 - Последний чекпоинт, 1 - Главное меню
    private String levelName; // Для возврата к чекпоинту
    private int playerHealth; // Здоровье игрока

    public GameOverScreen(NikitaGame game, String levelName, int playerHealth) {
        this.game = game;
        this.levelName = levelName;
        this.playerHealth = playerHealth;

        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();

        // Настройка камеры
        camera = new OrthographicCamera();
        camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        // Загрузка шрифтов
        titleFont = new BitmapFont(Gdx.files.internal("ui/font-subtitle.fnt"));
        buttonFont = new BitmapFont(Gdx.files.internal("ui/font.fnt"));

        // Настройка размеров шрифтов
        titleFont.getData().setScale(1.5f);
        buttonFont.getData().setScale(1.2f);
    }

    @Override
    public void show() {}

    @Override
    public void render(float delta) {
        // Обработка ввода
        handleInput();

        // Очистка экрана
        Gdx.gl.glClearColor(0.1f, 0.05f, 0.1f, 1); // Темно-фиолетовый фон
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();

        int winW = Gdx.graphics.getWidth();
        int winH = Gdx.graphics.getHeight();

        // Рендеринг фона диалога
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // Полупрозрачный фон
        shapeRenderer.setColor(0, 0, 0, 0.8f);
        shapeRenderer.rect(0, 0, winW, winH);

        // Фон диалога
        float boxW = 500;
        float boxH = 300;
        float boxX = (winW - boxW) / 2f;
        float boxY = (winH - boxH) / 2f;

        shapeRenderer.setColor(0.2f, 0.1f, 0.2f, 0.95f);
        shapeRenderer.rect(boxX, boxY, boxW, boxH);

        // Рамка диалога
        shapeRenderer.setColor(0.6f, 0.3f, 0.3f, 1f);
        float borderWidth = 3f;
        shapeRenderer.rect(boxX - borderWidth, boxY - borderWidth, boxW + 2*borderWidth, borderWidth); // Верх
        shapeRenderer.rect(boxX - borderWidth, boxY + boxH, boxW + 2*borderWidth, borderWidth); // Низ
        shapeRenderer.rect(boxX - borderWidth, boxY - borderWidth, borderWidth, boxH + 2*borderWidth); // Лево
        shapeRenderer.rect(boxX + boxW, boxY - borderWidth, borderWidth, boxH + 2*borderWidth); // Право

        shapeRenderer.end();

        // Рендеринг текста
        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        // Заголовок "Вы умерли"
        String title = "ВЫ УМЕРЛИ";
        layout.setText(titleFont, title);
        titleFont.setColor(Color.RED);
        titleFont.draw(batch, title, boxX + (boxW - layout.width) / 2f, boxY + boxH - 50);

        // Кнопки
        String option1 = "Последний чекпоинт";
        String option2 = "Главное меню";

        float button1Y = boxY + boxH - 130;
        float button2Y = boxY + boxH - 180;

        // Кнопка "Последний чекпоинт"
        layout.setText(buttonFont, option1);
        if (selectedOption == 0) {
            buttonFont.setColor(Color.GOLD);
        } else {
            buttonFont.setColor(Color.LIGHT_GRAY);
        }
        buttonFont.draw(batch, option1, boxX + (boxW - layout.width) / 2f, button1Y);

        // Кнопка "Главное меню"
        layout.setText(buttonFont, option2);
        if (selectedOption == 1) {
            buttonFont.setColor(Color.GOLD);
        } else {
            buttonFont.setColor(Color.LIGHT_GRAY);
        }
        buttonFont.draw(batch, option2, boxX + (boxW - layout.width) / 2f, button2Y);

        // Подсказка управления
        String hint = "Стрелки - выбор, Enter - подтвердить";
        layout.setText(buttonFont, hint);
        buttonFont.setColor(Color.GRAY);
        buttonFont.draw(batch, hint, boxX + (boxW - layout.width) / 2f, boxY + 30);

        batch.end();
    }

    private void handleInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.UP) || Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
            selectedOption = 1 - selectedOption; // Переключение между 0 и 1
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            if (selectedOption == 0) {
                // Возврат к последнему чекпоинту
                if (game != null) {
                    game.setScreen(new GameScreen(game, levelName));
                }
            } else {
                // Возврат в главное меню
                if (game != null) {
                    game.setScreen(new FirstScreen(game));
                }
            }
        }
    }

    @Override
    public void resize(int width, int height) {
        camera.setToOrtho(false, width, height);
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
        if (titleFont != null) titleFont.dispose();
        if (buttonFont != null) buttonFont.dispose();
    }
}
