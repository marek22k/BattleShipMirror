package battleship.sound;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

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
        try (
                InputStream resourceStream = Entrypoint.class.getResourceAsStream(this.soundResource);
                BufferedInputStream bufferedStream = new BufferedInputStream(resourceStream);
                AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(bufferedStream)
        ) {
            final Clip clip = AudioSystem.getClip();
            clip.open(audioInputStream);
            clip.start();
        }
    }
}
