package com.nikita.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import java.util.HashMap;

public class SoundManager {
    private Music music;
    private HashMap<String, Sound> sounds = new HashMap<>();

    public void playMusic(String file, boolean looping) {
        if (music != null) music.stop();
        music = Gdx.audio.newMusic(Gdx.files.internal(file));
        music.setLooping(looping);
        music.play();
    }

    public void stopMusic() {
        if (music != null) music.stop();
    }

    public void playSound(String file) {
        Sound sound = sounds.get(file);
        if (sound == null) {
            sound = Gdx.audio.newSound(Gdx.files.internal(file));
            sounds.put(file, sound);
        }
        sound.play();
    }

    public void dispose() {
        if (music != null) music.dispose();
        for (Sound s : sounds.values()) s.dispose();
    }
} 