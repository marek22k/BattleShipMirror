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
     * Initalisiert der Hauptfenster und einige für das Spiel notwendige Dinge.
     */
    public BattleShip() {
        this.currentGameLock = new Object();
        this.serverStatusLock = new Object();
        this.sound = new AtomicBoolean(true);
        this.mainwindow = new MainWindow(sound);
        this.currentLevel = 1;
        synchronized (this.serverStatusLock) {
            this.serverstatus = ServerStatus.STOPPED;
        }

        Logger.getLogger("").addHandler(this.mainwindow.getLogHandler());

        this.logger = Logger.getLogger(BattleShip.class.getName());
        this.logger.setLevel(Constants.logLevel);

        this.mainwindow.setConnectHandler((String hostname, int port) -> {
            if (this.hasNonBlankName()) {
                final Connection connection = Connection.connectTo(hostname, port);
                this.startGame(connection, false);
            } else {
                this.logger.log(Level.SEVERE, "Game cannot be started without a name.");
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(
                            null, "The game cannot be started without a name. Please enter a name.", "No name",
                            JOptionPane.ERROR_MESSAGE
                    );
                    this.mainwindow.enable(true);
                });
            }
        });

        this.mainwindow.setServerStartHandler(() -> {
            if (!this.hasNonBlankName()) {
                this.logger.log(Level.SEVERE, "Game cannot be started without a name.");
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(
                            null, "The game cannot be started without a name. Please enter a name.", "No name",
                            JOptionPane.ERROR_MESSAGE
                    );
                    this.mainwindow.stopServer();
                });
                return;
            }
            synchronized (this.serverStatusLock) {
                this.serverstatus = ServerStatus.RUNNING;
            }
            if (this.server != null) {
                this.mainwindow.stopServer();
            }

            final int port = Constants.SERVER_PORT;
            this.server = new Server(port);
            SwingUtilities.invokeLater(() -> this.mainwindow.updateServerPort(String.valueOf(port)));

            try {
                while (!Thread.currentThread().isInterrupted()) {
                    final Socket client = this.server.waitForClient();
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
                                    final Connection connection = new Connection(client);
                                    this.mainwindow.stopServer();
                                    this.startGame(connection, true);
                                } catch (final Exception e) {
                                    this.logger.log(Level.SEVERE, "Failed to accept client.", e);
                                }
                            }).start();
                        } else {
                            this.logger.log(Level.INFO, "Deny client.");
                            try {
                                client.close();
                            } catch (final Exception e) {
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

        this.mainwindow.setServerStopHandler(() -> {
            synchronized (this.serverStatusLock) {
                this.serverstatus = ServerStatus.STOPPED;
                if (this.server != null) {
                    this.server.stop();
                    this.server = null;
                }
            }
            SwingUtilities.invokeLater(() -> this.mainwindow.updateServerPort(""));
        });

        this.mainwindow.firstInit();
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
                    connection, isServer, this.mainwindow.getName(), selectedLevel, sound, (GameEndStatus status) -> {
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
