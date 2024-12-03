package battleship.network.commands;

/**
 * Repräsentiert ein IAMU-Paket
 */
public class IAMU implements Command {
    /**
     * Name des Spielers, welchen das IAMU-Paket übermittelt.
     */
    private final String name;

    /**
     * Erstellt aus einem IAMU-Paket in seiner genormten Übertragungsform als String
     * eine Repräsentation des Paketes.
     *
     * @param command Das IAMU-Paket
     * @return Repräsentation des IAMU-Pakets
     */
    public static IAMU fromString(final String command) {
        final String name = command.strip();
        return new IAMU(name);
    }

    /**
     * Erstellt eine Repräsentation des IAMU-Paketes.
     *
     * @param name Name des Spielers
     */
    public IAMU(final String name) {
        this.name = name;
    }

    @Override
    public String getFullCommand() {
        final StringBuilder builder = new StringBuilder();
        builder.append("IAMU ");

        builder.append(this.name);

        builder.append("\r\n");
        return builder.toString();
    }

    public String getName() {
        return this.name;
    }

    @Override
    public boolean isValid() {
        if (this.name.isBlank()) {
            return false;
        }

        return true;
    }
}
