package battleship;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;

import battleship.Game.BattleShip;
import battleship.sound.Sound;

/**
 * Eintrittspunkt für das Programm. Es speichert globale Einstellungen für das
 * Spiel, welche sich während der Laufzeit ändern können und stellt eine
 * Eintrittmethode (main) bereit, welche den Ton lädt und ein neues Spiel (in
 * Form der BattleShip-Klasse) startet.
 */
public final class Entrypoint {

    public static final AtomicBoolean sound;

    static {
        sound = new AtomicBoolean(true);
    }

    public static void main(String[] args) {
        for (final var handler : Logger.getLogger("").getHandlers()) {
            handler.setLevel(Constants.logLevel);
        }

        final Logger logger = Logger.getLogger(Entrypoint.class.getName());
        logger.setLevel(Constants.logLevel);

        logger.log(Level.INFO, "Starting game.");
        Sound.loadSound();
        SwingUtilities.invokeLater(() -> {
            final BattleShip g = new BattleShip();
            g.start();
        });
    }

}
