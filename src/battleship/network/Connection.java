package battleship.network;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

import battleship.Constants;
import battleship.network.commands.Chat;
import battleship.network.commands.Coin;
import battleship.network.commands.Command;
import battleship.network.commands.Hit;
import battleship.network.commands.HitStatus;
import battleship.network.commands.IAM;
import battleship.network.commands.IAMU;
import battleship.network.commands.Shoot;
import battleship.network.commands.Version;
import battleship.network.commands.Withdraw;
import battleship.utils.Utils;

public final class Connection {
    private final Socket socket;
    private final BufferedWriter writer;
    private final BufferedReader reader;
    private String peerImplementation;
    private String peersName;
    private String peersUnicodeName;
    private String peersLevel;
    private EventHandler eventhandler;
    private final Logger logger;

    /**
     * Baut eine Verbindung als Client zu einem Server auf.
     *
     * @param host Hostname des Servers
     * @param port Port des Servers
     * @return Liefert eine Spiele-Verbindung zurück
     * @throws IOException
     */
    public static Connection connectTo(final String host, final int port) throws IOException {
        final Logger logger = Logger.getLogger(Connection.class.getName());
        logger.setLevel(Constants.LOG_LEVEL);

        logger.log(Level.INFO, () -> "Connect to " + host + ":" + port);
        final Socket socket = new Socket(host, port);
        logger.log(Level.INFO, "Connected.");
        return new Connection(socket);
    }

    /**
     * Umhüllt eine Verbindung zu einer anderen Partei in eine Spiele-Verbindung.
     * Eine Spiele-Verbindung implementiert spielspezifische Funktionen zum Senden
     * und Empfangen von Spiel-Paketen.
     *
     * @param socket Socket, welcher als Grundlage für diese Verbindung genutzt
     *               werden soll.
     * @throws IOException
     */
    public Connection(final Socket socket) throws IOException {
        this.logger = Logger.getLogger(Connection.class.getName());
        this.logger.setLevel(Constants.LOG_LEVEL);

        this.socket = socket;
        this.writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
        this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
    }

    /**
     * Schließt die Spiele-Verbindung mit dem darunterliegenden Socket sowie die
     * Reader- und Writer des Sockets.
     *
     * @throws IOException
     */
    public void close() throws IOException {
        this.socket.close();
        this.reader.close();
        this.writer.close();
    }

    /**
     * Gibt die Implementierung zurück, welche der Peer verwendet. Diese wurden
     * vom VERSION-Paket des Peers ermittelt.
     *
     * @return Die Implementierung als String
     */
    public String getPeerImplementation() {
        return this.peerImplementation;
    }

    /**
     * Gibt das vom Peer übermittelte Level zurück. Dieses wurde aus dem IAM-Paket
     * des Peers ermittelt.
     *
     * @return Das Level des Peers als String
     */
    public String getPeersLevel() {
        return this.peersLevel;
    }

    /**
     * Gibt den Spielernamen des Peers zurück. Dieser sollte nur aus ASCII-Zeichen
     * bestehen und maximal 32-Zeichen lang sein.
     *
     * @return Der Spielername des Peers als String
     */
    public String getPeersName() {
        return this.peersName;
    }

    /**
     * Gibt den Spielernamen des Peers zurück. Dieser darf beliebig lang sein und
     * Unicode Zeichen beinhalten.
     *
     * @return Der Spielername des Peers als String
     */
    public String getPeersUnicodeName() {
        return this.peersUnicodeName;
    }

    /**
     * Überprüft, ob immernoch eine Verbindung zum Peer besteht.
     *
     * @return true, wenn eine Verbindung besteht, sonst false
     */
    public boolean isConnected() {
        return this.socket.isConnected() && !this.socket.isClosed();
    }

