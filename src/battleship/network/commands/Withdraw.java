package battleship.network.commands;

/**
 * Repräsentiert ein WITHDRAW-Paket
 */
public class Withdraw implements Command {
    /**
     * Erstellt aus einem WITHDRAW-Paket in seiner genormten Übertragungsform als
     * String eine Repräsentation des Paketes.
     *
     * @param command Das WITHDRAW-Paket
     * @return Repräsentation des WITHDRAW-Pakets
     */
    public static Withdraw fromString(final String command) {
        if (!command.isBlank()) {
            throw new RuntimeException("Withdraw contains message.");
        }
        return new Withdraw();
    }

    @Override
    public String getFullCommand() {
        return "WITHDRAW\r\n";
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public String toString() {
        return "Withdraw []";
    }
}
