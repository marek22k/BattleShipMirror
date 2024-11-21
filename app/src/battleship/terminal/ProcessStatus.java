package battleship.terminal;

public enum ProcessStatus {
    NOT_READY, /* Nicht bereit */
    RUNNING, /* Process gestartet */
    WAITING_FOR_END, /* Warte bis der Prozess beendet ist */
    STOPPED /* Es läuft kein Process */
}
