package battleship.network.commands;

/**
 * Repräsentiert ein CHAT-Paket
 */
public class Chat implements Command {
    /**
     * Nachricht, die übertragen wird
     */
    private final String message;

    /**
     * Erstellt aus einem CHAT-Paket in seiner genormten Übertragungsform als String
     * eine Repräsentation des Paketes.
     *
     * @param command Das CHAT-Paket
     * @return Repräsentation des CHAT-Pakets
     */
    public static Chat fromString(final String command) {
        return new Chat(command.strip());
    }

    /**
     * Erstellt eine Repräsentation des CHAT-Paketes
     *
     * @param message Die Nachricht, welche das Paket übermittelt.
     */
    public Chat(final String message) {
        this.message = message;
    }

    @Override
    public String getFullCommand() {
        final StringBuilder builder = new StringBuilder();
        builder.append("CHAT ").append(this.message).append("\r\n");

        return builder.toString();
    }

    public String getMessage() {
        return this.message;
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public String toString() {
        return "Chat [message.length=" + message.length() + "]";
    }
}
