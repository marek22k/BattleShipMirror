package battleship.network.commands;

/**
 * Repräsentiert ein COIN-Paket
 */
public class Coin implements Command {
    /**
     * Geworfene Münze
     */
    private final String flippedCoin;

    /**
     * Erstellt aus einem COIN-Paket in seiner genormten Übertragungsform als String
     * eine Repräsentation des Paketes.
     *
     * @param command Das COIN-Paket
     * @return Repräsentation des COIN-Pakets
     */
    public static Coin fromString(final String command) {
        final String coin = command.strip();
        return new Coin(coin);
    }

    /**
     * Erstellt eine Repräsentation des CHAT-Paketes.
     *
     * @param coin Die geworfene Münze (`"0"` oder `"1"`) als String.
     */
    public Coin(final String coin) {
        this.flippedCoin = coin;
    }

    /**
     * Gibt die geworfene Münze zurück.
     *
     * @return Die geworfene Münze
     */
    public String getCoin() {
        return this.flippedCoin;
    }

    @Override
    public String getFullCommand() {
        final StringBuilder builder = new StringBuilder();
        builder.append("COIN ").append(this.flippedCoin).append("\r\n");

        return builder.toString();
    }

    @Override
    public boolean isValid() {
        return "0".equals(this.flippedCoin) || "1".equals(this.flippedCoin);
    }

    @Override
    public String toString() {
        return "Coin [flippedCoin=" + this.flippedCoin + "]";
    }
}
