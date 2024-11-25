package battleship.sound;

import java.util.logging.Level;
import java.util.logging.Logger;

import battleship.Constants;

public final class Sound {
    private static SoundFile hit1;
    private static SoundFile hit2;
    private static SoundFile water;
    private static SoundFile victory;
    private static Logger logger;

    private Sound() {
        throw new UnsupportedOperationException("Sound cannot be instantiated");
    }

    public static void loadSound() {
        logger = Logger.getLogger(Sound.class.getName());
        logger.setLevel(Constants.logLevel);
        if (Constants.SOUND) {
            try {
                logger.log(Level.FINE, "Load sound.");
                water = new SoundFile(Constants.WATER_RESOURCE);
                hit1 = new SoundFile(Constants.HIT1_RESOURCE);
                hit2 = new SoundFile(Constants.HIT2_RESOURCE);
                victory = new SoundFile(Constants.VICTORY_RESOURCE);
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
        if (Constants.SOUND) {
            try {
                sf.play();
            } catch (final Exception e) {
                logger.log(Level.SEVERE, "Failed to play sound: " + sf.toString(), e);
            }
        }
    }
}
