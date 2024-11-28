package battleship.network.commands;

/**
 * Repräsentiert einen Spiele-Befehl.
 */
public interface Command {
    /**
     * Liefert den Befehl in seiner genormten Übertragungsform als String
     *
     * @return Der Befehl als String
     */
    public String getFullCommand();

    /**
     * Überprüft, ob der Befehl die Protokoll-Spezifikation einhält.
     *
     * @return true, wenn der Befehl die Protokoll-Spezifikation einhält und damit
     *         valide ist, sonst false.
     */
    public boolean isValid();
}
