package battleship.network.commands;

public class Coin implements Command {
    private final String coin;

    public static Coin fromString(String command) {
        final String coin = command.strip();
        return new Coin(coin);
    }

    public Coin(String coin) {
        this.coin = coin;
    }

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
