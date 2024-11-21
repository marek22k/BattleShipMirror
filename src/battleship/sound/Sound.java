package battleship.sound;

import java.util.logging.Level;
import java.util.logging.Logger;

import battleship.Constants;
import battleship.Entrypoint;

public final class Sound {
    private static SoundFile hit1;
    private static SoundFile hit2;
    private static SoundFile water;
    private static SoundFile victory;
    private static Logger logger;

    public static void loadSound() {
        logger = Logger.getLogger(Sound.class.getName());
        logger.setLevel(Constants.logLevel);
        if (Constants.SOUND) {
            try {
                logger.log(Level.FINE, "Load sound.");
                water = new SoundFile(Constants.waterFilename);
                hit1 = new SoundFile(Constants.hit1Filename);
                hit2 = new SoundFile(Constants.hit2Filename);
                victory = new SoundFile(Constants.victoryFilename);
            } catch (final Exception e) {
                logger.log(Level.SEVERE, "Failed to load sound.", e);
            }
        }
    }

    public static void playHit1() {
        play(hit1);
    }

    public static void playHit2() {
        play(hit2);
    }

    public static void playVictory() {
        play(victory);
    }

    public static void playWater() {
        play(water);
    }

    private static void play(SoundFile sf) {
        if (Constants.SOUND && Entrypoint.sound.get()) {
            try {
                sf.play();
            } catch (final Exception e) {
                logger.log(Level.SEVERE, "Failed to play sound.", e);
            }
        }
    }
}
