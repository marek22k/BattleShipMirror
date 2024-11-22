package battleship.Game;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import battleship.Constants;
import battleship.OpposingThings.OpposingField;
import battleship.OpposingThings.OpposingFieldStatus;
import battleship.OpposingThings.OpposingPlayingField;
import battleship.PlayersThings.PlayersField;
import battleship.PlayersThings.PlayersPlayingField;
import battleship.PlayersThings.PlayersShip;
import battleship.network.Connection;
import battleship.network.ConnectionEvent;
import battleship.network.commands.Hit;
import battleship.network.commands.HitStatus;
import battleship.network.commands.Shoot;
import battleship.sound.Sound;
import battleship.ui.GameWindow.GameWindow;
import battleship.ui.PlaygroundMatrix.FireEvent;
import battleship.utils.Utils;

/**
 * Repräsentiert eine Spiele-Sitzung mit entsprechenden Attributen und dem
 * Spiele-Fenster.
 *
 * X- und Y-Koordinaten werden intern mit Zahlen beginnend bei 0 dargestellt.
 * Für die Kommunikation mit dem Gegner wird dies umgewandelt.
 */
public final class GameSession {
    private String myCoin;
    private final String playersName;
    private final int playersLevel;
    private final AtomicBoolean sound;
    private OpposingPlayingField opposing;
    private OpposingField lastShoot;
    private PlayersPlayingField players;
    private GameWindow gamewindow;
    private TurnStatus turnstatus;
    private final Object turnLock;
    private Thread readThread;
    private Connection connection;
    private final boolean isServer;
    private final AtomicBoolean isRunning;
    private final GameExitHandler gameexithandler;
    private final Logger logger;
    private static Random random;

    static {
        random = new Random();
    }

    /**
     * Generierte eine zufällige Münze.
     *
     * @return "0" oder "1" als String
     */
    private static String getCoin() {
        return random.nextBoolean() ? "0" : "1";
    }

    /**
     * Initialisiert eine Spiele-Sitzung
     *
     * @param connection      Verbindung zum Gegner
     * @param isServer        Angabe darüber, ob die Verbindung mit uns als Server
     *                        hergestellt wurden ist
     * @param playersName     Name unseres Spielers
     * @param level           Level unseres Spielers
     * @param gameexithandler Benutzerdefinierte Funktion, welche nach Spielende
     *                        aufgerufen werden soll
     */
    public GameSession(
            Connection connection, boolean isServer, String playersName, int level, AtomicBoolean sound,
            GameExitHandler gameexithandler
    ) {
        this.isRunning = new AtomicBoolean(true);
        this.turnLock = new Object();
        this.sound = sound;
        this.connection = connection;
        this.playersName = Utils.sanitizeString(playersName);
        this.isServer = isServer;
        this.playersLevel = level;
        this.myCoin = getCoin();
        this.logger = Logger.getLogger(GameSession.class.getName());
        this.logger.setLevel(Constants.logLevel);
        synchronized (this.turnLock) {
            this.turnstatus = TurnStatus.NOT_READY;
        }
        this.gameexithandler = gameexithandler;
    }

    /**
     * Beginnt den Handshake mit dem Gegner und legt verschiedene Verbindungshandler
     * fest. Setzt des Runden-Status auf "Nicht bereit". Diese Funktion sollte
     * direkt nach dem Erstellen des Objektes aufgerufen werden.
     */
    public void begin() {
        synchronized (this.turnLock) {
            this.logger.log(Level.FINE, "Determine game information.");

            this.initGame();
        }
    }

