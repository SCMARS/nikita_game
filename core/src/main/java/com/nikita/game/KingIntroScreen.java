package com.nikita.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;

public class KingIntroScreen implements Screen {
    private NikitaGame game;
    private SpriteBatch batch;
    private Texture kingIntro;
    private BitmapFont font, nameFont;
    private float zoom = 1.0f;
    private float zoomTarget = 1.0f;
    private float zoomSpeed = 0.0f;
    private float fade = 0f;
    private boolean fading = false;
    private int dialogueIndex = 0;
    private float dialogueTimer = 0f;
    private float introAnim = 0f; // 0..1
    private boolean introDone = false;
    private Sound blip;
    private long blipId = -1;
    private int visibleChars = 0;
    private float charTimer = 0f;
    private GlyphLayout layout = new GlyphLayout();
    private int lastDialogueIndex = -1;
    private String[] dialogues = {
        "Андрей Шевчук: Они боятся меня… как и должны. Я дал им мир. Я дал им хлеб. Я дал им вечную тьму, чтобы они не знали страха перед смертью.",
        "",
        "Андрей Шевчук: Я обменял их души — на силу. Их дети... стали платой. Один за другим. И я не жалею.",
        "",
        "Андрей Шевчук: Пока живы другие короли — я не умру. Пока плачет мать — я правлю. Пока страдают дети — я бессмертен.",
        "",
        "Никита (шёпотом): Что… Что ты наделал?..",
        "",
        "Андрей Шевчук: Никита… Ты всегда была слишком любопытна.",
        "Андрей Шевчук: Выходи. Мы всё равно не можем скрывать это вечно.",
        "",
        "Стража: Взять её!"
    };
    private float phraseAlpha = 0f;

    public KingIntroScreen(NikitaGame game) {
        this.game = game;
        batch = new SpriteBatch();
        kingIntro = new Texture("king_intro.png");
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("Roboto-Regular.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 32;
        parameter.characters = FreeTypeFontGenerator.DEFAULT_CHARS + "АБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯабвгдеёжзийклмнопрстуфхцчшщъыьэюя";
        font = generator.generateFont(parameter);
        parameter.size = 36;
        parameter.color = Color.GOLD;
        nameFont = generator.generateFont(parameter);
        generator.dispose();
        // Звук для субтитров
        try { blip = Gdx.audio.newSound(Gdx.files.internal("music/bit_text_blip_high_pitch_style.wav")); } catch (Exception e) { blip = null; }
    }

    @Override
    public void show() {}

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0,0,0,1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Анимация появления (fade-in + zoom-in)
        if (!introDone) {
            introAnim += delta/1.2f;
            if (introAnim >= 1f) { introAnim = 1f; introDone = true; }
        }
        float animFade = Math.min(1f, introAnim);
        float animZoom = 1.08f - 0.08f * animFade;

        batch.begin();
        int winW = Gdx.graphics.getWidth();
        int winH = Gdx.graphics.getHeight();
        int texW = kingIntro.getWidth();
        int texH = kingIntro.getHeight();
        float scale = Math.min((float)winW / texW, (float)winH / texH) * animZoom;
        float drawW = texW * scale;
        float drawH = texH * scale;
        float drawX = (winW - drawW) / 2f;
        float drawY = (winH - drawH) / 2f;
        batch.setColor(1,1,1,animFade);
        batch.draw(kingIntro, drawX, drawY, drawW, drawH);
        batch.setColor(1,1,1,1);

        // Диалоги (центрируем по ширине картинки, перенос строк)
        if (dialogueIndex < dialogues.length) {
            String text = dialogues[dialogueIndex];
            if (dialogueIndex != lastDialogueIndex) {
                phraseAlpha = 0f;
                visibleChars = 0;
                charTimer = 0f;
            }
            lastDialogueIndex = dialogueIndex;
            if (!text.isEmpty()) {
                String name = "";
                String phrase = text;
                if (text.startsWith("Андрей")) {
                    name = "Андрей Шевчук";
                    phrase = text.substring(text.indexOf(":")+1).trim();
                } else if (text.startsWith("Никита")) {
                    name = "Никита";
                    phrase = text.substring(text.indexOf(":")+1).trim();
                } else if (text.startsWith("Стража")) {
                    name = "Стража";
                    phrase = text.substring(text.indexOf(":")+1).trim();
                }
                float dialogAreaW = drawW * 0.8f;
                float dialogX = drawX + drawW/2f;
                float dialogY = drawY + 80;
                // Имя по центру
                if (!name.isEmpty()) {
                    layout.setText(nameFont, name);
                    nameFont.setColor(Color.GOLD);
                    nameFont.draw(batch, name, dialogX - layout.width/2f, dialogY + 40);
                }
                // Плавное появление текста по буквам
                float charsPerSec = 40f;
                charTimer += delta * charsPerSec;
                int targetChars = Math.min(phrase.length(), (int)charTimer);
                if (targetChars > visibleChars && blip != null && phraseAlpha > 0.1f && phrase.charAt(visibleChars) != ' ') {
                    blip.play(0.18f);
                }
                visibleChars = targetChars;
                String visiblePhrase = phrase.substring(0, visibleChars);
                phraseAlpha += delta * 2.5f;
                if (phraseAlpha > 1f) phraseAlpha = 1f;
                font.setColor((text.startsWith("Никита") ? Color.LIGHT_GRAY : Color.WHITE).cpy().lerp(Color.CLEAR, 1f-phraseAlpha));
                layout.setText(font, visiblePhrase, Color.WHITE, dialogAreaW, 1, true);
                font.draw(batch, layout, dialogX - dialogAreaW/2f, dialogY);
            }
            dialogueTimer += delta;
            if ((introDone && dialogueTimer > 5f && visibleChars == (dialogueIndex < dialogues.length ? dialogues[dialogueIndex].replaceAll(".*:","").trim().length() : 0)) || (introDone && (Gdx.input.isKeyJustPressed(Input.Keys.E) || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)))) {
                dialogueIndex++;
                dialogueTimer = 0f;
                phraseAlpha = 0f;
                visibleChars = 0;
                charTimer = 0f;
            }
        } else {
            fading = true;
        }

        // Fade out (тоже с fit-центрированием)
        if (fading) {
            fade += delta / 1.5f;
            if (fade > 1f) fade = 1f;
            batch.setColor(0,0,0,fade);
            batch.draw(kingIntro, drawX, drawY, drawW, drawH);
            batch.setColor(1,1,1,1);
            if (fade >= 1f) {
                game.setScreen(new GameScreen(game, "maps/level_0.tmx"));
            }
        }
        batch.end();
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
        kingIntro.dispose();
        font.dispose();
        nameFont.dispose();
    }
}
