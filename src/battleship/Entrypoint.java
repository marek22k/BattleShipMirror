package battleship;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;

import battleship.game.BattleShip;
import battleship.sound.Sound;

/**
 * Eintrittspunkt für das Programm. Es stellt eine Eintrittmethode (main)
 * bereit, welche den Ton lädt und ein neues Spiel (in Form der
 * BattleShip-Klasse) startet.
 */
public final class Entrypoint {
    private Entrypoint() {
        throw new UnsupportedOperationException("Entrypoint cannot be instantiated");
    }

    public static void main(String[] args) {
        /* Setzen des Log-Levels für alle Log-Handler */
        for (final Handler handler : Logger.getLogger("").getHandlers()) {
            handler.setLevel(Constants.LOG_LEVEL);
        }

        final Logger logger = Logger.getLogger(Entrypoint.class.getName());
        logger.setLevel(Constants.LOG_LEVEL);

        logger.log(Level.INFO, "Starting game.");
        Sound.loadSound();
        SwingUtilities.invokeLater(() -> {
            final BattleShip g = new BattleShip();
            g.start();
        });
    }
}
