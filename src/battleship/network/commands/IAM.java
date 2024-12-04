package battleship.network.commands;

import battleship.Constants;
import battleship.utils.Utils;

/**
 * Repräsentiert ein IAM-Paket
 */
public class IAM implements Command {
    /**
     * Name des Spielers, welchen das IAM-Paket übermittelt.
     */
    private final String name;
    /**
     * Level des Spielers, welches das IAM-Paket übermittelt.
     */
    private final String level;

    /**
     * Erstellt aus einem IAM-Paket in seiner genormten Übertragungsform als String
     * eine Repräsentation des Paketes.
     *
     * @param command Das IAM-Paket
     * @return Repräsentation des IAM-Pakets
     */
    public static IAM fromString(final String command) {
        final String level = Utils.getFirstWordOrLine(command);
        final String name = Utils.getStringAfterFirstSpace(command);
        return new IAM(name, level);
    }

    /**
     * Erstellt eine Repräsentation des IAM-Paketes.
     *
     * @param name  Name des Spielers
     * @param level Level des Spielers
     */
    public IAM(final String name, final String level) {
        this.name = name;
        this.level = level;
    }

    @Override
    public String getFullCommand() {
        final StringBuilder builder = new StringBuilder();
        builder.append("IAM ").append(this.level).append(' ').append(this.name).append("\r\n");

        return builder.toString();
    }

    public String getLevel() {
        return this.level;
    }

    public String getName() {
        return this.name;
    }

    @Override
    public boolean isValid() {
        if (this.name.isBlank() || this.level.isBlank() || this.name.length() > 32) {
            return false;
        }

        try {
            final int level = Integer.parseInt(this.level);
            if (level > Constants.NUMBER_OF_LEVELS) {
                return false;
            }
        } catch (final Exception e) {
            return false;
        }

        return true;
    }

    @Override
    public String toString() {
        return "IAM [name=" + this.name + ", level=" + this.level + "]";
    }
}