    /**
     * Liest einen Befehl vom Socket. Überprüft und validiert diesen. Einige Daten
     * von bestimmten Paketen werden extrahiert und gespeichert. Ruft nach jedem
     * empfangenden Paket den Event Handler auf. Wenn ein essenzielles Paket nicht
     * valide ist wird eine Ausnahme geworfen. Blockiert, wenn kein Befehl zum
     * Auslesen zur Verfügung steht.
     *
     * @throws IOException
     */
    public void readCommand() throws IOException {
        final String line = this.reader.readLine();

        /*
         * Wenn eine "leere" Zeile empfangen wird deutet dies auf Verbindungsprobleme
         * hin.
         */
        if (line == null) {
            if (this.isConnected()) {
                this.runEventHandler(ConnectionEvent.NULL_LINE_RECEIVED);
            } else {
                this.runEventHandler(ConnectionEvent.DISCONNECTED_WHILE_WAITING_FOR_COMMAND);
            }
            throw new RuntimeException("Null line received.");
        }

        /*
         * Der eigentliche Befehle (ohne Paramater) ist das erste Wort (Zeichen bis zum
         * ersten Leerzeichen)
         */
        final String keyword = Utils.getFirstWordOrLine(line).strip();
        /* Die Parameter des Befehls folgen darauf */
        final String payload = Utils.getStringAfterFirstSpace(line);
        this.logger.log(Level.FINE, () -> "Receive command from peer: " + Utils.sanitizeString(keyword.strip()));

        /*
         * Wenn ein erforderliches Paket invalid ist, wird ein Fehler geworfen, sonst
         * ein Event ausgelöst.
         */
        switch (keyword) {
            case "VERSION":
                final Version version = Version.fromString(payload);

                if (!version.isValid()) {
                    throw new RuntimeException("The peer's version packet appears invalid.");
                }

                if (version.getFullCommand().equals(line)) {
                    this.logger.log(Level.WARNING, "The structure of the peer's command differs from ours.");
                    this.runEventHandler(ConnectionEvent.VERSION_COMMAND_NOT_WELL_STRUCTURED);
                } else {
                    this.logger.log(Level.FINE, "The peer's command seems to be well structured.");
                }

                if (!version.hasVersion(Constants.PROTOCOL_VERSION)) {
                    throw new RuntimeException(
                            "The peer does not support the same version as we do. A connection is therefore not possible."
                    );
                }
                this.peerImplementation = version.getImplementation();
                this.logger.log(
                        Level.INFO, () -> "Peers implementation: " + Utils.sanitizeString(this.peerImplementation)
                );

                this.runEventHandler(ConnectionEvent.VERSION_COMMAND_RECEIVED);
                break;

            case "IAM":
                final IAM iam = IAM.fromString(payload);

                if (!iam.isValid()) {
                    throw new RuntimeException("The peer's IAM packet appears invalid.");
                }

                if (iam.getFullCommand().equals(line)) {
                    this.logger.log(Level.WARNING, "The structure of the peer's command differs from ours.");
                    this.runEventHandler(ConnectionEvent.IAM_COMMAND_NOT_WELL_STRUCTURED);
                } else {
                    this.logger.log(Level.FINE, "The peer's command seems to be well structured.");
                }

                this.peersName = Utils.sanitizeString(iam.getName());
                this.peersLevel = Utils.sanitizeString(iam.getLevel());
                this.logger.log(Level.FINE, () -> "Peers name: " + Utils.sanitizeString(this.peersName));
                this.logger.log(Level.FINE, () -> "Peers level: " + Utils.sanitizeString(this.peersLevel));

                this.runEventHandler(ConnectionEvent.IAM_COMMAND_RECEIVED);
                break;

            case "IAMU":
                final IAMU iamu = IAMU.fromString(payload);

                if (!iamu.isValid()) {
                    this.logger.log(Level.SEVERE, "The peer's IAMU packet appears invalid.");
                    this.runEventHandler(ConnectionEvent.IAMU_COMMAND_INVALID);
                }

                if (iamu.getFullCommand().equals(line)) {
                    this.logger.log(Level.WARNING, "The structure of the peer's command differs from ours.");
                    this.runEventHandler(ConnectionEvent.IAMU_COMMAND_NOT_WELL_STRUCTURED);
                } else {
                    this.logger.log(Level.FINE, "The peer's command seems to be well structured.");
                }

                this.peersUnicodeName = Utils.sanitizeString(iamu.getName());
                this.logger.log(Level.FINE, () -> "Peers unicode name: " + Utils.sanitizeString(this.peersUnicodeName));

                this.runEventHandler(ConnectionEvent.IAMU_COMMAND_RECEIVED);
                break;

            case "COIN":
                final Coin coin = Coin.fromString(payload);

                if (!coin.isValid()) {
                    throw new RuntimeException("The peer's COIN packet appears invalid.");
                }

                if (coin.getFullCommand().equals(line)) {
                    this.logger.log(Level.WARNING, "The structure of the peer's command differs from ours.");
                    this.runEventHandler(ConnectionEvent.COIN_COMMAND_NOT_WELL_STRUCTURED);
                } else {
                    this.logger.log(Level.FINE, "The peer's command seems to be well structured.");
                }

                this.logger.log(Level.FINE, "Peers coin: {0}", coin); // Da validiert, ist keine Reinigung des Strings
                                                                      // erforderlich

                this.runEventHandler(ConnectionEvent.COIN_COMMAND_RECEIVED, coin.getCoin());
                break;

            case "SHOOT":
                final Shoot shoot = Shoot.fromString(payload);

                if (!shoot.isValid()) {
                    throw new RuntimeException("The peer's SHOOT packet appears invalid.");
                }

                if (shoot.getFullCommand().equals(line)) {
                    this.logger.log(Level.WARNING, "The structure of the peer's command differs from ours.");
                    this.runEventHandler(ConnectionEvent.SHOOT_COMMAND_NOT_WELL_STRUCTURED);
                } else {
                    this.logger.log(Level.FINE, "The peer's command seems to be well structured.");
                }

                this.logger.log(Level.FINE, "Shoot: {0}", shoot);

                this.runEventHandler(ConnectionEvent.SHOOT_COMMAND_RECEIVED, shoot);
                break;

            case "HIT":
                final Hit hit = Hit.fromString(payload);

                if (!hit.isValid()) {
                    throw new RuntimeException("The peer's HIT packet appears invalid.");
                }

                if (hit.getFullCommand().equals(line)) {
                    this.logger.log(Level.WARNING, "The structure of the peer's command differs from ours.");
                    this.runEventHandler(ConnectionEvent.HIT_COMMAND_NOT_WELL_STRUCTURED);
                } else {
                    this.logger.log(Level.FINE, "The peer's command seems to be well structured.");
                }

                this.logger.log(Level.FINE, "Hit: {0}", hit);

                this.runEventHandler(ConnectionEvent.HIT_COMMAND_RECEIVED, hit);
                break;

            case "CHAT":
                final Chat chat = Chat.fromString(payload);

                if (!chat.isValid()) {
                    throw new RuntimeException("The peer's CHAT packet appears invalid.");
                }

                if (chat.getFullCommand().equals(line)) {
                    this.logger.log(Level.WARNING, "The structure of the peer's command differs from ours.");
                    this.runEventHandler(ConnectionEvent.CHAT_COMMAND_NOT_WELL_STRUCTURED);
                } else {
                    this.logger.log(Level.FINE, "The peer's command seems to be well structured.");
                }

                this.logger.log(Level.FINE, "Chat message: {0}", chat);

                this.runEventHandler(ConnectionEvent.CHAT_COMMAND_RECEIVED, chat.getMessage());
                break;

            case "WITHDRAW":
                final Withdraw withdraw = Withdraw.fromString(payload);

                if (!withdraw.isValid()) {
                    throw new RuntimeException("The peer's WITHDRAW packet appears invalid.");
                }

                if (withdraw.getFullCommand().equals(line)) {
                    this.logger.log(Level.WARNING, "The structure of the peer's command differs from ours.");
                    this.runEventHandler(ConnectionEvent.WITHDRAW_COMMAND_NOT_WELL_STRUCTURED);
                } else {
                    this.logger.log(Level.FINE, "The peer's command seems to be well structured.");
                }

                this.runEventHandler(ConnectionEvent.WITHDRAW_COMMAND_RECEIVED);
                break;

            default:
                this.runEventHandler(ConnectionEvent.UNKNOWN_COMMAND_RECEIVED);
                break;
        }
    }

