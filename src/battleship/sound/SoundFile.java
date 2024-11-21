package battleship.sound;

import java.io.IOException;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import battleship.Entrypoint;

public final class SoundFile {
    private final String soundResource;

    public SoundFile(String resourceName) {
        this.soundResource = resourceName;
    }

    public void play() throws LineUnavailableException, IOException, UnsupportedAudioFileException {
        try (AudioInputStream audioInputStream = AudioSystem
                .getAudioInputStream(Entrypoint.class.getResourceAsStream(this.soundResource))) {
            final Clip clip = AudioSystem.getClip();
            clip.open(audioInputStream);
            clip.start();
        }
    }
}
