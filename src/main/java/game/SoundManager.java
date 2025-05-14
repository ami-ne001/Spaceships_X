package game;

import javax.sound.sampled.*;
import java.io.IOException;
import java.net.URL;

public class SoundManager {
    public static final int BACKGROUND_MUSIC = 0;
    public static final int SHOOT_SOUND = 1;
    public static final int EXPLOSION_SOUND = 2;
    public static final int HIT_SOUND = 3;

    private Clip[] sounds;
    private Clip backgroundMusic;
    private boolean musicOn = true;
    private boolean soundOn = true;

    public SoundManager() {
        sounds = new Clip[4];
        loadSounds();
    }

    private void loadSounds() {
        try {
            // Load background music
            URL url = getClass().getResource("/sound/bgmusic.wav");
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(url);
            backgroundMusic = AudioSystem.getClip();
            backgroundMusic.open(audioIn);

            // Load shoot sound
            url = getClass().getResource("/sound/shoot.wav");
            audioIn = AudioSystem.getAudioInputStream(url);
            sounds[SHOOT_SOUND] = AudioSystem.getClip();
            sounds[SHOOT_SOUND].open(audioIn);

            // Load explosion sound
            url = getClass().getResource("/sound/explosion.wav");
            audioIn = AudioSystem.getAudioInputStream(url);
            sounds[EXPLOSION_SOUND] = AudioSystem.getClip();
            sounds[EXPLOSION_SOUND].open(audioIn);

            // Load hit sound
            url = getClass().getResource("/sound/hit.wav");
            audioIn = AudioSystem.getAudioInputStream(url);
            sounds[HIT_SOUND] = AudioSystem.getClip();
            sounds[HIT_SOUND].open(audioIn);
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    public void playBackgroundMusic() {
        if (musicOn) {
            if (backgroundMusic != null) {
                backgroundMusic.loop(Clip.LOOP_CONTINUOUSLY);
                backgroundMusic.start();
            }
        }
    }

    public void stopBackgroundMusic() {
        if (backgroundMusic != null) {
            backgroundMusic.stop();
        }
    }

    public void playSound(int soundType) {
        if (soundOn && sounds[soundType] != null) {
            sounds[soundType].setFramePosition(0);
            sounds[soundType].start();
        }
    }
    
    public void playGameOverSound() {
        stopBackgroundMusic();
        playSound(EXPLOSION_SOUND);
    }

    public void toggleMusic() {
        musicOn = !musicOn;
        if (musicOn) {
            playBackgroundMusic();
        } else {
            stopBackgroundMusic();
        }
    }

    public void toggleSound() { soundOn = !soundOn; }
}