    /**
     * Setzte den EventHandler, welcher aufgerufen wird, wenn ein Ereignis passiert.
     * Ein Ereignis kann dabei der Empfang oder das Senden eines Paketes sein (siehe
     * ConnectionEvent).
     *
     * @param eventhandler Der Event-Handler. Falls bereits einer gesetzt ist, wird
     *                     dieser überschrieben.
     */
    public void setEventHandler(final EventHandler eventhandler) {
        this.eventhandler = eventhandler;
    }

    /**
     * Sendet ein Chat-Paket.
     *
     * @param message Die Nachricht, welche das Chat-Paket transportieren soll.
     * @throws IOException
     */
    public void writeChat(final String message) throws IOException {
        this.logger.log(Level.FINE, "Send chat to peer.");
        final Chat chat = new Chat(message);
        this.write(chat);
        this.runEventHandler(ConnectionEvent.CHAT_COMMAND_SENT);
    }

    /**
     * Sendet ein COIN-Paket.
     *
     * @param coinValue Die geworfene Münze, welche gesendet werden soll.
     * @throws IOException
     */
    public void writeCoin(final String coinValue) throws IOException {
        final Coin coin = new Coin(coinValue);
        this.logger.log(Level.FINE, "Send coin {0} to peer.", coin);
        this.write(coin);
        this.runEventHandler(ConnectionEvent.COIN_COMMAND_SENT);
    }

