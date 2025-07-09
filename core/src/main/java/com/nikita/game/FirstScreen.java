package com.nikita.game;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;

/** First screen of the application. Displayed after the application is created. */
public class FirstScreen implements Screen {
    private NikitaGame game;
    private SpriteBatch batch;
    private BitmapFont font, titleFont, fontSelected, titleFontShadow, fontShadow;
    private int selected = 0;
    private String[] menu = {"New Game", "Continue", "Exit"};
    private boolean canContinue = false;
    private GlyphLayout layout = new GlyphLayout();
    private float[] menuX = new float[3];
    private float[] menuY = new float[3];
    private Texture bg;
    private Music music;
    private boolean showExitConfirm = false;
    private GlyphLayout exitLayout = new GlyphLayout();
    private int exitSelected = 0; // 0 - Да, 1 - Нет
    private ShapeRenderer shapeRenderer;
    private float[] animScale = new float[3];
    private float[] buttonX = new float[3];
    private float[] buttonY = new float[3];
    private float[] buttonW = new float[3];
    private float[] buttonH = new float[3];

    public FirstScreen(NikitaGame game) {
        this.game = game;
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        bg = new Texture("start_menu_bg.png");
        // Создаем качественные шрифты с улучшенным рендерингом
        // Вычисляем масштаб на основе разрешения экрана
        float screenScale = Math.min(Gdx.graphics.getWidth() / 1280f, Gdx.graphics.getHeight() / 720f);

        font = new BitmapFont();
        font.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        font.getData().setScale(1.8f * screenScale);
        font.setColor(Color.WHITE);

        fontSelected = new BitmapFont();
        fontSelected.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        fontSelected.getData().setScale(2.2f * screenScale);
        fontSelected.setColor(Color.GOLD);

        titleFont = new BitmapFont();
        titleFont.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        titleFont.getData().setScale(4.0f * screenScale);
        titleFont.setColor(new Color(1.0f, 0.8f, 0.2f, 1.0f)); // Золотистый

        titleFontShadow = new BitmapFont();
        titleFontShadow.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        titleFontShadow.getData().setScale(4.0f * screenScale);
        titleFontShadow.setColor(new Color(0.2f, 0.1f, 0.0f, 0.8f)); // Темная тень

        fontShadow = new BitmapFont();
        fontShadow.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        fontShadow.getData().setScale(1.8f * screenScale);
        fontShadow.setColor(new Color(0, 0, 0, 0.6f));
        // Проверка наличия сохранения
        Preferences prefs = Gdx.app.getPreferences("save");
        canContinue = prefs.contains("level");
        // Музыка
        music = Gdx.audio.newMusic(Gdx.files.internal("prologue_theme.mp3"));
        music.setLooping(true);
        music.play();
        for (int i = 0; i < animScale.length; i++) animScale[i] = 1f;

        // Инициализируем правильные размеры
        resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    @Override
    public void show() {
        // Prepare your screen here.
    }

    @Override
    public void render(float delta) {
        // Включаем сглаживание для качественного рендеринга
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        Gdx.gl.glClearColor(0,0,0,1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Получаем актуальные размеры экрана
        int winW = Gdx.graphics.getWidth();
        int winH = Gdx.graphics.getHeight();

        // Устанавливаем правильную проекционную матрицу каждый кадр для корректного центрирования
        batch.getProjectionMatrix().setToOrtho2D(0, 0, winW, winH);
        shapeRenderer.getProjectionMatrix().setToOrtho2D(0, 0, winW, winH);

        batch.begin();
        // Фон
        int texW = bg.getWidth();
        int texH = bg.getHeight();
        float scale = Math.min((float)winW / texW, (float)winH / texH);
        float drawW = texW * scale;
        float drawH = texH * scale;
        float drawX = (winW - drawW) / 2f;
        float drawY = (winH - drawH) / 2f;
        batch.draw(bg, drawX, drawY, drawW, drawH);
        // Красивый заголовок с тенью
        String title = "NIKITA GAME";
        layout.setText(titleFont, title);
        float titleX = (winW - layout.width) / 2f;
        float titleY = winH - 120;

        // Рисуем тень заголовка
        titleFontShadow.draw(batch, title, titleX + 4, titleY - 4);
        // Рисуем основной заголовок
        titleFont.draw(batch, title, titleX, titleY);

        // Обновляем анимацию масштаба
        for (int i = 0; i < menu.length; i++) {
            float target = (i == selected) ? 1.15f : 1.0f;
            animScale[i] += (target - animScale[i]) * delta * 8f;
        }

        batch.end();

        // Рисуем красивые кнопки
        shapeRenderer.begin(ShapeType.Filled);

        // Вычисляем масштаб для кнопок на основе разрешения экрана
        float screenScale = Math.min(winW / 1280f, winH / 720f);

        // МАКСИМАЛЬНО ПРОСТОЕ центрирование - просто используем центр экрана
        float buttonWidth = 300;
        float buttonHeight = 60;

        for (int i = 0; i < menu.length; i++) {
            String text = menu[i];
            boolean disabled = (i == 1 && !canContinue);

            // Размеры кнопок
            buttonW[i] = buttonWidth;
            buttonH[i] = buttonHeight;

            // ЖЁСТКО центрируем каждую кнопку по X
            buttonX[i] = (winW - buttonWidth) / 2f;

            // Простое вертикальное позиционирование от центра
            buttonY[i] = winH / 2f + 50 - i * 80;

            // Центрируем текст в кнопке
            menuX[i] = winW / 2f; // точно по центру экрана
            menuY[i] = buttonY[i] + buttonHeight / 2f + 8;

            // Эффект свечения для выбранной кнопки
            if (i == selected) {
                // Внешнее свечение
                for (int glow = 0; glow < 6; glow++) {
                    float alpha = 0.1f - glow * 0.015f;
                    float expand = glow * 3f;
                    if (i == 0) shapeRenderer.setColor(1.0f, 0.7f, 0.2f, alpha); // Золотой для "New Game"
                    else if (i == 1) shapeRenderer.setColor(0.5f, 0.8f, 1.0f, alpha); // Голубой для "Continue"
                    else shapeRenderer.setColor(1.0f, 0.4f, 0.4f, alpha); // Красный для "Exit"

                    shapeRenderer.rect(buttonX[i] - expand, buttonY[i] - expand,
                                     buttonW[i] + expand * 2, buttonH[i] + expand * 2);
                }
            }

            // Основная кнопка
            if (disabled) {
                shapeRenderer.setColor(0.3f, 0.3f, 0.3f, 0.8f);
            } else if (i == selected) {
                if (i == 0) shapeRenderer.setColor(0.8f, 0.6f, 0.1f, 0.9f); // Золотой
                else if (i == 1) shapeRenderer.setColor(0.2f, 0.5f, 0.8f, 0.9f); // Голубой
                else shapeRenderer.setColor(0.8f, 0.2f, 0.2f, 0.9f); // Красный
            } else {
                shapeRenderer.setColor(0.4f, 0.4f, 0.5f, 0.7f);
            }
            shapeRenderer.rect(buttonX[i], buttonY[i], buttonW[i], buttonH[i]);

            // Рамка кнопки
            shapeRenderer.setColor(0.8f, 0.8f, 0.9f, 0.6f);
            shapeRenderer.rect(buttonX[i] - 2, buttonY[i] - 2, buttonW[i] + 4, 2); // верх
            shapeRenderer.rect(buttonX[i] - 2, buttonY[i] + buttonH[i], buttonW[i] + 4, 2); // низ
            shapeRenderer.rect(buttonX[i] - 2, buttonY[i], 2, buttonH[i]); // лево
            shapeRenderer.rect(buttonX[i] + buttonW[i], buttonY[i], 2, buttonH[i]); // право
        }

        shapeRenderer.end();

        // Рисуем текст кнопок
        batch.begin();

        for (int i = 0; i < menu.length; i++) {
            String text = menu[i];
            boolean disabled = (i == 1 && !canContinue);

            // Вычисляем центрированную позицию текста
            BitmapFont currentFont = (i == selected && !disabled) ? fontSelected : font;
            float textScale = (i == selected && !disabled) ? 2.2f : 1.8f;
            currentFont.getData().setScale(textScale * screenScale * animScale[i]);

            layout.setText(currentFont, text);
            float textX = menuX[i] - layout.width / 2f;
            float textY = menuY[i];

            // Тень текста
            fontShadow.getData().setScale(textScale * screenScale * animScale[i]);
            fontShadow.draw(batch, text, textX + 2, textY - 2);

            // Основной текст
            if (i == selected && !disabled) {
                if (i == 0) fontSelected.setColor(1.0f, 0.9f, 0.3f, 1.0f); // Яркое золото
                else if (i == 1) fontSelected.setColor(0.6f, 0.9f, 1.0f, 1.0f); // Яркий голубой
                else fontSelected.setColor(1.0f, 0.6f, 0.6f, 1.0f); // Яркий красный
                fontSelected.draw(batch, text, textX, textY);
            } else {
                font.setColor(disabled ? Color.DARK_GRAY : Color.WHITE);
                font.draw(batch, text, textX, textY);
            }
        }
        batch.end();

        // Рендеринг диалога выхода
        if (showExitConfirm) {
            renderExitDialog(winW, winH);
        }

        if (showExitConfirm) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT) || Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) exitSelected = 1-exitSelected;
            if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || Gdx.input.isKeyJustPressed(Input.Keys.SPACE) || (Gdx.input.justTouched() && handleExitMouse(winW, winH))) {
                if (exitSelected == 0) Gdx.app.exit();
                else showExitConfirm = false;
            }
            if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) showExitConfirm = false;
            return;
        }
        // Навигация клавиатурой
        if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
            do { selected = (selected + menu.length - 1) % menu.length; } while (selected == 1 && !canContinue);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
            do { selected = (selected + 1) % menu.length; } while (selected == 1 && !canContinue);
        }
        // Упрощенная навигация без мыши
        // Клик мышью или Enter/Пробел
        boolean mouseClicked = Gdx.input.justTouched();
        boolean keyClicked = Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || Gdx.input.isKeyJustPressed(Input.Keys.SPACE);
        if ((mouseClicked || keyClicked)) {
            if (selected == 0) {
                Preferences prefs = Gdx.app.getPreferences("save");
                prefs.clear();
                prefs.flush();
                game.setScreen(new KingIntroScreen(game));
            }
            if (selected == 1 && canContinue) {
                Preferences prefs = Gdx.app.getPreferences("save");
                String level = prefs.getString("level", "Level1.tmx");
                game.setScreen(new GameScreen(game, level));
            }
            if (selected == 2) {
                showExitConfirm = true;
                exitSelected = 0;
            }
        }
    }

    @Override
    public void resize(int width, int height) {
        // If the window is minimized on a desktop (LWJGL3) platform, width and height are 0, which causes problems.
        // In that case, we don't resize anything, and wait for the window to be a normal size before updating.
        if(width <= 0 || height <= 0) return;

        // Стандартный подход LibGDX - просто обновляем viewport
        Gdx.gl.glViewport(0, 0, width, height);
    }

    @Override
    public void pause() {
        // Invoked when your application is paused.
    }

    @Override
    public void resume() {
        // Invoked when your application is resumed after pause.
    }

    @Override
    public void hide() {
        if (music != null) music.stop();
    }

    @Override
    public void dispose() {
        batch.dispose();
        shapeRenderer.dispose();
        bg.dispose();
        font.dispose();
        fontSelected.dispose();
        fontShadow.dispose();
        titleFont.dispose();
        titleFontShadow.dispose();
        if (music != null) music.dispose();
    }

    // Рендеринг диалога выхода
    private void renderExitDialog(int winW, int winH) {
        String msg = "Exit game?";
        layout.setText(titleFont, msg);
        float boxW = layout.width + 80;
        float boxH = 180;
        float boxX = (winW - boxW)/2f;
        float boxY = (winH - boxH)/2f;

        // Полупрозрачный фон
        shapeRenderer.begin(ShapeType.Filled);
        shapeRenderer.setColor(0, 0, 0, 0.7f);
        shapeRenderer.rect(0, 0, winW, winH);

        // Фон диалога
        shapeRenderer.setColor(0.2f, 0.2f, 0.2f, 0.95f);
        shapeRenderer.rect(boxX, boxY, boxW, boxH);

        // Рамка диалога
        shapeRenderer.setColor(0.6f, 0.6f, 0.6f, 1f);
        shapeRenderer.rect(boxX-2, boxY-2, boxW+4, 2); // верх
        shapeRenderer.rect(boxX-2, boxY+boxH, boxW+4, 2); // низ
        shapeRenderer.rect(boxX-2, boxY, 2, boxH); // лево
        shapeRenderer.rect(boxX+boxW, boxY, 2, boxH); // право
        shapeRenderer.end();

        // Текст диалога
        batch.begin();
        titleFont.setColor(Color.WHITE);
        titleFont.draw(batch, msg, boxX + (boxW - layout.width)/2f, boxY + boxH - 40);

        // Кнопки
        String yes = "Yes";
        String no = "No";
        exitLayout.setText(fontSelected, yes);
        float yesX = boxX + 60;
        float btnY = boxY + 60;
        exitLayout.setText(fontSelected, no);
        float noX = boxX + boxW - 60 - exitLayout.width;

        // Выделение выбранной кнопки
        if (exitSelected == 0) {
            fontSelected.setColor(Color.GOLD);
            fontSelected.draw(batch, yes, yesX, btnY);
            font.setColor(Color.LIGHT_GRAY);
            font.draw(batch, no, noX, btnY);
        } else {
            font.setColor(Color.LIGHT_GRAY);
            font.draw(batch, yes, yesX, btnY);
            fontSelected.setColor(Color.GOLD);
            fontSelected.draw(batch, no, noX, btnY);
        }
        batch.end();
    }

    // Обработка клика мышью по кнопкам диалога выхода
    private boolean handleExitMouse(int winW, int winH) {
        float menuStartY = winH/2f + 20;
        String msg = "Выйти из игры?";
        layout.setText(titleFont, msg);
        float boxW = layout.width + 80;
        float boxH = 180;
        float boxX = (winW - boxW)/2f;
        float boxY = (winH - boxH)/2f;
        String yes = "Да";
        String no = "Нет";
        exitLayout.setText(fontSelected, yes);
        float yesX = boxX + 60;
        float btnY = boxY + 60;
        exitLayout.setText(fontSelected, no);
        float noX = boxX + boxW - 60 - exitLayout.width;
        int mouseX = Gdx.input.getX();
        int mouseY = winH - Gdx.input.getY();
        if (mouseY >= btnY - exitLayout.height && mouseY <= btnY && mouseX >= yesX && mouseX <= yesX + exitLayout.width) { exitSelected = 0; return true; }
        if (mouseY >= btnY - exitLayout.height && mouseY <= btnY && mouseX >= noX && mouseX <= noX + exitLayout.width) { exitSelected = 1; return true; }
        return false;
    }

    // (скругление убрано, т.к. ShapeRenderer не поддерживает его для rect)
}
