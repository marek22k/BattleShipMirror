package battleship.network;

/**
 * Event, welches dem Event-Handler als Aufruf-Grund übergeben wird.
 * 
 * Wenn ein Paket empfangen wird, wird er in seine einzelnen Komponenten verlegt
 * und danach wieder zusammengesetzt. Wenn das Wieder-Zusammengesetzte anders
 * aussieht als wir es empfangen haben wird ein `NOT_WELL_STRUCTURED`-Event
 * aufgerufen.
 * 
 * Wenn ein Paket die Protokoll-Spezifikation verletzt wird ein `INVALID`-Event
 * aufgerufen. `INVALID`-Events gibt es lediglich für nicht essenzielle Pakete
 * wie CHAT, was ein optionales Zusatzpaket ist.
 */
public enum ConnectionEvent {
    /**
     * Ein unbekannter Befehl wurde empfangen.
     */
    UNKNOWN_COMMAND_RECEIVED,

    /**
     * Ein VERSION-Paket wurde erfolgreich empfangen.
     */
    VERSION_COMMAND_RECEIVED,

    /**
     * Ein VERSION-Paket wurde erfolgreich gesendet.
     */
    VERSION_COMMAND_SENT,

    /**
     * Ein empfangenes VERSION-Paket ist anders aufgebaut als erwartet.
     */
    VERSION_COMMAND_NOT_WELL_STRUCTURED,

    /**
     * Ein IAM-Paket wurde erfolgreich empfangen.
     */
    IAM_COMMAND_RECEIVED,

    /**
     * Ein IAM-Paket wurde erfolgreich gesendet.
     */
    IAM_COMMAND_SENT,

    /**
     * Ein empfangenes IAM-Paket ist anders aufgebaut als erwartet.
     */
    IAM_COMMAND_NOT_WELL_STRUCTURED,

    /**
     * Ein IAMU-Paket wurde erfolgreich empfangen.
     */
    IAMU_COMMAND_RECEIVED,

    /**
     * Ein IAMU-Paket wurde erfolgreich gesendet.
     */
    IAMU_COMMAND_SENT,

    /**
     * Ein empfangenes IAMU-Paket ist invalid.
     */
    IAMU_COMMAND_INVALID,

    /**
     * Ein empfangenes IAMU-Paket ist anders aufgebaut als erwartet.
     */
    IAMU_COMMAND_NOT_WELL_STRUCTURED,

    /**
     * Ein COIN-Paket wurde erfolgreich empfangen.
     * 
     * Als Event-Objekt wird die vom Peer übermittelte Müunze als String übergeben.
     */
    COIN_COMMAND_RECEIVED,

    /**
     * Ein COIN-Paket wurde erfolgreich gesendet.
     */
    COIN_COMMAND_SENT,

    /**
     * Ein empfangenes COIN-Paket ist anders aufgebaut als erwartet.
     */
    COIN_COMMAND_NOT_WELL_STRUCTURED,

    /**
     * Ein SHOOT-Paket wurde erfolgreich empfangen.
     * 
     * Als Event-Objekt wird der Shoot-Befehl des Peers übergeben. Auf die
     * Koordinaten kann mit `getX()` und `getY()` zugegriffen werden.
     */
    SHOOT_COMMAND_RECEIVED,

    /**
     * Ein SHOOT-Paket wurde erfolgreich gesendet.
     */
    SHOOT_COMMAND_SENT,

    /**
     * Ein empfangenes SHOOT-Paket ist anders aufgebaut als erwartet.
     */
    SHOOT_COMMAND_NOT_WELL_STRUCTURED,

    /**
     * Ein HIT-Paket wurde erfolgreich empfangen.
     * 
     * Als Event-Objekt wird der Hit-Befehl des Peers übergeben. Auf die
     * Koordinaten kann mit `getX()` und `getY()` zugegriffen werden und auf den
     * Status mit `getHitStatus()`.
     */
    HIT_COMMAND_RECEIVED,

    /**
     * Ein HIT-Paket wurde erfolgreich gesendet.
     */
    HIT_COMMAND_SENT,

    /**
     * Ein empfangenes HIT-Paket ist anders aufgebaut als erwartet.
     */
    HIT_COMMAND_NOT_WELL_STRUCTURED,

    /**
     * Ein CHAT-Paket wurde erfolgreich empfangen.
     * 
     * Als Event-Objekt wird die übermittelte Nachricht als String übergeben.
     */
    CHAT_COMMAND_RECEIVED,

    /**
     * Ein CHAT-Paket wurde erfolgreich gesendet.
     */
    CHAT_COMMAND_SENT,

    /**
     * Ein empfangenes CHAT-Paket ist anders aufgebaut als erwartet.
     */
    CHAT_COMMAND_NOT_WELL_STRUCTURED,

    /**
     * Ein empfangenes CHAT-Paket ist invalid.
     */
    CHAT_COMMAND_INVALID,

    /**
     * Ein WITHDRAW-Paket wurde erfolgreich empfangen.
     */
    WITHDRAW_COMMAND_RECEIVED,

    /**
     * Ein WITHDRAW-Paket wurde erfolgreich gesendet.
     */
    WITHDRAW_COMMAND_SENT,

    /**
     * Ein empfangenes WITHDRAW-Paket ist anders aufgebaut als erwartet.
     */
    WITHDRAW_COMMAND_NOT_WELL_STRUCTURED,

    /**
     * Es wurde eine "leere" Zeile empfangen. Dies deutet auf Verbindungs-Probleme
     * hin.
     */
    NULL_LINE_RECEIVED,

    /**
     * Die Verbindung wurde getrennt, obwohl in der gleichen Zeit auf ein Befehl des
     * Peers gewartet wurde.
     */
    DISCONNECTED_WHILE_WAITING_FOR_COMMAND
}
