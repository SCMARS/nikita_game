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
    private String[] menu = {"Новая игра", "Продолжить", "Выйти"};
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

    public FirstScreen(NikitaGame game) {
        this.game = game;
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        bg = new Texture("start_menu_bg.png");
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("Roboto-Regular.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 48;
        parameter.characters = "АБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯабвгдеёжзийклмнопрстуфхцчшщъыьэюя0123456789.,:!?\"'()[]{}<>-_=+/*\\%$#@&~|; abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ«»—…– ";
        font = generator.generateFont(parameter);
        parameter.size = 56;
        parameter.color = Color.GOLD;
        fontSelected = generator.generateFont(parameter);
        parameter.size = 64;
        parameter.color = Color.GOLD;
        titleFont = generator.generateFont(parameter);
        parameter.size = 64;
        parameter.color = Color.BLACK;
        titleFontShadow = generator.generateFont(parameter);
        parameter.size = 48;
        fontShadow = generator.generateFont(parameter);
        generator.dispose();
        // Проверка наличия сохранения
        Preferences prefs = Gdx.app.getPreferences("save");
        canContinue = prefs.contains("level");
        // Музыка
        music = Gdx.audio.newMusic(Gdx.files.internal("prologue_theme.mp3"));
        music.setLooping(true);
        music.play();
        for (int i = 0; i < animScale.length; i++) animScale[i] = 1f;
    }

    @Override
    public void show() {
        // Prepare your screen here.
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0,0,0,1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        batch.begin();
        int winW = Gdx.graphics.getWidth();
        int winH = Gdx.graphics.getHeight();
        // Фон
        int texW = bg.getWidth();
        int texH = bg.getHeight();
        float scale = Math.min((float)winW / texW, (float)winH / texH);
        float drawW = texW * scale;
        float drawH = texH * scale;
        float drawX = (winW - drawW) / 2f;
        float drawY = (winH - drawH) / 2f;
        batch.draw(bg, drawX, drawY, drawW, drawH);
        // Заголовок с тенью
        String title = "NIKITA: Восстание Принцессы";
        layout.setText(titleFont, title);
        float titleX = (winW - layout.width) / 2f;
        float titleY = winH/2f + 120;
        titleFontShadow.draw(batch, title, titleX+3, titleY-3);
        titleFont.draw(batch, title, titleX, titleY);
        batch.end();
         
        float menuStartY = winH/2f + 20;
        int hovered = -1;
        int mouseX = Gdx.input.getX();
        int mouseY = winH - Gdx.input.getY();
        for (int i = 0; i < menu.length; i++) {
            String text = menu[i];
            boolean disabled = (i == 1 && !canContinue);
            layout.setText(font, text);
            float textX = (winW - layout.width) / 2f;
            float textY = menuStartY - i*190;
            menuX[i] = textX;
            menuY[i] = textY;
            boolean isHovered = mouseX >= textX-30 && mouseX <= textX + layout.width+30 && mouseY >= textY - layout.height-18 && mouseY <= textY+18;
            if (isHovered && !disabled) hovered = i;
            float target = (i == selected || isHovered) && !disabled ? 1.10f : 1f;
            animScale[i] += (target - animScale[i]) * Math.min(1, delta*16f);
            if (animScale[i] > 1.12f) animScale[i] = 1.12f;
            if (animScale[i] < 0.98f) animScale[i] = 0.98f;
        }
        // ShapeRenderer для кнопок
        shapeRenderer.begin(ShapeType.Filled);
        for (int i = 0; i < menu.length; i++) {
            String text = menu[i];
            boolean disabled = (i == 1 && !canContinue);
            layout.setText(font, text);
            float w = layout.width * animScale[i] + 80;
            float h = layout.height * animScale[i] + 56;
            float x = (winW - w) / 2f;
            float y = (menuStartY - i*190) - h/2f + layout.height/2f;
            float textX = x + (w - layout.width * animScale[i]) / 2f;
            float textY = y + (h + layout.height * animScale[i]) / 2f - 6;
            // Glow-эффект и фон по цвету кнопки
            if ((i == selected || (hovered == i && !disabled))) {
                if (i == 0) shapeRenderer.setColor(1.0f,0.7f,0.2f,0.38f); // Новая игра — янтарный
                else if (i == 1) shapeRenderer.setColor(0.5f,0.3f,0.8f,0.32f); // Продолжить — фиолетовый
                else shapeRenderer.setColor(0.7f,0.1f,0.1f,0.45f); // Выйти — красный
                for (int g=0; g<4; g++) {
                    float grow = 5+g*2;
                    shapeRenderer.rect(x-grow, y-grow, w+grow*2, h+grow*2);
                }
            }
           
            if (disabled) shapeRenderer.setColor(0.15f,0.15f,0.15f,0.7f);
            else if (i == selected || (hovered == i && !disabled)) {
                if (i == 0) shapeRenderer.setColor(0.45f,0.32f,0.12f,0.92f); // янтарный
                else if (i == 1) shapeRenderer.setColor(0.22f,0.18f,0.32f,0.92f); // фиолетовый
                else shapeRenderer.setColor(0.32f,0.08f,0.08f,0.92f); // красный
            } else {
                shapeRenderer.setColor(0.18f,0.18f,0.18f,0.85f);
            }
            shapeRenderer.rect(x, y, w, h);
        }
        shapeRenderer.end();

        batch.begin();
        for (int i = 0; i < menu.length; i++) {
            String text = menu[i];
            boolean disabled = (i == 1 && !canContinue);
            layout.setText(font, text);
            float w = layout.width * animScale[i] + 80;
            float h = layout.height * animScale[i] + 56;
            float x = (winW - w) / 2f;
            float y = (menuStartY - i*190) - h/2f + layout.height/2f;
            float textX = x + (w - layout.width * animScale[i]) / 2f;
            float textY = y + (h + layout.height * animScale[i]) / 2f - 6;
            // Тень
            fontShadow.setColor(0,0,0,0.7f);
            fontShadow.getData().setScale(animScale[i]);
            fontShadow.draw(batch, text, textX+2, textY-2);
            // Выделение выбранного пункта
            if ((i == selected || hovered == i) && !disabled) {
                if (i == 0) fontSelected.setColor(new Color(1.0f,0.8f,0.3f,1f)); // янтарный
                else if (i == 1) fontSelected.setColor(new Color(0.7f,0.5f,1f,1f)); // фиолетовый
                else fontSelected.setColor(new Color(1f,0.3f,0.3f,1f)); // красный
                fontSelected.getData().setScale(animScale[i]);
                fontSelected.draw(batch, text, textX, textY);
            } else {
                font.setColor(disabled ? Color.DARK_GRAY : Color.WHITE);
                font.getData().setScale(animScale[i]);
                font.draw(batch, text, textX, textY);
            }
        }
        batch.end();
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
        // Наведение мышью
        if (hovered != -1) selected = hovered;
        // Клик мышью или Enter/Пробел
        boolean mouseClicked = Gdx.input.justTouched() && hovered != -1;
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
                game.setScreen(new GameScreen(level));
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

        // Resize your screen here. The parameters represent the new window size.
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