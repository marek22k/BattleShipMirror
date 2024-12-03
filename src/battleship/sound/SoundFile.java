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

/**
 * Repräsentiert eine Sound-Datei
 */
public final class SoundFile {
    /**
     * Pfad zur Sound-Resource
     */
    private final String soundResource;

    /**
     * Erstellt eine neue Repräsentation einer Sound-Datei.
     *
     * @param resourceName Pfad zur Sound-Resource
     */
    public SoundFile(String resourceName) {
        if (resourceName == null) {
            throw new IllegalArgumentException("resourceName is null");
        }
        this.soundResource = resourceName;
    }

    /**
     * Spielt den Sound ab.
     *
     * @throws LineUnavailableException
     * @throws IOException
     * @throws UnsupportedAudioFileException
     */
    public void play() throws LineUnavailableException, IOException, UnsupportedAudioFileException {
        try (
                InputStream resourceStream = this.getClass().getResourceAsStream(this.soundResource);
                BufferedInputStream bufferedStream = new BufferedInputStream(resourceStream);
                AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(bufferedStream)
        ) {
            final Clip clip = AudioSystem.getClip();
            clip.open(audioInputStream);
            clip.addLineListener(event -> {
                if (
                    (event.getType() == LineEvent.Type.STOP || event.getType() == LineEvent.Type.CLOSE) && clip.isOpen()
                ) {
                    clip.close();
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
