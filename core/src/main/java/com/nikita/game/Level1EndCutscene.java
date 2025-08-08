package com.nikita.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.MathUtils;

public class Level1EndCutscene implements Screen {
    private NikitaGame game;
    private SpriteBatch batch;
    private BitmapFont font;
    private Texture background;
    private Music music;
    
    private float fadeIn = 0f;
    private float fadeOut = 0f;
    private int currentScene = 0;
    private float sceneTimer = 0f;
    private boolean sceneComplete = false;
    
    private String[] scenes = {
        "Никита выходит из подземелья замка...",
        "Перед ней раскинулся темный лес, окутанный туманом.",
        "Глеб: Твой отец не остановится. Он будет искать тебя.",
        "Никита: Я знаю. Но я не позволю ему продолжать эти жертвоприношения.",
        "Глеб: В лесу есть древний алтарь. Там мы сможем найти способ остановить его.",
        "Никита: Что за алтарь?",
        "Глеб: Место силы, где когда-то совершались ритуалы защиты королевства.",
        "Глеб: Но твой отец извратил их, превратив в источник своей власти.",
        "Никита: Тогда мы должны добраться туда первыми.",
        "Глеб: Путь опасен. Лес полон тварей, подчиненных воле твоего отца.",
        "Никита: Я готова. Что бы ни ждало нас впереди.",
        "Глеб: Тогда идем. Время работает против нас.",
        "Никита и Глеб исчезают в тумане леса...",
        "Где-то вдалеке слышится вой волков...",
        "И звуки погони, которые постепенно затихают..."
    };
    
    private float[] sceneDurations = {
        3f, 2.5f, 4f, 3.5f, 4.5f, 2f, 4f, 3.5f, 3f, 4f, 3f, 3f, 3f, 2.5f, 3f
    };

    public Level1EndCutscene(NikitaGame game) {
        this.game = game;
        batch = new SpriteBatch();
        
        // Инициализация шрифта с поддержкой кириллицы
        try {
            // Пытаемся загрузить FreeType шрифт
            FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("Roboto-Regular.ttf"));
            FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
            parameter.size = 24;
            parameter.color = Color.WHITE;
            parameter.characters = FreeTypeFontGenerator.DEFAULT_CHARS + "абвгдеёжзийклмнопрстуфхцчшщъыьэюяАБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯ";
            font = generator.generateFont(parameter);
            generator.dispose();
            System.out.println("✅ FreeType шрифт загружен успешно");
        } catch (Exception e) {
            // Если не удалось загрузить FreeType шрифт, используем стандартный
            System.out.println("⚠️ Не удалось загрузить FreeType шрифт: " + e.getMessage());
            System.out.println("⚠️ Используем стандартный шрифт");
            font = new BitmapFont();
            font.setColor(Color.WHITE);
            font.getData().setScale(1.5f);
        }
        
        // Загрузка фонового изображения (если есть)
        try {
            background = new Texture("throne_room.png");
        } catch (Exception e) {
            background = null;
        }
        
        // Загрузка музыки
        try {
            music = Gdx.audio.newMusic(Gdx.files.internal("prologue_theme.mp3"));
            music.setLooping(true);
            music.setVolume(0.5f);
            music.play();
        } catch (Exception e) {
            music = null;
        }
    }

    @Override
    public void show() {}

    @Override
    public void render(float delta) {
        // Обновление таймера сцены
        sceneTimer += delta;
        
        // Проверка завершения текущей сцены
        if (sceneTimer >= sceneDurations[currentScene] && !sceneComplete) {
            sceneComplete = true;
            fadeOut = 0f;
        }
        
        // Обновление fade эффектов
        if (!sceneComplete) {
            fadeIn = Math.min(1f, fadeIn + delta * 2f);
        } else {
            fadeOut = Math.min(1f, fadeOut + delta * 2f);
            if (fadeOut >= 1f) {
                // Переход к следующей сцене
                currentScene++;
                if (currentScene >= scenes.length) {
                    // Катсцена завершена, переходим к следующему уровню
                    if (music != null) {
                        music.stop();
                        music.dispose();
                    }
                    System.out.println("🎬 Катсцена завершена! Переход к level_2.tmx");
                    game.setScreen(new GameScreen(game, "maps/level_2.tmx"));
                    return;
                }
                sceneTimer = 0f;
                sceneComplete = false;
                fadeIn = 0f;
            }
        }
        
        // Очистка экрана
        Gdx.gl.glClearColor(0.1f, 0.05f, 0.1f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        
        batch.begin();
        
        // Рендеринг фона
        if (background != null) {
            batch.setColor(1f, 1f, 1f, 0.3f);
            batch.draw(background, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            batch.setColor(1f, 1f, 1f, 1f);
        }
        
        // Рендеринг текста сцены
        if (currentScene < scenes.length) {
            String sceneText = scenes[currentScene];
            
            // Плавное появление текста
            float alpha = sceneComplete ? (1f - fadeOut) : fadeIn;
            font.setColor(1f, 1f, 1f, alpha);
            
            // Простой рендеринг текста без разбиения
            float yPos = Gdx.graphics.getHeight() * 0.6f;
            
            // Центрируем текст
            float textWidth = font.draw(batch, sceneText, 0, 0).width;
            float xPos = (Gdx.graphics.getWidth() - textWidth) / 2f;
            font.draw(batch, sceneText, xPos, yPos);
        }
        
        // Рендеринг прогресса катсцены
        if (fadeIn > 0.5f && !sceneComplete) {
            String progress = "Сцена " + (currentScene + 1) + " из " + scenes.length;
            font.setColor(1f, 1f, 1f, 0.7f);
            float progressWidth = font.draw(batch, progress, 0, 0).width;
            float progressX = (Gdx.graphics.getWidth() - progressWidth) / 2f;
            font.draw(batch, progress, progressX, Gdx.graphics.getHeight() - 50f);
        }
        
        // Рендеринг подсказки
        if (fadeIn > 0.5f && !sceneComplete) {
            font.setColor(1f, 1f, 1f, MathUtils.sin(sceneTimer * 3f) * 0.5f + 0.5f);
            String hint = "Нажмите ПРОБЕЛ для пропуска";
            float hintWidth = font.draw(batch, hint, 0, 0).width;
            float hintX = (Gdx.graphics.getWidth() - hintWidth) / 2f;
            font.draw(batch, hint, hintX, 100f);
        }
        
        batch.end();
        
        // Обработка ввода для пропуска катсцены
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            if (music != null) {
                music.stop();
                music.dispose();
            }
            System.out.println("🎬 Катсцена пропущена! Переход к level_2.tmx");
            game.setScreen(new GameScreen(game, "maps/level_2.tmx"));
        }
    }

    @Override
    public void resize(int width, int height) {}

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {}

    @Override
    public void dispose() {
        batch.dispose();
        font.dispose();
        if (background != null) {
            background.dispose();
        }
        if (music != null) {
            music.dispose();
        }
    }
} 