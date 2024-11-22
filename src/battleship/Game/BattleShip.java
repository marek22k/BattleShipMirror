package battleship.Game;

import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import battleship.Constants;
import battleship.network.Connection;
import battleship.network.Server;
import battleship.sound.Sound;
import battleship.ui.MainWindow.MainWindow;

/**
 * Stellt das gesamte Spiel (und damit auch das Hauptfenster) in einer Klasse
 * da.
 */
public final class BattleShip {
    private int currentLevel;
    public final AtomicBoolean sound;
    private MainWindow mainwindow;
    private GameSession currentGame;
    private final Object currentGameLock;
    private Server server;
    private ServerStatus serverstatus;
    private final Object serverStatusLock;
    private final Logger logger;

    /**
     * Initialisiert der Hauptfenster und einige für das Spiel notwendige Dinge.
     */
    public BattleShip() {
        /* Anlegen von Lock Objekten */
        this.currentGameLock = new Object();
        this.serverStatusLock = new Object();
        /* Setzen der Sound-Einstellung */
        this.sound = new AtomicBoolean(true);
        /*
         * Erstellen des Hauptfensters und Verknüpfung mit der Sound-Einstellung
         * herstellen
         */
        this.mainwindow = new MainWindow(this.sound);
        this.currentLevel = 1;
        synchronized (this.serverStatusLock) {
            this.serverstatus = ServerStatus.STOPPED;
        }

        /* Einrichten der Ausgabe des Logs im Hauptfenster */
        Logger.getLogger("").addHandler(this.mainwindow.getLogHandler());

        /* Logger erstellen */
        this.logger = Logger.getLogger(BattleShip.class.getName());
        this.logger.setLevel(Constants.logLevel);

        /*
         * Handler anlegen, welcher aufgerufen wird, wenn der "Connect"-Button beim
         * Client geklickt wird
         */
        this.mainwindow.setConnectHandler((String hostname, int port) -> {
            /* Überprüfe Startbedingungen */
            if (!this.checkStartConditions()) {
                SwingUtilities.invokeLater(() -> this.mainwindow.enable(true));
                return;
            }

            /* Anlegen einer Verbindung zum Server */
            final Connection connection = Connection.connectTo(hostname, port);
            /* Starten der Spiele-Sitzung */
            this.startGame(connection, false);
        });

        /*
         * Handler anlegen, welcher aufgerufen wird, wenn der Server gestartet werden
         * soll
         */
        this.mainwindow.setServerStartHandler((int port) -> {
            /* Überprüfe Startbedingungen */
            if (!this.checkStartConditions()) {
                this.mainwindow.stopServer();
                return;
            }
            synchronized (this.serverStatusLock) {
                this.serverstatus = ServerStatus.RUNNING;
            }
            /*
             * Wenn der letzte Server nicht richtig beendet wurden ist, führe diesen Schritt
             * nun aus
             */
            if (this.server != null) {
                logger.log(
                        Level.WARNING,
                        "Server is started although the last server was not terminated correctly (reference still found). Cleaning this up."
                );
                this.mainwindow.stopServer();
            }

            /* Erstellen des eigentlichen Servers */
            this.server = new Server(port);

            /*
             * Versuche so lange wie möglich (-> bis eine Ausnahme auftritt) auf einen
             * Client zu warten
             */
            try {
                /* Warte solange auf Clients, solange der Server-Thread läuft */
                while (!Thread.currentThread().isInterrupted()) {
                    /* Diese Operation blockiert */
                    final Socket client = this.server.waitForClient();
                    /*
                     * Es soll gefragt werden, ob die Verbindungs-Anfrage des Client angenommen
                     * werden soll. Er wird gewartet bis der Nutzer antwortet, damit es bei einer
                     * Client-Flut kein Popup-Terror gibt.
                     */
                    SwingUtilities.invokeAndWait(() -> {
                        final String clientQuestion = client.getInetAddress().getHostName() + ":" + client.getPort()
                                + " (" + client.getInetAddress().getHostAddress() + ") -> "
                                + client.getLocalAddress().getHostName() + ":" + client.getLocalPort() + " ("
                                + client.getLocalAddress().getHostAddress() + ")";
                        this.logger.log(Level.INFO, "Incoming connection from client: " + clientQuestion);
                        final int option = JOptionPane.showConfirmDialog(
                                null, "Accept client? " + clientQuestion, "Accept client?", JOptionPane.YES_NO_OPTION
                        );
                        if (option == JOptionPane.YES_OPTION) {
                            new Thread(() -> {
                                try {
                                    /*
                                     * Wenn der Nutzer den Client annimmt, soll die Verbindung in ein
                                     * Schiffeversenken-Verbindung "gewrappt" werden, der server beendet und
                                     * anschließend die Spiele-Sitzung gestartet werden.
                                     */
                                    final Connection connection = new Connection(client);
                                    this.mainwindow.stopServer();
                                    this.startGame(connection, true);
                                } catch (final Exception e) {
                                    this.logger.log(Level.SEVERE, "Failed to accept client.", e);
                                }
                            }).start();
                        } else {
                            /*
                             * Wenn die Verbindung mit dem Client abgelehnt wird, soll die Verbindung
                             * richtig zugemacht werden
                             */
                            this.logger.log(Level.INFO, "Deny client.");
                            try {
                                client.close();
                            } catch (final Exception e) {
                                /*
                                 * Wenn dies scheitert, ist das zwar doof, aber nicht "unser Problem" -> der
                                 * Client wartet dann Ewigkeiten auf den Server. Dies betrifft aber nicht unsere
                                 * Operationen, sodass wir auf den nächsten Client warten können.
                                 */
                                this.logger.log(Level.SEVERE, "Failed to deny client.", e);
                            }
                        }
                    });
                }
            } catch (final SocketException e) {
                synchronized (this.serverStatusLock) {
                    if (this.serverstatus != ServerStatus.STOPPED) {
                        this.logger.log(Level.SEVERE, "Error when running the server.", e);
                        this.mainwindow.stopServer();
                    }
                }
            }
        });

        /* Handler, welcher ausgeführt wird, wenn der Server gestoppt werden soll */
        this.mainwindow.setServerStopHandler(() -> {
            synchronized (this.serverStatusLock) {
                this.serverstatus = ServerStatus.STOPPED;
                if (this.server != null) {
                    this.server.stop();
                    this.server = null;
                }
            }
        });

        this.mainwindow.firstInit();
    }

