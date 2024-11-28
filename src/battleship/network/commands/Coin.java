package battleship.network.commands;

/**
 * Repräsentiert ein COIN-Paket
 */
public class Coin implements Command {
    /**
     * Geworfene Münze
     */
    private final String coin;

    /**
     * Erstellt aus einem COIN-Paket in seiner genormten Übertragungsform als String
     * eine Repräsentation des Paketes.
     *
     * @param command Das COIN-Paket
     * @return
     */
    public static Coin fromString(String command) {
        final String coin = command.strip();
        return new Coin(coin);
    }

    /**
     * Erstellt eine Repräsentation des CHAT-Paketes.
     *
     * @param coin Die geworfene Münze (`"0"` oder `"1"`) als String.
     */
    public Coin(String coin) {
        this.coin = coin;
    }

    /**
     * Gibt die geworfene Münze zurück.
     *
     * @return Die geworfene Münze
     */
    public String getCoin() {
        return this.coin;
    }

    @Override
    public String getFullCommand() {
        final StringBuilder builder = new StringBuilder();
        builder.append("COIN ");

        builder.append(this.coin);

        builder.append("\r\n");
        return builder.toString();
    }

    @Override
    public boolean isValid() {
        if ("0".equals(this.coin) || "1".equals(this.coin)) {
            return true;
        }
        return false;
    }

}
