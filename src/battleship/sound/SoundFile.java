package battleship.sound;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

public final class SoundFile {
    private final String soundResource;

    public SoundFile(String resourceName) {
        if (resourceName == null) {
            throw new IllegalArgumentException("resourceName is null");
        }
        this.soundResource = resourceName;
    }

    public void play() throws LineUnavailableException, IOException, UnsupportedAudioFileException {
        try (
                InputStream resourceStream = this.getClass().getResourceAsStream(this.soundResource);
                BufferedInputStream bufferedStream = new BufferedInputStream(resourceStream);
                AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(bufferedStream)
        ) {
            final Clip clip = AudioSystem.getClip();
            clip.open(audioInputStream);
            clip.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP || event.getType() == LineEvent.Type.CLOSE) {
                    if (clip.isOpen()) {
                        clip.close();
                    }
                }
            });
            clip.start();
        }
    }

    @Override
    public String toString() {
        return "SoundFile [soundResource=" + this.soundResource + "]";
    }
}