    /**
     * Greift einen Gegner an. Dies funktioniert nur, wenn wir an der Reihe sind.
     * Verändert den Status zu "Warte auf Antwort nach Angriff".
     *
     * @param x X-Koordinate des Feldes, welches angegriffen werden soll
     * @param y Y-Koordinate des Feldes, welches angegriffen werden soll
     */
    private void attackOpponent(int x, int y) {
        synchronized (this.turnLock) {
            switch (this.turnstatus) {
                case MY_TURN:
                case MY_TURN_AFTER_HIT:
                    this.logger.log(Level.INFO, "Attack opponent at x=" + x + " y=" + y);
                    if (this.opposing.isUnknown(new OpposingField(x, y))) {
                        this.turnstatus = TurnStatus.WAITING_FOR_REPLY_AFTER_HIT;
                        SwingUtilities.invokeLater(() -> {
                            this.gamewindow.playersTurn(false);
                        });
                        this.lastShoot = new OpposingField(x, y);
                        try {
                            this.connection.writeShoot(x, y);
                        } catch (final IOException e) {
                            this.logger.log(Level.SEVERE, "Error when sending the attack.", e);
                            SwingUtilities.invokeLater(() -> {
                                JOptionPane.showMessageDialog(
                                        null, "The game is not ready yet. Please try again in a few moments.",
                                        "Not ready to attack", JOptionPane.INFORMATION_MESSAGE
                                );
                            });
                            this.turnstatus = TurnStatus.MY_TURN;
                            SwingUtilities.invokeLater(() -> {
                                this.gamewindow.playersTurn(true);
                            });
                        }
                    } else {
                        this.logger.log(Level.INFO, "Field is not attacked because it has already been attacked once.");
                    }
                    break;

                case NOT_READY:
                    this.logger.log(Level.INFO, "Not ready to attack.");
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(
                                null, "The game is not ready yet. Please try again in a few moments.",
                                "Not ready to attack", JOptionPane.INFORMATION_MESSAGE
                        );
                    });
                    break;

                default:
                    break;
            }
        }
    }

    /**
     * Verändert den Runden-Status und bewirkt entsprechende Veränderungen in der
     * GUI.
     *
     * @param turnstatus Neuer Runden-Status
     */
    private void changeTurn(TurnStatus turnstatus) {
        synchronized (this.turnLock) {
            this.turnstatus = turnstatus;
            switch (turnstatus) {
                case MY_TURN:
                case MY_TURN_AFTER_HIT:
                    SwingUtilities.invokeLater(() -> {
                        this.gamewindow.playersTurn(true);
                    });
                    break;

                case NOT_READY:
                case PREPARED:
                    break;

                case YOUR_TURN:
                case YOUR_TURN_AFTER_HIT:
                case WAITING_FOR_REPLY_AFTER_HIT:
                    SwingUtilities.invokeLater(() -> {
                        this.gamewindow.playersTurn(false);
                    });
            }
        }
    }

    /**
     * Siehe begin()
     */
    private void initGame() {
        try {
            synchronized (this.turnLock) {
                this.logger.log(Level.FINE, "Our coin: " + this.myCoin);
                this.connection.setEventHandler((ConnectionEvent event, Object eventObject) -> {
                    this.logger.log(Level.FINE, "Event received: " + event);
                    switch (event) {
                        case CHAT_COMMAND_INVALID:
                            break;

                        case CHAT_COMMAND_NOT_WELL_STRUCTURED:
                            SwingUtilities.invokeLater(() -> {
                                JOptionPane.showMessageDialog(
                                        null,
                                        "The chat command from the peer does not seem to be well structured. This may indicate a faulty implementation of this instance or the instance of the peer. This is somewhat worrying.",
                                        "Chat command not well structured", JOptionPane.WARNING_MESSAGE
                                );
                            });
                            break;

                        case CHAT_COMMAND_RECEIVED:
                            if (this.isReady()) {
                                final String message = (String) eventObject;
                                SwingUtilities.invokeLater(() -> {
                                    String peerName = this.connection.getPeersUnicodeName();
                                    if (peerName == null) {
                                        peerName = this.connection.getPeersName();
                                    }
                                    this.gamewindow.writeMessageFromPeer(peerName, message);
                                });
                            }
                            break;

                        case CHAT_COMMAND_SENT:
                            break;

                        case COIN_COMMAND_NOT_WELL_STRUCTURED:
                            SwingUtilities.invokeLater(() -> {
                                JOptionPane.showMessageDialog(
                                        null,
                                        "The coin command from the peer does not seem to be well structured. This may indicate a faulty implementation of this instance or the instance of the peer. This is somewhat worrying.",
                                        "Coin command not well structured", JOptionPane.WARNING_MESSAGE
                                );
                            });
                            break;

                        case COIN_COMMAND_SENT:
                            break;

                        case COIN_COMMAND_RECEIVED:
                            if (this.isPrepared()) {
                                final String peersCoin = (String) eventObject;
                                this.startGame(peersCoin);
                            }
                            break;

                        case NULL_LINE_RECEIVED:
                        case DISCONNECTED_WHILE_WAITING_FOR_COMMAND:
                            synchronized (this.turnLock) {
                                if (this.isRunning.get()) {
                                    this.logger.log(
                                            Level.SEVERE,
                                            "Disconnected while waiting for command or null line received. The current game will be ended."
                                    );
                                }
                                this.stopGame(GameEndStatus.CONNECTION_DISCONNECTED_OR_DISTURBED);
                            }
                            break;

                        case HIT_COMMAND_NOT_WELL_STRUCTURED:
                            SwingUtilities.invokeLater(() -> {
                                JOptionPane.showMessageDialog(
                                        null,
                                        "The hit command from the peer does not seem to be well structured. This may indicate a faulty implementation of this instance or the instance of the peer. This is somewhat worrying.",
                                        "Hit command not well structured", JOptionPane.WARNING_MESSAGE
                                );
                            });
                            break;

                        case HIT_COMMAND_RECEIVED:
                            if (this.isReady()) {
                                final Hit hit = (Hit) eventObject;
                                this.receiveAnswerToOwnAttack(hit.getX(), hit.getY(), hit.getHitStatus());
                            }
                            break;

                        case HIT_COMMAND_SENT:
                            break;

                        case IAMU_COMMAND_INVALID:
                            break;

                        case IAMU_COMMAND_NOT_WELL_STRUCTURED:
                            SwingUtilities.invokeLater(() -> {
                                JOptionPane.showMessageDialog(
                                        null,
                                        "The IAMU command from the peer does not seem to be well structured. This may indicate a faulty implementation of this instance or the instance of the peer. This is somewhat worrying.",
                                        "IAMU command not well structured", JOptionPane.WARNING_MESSAGE
                                );
                            });
                            break;

                        case IAMU_COMMAND_RECEIVED:
                            if (this.isPrepared()) {
                                if (!this.connection.getPeersName().equals(this.connection.getPeersUnicodeName())) {
                                    SwingUtilities.invokeLater(() -> {
                                        this.gamewindow.writeMessageFromSystem(
                                                this.connection.getPeersUnicodeName() + " (peer) has joined the game."
                                        );
                                    });
                                }
                            }
                            break;

                        case IAMU_COMMAND_SENT:
                            break;

                        case IAM_COMMAND_NOT_WELL_STRUCTURED:
                            SwingUtilities.invokeLater(() -> {
                                JOptionPane.showMessageDialog(
                                        null,
                                        "The IAM command from the peer does not seem to be well structured. This may indicate a faulty implementation of this instance or the instance of the peer. This is somewhat worrying.",
                                        "IAM command not well structured", JOptionPane.WARNING_MESSAGE
                                );
                            });
                            break;

                        case IAM_COMMAND_RECEIVED:
                            this.prepareGame();
                            break;

                        case IAM_COMMAND_SENT:
                            break;

                        case SHOOT_COMMAND_NOT_WELL_STRUCTURED:
                            SwingUtilities.invokeLater(() -> {
                                JOptionPane.showMessageDialog(
                                        null,
                                        "The shoot command from the peer does not seem to be well structured. This may indicate a faulty implementation of this instance or the instance of the peer. This is somewhat worrying.",
                                        "Shoot command not well structured", JOptionPane.WARNING_MESSAGE
                                );
                            });
                            break;

                        case SHOOT_COMMAND_RECEIVED:
                            if (this.isReady()) {
                                final Shoot shoot = (Shoot) eventObject;
                                this.receiveAttack(shoot.getX(), shoot.getY());
                            }
                            break;

                        case SHOOT_COMMAND_SENT:
                            break;

                        case UNKNOWN_COMMAND_RECEIVED:
                            break;

                        case VERSION_COMMAND_NOT_WELL_STRUCTURED:
                            SwingUtilities.invokeLater(() -> {
                                JOptionPane.showMessageDialog(
                                        null,
                                        "The version command from the peer does not seem to be well structured. This may indicate a faulty implementation of this instance or the instance of the peer. This is somewhat worrying.",
                                        "Version command not well structured", JOptionPane.WARNING_MESSAGE
                                );
                            });
                            break;

                        case VERSION_COMMAND_RECEIVED:
                            break;

                        case VERSION_COMMAND_SENT:
                            break;

                        case WITHDRAW_COMMAND_NOT_WELL_STRUCTURED:
                            SwingUtilities.invokeLater(() -> {
                                JOptionPane.showMessageDialog(
                                        null,
                                        "The withdraw command from the peer does not seem to be well structured. This may indicate a faulty implementation of this instance or the instance of the peer. This is somewhat worrying.",
                                        "Withdraw command not well structured", JOptionPane.WARNING_MESSAGE
                                );
                            });
                            break;

                        case WITHDRAW_COMMAND_RECEIVED:
                            this.stopGame(GameEndStatus.SUCCESSFUL_DRAW_FROM_PEER);
                            break;

                        case WITHDRAW_COMMAND_SENT:
                            break;

                        default:
                            this.logger.log(
                                    Level.SEVERE,
                                    "Unknown event triggered: " + event + "; eventObject null? " + (eventObject == null)
                            );
                            SwingUtilities.invokeLater(() -> {
                                JOptionPane.showMessageDialog(
                                        null,
                                        "An unknown event has been triggered. This is a bug in the implementation. Please report this error to the developers.",
                                        "Unknown event triggered", JOptionPane.WARNING_MESSAGE
                                );
                            });
                            break;
                    }
                });

                this.logger.log(Level.FINE, "Share our version information with the peer.");
                this.connection.writeVersion();
                this.logger.log(Level.FINE, "Share our name with the peer.");
                final String asciiName = Utils.toAscii(this.playersName);
                final String shortName = asciiName.substring(0, Math.min(32, asciiName.length()));
                this.connection.writeIAM(shortName, String.valueOf(this.playersLevel));
                this.logger.log(Level.FINE, "Share our full name with the peer.");
                this.connection.writeIAMU(this.playersName);

                this.readThread = new Thread() {
                    @Override
                    public void run() {
                        try {
                            GameSession.this.logger.log(Level.FINE, "Ready to receive commands from the peer.");
                            while (
                                GameSession.this.connection.isConnected() && !Thread.currentThread().isInterrupted()
                            ) {
                                GameSession.this.connection.readCommand();
                            }
                        } catch (final Exception e) {
                            if (Thread.currentThread().isInterrupted()) {
                                GameSession.this.logger.log(
                                        Level.FINE,
                                        "Good error when reading a command. The reading thread is now closed."
                                );
                            } else {
                                GameSession.this.logger.log(Level.SEVERE, "Error in reading thread.", e);
                                SwingUtilities.invokeLater(() -> {
                                    JOptionPane.showMessageDialog(
                                            null, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE
                                    );
                                });
                                GameSession.this.stopGame(GameEndStatus.CONNECTION_DISCONNECTED_OR_DISTURBED);
                            }
                        }
                    }
                };
                this.readThread.start();

                this.changeTurn(TurnStatus.NOT_READY);
            }
        } catch (final Exception e) {
            this.logger.log(Level.SEVERE, "Error when preparing the game.", e);
            this.stopGame(GameEndStatus.GAME_PREPARATION_OR_START_FAILED);
        }
    }

    /**
     * Überprüft, ob das Spiel vorbereitet ist (nach IAM, vor COIN)
     *
     * @return true, wenn das Spiel vorbereitet ist, sonst false.
     */
    private boolean isPrepared() {
        synchronized (this.turnLock) {
            return this.turnstatus != TurnStatus.NOT_READY;
        }
    }

    /**
     * Überprüft, ob aktuell gespielt wird - also alle Vorbereitungen erfolgreich
     * abgeschlossen sind.
     *
     * @return true, wenn aktuell gespielt wird, sonst false.
     */
    private boolean isReady() {
        synchronized (this.turnLock) {
            return this.turnstatus != TurnStatus.NOT_READY && this.turnstatus != TurnStatus.PREPARED;
        }
    }

    /**
     * Bereitet das Spiel vor. Dies funktioniert nur nachdem das IAM-Paket empfangen
     * wurden ist und entsprechende Informationen vom Gegner empfangen wurden sind.
     */
    private void prepareGame() {
        try {
            synchronized (this.turnLock) {
                final int level = Math.min(Integer.parseInt(this.connection.getPeersLevel()), this.playersLevel);
                final int levelSize = Constants.LEVEL_SIZES[level - 1];
                this.logger.log(Level.INFO, "Level: " + level);
                this.logger.log(Level.FINE, "Level size: " + levelSize);
                this.opposing = new OpposingPlayingField(levelSize);
                this.players = new PlayersPlayingField(levelSize);

                this.logger.log(Level.FINE, "Place our ships.");
                this.players.generateShips(Constants.LEVELS.get(level - 1));
                this.logger.log(Level.FINE, "Inform our peer that our ships have been placed.");
                this.connection.writeCoin(this.myCoin);

                SwingUtilities.invokeLater(() -> {
                    this.gamewindow = new GameWindow(levelSize, levelSize);
                    this.gamewindow.writeMessageFromSystem(this.playersName + " (we) has joined the game.");
                    this.gamewindow.setMessageHandler((String text) -> {
                        SwingUtilities.invokeLater(() -> {
                            this.gamewindow.writeMessageFromUser(this.playersName, text);
                        });
                        this.connection.writeChat(text);
                    });
                    this.gamewindow.getOpponentField().addFireListener((FireEvent fireevent) -> {
                        this.attackOpponent(fireevent.getX(), fireevent.getY());
                    });
                    this.gamewindow.setWithdrawHandler(() -> {
                        try {
                            this.connection.writeWithdraw();
                        } catch (final IOException e) {
                            this.logger.log(Level.SEVERE, "Error in notifying the peer that we have withdrawn.", e);
                        }
                        this.stopGame(GameEndStatus.SUCCESSFUL_DRAW_FROM_PLAYER);
                    });
                    this.gamewindow.setComputerMoveHandler(() -> {
                        try {
                            final OpposingField f = this.opposing.getComputerMove();
                            this.logger.log(Level.FINE, "Computer move on x=" + f.getX() + " and y=" + f.getY());
                            this.attackOpponent(f.getX(), f.getY());
                        } catch (final Exception e) {
                            this.logger.log(Level.SEVERE, "Failed to calculate computer move.");
                            this.logger.log(
                                    Level.FINE,
                                    "Current opposing playing field:\n"
                                            + Utils.writerToString(pw -> this.opposing.debugPrint(pw)) + "\n"
                                            + "Fields:\n" + Utils.writerToString(pw -> this.opposing.debugPrint2(pw))
                            );
                            throw e;
                        }
                    });
                    this.logger.log(Level.FINE, "Draw ships.");
                    this.players.print(this.gamewindow.getPlayersField());
                    this.opposing.print(this.gamewindow.getOpponentField());
                    this.gamewindow
                            .writeMessageFromSystem(this.connection.getPeersName() + " (peer) has joined the game.");
                });
                this.changeTurn(TurnStatus.PREPARED);
            }
        } catch (final Exception e) {
            this.logger.log(Level.SEVERE, "Error when preparing the game.", e);
            this.stopGame(GameEndStatus.GAME_PREPARATION_OR_START_FAILED);
        }
    }

    /**
     * Verarbeitet eine empfangendes HIT-Paket, indem das Resultat eines unserer
     * Angriffe verbucht wird.
     *
     * @param x         X-Koordinate, welche wir angegriffen haben (laut HIT-Paket
     *                  beziehungsweise Gegner)
     * @param y         Y-Koordinate, welche wir angegriffen haben (laut HIT-Paket
     *                  beziehungsweise Gegner)
     * @param hitstatus Vom Gegner übermittelter Status
     */
    private void receiveAnswerToOwnAttack(int x, int y, HitStatus hitstatus) {
        synchronized (this.turnLock) {
            if (x != this.lastShoot.getX() || y != this.lastShoot.getY()) {
                this.logger
                        .log(Level.SEVERE, "The opponent thinks we have attacked a square that we have not attacked.");
                this.logger.log(
                        Level.FINE,
                        "lastShoot x=" + this.lastShoot.getX() + " y=" + this.lastShoot.getY() + " Hit x=" + x + " y="
                                + y
                );
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(
                            null,
                            "Our or the peer's instance seems to have a faulty implementation. Or the opponent is cheating. We have attacked one of the opponent's squares, but the opponent thinks we have attacked a different square. This is problematic. To continue the game, we trust the opponent and continue with the opponent's information.",
                            "The opponent thinks we have attacked a square that we have not attacked.",
                            JOptionPane.ERROR_MESSAGE
                    );
                });
            }

            switch (this.turnstatus) {
                case WAITING_FOR_REPLY_AFTER_HIT:
                    switch (hitstatus) {
                        case WATER:
                            this.opposing.hit(new OpposingField(x, y), OpposingFieldStatus.WATER);
                            this.opposing.print(this.gamewindow.getOpponentField());
                            this.changeTurn(TurnStatus.YOUR_TURN);
                            if (this.sound.get()) {
                                Sound.playWater();
                            }
                            break;

                        case HIT:
                            this.opposing.hit(new OpposingField(x, y), OpposingFieldStatus.SHIP);
                            this.opposing.print(this.gamewindow.getOpponentField());
                            this.changeTurn(TurnStatus.MY_TURN_AFTER_HIT);
                            if (this.sound.get()) {
                                Sound.playHit1();
                            }
                            break;

                        case SUNK:
                            this.opposing.hit(new OpposingField(x, y), OpposingFieldStatus.SUNK);
                            this.opposing.print(this.gamewindow.getOpponentField());
                            this.changeTurn(TurnStatus.MY_TURN_AFTER_HIT);
                            if (this.sound.get()) {
                                Sound.playHit2();
                            }
                            break;

                        case SUNK_AND_VICTORY:
                            if (this.sound.get()) {
                                Sound.playHit2();
                            }
                            this.stopGame(GameEndStatus.SUCCESSFUL_WON);
                            break;

                        default:
                            this.logger.log(Level.SEVERE, "The field we have hit has a unknown status.");
                            SwingUtilities.invokeLater(() -> {
                                JOptionPane.showMessageDialog(
                                        null,
                                        "The field we have hit has a unknown status. The game can be continued. The opponent must send a valid status code.",
                                        "Invalid status", JOptionPane.ERROR_MESSAGE
                                );
                            });
                            break;
                    }
                    break;

                default:
                    this.logger.log(Level.WARNING, "Peer has sent the result of an attack that we did not carry out.");
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(
                                null,
                                "The peer has sent a response to an attack that we never made. This is quite strange and could indicate a faulty implementation of this or the instance of the peer. The command from the peer is ignored. The game can be continued.",
                                "Invalid command", JOptionPane.ERROR_MESSAGE
                        );
                    });
                    break;
            }
        }
    }

    /**
     * Verarbeitet einen Angriff des Gegners und antwortet entsprechend mit einem
     * HIT-Paket.
     *
     * @param x X-Koordinate, welche der Gegner angreift
     * @param y Y-Koordinate, welche der Gegner angreift
     */
    private void receiveAttack(int x, int y) {
        synchronized (this.turnLock) {
            switch (this.turnstatus) {
                case YOUR_TURN:
                case YOUR_TURN_AFTER_HIT:
                    try {
                        final PlayersShip ship = this.players.hit(new PlayersField(x, y));
                        if (ship == null) {
                            /* Gegner hat Wasser getroffen */
                            this.changeTurn(TurnStatus.MY_TURN);
                            this.gamewindow.playersTurn(true);
                            this.connection.writeHit(x, y, HitStatus.WATER);
                            this.players.print(this.gamewindow.getPlayersField());
                            if (this.sound.get()) {
                                Sound.playWater();
                            }
                        } else if (ship.isSunk()) {
                            if (this.players.allSunk()) {
                                /* Schiff versenkt und gewonnen */
                                this.connection.writeHit(x, y, HitStatus.SUNK_AND_VICTORY);
                                this.players.print(this.gamewindow.getPlayersField());
                                if (this.sound.get()) {
                                    Sound.playHit2();
                                }
                                this.stopGame(GameEndStatus.SUCCESSFUL_LOST);
                            } else {
                                /* Schiff versenkt */
                                this.connection.writeHit(x, y, HitStatus.SUNK);
                                this.players.print(this.gamewindow.getPlayersField());
                                if (this.sound.get()) {
                                    Sound.playHit2();
                                }
                                this.changeTurn(TurnStatus.YOUR_TURN_AFTER_HIT);
                            }
                        } else {
                            /* Schiff getroffen */
                            this.connection.writeHit(x, y, HitStatus.HIT);
                            this.players.print(this.gamewindow.getPlayersField());
                            if (this.sound.get()) {
                                Sound.playHit1();
                            }
                            this.changeTurn(TurnStatus.YOUR_TURN_AFTER_HIT);
                        }
                    } catch (final Exception e) {
                        this.logger.log(Level.SEVERE, "Error in telling the peer about their attack.", e);
                        SwingUtilities.invokeLater(() -> {
                            JOptionPane.showMessageDialog(
                                    null,
                                    "The opponent has attacked and we wanted to inform them of the result of their attack, but an error has occurred. It is now the opponent's turn.",
                                    "Error sending the message", JOptionPane.ERROR_MESSAGE
                            );
                        });
                    }
                    break;

                default:
                    this.logger.log(Level.WARNING, "The opponent tries to attack even though it is not his turn.");
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(
                                null,
                                "The opponent tries to attack even though it is not their turn. Our implementation or that of the peer could be faulty. Or the opponent is trying to cheat. To continue the game, we ignore this command from the opponent.",
                                "Invalid command", JOptionPane.ERROR_MESSAGE
                        );
                    });
                    break;
            }
        }
    }

    /**
     * Startet das Spiel (nach einem COIN-Paket).
     *
     * @param peersCoin Die Münze des Gegners (entweder "0" oder "1").
     */
    private void startGame(String peersCoin) {
        try {
            synchronized (this.turnLock) {
                if (this.turnstatus != TurnStatus.PREPARED) {
                    this.logger.log(Level.SEVERE, "The game is to be started, although it has not yet been prepared.");
                    return;
                }

                final int peerCoinValue = Integer.parseInt(peersCoin);
                final int myCoinValue = Integer.parseInt(this.myCoin);

                final int xorResult = peerCoinValue ^ myCoinValue;

                final boolean peerStarts = this.isServer ? xorResult == 0 : xorResult == 1;

                if (peerStarts) {
                    this.logger.log(Level.INFO, "The peer starts the game.");
                } else {
                    this.logger.log(Level.INFO, "We start the game.");
                }
                this.changeTurn(peerStarts ? TurnStatus.YOUR_TURN : TurnStatus.MY_TURN);
                SwingUtilities.invokeLater(() -> {
                    this.gamewindow.show();
                });
            }
        } catch (final Exception e) {
            this.logger.log(Level.SEVERE, "Error when starting the game.", e);
            this.stopGame(GameEndStatus.GAME_PREPARATION_OR_START_FAILED);
        }
    }

    /**
     * Bricht ein Spiel zum schnellstmöglich nächstmöglichen (von Locks abhängig)
     * Zeitpunkt ab.
     *
     * @param status Status, mit welchem das Spiel abgebrochen werden soll. Dieser
     *               Wert wird später dem GameExitHandler übergeben.
     */
    private void stopGame(GameEndStatus status) {
        if (this.isRunning.compareAndSet(true, false)) {
            this.logger.log(Level.FINE, "Stop current game with status " + status + ".");

            if (this.gamewindow != null) {
                try {
                    SwingUtilities.invokeAndWait(() -> this.gamewindow.close());
                } catch (final Exception e) {
                    this.logger.log(Level.SEVERE, "Failed to close game window.", e);
                }
            }

            if (this.readThread.isAlive()) {
                this.readThread.interrupt();
            }

            try {
                if (this.connection != null) {
                    this.connection.close();
                }
            } catch (final IOException e) {
                this.logger.log(Level.SEVERE, "The connection to the peer could not be closed.", e);
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(
                            null,
                            "The connection to the peer could not be closed. This should not happen. A restart of the game is recommended. Please report the error to the developers.",
                            "Error while waiting.", JOptionPane.ERROR_MESSAGE
                    );
                });
            }

            this.myCoin = null;
            this.opposing = null;
            this.players = null;
            this.gamewindow = null;
            this.readThread = null;
            this.connection = null;

            this.logger.log(Level.INFO, "Game stopped.");

            if (this.gameexithandler == null) {
                this.logger.log(Level.WARNING, "No game exit handler.");
            } else {
                this.logger.log(Level.FINE, "Run game exit handler.");
                this.gameexithandler.handle(status);
            }
        } else {
            this.logger.log(Level.FINE, "Game already stopped.");
        }
    }
}
