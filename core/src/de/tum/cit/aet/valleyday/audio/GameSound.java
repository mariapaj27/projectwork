package de.tum.cit.aet.valleyday.audio;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.utils.Timer;

/**
 * Sound effects used in the game.
 */
public enum GameSound {

    DIG("dig.mp3"),
    WIN("win.wav"),
    LOSE("lose.mp3"),
    PICK("itempick.wav"),
    ROAR("roar.wav");

    private final Sound sound;

    GameSound(String fileName) {
        this.sound = Gdx.audio.newSound(Gdx.files.internal("audio/" + fileName));
    }

    public void play() {
        this.sound.play();
    }

    /**
     * Play the sound on repeat(bc short).
     * @param times Number of times to play.
     * @param delaySeconds Delay in seconds between each play.
     */
    public void playRepeated(int times, float delaySeconds) {
        for (int i = 0; i < times; i++) {
            float delay = i * delaySeconds;
            Timer.schedule(new Timer.Task() {
                @Override
                public void run() {
                    sound.play();
                }
            }, delay);
        }
    }
}

