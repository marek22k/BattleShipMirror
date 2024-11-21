package battleship.network.commands;

public class Withdraw implements Command {
    public static Withdraw fromString(String command) {
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
}
