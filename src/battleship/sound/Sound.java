package battleship.sound;

import java.util.logging.Level;
import java.util.logging.Logger;

import battleship.Constants;

/**
 * Globale Klasse, welche für das Laden, Verwalten und Abspielen von Tönen und
 * ähnlichem zuständig ist.
 */
public final class Sound {
    /*
     * Die verschiedenen Töne:
     */
    private static SoundFile hit1;
    private static SoundFile hit2;
    private static SoundFile water;
    private static SoundFile victory;

    private static Logger logger;

    /**
     * Sound kann nicht instanziiert werden
     */
    private Sound() {
        throw new UnsupportedOperationException("Sound cannot be instantiated");
    }

    /**
     * Lädt den Sound, indem die Repräsentationen der einzelnen Töne erstellt
     * werden. In diesem Schritt selbst wird keine Datei geladen.
     */
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

    /**
     * Spiele den Ton Hit 1
     */
    public static void playHit1() {
        play(hit1);
    }

    /**
     * Spiele den Ton Hit 2
     */
    public static void playHit2() {
        play(hit2);
    }

    /**
     * Spiele den Sieges-Ton
     */
    public static void playVictory() {
        play(victory);
    }

    /**
     * Spiele des Wasser-Ton
     */
    public static void playWater() {
        play(water);
    }

    /**
     * Spiele eine bestimmte Sound-Datei ab. Sollte dies fehlschlagen wird eine
     * Fehlermeldung geloggt. Es wird kein Fehler geworfen.
     *
     * @param sf Die Sound-Datei, welche abgespielt werden soll.
     */
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