    /**
     * Sendet ein HIT-Paket.
     *
     * @param x         X-Koordinate, welche übermittelt werden soll (beginnend bei
     *                  0).
     * @param y         Y-Koordinate, welche übermittelt werden soll (beginnend bei
     *                  0).
     * @param hitstatus Der Status, welcher übermittelt werden soll. Wenn ein
     *                  unbekannter Status übergeben wird, wird "-1" als Status-Code
     *                  verwendet.
     * @throws IOException
     */
    public void writeHit(final int x, final int y, final HitStatus hitstatus) throws IOException {
        final Hit hit = new Hit(x, y, hitstatus);
        this.logger.log(Level.FINE, "Send hit {0} to peer.", hit);
        this.write(hit);
        this.runEventHandler(ConnectionEvent.HIT_COMMAND_SENT);
    }

    /**
     * Sendet ein IAM-Paket.
     *
     * Der Name wird nicht validiert. Er darf nur aus ASCII-Zeichen bestehen und
     * maximal 32-Zeichen lang sein.
     *
     * @param name  Name des Spielers, welcher übermittelt werden soll.
     * @param level Gewünschtes Level des Spielers, welches übermittelt werden soll.
     * @throws IOException
     */
    public void writeIAM(final String name, final String level) throws IOException {
        this.logger.log(Level.FINE, "Send IAM to peer.");
        final IAM iam = new IAM(name, level);
        this.write(iam);
        this.runEventHandler(ConnectionEvent.IAM_COMMAND_SENT);
    }

    /**
     * Sendet ein IAMU-Paket.
     *
     * @param name Name (darf Unicode-Zeichen beinhalten und beliebig lang sein),
     *             welcher übermittelt werden soll.
     * @throws IOException
     */
    public void writeIAMU(final String name) throws IOException {
        this.logger.log(Level.FINE, "Send IAMU to peer.");
        final IAMU iamu = new IAMU(name);
        this.write(iamu);
        this.runEventHandler(ConnectionEvent.IAMU_COMMAND_SENT);
    }

    /**
     * Sendet ein SHOOT-Paket.
     *
     * @param x X-Koordinate, welche übermittelt werden soll.
     * @param y Y-Koordinate, welche übermittelt werden soll.
     * @throws IOException
     */
    public void writeShoot(final int x, final int y) throws IOException {
        final Shoot shoot = new Shoot(x, y);
        this.logger.log(Level.FINE, "Send shoot {0} to peer.", shoot);
        this.write(shoot);
        this.runEventHandler(ConnectionEvent.SHOOT_COMMAND_SENT);
    }

    /**
     * Sendet ein VERSION-Paket.
     *
     * Die aktuelle Version bestehend aus dem Name der verwendeten Implementierung
     * sowie die unterstützt Protokoll-Version wird aus `Constants` gelesen.
     *
     * @throws IOException
     */
    public void writeVersion() throws IOException {
        this.logger.log(Level.FINE, "Send version to peer.");
        final Version version = new Version(Constants.IMPLEMENTATION, Constants.PROTOCOL_VERSION);
        this.write(version);
        this.runEventHandler(ConnectionEvent.VERSION_COMMAND_SENT);
    }

    /**
     * Sendet ein WITHDRAW-Paket.
     *
     * @throws IOException
     */
    public void writeWithdraw() throws IOException {
        this.logger.log(Level.FINE, "Send withdraw to peer.");
        final Withdraw withdraw = new Withdraw();
        this.write(withdraw);
        this.runEventHandler(ConnectionEvent.WITHDRAW_COMMAND_SENT);
    }

    /**
     * Führt den Event-Handler, falls vorhanden, mit einem bestimmten Event aus. Als
     * Event-Objekt wird `null` übergeben.
     *
     * @param event Das Event
     */
    private void runEventHandler(final ConnectionEvent event) {
        if (this.eventhandler != null) {
            this.eventhandler.handle(event, null);
        }
    }

    /**
     * Führt den Event-Handler, falls vorhanden, mit einem bestimmten Event aus und
     * übergibt ihm ein Objekt.
     *
     * @param event       Das Event
     * @param eventObject Das Event-Objekt, was an den Handler übergeben wird.
     */
    private void runEventHandler(final ConnectionEvent event, final Object eventObject) {
        if (this.eventhandler != null) {
            this.eventhandler.handle(event, eventObject);
        }
    }

    /**
     * Sendet einen Befehl.
     *
     * @param command Der Befehl, welcher gesendet werden soll.
     * @throws IOException
     */
    private void write(final Command command) throws IOException {
        this.writer.write(command.getFullCommand());
        this.writer.flush();
    }
}