    /**
     * Überprüft, ob alles notwendige vorliegt um das Spiel zu starten. Bei dem
     * Notwendigen handelt es sich um den Namen des Spielers.
     *
     * @return true, wenn alles Notwendige vorliegt, sonst false.
     */
    public boolean checkStartConditions() {
        if (this.hasNonBlankName()) {
            return true;
        }

        this.logger.log(Level.SEVERE, "Game cannot be started without a name.");
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(
                    null, "The game cannot be started without a name. Please enter a name.", "No name",
                    JOptionPane.ERROR_MESSAGE
            );
        });
        return false;
    }

    /**
     * Überprüft, ob der eingegebene Spielername nicht leer ist.
     *
     * @return Gibt true zurück, wenn der Spielername nicht leer ist, sonst false.
     */
    public boolean hasNonBlankName() {
        return !this.mainwindow.getName().isBlank();
    }

    /**
     * Startet das Spiel, indem das Hauptfenster angezeigt wird
     */
    public void start() {
        this.mainwindow.show();
    }

    /**
     * Startet eine Spielesitzung mit einem Peer
     *
     * @param connection Die Verbindung zunm Peer
     * @param isServer   Gibt an, ob wir beim Verbindungsaufbau als Server agiert
     *                   haben. Dies ist notwendig zu wissen, um zu Entscheiden, wer
     *                   anfängt.
     * @throws Exception
     */
    private void startGame(Connection connection, boolean isServer) throws Exception {
        this.mainwindow.enable(false);
        final int selectedLevel = this.mainwindow.getSelectedLevel();
        synchronized (this.currentGameLock) {
            synchronized (this.serverStatusLock) {
                if (this.serverstatus != ServerStatus.STOPPED) {
                    this.logger.log(Level.SEVERE, "The game cannot be started if the server is still running.");
                    return;
                }
            }
            if (this.currentGame != null) {
                this.logger.log(Level.SEVERE, "One game is already running.");
                SwingUtilities.invokeLater(
                        () -> JOptionPane.showMessageDialog(
                                null, "A new game is to be launched, but one is currently still running.",
                                "One game is already running.", JOptionPane.ERROR_MESSAGE
                        )
                );
                return;
            }
            this.currentGame = new GameSession(
                    connection, isServer, this.mainwindow.getName(), selectedLevel, this.sound,
                    (GameEndStatus status) -> {
                        this.logger.log(Level.FINE, "In game exit handler, status=" + status);
                        switch (status) {
                            case SUCCESSFUL_WON:
                                Sound.playVictory();
                                SwingUtilities.invokeLater(
                                        () -> JOptionPane.showMessageDialog(
                                                null, "You have won! Congratulations!", "You won!",
                                                JOptionPane.INFORMATION_MESSAGE
                                        )
                                );
                                break;

                            case SUCCESSFUL_DRAW_FROM_PEER:
                                Sound.playVictory();
                                SwingUtilities.invokeLater(
                                        () -> JOptionPane.showMessageDialog(
                                                null, "You have won because your opponent has withdrawn.", "You won!",
                                                JOptionPane.INFORMATION_MESSAGE
                                        )
                                );
                                break;

                            case CONNECTION_DISCONNECTED_OR_DISTURBED:
                                SwingUtilities.invokeLater(
                                        () -> JOptionPane.showMessageDialog(
                                                null, "There was a connection error. The game was therefore ended.",
                                                "Connection error", JOptionPane.INFORMATION_MESSAGE
                                        )
                                );
                                break;

                            case GAME_PREPARATION_OR_START_FAILED:
                                SwingUtilities.invokeLater(
                                        () -> JOptionPane.showMessageDialog(
                                                null,
                                                "An error has occurred while preparing or starting the game. The game could therefore not be started and was ended prematurely.",
                                                "Error at game start", JOptionPane.INFORMATION_MESSAGE
                                        )
                                );
                                break;

                            case SUCCESSFUL_DRAW_FROM_PLAYER:
                                SwingUtilities.invokeLater(
                                        () -> JOptionPane.showMessageDialog(
                                                null, "You have withdrawn!", "You have withdrawn!",
                                                JOptionPane.INFORMATION_MESSAGE
                                        )
                                );
                                break;

                            case SUCCESSFUL_LOST:
                                SwingUtilities.invokeLater(
                                        () -> JOptionPane.showMessageDialog(
                                                null, "You have lost!", "You have lost!",
                                                JOptionPane.INFORMATION_MESSAGE
                                        )
                                );
                                break;

                            default:
                                this.logger.log(Level.SEVERE, "Game ended for unknown reason: " + status);
                                SwingUtilities.invokeLater(
                                        () -> JOptionPane.showMessageDialog(
                                                null,
                                                "The game was ended for an unknown reason (" + status
                                                        + "). Please report this error.",
                                                "Error", JOptionPane.ERROR_MESSAGE
                                        )
                                );
                                break;
                        }
                        if (
                            status == GameEndStatus.SUCCESSFUL_WON || status == GameEndStatus.SUCCESSFUL_DRAW_FROM_PEER
                        ) {
                            this.logger.log(Level.INFO, "Level up!");
                            this.currentLevel = Math
                                    .min(Math.max(selectedLevel + 1, this.currentLevel), Constants.NUMBER_OF_LEVELS);
                            this.logger.log(Level.INFO, "New level: " + this.currentLevel);
                            SwingUtilities.invokeLater(() -> this.mainwindow.updateLevels(this.currentLevel));
                        }
                        this.mainwindow.enable(true);
                        this.currentGame = null;
                    }
            );
        }
        this.currentGame.begin();
    }
}
