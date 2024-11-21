package battleship.network.commands;

public interface Command {
    public String getFullCommand();

    public boolean isValid();
}
