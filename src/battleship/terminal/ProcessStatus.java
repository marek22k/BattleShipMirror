package battleship.terminal;

/**
 * Repräsentiert den Status eines Prozesses und damit indirekt, welche
 * GUI-Element zur Verfügung stehen sollen.
 */
public enum ProcessStatus {
    /**
     * Nicht bereit
     */
    NOT_READY,

    /**
     * Process gestartet
     */
    RUNNING,

    /**
     * Warte bis der Prozess beendet ist
     */
    WAITING_FOR_END,

    /**
     * Es läuft kein Process
     */
    STOPPED
}
