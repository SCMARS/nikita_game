package com.nikita.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.Input;

public class PrologueScreen implements Screen {
    private NikitaGame game;
    private SpriteBatch batch;
    private Texture throneRoom, nikita, king, gleb, diary;
    private DialogueSystem dialogue;
    private float fade = 0f;
    private int stage = 0;
    private boolean fading = false;
    private Music music;

    public PrologueScreen(NikitaGame game) {
        this.game = game;
        batch = new SpriteBatch();
        throneRoom = new Texture("throne_room.png");
        nikita = new Texture("nikita.png");
        king = new Texture("king.png");
        gleb = new Texture("gleb.png");
        diary = new Texture("diary.png");
        dialogue = new DialogueSystem();
        dialogue.startDialogue(new String[]{
            "Король: Дети приносятся в жертву для открытия врат вечности.",
            "Никита: Отец, это безумие! Я не позволю!",
            "(Никита бросает дневник мага к ногам короля)",
            "Глеб: Никита, сюда! Быстрее!",
            "(Маг Глеб телепортирует Никиту через тайный ход)",
            "(Гвардия бросается за Никитой, экран затемняется...)"
        });
        music = Gdx.audio.newMusic(Gdx.files.internal("prologue_theme.mp3"));
        music.setLooping(true);
        music.play();
    }

    @Override
    public void show() {}

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.1f, 0.05f, 0.1f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        batch.begin();
        batch.draw(throneRoom, 0, 0, 1280, 720);
        batch.draw(king, 900, 300, 128, 256);
        batch.draw(nikita, 600, 320, 96, 192);
        if (stage >= 2) batch.draw(diary, 800, 340, 48, 32);
        if (stage >= 3) batch.draw(gleb, 500, 320, 96, 192);
        dialogue.render(batch);
        if (fading) {
            fade += delta / 2f;
            if (fade > 1f) fade = 1f;
            batch.setColor(0, 0, 0, fade);
            batch.draw(throneRoom, 0, 0, 1280, 720);
            batch.setColor(1, 1, 1, 1);
            if (fade >= 1f) {
                music.stop();
                game.setScreen(new GameScreen(game, "maps/level_true.tmx"));
            }
        }
        batch.end();
        if (!dialogue.isActive() && !fading) {
            stage++;
            if (stage >= 6) fading = true;
            else dialogue.startDialogue(new String[]{
                "..."
            });
        }
        // Возможность пропустить кат-сцену на цифру 9
        if (!fading && Gdx.input.isKeyJustPressed(Input.Keys.NUM_9)) {
            fading = true;
            fade = 0f;
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
        throneRoom.dispose();
        nikita.dispose();
        king.dispose();
        gleb.dispose();
        diary.dispose();
        music.dispose();
    }
}
